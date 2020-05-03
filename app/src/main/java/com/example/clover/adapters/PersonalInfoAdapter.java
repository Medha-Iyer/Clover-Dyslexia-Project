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

public class PersonalInfoAdapter extends RecyclerView.Adapter<PersonalInfoAdapter.PersonalViewHolder>{
    private ArrayList<PersonalInfoItem> infoList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onEditClick(PersonalInfoItem item, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public class PersonalViewHolder extends RecyclerView.ViewHolder {
        TextView infoTitle;
        TextView infoUser;
        ImageView infoIcon;
        ImageView infoEdit;

        public PersonalViewHolder(@NonNull View itemView, final PersonalInfoAdapter.OnItemClickListener listener) {
            super(itemView);
            infoTitle = itemView.findViewById(R.id.info_title);
            infoUser = itemView.findViewById(R.id.info_user);
            infoIcon = itemView.findViewById(R.id.info_icon);
            infoEdit = itemView.findViewById(R.id.editInfo);

            infoEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onEditClick(infoList.get(position), position);
                    }
                }
            });
        }
    }

    public PersonalInfoAdapter(ArrayList<PersonalInfoItem> g){
        infoList = g;
    }

    @NonNull
    @Override
    public PersonalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.personal_info_item, parent, false);
        PersonalViewHolder evh = new PersonalViewHolder(view, mListener);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull PersonalViewHolder holder, int position) {
        holder.infoTitle.setText(infoList.get(position).getItemTitle());
        holder.infoUser.setText(infoList.get(position).getItemText());
        holder.infoIcon.setImageResource(infoList.get(position).getItemIcon());
        holder.infoEdit.setImageResource(R.drawable.ic_edit);
    }

    @Override
    public int getItemCount() {
        if(infoList==null){
            return 0;
        }

        return infoList.size();
    }
}
