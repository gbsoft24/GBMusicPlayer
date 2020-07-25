package com.gbsoft.gbmusicplayer.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gbsoft.gbmusicplayer.R;
import com.gbsoft.gbmusicplayer.misc.ConstantsAndEnums;
import com.gbsoft.gbmusicplayer.misc.CustomMediaPlayer;
import com.gbsoft.gbmusicplayer.misc.PlayService;
import com.gbsoft.gbmusicplayer.misc.SongsRecyclerViewAdapter;
import com.gbsoft.gbmusicplayer.model.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public static ServiceConnection conn;
    private List<Song> songsList;

    private boolean isFirstTime = true;
    public static PlayService playService;

    public static int index = -1;

    private SeekBar skBar;
    private Button btnPlay;
    private TextView tvTitle;
    private Timer myTimer;
    private TimerTask myTimerTask;

    public static SongsRecyclerViewAdapter adapter;

    private Cursor songCursor;
    private Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    public static CustomMediaPlayer mp;
    private MenuItem shuffle, repeat;

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) this.findViewById(R.id.toolbar));

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                PlayService.LocalBinder binder = (PlayService.LocalBinder) service;
                playService = binder.getService();
                Log.d("Service", "Service has been binded and connected.");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d("Service", "Service has been disconnected.");
                playService = null;
            }
        };

        myTimer = new Timer();

        if (isStoragePermissionGranted())
            Toast.makeText(this, "Storage Permission has been granted!!", Toast.LENGTH_LONG).show();
        songsList = new ArrayList<>();

        final RecyclerView recyclerView = findViewById(R.id.recView);
        adapter = new SongsRecyclerViewAdapter(songsList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        scanSongs();
        if (songsList.isEmpty())
            songsList.add(new Song("No songs still", "unknown", 0, "", null));

        mp = new CustomMediaPlayer(songsList);
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mplayer) {
                if (isFirstTime)
                    isFirstTime = false;
                mp.start();
                btnPlay.setText(R.string.pause_button);
                if (index != -1)
                    tvTitle.setText(songsList.get(index).getSongTitle());
                adapter.notifyItemChanged(index);
                seekBarAdjuster();
                recyclerView.scrollToPosition(index);
            }
        });
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mplayer) {
                btnPlay.setText(R.string.play_button);
                adapter.notifyItemChanged(index);
                playService.play(true);
                index = mp.getPlayingIndex();
            }
        });

        btnPlay = findViewById(R.id.btnPlay);

        tvTitle = findViewById(R.id.txtTitle);
        tvTitle.setSelected(true);
        skBar = findViewById(R.id.skBar);
        skBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    mp.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        myTimer.cancel();
        unbindService(conn);
        super.onDestroy();
    }

    public void onPlayClick(View view) {
        if (isFirstTime) {
            adapter.notifyItemChanged(index);
            mp.setPlayingIndex(0);
            playService.play(null);
            index = 0;
        } else if (mp.getState() == ConstantsAndEnums.MEDIA_PLAYER_STATE.PLAYING) {
            mp.pause();
            btnPlay.setText(R.string.play_button);
        } else if (mp.getState() == ConstantsAndEnums.MEDIA_PLAYER_STATE.PAUSED) {
            mp.start();
            btnPlay.setText(R.string.pause_button);
        }
    }

    public void onNextClick(View view) {
        if (mp.getState() == ConstantsAndEnums.MEDIA_PLAYER_STATE.PLAYING || mp.getState() == ConstantsAndEnums.MEDIA_PLAYER_STATE.PAUSED) {
            myTimerTask.cancel();
            adapter.notifyItemChanged(index);
            playService.play(true);
            index = mp.getPlayingIndex();
        }
    }

//    public Song next() {
//        if (shuffle.isChecked()) {
//            index = new Random().nextInt(songsList.size());
//        } else {
//            index++;
//            if (index == songsList.size() - 1)
//                index = 0;
//        }
//        return songsList.get(index);
//    }

    public void onPrevClick(View view) {
        if (mp.getState() == ConstantsAndEnums.MEDIA_PLAYER_STATE.PLAYING || mp.getState() == ConstantsAndEnums.MEDIA_PLAYER_STATE.PAUSED) {
            myTimerTask.cancel();
            adapter.notifyItemChanged(index);
            playService.play(false);
            index = mp.getPlayingIndex();
        }
    }

//    public Song prev() {
//        if (shuffle.isChecked())
//            index = new Random().nextInt(songsList.size());
//        else {
//            index--;
//            if (index == -1)
//                index = songsList.size() - 1;
//        }
//        return songsList.get(index);
//    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent playIntent = new Intent(this, PlayService.class);
        bindService(playIntent, conn, Context.BIND_AUTO_CREATE);
    }

    public void seekBarAdjuster() {
        myTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mp.isPlaying() && mp.getState() != ConstantsAndEnums.MEDIA_PLAYER_STATE.IDLE) {
                    skBar.setMax(mp.getDuration());
                    skBar.setProgress(mp.getCurrentPosition());
                }
            }
        };
        myTimer.scheduleAtFixedRate(myTimerTask, 0, 100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        repeat = menu.findItem(R.id.menu_repeat);
        shuffle = menu.findItem(R.id.menu_shuffle);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_repeat) {
            repeat.setChecked(!repeat.isChecked());
            shuffle.setChecked(false);
            mp.setShuffleOrRepeat(repeat.isChecked() ? false : null);
        } else if (item.getItemId() == R.id.menu_shuffle) {
            shuffle.setChecked(!shuffle.isChecked());
            repeat.setChecked(false);
            mp.setShuffleOrRepeat(shuffle.isChecked() ? true : null);
        } else if (item.getItemId() == R.id.menu_about_me) {
            Toast.makeText(this, "You probably know me already. In case you don't, HI! from GB", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void scanSongs() {
        songCursor = getContentResolver().query(songUri, null, null, null, null);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (songCursor != null && songCursor.moveToFirst()) {
                    songsList.clear();
                    Log.d("Scanning", "Song Scanning has been started.");
                    int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                    int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                    int songPath = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                    int songID = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                    do {
                        String currSongArtist = songCursor.getString(songArtist);
                        String currSongTitle = songCursor.getString(songTitle);
                        String currSongPath = songCursor.getString(songPath);
                        long currSongID = songCursor.getLong(songID);
                        Uri currSongUri = ContentUris.withAppendedId(songUri, currSongID);
                        songsList.add(new Song(currSongTitle, currSongArtist, currSongID, currSongPath, currSongUri));
                        Log.d("songs", currSongTitle + " id: " + currSongID);
                    } while (songCursor.moveToNext());
                }
            }
        });
    }

    public static class PlayerBroadcastReceiver extends BroadcastReceiver {
        private final String LOG_TAG = "broadcastreceiver";
        private final String ACTION_PLAY = "com.gbsoft.action.PLAY";
        private final String ACTION_NEXT = "com.gbsoft.action.NEXT";
        private final String ACTION_PREVIOUS = "com.gbsoft.action.PREVIOUS";
        private final String ACTION_PAUSE = "com.gbsoft.action.PAUSE";
        private final String ACTION_EXIT = "com.gbsoft.action.EXIT";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_PLAY:
                    Log.d(LOG_TAG, "action play");
                    if (MainActivity.mp.getState() == ConstantsAndEnums.MEDIA_PLAYER_STATE.PLAYING) {
                        MainActivity.mp.pause();
                    } else if (MainActivity.mp.getState() == ConstantsAndEnums.MEDIA_PLAYER_STATE.PAUSED) {
                        MainActivity.mp.start();
                    }
                    break;
                case ACTION_NEXT:
                    Log.d(LOG_TAG, "action next");
                    MainActivity.adapter.notifyItemChanged(index);
                    MainActivity.playService.play(true);
                    MainActivity.index = MainActivity.mp.getPlayingIndex();
                    break;
                case ACTION_PREVIOUS:
                    Log.d(LOG_TAG, "action previous");
                    MainActivity.adapter.notifyItemChanged(index);
                    MainActivity.playService.play(false);
                    MainActivity.index = MainActivity.mp.getPlayingIndex();
                    break;
                case ACTION_EXIT:
                    Log.d(LOG_TAG, "action exit");
                    NotificationManagerCompat compat = NotificationManagerCompat.from(context);
                    compat.cancel(PlayService.NOTIFICATION_ID);
                    break;

            }
        }
    }
}
