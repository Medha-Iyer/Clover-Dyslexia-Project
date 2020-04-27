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
    public OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick (int position);
    }

    public void setOnItemClickListener (OnItemClickListener listener){
        mListener = listener;
    }

    public class VoiceViewHolder extends RecyclerView.ViewHolder {
        TextView Voiceword;
        ImageView Voiceicon, HearIcon;

        public VoiceViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            Voiceword = itemView.findViewById(R.id.voiceText);
            Voiceicon = itemView.findViewById(R.id.voiceIcon);
            HearIcon = itemView.findViewById(R.id.hearIcon);

            HearIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public GameAdapter(ArrayList<GameItem> g){
        gameList = g;
    }

    @NonNull
    @Override
    public VoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_list, parent, false);
        VoiceViewHolder vvh = new VoiceViewHolder(view, mListener);
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
