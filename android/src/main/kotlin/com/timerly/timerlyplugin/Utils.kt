package com.timerly.timerlyplugin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.timerly.timerlyplugin.models.CreateForegroundServiceRequest
import com.timerly.timerlyplugin.models.TimerlyTimerEvent
import io.flutter.app.FlutterActivity

object Utils {

    val gson = Gson()

    fun formatSeconds(result: Long): String {
        return String.format("%02d", result / 3600) + ":" + String.format("%02d", result / 60 % 60) + ":" + String.format("%02d", result % 60)
    }

    fun createLocalNotification(activity: FlutterActivity, createForegroundServiceRequest: CreateForegroundServiceRequest) {
        /**Creates an explicit intent for an Activity in your app */
        val resultIntent = Intent(activity, TimerlyForegroundService::class.java)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val resultPendingIntent = PendingIntent.getActivity(activity,
                createForegroundServiceRequest.serviceId /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        val mBuilder = NotificationCompat.Builder(activity, createForegroundServiceRequest.channelId)
        mBuilder.setSmallIcon(R.mipmap.ic_launcher)

        mBuilder.setContentTitle(createForegroundServiceRequest.title)
                .setContentText(createForegroundServiceRequest.message)
                .setAutoCancel(createForegroundServiceRequest.autoCancel!!)
//                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
//                .setContentIntent(resultPendingIntent)

        if (createForegroundServiceRequest.bigContentTitle != null) {
            // Make notification show big text.
            val bigTextStyle = NotificationCompat.BigTextStyle()
            bigTextStyle.setBigContentTitle(createForegroundServiceRequest.bigContentTitle)
            bigTextStyle.bigText(createForegroundServiceRequest.bigContentMessage)
            // Set big text style.
            mBuilder.setStyle(bigTextStyle)
        }

        mBuilder.setWhen(System.currentTimeMillis())

        // Make head-up notification.
        mBuilder.setFullScreenIntent(resultPendingIntent, createForegroundServiceRequest.setFullScreenIntent!!)

        for (notificationActionButton in createForegroundServiceRequest.actionButtons!!) {
            val actionIntent = Intent(activity, TimerlyForegroundService::class.java);
            actionIntent.action = Utils.gson.toJson(TimerlyTimerEvent(id = createForegroundServiceRequest.serviceId, command = notificationActionButton.command, widgetType = createForegroundServiceRequest.widgetType))
            val pendingActionIntent = PendingIntent.getService(activity, 0, actionIntent, 0)
            val buttonAction = NotificationCompat.Action(android.R.drawable.ic_search_category_default, notificationActionButton.name, pendingActionIntent)
            mBuilder.addAction(buttonAction)
        }

        val mNotificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = createForegroundServiceRequest.channelPriority
            // Make the notification max priority.
            mBuilder.setPriority(createForegroundServiceRequest.channelPriority)
            val notificationChannel = NotificationChannel(createForegroundServiceRequest.channelId, createForegroundServiceRequest.channelName, importance)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mBuilder.setChannelId(createForegroundServiceRequest.channelId)
            mNotificationManager.createNotificationChannel(notificationChannel)
        } else {
            // Make the notification max priority.
            mBuilder.setPriority(createForegroundServiceRequest.priority!!)
        }
        val notification = mBuilder.build()
        mNotificationManager.notify(createForegroundServiceRequest.serviceId, notification)
    }
}