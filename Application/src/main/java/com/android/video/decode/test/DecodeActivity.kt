package com.android.video.decode.test

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.widget.GridView

import java.util.ArrayList

import java.lang.Math.ceil
import java.lang.Math.sqrt

internal enum class Resolution {
    R240, R480, R720, R1080
}

internal enum class FPS {
    R15, R30
}

class DecodeActivity : Activity() {
    private val TAG = "DecodeActivity"
    private var mVideoFileList: ArrayList<String>? = null

    private var mRes = Resolution.R240
    private var mFPS = FPS.R30

    private var mNumVids = 4
    private var mVideoAdapter: BaseVideoAdapter? = null

    companion object {
        val ASSET_URI = "content://com.android.video.decode.test.provider/"
    }

    private fun calcAutoGridSize(numVids: Int): Int {
        return ceil(sqrt(numVids.toDouble())).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_grid)

        val intent = intent
        mNumVids = intent.getIntExtra(SelectActivity.EXTRA_NUM_STREAMS, 6)
        mRes = intent.getSerializableExtra(SelectActivity.EXTRA_RESOLUTION) as Resolution
        mFPS = intent.getSerializableExtra(SelectActivity.EXTRA_FPS) as FPS

        mVideoFileList = ArrayList()

        val amanager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true)
        amanager.setStreamMute(AudioManager.STREAM_ALARM, true)
        amanager.setStreamMute(AudioManager.STREAM_MUSIC, true)
        amanager.setStreamMute(AudioManager.STREAM_RING, true)
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true)

        var vidFile = "content://com.android.video.decode.test.provider"
        var fps = "30fps"
        if (mFPS == FPS.R15) {
            fps = "15fps"
        }

        when (mRes) {
            Resolution.R240 -> vidFile = ASSET_URI + "$fps/Footage240.mp4"
            Resolution.R480 -> vidFile = ASSET_URI + "$fps/Footage480.mp4"
            Resolution.R720 -> vidFile = ASSET_URI + "$fps/Footage720.mp4"
            Resolution.R1080 -> vidFile = ASSET_URI + "$fps/Footage1080.mp4"
        }

//        mVideoFileList!!.add("rtsp://192.168.1.11:7447/i1ydjtJ05GSyUG61")
//        mVideoFileList!!.add("rtsp://192.168.1.11:7447/aqvLQsyrr2M0hFgA")
//        mVideoFileList!!.add("rtsp://192.168.1.11:7447/a875k0hDEx2zcanE")
//        mVideoFileList!!.add("rtsp://192.168.1.11:7447/yzxMoKmOxRlHFFY9")
//        mVideoFileList!!.add("rtsp://192.168.1.11:7447/oqP42ITNF8v2RDmh")
//        mVideoFileList!!.add("rtsp://192.168.1.11:7447/kgsC6ksd4TTltGzY")
//        mVideoFileList!!.add("rtsp://192.168.1.11:7447/WP59IvUDSAtUBMlj")
//        mVideoFileList!!.add("rtsp://192.168.1.11:7447/FFj0PzIfJnoQOFAb")
//        mVideoFileList!!.add("rtsp://192.168.1.11:7447/lgDidy5zVj0AzY6L")
//        mVideoFileList!!.add("rtsp://192.168.1.11:7447/lTXy0upDl3Er7nJG")
//        mVideoFileList!!.add("rtsp://192.168.1.11:7447/CsVKYimgqyYZrgAT")
//        mVideoFileList!!.add("rtsp://192.168.1.11:7447/VZZOVAk5ckrmu0f6")
//        mVideoFileList!!.add("rtsp://192.168.1.11:7447/fToXUhG5ufRgz13F")
//        mVideoFileList!!.add("rtsp://192.168.1.11:7447/gHctQPVgGZTEzKwg")
//        mVideoFileList!!.add("rtsp://192.168.1.11:7447/7x1nEgf7Eo2bYMOg")
//        mVideoFileList!!.add("rtsp://192.168.1.11:7447/6cILsqpOdFFUBgao")

        try {
            for (i in 0 until mNumVids) {
                mVideoFileList!!.add(vidFile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception thrown " + e.toString())
            e.printStackTrace()
        }

        val numCols = calcAutoGridSize(mNumVids)
        val gridView = findViewById(R.id.gridview) as GridView
        //mVideoAdapter = VideoViewAdapter(this, mVideoFileList!!, numCols)
        mVideoAdapter = MediaCodecAdapter(this, mVideoFileList!!, numCols)
        gridView.numColumns = numCols
        gridView.adapter = mVideoAdapter

        mVideoAdapter!!.playVids(mNumVids)
    }
}
