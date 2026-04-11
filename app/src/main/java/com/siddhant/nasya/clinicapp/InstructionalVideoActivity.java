package com.siddhant.nasya.clinicapp;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class InstructionalVideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructional_video);

        VideoView videoView = findViewById(R.id.videoView);
        Button btnClose = findViewById(R.id.btnCloseVideo);

        // 1. Link to the video file in res/raw
        // Replace 'nasya_tutorial' with whatever you named your file!
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.nasya_tutorial;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);

        // 2. Add Play/Pause/Seek controls
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        // 3. Start playing automatically
        videoView.start();

        btnClose.setOnClickListener(v -> finish());
    }
}