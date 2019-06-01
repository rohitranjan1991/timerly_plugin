package com.timerly.timerlyplugin.managers

import com.timerly.timerlyplugin.models.Stopwatch
import io.flutter.plugin.common.EventChannel

object StopwatchManager{

    private val stopwatches:Map<Int,Stopwatch> = mutableMapOf()
    private var eventSink: EventChannel.EventSink? = null

    fun setEventSink(es: EventChannel.EventSink?) {
        eventSink = es
    }

    fun unsetEventSink() {
        eventSink = null
    }

    fun addNewStopWatch(stopWatch:Stopwatch?){}

    fun startStopWatch(id:Int){}
    fun stopStopWatch(id:Int){}
    fun removeStopWatch(id:Int){}
    fun resetStopWatch(id:Int){}
    fun updateStopWatch(stopWatch:Stopwatch?){}


}