package com.android.video.decode.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;

import java.util.ArrayList;

import static java.lang.Math.ceil;
import static java.lang.Math.sqrt;

enum Resolution
{
    R240, R480, R720, R1080;
}

public class DecodeActivity extends Activity {
    private final String TAG = "DecodeActivity";
    private ArrayList<String> mVideoFileList;

    private Resolution mRes = Resolution.R240;
    private int mNumVids = 4;
    private VideoAdapter mVideoAdapter = null;

    private int calcAutoGridSize(int numVids){
        return (int) ceil(sqrt(numVids));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_grid);

        Intent intent = getIntent();
        mNumVids = intent.getIntExtra(SelectActivity.EXTRA_NUM_STREAMS, 6);
        mRes = (Resolution) intent.getSerializableExtra(SelectActivity.EXTRA_RESOLUTION);
        Log.i(TAG,"will play "+mNumVids+" with resolution enum "+mRes);

        mVideoFileList = new ArrayList<>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_grid);

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
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception thrown "+e.toString());
            e.printStackTrace();
        }

        int numCols = calcAutoGridSize(mNumVids);
        GridView gridView = findViewById(R.id.gridview);
        mVideoAdapter = new VideoAdapter(this, mVideoFileList, numCols);
        gridView.setNumColumns(numCols);
        gridView.setAdapter(mVideoAdapter);

        mVideoAdapter.playVids(mNumVids);
    }
}
