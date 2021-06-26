package com.sramiyas.cs478_p5_musiccentral;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.sramiyas.cs478_p5_musicaidl.MusicAIDL;

public class MusicService extends Service {
    private static final String TAG = "Service";
    private static final String CHANNEL_ID = "MusicPlayer";
    private static final int NOTIFICATION_ID = 999;
    Notification notification;
    private int Images[] =  {R.drawable.img1,R.drawable.img2, R.drawable.img3, R.drawable.img4, R.drawable.img5,};
    String[] Titles = {"SoundHelix Song 1","SoundHelix Song 2","SoundHelix Song 3","SoundHelix Song 4","SoundHelix Song 5"};
    String[] Band_names = {"TSchürger","TSchürger","TSchürger","TSchürger","TSchürger"};
    String[]  urls   = { "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3","https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3","https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3","https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3","https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3" };

    private final MusicAIDL.Stub mBinder = new MusicAIDL.Stub() {

        //GET SONG URL
        @Override
        public String getsongURL(int id) throws RemoteException {
            synchronized (this) {
                return urls[id];
            }
        }

        //ALL SONG INFORMATION
        @Override
        public Bundle getAllInfo() throws RemoteException {
            synchronized (this) {
                Bundle bundle = new Bundle();
                //Convert Image into bitmap
                Bitmap icon1 = BitmapFactory.decodeResource(getResources(),
                        R.drawable.img1);
                Bitmap icon2 = BitmapFactory.decodeResource(getResources(),
                        R.drawable.img2);
                Bitmap icon3 = BitmapFactory.decodeResource(getResources(),
                        R.drawable.img3);
                Bitmap icon4 = BitmapFactory.decodeResource(getResources(),
                        R.drawable.img4);
                Bitmap icon5 = BitmapFactory.decodeResource(getResources(),
                        R.drawable.img5);

                //Adding data to bundle
                bundle.putParcelable("simage1", icon1);
                bundle.putParcelable("simage2", icon2);
                bundle.putParcelable("simage3", icon3);
                bundle.putParcelable("simage4", icon4);
                bundle.putParcelable("simage5", icon5);
                bundle.putStringArray("TITLES", Titles);
                bundle.putStringArray("BANDNAME", Band_names);
                bundle.putStringArray("SONGURL", urls);
                return bundle;
            }
        }

        //Get One Sog Information
        @Override
        public Bundle getOneSong(int index) throws RemoteException {
            synchronized (this) {
                Bundle bundle = new Bundle();
                //Adding song data into bundle
                Bitmap icon1 = BitmapFactory.decodeResource(getResources(),
                        Images[index]);
                bundle.putParcelable("oneimage", icon1);
                bundle.putString("TITLES", Titles[index]);
                bundle.putString("BANDNAME", Band_names[index]);
                bundle.putString("SONGURL", urls[index]);
                return bundle;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"onbind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate");
        this.createNotificationChannel();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("com.sramiyas.cs478_p5_musicclient",
                "com.sramiyas.cs478_p5_musicclient.MainActivity"));
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0) ;
        notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setChannelId(CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true).setContentTitle("Music Playing")
                .setTicker("Music is playing!")
                .build();
        startForeground(NOTIFICATION_ID,notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        CharSequence name = "Music player notification";
        String description = "The channel for music player notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, name, importance);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel.setDescription(description);
        }
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }
    }
}