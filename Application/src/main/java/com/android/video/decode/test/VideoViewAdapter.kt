package com.android.video.decode.test

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.MediaController
import android.widget.VideoView
import java.util.*
import android.util.DisplayMetrics



class VideoViewAdapter(context: Context, videoList : ArrayList<String>, numCols : Int) : BaseVideoAdapter(context, videoList, numCols) {
    private val TAG = "VideoViewAdapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var vidView = VideoView(context)
        viewMap.put(position, vidView)

        val displayMetrics = DisplayMetrics()
        (context as Activity).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        val vidHeight = displayMetrics.heightPixels/numCols
        val vidWidth = displayMetrics.widthPixels/numCols

        vidView.setLayoutParams(ViewGroup.LayoutParams(vidWidth, vidHeight))
        return vidView
    }

    override fun loadAndPlay(vv: View, videoPath: String) {
        Log.d(TAG, "loading video file $videoPath")
        val uri = Uri.parse(videoPath)
        var videoView = vv as VideoView
        videoView.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE)
        videoView.setMediaController(MediaController(context))
        videoView.setVideoPath(videoPath)
        videoView.requestFocus()
        videoView.start()
    }
}