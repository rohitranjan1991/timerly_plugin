package com.timerly.timerlyplugin

import com.google.gson.Gson

object Utils {

    val gson = Gson()

    fun formatSeconds(result: Long): String {
        return String.format("%02d", result / 3600) + ":" + String.format("%02d", result / 60 % 60) + ":" + String.format("%02d", result % 60)
    }
}