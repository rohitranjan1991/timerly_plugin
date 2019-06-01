package com.timerly.timerlyplugin

import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.timerly.timerlyplugin.models.CreateForegroundServiceRequest
import com.timerly.timerlyplugin.models.RemoveNotificationRequest
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
    private var isForegroundServiceActive = false;
    private var notificationIds: MutableList<Int> = mutableListOf()

    override fun onMethodCall(call: MethodCall, result: Result): Unit {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android ${Build.VERSION.RELEASE}")
        } else if (call.method.equals("addNotification")) {
            val data = call.argument<String>("data")
            if (!EventBus.getDefault().isRegistered(this))
                EventBus.getDefault().register(this)
            val createForegroundServiceRequest = Gson().fromJson<CreateForegroundServiceRequest>(data, CreateForegroundServiceRequest::class.java)
            val intent = Intent(activity, TimerlyForegroundService::class.java)
            intent.putExtra("data", Gson().toJson(createForegroundServiceRequest))
            intent.action = TimerlyForegroundService.ACTION_ADD_NOTIFICATION
            activity.startService(intent)
            if (!notificationIds.contains(createForegroundServiceRequest.serviceId))
                notificationIds.add(createForegroundServiceRequest.serviceId)
        } else if (call.method.equals("removeNotification")) {
            val data = call.argument<String>("data")
            val removeNotificationRequest = Gson().fromJson<RemoveNotificationRequest>(data, RemoveNotificationRequest::class.java)
            Log.d("TimerlyPlugin","Removing Notification");
            if (!notificationIds.contains(removeNotificationRequest.notificationId))
                notificationIds.remove(removeNotificationRequest.notificationId)

            val intent = Intent(activity, TimerlyForegroundService::class.java)
            intent.putExtra("data", Gson().toJson(removeNotificationRequest))
            intent.action = TimerlyForegroundService.ACTION_REMOVE_NOTIFICATION
            activity.startService(intent)
            Log.d("TimerlyPlugin","Removing Started");
            /*if (notificationIds.isEmpty()) {
                val intent1 = Intent(activity, TimerlyForegroundService::class.java)
                intent1.action = TimerlyForegroundService.ACTION_STOP_SERVICE
                activity.startService(intent1)
                EventBus.getDefault().unregister(this)
            }*/

        } else if (call.method.equals("updateNotification")) {
            val data = call.argument<String>("data")
            val createForegroundServiceRequest = Gson().fromJson<CreateForegroundServiceRequest>(data, CreateForegroundServiceRequest::class.java)
            val intent = Intent(activity, TimerlyForegroundService::class.java)
            intent.putExtra("data", Gson().toJson(createForegroundServiceRequest))
            intent.action = TimerlyForegroundService.ACTION_UPDATE_NOTIFICATION
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
