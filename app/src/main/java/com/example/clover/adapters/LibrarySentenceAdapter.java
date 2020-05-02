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

    private ArrayList<LibraryCardItem> sentenceList;
    private OnItemClickListener mListener;

    private ArrayList<LibraryCardItem> exampleListFull;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemClick1(LibraryCardItem item, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){

        mListener = listener;
    }

    public class BlockViewHolder extends RecyclerView.ViewHolder {
        //you have to initialize these parts for a card
        public TextView mTitle;
        public TextView mText;
        public ImageView mEditBtn;

        public BlockViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            //values from item_one.xml, references to views
            mTitle = itemView.findViewById(R.id.titleOne);
            mText = itemView.findViewById(R.id.contentOne);
            mEditBtn = itemView.findViewById(R.id.editNote);

            itemView.setOnClickListener(new View.OnClickListener() {
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

            mEditBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick1(sentenceList.get(position), position);
                    }
                }
            });
        }
    }

    public LibrarySentenceAdapter(ArrayList<LibraryCardItem> exList){
        sentenceList = exList;
        exampleListFull = new ArrayList<>(exList);
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
        LibraryCardItem currentItem = sentenceList.get(position);
        currentItem.setPosition(position);

        if (currentItem.getState()) {
            holder.mEditBtn.setImageResource(R.drawable.ic_edit);
        }
        holder.mTitle.setText(currentItem.getItemTitle());
        holder.mText.setText(currentItem.getItemText());
    }

    @Override //returns total amount of items in list
    public int getItemCount() {
        if(sentenceList==null){
            return 0;
        }
        return sentenceList.size();
    }

    public void setLibraryItems(ArrayList<LibraryCardItem> items){
        sentenceList = items;
        notifyDataSetChanged();
    }
}
