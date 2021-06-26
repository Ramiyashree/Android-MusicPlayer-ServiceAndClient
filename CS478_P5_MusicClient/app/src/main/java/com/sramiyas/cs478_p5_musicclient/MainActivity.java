package com.sramiyas.cs478_p5_musicclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.sramiyas.cs478_p5_musicaidl.MusicAIDL;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    protected static final String TAG = "MainActivity";
    protected static MusicAIDL mKeyGeneratorService;
    public static MediaPlayer mediaPlayer ;
    public static boolean mIsBound = false;
    static boolean serviceState = false;
    public  Bundle bundleSecondActivity = new Bundle();
    private Bitmap[] image = new Bitmap[7];
    private byte[][] byteArray = new byte[7][];
    static Button startService;
    static Button stopService;
    static Button GetallSongsInfo;
    static Spinner spinner;
    static int idOfOneSong;
    static int updatedIdSong;
    static Button selectOneSong;
    static TextView status;
    static String title;

    @Override
    public void onCreate(Bundle bundleData)  {

        super.onCreate(bundleData);
        setContentView(R.layout.activity_main);
        status = (TextView) findViewById(R.id.status);
        selectOneSong = (Button) findViewById(R.id.onesong);
        GetallSongsInfo = (Button) findViewById(R.id.songlist);
        startService = (Button) findViewById(R.id.start_service);
        stopService = (Button) findViewById(R.id.stop_service);
        spinner = (Spinner) findViewById(R.id.song_spinner);
        spinner.setPrompt("Select your favorite Planet!");

        //Set Spinner values
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.song_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        //Set status oncreate of the activity
        if (serviceState) {
            status.setText("Service is bound");
            checkStartForeground();
            checkbutton_availability();
        } else {
            status.setText("Service not bound");
        }

        //StartService
        startService.setOnClickListener(v -> {
            status.setText("Initating Start Service");
            Log.i(TAG, "Initating Start Service");
            checkStartForeground();
        });

        //StopService
        stopService.setOnClickListener(v -> {
            stopService();
            checkbutton_availability();
        });

        //GET ONE SPECIFIC SONG BASED ON USER SELECTION
        selectOneSong.setOnClickListener(v -> {
             idOfOneSong = spinner.getSelectedItemPosition();
             if(idOfOneSong!=0) {
                 try {
                     if (mIsBound) {
                         updatedIdSong = idOfOneSong-1;
                         Bundle bundle = mKeyGeneratorService.getOneSong(idOfOneSong-1);

                         //Extract bundle information
                         Bitmap onebitmapimage = bundle.getParcelable("oneimage");
                         title = bundle.getString("TITLES");
                         String band = bundle.getString("BANDNAME");
                         String songurl = bundle.getString("SONGURL");
                         String[] titles = {title};
                         String[] bands = {band};
                         String[] songurls = {songurl};
                         ByteArrayOutputStream onestream = new ByteArrayOutputStream();
                         onebitmapimage.compress(Bitmap.CompressFormat.PNG, 100, onestream);
                         byte[] onebyteArray = onestream.toByteArray();

                         //Store it in another bundle to pass it to secondactivity
                         bundleSecondActivity.putStringArray("SONGS", titles);
                         bundleSecondActivity.putStringArray("BANDS", bands);
                         bundleSecondActivity.putStringArray("URLs", songurls);
                         bundleSecondActivity.putByteArray("image", onebyteArray);

                         //Pass data to secondactivity and start second activity
                         Intent SecondActivityIntent = new Intent(this, SecondActivity.class);
                         SecondActivityIntent.putExtra("click", "getOneSong");
                         SecondActivityIntent.putExtras(bundleSecondActivity);
                         startActivity(SecondActivityIntent);
                     } else {
                         status.setText("Service not bound");
                         Log.i("TAG", "SERVICE NOT BOUND");
                     }
                 } catch (RemoteException e) {
                     Log.e(TAG, e.toString());
                 }
             }else{
                 Toast.makeText(this, "Please Select a song", Toast.LENGTH_LONG).show() ;
             }

        });

        //GET ALL SONG INFO FUNCTIONALITY
        GetallSongsInfo.setOnClickListener(v -> {
            try {
                if (mIsBound) {
                    //Extra bundle information
                    Bundle bundle = mKeyGeneratorService.getAllInfo();
                    String[] titles = bundle.getStringArray("TITLES");
                    String[] band = bundle.getStringArray("BANDNAME");
                    String[] songurl = bundle.getStringArray("SONGURL");
                    for (int i = 0; i < 5; i++) {
                        String s = "simage" + (i + 1);
                        Log.i("hello", s);
                        image[i] = bundle.getParcelable(s);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        image[i].compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byteArray[i] = stream.toByteArray();
                        bundleSecondActivity.putByteArray(s, byteArray[i]);
                    }
                    //Store it in another bundle to pass it to secondactivity
                    bundleSecondActivity.putStringArray("SONGS", titles);
                    bundleSecondActivity.putStringArray("BANDS", band);
                    bundleSecondActivity.putStringArray("URLs", songurl);

                    //Pass data to secondactivity and start second activity
                    Intent SecondActivityIntent = new Intent(this, SecondActivity.class);
                    SecondActivityIntent.putExtra("click", "getAllSongs");
                    SecondActivityIntent.putExtras(bundleSecondActivity);
                    startActivity(SecondActivityIntent);
                }
                else{
                    status.setText("Service Not bound");
                    Log.i(TAG, "SERVICE NOT BOUND");
                }
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
        });
    }

    //Spinner OnItem Select
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        Log.i(TAG, title + "is selected to play");
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    //Play Song
    protected static void PlaySong(String url) throws IOException {
       if(!IsPlaying()) {
            mediaPlayer= new MediaPlayer();
            mediaPlayer.setLooping(false);
            try {
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepare();
                mediaPlayer.start();
                Log.i(TAG, "Song Started playing");
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Once the song is played completely
            mediaPlayer.setOnCompletionListener(mp -> {
                mediaPlayer.release();
                mediaPlayer = null;
            });
        }
        else {
           stopSong(); //stop the previous song
           PlaySong(url); //play the newly clicked song
        }

    }

    //Pause Song
    protected static boolean pausePlayer() {
        if (mediaPlayer!= null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            status.setText("Song Paused");
            Log.i(TAG, "Song Paused");
            return true;
        } else {
            return false;
        }
    }

    //Resume Song
    public static boolean resumeSong() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            status.setText("Song Resumed");
            Log.i(TAG, "Song Resumed");
            return true;
        } else {
            return false;
        }
    }

    //Stop song
    public static boolean stopSong() {
        if (mediaPlayer!= null) {
            if (mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            status.setText("Song Stopped");
            Log.i(TAG, "Song stopped");
            return true;
        } else {
            return false;
        }
    }

    //Checking if the music is playing or not
    protected static boolean IsPlaying() {
        if (mediaPlayer != null) {
            Log.i(TAG, "Song is playing");
            return mediaPlayer.isPlaying();
        }
        else {
            Log.i(TAG, "Song is not playing");
            return false;
        }
    }

    //Enabling and Disabling command by graying out based on service status
    private  void checkbutton_availability() {
        Log.i(TAG,"Enablinga nd disabling"+mIsBound);
        if(!mIsBound) {
            startService.setEnabled(true);
            stopService.setEnabled(false);
            GetallSongsInfo.setEnabled(false);
            spinner.setEnabled(false);
            selectOneSong.setEnabled(false);
        }
        if(mIsBound) {
            startService.setEnabled(true);
            stopService.setEnabled(true);
            GetallSongsInfo.setEnabled(true);
            spinner.setEnabled(true);
            selectOneSong.setEnabled(true);
        }
    }


    // StopService
    private void stopService() {
        doUnbindService();
        Intent i = new Intent(MusicAIDL.class.getName());
        ResolveInfo info = getPackageManager().resolveService(i, 0);
        i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));
        if (getApplicationContext().stopService(i) && serviceState) {
            // successfully stopped service
            serviceState = false;
            Log.i(TAG, "stop service succeeded");
        }        status.setText("Service Stopped");
        Log.i(TAG,"Service Stopped");
        checkbutton_availability();
    }

    //Unbindingservice
    private void  doUnbindService(){
        if (mIsBound) {
            Log.i(TAG,"Unbinding Service");
            mIsBound=false;
            unbindService(this.mConnection);
            status.setText("Service Unbinded");
            Log.i(TAG,"Service Unbinded");
        }
    }

    //Starting Foregound Service
    protected void checkStartForeground() {
        Intent i = new Intent(MusicAIDL.class.getName());
        ResolveInfo info = getPackageManager().resolveService(i, 0);
        i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));
        if (getApplicationContext().startForegroundService(i) != null && !serviceState) {
            serviceState = true;
            status.setText("Service started");
            Log.i(TAG, "Startservice succeeded");

        }
        checkbind();
    }

    //Binding Service
    protected   void checkbind() {
        boolean bindValue = false;
        if (!mIsBound && serviceState) {
            Intent i = new Intent(MusicAIDL.class.getName());
            ResolveInfo info = getPackageManager().resolveService(i, 0);
            i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));
            bindValue = bindService(i, this.mConnection, Context.BIND_AUTO_CREATE);
            if (bindValue) {
                status.setText("BindService Succeeded!");
                Log.i(TAG, "bindService succeeded!");
            } else {
                Log.i(TAG, "bindService failed!");
            }
        }
    }

    //ServiceConnection and ServiceDisconnection
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder iservice) {
            mKeyGeneratorService = MusicAIDL.Stub.asInterface(iservice);
            mIsBound = true;
            checkbutton_availability();
            Log.i(TAG,"on_service connected");
        }

        public void onServiceDisconnected(ComponentName className) {
            mKeyGeneratorService = null;
            mIsBound = false;
            if(mediaPlayer!=null) {
                mediaPlayer.stop();
                Log.i(TAG, "Music Stopped");
            }
            checkbutton_availability();
            Log.i(TAG,"on_service disconnected");
        }

    };

    @Override
    protected void onStart() {
        super.onStart();
        checkbutton_availability();
    }

    @Override
    protected void onStop() {
        Log.i(TAG,"onStop Activity");
        super.onStop();
        checkbutton_availability();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG,"ondestroy Activity");
        super.onDestroy();
        stopService();
        checkbutton_availability();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkbutton_availability();
        Log.i(TAG,"onResume Activity");
    }

    @Override
    public void onPause() {
        Log.i(TAG,"onPause Activity");
        super.onPause();
    }


}