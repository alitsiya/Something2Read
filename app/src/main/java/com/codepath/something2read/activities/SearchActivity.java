package com.codepath.something2read.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.codepath.something2read.R;
import com.codepath.something2read.adapters.ArticleArrayAdapter;
import com.codepath.something2read.models.Article;
import com.codepath.something2read.utils.EndlessScrollListener;
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
import static com.codepath.something2read.activities.SettingsActivity.SORT_ORDER;
import static com.codepath.something2read.activities.SettingsActivity.PREFS_NAME;
import static com.codepath.something2read.activities.SettingsActivity.STARTING_DATE;

public class SearchActivity extends AppCompatActivity {
    private final Logger mLogger = LoggerFactory.getLogger(SearchActivity.class);

    @BindView(R.id.etQuery) EditText etQuery;
    @BindView(R.id.btnSearch) Button btnSearch;
    @BindView(R.id.gvResults) GridView gvResults;

    ArrayList<Article> mArticles = new ArrayList<>();
    ArticleArrayAdapter mAdapter;
    private SharedPreferences mSharedPreferences;

    private static String SEARCH_API_ENDPOINT = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
    private final String API_KEY = "4bd426acd50844b1a81d7d5ffc5a4768";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ButterKnife.bind(this);
        mAdapter = new ArticleArrayAdapter(this, mArticles);
        gvResults.setAdapter(mAdapter);
        mSharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), ArticleActivity.class);
                Article article = mArticles.get(position);
                i.putExtra("article", article);
                startActivity(i);
            }
        });
        gvResults.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list
                loadNextDataFromApi(page);
                // or loadNextDataFromApi(totalItemsCount);
                return true;
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapter.clear();
                etQuery.onEditorAction(EditorInfo.IME_ACTION_DONE);
                if (isNetworkAvailable() || isOnline()) {
                    onArticleSearch(0);
                } else {
                    Toast.makeText(getApplicationContext(), "Network is not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    private void loadNextDataFromApi(int offset) {
        // Send an API request to retrieve appropriate paginated data
        onArticleSearch(offset);
    }

    public void onArticleSearch(int page) {
        String query = etQuery.getText().toString();
        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        params.put("api-key", API_KEY);
        params.put("page", page);
        params.put("q", query);
        params = getAdditionalRequestParametersFromPrefs(params);
        client.get(SEARCH_API_ENDPOINT, params, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                JSONArray articleJsonResults;
                try {
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    mArticles.addAll(Article.fromJSONArray(articleJsonResults));
                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
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
        String startingDate = mSharedPreferences.getString(STARTING_DATE, null);
        if (startingDate != null) {
            params.put("begin_date", startingDate); //Format: YYYYMMDD
        }
        String order = mSharedPreferences.getString(SORT_ORDER, "newest");
        params.put("sort", order);
        String newsDeskQuery = mSharedPreferences.getString(NEWS_DESK, null);
        if (newsDeskQuery != null) {
            params.put("fq", newsDeskQuery);
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
    public boolean isOnline() {
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

}