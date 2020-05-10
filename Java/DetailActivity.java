package com.example.hw93;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
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
import androidx.cardview.widget.CardView;
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

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DetailActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    boolean clicked = false;
    private ProgressBar spinner;
    private TextView progress;
    private ImageButton bookmarkBTN;
    private ImageButton twitterBTN;
    private CardView detailCard;

    private String title;
    private String section;
    private String time;
    private String id;
    private String imglink;
    private String link;
    private String[] monthShortNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun","Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_detail);
        Intent myIntent = getIntent();
        link = myIntent.getStringExtra("link");

        //initial spinner
        detailCard = findViewById(R.id.detail_card);
        detailCard.setVisibility(View.GONE);
        spinner = findViewById(R.id.progressBar_detail);
        spinner.setVisibility(View.VISIBLE);
        progress = findViewById(R.id.progress_detail);
        progress.setVisibility(View.VISIBLE);

        //initial toolbar and active it
        mToolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        bookmarkBTN = findViewById(R.id.bookmark_btn);
        twitterBTN = findViewById(R.id.twitter_btn);

        bookmarkBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                SharedPreferences sharedPreferences = context.getSharedPreferences("id_list", Context.MODE_PRIVATE);
                // Retrieve the values
                Set<String> pre = sharedPreferences.getStringSet("id_list", null);
                // pre is null only can add
                final String storage = id + "###" + title + "###" + imglink + "###" + section + "###" + time;
                if (pre == null) {
                    Set<String> set = new HashSet<String>();
                    pre = set;
                } else {
                    for (String s : pre) {
                        String[] id_test = s.split("###");
                        if (id.equals(id_test[0])) {
                            // Remove from preference NOTE: remove from pre, push again didn't work out
                            pre.remove(s);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();
                            myEdit.clear();
                            myEdit.putStringSet("id_list", pre);
                            myEdit.apply();

                            Toast.makeText(v.getContext(), "\"" + title + "\"" + " was removed from Bookmarks",
                                    Toast.LENGTH_LONG).show();
                            // Change bookmark image
                            bookmarkBTN.setBackgroundResource(R.drawable.bookmark_unselected48_foreground);
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
                Toast.makeText(v.getContext(), "\"" + title + "\"" + " was added to Bookmarks",
                        Toast.LENGTH_LONG).show();
                bookmarkBTN.setBackgroundResource(R.drawable.bookmark_selected48_foreground);
            }
        });
        twitterBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://twitter.com/intent/tweet?" + "text=Check out this Link:&" + "hashtags=CSCI571NewsSearch&" + "url=https://theguardian.com/" + id;
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        fetchData(link);
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

    public void fetchData(String link) {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://theh9backend.wm.r.appspot.com/api/Detail?link=" + link;

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
        JSONObject obj = response.getJSONObject("content");
        title = obj.getString("webTitle");
        section = obj.getString("sectionName");
        time = obj.getString("webPublicationDate");
        id = obj.getString("id");

        //check bookmark with sharedpreference
        Context context = this;
        SharedPreferences sharedPreferences = context.getSharedPreferences("id_list", Context.MODE_PRIVATE);
        Set<String> pre = sharedPreferences.getStringSet("id_list", null);
        Boolean selected = false;

        // make bookmark consistent with storage data
        if (pre != null) {
            for (String s : pre) {
                String[] id_test = s.split("###");
                if (id.equals(id_test[0])) {
                    selected = true;
                    bookmarkBTN.setBackgroundResource(R.drawable.bookmark_selected48_foreground);
                    break;
                }
            }
            if (!selected) {
                bookmarkBTN.setBackgroundResource(R.drawable.bookmark_unselected48_foreground);
            }
        } else {
            bookmarkBTN.setBackgroundResource(R.drawable.bookmark_unselected48_foreground);
        }

        final String url = obj.getString("webUrl");

        JSONObject blocks = obj.getJSONObject("blocks");
        imglink = "";
        if (!blocks.has("main")) {
            imglink = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";
        } else {
            JSONObject main = blocks.getJSONObject("main");
            JSONArray elements = main.getJSONArray("elements");
            JSONObject zero = elements.getJSONObject(0);
            JSONArray assets = zero.getJSONArray("assets");
            if (assets.length() == 0) {
                imglink = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";
            } else {
                JSONObject zerot = assets.getJSONObject(0);
                imglink = zerot.getString("file");
            }
        }
        String description = new String();

        JSONArray body = blocks.getJSONArray("body");
        for (int i = 0; i < body.length(); i++) {
            description += body.getJSONObject(i).getString("bodyHtml");
        }


        Picasso.with(this)
                .load(imglink)
                .error(R.drawable.fallback_logo)
                .placeholder(R.drawable.fallback_logo)
                .into((ImageView) findViewById(R.id.imageView));

        TextView title_view = findViewById(R.id.textView);
        TextView des_view = findViewById(R.id.textView4);
        TextView time_view = findViewById(R.id.textView3);
        TextView section_view = findViewById(R.id.textView2);
        TextView url_view = findViewById(R.id.url_view);

        spinner.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        detailCard.setVisibility(View.VISIBLE);

        mToolbar.setTitle(title);

        title_view.setText(title);
        section_view.setText(section);

        Instant timestamp = Instant.parse(time);
        ZonedDateTime articleTime = timestamp.atZone(ZoneId.of("America/Los_Angeles"));

        Instant instant = articleTime.toInstant();
        Date dateFormat = Date.from(instant);

        if(articleTime.getDayOfMonth() < 10)
        {
            time_view.setText("0" + articleTime.getDayOfMonth() + " " + monthShortNames[dateFormat.getMonth()] + " " + articleTime.getYear());
        }
        else { time_view.setText(articleTime.getDayOfMonth() + " " + monthShortNames[dateFormat.getMonth()] + " " + articleTime.getYear()); }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            des_view.setText(Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY));
        } else {
            des_view.setText(Html.fromHtml(description));
        }

        url_view.setText(R.string.detailURL);
        url_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(url); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return;
            }
        });
    }


}
