package com.timerly.timerlyplugin.models


data class Lap(val count: Int, val atTime: Long)
data class Timer(val id: Int, val name: String, var currentTime: Long, val laps: MutableList<Lap>? = mutableListOf())