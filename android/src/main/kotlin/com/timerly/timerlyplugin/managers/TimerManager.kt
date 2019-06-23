package com.timerly.timerlyplugin.managers

import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.timerly.timerlyplugin.TimerlyForegroundService
import com.timerly.timerlyplugin.Utils
import com.timerly.timerlyplugin.Utils.createLocalNotification
import com.timerly.timerlyplugin.Utils.formatSeconds
import com.timerly.timerlyplugin.models.*
import com.timerly.timerlyplugin.models.Constants.COMMAND_FROM_SERVICE_BUTTON_1_CLICKED
import com.timerly.timerlyplugin.models.Constants.COMMAND_FROM_SERVICE_BUTTON_2_CLICKED
import com.timerly.timerlyplugin.models.Constants.COMMAND_FROM_SERVICE_BUTTON_CLOSE_CLICKED
import com.timerly.timerlyplugin.models.Constants.COMMAND_FROM_SERVICE_STOP_CLICKED
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_ADD
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_REMOVE
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_RESET
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_START
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_STOP
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_UPDATE_TITLE
import com.timerly.timerlyplugin.models.Constants.COMMAND_TO_SERVICE_UPDATE_VALUE
import com.timerly.timerlyplugin.services.ITimerTickCallback
import com.timerly.timerlyplugin.services.MediaService
import com.timerly.timerlyplugin.services.TimerService
import io.flutter.app.FlutterActivity
import io.flutter.plugin.common.EventChannel
import org.greenrobot.eventbus.EventBus


object TimerManager {

    private val timers: MutableMap<Int, Timer> = mutableMapOf()
    private var eventSink: EventChannel.EventSink? = null
    val widgetType: Int = 1

    fun setEventSink(es: EventChannel.EventSink?) {
        eventSink = es
    }

    fun unsetEventSink() {
        eventSink = null
    }

    /**
     * adds a new Timer
     */
    fun addNewTimer(timer: Timer?, activity: FlutterActivity) {
        if (timer == null) {
            Log.d("TimerManager", "Add Timer Failed. Timer Object null")
            return
        }

        if (!timers.containsKey(timer.id)) {
            timers.put(timer.id, timer)

            // TODO - check teh use of this if condition
            if (timers.size == 1) {
                eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(timer.id, TimerManager.widgetType, COMMAND_TO_SERVICE_ADD, Utils.gson.toJson(timer))))
            }
            // show the floating Widget if the value is true
            if (timer.isFloatingWidgetDisplayed) {
                toggleFloatingWidget(timer.id)
            }
            Log.d("TimerManager", "Timer Added with Id: " + timer.id)
        }
    }

    /**
     * toggles the floating Widget Window
     */
    fun toggleFloatingWidget(id: Int) {
        if (timers.containsKey(id)) {
            val timer = timers.get(id)
            if (timer!!.isFloatingWidgetDisplayed) {
                EventBus.getDefault().post(TimerlyTimerEvent(id, TimerManager.widgetType, COMMAND_TO_SERVICE_REMOVE, Utils.gson.toJson(timer)))
            } else {
                EventBus.getDefault().post(TimerlyTimerEvent(id, TimerManager.widgetType, COMMAND_TO_SERVICE_ADD, Utils.gson.toJson(timer)))
            }
            timer.isFloatingWidgetDisplayed = !timer.isFloatingWidgetDisplayed
        }
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
            timer.isPlaying = true
            val request = CreateForegroundServiceRequest(timer!!.id, 1, timer!!.name, "Timer Started", timer!!.name, "Timer Started", false, NotificationCompat.PRIORITY_MAX, true, "Timer Notifications", "245699", listOf(NotificationActionButton(timer!!.id, "Stop", COMMAND_FROM_SERVICE_BUTTON_1_CLICKED), NotificationActionButton(timer!!.id, "Reset", COMMAND_FROM_SERVICE_STOP_CLICKED)), 4)

            val intent = Intent(activity, TimerlyForegroundService::class.java)
            intent.putExtra("data", Utils.gson.toJson(request))
            intent.action = TimerlyForegroundService.ACTION_ADD_NOTIFICATION
            activity.startService(intent)

            //Floating Service Event
            EventBus.getDefault().post(TimerlyTimerEvent(timer.id, TimerManager.widgetType, COMMAND_TO_SERVICE_START, ""))
            eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(timer.id, widgetType, "START_TIMER", Utils.gson.toJson(timer))))

            if (timer.currentTime == 0L)
                timer.currentTime = timer.initialTime
            TimerService.addTimerCallback(timer!!.id, object : ITimerTickCallback {
                override fun onTimerTick() {
                    timer.currentTime -= 1
                    if (timer.currentTime > -1) {
                        Log.d("TimerManager", "TIMER UPDATE: Updated Timer Value with Id: " + timer.id + " with current time : " + timer.currentTime)
                        val request1 = CreateForegroundServiceRequest(timer!!.id, 1, timer!!.name, formatSeconds(timer.currentTime), timer!!.name, formatSeconds(timer.currentTime), false, NotificationCompat.PRIORITY_LOW, true, "Timer Notifications", "245699", listOf(NotificationActionButton(timer!!.id, "Stop", COMMAND_FROM_SERVICE_BUTTON_1_CLICKED), NotificationActionButton(timer!!.id, "Reset", COMMAND_FROM_SERVICE_BUTTON_2_CLICKED)), 2)
                        val intent1 = Intent(activity, TimerlyForegroundService::class.java)
                        intent1.putExtra("data", Utils.gson.toJson(request1))
                        intent1.action = TimerlyForegroundService.ACTION_UPDATE_NOTIFICATION
                        activity.startService(intent1)
                        eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(timer.id, widgetType, "UPDATE_TIMER_VALUE", Utils.gson.toJson(timer))))
                        EventBus.getDefault().post(TimerlyTimerEvent(timer.id, widgetType, COMMAND_TO_SERVICE_UPDATE_VALUE, Utils.gson.toJson(timer)))
                        timer.isPlaying = true
                        //FloatingServiceManager.updateTimer(timer)
                    } else {
                        stopTimer(id, activity)
                        Log.d("TimerManager", Utils.gson.toJson(timer))
                        MediaService.playAlarm(timer.id, timer.alarmSoundValue!!, activity)
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
            EventBus.getDefault().post(TimerlyTimerEvent(id, widgetType, COMMAND_TO_SERVICE_STOP, ""))
            val notification = CreateForegroundServiceRequest(timer!!.id + 6798123, 1, timer!!.name, "Timer Ended", timer!!.name, "Timer Ended", true, NotificationCompat.PRIORITY_MAX, false, "Timer Notifications", "245699", listOf(), 4)
            createLocalNotification(activity, notification)
        }
        if (timers.size == 0) {

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
            EventBus.getDefault().post(TimerlyTimerEvent(id, widgetType, COMMAND_TO_SERVICE_REMOVE, ""))
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
            stopTimer(id, activity)
            eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(timer.id, widgetType, "UPDATE_TIMER_VALUE", Utils.gson.toJson(timer))))
            EventBus.getDefault().post(TimerlyTimerEvent(id, widgetType, COMMAND_TO_SERVICE_RESET, Utils.gson.toJson(timer)))
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
            EventBus.getDefault().post(TimerlyTimerEvent(id, widgetType, COMMAND_TO_SERVICE_UPDATE_TITLE, name))
        }
    }

    /**
     * updates the initial value of the timer
     */
    fun updateInitialTimeTimer(id: Int, value: Long) {
        Log.d("TimerManager", "UPDATE Timer: Looking for Timer with Id: $id")
        if (timers.containsKey(id)) {
            val timer = timers.get(id)
            timer!!.initialTime = value
            timer!!.currentTime = value
            EventBus.getDefault().post(TimerlyTimerEvent(id, widgetType, COMMAND_TO_SERVICE_UPDATE_VALUE, Utils.gson.toJson(timer)))
        }
    }

    /**
     * updates the alarm id of the Timer
     */
    fun updateTimerAlarm(id: Int, value: Int) {
        Log.d("TimerManager", "UPDATE Timer Alarm: Looking for Timer with Id: $id")
        if (timers.containsKey(id)) {
            timers.get(id)!!.alarmSoundValue = value
        }
    }

    /**
     * processes Notification Button Callback
     */
    fun processNotificationCallback(eventSink: EventChannel.EventSink?, timerlyTimerEvent: TimerlyTimerEvent, activity: FlutterActivity) {
        if (timers.containsKey(timerlyTimerEvent.id)) {
            val timer = timers.get(timerlyTimerEvent.id)
            when (timerlyTimerEvent.command) {
                COMMAND_FROM_SERVICE_BUTTON_1_CLICKED -> {
                    if (timers.containsKey(timer!!.id)) {
                        val tim = timers.get(timer!!.id)
                        if (tim!!.isPlaying)
                            stopTimer(tim.id, activity)
                        else
                            startTimer(tim.id, activity)
                    }
                }
                COMMAND_FROM_SERVICE_BUTTON_2_CLICKED -> {
                    resetTimer(timer!!.id, activity)
                }
                COMMAND_FROM_SERVICE_STOP_CLICKED -> {
                    stopTimer(timerlyTimerEvent.id, activity)
                }
                COMMAND_FROM_SERVICE_BUTTON_CLOSE_CLICKED -> {
                    toggleFloatingWidget(timer!!.id)
                }
            }
//            eventSink?.success(Utils.gson.toJson(TimerlyTimerEvent(timerlyTimerEvent.id, widgetType, timerlyTimerEvent.command, Utils.gson.toJson(timer))))
        }
    }

    /**
     * returns all the timers
     */
    fun getAllTimersData(): List<Timer> {
        return timers.values.toList()
    }
}