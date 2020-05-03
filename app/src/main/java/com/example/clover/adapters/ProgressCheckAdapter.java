package com.example.clover.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clover.R;
import com.example.clover.pojo.ProgressCheckItem;

import java.util.ArrayList;

public class ProgressCheckAdapter extends RecyclerView.Adapter<ProgressCheckAdapter.ProgressHolder>{
    private ArrayList<ProgressCheckItem> gameList;
    public OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick (int position);
    }

    public void setOnItemClickListener (OnItemClickListener listener) {mListener = listener; }

    public class ProgressHolder extends RecyclerView.ViewHolder{
        TextView title;
        ImageView gameIcon;

        public ProgressHolder(@NonNull View itemView, final OnItemClickListener listener){
            super(itemView);
            title = itemView.findViewById(R.id.game_title);
            gameIcon = itemView.findViewById(R.id.game_icon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null){
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public ProgressCheckAdapter(ArrayList<ProgressCheckItem> g){ gameList = g; }

    @NonNull
    @Override
    public ProgressHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_check_item,parent,false);
        ProgressHolder vvh = new ProgressHolder(view, mListener);
        return vvh;
    }

    @Override
    public void onBindViewHolder(@NonNull ProgressHolder holder, int position) {
        holder.title.setText(gameList.get(position).getItemTitle());
        holder.gameIcon.setImageResource(gameList.get(position).getItemIcon());
    }

    @Override
    public int getItemCount() {
        if(gameList==null){
            return 0;
        }

        return gameList.size();
    }
}
