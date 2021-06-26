package com.sramiyas.cs478_p5_musicclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.sramiyas.cs478_p5_musicaidl.MusicAIDL;

public class SecondActivity extends AppCompatActivity   {
    private static final String TAG = "SecondActivity";
    protected MusicAIDL mKeyGeneratorService=MainActivity.mKeyGeneratorService;
    MediaPlayer mediaPlayer =MainActivity.mediaPlayer;
    private String[] url ;
    private String[] names ;
    private String[] artists;
    private RecyclerView nameView;
    static Button pauseButton;
    static Button resumeButton;

    Bitmap bmp;
    private Bitmap[] image = new Bitmap[7];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view);
        nameView = (RecyclerView) findViewById(R.id.recycler_view);
        pauseButton = (Button) findViewById(R.id.pause_button);
        resumeButton = (Button) findViewById(R.id.resume_button);

        pauseButton.setEnabled(false);
        resumeButton.setEnabled(false);
        Intent i = getIntent();
        String click = i.getStringExtra("click");

        //Retrieving image of a single song
        if(click.equals("getOneSong")) {
            byte[] byteArray = i.getByteArrayExtra("image");
            image[0] = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            Log.i("allimage", String.valueOf(bmp));
        }

        //Retrieving images of list of songs
        if(click.equals("getAllSongs")) {
            Log.i("twoclick","playy");
            byte[][] byteArray = new byte[7][];
            String s;
            for (int j = 0; j < 5; j++) {
                s = "simage" + (j + 1);
                byteArray[j] = getIntent().getByteArrayExtra(s);
                image[j] = BitmapFactory.decodeByteArray(byteArray[j], 0, byteArray[j].length);
            }
        }
        //Extracting data from intent
        names =i.getStringArrayExtra("SONGS");
        artists =i.getStringArrayExtra("BANDS");
        url = i.getStringArrayExtra("URLs");

        MyAdapter myAdapter = new MyAdapter(this, names, artists, image, url,click);
        nameView.setAdapter(myAdapter);
        boolean b = false;

        pauseButton.setOnClickListener(v -> {
            if(MainActivity.mIsBound && MainActivity.serviceState && (MainActivity.mediaPlayer != null) && MainActivity.IsPlaying()) {
                MainActivity.pausePlayer();
                resumeButton.setEnabled(true);
                pauseButton.setEnabled(false);
            }
            else {
                Toast.makeText(this, "Client is not bound and service is stop state.", Toast.LENGTH_LONG).show() ;
            }
        });

        resumeButton.setOnClickListener(v -> {
            if(MainActivity.mIsBound && MainActivity.serviceState && (MainActivity.mediaPlayer != null) && !MainActivity.IsPlaying()) {
                MainActivity.resumeSong();
                resumeButton.setEnabled(false);
                pauseButton.setEnabled(true);
            }
            else {
                Toast.makeText(this, "Client is not bound and service is stop state.", Toast.LENGTH_LONG).show() ;
            }
        });
    }

    //Checking is music is playing or not
    private boolean IsPlaying() {
        if (mediaPlayer != null) {
            Log.i(TAG, "Music is Playing");
            return mediaPlayer.isPlaying();
        }
        else {
            return false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "Activity Stopped");
        if(MainActivity.mediaPlayer!=null) {
            MainActivity.mediaPlayer.stop();
            Log.i(TAG, "Music Stopped");
        }


    }
    @Override
        public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Activity Destroyed");
        if(MainActivity.mediaPlayer!=null) {
            MainActivity.mediaPlayer.stop();
            Log.i(TAG, "Music Stopped");
        }
    }
}