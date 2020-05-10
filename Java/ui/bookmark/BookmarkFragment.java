package com.example.hw93.ui.bookmark;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hw93.Bookmark_adapter;
import com.example.hw93.DetailActivity;
import com.example.hw93.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Set;


public class BookmarkFragment extends Fragment implements Bookmark_adapter.CardViewClickListener {
    private RecyclerView mRecyclerView;
    private ArrayList<String> imagelink;
    private ArrayList<String> title;
    private ArrayList<String> section;
    private ArrayList<String> time;
    private ArrayList<String> id;

    private Bookmark_adapter bookmark_adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.bookmark_card);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("id_list", Context.MODE_PRIVATE);
        // Retrieve the values
        Set<String> pre = sharedPreferences.getStringSet("id_list", null);
        if (pre == null || pre.size() == 0) {
            view.findViewById(R.id.text_bookmark).setVisibility(View.VISIBLE);
            return view;
        }
        view.findViewById(R.id.text_bookmark).setVisibility(View.GONE);

        imagelink = new ArrayList<>();
        title = new ArrayList<>();
        section = new ArrayList<>();
        time = new ArrayList<>();
        id = new ArrayList<>();

        for (String item : pre) {
            String[] current = item.split("###");
            id.add(current[0]);
            title.add(current[1]);
            imagelink.add(current[2]);
            section.add(current[3]);
            time.add(current[4]);
        }
        setupRecyclerView();

        // Each row display two card
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        return view;
    }

    private void setupRecyclerView() {
        bookmark_adapter = new Bookmark_adapter(imagelink, title, section, time, id, this);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(bookmark_adapter);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
    }

    // Check if removed from detail activity
    @Override
    public void onResume() {
        super.onResume();
        // Update private value (we need dynamic change the number of card, is different with home and headline fragment)
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("id_list", Context.MODE_PRIVATE);
        Set<String> pre = sharedPreferences.getStringSet("id_list", null);
        if (pre == null || pre.size() == 0) {
            getView().findViewById(R.id.text_bookmark).setVisibility(View.VISIBLE);
            return;
        }
        imagelink = new ArrayList<>();
        title = new ArrayList<>();
        section = new ArrayList<>();
        time = new ArrayList<>();
        id = new ArrayList<>();

        for (String item : pre) {
            String[] current = item.split("###");
            id.add(current[0]);
            title.add(current[1]);
            imagelink.add(current[2]);
            section.add(current[3]);
            time.add(current[4]);
        }

        if (id.size() == 0) {
            // if storage cleared
            getView().findViewById(R.id.text_bookmark).setVisibility(View.VISIBLE);
        } else {
            // if still has bookmark
            getView().findViewById(R.id.text_bookmark).setVisibility(View.GONE);
            setupRecyclerView();
            mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }
    }

    @Override
    public void cardViewClickListener(final View v, final int position, String aLong) {
        if (aLong.equals("short")) {
            String link = id.get(position);
            Intent myIntent = new Intent(getActivity(), DetailActivity.class);
            myIntent.putExtra("link", link);
            startActivity(myIntent);

        } else if (aLong.equals("long")) {
            String img = imagelink.get(position);
            final String cur_title = title.get(position);
            String cur_section = section.get(position);

            String cur_time = time.get(position);


            final String cur_id = id.get(position);
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

            bookmark.setImageResource(R.drawable.bookmark_selected48_foreground);
            // bookmark click
            bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View dialogView) {
                    // This is delete action

                    Context context = getContext();
                    SharedPreferences sharedPreferences = context.getSharedPreferences("id_list", Context.MODE_PRIVATE);
                    // Retrieve the values
                    Set<String> pre = sharedPreferences.getStringSet("id_list", null);
                    for (String s : pre) {
                        String[] id_test = s.split("###");
                        if (cur_id.equals(id_test[0])) {
                            // Remove from preference
                            pre.remove(s);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();
                            myEdit.clear();
                            myEdit.putStringSet("id_list", pre);
                            myEdit.apply();

                            Toast.makeText(v.getContext(), "\"" + cur_title + "\"" + " was removed from Bookmarks",
                                    Toast.LENGTH_LONG).show();
                            imagelink.remove(position);
                            title.remove(position);
                            section.remove(position);
                            time.remove(position);
                            id.remove(position);

                            if (id.size() == 0) {
                                // if storage cleared
                                v.getRootView().findViewById(R.id.text_bookmark).setVisibility(View.VISIBLE);
                            } else {
                                // if still has bookmark
                                setupRecyclerView();
                                mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                            }
                            dialog.dismiss();
                            return;
                        }
                    }
                }
            });

            twitter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View dialogView) {
                    String url = "https://twitter.com/intent/tweet?" + "text=Check out this Link:&" + "hashtags=CSCI571NewsSearch&" + "url=https://theguardian.com/" + id.get(position);
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });
            dialog.show();
        }
    }
}