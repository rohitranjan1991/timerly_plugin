package com.timerly.timerlyplugin

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.timerly.timerlyplugin.models.CreateForegroundServiceRequest
import com.timerly.timerlyplugin.models.NotificationActionButton
import com.timerly.timerlyplugin.models.TimerlyNotificationEvent
import org.greenrobot.eventbus.EventBus
import android.app.NotificationManager


class TimerlyForegroundService : Service() {

    private val TAG_FOREGROUND_SERVICE = "TimerlyForegroundTag"
    private var mNotificationManager: NotificationManager? = null
    private var mBuilder: NotificationCompat.Builder? = null
    private var actionButtons: List<NotificationActionButton>? = null


    override fun onBind(p0: Intent?): IBinder {
        throw UnsupportedOperationException("Not yet implemented");
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            when (action) {
                ACTION_START_FOREGROUND_SERVICE -> {
                    startForegroundService(Gson().fromJson(intent.extras!!.getString("data"), CreateForegroundServiceRequest::class.java))
                    Log.d(TAG_FOREGROUND_SERVICE, "Foreground service is started.")
                }
                ACTION_STOP_FOREGROUND_SERVICE -> {
                    stopForegroundService()
                    Log.d(TAG_FOREGROUND_SERVICE, "Foreground service is stopped.")
                }
                ACTION_UPDATE_NOTIFICATION_SERVICE -> {
                    Log.d(TAG_FOREGROUND_SERVICE, "Foreground notification is been updated.")
                    val createForegroundServiceRequest = Gson().fromJson(intent.extras!!.getString("data"), CreateForegroundServiceRequest::class.java)
                    val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    mNotificationManager.notify(createForegroundServiceRequest.serviceID, createNotification(createForegroundServiceRequest, true))
                    Toast.makeText(applicationContext, "Foreground service is updated.", Toast.LENGTH_LONG).show()
                }
                else -> {
                    actionButtons?.let {
                        val actionButton = actionButtons?.find { it.command.equals(action) }
                        if (actionButton != null) {
                            EventBus.getDefault().post(TimerlyNotificationEvent(action!!))
                        }
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /* Used to build and start foreground service. */
    private fun startForegroundService(createForegroundServiceRequest: CreateForegroundServiceRequest) {
        Log.d(TAG_FOREGROUND_SERVICE, "Start foreground service.")

        // Start foreground service.
        startForeground(createForegroundServiceRequest.serviceID, createNotification(createForegroundServiceRequest))
    }


    /**
     * Create and push the notification
     */
    fun createNotification(createForegroundServiceRequest: CreateForegroundServiceRequest, isAnUpdate: Boolean? = false): Notification? {
        /**Creates an explicit intent for an Activity in your app */
        val resultIntent = Intent(this, TimerlyForegroundService::class.java)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val resultPendingIntent = PendingIntent.getActivity(this,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        mBuilder = NotificationCompat.Builder(this)
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

//        val largeIconBitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_music_32)
//        mBuilder!!.setLargeIcon(largeIconBitmap)
        // Make the notification max priority.
        mBuilder!!.setPriority(NotificationManager.IMPORTANCE_HIGH)
        // Make head-up notification.
        mBuilder!!.setFullScreenIntent(resultPendingIntent, createForegroundServiceRequest.setFullScreenIntent!!)

        actionButtons = createForegroundServiceRequest.actionButtons!!
        for (notificationActionButton in createForegroundServiceRequest.actionButtons!!) {
            val actionIntent = Intent(this, TimerlyForegroundService::class.java);
            actionIntent.action = notificationActionButton.command
            val pendingActionIntent = PendingIntent.getService(this, 0, actionIntent, 0)
            val buttonAction = NotificationCompat.Action(android.R.drawable.ic_search_category_default, notificationActionButton.name, pendingActionIntent)
            mBuilder!!.addAction(buttonAction)
        }

        mNotificationManager = applicationContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = createForegroundServiceRequest.priority
            val notificationChannel = NotificationChannel(createForegroundServiceRequest.channelID, createForegroundServiceRequest.channelName, importance!!)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            assert(mNotificationManager != null)
            mBuilder!!.setChannelId(createForegroundServiceRequest.channelID)
            mNotificationManager!!.createNotificationChannel(notificationChannel)
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

        // Stop foreground service and remove the notification.
        stopForeground(true)

        // Stop the foreground service.
        stopSelf()
    }


    companion object {
        val ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE"

        val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"

        val ACTION_UPDATE_NOTIFICATION_SERVICE = "ACTION_UPDATE_NOTIFICATION_SERVICE"
    }
}