package com.example.hw93.ui.headlines;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
import com.example.hw93.DetailActivity;
import com.example.hw93.MyAdapter;
import com.example.hw93.R;
import com.example.hw93.Tab_adapter;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;


public class HeadlinesFragment extends Fragment implements Tab_adapter.RecyclerViewClickListener, MyAdapter.CardViewClickListener {

    private String[] imagelink;
    private String[] title;
    private String[] section;
    private String[] time;
    private String[] id;
    private String[] bookmark;
    private String cur_tab;
    private RecyclerView mRecyclerView;
    private RecyclerView headline_tab_view;
    private MyAdapter headlineAdapter;
    LinearLayoutManager HorizontalLayout;
    private ProgressBar spinner;
    private TextView progress;
    SwipeRefreshLayout mSwipeRefreshLayout;
    View preview;

    public String[] tabs = new String[]{"WORLD", "BUSINESS", "POLITICS", "SPORTS", "TECHNOLOGY", "SCIENCE"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_headlines, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.text_headlines);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        spinner = view.findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);
        progress = view.findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);

        cur_tab = "world";
        headline_tab_view = (RecyclerView) view.findViewById(R.id.headline_nav);

        // initial tab first, then create each news card
        setupTabRecyclerView(headline_tab_view);
        fetchData(view, cur_tab);

        // swipe function
        mSwipeRefreshLayout = view.findViewById(R.id.headline_swipe_fresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String[] inputs = new String[]{"latest"};
                mSwipeRefreshLayout.setRefreshing(false);
                fetchData(view, cur_tab);
            }
        });
        return view;
    }

    public void fetchData(View view, String tab) {
        tab = tab.toLowerCase();
        if (tab.equals("sports")) {
            tab = "sport";
        }
        RequestQueue queue = Volley.newRequestQueue(view.getContext());
        String url = "https://theh9backend.wm.r.appspot.com/api/Gurdian?cate=" + tab;

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

    @Override
    public void onResume() {
        super.onResume();
        // Keep bookmark consistent
        if (headlineAdapter != null) {
            setupRecyclerView();
        }
    }

    // call adapter fill in data to cards
    private void setupRecyclerView() {
        headlineAdapter = new MyAdapter(imagelink, title, section, time, bookmark, id, "headline", this);

        mRecyclerView.setAdapter(headlineAdapter);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        spinner.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void setupTabRecyclerView(@NonNull RecyclerView recyclerView) {
        Tab_adapter tabAdapter = new Tab_adapter(tabs, this);
        recyclerView.setHasFixedSize(true);
        HorizontalLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(HorizontalLayout);
        recyclerView.setAdapter(tabAdapter);
    }

    // Tab click
    @Override
    public void recyclerViewListClicked(View tab_view, String tab_name) {

        spinner.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        //only run at first time, bind preview with first tab viewHolder
        if (preview == null) {
            preview = tab_view;
            return;
        }

        preview.setBackgroundResource(R.color.white);
        preview = tab_view;
        cur_tab = tab_name;
        tab_view.setBackgroundResource(R.drawable.background_headline_tab);
        fetchData(tab_view, cur_tab);
        return;
    }

    // Card click
    @Override
    public void cardViewClickListener(final View v, final int position, String aLong) {
        // Short click || Long click
        if (aLong.equals("short")) {
            String link = id[position];
            Intent myIntent = new Intent(getActivity(), DetailActivity.class);
            myIntent.putExtra("link", link);
            startActivity(myIntent);

        } else if (aLong.equals("long")) {
            String img = imagelink[position];
            final String cur_title = title[position];
            String cur_section = section[position];
            String cur_time = time[position];
            final String cur_id = id[position];
            final String storage = cur_id + "###" + cur_title + "###" + img + "###" + cur_section + "###" + cur_time;
            final Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.dialog);
            ImageView imageView = (ImageView) dialog.findViewById(R.id.image);
            Picasso.with(getContext())
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
            Context context = getContext();
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
                    Context context = getContext();
                    SharedPreferences sharedPreferences = context.getSharedPreferences("id_list", Context.MODE_PRIVATE);
                    // Retrieve the values
                    Set<String> pre = sharedPreferences.getStringSet("id_list", null);
                    // pre is null only can add
                    if (pre == null) {
                        Set<String> set = new HashSet<String>();
                        pre = set;
                    } else {
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
}


