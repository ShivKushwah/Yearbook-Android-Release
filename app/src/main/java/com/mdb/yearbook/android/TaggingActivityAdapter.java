package com.mdb.yearbook.android;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Shiv
 */

public class TaggingActivityAdapter extends RecyclerView.Adapter<TaggingActivityAdapter.CustomViewHolder> implements CompoundButton.OnCheckedChangeListener {
    //adapter for the roommates tab

    Context context;
    TaggingActivity activity;
    ArrayList<String> taggedMembers;
//    ArrayList<Boolean> taggedMembersBooleanArray;
    ArrayList<String> isTagged;
    ArrayList<String> taggedMembersCopy;

    public TaggingActivityAdapter(Context context, ArrayList<String> taggedMembers, TaggingActivity activity) {
        this.context = context;
        this.taggedMembers = taggedMembers;
        this.activity = activity;
//        taggedMembersBooleanArray = new ArrayList<>();
        isTagged = new ArrayList<>();
//        taggedMembersCopy = (ArrayList) taggedMembers.clone();
    }

    @Override
    public TaggingActivityAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tagging_card_view, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaggingActivityAdapter.CustomViewHolder holder, int position) {
        String member = taggedMembers.get(position);
        holder.checkBox.setText(member);
        holder.checkBox.setOnCheckedChangeListener(this);

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // TODO Auto-generated method stub

        if (buttonView.isChecked()) {
//            taggedMembersBooleanArray.add(taggedMembers.indexOf(buttonView.getText().toString()), true);
            isTagged.add(buttonView.getText().toString());
        }
        //checked.add((Integer) viewHolder.checkBox.getTag());
        else {
            //checked.remove((Integer) viewHolder.checkBox.getTag());
//            taggedMembersBooleanArray.add(taggedMembers.indexOf(buttonView.getText().toString()), false);
            for(int i = 0; i < isTagged.size(); i++) {
                if (isTagged.get(i).equals(buttonView.getText().toString())) {
                    isTagged.remove(i);
                }
            }
        }
    }


        @Override
    public int getItemCount() {
        return taggedMembers.size();
    }

    public void updateList(ArrayList<String> newList)
    {
        this.taggedMembers = newList;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {

        CustomViewHolder holder;
        CheckBox checkBox;

        public CustomViewHolder(View view) {
            super(view);

            holder = this;

            checkBox = (CheckBox) view.findViewById(R.id.checkBox);
            //checkBox.setOnKeyListener();

        }
    }

}
