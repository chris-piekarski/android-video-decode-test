package com.android.video.decode.test

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.BaseAdapter
import java.util.*

abstract class BaseVideoAdapter(val context: Context, var videoList : ArrayList<String>, val numCols : Int) : BaseAdapter() {
    var viewMap = HashMap<Int, View>()
    private var handler = Handler()
    private val TAG = "MediaCodecAdapter"

    override fun getItem(position: Int): View? {
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

    abstract fun loadAndPlay(vv: View, videoPath: String)

    fun playVids(numVids:Int){
        handler.postDelayed(PlayAllRunnable(0,numVids),1000);
    }

}