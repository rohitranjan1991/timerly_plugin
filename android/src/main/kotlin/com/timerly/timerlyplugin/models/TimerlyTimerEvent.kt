package com.timerly.timerlyplugin.models

import java.io.Serializable

data class TimerlyTimerEvent(val id: Int, val command: String, val data: String? = null) : Serializable