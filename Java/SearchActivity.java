package com.example.hw93;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class SearchActivity extends AppCompatActivity implements MyAdapter.CardViewClickListener, SwipeRefreshLayout.OnRefreshListener {
    private String[] imagelink;
    private String[] title;
    private String[] section;
    private String[] time;
    private String[] id;
    private String[] bookmark;
    private RecyclerView mRecyclerView;
    private MyAdapter headlineAdapter;
    private Toolbar mToolbar;
    private String query;

    private ProgressBar spinner;
    private TextView progress;
    SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_search);

        mRecyclerView = (RecyclerView) findViewById(R.id.search_card);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        //initial spinner
        spinner = findViewById(R.id.progressBar_search);
        spinner.setVisibility(View.VISIBLE);
        progress = findViewById(R.id.progress_search);
        progress.setVisibility(View.VISIBLE);

        // Get result from mainActivity
        Intent myIntent = getIntent();
        query = myIntent.getStringExtra("query");

        // Set action bar with back button
        mToolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Search Results for " + query);
        // swipe function
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.search_swipe);
        mSwipeRefreshLayout.setOnRefreshListener(this);


        fetchData(query);
        return;
    }

    //back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                //NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void fetchData(String word) {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://theh9backend.wm.r.appspot.com/api/Search?keyword=" + word;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            manipulateData(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e("Home JSON error", error.getMessage(), error);
                    }
                });
        queue.add(jsonObjectRequest);
    }

    public void manipulateData(JSONObject info) throws JSONException {
        JSONObject response = info.getJSONObject("response");
        JSONArray obj = response.getJSONArray("results");
        imagelink = new String[obj.length()];
        title = new String[obj.length()];
        section = new String[obj.length()];
        time = new String[obj.length()];
        id = new String[obj.length()];
        bookmark = new String[obj.length()];

        for (int i = 0; i < obj.length(); i++) {
            JSONObject cur = obj.getJSONObject(i);
            //String assets = obj.getJSONObject(i).getJSONObject("blocks").getJSONObject("main").getJSONObject("elements").getJSONObject("0").getString("assets");

            JSONObject blocks = cur.getJSONObject("blocks");
            if (blocks.has("main")) {
                JSONObject main = blocks.getJSONObject("main");

                JSONArray elements = main.getJSONArray("elements");
                JSONObject zero = elements.getJSONObject(0);
                JSONArray assets = zero.getJSONArray("assets");
                if (assets.length() == 0) {
                    imagelink[i] = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";

                } else {
                    imagelink[i] = assets.getJSONObject(0).getString("file");
                }
            } else {
                imagelink[i] = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";
            }
            section[i] = cur.getString("sectionName");
            title[i] = cur.getString("webTitle");
            time[i] = cur.getString("webPublicationDate");
            id[i] = cur.getString("id");
            bookmark[i] = "false";
        }
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        headlineAdapter = new MyAdapter(imagelink, title, section, time, bookmark,id, "search", this);

        mRecyclerView.setAdapter(headlineAdapter);

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        spinner.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void cardViewClickListener(final View v, final int position, String aLong) {
        if (aLong.equals("short")) {
            String link = id[position];
            Intent myIntent = new Intent(this, DetailActivity.class);
            myIntent.putExtra("link", link);
            startActivity(myIntent);
            //getActivity().finish();
        } else if (aLong.equals("long")) {
            String img = imagelink[position];
            final String cur_title = title[position];
            String cur_section = section[position];
            String cur_time = time[position];
            final String cur_id = id[position];
            final String storage = cur_id + "###" + cur_title + "###" + img + "###" + cur_section + "###" + cur_time;
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog);
            ImageView imageView = (ImageView) dialog.findViewById(R.id.image);
            Picasso.with(this)
                    .load(img)
                    .error(R.drawable.fallback_logo)
                    .placeholder(R.drawable.fallback_logo)
                    .into(imageView);
            TextView text = (TextView) dialog.findViewById(R.id.title);
            text.setText(cur_title);
            ImageView twitter = dialog.findViewById(R.id.twitter);
            final ImageView bookmark = dialog.findViewById(R.id.bookmark);
            twitter.setImageResource(R.drawable.bluetwitter);

            // Test if this card is in bookmark
            final Context context = this;
            SharedPreferences sharedPreferences = context.getSharedPreferences("id_list", Context.MODE_PRIVATE);
            // Retrieve the values
            Set<String> pre = sharedPreferences.getStringSet("id_list", null);
            Boolean selected = false;
            if (pre != null) {
                //pre = new HashSet<String>();
                for (String s : pre) {
                    String[] id_test = s.split("###");
                    if (cur_id.equals(id_test[0])) {
                        selected = true;
                        bookmark.setImageResource(R.drawable.bookmark_selected48_foreground);
                        break;
                    }
                }
                if (!selected) {
                    bookmark.setImageResource(R.drawable.bookmark_unselected48_foreground);
                }
            } else {
                bookmark.setImageResource(R.drawable.bookmark_unselected48_foreground);
            }


            // bookmark click
            bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View dialogView) {
                    // This is delete action
                    ImageButton card_bookmark = v.findViewById(R.id.bookmark_icon);
                    //Context context = this;
                    SharedPreferences sharedPreferences = context.getSharedPreferences("id_list", Context.MODE_PRIVATE);
                    // Retrieve the values
                    Set<String> pre = sharedPreferences.getStringSet("id_list", null);
                    // pre is null only can add
                    if (pre == null) {
                        Set<String> set = new HashSet<String>();
                        pre = set;
                    } else {
                        //pre = new HashSet<String>();
                        for (String s : pre) {
                            String[] id_test = s.split("###");
                            if (cur_id.equals(id_test[0])) {
                                // Remove from preference NOTE: remove from pre, push again didn't work out
                                pre.remove(s);
                                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                myEdit.clear();
                                myEdit.putStringSet("id_list", pre);
                                myEdit.apply();

                                Toast.makeText(v.getContext(), "\"" + cur_title + "\"" + " was removed from Bookmarks",
                                        Toast.LENGTH_LONG).show();
                                // Change bookmark image
                                bookmark.setImageResource(R.drawable.bookmark_unselected48_foreground);
                                card_bookmark.setBackgroundResource(R.drawable.bookmark_unselected48_foreground);
                                return;
                            }
                        }
                    }
                    // Add new id in it
                    pre.add(storage);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                    myEdit.clear();
                    myEdit.putStringSet("id_list", pre);
                    myEdit.apply();

                    Toast.makeText(v.getContext(), "\"" + cur_title + "\"" + " was added to Bookmarks",
                            Toast.LENGTH_LONG).show();
                    // Change bookmark image
                    //bookmark.setBackgroundResource(R.drawable.bookmark_selected48_foreground);
                    bookmark.setImageResource(R.drawable.bookmark_selected48_foreground);

                    card_bookmark.setBackgroundResource(R.drawable.bookmark_selected48_foreground);
                }
            });

            twitter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View dialogView) {
                    String url = "https://twitter.com/intent/tweet?" + "text=Check out this Link:&" + "hashtags=CSCI571NewsSearch&" + "url=https://theguardian.com/" + id[position];
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });


            dialog.show();
        }
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(false);
        fetchData(query);
        return;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Keep bookmark consistent
        if (headlineAdapter != null) {
            setupRecyclerView();
        }
    }
}



