package com.timerly.timerlyplugin

import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.timerly.timerlyplugin.managers.TimerManager
import com.timerly.timerlyplugin.managers.StopwatchManager
import com.timerly.timerlyplugin.models.*
import com.timerly.timerlyplugin.services.MediaService
import io.flutter.app.FlutterActivity
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class TimerlyPlugin(val activity: FlutterActivity) : MethodCallHandler, EventChannel.StreamHandler {

    private var eventSink: EventChannel.EventSink? = null
    private var isForegroundServiceActive = false;
    private var notificationIds: MutableList<Int> = mutableListOf()

    override fun onMethodCall(call: MethodCall, result: Result): Unit {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android ${Build.VERSION.RELEASE}")
        }
        //EventBus.getDefault().unregister(this)

        else if (call.method.equals("getAllWidgets")) {
            val response = JsonObject();
            val allTimers = JsonArray();
            val allStopwatches = JsonArray();
            StopwatchManager.getAllStopwatchData().forEach { allTimers.add(Utils.gson.toJson(it)) }
            TimerManager.getAllTimersData().forEach { allStopwatches.add(Utils.gson.toJson(it)) }
            response.add("timers", allTimers)
            response.add("stopwatches", allStopwatches)
            result.success(response.toString());
        }
        // Stopwatch Commands

        else if (call.method.equals("addStopwatch")) {
            val data = call.argument<String>("data")
            val timer = Gson().fromJson<Stopwatch>(data, Stopwatch::class.java)
            StopwatchManager.addNewStopwatch(timer)
        } else if (call.method.equals("startStopwatch")) {
            val data = call.argument<String>("data")
            val timer = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            StopwatchManager.startStopwatch(timer.id, activity)
        } else if (call.method.equals("stopStopwatch")) {
            val data = call.argument<String>("data")
            val timer = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            StopwatchManager.stopStopwatch(timer.id, activity)
        } else if (call.method.equals("removeStopwatch")) {
            val data = call.argument<String>("data")
            val timer = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            StopwatchManager.removeStopwatch(timer.id)
        } else if (call.method.equals("lapStopwatch")) {
            val data = call.argument<String>("data")
            val timer = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            StopwatchManager.lapStopwatch(timer.id)
        } else if (call.method.equals("resetStopwatch")) {
            val data = call.argument<String>("data")
            val timer = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            StopwatchManager.resetStopwatch(timer.id, activity)
        } else if (call.method.equals("updateStopwatchName")) {
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest2>(data, GenericRequest2::class.java)
            StopwatchManager.updateStopwatchName(gr.id, gr.arg1 as String)
        }

        // Timer Commands

        else if (call.method.equals("addTimer")) {
            val data = call.argument<String>("data")
            val stopwatch = Gson().fromJson<Timer>(data, Timer::class.java)
            TimerManager.addNewTimer(stopwatch)
        } else if (call.method.equals("startTimer")) {
            val data = call.argument<String>("data")
            val stopwatch = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            TimerManager.startTimer(stopwatch.id, activity)
        } else if (call.method.equals("stopTimer")) {
            val data = call.argument<String>("data")
            val stopwatch = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            TimerManager.stopTimer(stopwatch.id, activity)
        } else if (call.method.equals("removeTimer")) {
            val data = call.argument<String>("data")
            val stopwatch = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            TimerManager.removeTimer(stopwatch.id)
        } else if (call.method.equals("resetTimer")) {
            val data = call.argument<String>("data")
            val stopwatch = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            TimerManager.resetTimer(stopwatch.id, activity)
        } else if (call.method.equals("updateTimerName")) {
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest2>(data, GenericRequest2::class.java)
            TimerManager.updateTimerName(gr.id, gr.arg1 as String)
        } else if (call.method.equals("updateInitialTimeTimer")) {
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest2>(data, GenericRequest2::class.java)
            TimerManager.updateInitialTimeTimer(gr.id, (gr.arg1 as Double).toLong())
        }
        else if (call.method.equals("updateTimerAlarm")) {
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest2>(data, GenericRequest2::class.java)
            TimerManager.updateInitialTimeTimer(gr.id, (gr.arg1 as Double).toLong())
        }

        // misc command
        else if (call.method.equals("stopAlarm")) {
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest2>(data, GenericRequest2::class.java)
            MediaService.stopAlarm(1)
        }
        else if (call.method.equals("playAlarm")) {
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest2>(data, GenericRequest2::class.java)
            MediaService.stopAlarm(1)
            MediaService.playAlarm(1, (gr.arg1 as Double).toInt(), activity)
        } else {
            result.notImplemented()
        }
    }

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar): Unit {
            val instance = TimerlyPlugin(registrar.activity() as FlutterActivity)
            val channel = MethodChannel(registrar.messenger(), "timerly_plugin")
            channel.setMethodCallHandler(instance)
            val eventChannel = EventChannel(registrar.messenger(), "com.timerly/notification/event")
            eventChannel.setStreamHandler(instance)
        }
    }

    override fun onListen(p0: Any?, p1: EventChannel.EventSink?) {
        Log.w("TimerlyNotification", "onListen")
        eventSink = p1
        StopwatchManager.setEventSink(p1)
        TimerManager.setEventSink(p1)
    }

    override fun onCancel(p0: Any?) {
        Log.w("TimerlyNotification", "cancelling listener")
        eventSink = null
        StopwatchManager.unsetEventSink()
        TimerManager.unsetEventSink()

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotificationEvent(timerlyTimerEvent: TimerlyTimerEvent) {
        Log.d("TimerlyNotification", "received EventBus Notification")
        StopwatchManager.processNotificationCallback(eventSink, timerlyTimerEvent, activity)
        TimerManager.processNotificationCallback(eventSink, timerlyTimerEvent, activity)
    }
}
