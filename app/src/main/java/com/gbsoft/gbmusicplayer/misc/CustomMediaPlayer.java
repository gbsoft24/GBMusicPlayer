package com.gbsoft.gbmusicplayer.misc;

import android.media.MediaPlayer;

import com.gbsoft.gbmusicplayer.model.Song;
import com.gbsoft.gbmusicplayer.ui.MainActivity;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class CustomMediaPlayer extends MediaPlayer {

    private ConstantsAndEnums.MEDIA_PLAYER_STATE state;
    private List<Song> songsList;
    private int playingIndex = 0;
    private boolean shuffle = false, repeat = false;

    public CustomMediaPlayer(List<Song> songsList) {
        this.songsList = songsList;
        state = ConstantsAndEnums.MEDIA_PLAYER_STATE.IDLE;
        try {
            setDataSource(songsList.get(playingIndex).getSongPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reset() {
        super.reset();
        state = ConstantsAndEnums.MEDIA_PLAYER_STATE.IDLE;
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        state = ConstantsAndEnums.MEDIA_PLAYER_STATE.PLAYING;
        MainActivity.playService.showNotification(songsList.get(playingIndex).getSongTitle(), true);
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        state = ConstantsAndEnums.MEDIA_PLAYER_STATE.PAUSED;
        MainActivity.playService.showNotification(songsList.get(playingIndex).getSongTitle(), false);
    }

    @Override
    public void prepare() throws IllegalStateException, IOException {
        try {
            reset();
            setDataSource(songsList.get(playingIndex).getSongPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.prepare();
    }

    void playNextOrPrevious(boolean next) throws IOException {
        int totalIndices = songsList.size() - 1;
        if (next) {
            if (shuffle) {
                playingIndex = new Random().nextInt(totalIndices + 1);
            } else if (repeat) {

            } else {
                ++playingIndex;
                if (playingIndex == totalIndices)
                    playingIndex = 0;
            }
        } else {
            if (shuffle) {
                playingIndex = new Random().nextInt(totalIndices + 1);
            } else if (repeat) {

            } else {
                --playingIndex;
                if (playingIndex == 0)
                    playingIndex = totalIndices;
            }
        }
        if (state != ConstantsAndEnums.MEDIA_PLAYER_STATE.IDLE)
            prepare();
    }

    public ConstantsAndEnums.MEDIA_PLAYER_STATE getState() {
        return state;
    }

    public void setPlayingIndex(int index) {
        playingIndex = index;
    }

    public int getPlayingIndex() {
        return playingIndex;
    }

    String returnSongTitle() {
        return songsList.get(playingIndex).getSongTitle();
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
        state = ConstantsAndEnums.MEDIA_PLAYER_STATE.STOPPED;
    }

    @Override
    public void release() {
        super.release();
        state = ConstantsAndEnums.MEDIA_PLAYER_STATE.END;
    }

    public void setShuffleOrRepeat(Boolean shuffle) {
        if (shuffle == null) {
            this.shuffle = this.repeat = false;
        } else if (shuffle) {
            this.shuffle = true;
            this.repeat = false;
        } else {
            this.repeat = true;
            this.shuffle = false;
        }
    }
}
