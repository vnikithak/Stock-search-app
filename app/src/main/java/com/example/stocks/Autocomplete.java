package com.example.stocks;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;

import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;


public class Autocomplete {

    private static final int SEARCH_QUERY_THRESHOLD = 3;

    private static final String[] autocompleteResults = new String[] {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1
    };

    public static void setAutocompleteListeners(Context ctx, SearchView searchView) {
        searchView.setSuggestionsAdapter(new SimpleCursorAdapter(
                ctx, android.R.layout.simple_list_item_1, null,
                new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1 },
                new int[] { android.R.id.text1 }));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                if (query.length() >= SEARCH_QUERY_THRESHOLD) {
                    Autocomplete.fetchAutocompleteResults(ctx, query, searchView);
                }
                 else
                    searchView.getSuggestionsAdapter().changeCursor(null);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {

                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                String term = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
                cursor.close();
                Intent intent = new Intent(ctx, DetailedStockActivity.class);
                intent.setAction(Intent.ACTION_SEARCH);
                intent.putExtra(SearchManager.QUERY, term);
                intent.putExtra(Intent.EXTRA_TEXT, term.split("-")[0]);
                ctx.startActivity(intent);
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                return onSuggestionSelect(position);
            }
        });
    }

    public static void fetchAutocompleteResults(Context ctx, String query, SearchView searchView) {
        MatrixCursor cursor = new MatrixCursor(autocompleteResults);
        ApiCall.makeAutoComplete(ctx, query, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            JSONArray autoCompleteArray = responseObject.getJSONArray("autocomplete");
                            for (int index = 0; index < autoCompleteArray.length(); index++) {
                                JSONObject obj = (JSONObject) autoCompleteArray.get(index);
                                String ticker = obj.getString("ticker");
                                String companyName = obj.getString("name");
                                Object[] result = new Object[]{index, ticker + " - " + companyName};
                                cursor.addRow(result);
                            }
                            searchView.getSuggestionsAdapter().changeCursor(cursor);
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
}
