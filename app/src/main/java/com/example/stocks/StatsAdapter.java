package com.example.stocks;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.MyViewHolder> {

    ArrayList statsList = new ArrayList<String>();
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView statsTextView;
        public MyViewHolder(View itemView) {
            super(itemView);
            statsTextView = (TextView) itemView.findViewById(R.id.statsTextView);
        }
    }

    public StatsAdapter(Context context, ArrayList objects) {
       this.context = context;
        statsList = objects;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stats_grid_view_item, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public int getItemCount() {
        return statsList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.statsTextView.setText((String)(statsList.get(position)));
    }
}