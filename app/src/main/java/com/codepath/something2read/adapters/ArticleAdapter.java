package com.codepath.something2read.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.something2read.R;
import com.codepath.something2read.models.Article;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import java.util.ArrayList;
import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private static String URL = "https://alwaysevie.files.wordpress.com/2012/07/teacup-kittens2.jpg";
    // Store a member variable for the contacts
    private List<Article> mArticles = new ArrayList<>();
    // Store the context for easy access
    private Context mContext;

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public ImageView ivImage;
        public TextView tvTitle;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
        }
    }


    // Pass in the contact array into the constructor
    public ArticleAdapter(Context context, List<Article> articles) {
        mArticles = articles;
        mContext = context;
//        super(context, android.R.layout.simple_list_item_1, articles);
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ArticleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View articleView = inflater.inflate(R.layout.item_article_result, parent, false);

        // Return a new holder instance
        return new ViewHolder(articleView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ArticleAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Article article = mArticles.get(position);

        // Set item views based on your views and data model
        ImageView imageView = viewHolder.ivImage;
        Log.d("@@@", "Loading image " + article.getThumbNail());
        if (article.getThumbNail() != null) {
            Picasso.with(mContext).load(article.getThumbNail()).into(imageView);
        } else {
            Picasso.with(mContext)
                .load(URL)
                .into(imageView);
        }
        TextView textView = viewHolder.tvTitle;
        textView.setText(article.getHeadline());
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mArticles.size();
    }

//    @NonNull
//    @Override
//    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        Article article = this.getItem(position);
//
//        if (convertView == null) {
//            LayoutInflater inflater = LayoutInflater.from(getContext());
//            convertView = inflater.inflate(R.layout.item_article_result, parent, false);
//        }
//
//        ImageView imageView = (ImageView) convertView.findViewById(R.id.ivImage);
//        imageView.setImageResource(0);
//
//        TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
//        tvTitle.setText(article.getHeadline());
//
//        String thumbnail = article.getThumbNail();
//
//        if (!TextUtils.isEmpty(thumbnail)) {
//            Picasso.with(getContext()).load(thumbnail)
//                .placeholder(R.drawable.placeholder)
//                .error(R.drawable.error)
//                .transform(new RoundedCornersTransformation(10, 10))
//                .into(imageView);
//        }
//
//        return convertView;
//    }
}
