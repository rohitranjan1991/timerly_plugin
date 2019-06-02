package com.timerly.timerlyplugin.managers

import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.timerly.timerlyplugin.TimerlyForegroundService
import com.timerly.timerlyplugin.Utils
import com.timerly.timerlyplugin.Utils.createLocalNotification
import com.timerly.timerlyplugin.Utils.formatSeconds
import com.timerly.timerlyplugin.models.*
import com.timerly.timerlyplugin.services.ITimerTickCallback
import com.timerly.timerlyplugin.services.TimerService
import io.flutter.app.FlutterActivity
import io.flutter.plugin.common.EventChannel

object TimerManager {

    private val timers: MutableMap<Int, Timer> = mutableMapOf()
    private var eventSink: EventChannel.EventSink? = null
    private val widgetType: Int = 0


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
        if (!timers.containsKey(timer.id))
            timers.put(timer.id, timer)

    }

    /**
     * starts the timer with the passed in Id
     */
    fun startTimer(id: Int, activity: FlutterActivity) {
        Log.d("TimerManager", "START TIMER: Looking for Timer with Id: $id")
        if (timers.containsKey(id)) {
            Log.d("TimerManager", "START TIMER: Starting Timer with Id: $id")
            val timer = timers.get(id)

            val request = CreateForegroundServiceRequest(timer!!.id, 0, timer!!.name, "Timer Started", timer!!.name, "Timer Started", false, NotificationCompat.PRIORITY_MAX, true, "Timer Notifications", "245698", listOf(NotificationActionButton(timer!!.id, "Lap", "LAP"), NotificationActionButton(timer!!.id, "Stop", "STOP")), 4)

            val intent = Intent(activity, TimerlyForegroundService::class.java)
            intent.putExtra("data", Utils.gson.toJson(request))
            intent.action = TimerlyForegroundService.ACTION_ADD_NOTIFICATION
            activity.startService(intent)

            TimerService.addTimerCallback(timer!!.id, object : ITimerTickCallback {
                override fun onTimerTick() {
                    timer.currentTime += 1
                    Log.d("TimerManager", "TIMER UPDATE: Updated Timer Value with Id: " + timer.id + " with current time : " + timer.currentTime)
                    val request1 = CreateForegroundServiceRequest(timer!!.id, 0, timer!!.name, formatSeconds(timer.currentTime), timer!!.name, formatSeconds(timer.currentTime), false, NotificationCompat.PRIORITY_LOW, true, "Timer Notifications", "245698", listOf(NotificationActionButton(timer!!.id, "Lap", "LAP"), NotificationActionButton(timer!!.id, "Stop", "STOP")), 2)
                    val intent1 = Intent(activity, TimerlyForegroundService::class.java)
                    intent1.putExtra("data", Utils.gson.toJson(request1))
                    intent1.action = TimerlyForegroundService.ACTION_UPDATE_NOTIFICATION
                    activity.startService(intent1)
                    timer.isPlaying = true
                    eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(timer.id, widgetType, "UPDATE_TIMER_VALUE", Utils.gson.toJson(timer))))
                }
            })
        }
    }

    /**
     * stops the timer
     */
    fun stopTimer(id: Int, activity: FlutterActivity) {
        Log.d("TimerManager", "STOP TIMER: Stopping Timer with Id: $id")
        if (timers.containsKey(id)) {
            val timer = timers.get(id);
            timer!!.isPlaying = false
            TimerService.removeTimerCallback(id)
            val intent = Intent(activity, TimerlyForegroundService::class.java)
            intent.putExtra("data", Utils.gson.toJson(RemoveNotificationRequest(id)))
            intent.action = TimerlyForegroundService.ACTION_REMOVE_NOTIFICATION
            activity.startService(intent)
            eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(id, widgetType, "STOP_TIMER_VALUE", Utils.gson.toJson(timer))))
            val notification = CreateForegroundServiceRequest(timer!!.id + 24037, 0, timer!!.name, "Timer Ended", timer!!.name, "Timer Ended", true, NotificationCompat.PRIORITY_MAX, false, "Timer Notifications", "245698", listOf(), 4)
            createLocalNotification(activity, notification)
        }
    }

    /**
     * removes the Timer with the passed in Id
     */
    fun removeTimer(id: Int) {
        Log.d("TimerManager", "REMOVE TIMER: Removing Timer with Id: $id")
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
        Log.d("TimerManager", "LAP TIMER: Timer with Id: $id")
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
//            stopTimer(id, activity)
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
    fun updateTimerName(id: Int, name: String) {
        Log.d("TimerManager", "UPDATE TIMER NAME: Looking for Timer with name: $name Id: $id")
        if (timers.containsKey(id)) {
            timers.get(id)!!.name = name
        }
    }

//    fun getAllTimers

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
            eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(timerlyTimerEvent.id, widgetType, timerlyTimerEvent.command, Utils.gson.toJson(timer))))
        }
    }

    /**
     * returns all the timers
     */
    fun getAllTimerData(): List<Timer> {
        return timers.values.toList()
    }
}