package com.timerly.timerlyplugin

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.timerly.timerlyplugin.managers.StopwatchManager
import com.timerly.timerlyplugin.managers.TimerManager
import com.timerly.timerlyplugin.models.*
import com.timerly.timerlyplugin.models.Constants.COMMAND_FROM_SERVICE_BUTTON_OPEN_ACTIVITY
import com.timerly.timerlyplugin.services.FloatingWidgetService
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
import androidx.core.content.ContextCompat.startActivity


class TimerlyPlugin(val activity: FlutterActivity) : MethodCallHandler, EventChannel.StreamHandler {

    private var eventSink: EventChannel.EventSink? = null
    private var isForegroundServiceActive = false;
    private var notificationIds: MutableList<Int> = mutableListOf()
    /*  Permission request code to draw over other apps  */
    private val DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE = 1222
    private var isAppVisible = true

    override fun onMethodCall(call: MethodCall, result: Result): Unit {
        Log.d("TimerlyPlugin", "call.method : " + call.method)
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
        } else if (call.method.equals("getWidgetsById")) {
            val response = JsonObject();
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            val timer = TimerManager.getTimerById(gr.id)
            val stopwatch = StopwatchManager.getStopwatchById(gr.id)
            Log.d("TiemrlyPlugin", "getWidgetsById called  : " + (timer == null) + " : " + (stopwatch == null))
            if (timer == null && stopwatch == null) {
                result.success(response.toString())
            } else {
                result.success(if (timer != null) Utils.gson.toJson(timer).toString() else Utils.gson.toJson(stopwatch).toString())
            }
        } else if (call.method.equals("updateAppVisibilityState")) {
            val data = call.argument<String>("data")
            val state = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            isAppVisible = state.id != 0
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
        } else if (call.method.equals("toggleStopwatchFloatingWidget")) {
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            StopwatchManager.toggleFloatingWidget(gr.id)
        }

        // Timer Commands

        else if (call.method.equals("addTimer")) {
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<Timer>(data, Timer::class.java)
            TimerManager.addNewTimer(gr, activity)
        } else if (call.method.equals("startTimer")) {
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            TimerManager.startTimer(gr.id, activity)
        } else if (call.method.equals("stopTimer")) {
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            TimerManager.stopTimer(gr.id, activity)
        } else if (call.method.equals("removeTimer")) {
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            TimerManager.removeTimer(gr.id)
        } else if (call.method.equals("resetTimer")) {
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            TimerManager.resetTimer(gr.id, activity)
        } else if (call.method.equals("updateTimerName")) {
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest2>(data, GenericRequest2::class.java)
            TimerManager.updateTimerName(gr.id, gr.arg1 as String)
        } else if (call.method.equals("updateInitialTimeTimer")) {
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest2>(data, GenericRequest2::class.java)
            TimerManager.updateInitialTimeTimer(gr.id, (gr.arg1 as Double).toLong())
        } else if (call.method.equals("updateTimerAlarm")) {
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest2>(data, GenericRequest2::class.java)
            TimerManager.updateTimerAlarm(gr.id, (gr.arg1 as Double).toInt())
        } else if (call.method.equals("toggleTimerFloatingWidget")) {
            Log.d("Timerly Plugin", "toggleTimerFloatingWidget called")
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest1>(data, GenericRequest1::class.java)
            TimerManager.toggleFloatingWidget(gr.id)
        }

        // misc command
        else if (call.method.equals("stopAlarm")) {
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest2>(data, GenericRequest2::class.java)
            MediaService.stopAlarm(1)
        } else if (call.method.equals("playAlarm")) {
            val data = call.argument<String>("data")
            val gr = Gson().fromJson<GenericRequest2>(data, GenericRequest2::class.java)
            MediaService.stopAlarm(1)
            MediaService.playAlarm(1, (gr.arg1 as Double).toInt(), activity)
        }


        // test Commands
        else if (call.method.equals("testCmd1")) {
            startFloatingWidgetService()


        } else if (call.method.equals("testCmd2")) {
            createFloatingWidget()
        } else {
            result.notImplemented()
        }
    }

    /*  start floating widget service  */
    fun createFloatingWidget() {

        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()))
            activity.startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE)
        } else
        //If permission is granted start floating widget service
            startFloatingWidgetService()

    }


    /*  Start Floating widget service and finish current activity */
    private fun startFloatingWidgetService() {
        Log.d(TimerlyPlugin::class.java.name, "Starting Chat Head Service")
        activity.startService(Intent(activity, FloatingWidgetService::class.java))
        //activity.finish()
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
        createFloatingWidget()
    }

    override fun onCancel(p0: Any?) {
        Log.w("TimerlyNotification", "cancelling listener")
        eventSink = null
        StopwatchManager.unsetEventSink()
        TimerManager.unsetEventSink()
//        FloatingServiceManager.unsetEventSink()

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onNotificationEvent(timerlyTimerEvent: TimerlyTimerEvent) {
        Log.d("TimerlyNotification", "received EventBus Notification in Timerly Plugin")
        when (timerlyTimerEvent.command) {
            COMMAND_FROM_SERVICE_BUTTON_OPEN_ACTIVITY -> {
                if (!isAppVisible) {
                    val dialogIntent = Intent(activity, activity.javaClass)
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    activity.startActivity(dialogIntent)
                }
            }
            else -> {
                when (timerlyTimerEvent.widgetType) {
                    TimerManager.widgetType -> {
                        TimerManager.processNotificationCallback(eventSink, timerlyTimerEvent, activity)
                    }
                    StopwatchManager.widgetType -> {
                        StopwatchManager.processNotificationCallback(eventSink, timerlyTimerEvent, activity)
                    }
                }
            }
        }
    }

    init {
//        FloatingServiceManager.doBindService(activity)
        Log.d("TimerlyPlugin", "Starting Floating Widget Service")

    }

}
