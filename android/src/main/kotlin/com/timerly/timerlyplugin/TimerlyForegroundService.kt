package com.timerly.timerlyplugin

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.timerly.timerlyplugin.models.CreateForegroundServiceRequest
import com.timerly.timerlyplugin.models.RemoveNotificationRequest
import com.timerly.timerlyplugin.models.TimerlyTimerEvent
import org.greenrobot.eventbus.EventBus


class TimerlyForegroundService : Service() {

    private val TAG_FOREGROUND_SERVICE = "TimerlyForegroundTag"
    private var mNotificationManager: NotificationManager? = null
    private var mBuilder: NotificationCompat.Builder? = null
    private val gson = Gson()
    private var isServiceForeground: Boolean = false
    private var mainServiceId = -1;


    override fun onBind(p0: Intent?): IBinder {
        throw UnsupportedOperationException("Not yet implemented");
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent != null) {
            val action = intent.action
            when (action) {
                ACTION_ADD_NOTIFICATION -> {
                    addNotification(gson.fromJson(intent.extras!!.getString("data"), CreateForegroundServiceRequest::class.java))
                    Log.d(TAG_FOREGROUND_SERVICE, "Foreground service is started.")
                }
                ACTION_REMOVE_NOTIFICATION -> {
                    val removeNotificationRequest = gson.fromJson(intent.extras!!.getString("data"), RemoveNotificationRequest::class.java)
                    removeNotification(removeNotificationRequest);
                }
                ACTION_UPDATE_NOTIFICATION -> {
                    val createForegroundServiceRequest = gson.fromJson(intent.extras!!.getString("data"), CreateForegroundServiceRequest::class.java)
                    val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    mNotificationManager.notify(createForegroundServiceRequest.serviceId, createNotification(createForegroundServiceRequest, true))
                }
                ACTION_STOP_SERVICE -> {

                }
                else -> {
                    EventBus.getDefault().post(gson.fromJson(action, TimerlyTimerEvent::class.java))
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /* Used to add new Notification. */
    private fun addNotification(createForegroundServiceRequest: CreateForegroundServiceRequest) {
        Log.d("Timerly Plugin", "Service Id: " + createForegroundServiceRequest.serviceId)
        if (!isServiceForeground) {// Start foreground service.
            isServiceForeground = true
            mainServiceId = createForegroundServiceRequest.serviceId
            startForeground(createForegroundServiceRequest.serviceId, createNotification(createForegroundServiceRequest))
        } else {
            val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.notify(createForegroundServiceRequest.serviceId, createNotification(createForegroundServiceRequest, true))
        }
    }

    /**
     * removes the notification service
     */
    private fun removeNotification(removeNotificationRequest: RemoveNotificationRequest) {
        Log.d("TimerlyPlugin", "removeNotification called " + removeNotificationRequest.notificationId)
        if (removeNotificationRequest.notificationId == mainServiceId) {
            stopForegroundService()
        } else {
            val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.cancel(removeNotificationRequest.notificationId)
        }
    }


    /**
     * Create and push the notification
     */
    private fun createNotification(createForegroundServiceRequest: CreateForegroundServiceRequest, isAnUpdate: Boolean? = false): Notification? {
        /**Creates an explicit intent for an Activity in your app */
        val resultIntent = Intent(this, TimerlyForegroundService::class.java)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val resultPendingIntent = PendingIntent.getActivity(this,
                createForegroundServiceRequest.serviceId /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        mBuilder = NotificationCompat.Builder(this, createForegroundServiceRequest.channelId)
        mBuilder!!.setSmallIcon(R.mipmap.ic_launcher)

        mBuilder!!.setContentTitle(createForegroundServiceRequest.title)
                .setContentText(createForegroundServiceRequest.message)
                .setAutoCancel(createForegroundServiceRequest.autoCancel!!)
//                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent)

        if (createForegroundServiceRequest.bigContentTitle != null) {
            // Make notification show big text.
            val bigTextStyle = NotificationCompat.BigTextStyle()
            bigTextStyle.setBigContentTitle(createForegroundServiceRequest.bigContentTitle)
            bigTextStyle.bigText(createForegroundServiceRequest.bigContentMessage)
            // Set big text style.
            mBuilder!!.setStyle(bigTextStyle)
        }

        mBuilder!!.setWhen(System.currentTimeMillis())

        // Make head-up notification.
        mBuilder!!.setFullScreenIntent(resultPendingIntent, createForegroundServiceRequest.setFullScreenIntent!!)

        for (notificationActionButton in createForegroundServiceRequest.actionButtons!!) {
            val actionIntent = Intent(this, TimerlyForegroundService::class.java);
            actionIntent.action = gson.toJson(TimerlyTimerEvent(id = createForegroundServiceRequest.serviceId, command = notificationActionButton.command, widgetType = createForegroundServiceRequest.widgetType))
            val pendingActionIntent = PendingIntent.getService(this, 0, actionIntent, 0)
            val buttonAction = NotificationCompat.Action(android.R.drawable.ic_search_category_default, notificationActionButton.name, pendingActionIntent)
            mBuilder!!.addAction(buttonAction)
        }

        mNotificationManager = applicationContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = createForegroundServiceRequest.channelPriority
            mBuilder!!.setPriority(createForegroundServiceRequest.channelPriority)
            val notificationChannel = NotificationChannel(createForegroundServiceRequest.channelId, createForegroundServiceRequest.channelName, importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            assert(mNotificationManager != null)
            mBuilder!!.setChannelId(createForegroundServiceRequest.channelId)
            mNotificationManager!!.createNotificationChannel(notificationChannel)
        } else {
            // Make the notification max priority.
            mBuilder!!.setPriority(createForegroundServiceRequest.priority!!)
        }
        assert(mNotificationManager != null)
        val notification = mBuilder!!.build()
        if (isAnUpdate!!) {
            notification.flags = Notification.FLAG_ONGOING_EVENT
        }
        return notification
    }

    private fun stopForegroundService() {
        Log.d(TAG_FOREGROUND_SERVICE, "Stop foreground service.")
        isServiceForeground = false
        // Stop foreground service and remove the notification.
        stopForeground(true)

        // Stop the foreground service.
        stopSelf()
    }


    companion object {

        val ACTION_UPDATE_NOTIFICATION = "ACTION_UPDATE_NOTIFICATION"
        val ACTION_REMOVE_NOTIFICATION = "ACTION_REMOVE_NOTIFICATION"
        val ACTION_ADD_NOTIFICATION = "ACTION_ADD_NOTIFICATION"
        val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    }
}