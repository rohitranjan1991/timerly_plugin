package com.timerly.timerlyplugin.models

data class Timer(val id: Int, var initialTime: Long, var currentTime: Long, var name: String, var isPlaying: Boolean, var alarmValue: Int? = 1)
