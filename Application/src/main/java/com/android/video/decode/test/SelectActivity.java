package com.android.video.decode.test;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.Serializable;


public class SelectActivity extends Activity {
    private final String TAG = "SelectActivity";
    private Resolution mResolution = Resolution.R240;
    private FPS mFPS = FPS.R30;
    private int mNumStreams = 6;

    final static String EXTRA_RESOLUTION = "RESOLUTION";
    final static String EXTRA_NUM_STREAMS = "NUM_STREAMS";
    final static String EXTRA_FPS = "FPS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_layout);

        showDeviceDecoderInfo();

        final TextView cv = (TextView) findViewById(R.id.numStreamsText);
        final SeekBar sk = (SeekBar) findViewById(R.id.numStreamSeek);
        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                cv.setText(String.valueOf(progress));
                cv.invalidate();
                mNumStreams = progress;
            }
        });

        RadioGroup rGroup = (RadioGroup)findViewById(R.id.resolution);
        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                RadioButton checkedRadioButton = (RadioButton)group.findViewById(checkedId);
                boolean isChecked = checkedRadioButton.isChecked();
                if (isChecked)
                {
                    switch (checkedId) {
                        case R.id.r240p:
                            mResolution = Resolution.R240;
                            break;
                        case R.id.r480p:
                            mResolution = Resolution.R480;
                            break;
                        case R.id.r576p:
                            mResolution = Resolution.R576;
                            break;
                        case R.id.r720p:
                            mResolution = Resolution.R720;
                            break;
                        case R.id.r1080p:
                            mResolution = Resolution.R1080;
                            break;
                    }
                }
            }
        });

        RadioGroup rfpsGroup = (RadioGroup)findViewById(R.id.fps);
        rfpsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                RadioButton checkedRadioButton = (RadioButton)group.findViewById(checkedId);
                boolean isChecked = checkedRadioButton.isChecked();
                if (isChecked)
                {
                    switch (checkedId) {
                        case R.id.fps15:
                            mFPS = FPS.R15;
                            break;
                        case R.id.fps30:
                            mFPS = FPS.R30;
                            break;
                    }
                }
            }
        });


        final Button button = findViewById(R.id.start);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),DecodeActivity.class);
                intent.putExtra(EXTRA_RESOLUTION, (Serializable) mResolution);
                intent.putExtra(EXTRA_FPS, (Serializable) mFPS);
                intent.putExtra(EXTRA_NUM_STREAMS, mNumStreams);
                startActivity(intent);
            }
        });
    }

    private void showDeviceDecoderInfo() {

        int mediaCodecCount = MediaCodecList.getCodecCount();

        for (int i = 0; i < mediaCodecCount; ++i) {
            MediaCodecInfo mediaCodecInfo = MediaCodecList.getCodecInfoAt(i);
            if (mediaCodecInfo.isEncoder()) {
                continue;
            }

            Log.e(TAG, "CodecName:" + mediaCodecInfo.getName());
            String [] types = mediaCodecInfo.getSupportedTypes();

            String strTypes = "";
            for (int j = 0; j < types.length; ++j) {
                if (!strTypes.isEmpty()) {
                    String SPLITE = ",  ";
                    strTypes += SPLITE;
                }
                strTypes += types[j];
            }

            Log.e(TAG, "Support Type:$strTypes");
        }
    }

}