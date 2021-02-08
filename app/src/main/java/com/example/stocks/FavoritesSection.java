package com.example.stocks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FavoritesSection extends RecyclerView.Adapter<FavoritesSection.FavoriteItem> implements ItemMoveCallbackFavorites.ItemTouchHelperContract {
    JSONArray favoritesList;
    Context context;

    public class FavoriteItem extends RecyclerView.ViewHolder {
        TextView favoriteItemTicker;
        TextView favoriteItemCurrent;
        TextView favoriteItemChange;
        TextView favoriteItemCompanyShares;
        ImageButton favoriteItemRight;
        ImageView favoriteTrending;
        View rowView;

        public FavoriteItem(View itemView) {

            super(itemView);
            rowView = itemView;
            favoriteItemTicker = (TextView) itemView.findViewById(R.id.favoriteItemTicker);
            favoriteItemCurrent = (TextView) itemView.findViewById(R.id.favoriteItemCurrent);
            favoriteItemChange = (TextView) itemView.findViewById(R.id.favoriteItemChange);
            favoriteItemCompanyShares = (TextView) itemView.findViewById(R.id.favoriteItemCompanyShares);
            favoriteItemRight = (ImageButton) itemView.findViewById(R.id.favoriteNextButton);
            favoriteTrending = (ImageView) itemView.findViewById(R.id.trendingFavorite);
        }
    }

    public FavoritesSection(Context context, JSONArray listArray) throws JSONException {
        this.context = context;
        this.favoritesList = listArray;
    }


    @Override
    public int getItemCount() {
        return favoritesList.length(); // number of items of this section
    }

    @Override
    public FavoriteItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_item, parent, false);
        FavoriteItem vh = new FavoriteItem(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(FavoriteItem holder, int position) {
        try {
            Gson gson = new Gson();
            SharedPreferences sharedpreferences = this.context.getSharedPreferences("mypref",
                    Context.MODE_PRIVATE);
            String jsonPortfolioTickers = sharedpreferences.getString("portfolioTickers", "");
            String[] portfolioTickers = jsonPortfolioTickers.split(",");
            List<String> portfolioTickersList = Arrays.asList(portfolioTickers);
            String jsonPortfolio = sharedpreferences.getString("portfolio", "");
            JSONObject portfolio = gson.fromJson(jsonPortfolio, JSONObject.class);


            JSONObject x = (JSONObject) favoritesList.get(position);
            holder.favoriteItemTicker.setText(x.getString("ticker"));
            holder.favoriteItemCurrent.setText(String.valueOf(x.getDouble("last")));
            double change = x.getDouble("change");
            if(change<0){
                holder.favoriteItemChange.setText(String.valueOf(change*-1));
                holder.favoriteItemChange.setTextColor( this.context.getResources().getColor(R.color.red));
                holder.favoriteTrending.setImageResource(R.drawable.ic_baseline_trending_down_24);
            }
            else if(change>0){
                holder.favoriteItemChange.setText(String.valueOf(change));
                holder.favoriteItemChange.setTextColor( this.context.getResources().getColor(R.color.green));
                holder.favoriteTrending.setImageResource(R.drawable.ic_twotone_trending_up_24);
            }
            else{
                holder.favoriteItemChange.setText(String.valueOf(change));
                holder.favoriteItemChange.setTextColor( this.context.getResources().getColor(R.color.grey_700));
            }
            //check if it is in portfolio
            if(portfolioTickersList.contains(x.getString("ticker").toUpperCase())){
                try {
                    JSONObject newPref = (JSONObject) gson.fromJson((String)portfolio.get(x.getString("ticker").toUpperCase()),JSONObject.class);
                    holder.favoriteItemCompanyShares.setText(newPref.getDouble("shares")+" shares");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else
                holder.favoriteItemCompanyShares.setText(x.getString("name"));

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

            holder.favoriteItemRight.setOnClickListener(new View.OnClickListener() {
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


    public void removeItem(int position) {
        try{
            JSONObject x = (JSONObject) favoritesList.get(position);
            String tickerToRemove = x.getString("ticker");
            SharedPreferences sharedpreferences = this.context.getSharedPreferences("mypref",
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            String jsonFavorites = sharedpreferences.getString("favorites", "");
            String[] favoriteTickers = jsonFavorites.split(",");
            List<String> favoriteTickersList = new ArrayList<>(Arrays.asList(favoriteTickers));
            for (int i = 0; i < favoriteTickersList.size(); i++)
            {
                if (favoriteTickersList.get(i).equals(tickerToRemove.toUpperCase()))
                    favoriteTickersList.remove(i);
            }
            String str = TextUtils.join(",",favoriteTickersList);
            editor.putString("favorites", str);
            editor.commit();
            favoritesList.remove(position);
            notifyItemRemoved(position);
        }
        catch(Exception e){
        }
    }

    public JSONArray getData() {
        return favoritesList;
    }

    @Override
    public void onRowMoved(int fromPosition, int toPosition) throws JSONException {
        //change in shared preferences
        SharedPreferences sharedpreferences = this.context.getSharedPreferences("mypref",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        String[] favoriteTickers = sharedpreferences.getString("favorites", "").split(",");
        List<String> favoriteTickersList = new ArrayList<>(Arrays.asList(favoriteTickers));

        String temp = favoriteTickersList.get(fromPosition);
        favoriteTickersList.set(fromPosition,favoriteTickersList.get(toPosition));
        favoriteTickersList.set(toPosition,temp);
        String str = TextUtils.join(",",favoriteTickersList);
        editor.putString("favorites", str);
        editor.commit();

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                JSONObject x = (JSONObject) favoritesList.get(i);
                favoritesList.put(i,(JSONObject) favoritesList.get(i+1));
                favoritesList.put(i+1,x);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                JSONObject x = (JSONObject) favoritesList.get(i);
                favoritesList.put(i,(JSONObject) favoritesList.get(i-1));
                favoritesList.put(i-1,x);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onRowSelected(FavoritesSection.FavoriteItem myViewHolder) {
        myViewHolder.rowView.setBackgroundColor(Color.GRAY);
    }

    @Override
    public void onRowClear(FavoritesSection.FavoriteItem myViewHolder) {
        myViewHolder.rowView.setBackgroundColor(Color.WHITE);
    }

    public void dataRefresh(JSONArray newArray){
        try{
            for(int i=0;i<newArray.length();i++){
                JSONObject newObject = (JSONObject) newArray.get(i);
                JSONObject x = (JSONObject) favoritesList.get(i);
                x.put("last",newObject.getDouble("last"));
                x.put("change",newObject.getDouble("change"));
            }
            notifyDataSetChanged();
        }catch (Exception e){
        }
    }

}
