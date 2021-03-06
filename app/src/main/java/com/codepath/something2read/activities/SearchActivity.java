package com.codepath.something2read.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.codepath.something2read.R;
import com.codepath.something2read.adapters.ArticleAdapter;
import com.codepath.something2read.utils.EndlessRecyclerViewScrollListener;
import com.codepath.something2read.models.Article;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

import java.io.IOException;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codepath.something2read.activities.SettingsActivity.NEWS_DESK;
import static com.codepath.something2read.activities.SettingsActivity.PREFS_NAME;
import static com.codepath.something2read.activities.SettingsActivity.SORT_ORDER;
import static com.codepath.something2read.activities.SettingsActivity.STARTING_DATE;

public class SearchActivity extends AppCompatActivity implements ArticleAdapter.ItemClickListener {
    private final Logger mLogger = LoggerFactory.getLogger(SearchActivity.class);

    @BindView(R.id.rvResults) RecyclerView rvResults;

    ArrayList<Article> mArticles;
    ArticleAdapter mAdapter;
    private String mQuery;
    private SharedPreferences mSharedPreferences;

    private EndlessRecyclerViewScrollListener mScrollListener;

    private static String SEARCH_API_ENDPOINT = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
    private final String API_KEY = "4bd426acd50844b1a81d7d5ffc5a4768";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ButterKnife.bind(this);
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        mArticles = new ArrayList<>();
        // Create adapter passing in the sample user data
        mAdapter = new ArticleAdapter(this, mArticles, this);

        // Attach the adapter to the recyclerview to populate items
        rvResults.setAdapter(mAdapter);
        // Set layout manager to position the items. 0 - HORIZONTAL, 1 - VERTICAL
        StaggeredGridLayoutManager layoutManager =
            new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        rvResults.setLayoutManager(layoutManager);

        mScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(page);
            }
        };
        rvResults.addOnScrollListener(mScrollListener);
        mSharedPreferences = getSharedPreferences(PREFS_NAME, 0);
    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    private void loadNextDataFromApi(int offset) {
        // Send an API request to retrieve appropriate paginated data
        onArticleSearch(offset);
    }

    public void onArticleSearch(int page) {
        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        params.put("api-key", API_KEY);
        params.put("page", page);
        params.put("q", mQuery);
        params = getAdditionalRequestParametersFromPrefs(params);
        client.get(SEARCH_API_ENDPOINT, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                JSONArray articleJsonResults;
                try {
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    // update the existing list
                    mArticles.addAll(Article.fromJSONArray(articleJsonResults));
                    // curSize should represent the first element that got added
                    // newItems.size() represents the itemCount
                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                mQuery = query;
//                mAdapter.clear();
                if (isNetworkAvailable() || isOnline()) {
                    onArticleSearch(0);
                } else {
                    Toast.makeText(getApplicationContext(), "Network is not available", Toast.LENGTH_SHORT).show();
                }
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // Clear view when search query is cleared
                    mArticles.clear();
                    mAdapter.notifyDataSetChanged();
                    mScrollListener.resetState();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private RequestParams getAdditionalRequestParametersFromPrefs(RequestParams params) {
        try {
            String startingDate = mSharedPreferences.getString(STARTING_DATE, null);
            if (startingDate != null) {
                params.put("begin_date", startingDate); //Format: YYYYMMDD
            }
            String order = mSharedPreferences.getString(SORT_ORDER, "newest");
            params.put("sort", order);
            String newsDeskQuery = mSharedPreferences.getString(NEWS_DESK, null);
            if (!newsDeskQuery.isEmpty()) {
                params.put("fq", "news_desk:(" + newsDeskQuery + ")");
            }
        } catch (NullPointerException e) {
            //
        }
        return params;
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
            = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    //using this method as a helper method since it returns false of Pixel
    private boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            mLogger.debug("IsOnline returns IOException " + e);
        } catch (InterruptedException e) {
            mLogger.debug("IsOnline returns InterruptedException " + e);
        }
        return false;
    }

    @Override
    public void onItemClicked(View v, Article article) {
        Intent i = new Intent(this, ArticleActivity.class);
        i.putExtra("article", article);
        startActivity(i);
    }
}
