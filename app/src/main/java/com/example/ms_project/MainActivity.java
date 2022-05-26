package com.example.ms_project;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ddf.minim.*;
import ddf.minim.analysis.*;
import ddf.minim.analysis.FFT;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SAMPLE_RATE = 16000;
    private static final int PERMISSION_CODE = 1;
    private ImageButton recordBtn;
    private boolean isRecording = false;
    private AudioRecord mRecorder = null;

    FFT fft;
    int fftWidth = 1024;
    int specSize;
    float[] buffer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recordBtn = findViewById(R.id.record_button);

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording) {
                    stopRecording();
                } else {
                    if (checkPermission()) {
                        startRecording();
                    } else {
                        requestPermission();
                    }
                }
            }
        });
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
    }

    private boolean checkPermission() {
        int storagePermission = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int audioPermission = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return storagePermission == PackageManager.PERMISSION_GRANTED && audioPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void startRecording() {
        isRecording = true;
        recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.stop_recording));

        int buflen = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord mRecorder =
                new AudioRecord(
                        MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_FLOAT,
                        buflen);

        mRecorder.startRecording();

//        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
//        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
//        Date date = new Date();
//        String fileName = "Recording_" + sdFormat.format(date) + ".3gp";
//        File file = new File(path, fileName);

        this.buffer = new float[fftWidth];
        fft = new FFT(fftWidth, mRecorder.getSampleRate());
        specSize = fft.specSize();
        float[] values = new float[this.fft.specSize()];

        Handler handler = new Handler();

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                if(!isRecording){
                    return;
                }

                mRecorder.read(buffer, 0, fftWidth, AudioRecord.READ_NON_BLOCKING);
                fft.forward(buffer);

                for (int i = 0; i < fft.specSize(); i++) {
                    float tmp = fft.getBand(i);
                    values[i] = tmp;
                    Log.d(TAG, String.valueOf(tmp));
                };

                handler.post(this);
            }

        };

        handler.post(r);
    }

    private void stopRecording(){
        isRecording = false;
        recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.start_recording));

        if (mRecorder != null
                && mRecorder.getState() != AudioRecord.STATE_UNINITIALIZED) {
            mRecorder.stop();
            mRecorder.release();
            Log.i("AudioCodec", "Sampling stopped");
        }
        Log.i("AudioCodec", "Recorder set to null");
        mRecorder = null;

//        mRecorder.stop();
//        mRecorder.release();
//        mRecorder = null;
    }
}