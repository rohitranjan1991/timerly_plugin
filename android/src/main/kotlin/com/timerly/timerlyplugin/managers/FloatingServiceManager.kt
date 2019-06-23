package com.timerly.timerlyplugin.managers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.timerly.timerlyplugin.models.Stopwatch
import com.timerly.timerlyplugin.models.Timer
import com.timerly.timerlyplugin.services.FloatingWidgetService
import io.flutter.app.FlutterActivity
import io.flutter.plugin.common.EventChannel


object FloatingServiceManager {

    private var mBoundService: FloatingWidgetService? = null
    private var mIsBound: Boolean = false

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // This is called when the connection with the service has
            // been established, giving us the service object we can use
            // to interact with the service.  Because we have bound to a
            // explicit service that we know is running in our own
            // process, we can cast its IBinder to a concrete class and
            // directly access it.
            mBoundService = (service as FloatingWidgetService.LocalBinder).service
            mIsBound = true
            // Tell the user about this for our demo.
            Log.d(FloatingServiceManager::class.java.name, "FloatingServiceManager Service Connected")
//            Toast.makeText(applica,
//                    "Service Connected",
//                    Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            // This is called when the connection with the service has
            // been unexpectedly disconnected -- that is, its process
            // crashed. Because it is running in our same process, we
            // should never see this happen.
            mBoundService = null
            Log.d(FloatingServiceManager::class.java.name, "FloatingServiceManager Service Disconnected")
//            mBoundService!!.
//            Toast.makeText(this@Binding,
//                    R.string.local_service_disconnected,
//                    Toast.LENGTH_SHORT).show()
        }
    }

    fun doBindService(activity: FlutterActivity) {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation
        // that we know will be running in our own process (and thus
        // won't be supporting component replacement by other
        // applications).
        activity.bindService(Intent(activity, FloatingWidgetService::class.java),
                mConnection,
                Context.BIND_AUTO_CREATE)

    }

    fun doUnbindService(activity: FlutterActivity) {
        if (mIsBound) {
            // Detach our existing connection.
            activity.unbindService(mConnection)
            mIsBound = false
        }
    }

    fun addTimer(timer: Timer, activity: FlutterActivity) {
        /*if (!mIsBound) {
            doBindService(activity)
        }*/
        mBoundService?.addTimer(timer)
    }

    fun addStopwatch(stopwatch: Stopwatch) {
        mBoundService?.addStopwatch(stopwatch)
    }

    fun removeStopwatch(id: Int) {
        mBoundService?.removeStopwatch(id)
    }

    fun removeTimer(id: Int) {
        mBoundService?.removeTimer(id)
    }

    fun updateTimer(timer: Timer) {
        mBoundService?.updateTimer(timer)
    }

    fun updateStopwatch(stopwatch: Stopwatch) {
        mBoundService?.updateStopwatch(stopwatch)
    }


}