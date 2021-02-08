package com.example.stocks;

import androidx.annotation.RequiresApi;
import com.google.gson.Gson;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DetailedStockActivity extends AppCompatActivity {

    String ticker,companyName,about;
    SharedPreferences.Editor editor;
    public static final String mypreference = "mypref";
    Double lastPrice, change,low,high,mid,open,bidPrice,volume;
    Boolean expandable;
    SharedPreferences sharedpreferences;
    Menu optionsMenu;
    int menuState=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(true);
        //get ticker
        Intent intent = getIntent();
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            ticker = intent.getStringExtra(Intent.EXTRA_TEXT).trim();
            Log.d("TAG", "onCreate: " + ticker);
        }
        expandable=true;
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_stock);
        LinearLayout linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        LinearLayout linearLayoutDetailedStock = (LinearLayout)findViewById(R.id.linearLayoutDetailedStock);
        linlaHeaderProgress.setVisibility(View.VISIBLE);
        linearLayoutDetailedStock.setVisibility(View.GONE);

        //get favorites
        sharedpreferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        Gson gson = new Gson();


        Toolbar myToolbar = (Toolbar) findViewById(R.id.detailedToolBar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setHomeButtonEnabled(true);


        //display basic details
        ApiCall.makeSummary(this, ticker, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject summary = new JSONObject(response);
                    ticker = summary.getString("ticker");
                    companyName = summary.getString("companyName");
                    about = summary.getString("description");
                    lastPrice = summary.getDouble("last");
                    change = summary.getDouble("change");
                    low = summary.getDouble("low");
                    high = summary.getDouble("high");
                    mid = summary.getDouble("mid");
                    open = summary.getDouble("open");
                    bidPrice = summary.getDouble("bidPrice");
                    volume = summary.getDouble("volume");
                    ((TextView)findViewById (R.id.ticker)).setText(ticker);
                    ((TextView)findViewById (R.id.companyName)).setText(companyName);
                    TextView changeView = (TextView)(TextView)findViewById (R.id.change);
                    if(change<0){
                        changeView.setText( "-$"+ (change * -1));
                        changeView.setTextColor( getResources().getColor(R.color.red));
                    }
                    else if(change>0){
                        changeView.setText( "$"+ change);
                        changeView.setTextColor( getResources().getColor(R.color.green));
                    }
                    else{
                        changeView.setText( "$"+ change);
                        changeView.setTextColor( getResources().getColor(R.color.grey_700));
                    }
                    ((TextView)findViewById (R.id.current)).setText('$'+String.valueOf(lastPrice));

                    //display about
                    TextView aboutView = ((TextView)findViewById (R.id.about));
                    aboutView.setText(about);
                    Button btnShowMore=(Button)findViewById(R.id.btnShowMore);
                    btnShowMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (btnShowMore.getText().toString().equalsIgnoreCase("Show more...."))
                            {
                                aboutView.setMaxLines(Integer.MAX_VALUE);
                                btnShowMore.setText("Show less");
                            }
                            else
                            {
                                aboutView.setMaxLines(2);
                                btnShowMore.setText("Show more....");
                            }
                        }
                    });

                    //display stats
                    ((TextView)findViewById (R.id.statsCurrent)).setText("Current Price:"+(new BigDecimal(lastPrice).setScale(2, RoundingMode.HALF_UP)).toPlainString());
                    ((TextView)findViewById (R.id.statsLow)).setText("Low:"+(new BigDecimal(low).setScale(2, RoundingMode.HALF_UP)).toPlainString());
                    ((TextView)findViewById (R.id.statsBid)).setText("Bid Price:"+(new BigDecimal(bidPrice).setScale(2, RoundingMode.HALF_UP)).toPlainString());
                    ((TextView)findViewById (R.id.statsOpen)).setText("Open Price:"+(new BigDecimal(open).setScale(2, RoundingMode.HALF_UP)).toPlainString());
                    ((TextView)findViewById (R.id.statsMid)).setText("Mid:"+(new BigDecimal(mid).setScale(2, RoundingMode.HALF_UP)).toPlainString());
                    ((TextView)findViewById (R.id.statsHigh)).setText("High:"+(new BigDecimal(high).setScale(2, RoundingMode.HALF_UP)).toPlainString());
                    ((TextView)findViewById (R.id.statsVolume)).setText("Volume:"+new DecimalFormat("#,###.00").format(volume));

                    TextView portfolioText = (TextView)findViewById(R.id.portfolioText);
                    String jsonPortfolioTickers = sharedpreferences.getString("portfolioTickers", "");
                    String[] portfolioTickers = jsonPortfolioTickers.split(",");
                    List<String> portfolioTickersList = Arrays.asList(portfolioTickers);

                    String jsonPortfolio = sharedpreferences.getString("portfolio", "");
                    JSONObject portfolio = gson.fromJson(jsonPortfolio, JSONObject.class);


                    if(portfolioTickersList.contains(ticker.toUpperCase())){
                        try {
                            JSONObject newPref = (JSONObject) gson.fromJson((String)portfolio.get(ticker.toUpperCase()),JSONObject.class);
                            double numShares =newPref.getDouble("shares");
                            BigDecimal numBd = new BigDecimal(numShares).setScale(4, RoundingMode.HALF_UP);
                            BigDecimal bd = new BigDecimal(numShares*lastPrice).setScale(2, RoundingMode.HALF_UP);
                            portfolioText.setText("Shares owned:"+(numBd.toPlainString())+"\nMarket Value:$"+(bd.toPlainString()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        portfolioText.setText("You have 0 shares of "+ticker.toUpperCase()+"\nStart trading!");
                    }
                    Button portfolioTradeButton = (Button)findViewById(R.id.portfolioTradeButton);
                    portfolioTradeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(DetailedStockActivity.this);
                            final View customLayout = ((LayoutInflater) DetailedStockActivity.this.getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.trade_dialog, null);
                            builder.setView(customLayout);
                            Float amountAvailable = sharedpreferences.getFloat("startAmount",0);
                            TextView tradeHeadline = (TextView)customLayout.findViewById(R.id.tradeDialogHeadline);
                            tradeHeadline.setText("Trade "+companyName+" shares");
                            TextView remainingMoney = (TextView)customLayout.findViewById(R.id.remainingMoney);
                            remainingMoney.setText("$"+amountAvailable+" available to buy "+ticker);
                            TextView tradeShareCost = (TextView)customLayout.findViewById(R.id.tradeDialogShareCost);
                            tradeShareCost.setText("0 x $"+lastPrice+"/share=$0.0");
                            EditText inputNumShares = (EditText)customLayout.findViewById(R.id.editNumShares);
                            inputNumShares.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    int num;
                                    if((inputNumShares.getText().toString()).length()==0)
                                        num=0;
                                    else
                                        num = Integer.parseInt(inputNumShares.getText().toString());
                                    tradeShareCost.setText(num+" x $"+lastPrice+"/share=$"+(num*lastPrice));
                                }

                                @Override
                                public void afterTextChanged(Editable s) { }
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();

                            //add click listener for buy button
                            Button portfolioBuyButton = (Button)customLayout.findViewById(R.id.tradeDialogBuy);
                            portfolioBuyButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(!TextUtils.isDigitsOnly(inputNumShares.getText().toString()))
                                        Toast.makeText(DetailedStockActivity.this, "Please enter valid amount",Toast.LENGTH_SHORT).show();
                                    else{
                                        int num = Integer.parseInt(inputNumShares.getText().toString());
                                        if(num<=0)
                                            Toast.makeText(DetailedStockActivity.this, "Cannot buy less than 0 shares",Toast.LENGTH_SHORT).show();
                                        else if(num*lastPrice>amountAvailable)
                                            Toast.makeText(DetailedStockActivity.this, "Not enough money to buy",Toast.LENGTH_SHORT).show();
                                        else{
                                            //change in shared preferences
                                            Float amount = sharedpreferences.getFloat("startAmount",20000);
                                            String jsonPortfolioTickers = sharedpreferences.getString("portfolioTickers", "");
                                            String[] portfolioTickers = jsonPortfolioTickers.split(",");
                                            List<String> portfolioTickersList = Arrays.asList(portfolioTickers);

                                            String jsonPortfolio = sharedpreferences.getString("portfolio", "");
                                            JSONObject portfolio = gson.fromJson(jsonPortfolio, JSONObject.class);

                                            if(portfolioTickersList.contains(ticker.toUpperCase())){
                                                try {
                                                    JSONObject tickerObjectPref = gson.fromJson((String) portfolio.get(ticker.toUpperCase()),JSONObject.class);
                                                    double numShares =tickerObjectPref.getDouble("shares");
                                                    numShares+=num;
                                                    //set this
                                                    tickerObjectPref.put("shares",numShares);
                                                    portfolio.put(ticker.toUpperCase(),gson.toJson(tickerObjectPref));
                                                    editor.putString("portfolio",  gson.toJson(portfolio));
                                                    editor.commit();

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else{
                                                try {
                                                    //add to portfolio tickers list
                                                    if(jsonPortfolioTickers=="")
                                                        editor.putString("portfolioTickers", ticker.toUpperCase());
                                                    else
                                                        editor.putString("portfolioTickers", jsonPortfolioTickers+","+ticker.toUpperCase());
                                                    JSONObject tickerObjectPref = new JSONObject();
                                                    tickerObjectPref.put("shares",num);
                                                    portfolio.put(ticker.toUpperCase(),gson.toJson(tickerObjectPref));
                                                    editor.putString("portfolio",  gson.toJson(portfolio));
                                                    editor.commit();

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            editor.putFloat("startAmount", (float) (amount-(num*lastPrice)));
                                            editor.commit();
                                            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                @Override
                                                public void onDismiss(DialogInterface dialog) {
                                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(DetailedStockActivity.this);
                                                    final View customLayout1 = ((LayoutInflater) DetailedStockActivity.this.getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.success_dialog, null);
                                                    builder1.setView(customLayout1);
                                                    TextView dialogSuccessMsg = (TextView) customLayout1.findViewById(R.id.dialogSuccessMessage);
                                                    dialogSuccessMsg.setText("You have successfully bought "+num+" shares of "+ticker.toUpperCase());
                                                    AlertDialog dialog1 = builder1.create();
                                                    Button doneButton = (Button)customLayout1.findViewById(R.id.dialogDone);
                                                    doneButton.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            dialog1.dismiss();
                                                        }
                                                    });
                                                    dialog1.show();
                                                }
                                            });
                                            TextView portfolioText1 = (TextView)findViewById(R.id.portfolioText);
                                            String jsonPortfolioTickers1 = sharedpreferences.getString("portfolioTickers", "");
                                            List<String> portfolioTickersList1 = Arrays.asList(jsonPortfolioTickers1.split(","));
                                            JSONObject portfolio1 = gson.fromJson(sharedpreferences.getString("portfolio", ""), JSONObject.class);


                                            if(portfolioTickersList1.contains(ticker.toUpperCase())){
                                                try {
                                                    JSONObject newPref1 = (JSONObject) gson.fromJson((String)portfolio1.get(ticker.toUpperCase()),JSONObject.class);
                                                    double numShares1 =newPref1.getDouble("shares");
                                                    BigDecimal numBd1 = new BigDecimal(numShares1).setScale(4, RoundingMode.HALF_UP);
                                                    BigDecimal bd1 = new BigDecimal(numShares1*lastPrice).setScale(2, RoundingMode.HALF_UP);
                                                    portfolioText1.setText("Shares owned:"+numBd1.toPlainString()+"\nMarket Value:$"+(bd1.toPlainString()));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            else{
                                                portfolioText1.setText("You have 0 shares of "+ticker.toUpperCase()+"\nStart trading!");
                                            }
                                            dialog.dismiss();
                                        }
                                    }
                                }
                            });

                            //add click listener for sell
                            Button portfolioSellButton = (Button)customLayout.findViewById(R.id.tradeDialogSell);
                            portfolioSellButton.setOnClickListener(new View.OnClickListener() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public void onClick(View v) {
                                    if(!TextUtils.isDigitsOnly(inputNumShares.getText().toString()))
                                        Toast.makeText(DetailedStockActivity.this, "Please enter valid amount",Toast.LENGTH_SHORT).show();
                                    else{
                                        try {
                                            Float amount = sharedpreferences.getFloat("startAmount",20000);
                                            String jsonPortfolioTickers = sharedpreferences.getString("portfolioTickers", "");
                                            String[] portfolioTickers = jsonPortfolioTickers.split(",");
                                            List<String> portfolioTickersList = new ArrayList<>(Arrays.asList(portfolioTickers));
                                            String jsonPortfolio = sharedpreferences.getString("portfolio", "");
                                            JSONObject portfolio = gson.fromJson(jsonPortfolio, JSONObject.class);
                                            int num = Integer.parseInt(inputNumShares.getText().toString());

                                            if (num <= 0)
                                                Toast.makeText(DetailedStockActivity.this, "Cannot sell less than 0 shares", Toast.LENGTH_SHORT).show();
                                            else if(!portfolioTickersList.contains(ticker.toUpperCase()))
                                                Toast.makeText(DetailedStockActivity.this, "Not enough shares to sell",Toast.LENGTH_SHORT).show();
                                            else {
                                                JSONObject tickerObjectPref = gson.fromJson((String) portfolio.get(ticker.toUpperCase()), JSONObject.class);
                                                double numShares = tickerObjectPref.getDouble("shares");
                                                if(num>numShares)
                                                    Toast.makeText(DetailedStockActivity.this, "Not enough shares to sell",Toast.LENGTH_SHORT).show();
                                                else {
                                                    numShares -= num;
                                                    if (numShares == 0) {
                                                        for (int i = 0; i < portfolioTickersList.size(); i++) {
                                                            if (portfolioTickersList.get(i).equals(ticker.toUpperCase()))
                                                                portfolioTickersList.remove(i);
                                                        }
                                                        String str = String.join(",", portfolioTickersList);
                                                        editor.putString("portfolioTickers", str);
                                                        portfolio.remove(ticker.toUpperCase());
                                                        editor.putString("portfolio", gson.toJson(portfolio));
                                                        editor.commit();
                                                    } else {
                                                        JSONObject obj = new JSONObject();
                                                        obj.put("shares", numShares);
                                                        portfolio.put(ticker.toUpperCase(), gson.toJson(obj));
                                                        editor.putString("portfolio", gson.toJson(portfolio));
                                                        editor.commit();
                                                    }
                                                    editor.putFloat("startAmount", (float) (amount+(num*lastPrice)));
                                                    editor.commit();
                                                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                        @Override
                                                        public void onDismiss(DialogInterface dialog) {
                                                            AlertDialog.Builder builder1 = new AlertDialog.Builder(DetailedStockActivity.this);
                                                            final View customLayout1 = ((LayoutInflater) DetailedStockActivity.this.getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.success_dialog, null);
                                                            builder1.setView(customLayout1);
                                                            TextView dialogSuccessMsg = (TextView) customLayout1.findViewById(R.id.dialogSuccessMessage);
                                                            dialogSuccessMsg.setText("You have successfully sold "+num+" shares of "+ticker.toUpperCase());
                                                            AlertDialog dialog1 = builder1.create();
                                                            Button doneButton = (Button)customLayout1.findViewById(R.id.dialogDone);
                                                            doneButton.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    dialog1.dismiss();
                                                                }
                                                            });
                                                            dialog1.show();
                                                        }
                                                    });
                                                    TextView portfolioText1 = (TextView)findViewById(R.id.portfolioText);
                                                    String jsonPortfolioTickers1 = sharedpreferences.getString("portfolioTickers", "");
                                                    List<String> portfolioTickersList1 = Arrays.asList(jsonPortfolioTickers1.split(","));
                                                    JSONObject portfolio1 = gson.fromJson(sharedpreferences.getString("portfolio", ""), JSONObject.class);

                                                    if(portfolioTickersList1.contains(ticker.toUpperCase())){
                                                        try {
                                                            JSONObject newPref1 = (JSONObject) gson.fromJson((String)portfolio1.get(ticker.toUpperCase()),JSONObject.class);
                                                            double numShares1 =newPref1.getDouble("shares");
                                                            BigDecimal numBd1 = new BigDecimal(numShares1).setScale(4, RoundingMode.HALF_UP);
                                                            BigDecimal bd1 = new BigDecimal(numShares1*lastPrice).setScale(2, RoundingMode.HALF_UP);
                                                            portfolioText1.setText("Shares owned:"+numBd1.toPlainString()+"\nMarket Value:$"+(bd1.toPlainString()));
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                    else
                                                        portfolioText1.setText("You have 0 shares of "+ticker.toUpperCase()+"\nStart trading!");
                                                    dialog.dismiss();
                                                }



                                            }
                                        }
                                        catch(Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });

                        }
                    });
                    //display news
                    ApiCall.makeNews(DetailedStockActivity.this, ticker, new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            try {

                                JSONObject responseObject = new JSONObject(response);
                                JSONArray news = responseObject.getJSONArray("news");
                                RecyclerView recyclerView = (RecyclerView)findViewById(R.id.newsRecyclerView);
                                GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),1);
                                recyclerView.setLayoutManager(gridLayoutManager); // set LayoutManager to RecyclerView
                                NewsAdapter newsAdapter = new NewsAdapter(DetailedStockActivity.this, news);
                                recyclerView.setHasFixedSize(true);
                                recyclerView.setAdapter(newsAdapter); // set the Adapter to RecyclerView
                                linlaHeaderProgress.setVisibility(View.GONE);
                                setProgressBarIndeterminateVisibility(false);
                                menuState =1;
                                DetailedStockActivity.this.invalidateOptionsMenu();
                                linearLayoutDetailedStock.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }

                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }

        });

        //display chart
        WebView webview = (WebView)findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.clearCache(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.setWebViewClient(new WebViewClient());
        webview.loadUrl("file:///android_asset/highcharts.html?ticker="+ticker);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String jsonFavorites = sharedpreferences.getString("favorites", "");
        String[] favoriteTickers = jsonFavorites.split(",");
        List<String> favoriteTickersList = Arrays.asList(favoriteTickers);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detailedmenu, menu);

        optionsMenu = menu;
        MenuItem favoriteIcon = menu.findItem(R.id.action_favorite);
        if(menuState==1)
            favoriteIcon.setVisible(true);
        else
            favoriteIcon.setVisible(false);

        if(favoriteTickersList.contains(ticker.toUpperCase())){
            favoriteIcon.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_star_24));
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
            {
                String jsonFavorites = sharedpreferences.getString("favorites", "");
                String[] favoriteTickers = jsonFavorites.split(",");
                List<String> favoriteTickersList = new ArrayList<>(Arrays.asList(favoriteTickers));

                if(favoriteTickersList.contains(ticker.toUpperCase())){
                    Toast.makeText(this,ticker+" was removed from favorites",Toast.LENGTH_SHORT).show();
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_star_border_24));
                    for (int i = 0; i < favoriteTickersList.size(); i++)
                    {
                        if (favoriteTickersList.get(i).equals(ticker.toUpperCase()))
                           favoriteTickersList.remove(i);
                    }
                    String str = String.join(",",favoriteTickersList);
                    editor.putString("favorites", str);
                }
                else{
                    Toast.makeText(this,ticker+" was added to favorites",Toast.LENGTH_SHORT).show();
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_star_24));

                    //set in shared preferences
                    if(jsonFavorites=="")
                        editor.putString("favorites", ticker.toUpperCase());
                    else
                        editor.putString("favorites", jsonFavorites+","+ticker.toUpperCase());
                }
                editor.commit();
                return true;
            }
            case android.R.id.home:
                this.finish();
                return super.onOptionsItemSelected(item);

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(DetailedStockActivity.this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }
}