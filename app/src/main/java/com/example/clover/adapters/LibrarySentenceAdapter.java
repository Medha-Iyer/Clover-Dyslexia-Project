package com.example.clover.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clover.pojo.LibraryCardItem;
import com.example.clover.R;

import java.util.ArrayList;

public class LibrarySentenceAdapter extends RecyclerView.Adapter<LibrarySentenceAdapter.BlockViewHolder> {

    private ArrayList<String> sentenceList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){

        mListener = listener;
    }

    public class BlockViewHolder extends RecyclerView.ViewHolder {
        //you have to initialize these parts for a card
        public TextView mText;
        public ImageView mHearBtn;

        public BlockViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            //values from item_one.xml, references to views
            mText = itemView.findViewById(R.id.library_sentence);
            mHearBtn = itemView.findViewById(R.id.hearIcon);

            mHearBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    public LibrarySentenceAdapter(ArrayList<String> exList){
        sentenceList = exList;
    }

    @NonNull
    @Override
    public BlockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.library_sentence_item, parent, false);
        BlockViewHolder evh = new BlockViewHolder(v, mListener);
        return evh;
    }

    @Override //referring to item at certain position
    public void onBindViewHolder(@NonNull BlockViewHolder holder, int position) {
        holder.mText.setText(sentenceList.get(position));
        holder.mHearBtn.setImageResource(R.drawable.ic_hear);
    }

    @Override //returns total amount of items in list
    public int getItemCount() {
        if(sentenceList==null){
            return 0;
        }
        return sentenceList.size();
    }

    public void setLibraryItems(ArrayList<String> items){
        sentenceList = items;
        notifyDataSetChanged();
    }
}
