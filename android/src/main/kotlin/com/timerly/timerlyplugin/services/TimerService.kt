package com.timerly.timerlyplugin.services

import android.util.Log
import java.util.*

object TimerService {

    var isTimerStarted = false
    private var timer: Timer? = null
    private val callbackList: MutableMap<Int, ITimerTickCallback> = mutableMapOf()

    fun startTimer() {
        if (timer == null) {
            val timerTask = object : TimerTask() {
                override fun run() {
                    callbackList.forEach {
                        Log.d("TimerService", "Timer ticking")
                        it.value.onTimerTick()
                    }
                }
            }
            timer = Timer()
            timer!!.scheduleAtFixedRate(timerTask, 1000, 1000)
            isTimerStarted = true
        }
    }

    fun stopTimer() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
            isTimerStarted = false
        }
    }

    fun addTimerCallback(id: Int, iTimerTickCallback: ITimerTickCallback) {
        if (!isTimerStarted)
            startTimer()
        callbackList.put(id, iTimerTickCallback)
    }

    fun removeTimerCallback(id: Int) {
        if (callbackList.containsKey(id))
            callbackList.remove(id)
        if (callbackList.isEmpty()) {
            stopTimer()
        }
    }

}

interface ITimerTickCallback {
    fun onTimerTick(): Unit
}