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
    ArrayList<GameItem> gameList;

    public class VoiceViewHolder extends RecyclerView.ViewHolder {
        TextView Voiceword;
        ImageView Voiceicon;

        public VoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            Voiceword = itemView.findViewById(R.id.voiceText);
            Voiceicon = itemView.findViewById(R.id.voiceIcon);
        }
    }

    public GameAdapter(ArrayList<GameItem> g){
        gameList = g;
    }

    @NonNull
    @Override
    public VoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_list, parent, false);
        VoiceViewHolder vvh = new VoiceViewHolder(view);
        return vvh;
    }

    @Override
    public void onBindViewHolder(@NonNull VoiceViewHolder holder, int position) {
        holder.Voiceword.setText(gameList.get(position).getItemWord());
        holder.Voiceicon.setImageResource(gameList.get(position).getItemIcon());
    }

    @Override
    public int getItemCount() {
        if(gameList==null){
            return 0;
        }

        return gameList.size();
    }
}
