package com.android.video.decode.test

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.VideoView
import java.util.*

class VideoAdapter(val context: Context, var videoList : ArrayList<String>) : BaseAdapter() {

    var viewMap = HashMap<Int, VideoView>()

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
}