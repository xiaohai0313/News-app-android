package com.example.hw93;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hw93.ui.bookmark.BookmarkFragment;
import com.example.hw93.ui.headlines.HeadlinesFragment;
import com.example.hw93.ui.home.HomeFragment;
import com.example.hw93.ui.trending.TrendingFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity<LocationFragment> extends AppCompatActivity {

    private HomeFragment hfg;
    private HeadlinesFragment hefg;
    private TrendingFragment tfg;
    private BookmarkFragment bfg;
    private SearchView mSearchView;
    private Toolbar mToolbar;
    private AutoSuggestAdapter searchAdapter;
    private List<String> searchResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

        //initial result array and adapter bind with 1line TextView
        searchResult = new ArrayList<String>();
        searchAdapter = new AutoSuggestAdapter(this,android.R.layout.simple_dropdown_item_1line);

        //initial toolbar and active it
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        //Set up bottom nav view, initial transaction with homeFragment
        BottomNavigationView navView = findViewById(R.id.nav_view);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, new HomeFragment());
        transaction.commit();
        navView.setOnNavigationItemSelectedListener(navigationlistener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the search menu action bar.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        // Get the search menu.
        MenuItem searchMenu = menu.findItem(R.id.search_bar_menu);

        // Get SearchView object.
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        ComponentName componentName = new ComponentName(this, SearchActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

        // Set AutoCompleteTextView object
        final AutoCompleteTextView searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setAdapter(searchAdapter);

        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                // Fill in search box
                String queryString=(String)adapterView.getItemAtPosition(itemIndex);
                searchAutoComplete.setText(queryString);
            }
        });

        // Below event is triggered when submit search query.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Go search Activity
                Intent myIntent = new Intent(MainActivity.this, SearchActivity.class);
                myIntent.putExtra("query",query);
                startActivity(myIntent);
                //MainActivity.this.finish();       Don't add this line otherwise we couldn't come back here again
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length() < 3)
                {
                    // Clean history if length < 3
                    searchAdapter.clear();
                }
                else if(newText.length() >= 3)
                {
                    RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                    String url = "https://api.cognitive.microsoft.com/bing/v7.0/suggestions?q=" + newText;

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                            (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        JSONArray suggestionGroups = response.getJSONArray("suggestionGroups");
                                        JSONArray searchSuggestions = suggestionGroups.getJSONObject(0).getJSONArray("searchSuggestions");
                                        searchResult.clear();
                                        for(int i = 0 ; i < searchSuggestions.length(); i++)
                                        {
                                            searchResult.add(searchSuggestions.getJSONObject(i).getString("displayText"));
                                        }
                                        // Update Adapter data and force updateView
                                        searchAdapter.setData(searchResult);
                                        searchAdapter.notifyDataSetChanged();
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
                            })
                    {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String>  params = new HashMap<String, String>();
                            params.put("Ocp-Apim-Subscription-Key", "9c85981dd8db40a8b68540a9f9141e1a");
                            return params;
                        }
                    };
                    queue.add(jsonObjectRequest);
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    // fragments switch
    private BottomNavigationView.OnNavigationItemSelectedListener navigationlistener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    transaction.replace(R.id.nav_host_fragment, new HomeFragment());
                    break;
                case R.id.navigation_headlines:
                    transaction.replace(R.id.nav_host_fragment, new HeadlinesFragment());
                    break;
                case R.id.navigation_trending:
                    transaction.replace(R.id.nav_host_fragment, new TrendingFragment());
                    break;
                case R.id.navigation_bookmark:
                    transaction.replace(R.id.nav_host_fragment, new BookmarkFragment());
                    break;
            }
            transaction.commit();
            return true;
        }
    };
}
