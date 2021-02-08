package com.example.stocks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.Date;



public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedpreferences;
    JSONObject favorites, portfolio;
    JSONArray favoritesArray, portfolioArray;
    FavoritesSection favoriteAdapter;
    PortfolioSection portfolioAdapter;
    GridLayoutManager gridLayoutManager, gridLayoutManagerPortfolio;
    RecyclerView favoritesRecycler, portfolioRecycler;
    double netWorth;
    public static final String mypreference = "mypref";
    public static final String portfolioKey = "portfolio";
    public static final String favoritesKey = "favorites";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout mainProgress  = (LinearLayout) findViewById(R.id.mainProgress);
        LinearLayout linearLayoutMain = (LinearLayout)findViewById(R.id.mainLinearLayout);
        mainProgress.setVisibility(View.VISIBLE);
        linearLayoutMain.setVisibility(View.GONE);


        favoritesRecycler = (RecyclerView)findViewById(R.id.favoritesRecyclerView);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(),1);
        favoritesRecycler.setLayoutManager(gridLayoutManager); // set LayoutManager to RecyclerView

        portfolioRecycler = (RecyclerView)findViewById(R.id.portfolioRecyclerView);
        gridLayoutManagerPortfolio = new GridLayoutManager(getApplicationContext(),1);
        portfolioRecycler.setNestedScrollingEnabled(false);
        portfolioRecycler.setLayoutManager(gridLayoutManagerPortfolio); // set LayoutManager to RecyclerView


        Toolbar myToolbar = (Toolbar) findViewById(R.id.homeToolBar);
        setSupportActionBar(myToolbar);

        //tiingo text
        TextView tiingoText = (TextView)findViewById(R.id.tiingoText);
        tiingoText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiingo.com/"));
                MainActivity.this.startActivity(intent);
            }
        });

        //set date
        TextView dateText = (TextView)findViewById(R.id.date);
        Date currDate = new Date();
        dateText.setText(DateFormat.getDateInstance().format(currDate));

        sharedpreferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        Gson gson = new Gson();


        //set start amount
        if (!sharedpreferences.contains("startAmount")) {
            editor.putFloat("startAmount", 20000);
            editor.commit();
        }

        netWorth = sharedpreferences.getFloat("startAmount",0);

        //get portfolio
        if (!sharedpreferences.contains("portfolioTickers")) {
            editor.putString("portfolioTickers", "");
            editor.commit();
        }
        String jsonPortfolioTickers = sharedpreferences.getString("portfolioTickers", "");

        if (!sharedpreferences.contains(portfolioKey)) {
            String jsonObject = gson.toJson(new JSONObject());
            editor.putString("portfolio", jsonObject);
            editor.commit();
        }
        String jsonPortfolio = sharedpreferences.getString(portfolioKey, "");
        portfolio = gson.fromJson(jsonPortfolio, JSONObject.class);

        //get favorites
        if (!sharedpreferences.contains(favoritesKey)) {
            editor.putString("favorites", "");
            editor.commit();
        }
        String jsonFavorites = sharedpreferences.getString(favoritesKey, "");
            //get data for favorites
            ApiCall.makeFavorites(this, jsonFavorites, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject responseObject = new JSONObject(response);
                        favoritesArray = responseObject.getJSONArray("watchlistData");
                        portfolioArray=new JSONArray();

                        ApiCall.makePortfolio(MainActivity.this, jsonPortfolioTickers, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject responseObject = new JSONObject(response);
                                    //get data and create objects
                                    JSONArray x  = responseObject.getJSONArray("portfolioData");

                                    for(int i =0; i<x.length(); i++){
                                        JSONObject temp = (JSONObject) x.get(i);
                                        JSONObject newObject = new JSONObject();
                                        newObject.put("ticker",temp.getString("ticker"));
                                        newObject.put("last",temp.getDouble("last"));
                                        newObject.put("change",temp.getDouble("change"));
                                        JSONObject tickerObjectPref = gson.fromJson((String) portfolio.get(temp.getString("ticker")),JSONObject.class);
                                        Double numShares =tickerObjectPref.getDouble("shares");
                                        newObject.put("numShares",numShares);
                                        netWorth+=(numShares*temp.getDouble("last"));
                                        portfolioArray.put(newObject);
                                    }
                                    BigDecimal bd1 = new BigDecimal(netWorth).setScale(2, RoundingMode.HALF_UP);
                                    ((TextView)findViewById(R.id.netWorth)).setText(String.valueOf(bd1));
                                    portfolioAdapter = new PortfolioSection(MainActivity.this, portfolioArray);
                                    portfolioRecycler.setHasFixedSize(false);
                                    ItemTouchHelper.Callback callback = new ItemMoveCallbackPortfolio(portfolioAdapter);
                                    ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                                    touchHelper.attachToRecyclerView(portfolioRecycler);
                                    portfolioRecycler.setAdapter(portfolioAdapter);
                                    RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(favoritesRecycler.getContext(), R.drawable.divider));
                                    portfolioRecycler.addItemDecoration(dividerItemDecoration);
                                    mainProgress.setVisibility(View.GONE);
                                    setProgressBarIndeterminateVisibility(false);
                                    linearLayoutMain.setVisibility(View.VISIBLE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                            }
                        });

                        favoriteAdapter = new FavoritesSection(MainActivity.this, favoritesArray);
                        favoritesRecycler.setHasFixedSize(true);
                        enableSwipeToDeleteAndUndoFavorites();
                        ItemTouchHelper.Callback callback = new ItemMoveCallbackFavorites(favoriteAdapter);
                        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                        touchHelper.attachToRecyclerView(favoritesRecycler);
                        favoritesRecycler.setAdapter(favoriteAdapter);
                        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(favoritesRecycler.getContext(), R.drawable.divider));
                        favoritesRecycler.addItemDecoration(dividerItemDecoration);


                        Handler handler = new Handler();
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                String jsonPortfolioTickers = sharedpreferences.getString("portfolioTickers", "");
                                String jsonPortfolio = sharedpreferences.getString(portfolioKey, "");
                                portfolio = gson.fromJson(jsonPortfolio, JSONObject.class);
                                String jsonFavorites = sharedpreferences.getString(favoritesKey, "");
                                ApiCall.makeFavorites(MainActivity.this, jsonFavorites, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONObject responseObject = new JSONObject(response);
                                            favoritesArray=new JSONArray();
                                            favoritesArray = responseObject.getJSONArray("watchlistData");
                                            portfolioArray=new JSONArray();

                                            ApiCall.makePortfolio(MainActivity.this, jsonPortfolioTickers, new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    try {
                                                        Log.e("Data","New data fetched");
                                                        JSONObject responseObject = new JSONObject(response);
                                                        //get data and create objects
                                                        JSONArray x  = responseObject.getJSONArray("portfolioData");
                                                        for(int i =0; i<x.length(); i++){
                                                            JSONObject temp = (JSONObject) x.get(i);
                                                            JSONObject newObject = new JSONObject();
                                                            newObject.put("ticker",temp.getString("ticker"));
                                                            newObject.put("last",temp.getDouble("last"));
                                                            newObject.put("change",temp.getDouble("change"));
                                                            JSONObject tickerObjectPref = gson.fromJson((String) portfolio.get(temp.getString("ticker")),JSONObject.class);
                                                            Double numShares =tickerObjectPref.getDouble("shares");
                                                            newObject.put("numShares",numShares);
                                                            netWorth+=(numShares*temp.getDouble("last"));
                                                            portfolioArray.put(newObject);
                                                        }

                                                       portfolioAdapter.dataRefresh(portfolioArray);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                }
                                            });
                                            favoriteAdapter.dataRefresh(favoritesArray);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                    }
                                });
                                handler.postDelayed(this, 15000);
                            }
                        };

                        handler.postDelayed(runnable, 15000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       getMenuInflater().inflate(R.menu.menu, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);
        searchView.onActionViewExpanded();
        Autocomplete.setAutocompleteListeners(this, searchView);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void enableSwipeToDeleteAndUndoFavorites() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                try {
                    final int position = viewHolder.getAdapterPosition();
                    final JSONObject item = (JSONObject) favoriteAdapter.getData().get(position);

                    favoriteAdapter.removeItem(position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(favoritesRecycler);
    }


    private void getFavsPortfolio(){
        LinearLayout mainProgress  = (LinearLayout) findViewById(R.id.mainProgress);
        LinearLayout linearLayoutMain = (LinearLayout)findViewById(R.id.mainLinearLayout);

        favoritesRecycler = (RecyclerView)findViewById(R.id.favoritesRecyclerView);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(),1);
        favoritesRecycler.setLayoutManager(gridLayoutManager); // set LayoutManager to RecyclerView

        portfolioRecycler = (RecyclerView)findViewById(R.id.portfolioRecyclerView);
        gridLayoutManagerPortfolio = new GridLayoutManager(getApplicationContext(),1);
        portfolioRecycler.setNestedScrollingEnabled(false);
        portfolioRecycler.setLayoutManager(gridLayoutManagerPortfolio); // set LayoutManager to RecyclerView


        sharedpreferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);
        Gson gson = new Gson();

        String jsonPortfolioTickers = sharedpreferences.getString("portfolioTickers", "");
        String jsonPortfolio = sharedpreferences.getString(portfolioKey, "");
        portfolio = gson.fromJson(jsonPortfolio, JSONObject.class);
        String jsonFavorites = sharedpreferences.getString(favoritesKey, "");

        //get data for favorites
        ApiCall.makeFavorites(this, jsonFavorites, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    if(jsonFavorites==""){
                        ((TextView)findViewById(R.id.netWorth)).setVisibility(View.GONE);
                        ((TextView)findViewById(R.id.netWorthText)).setVisibility(View.GONE);
                        findViewById(R.id.horizontalLine).setVisibility(View.GONE);
                    }
                    JSONObject responseObject = new JSONObject(response);
                    JSONArray favoritesArray1 = responseObject.getJSONArray("watchlistData");
                    JSONArray portfolioArray1=new JSONArray();

                    ApiCall.makePortfolio(MainActivity.this, jsonPortfolioTickers, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                double netWorth1 = sharedpreferences.getFloat("startAmount",0);
                                JSONObject responseObject = new JSONObject(response);
                                //get data and create objects
                                JSONArray x  = responseObject.getJSONArray("portfolioData");
                                for(int i =0; i<x.length(); i++){
                                    JSONObject temp = (JSONObject) x.get(i);
                                    JSONObject newObject = new JSONObject();
                                    newObject.put("ticker",temp.getString("ticker"));
                                    newObject.put("last",temp.getDouble("last"));
                                    newObject.put("change",temp.getDouble("change"));
                                    JSONObject tickerObjectPref = gson.fromJson((String) portfolio.get(temp.getString("ticker")),JSONObject.class);
                                    Double numShares =tickerObjectPref.getDouble("shares");
                                    newObject.put("numShares",numShares);
                                    netWorth1+=(numShares*temp.getDouble("last"));
                                    portfolioArray1.put(newObject);
                                }
                                BigDecimal bd1 = new BigDecimal(netWorth1).setScale(2, RoundingMode.HALF_UP);
                                ((TextView)findViewById(R.id.netWorth)).setText(String.valueOf(bd1));
                                portfolioAdapter = new PortfolioSection(MainActivity.this, portfolioArray1);
                                portfolioRecycler.setHasFixedSize(false);
                                ItemTouchHelper.Callback callback = new ItemMoveCallbackPortfolio(portfolioAdapter);
                                ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                                touchHelper.attachToRecyclerView(portfolioRecycler);
                                portfolioRecycler.setAdapter(portfolioAdapter);
                                RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(favoritesRecycler.getContext(), R.drawable.divider));
                                portfolioRecycler.addItemDecoration(dividerItemDecoration);
                                mainProgress.setVisibility(View.GONE);
                                setProgressBarIndeterminateVisibility(false);
                                linearLayoutMain.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });

                    favoriteAdapter = new FavoritesSection(MainActivity.this, favoritesArray1);
                    favoritesRecycler.setHasFixedSize(true);
                    enableSwipeToDeleteAndUndoFavorites();
                    ItemTouchHelper.Callback callback = new ItemMoveCallbackFavorites(favoriteAdapter);
                    ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                    touchHelper.attachToRecyclerView(favoritesRecycler);
                    favoritesRecycler.setAdapter(favoriteAdapter);
                    RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(favoritesRecycler.getContext(), R.drawable.divider));
                    favoritesRecycler.addItemDecoration(dividerItemDecoration);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        LinearLayout mainProgress  = (LinearLayout) findViewById(R.id.mainProgress);
        LinearLayout linearLayoutMain = (LinearLayout)findViewById(R.id.mainLinearLayout);
        mainProgress.setVisibility(View.VISIBLE);
        linearLayoutMain.setVisibility(View.GONE);
        getFavsPortfolio();
    }
}