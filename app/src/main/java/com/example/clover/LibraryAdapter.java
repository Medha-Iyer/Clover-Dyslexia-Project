package com.example.clover;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ExampleViewHolder> implements Filterable {

    private ArrayList<LibraryCardItem> mExampleList;
    private OnItemClickListener mListener;

    private ArrayList<LibraryCardItem> exampleListFull;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public static class ExampleViewHolder extends RecyclerView.ViewHolder {
        //you have to initialize these parts for a card
        public ImageView mImageView;
        public TextView mTitle;
        public TextView mText;

        public ExampleViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            //values from item_one.xml, references to views
            mImageView = itemView.findViewById(R.id.imageOne);
            mTitle = itemView.findViewById(R.id.titleOne);
            mText = itemView.findViewById(R.id.contentOne);

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
        }
    }

    public LibraryAdapter(ArrayList<LibraryCardItem> exList){
        mExampleList = exList;
        exampleListFull = new ArrayList<>(exList);
    }

    @NonNull
    @Override
    public ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_one, parent, false);
        ExampleViewHolder evh = new ExampleViewHolder(v, mListener);
        return evh;
    }

    @Override //referring to item at certain position
    public void onBindViewHolder(@NonNull ExampleViewHolder holder, int position) {
        LibraryCardItem currentItem = mExampleList.get(position);

        holder.mImageView.setImageBitmap(convertStringToBitmap(currentItem.getImageString()));
        holder.mTitle.setText(currentItem.getItemTitle());
        holder.mText.setText(currentItem.getItemText());
    }

    private Bitmap convertStringToBitmap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    @Override //returns total amount of items in list
    public int getItemCount() {
        if(mExampleList==null){
            return 0;
        }
        return mExampleList.size();
    }

    @Override
    public Filter getFilter(){
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<LibraryCardItem> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length()==0){
                filteredList.addAll(exampleListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (LibraryCardItem item : exampleListFull){
                    if (item.getItemTitle().toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mExampleList.clear();
            mExampleList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
