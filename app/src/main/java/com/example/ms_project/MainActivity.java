package com.example.ms_project;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1;
    private ImageButton recordBtn;
    private boolean isRecording = false;
    private MediaRecorder mRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recordBtn = findViewById(R.id.record_button);

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecording) {
                    stopRecording();
                } else {
                    if(checkPermission()){
                        startRecording();
                    } else {
                        requestPermission();
                    }
                }
            }
        });
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
    }

    private boolean checkPermission(){
        int storagePermission = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int audioPermission = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return storagePermission == PackageManager.PERMISSION_GRANTED && audioPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void startRecording(){
        isRecording = true;
        recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.stop_recording));

        mRecorder = new MediaRecorder();

        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        Date date = new Date();
        String fileName = "Recording_" + sdFormat.format(date) + ".3gp";
        File file = new File(path, fileName);

        mRecorder.setOutputFile(file);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try{
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRecorder.start();
    }

    private void stopRecording(){
        isRecording = false;
        recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.start_recording));

        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }
}