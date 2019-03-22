package com.android.video.decode.test

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

class VideoAdapter(val context: Context, var videoList : ArrayList<String>) : BaseAdapter() {
    var viewMap = HashMap<Int, VideoView>()
    private var handler = Handler()
    private val TAG = "VideoAdapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var vidView = VideoView(context)
        viewMap.put(position, vidView)
        return vidView
    }

    override fun getItem(position: Int): VideoView? {
        return viewMap.get(position)
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return videoList.size
    }

    private inner class PlayAllRunnable(private val mCurrVid: Int, private val mNumToPlay: Int) : Runnable {
        override fun run() {
            Log.d(TAG, "mCurrVid $mCurrVid")
            loadAndPlay(getItem(mCurrVid)!!, videoList.get(mCurrVid))
            if (mCurrVid < getCount() - 1 && mCurrVid < mNumToPlay - 1) {
                handler.postDelayed(PlayAllRunnable(mCurrVid + 1, mNumToPlay), 1000)
            }
        }
    }

    private fun loadAndPlay(vv: VideoView, videoPath: String) {
        Log.d(TAG, "loading video file $videoPath")
        val uri = Uri.parse(videoPath)
        vv.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE)
        vv.setMediaController(MediaController(context))
        vv.setVideoPath(videoPath)
        vv.requestFocus()
        vv.start()
    }

    fun playVids(numVids:Int){
        handler.postDelayed(PlayAllRunnable(0,numVids),1000);
    }
}