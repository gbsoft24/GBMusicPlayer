package com.gbsoft.gbmusicplayer.misc;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.gbsoft.gbmusicplayer.R;
import com.gbsoft.gbmusicplayer.ui.MainActivity;

import java.io.IOException;

public class PlayService extends Service {
    private MainActivity.PlayerBroadcastReceiver receiver;
    private NotificationManagerCompat notificationManagerCompat;
    private final int CHANNEL_ID = 1010;
    public static final int NOTIFICATION_ID = 1011;
    private final String LOG_CAT = "playservice";
    private final String ACTION_PLAY = "com.gbsoft.action.PLAY";
    private final String ACTION_NEXT = "com.gbsoft.action.NEXT";
    private final String ACTION_PREVIOUS = "com.gbsoft.action.PREVIOUS";
    private final String ACTION_EXIT = "com.gbsoft.action.EXIT";
    //    private final String ACTION_RESUME = "com.gbsoft.action.RESUME";
    private final String ACTION_PAUSE = "com.gbsoft.action.PAUSE";

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        notificationManagerCompat.cancel(NOTIFICATION_ID);
        MainActivity.mp.release();
        stopSelf();
        super.onDestroy();
    }

    public void play(Boolean next) {
        if (MainActivity.mp.isPlaying())
            MainActivity.mp.stop();
        if (next == null) {
            try {
                MainActivity.mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                MainActivity.mp.playNextOrPrevious(next);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(LOG_CAT, "Setting data source in created player instance");
    }

    public void showNotification(String songTitle, boolean playPause) {

        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, String.valueOf(CHANNEL_ID));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        RemoteViews views;
        if (playPause)
            views = new RemoteViews(getPackageName(), R.layout.notification_play);
        else
            views = new RemoteViews(getPackageName(), R.layout.notification_pause);
        views.setTextViewText(R.id.notifTxtView, songTitle);

        Intent playIntent = new Intent(ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 100, playIntent, 0);
        views.setOnClickPendingIntent(R.id.imgBtnPlayPause, playPendingIntent);

//            Intent pauseIntent = new Intent(ACTION_PAUSE);
//            PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 101, pauseIntent, 0);
//            views.setOnClickPendingIntent(R.id.imgBtnPlayPause, pausePendingIntent);

        //next
        Intent nextIntent = new Intent(ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 102, nextIntent, 0);
        views.setOnClickPendingIntent(R.id.imgBtnNext, nextPendingIntent);

        //previous
        Intent prevIntent = new Intent(ACTION_PREVIOUS);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, 103, prevIntent, 0);
        views.setOnClickPendingIntent(R.id.imgBtnPrev, prevPendingIntent);

        //exit
        Intent exitIntent = new Intent(ACTION_EXIT);
        PendingIntent exitPendingIntent = PendingIntent.getBroadcast(this, 104, exitIntent, 0);
        views.setOnClickPendingIntent(R.id.btnExit, exitPendingIntent);

        builder.setCustomBigContentView(views);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT).setAutoCancel(true);
        builder.setCategory(NotificationCompat.CATEGORY_RECOMMENDATION);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        Intent openMusicApp = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 105, openMusicApp, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

        notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());

    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = getString(R.string.app_name);
            String channelDesc = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(String.valueOf(CHANNEL_ID), channelName, importance);
            channel.setDescription(channelDesc);

            NotificationManager managerCompat = getSystemService(NotificationManager.class);
            managerCompat.createNotificationChannel(channel);
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        receiver = new MainActivity.PlayerBroadcastReceiver();
        notificationManagerCompat = NotificationManagerCompat.from(this);
        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(ACTION_PLAY);
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_PREVIOUS);
        filter.addAction(ACTION_EXIT);
        registerReceiver(receiver, filter);
        Log.d(LOG_CAT, "receiver has been registered");
        return mBinder;
    }
}
