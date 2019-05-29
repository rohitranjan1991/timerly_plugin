package com.timerly.timerlyplugin

import android.util.Log
import com.timerly.timerlyplugin.models.TimerlyNotificationEvent
import io.flutter.app.FlutterActivity
import io.flutter.plugin.common.EventChannel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class TimerlyNotificationEventManager(val activity: FlutterActivity) {

    private var eventChannel: EventChannel? = null
    private var events: EventChannel.EventSink? = null

    fun startEventChannel() {
        eventChannel = EventChannel(activity.flutterView, "com.timerly/notification/stream")
        eventChannel!!.setStreamHandler(
                object : EventChannel.StreamHandler {
                    override fun onListen(args: Any, events: EventChannel.EventSink) {
                        Log.w("TimerlyNotification", "onListen")
                    }

                    override fun onCancel(args: Any) {
                        Log.w("TimerlyNotification", "cancelling listener")
                    }
                }
        )
    }

}