package com.example.stocks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class PortfolioSection extends RecyclerView.Adapter<PortfolioSection.PortfolioItem> implements ItemMoveCallbackPortfolio.ItemTouchHelperContract {
    JSONArray portfolioList;
    Context context;

    public class PortfolioItem extends RecyclerView.ViewHolder {
        TextView portfolioItemTicker;
        TextView portfolioItemCurrent;
        TextView portfolioItemChange;
        TextView portfolioItemCompanyShares;
        ImageButton portfolioItemRight;
        ImageView portfolioTrending;
        View rowView;

        public PortfolioItem(View itemView) {
            super(itemView);
            rowView = itemView;

            portfolioItemTicker = (TextView) itemView.findViewById(R.id.portfolioItemTicker);
            portfolioItemCurrent = (TextView) itemView.findViewById(R.id.portfolioItemCurrent);
            portfolioItemChange = (TextView) itemView.findViewById(R.id.portfolioItemChange);
            portfolioItemCompanyShares = (TextView) itemView.findViewById(R.id.portfolioItemCompanyShares);
            portfolioItemRight = (ImageButton) itemView.findViewById(R.id.portfolioNextButton);
            portfolioTrending = (ImageView) itemView.findViewById(R.id.trendingPortfolio);
        }
    }

    public PortfolioSection(Context context, JSONArray listArray) throws JSONException {
        this.context = context;
        this.portfolioList = listArray;
    }

    @Override
    public int getItemCount() {
        return portfolioList.length(); // number of items of this section
    }

    @Override
    public PortfolioItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.portfolio_item, parent, false);
        PortfolioItem vh = new PortfolioItem(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(PortfolioItem holder, int position) {
        try {
            JSONObject x = (JSONObject) portfolioList.get(position);
            holder.portfolioItemTicker.setText(x.getString("ticker"));
            holder.portfolioItemCurrent.setText(String.valueOf(x.getDouble("last")));
            double change = x.getDouble("change");
            if(change<0){
                holder.portfolioItemChange.setText(String.valueOf(change*-1));
                holder.portfolioItemChange.setTextColor( this.context.getResources().getColor(R.color.red));
                holder.portfolioTrending.setImageResource(R.drawable.ic_baseline_trending_down_24);
            }
            else if(change>0){
                holder.portfolioItemChange.setText(String.valueOf(change));
                holder.portfolioItemChange.setTextColor( this.context.getResources().getColor(R.color.green));
                holder.portfolioTrending.setImageResource(R.drawable.ic_twotone_trending_up_24);
            }
            else{
                holder.portfolioItemChange.setText(String.valueOf(change));
                holder.portfolioItemChange.setTextColor( this.context.getResources().getColor(R.color.grey_700));
            }
            holder.portfolioItemCompanyShares.setText(String.valueOf(x.getDouble("numShares"))+" shares");

            holder.portfolioItemRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent i = new Intent(v.getContext(), DetailedStockActivity.class);
                        i.putExtra(Intent.EXTRA_TEXT,x.getString("ticker").toUpperCase());
                        v.getContext().startActivity(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent i = new Intent(v.getContext(), DetailedStockActivity.class);
                        i.putExtra(Intent.EXTRA_TEXT,x.getString("ticker").toUpperCase());
                        v.getContext().startActivity(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRowMoved(int fromPosition, int toPosition) throws JSONException {

        SharedPreferences sharedpreferences = this.context.getSharedPreferences("mypref",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        String[] portfolioTickers = sharedpreferences.getString("portfolioTickers", "").split(",");
        List<String> portfolioTickersList = new ArrayList<>(Arrays.asList(portfolioTickers));

        String temp = portfolioTickersList.get(fromPosition);
        portfolioTickersList.set(fromPosition,portfolioTickersList.get(toPosition));
        portfolioTickersList.set(toPosition,temp);
        String str = TextUtils.join(",",portfolioTickersList);
        editor.putString("portfolioTickers", str);
        editor.commit();

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                JSONObject x = (JSONObject) portfolioList.get(i);
                portfolioList.put(i,(JSONObject) portfolioList.get(i+1));
                portfolioList.put(i+1,x);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                JSONObject x = (JSONObject) portfolioList.get(i);
                portfolioList.put(i,(JSONObject) portfolioList.get(i-1));
                portfolioList.put(i-1,x);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onRowSelected(PortfolioItem myViewHolder) {
        myViewHolder.rowView.setBackgroundColor(Color.GRAY);
    }

    @Override
    public void onRowClear(PortfolioItem myViewHolder) {
        myViewHolder.rowView.setBackgroundColor(Color.WHITE);
    }

    public void dataRefresh(JSONArray newArray){
        try{
            for(int i=0;i<newArray.length();i++){
                JSONObject newObject = (JSONObject) newArray.get(i);
                JSONObject x = (JSONObject) portfolioList.get(i);
                x.put("last",newObject.getDouble("last"));
                x.put("change",newObject.getDouble("change"));
            }
            notifyDataSetChanged();
        }catch (Exception e){
        }
    }
}
