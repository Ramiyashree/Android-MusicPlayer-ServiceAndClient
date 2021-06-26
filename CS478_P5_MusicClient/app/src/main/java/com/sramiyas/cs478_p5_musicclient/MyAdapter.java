package com.sramiyas.cs478_p5_musicclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private static final String TAG = "SecondActivity";
    String  songTitle[], songArtist[], songURL[];
    Bitmap[]  images;
    String bclick;
    Context c;

    //Adapter Constructor
    public MyAdapter(Context context, String name[], String artist[], Bitmap[] image,String[] url,String click) {
        c=context;
        songTitle=name;
        songArtist=artist;
        images=image;
        songURL=url;
        bclick=click;
    }
    //creates a new Viewholder and view associated using inflater
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater= LayoutInflater.from(c);
        View view= inflater.inflate(R.layout.song_list,parent,false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    //populates the view with its associated data using the position
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.songTitleTV.setText(songTitle[position]);
        holder.songArtistTV.setText(songArtist[position]);
        holder.songImageIV.setImageBitmap(images[position]);
    }

    // return the number of items
    @Override
    public int getItemCount() {
        Log.i(TAG,"d1 is "+songTitle.length);
        return songTitle.length;
    }

    // access itemView and assigns data members for the itemView
    public class MyViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener{
        TextView songTitleTV,songArtistTV;
        ImageView songImageIV;
        private View itemView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            songTitleTV=itemView.findViewById(R.id.textView);
            songArtistTV=itemView.findViewById(R.id.artist);
            songImageIV=itemView.findViewById(R.id.imageView);
            this.itemView = itemView;
            itemView.setOnClickListener(this);
        }

        //onclick of the list item in RV the song is played
        @Override
        public void onClick(View v) {
            SecondActivity.pauseButton.setEnabled(true);
            try {
                if (MainActivity.mIsBound && MainActivity.serviceState) {
                    if (bclick.equals("getOneSong")) {
                        Log.i("music", "music"+String.valueOf(MainActivity.idOfOneSong));
                        Bundle bundle = MainActivity.mKeyGeneratorService.getOneSong(MainActivity.updatedIdSong);
                        String songurl = bundle.getString("SONGURL");
                        MainActivity.PlaySong(songurl);
                        Log.i(TAG, "Song is playing");
                    } else {
                        Bundle bundle = MainActivity.mKeyGeneratorService.getOneSong(getAdapterPosition());
                        String songurl = bundle.getString("SONGURL");
                        MainActivity.PlaySong(songurl);
                        SecondActivity.pauseButton.setEnabled(true);
                        SecondActivity.resumeButton.setEnabled(false);
                        Log.i(TAG, "Song is playing");
                    }
                    } else{
                    Log.i("TAG", "SERVICE NOT BOUND");
                }
            } catch (RemoteException | IOException e) {
                Log.e("TAG", e.toString());
            }
        }
    }
}