package com.gbsoft.gbmusicplayer.model;

/*
 * Created by Ravi Lal Pandey on 15/02/2017.
 */

import android.net.Uri;

import androidx.annotation.NonNull;

public class Song {
    private long id;
    private String songArtist;
    private String songTitle;
    private String songPath;
    private Uri songUri;

    public Uri getSongUri() {
        return songUri;
    }

    public void setSongUri(Uri songUri) {
        this.songUri = songUri;
    }

    public Song(String songTitle, String songArtist, long id, String songPath, Uri songUri) {
        this.songTitle = songTitle;
        this.songArtist = songArtist;
        this.id = id;
        this.songPath = songPath;
        this.songUri = songUri;
    }

    public Song() {
    }

    public String getSongPath() {
        return songPath;
    }

    public void setSongPath(String songPath) {
        this.songPath = songPath;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    @NonNull
    @Override
    public String toString() {
        return songTitle + " by " + songArtist;
    }
}
