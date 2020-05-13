package com.example.hw93.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class HomeFragment extends Fragment implements LocationListener, MyAdapter.CardViewClickListener {
    private static String TAG = HomeFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private MyAdapter headlineAdapter;
    private String[] imagelink;
    private String[] title;
    private String[] section;
    private String[] time;
    private String[] id;
    private String[] bookmark;
    public double latitude;
    public double longitude;
    public Criteria criteria;
    LocationManager mgr;
    private ProgressBar spinner;
    private TextView progress;
    public String bestProvider;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.home_card);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_fresh);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        spinner = view.findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);
        progress = view.findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);

        mgr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        bestProvider = mgr.getBestProvider(criteria, true);
        checkLocationPermission();

        getLocation(view);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String[] inputs = new String[]{"latest"};
                mSwipeRefreshLayout.setRefreshing(false);
                getLocation(view);
            }
        });
        return view;
    }

    // API calls
    public void fetchData(final String[] split, final View view) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url;
        if (split[0] == "latest") {
            url = "https://theh9backend.wm.r.appspot.com/api/latest";
        } else {
            url = "https://api.openweathermap.org/data/2.5/weather?q=" + split[1] +
                    "&units=metric&appid=keys";
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            manipulateData(response, split, view);
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

    // Manipulate response data
    public void manipulateData(JSONObject info, String[] split, View view) throws JSONException {
        if (split[0].equals("latest")) {
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
                if (cur.has("fields")) {
                    JSONObject fields = cur.getJSONObject("fields");
                    //String assets = cur.getJSONObject("fields").getString("thumbnail");
                    if (fields.has("thumbnail") && fields.getString("thumbnail").length() > 0) {
                        imagelink[i] = fields.getString("thumbnail");
                    } else {
                        imagelink[i] = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";
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
        } else if (split[0].equals("weather")) {
            JSONObject main = info.getJSONObject("main");
            String temp = main.getString("temp");
            Integer index = temp.indexOf(".");
            if (index == -1) {
                temp = temp + " \u2103";
            } else {
                temp = temp.substring(0, index) + " \u2103";
            }

            JSONArray weather = info.getJSONArray("weather");
            String summary = weather.getJSONObject(0).getString("main");
            String img_link;
            ImageView imageView = view.findViewById(R.id.weather_card);
            if (summary.equals("Clouds")) {
                imageView.setImageResource(R.drawable.cloudy_weather);
            } else if (summary.equals("Clear")) {
                imageView.setImageResource(R.drawable.clear_weather);
            } else if (summary.equals("Snow")) {
                imageView.setImageResource(R.drawable.snowy_weather);
            } else if (summary.equals("Rain/Drizzle")) {
                imageView.setImageResource(R.drawable.rainy_weather);
            } else if (summary.equals("Thunderstorm")) {
                imageView.setImageResource(R.drawable.thunder_weather);
            } else {
                imageView.setImageResource(R.drawable.sunny_weather);
            }
            view.setClipToOutline(true);

            imageView.setClipToOutline(true);
            TextView city = (TextView) view.findViewById(R.id.city_text);
            city.setText(split[1]);
            TextView state = (TextView) view.findViewById(R.id.state_text);
            state.setText(split[2]);
            TextView temputure = (TextView) view.findViewById(R.id.temp_text);
            temputure.setText(temp);
            TextView condition = (TextView) view.findViewById(R.id.condition_text);
            condition.setText(summary);

        }
    }

    // Check for location permission at first time
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                requestPermissions(
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);

                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    public void getLocation(View view) {

        //You can still do this if you like, you might get lucky:

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (bestProvider != null) {
            Location location = mgr.getLastKnownLocation(bestProvider);
            if (location != null) {
                Log.e("TAG", "GPS is on");
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String cityName = addresses.get(0).getLocality();
                String stateName = addresses.get(0).getAdminArea();
                String countryName = addresses.get(0).getCountryName();
                String countrycode = addresses.get(0).getCountryCode();
                String[] input = new String[3];
                input[0] = "weather";
                input[1] = cityName;
                input[2] = stateName;

                fetchData(input, view);
                String[] inputs = new String[]{"latest"};

                fetchData(inputs, view);

            } else {

                mgr.requestLocationUpdates(bestProvider, 1000, 0, this);
            }
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        mgr.removeUpdates(this);
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.i("Location info: Lat", String.valueOf(latitude));
        Log.i("Location info: Lng", String.valueOf(longitude));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "StatusChanged");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        //View view = inflater.inflate(R.layout.fragment_home, container, false);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        mgr.requestLocationUpdates(mgr.NETWORK_PROVIDER, 0, 0, this);
                        //mgr.requestLocationUpdates(bestProvider, 0, 0, this);
                        Location location = mgr.getLastKnownLocation(mgr.getBestProvider(new Criteria(), true));
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                        List<Address> addresses = null;
                        try {
                            addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String cityName = addresses.get(0).getLocality();
                        String stateName = addresses.get(0).getAdminArea();
                        String countryName = addresses.get(0).getCountryName();
                        String countrycode = addresses.get(0).getCountryCode();
                        String[] input = new String[3];
                        input[0] = "weather";
                        input[1] = cityName;
                        input[2] = stateName;

                        fetchData(input, getView());
                        String[] inputs = new String[]{"latest"};

                        fetchData(inputs, getView());
                        //onLocationChanged(location);
                        //getLocation(getView());
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

    // Check permission and each card bookmark
    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            // Re-render for bookmark checking
            if (headlineAdapter != null) {
                setupRecyclerView();
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mgr.removeUpdates(this);
        }
    }

    private void setupRecyclerView() {
        headlineAdapter = new MyAdapter(imagelink, title, section, time, bookmark, id, "latest", this);
        mRecyclerView.setAdapter(headlineAdapter);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        spinner.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
    }

    // Deal card click event
    @Override
    public void cardViewClickListener(final View v, final int position, String aLong) {
        if (aLong.equals("short")) {
            String link = id[position];
            Intent myIntent = new Intent(getActivity(), DetailActivity.class);
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
}


