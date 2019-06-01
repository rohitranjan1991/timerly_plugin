package com.timerly.timerlyplugin.managers

import android.app.NotificationManager
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.timerly.timerlyplugin.TimerlyForegroundService
import com.timerly.timerlyplugin.Utils
import com.timerly.timerlyplugin.Utils.formatSeconds
import com.timerly.timerlyplugin.models.*
import com.timerly.timerlyplugin.services.ITimerTickCallback
import com.timerly.timerlyplugin.services.TimerService
import io.flutter.app.FlutterActivity
import io.flutter.plugin.common.EventChannel

object TimerManager {

    private val timers: MutableMap<Int, Timer> = mutableMapOf()
    private var eventSink: EventChannel.EventSink? = null


    fun setEventSink(es: EventChannel.EventSink?) {
        eventSink = es
    }

    fun unsetEventSink() {
        eventSink = null
    }

    /**
     * adds a new Timer
     */
    fun addNewTimer(timer: Timer?) {
        if (timer == null) {
            Log.d("TimerManager", "Add Timer Failed. Timer Object null")
            return
        }
        Log.d("TimerManager", "Timer Added with Id: " + timer.id)
        timers.put(timer.id, timer)
    }

    /**
     * starts the timer with the passed in Id
     */
    fun startTimer(id: Int, activity: FlutterActivity) {
        Log.d("TimerManager", "START TIMER: Looking for Timer with Id: " + id)
        if (timers.containsKey(id)) {
            Log.d("TimerManager", "START TIMER: Starting Timer with Id: " + id)
            val timer = timers.get(id)

            val request = CreateForegroundServiceRequest(timer!!.id, timer!!.name, "Timer Started", timer!!.name, "Timer Started", false, NotificationManager.IMPORTANCE_HIGH, true, "Timer Notifications", "245698", listOf(NotificationActionButton(timer!!.id, "Lap", "LAP"), NotificationActionButton(timer!!.id, "Stop", "STOP")))

            val intent = Intent(activity, TimerlyForegroundService::class.java)
            intent.putExtra("data", Gson().toJson(request))
            intent.action = TimerlyForegroundService.ACTION_ADD_NOTIFICATION
            activity.startService(intent)

            TimerService.addTimerCallback(timer!!.id, object : ITimerTickCallback {
                override fun onTimerTick() {
                    timer.currentTime += 1
                    Log.d("TimerManager", "TIMER UPDATE: Updated Timer Value with Id: " + timer.id + " with current time : " + timer.currentTime)
                    val request1 = CreateForegroundServiceRequest(timer!!.id, timer!!.name, formatSeconds(timer.currentTime), timer!!.name, "Timer Started", false, NotificationManager.IMPORTANCE_LOW, true, "Timer Notifications", "245698", listOf(NotificationActionButton(timer!!.id, "Lap", "LAP"), NotificationActionButton(timer!!.id, "Stop", "STOP")))
                    val intent1 = Intent(activity, TimerlyForegroundService::class.java)
                    intent1.putExtra("data", Gson().toJson(request1))
                    intent1.action = TimerlyForegroundService.ACTION_UPDATE_NOTIFICATION
                    activity.startService(intent1)
                    eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(timer.id, "UPDATE_TIMER_VALUE", Utils.gson.toJson(timer))))
                }
            })
        }
    }

    /**
     * stops the timer
     */
    fun stopTimer(id: Int, activity: FlutterActivity) {
        Log.d("TimerManager", "STOP TIMER: Stoping Timer with Id: " + id)
        if (timers.containsKey(id)) {
            val timer = timers.get(id);
            TimerService.removeTimerCallback(id)
            val intent = Intent(activity, TimerlyForegroundService::class.java)
            intent.putExtra("data", Gson().toJson(RemoveNotificationRequest(id)))
            intent.action = TimerlyForegroundService.ACTION_REMOVE_NOTIFICATION
            activity.startService(intent)
            eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(id, "UPDATE_TIMER_VALUE", Utils.gson.toJson(timer))))
        }
    }

    /**
     * removes the Timer with the passed in Id
     */
    fun removeTimer(id: Int) {
        Log.d("TimerManager", "REMOVE TIMER: Removing Timer with Id: " + id)
        if (timers.containsKey(id)) {
            timers.remove(id)
        }
        if (timers.isEmpty()) {
            TimerService.removeTimerCallback(id)
        }
    }

    /**
     * adds the lap to the current Timer
     */
    fun lapTimer(id: Int) {
        Log.d("TimerManager", "LAP TIMER: Timer with Id: " + id)
        if (timers.containsKey(id)) {
            val timer = timers.get(id)!!
            timer!!.laps!!.add(Lap(timer!!.laps!!.size + 1, timer.currentTime))
        }
    }

    /**
     * resets the current timer
     */
    fun resetTimer(id: Int, activity: FlutterActivity): Timer? {
        Log.d("TimerManager", "RESET TIMER: Timer with Id: " + id)
        if (timers.containsKey(id)) {
            stopTimer(id, activity)
            val timer = timers.get(id);
            timer!!.currentTime = 0
            timer.laps!!.clear()
            return timer
        }
        return null
    }

    /**
     * updates the timer
     * @param timer: The timer Object Instance
     */
    fun updateTimer(timer: Timer?) {
        if (timer == null) {
            Log.d("TimerManager", "UPDATE TIMER: Null Timer")
            return
        }
        Log.d("TimerManager", "UPDATE TIMER: Looking for Timer with Id: " + timer.id)
        if (timers.containsKey(timer.id)) {
            timers.put(timer.id, timer)
        }
    }

    /**
     * processes Notification Button Callback
     */
    fun processNotificationCallback(eventSink: EventChannel.EventSink?, timerlyTimerEvent: TimerlyTimerEvent, activity: FlutterActivity) {
        if (timers.containsKey(timerlyTimerEvent.id)) {
            val timer = timers.get(timerlyTimerEvent.id)
            when (timerlyTimerEvent.command) {
                "STOP" -> {
                    stopTimer(timerlyTimerEvent.id, activity)
                }
                "LAP" -> {
                    lapTimer(timerlyTimerEvent.id)
                }
                "REMOVE" -> {
                    removeTimer(timerlyTimerEvent.id)
                }
            }
            eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(timerlyTimerEvent.id, timerlyTimerEvent.command, Utils.gson.toJson(timer))))
        }
    }

}