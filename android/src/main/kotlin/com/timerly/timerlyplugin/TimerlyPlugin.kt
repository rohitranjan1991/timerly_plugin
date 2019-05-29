package com.timerly.timerlyplugin

import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.timerly.timerlyplugin.models.CreateForegroundServiceRequest
import com.timerly.timerlyplugin.models.TimerlyNotificationEvent
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

    private var timerlyNotificationEventManager: TimerlyNotificationEventManager? = null
    private var eventSink: EventChannel.EventSink? = null

    override fun onMethodCall(call: MethodCall, result: Result): Unit {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android ${Build.VERSION.RELEASE}")
        } else if (call.method.equals("startForegroundService")) {
            Log.d("TimerlyPlugin", "Starting Foreground Service")
            val data = call.argument<String>("data")
            EventBus.getDefault().register(this)
            val createForegroundServiceRequest = Gson().fromJson<CreateForegroundServiceRequest>(data, CreateForegroundServiceRequest::class.java)
            val intent = Intent(activity, TimerlyForegroundService::class.java)
            intent.putExtra("data", Gson().toJson(createForegroundServiceRequest))
            intent.action = TimerlyForegroundService.ACTION_START_FOREGROUND_SERVICE
            activity.startService(intent)

        } else if (call.method.equals("stopForegroundService")) {
            val intent = Intent(activity, TimerlyForegroundService::class.java)
            intent.action = TimerlyForegroundService.ACTION_STOP_FOREGROUND_SERVICE
            activity.startService(intent)
            if (timerlyNotificationEventManager != null) {
                timerlyNotificationEventManager = null
            }
            EventBus.getDefault().unregister(this)
        } else if (call.method.equals("updateForegroundService")) {
            Log.d("TimerlyPlugin", "Updating Foreground Service")
            val data = call.argument<String>("data")
            val createForegroundServiceRequest = Gson().fromJson<CreateForegroundServiceRequest>(data, CreateForegroundServiceRequest::class.java)
            val intent = Intent(activity, TimerlyForegroundService::class.java)
            intent.putExtra("data", Gson().toJson(createForegroundServiceRequest))
            intent.action = TimerlyForegroundService.ACTION_UPDATE_NOTIFICATION_SERVICE
            activity.startService(intent)
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
            val eventChannel = EventChannel(registrar.messenger(), "com.timerly/notification/stream")
            eventChannel.setStreamHandler(instance)
        }
    }

    override fun onListen(p0: Any?, p1: EventChannel.EventSink?) {
        Log.w("TimerlyNotification", "onListen")
        eventSink = p1
    }

    override fun onCancel(p0: Any?) {
        Log.w("TimerlyNotification", "cancelling listener")
        eventSink = null

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotificationEvent(notificationEvent: TimerlyNotificationEvent) {
        Log.d("TimerlyNotification", "received EventBus Notification")
        eventSink?.success(Gson().toJson(notificationEvent))
    }
}
