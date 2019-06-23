package com.timerly.timerlyplugin.managers

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.timerly.timerlyplugin.TimerlyForegroundService
import com.timerly.timerlyplugin.Utils
import com.timerly.timerlyplugin.Utils.createLocalNotification
import com.timerly.timerlyplugin.Utils.formatSeconds
import com.timerly.timerlyplugin.models.*
import com.timerly.timerlyplugin.models.Constants.COMMAND_FROM_SERVICE_BUTTON_1_CLICKED
import com.timerly.timerlyplugin.models.Constants.COMMAND_FROM_SERVICE_BUTTON_2_CLICKED
import com.timerly.timerlyplugin.models.Constants.COMMAND_FROM_SERVICE_BUTTON_CLOSE_CLICKED
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_ADD
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_REMOVE
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_RESET
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_START
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_STOP
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_UPDATE_TITLE
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_UPDATE_VALUE
import com.timerly.timerlyplugin.services.ITimerTickCallback
import com.timerly.timerlyplugin.services.TimerService
import io.flutter.app.FlutterActivity
import io.flutter.plugin.common.EventChannel
import org.greenrobot.eventbus.EventBus

object StopwatchManager {

    private val stopwatches: MutableMap<Int, Stopwatch> = mutableMapOf()
    private var eventSink: EventChannel.EventSink? = null
    val widgetType: Int = 0


    fun setEventSink(es: EventChannel.EventSink?) {
        eventSink = es
    }

    fun unsetEventSink() {
        eventSink = null
    }

    /**
     * adds a new Timer
     */
    fun addNewStopwatch(stopwatch: Stopwatch?) {
        if (stopwatch == null) {
            Log.d("StopwatchManager", "Add Timer Failed. Timer Object null")
            return
        }
        Log.d("StopwatchManager", "Stopwatch Added with Id: " + stopwatch.id)
        if (!stopwatches.containsKey(stopwatch.id)) {
            stopwatches.put(stopwatch.id, stopwatch)
            if (stopwatch.isFloatingWidgetDisplayed) {
                toggleFloatingWidget(stopwatch.id)
            }
        }

    }

    /**
     * toggles the floating Widget Window
     */
    fun toggleFloatingWidget(id: Int) {
        if (stopwatches.containsKey(id)) {
            val stopwatch = stopwatches.get(id)
            if (stopwatch!!.isFloatingWidgetDisplayed) {
                EventBus.getDefault().post(TimerlyTimerEvent(id, StopwatchManager.widgetType, COMMAND_TO_SERVICE_REMOVE, Utils.gson.toJson(stopwatch)))
            } else {
                EventBus.getDefault().post(TimerlyTimerEvent(id, StopwatchManager.widgetType, COMMAND_TO_SERVICE_ADD, Utils.gson.toJson(stopwatch)))
            }
            stopwatch.isFloatingWidgetDisplayed = !stopwatch.isFloatingWidgetDisplayed
        }
    }

    /**
     * starts the Stopwatch with the passed in Id
     */
    fun startStopwatch(id: Int, activity: FlutterActivity) {
        Log.d("StopwatchManager", "START Stopwatch: Looking for Timer with Id: $id")
        if (stopwatches.containsKey(id)) {
            Log.d("StopwatchManager", "START Stopwatch: Starting Timer with Id: $id")
            val stopwatch = stopwatches.get(id)
            stopwatch!!.isPlaying = true
            val request = CreateForegroundServiceRequest(stopwatch!!.id, 0, stopwatch!!.name, "Stopwatch Started", stopwatch!!.name, "Stopwatch Started", false, NotificationCompat.PRIORITY_MAX, true, "Stopwatch Notifications", "245698", listOf(NotificationActionButton(stopwatch!!.id, "Lap", COMMAND_FROM_SERVICE_BUTTON_1_CLICKED), NotificationActionButton(stopwatch!!.id, "Stop", COMMAND_FROM_SERVICE_BUTTON_2_CLICKED)), 4)
            val intent = Intent(activity, TimerlyForegroundService::class.java)
            intent.putExtra("data", Utils.gson.toJson(request))
            intent.action = TimerlyForegroundService.ACTION_ADD_NOTIFICATION
            activity.startService(intent)

            //Floating Service Event
            EventBus.getDefault().post(TimerlyTimerEvent(stopwatch.id, StopwatchManager.widgetType, COMMAND_TO_SERVICE_START, ""))
            eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(stopwatch.id, widgetType, "START_STOPWATCH", Utils.gson.toJson(stopwatch))))

            TimerService.addTimerCallback(stopwatch!!.id, object : ITimerTickCallback {
                override fun onTimerTick() {
                    stopwatch.currentTime += 1
                    Log.d("StopwatchManager", "Stopwatch UPDATE: Updated Timer Value with Id: " + stopwatch.id + " with current time : " + stopwatch.currentTime)
                    val request1 = CreateForegroundServiceRequest(stopwatch!!.id, 0, stopwatch!!.name, formatSeconds(stopwatch.currentTime), stopwatch!!.name, formatSeconds(stopwatch.currentTime), false, NotificationCompat.PRIORITY_LOW, true, "Stopwatch Notifications", "245698", listOf(NotificationActionButton(stopwatch!!.id, "Lap", COMMAND_FROM_SERVICE_BUTTON_1_CLICKED), NotificationActionButton(stopwatch!!.id, "Stop", COMMAND_FROM_SERVICE_BUTTON_2_CLICKED)), 2)
                    val intent1 = Intent(activity, TimerlyForegroundService::class.java)
                    intent1.putExtra("data", Utils.gson.toJson(request1))
                    intent1.action = TimerlyForegroundService.ACTION_UPDATE_NOTIFICATION
                    activity.startService(intent1)
                    stopwatch.isPlaying = true
                    eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(stopwatch.id, widgetType, "UPDATE_STOPWATCH_VALUE", Utils.gson.toJson(stopwatch))))
                    EventBus.getDefault().post(TimerlyTimerEvent(stopwatch.id, widgetType, COMMAND_TO_SERVICE_UPDATE_VALUE, Utils.gson.toJson(stopwatch)))
                }
            })
        }
    }

    /**
     * stops the timer
     */
    fun stopStopwatch(id: Int, activity: FlutterActivity) {
        Log.d("StopwatchManager", "STOP Stopwatch: Stopping Stopwatch with Id: $id")
        if (stopwatches.containsKey(id)) {
            val stopwatch = stopwatches.get(id);
            stopwatch!!.isPlaying = false
            TimerService.removeTimerCallback(id)
            val intent = Intent(activity, TimerlyForegroundService::class.java)
            intent.putExtra("data", Utils.gson.toJson(RemoveNotificationRequest(id)))
            intent.action = TimerlyForegroundService.ACTION_REMOVE_NOTIFICATION
            activity.startService(intent)
            eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(id, widgetType, "STOP_STOPWATCH_VALUE", Utils.gson.toJson(stopwatch))))
            EventBus.getDefault().post(TimerlyTimerEvent(id, widgetType, COMMAND_TO_SERVICE_STOP, ""))
            val notification = CreateForegroundServiceRequest(stopwatch!!.id + 24037, 0, stopwatch!!.name, "Stopwatch Ended", stopwatch!!.name, "Stopwatch Ended", true, NotificationCompat.PRIORITY_MAX, false, "Stopwatch Notifications", "245698", listOf(), 4)
            createLocalNotification(activity, notification)
        }
    }

    /**
     * removes the Stopwatch with the passed in Id
     */
    fun removeStopwatch(id: Int) {
        Log.d("StopwatchManager", "REMOVE Stopwatch: Removing Stopwatch with Id: $id")
        if (stopwatches.containsKey(id)) {
            stopwatches.remove(id)
        }
        if (stopwatches.isEmpty()) {
            TimerService.removeTimerCallback(id)
            EventBus.getDefault().post(TimerlyTimerEvent(id, widgetType, COMMAND_TO_SERVICE_REMOVE, ""))
        }
    }

    /**
     * adds the lap to the current Stopwatch
     */
    fun lapStopwatch(id: Int) {
        Log.d("StopwatchManager", "LAP STOPWATCH: Stopwatch with Id: $id")
        if (stopwatches.containsKey(id)) {
            val stopwatch = stopwatches.get(id)!!
            stopwatch!!.laps!!.add(Lap(stopwatch!!.laps!!.size + 1, stopwatch.currentTime))
        }
    }

    /**
     * resets the current Stopwatch
     */
    fun resetStopwatch(id: Int, activity: FlutterActivity): Stopwatch? {
        Log.d("StopwatchManager", "RESET Stopwatch: Stopwatch with Id: $id")
        if (stopwatches.containsKey(id)) {
            val stopwatch = stopwatches.get(id);
            stopwatch!!.currentTime = 0
            stopwatch.laps!!.clear()
            stopStopwatch(id, activity)
            eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(stopwatch.id, widgetType, "UPDATE_STOPWATCH_VALUE", Utils.gson.toJson(stopwatch))))
            EventBus.getDefault().post(TimerlyTimerEvent(id, widgetType, COMMAND_TO_SERVICE_RESET, Utils.gson.toJson(stopwatch)))
            return stopwatch
        }
        return null
    }

    /**
     * updates the Stopwatch
     */
    fun updateStopwatchName(id: Int, name: String) {
        Log.d("StopwatchManager", "UPDATE Stopwatch NAME: Looking for Stopwatch with Id: $id")
        if (stopwatches.containsKey(id)) {
            stopwatches.get(id)!!.name = name
            EventBus.getDefault().post(TimerlyTimerEvent(id, widgetType, COMMAND_TO_SERVICE_UPDATE_TITLE, name))
        }
    }

    /**
     * processes Notification Button Callback
     */
    fun processNotificationCallback(eventSink: EventChannel.EventSink?, timerlyTimerEvent: TimerlyTimerEvent, activity: FlutterActivity) {
        if (stopwatches.containsKey(timerlyTimerEvent.id)) {
            val stopwatch = stopwatches.get(timerlyTimerEvent.id)
            when (timerlyTimerEvent.command) {
                COMMAND_FROM_SERVICE_BUTTON_1_CLICKED -> {
                    if (stopwatch!!.isPlaying) {
                        lapStopwatch(stopwatch.id)
                        Toast.makeText(activity, "Added Lap for the timer " + stopwatch.name + " at time " + Utils.formatSeconds(stopwatch.currentTime), Toast.LENGTH_SHORT).show()
                    } else
                        startStopwatch(stopwatch.id, activity)

                }
                COMMAND_FROM_SERVICE_BUTTON_2_CLICKED -> {
                    if (stopwatch!!.isPlaying)
                        stopStopwatch(timerlyTimerEvent.id, activity)
                    else
                        resetStopwatch(stopwatch.id, activity)
                }
                COMMAND_FROM_SERVICE_BUTTON_CLOSE_CLICKED -> {
                    toggleFloatingWidget(timerlyTimerEvent.id)
                }
            }
            eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(timerlyTimerEvent.id, widgetType, timerlyTimerEvent.command, Utils.gson.toJson(stopwatch))))
        }
    }

    /**
     * returns all the stopwatches
     */
    fun getAllStopwatchData(): List<Stopwatch> {
        return stopwatches.values.toList()
    }

    /**
     * returns back the stopwatch by ID
     */
    fun getStopwatchById(id: Int): Stopwatch? {
        if (stopwatches.containsKey(id)) {
            return stopwatches.get(id)!!
        }
        return null
    }
}