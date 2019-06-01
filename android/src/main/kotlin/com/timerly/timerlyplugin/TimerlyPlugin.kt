package com.timerly.timerlyplugin

import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.timerly.timerlyplugin.managers.StopwatchManager
import com.timerly.timerlyplugin.managers.TimerManager
import com.timerly.timerlyplugin.models.*
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

        // Timer Commands

        else if (call.method.equals("addTimer")) {
            val data = call.argument<String>("data")
            val timer = Gson().fromJson<Timer>(data, Timer::class.java)
            TimerManager.addNewTimer(timer)
        } else if (call.method.equals("startTimer")) {
            val data = call.argument<String>("data")
            val timer = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            TimerManager.startTimer(timer.id, activity)
        } else if (call.method.equals("stopTimer")) {
            val data = call.argument<String>("data")
            val timer = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            TimerManager.stopTimer(timer.id, activity)
        } else if (call.method.equals("removeTimer")) {
            val data = call.argument<String>("data")
            val timer = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            TimerManager.removeTimer(timer.id)
        } else if (call.method.equals("lapTimer")) {
            val data = call.argument<String>("data")
            val timer = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            TimerManager.lapTimer(timer.id)
        } else if (call.method.equals("resetTimer")) {
            val data = call.argument<String>("data")
            val timer = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            TimerManager.resetTimer(timer.id, activity)
        } else if (call.method.equals("updateTimer")) {
            val data = call.argument<String>("data")
            val timer = Gson().fromJson<Timer>(data, Timer::class.java)
            TimerManager.updateTimer(timer)
        }

        // Stopwatch Commands

        else {
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
        TimerManager.setEventSink(p1)
        StopwatchManager.setEventSink(p1)
    }

    override fun onCancel(p0: Any?) {
        Log.w("TimerlyNotification", "cancelling listener")
        eventSink = null
        TimerManager.unsetEventSink()
        StopwatchManager.unsetEventSink()

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotificationEvent(timerlyTimerEvent: TimerlyTimerEvent) {
        Log.d("TimerlyNotification", "received EventBus Notification")
        TimerManager.processNotificationCallback(eventSink, timerlyTimerEvent, activity)
    }
}
