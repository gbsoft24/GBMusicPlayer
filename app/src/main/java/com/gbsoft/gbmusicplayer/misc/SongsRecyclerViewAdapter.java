package com.gbsoft.gbmusicplayer.misc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gbsoft.gbmusicplayer.R;
import com.gbsoft.gbmusicplayer.model.Song;
import com.gbsoft.gbmusicplayer.ui.MainActivity;

import java.util.List;
/*
 * Created by Ravi Lal Pandey on 15/02/2017.
 */

public class SongsRecyclerViewAdapter extends RecyclerView.Adapter<SongsRecyclerViewAdapter.MyViewHolder> {
    private List<Song> songList;
    private Context context;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtSongTitle;
        TextView txtSongArtist;
        RelativeLayout relLayout;

        MyViewHolder(View itemView) {
            super(itemView);
            txtSongArtist = itemView.findViewById(R.id.songArtist);
            txtSongTitle = itemView.findViewById(R.id.songTitle);
            relLayout = itemView.findViewById(R.id.relLayout);
            relLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    notifyItemChanged(MainActivity.index);
                    MainActivity.index = pos;
                    MainActivity.mp.setPlayingIndex(pos);
                    MainActivity.playService.play(null);
                }
            });
        }
    }

    public SongsRecyclerViewAdapter(List<Song> mSongList, Context mContext) {
        context = mContext;
        songList = mSongList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.relLayout.setBackgroundColor(position == MainActivity.index ? context.getResources().getColor(R.color.colorAccent)
                : context.getResources().getColor(R.color.colorCustom));
        holder.txtSongTitle.setText(songList.get(position).getSongTitle());
        holder.txtSongArtist.setText(songList.get(position).getSongArtist());
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }
}

