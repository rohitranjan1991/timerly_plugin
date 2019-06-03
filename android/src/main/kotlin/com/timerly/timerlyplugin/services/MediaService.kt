package com.timerly.timerlyplugin.services

import android.app.Activity
import android.media.MediaPlayer
import android.util.Log
import com.timerly.timerlyplugin.R

object MediaService {

    var mediaPlayers: MutableMap<Int, MediaPlayer> = mutableMapOf()

    fun playAlarm(id: Int, alarmID: Int, activity: Activity) {
        var resID = R.raw.alrm1
        when (alarmID) {
            1 -> {
                resID = R.raw.alrm1
            }
            2 -> {
                resID = R.raw.alrm2
            }
            3 -> {
                resID = R.raw.alrm3
            }
        }
        val mediaPlayer = MediaPlayer.create(activity, resID)
        mediaPlayer.setOnCompletionListener {
            stopAlarm(id)
        }
        mediaPlayer.start()
        mediaPlayers.put(id, mediaPlayer)
    }

    fun stopAlarm(id: Int) {
        if (mediaPlayers.containsKey(id)) {
            Log.d("MediaService", "Stopping Media Player with ID: $id")
            mediaPlayers.get(id)!!.stop()
            mediaPlayers.get(id)!!.reset()
            mediaPlayers.get(id)!!.release()
            mediaPlayers.remove(id)
        }
    }

}