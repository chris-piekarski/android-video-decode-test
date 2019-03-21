package com.android.video.decode.test;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;
import android.os.Handler;

import java.util.ArrayList;

enum Resolution
{
    R240, R480, R720, R1080;
}

public class DecodeActivity extends Activity {
    private final String TAG = "DecodeActivity";

    private ArrayList<VideoView> mVideoViewList;
    private ArrayList<String> mVideoFileList;

    private Resolution mRes = Resolution.R240;
    private int mNumVids = 4;

    private Handler mHandler;
    private class PlayAllRunnable implements Runnable{
        private int mCurrVid;
        private int mNumToPlay;

        public PlayAllRunnable(int currVid, int numberToPlay){
            mCurrVid = currVid;
            mNumToPlay = numberToPlay;
        }
        @Override
        public void run() {
            Log.d(TAG,"mCurrVid "+mCurrVid);
            loadAndPlay(mVideoViewList.get(mCurrVid), mVideoFileList.get(mCurrVid));
            if( mCurrVid < mVideoViewList.size()-1 && mCurrVid < mNumToPlay-1){
                mHandler.postDelayed(new PlayAllRunnable(mCurrVid+1, mNumToPlay), 1000);
            }
        }
    }

    private void loadAndPlay(VideoView vv, String videoPath){
        Log.d(TAG,"loading video file "+videoPath);
        Uri uri = Uri.parse(videoPath);
        vv.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE);
        vv.setMediaController(new MediaController(this));
        vv.setVideoPath(videoPath);
        vv.requestFocus();
        vv.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_grid);

        mHandler = new Handler();
        mVideoFileList = new ArrayList<>();
        mVideoViewList = new ArrayList<>();

        AudioManager amanager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);

        amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
        amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        amanager.setStreamMute(AudioManager.STREAM_RING, true);
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);

        String vidFile = "content://com.android.video.decode.test.provider";
        switch(mRes) {
            case R240:
                vidFile = "content://com.android.video.decode.test.provider/30fps/Footage240.mp4";
                break;
            case R480:
                vidFile = "content://com.android.video.decode.test.provider/30fps/Footage480.mp4";
                break;
            case R720:
                vidFile = "content://com.android.video.decode.test.provider/30fps/Footage720.mp4";
                break;
            case R1080:
                vidFile = "content://com.android.video.decode.test.provider/30fps/Footage1080.mp4";
                break;
        }

        try {
            for(int i = 0; i < mNumVids; ++i) {
                mVideoFileList.add(vidFile);
                mVideoViewList.add((VideoView) this.findViewById(R.id.vid1 + i));
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception thrown "+e.toString());
            e.printStackTrace();
        }

        mHandler.post(new PlayAllRunnable(0,mNumVids));
    }

}
