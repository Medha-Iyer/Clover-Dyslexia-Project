package com.example.clover.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clover.R;

import java.util.ArrayList;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.VoiceViewHolder>{
    ArrayList<String> voiceResults;
    ArrayList<Integer> voiceIconResults;

    public GameAdapter(ArrayList<String> words, ArrayList<Integer> icons){
        voiceResults = words;
        voiceIconResults = icons;
    }

    @NonNull
    @Override
    public GameAdapter.VoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_list, parent, false);
        return new VoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameAdapter.VoiceViewHolder holder, int position) {
        holder.Voiceword.setText(voiceResults.get(position));
        for(int i=0; i<10; i++){
            holder.Voiceicon.setImageResource(voiceIconResults.get(i));
        }
    }

    @Override
    public int getItemCount() {
        return voiceResults.size();
    }

    public class VoiceViewHolder extends RecyclerView.ViewHolder {
        TextView Voiceword = itemView.findViewById(R.id.voiceText);
        ImageView Voiceicon = itemView.findViewById(R.id.voiceIcon);

        public VoiceViewHolder(@NonNull View itemView) {
            super(itemView);

        }
    }
}
