package com.android.video.decode.test

import android.app.Activity
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import java.io.IOException
import java.util.*
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.android.video.decode.test.DecodeActivity.Companion.ASSET_URI


class MediaCodecAdapter(context: Context, videoList: ArrayList<String>, numCols: Int) : BaseVideoAdapter(context, videoList, numCols) {
    private val TAG = "asdfasdfMediaCodecAdapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var vidView = Preview(context)
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
        //listAllCodecNames()


    }

    private fun listAllCodecNames() {
        Log.e(TAG, "Listing out available codecs: ")
        val numCodecs = MediaCodecList.getCodecCount()
        for (i in 0 until numCodecs) {
            val codecInfo = MediaCodecList.getCodecInfoAt(i)
            Log.e(TAG, codecInfo.name)
            codecInfo.name

        }
    }


    internal inner class Preview(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
        private var numStreams = 0

        init {
            numStreams = 0
            Log.d(TAG, "Preview()")
            val holder = holder
            holder.addCallback(this)
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            Log.d(TAG, "surfaceCreated")
            var afd = context.getAssets().openFd("15fps/Footage480.mp4")
            var player:PlayerThread? = null
//            if(numStreams < 5) {
//                player = PlayerThread(holder.surface, afd, "OMX.google.h264.decoder")
//            } else {
                player = PlayerThread(holder.surface, afd, "OMX.qcom.video.decoder.avc")
//            }
            numStreams++
            player?.start()
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            Log.d(TAG, "surfaceDestroyed")
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            Log.d(TAG, "surfaceChanged")
        }
    }








    private inner class PlayerThread(private val surface: Surface, private val fileUri: AssetFileDescriptor, private val codecName:String? = null) : Thread() {
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
                        if(codecName != null) {
                            Log.d(TAG, "Creating instance of requested codec: " + codecName)
                            decoder = MediaCodec.createByCodecName(codecName)
                        }else {
                            decoder = MediaCodec.createDecoderByType(mime)
                        }
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
                    val inIndex = decoder!!.dequeueInputBuffer(100000)
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

                val outIndex = decoder!!.dequeueOutputBuffer(info, 100000)
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