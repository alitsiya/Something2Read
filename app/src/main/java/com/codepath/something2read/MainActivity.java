package com.codepath.something2read;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.codepath.something2read.adapters.ArticleArrayAdapter;
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

import java.util.ArrayList;

public class MainActivity extends Activity {

    @BindView(R.id.etQuery) EditText etQuery;
    @BindView(R.id.btnSearch) Button btnSearch;
    @BindView(R.id.gvResults) GridView gvResults;

    ArrayList<Article> mArticles = new ArrayList<>();
    ArticleArrayAdapter mAdapter;

    private static String SEARCH_API_ENDPOINT = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
    private final String API_KEY = "4bd426acd50844b1a81d7d5ffc5a4768";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mAdapter = new ArticleArrayAdapter(this, mArticles);
        gvResults.setAdapter(mAdapter);
        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getApplicationContext(), ArticleActivity.class);
                Article article = mArticles.get(position);
                i.putExtra("article", article);
                startActivity(i);
            }
        });
    }
    public void onArticleSearch(View view) {
        String query = etQuery.getText().toString();
        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();
        params.put("api-key", API_KEY);
        params.put("page", 0);
        params.put("q", query);
        client.get(SEARCH_API_ENDPOINT, params, new JsonHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("@@@", "" + errorResponse);
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("@@@", response.toString());

                JSONArray articleJsonResults;
                try {
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    Log.d("@@@", "" + articleJsonResults);
                    mArticles.addAll(Article.fromJSONArray(articleJsonResults));
                    mAdapter.notifyDataSetChanged();
                    Log.d("@@@", "" + mArticles);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }
}
