package com.timerly.timerlyplugin.models


data class Lap(val count: Int, val atTime: Long)
data class Timer(val id: Int, var name: String, var currentTime: Long, var isPlaying: Boolean, val laps: MutableList<Lap>? = mutableListOf())