package com.mdb.yearbook.android;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class SettingsActivityAdapter extends RecyclerView.Adapter<SettingsActivityAdapter.CustomViewHolder> {
    //adapter for the roommates tab

    Context context;
    SettingsActivity activity;


    public SettingsActivityAdapter(Context context, SettingsActivity activity) {
        this.context = context;
        this.activity = activity;
    }

    @Override
    public SettingsActivityAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_card_view_1, parent, false);

        if (viewType == 0) {
            ((TextView )view.findViewById(R.id.settings_text)).setText("Join New Group");
            ((ImageView) view.findViewById(R.id.settings_image)).setImageResource(R.drawable.new_group);

        } else if (viewType == 1) {
            ((TextView )view.findViewById(R.id.settings_text)).setText("Add Parents");
            ((ImageView) view.findViewById(R.id.settings_image)).setImageResource(R.drawable.students_settings);

        } else {
            ((TextView )view.findViewById(R.id.settings_text)).setText("Logout");

        }
        return new SettingsActivityAdapter.CustomViewHolder(view);

    }

    @Override
    public void onBindViewHolder(SettingsActivityAdapter.CustomViewHolder holder, int position) {
        holder.position = position;

    }



    @Override
    public int getItemCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public class CustomViewHolder extends RecyclerView.ViewHolder {

        SettingsActivityAdapter.CustomViewHolder holder;
        int position;
        //CheckBox checkBox;

        public CustomViewHolder(View view) {
            super(view);

            holder = this;

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (position == 0) {
                        ((SettingsActivity) SettingsActivity.context).displayChangeGroups();
                    } else if (position == 1) {
                        ((SettingsActivity) SettingsActivity.context).addToGroup();

                    }
                    else {
                        ((SettingsActivity) SettingsActivity.context).logout();

                    }
                }
            });

        }
    }
}