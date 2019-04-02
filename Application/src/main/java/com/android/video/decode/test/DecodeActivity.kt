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
    R240, R480, R576, R720, R1080
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
        Log.i(TAG, "will play $mNumVids with resolution enum $mRes and fps $mFPS")

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
            Resolution.R240 -> vidFile = "content://com.android.video.decode.test.provider/$fps/Footage240.mp4"
            Resolution.R480 -> vidFile = "content://com.android.video.decode.test.provider/$fps/Footage480.mp4"
            Resolution.R576 -> vidFile = "content://com.android.video.decode.test.provider/$fps/Footage576.mp4"
            Resolution.R720 -> vidFile = "content://com.android.video.decode.test.provider/$fps/Footage720.mp4"
            Resolution.R1080 -> vidFile = "content://com.android.video.decode.test.provider/$fps/Footage1080.mp4"
        }

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
        mVideoAdapter = VideoViewAdapter(this, mVideoFileList!!, numCols)
        //mVideoAdapter = MediaCodecAdapter(this, mVideoFileList!!, numCols)
        gridView.numColumns = numCols
        gridView.adapter = mVideoAdapter

        mVideoAdapter!!.playVids(mNumVids)
    }
}
