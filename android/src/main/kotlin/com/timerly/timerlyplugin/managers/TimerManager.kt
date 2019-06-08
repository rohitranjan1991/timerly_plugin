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
import com.timerly.timerlyplugin.services.MediaService


object TimerManager {

    private val timers: MutableMap<Int, Timer> = mutableMapOf()
    private var eventSink: EventChannel.EventSink? = null
    private val widgetType: Int = 1

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
     * starts the Timer with the id
     */
    fun startTimer(id: Int, activity: FlutterActivity) {
        Log.d("TimerManager", "START Timer: Looking for Timer with Id: $id")
        if (timers.containsKey(id)) {
            Log.d("TimerManager", "START Timer: Starting Timer with Id: $id")
            val timer = timers.get(id)
            if (timer!!.initialTime == 0L) {
                Log.d("TimerManager", "START Timer: Timer not started due to time not set with Id: $id")
                return
            }
            val request = CreateForegroundServiceRequest(timer!!.id, 1, timer!!.name, "Timer Started", timer!!.name, "Timer Started", false, NotificationCompat.PRIORITY_MAX, true, "Timer Notifications", "245699", listOf(NotificationActionButton(timer!!.id, "Stop", "STOP")), 4)

            val intent = Intent(activity, TimerlyForegroundService::class.java)
            intent.putExtra("data", Utils.gson.toJson(request))
            intent.action = TimerlyForegroundService.ACTION_ADD_NOTIFICATION
            activity.startService(intent)
            timer.currentTime = timer.initialTime
            TimerService.addTimerCallback(timer!!.id, object : ITimerTickCallback {
                override fun onTimerTick() {
                    timer.currentTime -= 1
                    if (timer.currentTime > -1) {
                        Log.d("TimerManager", "TIMER UPDATE: Updated Timer Value with Id: " + timer.id + " with current time : " + timer.currentTime)
                        val request1 = CreateForegroundServiceRequest(timer!!.id, 1, timer!!.name, formatSeconds(timer.currentTime), timer!!.name, formatSeconds(timer.currentTime), false, NotificationCompat.PRIORITY_LOW, true, "Timer Notifications", "245699", listOf(NotificationActionButton(timer!!.id, "Stop", "STOP")), 2)
                        val intent1 = Intent(activity, TimerlyForegroundService::class.java)
                        intent1.putExtra("data", Utils.gson.toJson(request1))
                        intent1.action = TimerlyForegroundService.ACTION_UPDATE_NOTIFICATION
                        activity.startService(intent1)
                        eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(timer.id, widgetType, "UPDATE_TIMER_VALUE", Utils.gson.toJson(timer))))
                        timer.isPlaying = true;
                    } else {
                        stopTimer(id, activity)
                        Log.d("TimerManager", Utils.gson.toJson(timer))
                        MediaService.playAlarm(timer.id, timer.alarmValue!!, activity)
                    }
                }
            })
        }
    }

    /**
     * stops the Timer with the id
     */
    fun stopTimer(id: Int, activity: FlutterActivity) {
        Log.d("TimerManager", "STOP Timer: Stopping Timer with Id: $id")
        if (timers.containsKey(id)) {
            val timer = timers.get(id);
            timer!!.isPlaying = false
            TimerService.removeTimerCallback(id)
            val intent = Intent(activity, TimerlyForegroundService::class.java)
            intent.putExtra("data", Utils.gson.toJson(RemoveNotificationRequest(id)))
            intent.action = TimerlyForegroundService.ACTION_REMOVE_NOTIFICATION
            activity.startService(intent)
            eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(id, widgetType, "STOP_TIMER_VALUE", Utils.gson.toJson(timer))))
            val notification = CreateForegroundServiceRequest(timer!!.id + 6798123, 1, timer!!.name, "Timer Ended", timer!!.name, "Timer Ended", true, NotificationCompat.PRIORITY_MAX, false, "Timer Notifications", "245699", listOf(), 4)
            createLocalNotification(activity, notification)
        }
    }

    /**
     * removes the Timer with the id
     */
    fun removeTimer(id: Int) {
        Log.d("TimerManager", "REMOVE Timer: Removing Timer with Id: $id")
        if (timers.containsKey(id)) {
            timers.remove(id)
        }
        if (timers.isEmpty()) {
            TimerService.removeTimerCallback(id)
        }
    }

    /**
     * resets the Timer with the id
     */
    fun resetTimer(id: Int, activity: FlutterActivity): Timer? {
        Log.d("TimerManager", "RESET Timer: Timer with Id: $id")
        if (timers.containsKey(id)) {
            val timer = timers.get(id);
            timer!!.currentTime = timer.initialTime
            return timer
        }
        return null
    }

    /**
     * updates the Timer name
     */
    fun updateTimerName(id: Int, name: String) {
        Log.d("TimerManager", "UPDATE Timer: Looking for Timer with Id: $id")
        if (timers.containsKey(id)) {
            timers.get(id)?.name = name
        }
    }

    /**
     * updates the initial value of the timer
     */
    fun updateInitialTimeTimer(id: Int, value: Long) {
        Log.d("TimerManager", "UPDATE Timer: Looking for Timer with Id: $id")
        if (timers.containsKey(id)) {
            timers.get(id)!!.initialTime = value
            timers.get(id)!!.currentTime = value
        }
    }

    /**
     * updates the alarm id of the Timer
     */
    fun updateTimerAlarm(id: Int, value: Int) {
        Log.d("TimerManager", "UPDATE Timer Alarm: Looking for Timer with Id: $id")
        if (timers.containsKey(id)) {
            timers.get(id)!!.alarmValue = value
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
            }
            eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(timerlyTimerEvent.id, widgetType, timerlyTimerEvent.command, Utils.gson.toJson(timer))))
        }
    }

    /**
     * returns all the timers
     */
    fun getAllTimersData(): List<Timer> {
        return timers.values.toList()
    }
}