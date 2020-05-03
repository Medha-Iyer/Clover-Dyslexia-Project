package com.example.clover.adapters;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clover.R;
import com.example.clover.pojo.PersonalInfoItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import uk.co.deanwild.flowtextview.FlowTextView;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private ArrayList<PersonalInfoItem> booksList;
    private OnItemClickListener mListener;

    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private String userId = fAuth.getCurrentUser().getUid();
    DocumentReference documentReference = fStore.collection("users").document(userId);

    public interface OnItemClickListener {
        void onSaveClick(PersonalInfoItem item, int position);
        void onUnsaveClick(PersonalInfoItem item, int position);
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
                        if (saveIcon.getTag().equals("not saved")){
                            listener.onSaveClick(booksList.get(position), position);
                            saveIcon.setImageResource(R.drawable.ic_save_select);
                            saveIcon.setTag("saved");
                        } else {
                            listener.onUnsaveClick(booksList.get(position), position);
                            saveIcon.setImageResource(R.drawable.ic_save);
                            saveIcon.setTag("not saved");
                        }

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
    public void onBindViewHolder(@NonNull final BookViewHolder holder, int position) {
        holder.bookTitle.setText(booksList.get(position).getItemTitle());

        holder.bookDescription.setText(booksList.get(position).getItemText());
        holder.bookCover.setImageResource(booksList.get(position).getItemIcon());
        holder.speakIcon.setImageResource(R.drawable.ic_hear);

        documentReference = fStore.collection("users").document(userId)
                .collection("books").document(booksList.get(position).getItemTitle());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("BookAdapter", "Document exists!");
                        holder.saveIcon.setImageResource(R.drawable.ic_save_select);
                        holder.saveIcon.setTag("saved");
                    } else {
                        Log.d("BookAdapter", "Document does not exist!");
                        holder.saveIcon.setImageResource(R.drawable.ic_save);
                        holder.saveIcon.setTag("not saved");
                    }
                } else {
                    Log.d("BookAdapter", "Failed with: ", task.getException());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(booksList==null){
            return 0;
        }

        return booksList.size();
    }

    public void setBookItems(ArrayList<PersonalInfoItem> items){
        booksList = items;
        notifyDataSetChanged();
    }
}
