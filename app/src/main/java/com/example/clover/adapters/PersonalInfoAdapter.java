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

public class PersonalInfoAdapter extends RecyclerView.Adapter<PersonalInfoAdapter.ExampleViewHolder>{
    private ArrayList<PersonalInfoItem> infoList;

    public class ExampleViewHolder extends RecyclerView.ViewHolder {
        TextView infoTitle;
        TextView infoUser;
        ImageView infoIcon;
        ImageView infoEdit;

        public ExampleViewHolder(@NonNull View itemView) {
            super(itemView);
            infoTitle = itemView.findViewById(R.id.info_title);
            infoUser = itemView.findViewById(R.id.info_user);
            infoIcon = itemView.findViewById(R.id.info_icon);
            infoEdit = itemView.findViewById(R.id.editInfo);
        }
    }

    public PersonalInfoAdapter(ArrayList<PersonalInfoItem> g){
        infoList = g;
    }

    @NonNull
    @Override
    public ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.personal_info_item, parent, false);
        ExampleViewHolder evh = new ExampleViewHolder(view);
        return evh;
    }

    @Override
    public void onBindViewHolder(@NonNull ExampleViewHolder holder, int position) {
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
