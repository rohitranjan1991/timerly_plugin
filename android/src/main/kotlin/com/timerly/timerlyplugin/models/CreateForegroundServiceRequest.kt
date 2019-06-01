package com.timerly.timerlyplugin.models

import java.io.Serializable

data class NotificationActionButton(val notificationId: Int, val name: String, val command: String) : Serializable

data class CreateForegroundServiceRequest(val serviceId: Int = 0, val title: String, val message: String, val bigContentTitle: String?, val bigContentMessage: String?, val autoCancel: Boolean? = false, val priority: Int? = 4, val setFullScreenIntent: Boolean? = false,
                                          val channelName: String, val channelId: String, val actionButtons: List<NotificationActionButton>? = listOf(), val channelPriority: Int) : Serializable


data class RemoveNotificationRequest(val notificationId: Int) : Serializable

data class GenericRequest1(val id: Int)