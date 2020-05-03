package com.example.clover.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clover.R;
import com.example.clover.pojo.PersonalInfoItem;

import java.util.ArrayList;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private ArrayList<PersonalInfoItem> booksList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onSaveClick(PersonalInfoItem item, int position);
        void onSpeakClick(PersonalInfoItem item, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public class BookViewHolder extends RecyclerView.ViewHolder {
        TextView bookTitle;
        TextView bookDescription;
        ImageView bookCover;
        ImageView saveIcon;
        ImageView speakIcon;

        public BookViewHolder(@NonNull View itemView, final BookAdapter.OnItemClickListener listener) {
            super(itemView);
            bookTitle = itemView.findViewById(R.id.book_title);
            bookDescription = itemView.findViewById(R.id.book_description);
            bookCover = itemView.findViewById(R.id.book_cover);
            saveIcon = itemView.findViewById(R.id.book_save);
            speakIcon = itemView.findViewById(R.id.book_speak);

            saveIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onSaveClick(booksList.get(position), position);
                    }
                }
            });

            speakIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onSpeakClick(booksList.get(position), position);
                    }
                }
            });
        }

    }

    public BookAdapter(ArrayList<PersonalInfoItem> g){
        booksList = g;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_item, parent, false);
        BookViewHolder evh = new BookViewHolder(view, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        holder.bookTitle.setText(booksList.get(position).getItemTitle());
        holder.bookDescription.setText(booksList.get(position).getItemText());
        holder.bookCover.setImageResource(booksList.get(position).getItemIcon());
        holder.saveIcon.setImageResource(R.drawable.ic_save);
        holder.speakIcon.setImageResource(R.drawable.ic_hear);
    }

    @Override
    public int getItemCount() {
        if(booksList==null){
            return 0;
        }

        return booksList.size();
    }
}
