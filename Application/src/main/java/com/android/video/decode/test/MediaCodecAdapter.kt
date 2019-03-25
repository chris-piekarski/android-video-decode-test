package com.android.video.decode.test

import android.app.Activity
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import java.io.IOException
import java.util.*


class MediaCodecAdapter(context: Context, videoList: ArrayList<String>, numCols: Int) : BaseVideoAdapter(context, videoList, numCols) {
    private val TAG = "MediaCodecAdapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var vidView = SurfaceView(context)
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

        var afd = context.getAssets().openFd("30fps/Footage720.mp4")
        var player = PlayerThread((vv as SurfaceView).holder.surface, afd);
        player.start()
    }

    private inner class PlayerThread(private val surface: Surface, private val fileUri: AssetFileDescriptor) : Thread() {
        private var extractor: MediaExtractor? = null
        private var decoder: MediaCodec? = null

        override fun run() {
            extractor = MediaExtractor()
            try {
                extractor!!.setDataSource(fileUri)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            for (i in 0 until extractor!!.trackCount) {
                val format = extractor!!.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime.startsWith("video/")) {
                    extractor!!.selectTrack(i)
                    try {
                        decoder = MediaCodec.createDecoderByType(mime)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    decoder!!.configure(format, surface, null, 0)
                    break
                }
            }

            if (decoder == null) {
                Log.e("DecodeActivity", "Can't find video info!")
                return
            }

            decoder!!.start()

            val inputBuffers = decoder!!.inputBuffers
            var outputBuffers = decoder!!.outputBuffers
            val info = MediaCodec.BufferInfo()
            var isEOS = false
            val startMs = System.currentTimeMillis()

            while (!Thread.interrupted()) {
                if (!isEOS) {
                    val inIndex = decoder!!.dequeueInputBuffer(10000)
                    if (inIndex >= 0) {
                        val buffer = inputBuffers[inIndex]
                        val sampleSize = extractor!!.readSampleData(buffer, 0)
                        if (sampleSize < 0) {
                            // We shouldn't stop the playback at this point, just pass the EOS
                            // flag to decoder, we will get it again from the
                            // dequeueOutputBuffer
                            Log.d("DecodeActivity", "InputBuffer BUFFER_FLAG_END_OF_STREAM")
                            decoder!!.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            isEOS = true
                        } else {
                            decoder!!.queueInputBuffer(inIndex, 0, sampleSize, extractor!!.sampleTime, 0)
                            extractor!!.advance()
                        }
                    }
                }

                val outIndex = decoder!!.dequeueOutputBuffer(info, 10000)
                when (outIndex) {
                    MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                        Log.d("DecodeActivity", "INFO_OUTPUT_BUFFERS_CHANGED")
                        outputBuffers = decoder!!.outputBuffers
                    }
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> Log.d("DecodeActivity", "New format " + decoder!!.outputFormat)
                    MediaCodec.INFO_TRY_AGAIN_LATER -> Log.d("DecodeActivity", "dequeueOutputBuffer timed out!")
                    else -> {
                        val buffer = outputBuffers[outIndex]
                        Log.v("DecodeActivity", "We can't use this buffer but render it due to the API limit, $buffer")

                        // We use a very simple clock to keep the video FPS, or the video
                        // playback will be too fast
                        while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                            try {
                                Thread.sleep(10)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                                break
                            }

                        }
                        decoder!!.releaseOutputBuffer(outIndex, true)
                    }
                }

                // All decoded frames have been rendered, we can stop playing now
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    Log.d("DecodeActivity", "OutputBuffer BUFFER_FLAG_END_OF_STREAM")
                    break
                }
            }

            decoder!!.stop()
            decoder!!.release()
            extractor!!.release()
        }
    }

}