package com.timerly.timerlyplugin.models

import java.io.Serializable

// widgetType:
// 0: Timer
// 1: Stopwatch
data class TimerlyTimerEvent(val id: Int, val widgetType: Int, val command: String, val data: String? = null) : Serializable