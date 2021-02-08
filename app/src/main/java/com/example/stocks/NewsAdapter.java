package com.example.stocks;

import android.content.Context;
import android.net.Uri;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.MyViewHolder> {
    JSONArray news;
    Context context;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView publisher, title, daysAgo;
        ImageView image;
        public MyViewHolder(View itemView) {
            super(itemView);
            publisher = (TextView) itemView.findViewById(R.id.publisher);
            title = (TextView) itemView.findViewById(R.id.headline);
            image = (ImageView) itemView.findViewById(R.id.newsImage);
            daysAgo = (TextView) itemView.findViewById(R.id.daysAgo);
        }
    }

    public NewsAdapter(Context context, JSONArray news) {
        this.context = context;
        this.news=news;
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0)
            return 0;
        return -1;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder vh;
        switch(viewType){
            case 0:
                View v1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_first_item, parent, false);
                vh = new MyViewHolder(v1);
                break;
            default:
                View v2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);
                vh = new MyViewHolder(v2);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position){
        try {
        JSONObject x = (JSONObject) news.get(position);
        holder.publisher.setText(x.getString("publisher"));
        holder.title.setText(x.getString("title"));
        holder.daysAgo.setText(x.getString("daysAgo"));
        Picasso.get().load(x.getString("image")).fit().centerCrop().into(holder.image);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    JSONObject x = (JSONObject) news.get(position);
                    Uri uri = Uri.parse(x.getString("url"));
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    context.startActivity(intent);
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        });

       holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
          @Override
          public boolean onLongClick(View view) {
              try {
                  AlertDialog.Builder builder = new AlertDialog.Builder(context);
                  final View customLayout = ((LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE )).inflate(R.layout.news_dialog, null);
                  builder.setView(customLayout);

                  //set data
                  TextView dialogHeadline = (TextView) customLayout.findViewById(R.id.dialogHeadline);
                  JSONObject x = (JSONObject) news.get(position);
                  Uri uri = Uri.parse(x.getString("url"));
                  dialogHeadline.setText(x.getString("title"));
                  ImageView image = (ImageView) customLayout.findViewById(R.id.dialogImage);
                  Picasso.get().load(x.getString("image")).fit().centerCrop().into(image);
                  ImageButton chromeButton = (ImageButton) customLayout.findViewById(R.id.chromeButton);
                  ImageButton twitterButton = (ImageButton) customLayout.findViewById(R.id.twitterButton);
                  chromeButton.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                          Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                          context.startActivity(intent);
                      }
                  });

                  twitterButton.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                          Uri twitterUri = Uri.parse("https://twitter.com/intent/tweet?text=Check out this Link:&url="+uri+"&hashtags=CSCI571StockApp");
                          context.startActivity(new Intent(Intent.ACTION_VIEW, twitterUri));

                      }
                  });

                  AlertDialog dialog = builder.create();
                  dialog.show();
              } catch (JSONException e) {
                  e.printStackTrace();
              }
              return false;
          }
      });
    }

    @Override
    public int getItemCount() {
        return news.length();
    }
}