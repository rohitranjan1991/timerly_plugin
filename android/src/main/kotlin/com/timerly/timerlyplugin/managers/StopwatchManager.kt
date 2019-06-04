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


object StopwatchManager {

    private val stopwatches: MutableMap<Int, Stopwatch> = mutableMapOf()
    private var eventSink: EventChannel.EventSink? = null
    private val widgetType: Int = 1

    fun setEventSink(es: EventChannel.EventSink?) {
        eventSink = es
    }

    fun unsetEventSink() {
        eventSink = null
    }

    /**
     * adds a new StopWatch
     */
    fun addNewStopwatch(stopWatch: Stopwatch?) {
        if (stopWatch == null) {
            Log.d("StopWatchManager", "Add Stopwatch Failed. Stopwatch Object null")
            return
        }
        Log.d("StopWatchManager", "Stopwatch Added with Id: " + stopWatch.id)
        if (!stopwatches.containsKey(stopWatch.id))
            stopwatches.put(stopWatch.id, stopWatch)
    }

    /**
     * starts the stopwatch with the id
     */
    fun startStopwatch(id: Int, activity: FlutterActivity) {
        Log.d("StopWatchManager", "START Stopwatch: Looking for StopWatch with Id: $id")
        if (stopwatches.containsKey(id)) {
            Log.d("StopWatchManager", "START StopWatch: Starting StopWatch with Id: $id")
            val stopwatch = stopwatches.get(id)
            if (stopwatch!!.initialTime == 0L) {
                Log.d("StopWatchManager", "START StopWatch: StopWatch not started due to time not set with Id: $id")
                return
            }
            val request = CreateForegroundServiceRequest(stopwatch!!.id, 1, stopwatch!!.name, "Timer Started", stopwatch!!.name, "Stopwatch Started", false, NotificationCompat.PRIORITY_MAX, true, "StopWatch Notifications", "245699", listOf(NotificationActionButton(stopwatch!!.id, "Stop", "STOP")), 4)

            val intent = Intent(activity, TimerlyForegroundService::class.java)
            intent.putExtra("data", Utils.gson.toJson(request))
            intent.action = TimerlyForegroundService.ACTION_ADD_NOTIFICATION
            activity.startService(intent)
            stopwatch.currentTime = stopwatch.initialTime
            TimerService.addTimerCallback(stopwatch!!.id, object : ITimerTickCallback {
                override fun onTimerTick() {
                    stopwatch.currentTime -= 1
                    if (stopwatch.currentTime > -1) {
                        Log.d("StopWatchManager", "TIMER UPDATE: Updated Timer Value with Id: " + stopwatch.id + " with current time : " + stopwatch.currentTime)
                        val request1 = CreateForegroundServiceRequest(stopwatch!!.id, 1, stopwatch!!.name, formatSeconds(stopwatch.currentTime), stopwatch!!.name, formatSeconds(stopwatch.currentTime), false, NotificationCompat.PRIORITY_LOW, true, "StopWatch Notifications", "245699", listOf(NotificationActionButton(stopwatch!!.id, "Stop", "STOP")), 2)
                        val intent1 = Intent(activity, TimerlyForegroundService::class.java)
                        intent1.putExtra("data", Utils.gson.toJson(request1))
                        intent1.action = TimerlyForegroundService.ACTION_UPDATE_NOTIFICATION
                        activity.startService(intent1)
                        eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(stopwatch.id, widgetType, "UPDATE_STOPWATCH_VALUE", Utils.gson.toJson(stopwatch))))
                        stopwatch.isPlaying = true;
                    } else {
                        stopStopwatch(id, activity)
                        Log.d("StopwatchManager", Utils.gson.toJson(stopwatch))
                        MediaService.playAlarm(stopwatch.id, stopwatch.alarmValue!!, activity)
                    }
                }
            })
        }
    }

    /**
     * stops the stopwatch with the id
     */
    fun stopStopwatch(id: Int, activity: FlutterActivity) {
        Log.d("StopWatchManager", "STOP StopWatch: Stopping StopWatch with Id: $id")
        if (stopwatches.containsKey(id)) {
            val stopwatch = stopwatches.get(id);
            stopwatch!!.isPlaying = false
            TimerService.removeTimerCallback(id)
            val intent = Intent(activity, TimerlyForegroundService::class.java)
            intent.putExtra("data", Utils.gson.toJson(RemoveNotificationRequest(id)))
            intent.action = TimerlyForegroundService.ACTION_REMOVE_NOTIFICATION
            activity.startService(intent)
            eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(id, widgetType, "STOP_STOPWATCH_VALUE", Utils.gson.toJson(stopwatch))))
            val notification = CreateForegroundServiceRequest(stopwatch!!.id + 6798123, 1, stopwatch!!.name, "Stopwatch Ended", stopwatch!!.name, "Stopwatch Ended", true, NotificationCompat.PRIORITY_MAX, false, "StopWatch Notifications", "245699", listOf(), 4)
            createLocalNotification(activity, notification)
        }
    }

    /**
     * removes the stopwatch with the id
     */
    fun removeStopwatch(id: Int) {
        Log.d("StopWatchManager", "REMOVE StopWatch: Removing StopWatch with Id: $id")
        if (stopwatches.containsKey(id)) {
            stopwatches.remove(id)
        }
        if (stopwatches.isEmpty()) {
            TimerService.removeTimerCallback(id)
        }
    }

    /**
     * resets the stopwatch with the id
     */
    fun resetStopwatch(id: Int, activity: FlutterActivity): Stopwatch? {
        Log.d("StopWatchManager", "RESET StopWatch: StopWatch with Id: $id")
        if (stopwatches.containsKey(id)) {
//            stopStopwatch(id, activity)
            val stopWatch = stopwatches.get(id);
            stopWatch!!.currentTime = stopWatch.initialTime
            return stopWatch
        }
        return null
    }

    /**
     * updates the stopwatch name
     */
    fun updateStopwatchName(id: Int, name: String) {
        Log.d("StopWatchManager", "UPDATE Stopwatch: Looking for Stopwatch with Id: $id")
        if (stopwatches.containsKey(id)) {
            stopwatches.get(id)?.name = name
        }
    }

    /**
     * updates the initial value of the timer
     */
    fun updateInitialTimeStopwatch(id: Int, value: Long) {
        Log.d("StopWatchManager", "UPDATE Stopwatch: Looking for Stopwatch with Id: $id")
        if (stopwatches.containsKey(id)) {
            stopwatches.get(id)!!.initialTime = value
            stopwatches.get(id)!!.currentTime = value
        }
    }

    /**
     * updates the alarm id of the stopwatch
     */
    fun updateStopwatchAlarm(id: Int, value: Int) {
        Log.d("StopWatchManager", "UPDATE Stopwatch Alarm: Looking for Stopwatch with Id: $id")
        if (stopwatches.containsKey(id)) {
            stopwatches.get(id)!!.alarmValue = value
        }
    }

    /**
     * processes Notification Button Callback
     */
    fun processNotificationCallback(eventSink: EventChannel.EventSink?, timerlyTimerEvent: TimerlyTimerEvent, activity: FlutterActivity) {
        if (stopwatches.containsKey(timerlyTimerEvent.id)) {
            val stopwatch = stopwatches.get(timerlyTimerEvent.id)
            when (timerlyTimerEvent.command) {
                "STOP" -> {
                    stopStopwatch(timerlyTimerEvent.id, activity)
                }
            }
            eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(timerlyTimerEvent.id, widgetType, timerlyTimerEvent.command, Utils.gson.toJson(stopwatch))))
        }
    }

    /**
     * returns all the stopwatches
     */
    fun getAllStopwatchesData(): List<Stopwatch> {
        return stopwatches.values.toList()
    }
}