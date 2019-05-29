package com.timerly.timerlyplugin.models

import java.io.Serializable

data class NotificationActionButton(val name: String, val command: String) : Serializable

data class CreateForegroundServiceRequest(val serviceID: Int = 0, val title: String, val message: String, val bigContentTitle: String?, val bigContentMessage: String?, val autoCancel: Boolean? = false, val priority: Int? = 4, val setFullScreenIntent: Boolean? = false,
                                          val channelName: String, val channelID: String, val actionButtons: List<NotificationActionButton>? = listOf()) : Serializable