package com.example.clover.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clover.R;
import com.example.clover.pojo.GameItem;

import java.util.ArrayList;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.VoiceViewHolder>{
    private ArrayList<GameItem> gameList;

    public GameAdapter(ArrayList<GameItem> g){
        gameList = g;
    }

    @NonNull
    @Override
    public GameAdapter.VoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_list, parent, false);
        GameAdapter.VoiceViewHolder evh = new GameAdapter.VoiceViewHolder(view);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull GameAdapter.VoiceViewHolder holder, int position) {
        holder.Voiceword.setText(gameList.get(position).getItemWord());
        holder.Voiceicon.setImageResource(gameList.get(position).getItemIcon());
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    public class VoiceViewHolder extends RecyclerView.ViewHolder {
        public TextView Voiceword;
        public ImageView Voiceicon;

        public VoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            Voiceword = itemView.findViewById(R.id.voiceText);
            Voiceicon = itemView.findViewById(R.id.voiceIcon);
        }
    }
}
