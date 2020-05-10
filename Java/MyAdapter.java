package com.example.hw93;


import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {


    static String latestORheadline;
    static String[] cur_image;
    static String[] cur_title;
    static String[] cur_section;
    static String[] cur_time;
    static String[] cur_bookmark;
    static String[] cur_id;
    private static CardViewClickListener itemListener;

    public MyAdapter(String[] link, String[] title, String[] section, String[] time, String[] bookmark, String[] id, String type, CardViewClickListener itemListener) {
        cur_image = link;
        cur_title = title;
        cur_section = section;
        cur_time = time;
        cur_bookmark = bookmark;
        cur_id = id;
        latestORheadline = type;
        this.itemListener = itemListener;

        //this.cur_image = link;
    }

    public interface CardViewClickListener {
        void cardViewClickListener(View v, int position, String aLong);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public ImageView mPoster;
        public TextView title;
        public TextView section;
        public TextView time;
        public ImageButton bookmark;
        public TextView id;

        public ViewHolder(final View itemView) {
            super(itemView);

            mPoster = (ImageView) itemView.findViewById(R.id.title_img);
            title = (TextView) itemView.findViewById(R.id.title_text);
            section = (TextView) itemView.findViewById(R.id.section);
            time = (TextView) itemView.findViewById(R.id.time);
            bookmark = (ImageButton) itemView.findViewById(R.id.bookmark_icon);
            id = (TextView) itemView.findViewById(R.id.invisible_id_field);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            // bookmark bind with itemView => each bookmark is combine with individual card
            bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // This is delete action
                    String this_id = cur_id[getAdapterPosition()];
                    String this_img = cur_image[getAdapterPosition()];
                    String this_title = cur_title[getAdapterPosition()];
                    String this_section = cur_section[getAdapterPosition()];
                    String this_time = cur_time[getAdapterPosition()];
                    String storage = this_id + "###" + this_title + "###" + this_img + "###" + this_section + "###" + this_time ;

                    Context context = itemView.getContext();
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
                            if (this_id.equals(id_test[0])) {
                                // Remove from preference NOTE: remove from pre, push again didn't work out
                                pre.remove(s);
                                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                myEdit.clear();
                                myEdit.putStringSet("id_list", pre);
                                myEdit.apply();
                                Toast.makeText(v.getContext(), "\"" + this_title + "\"" + " was removed from Bookmarks",
                                        Toast.LENGTH_LONG).show();
                                // Change bookmark image
                                bookmark.setBackgroundResource(R.drawable.bookmark_unselected48_foreground);
                                return;
                            }
                        }
                    }
                    // Add new id in it
                    pre.add(storage);
                    Toast.makeText(v.getContext(), "\"" + this_title + "\"" + " was added to Bookmarks",
                            Toast.LENGTH_LONG).show();
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                    myEdit.clear();
                    myEdit.putStringSet("id_list", pre);
                    myEdit.apply();

                    // Change bookmark image
                    bookmark.setBackgroundResource(R.drawable.bookmark_selected48_foreground);
                    return;
                }

            });
        }

        @Override
        public void onClick(View itemView) {
            itemListener.cardViewClickListener(itemView, this.getPosition(), "short");
            return;
        }

        @Override
        public boolean onLongClick(View itemView) {
            itemListener.cardViewClickListener(itemView, this.getPosition(), "long");
            return true;
        }
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.home_card, parent, false);
        MyAdapter.ViewHolder ViewHolder = new MyAdapter.ViewHolder(view);
        return ViewHolder;
    }

    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {
        String image = cur_image[position];
        String title = cur_title[position];
        String section = cur_section[position];
        String time = cur_time[position];
        String book = cur_bookmark[position];
        String id = cur_id[position];
        holder.section.setText(section);
        holder.title.setText(title);
        //holder.time.setText(time);
        holder.id.setText(id);

        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.of("America/Los_Angeles"));

        Instant timestamp = Instant.parse(time);
        ZonedDateTime articleTime = timestamp.atZone(ZoneId.of("America/Los_Angeles"));

        Calendar calendar = GregorianCalendar.from(articleTime);
        Calendar current_time = Calendar.getInstance();

        long secondDifference = ChronoUnit.SECONDS.between(articleTime, currentTime);
        long hourDifference = ChronoUnit.HOURS.between(articleTime, currentTime);
        long minDifference = ChronoUnit.MINUTES.between(articleTime, currentTime);
        long dayDifference = ChronoUnit.DAYS.between(articleTime, currentTime);
        String diff = "";
        // only search has day option
        if(dayDifference > 0 && latestORheadline.equals("search"))
        {
            diff += dayDifference + "d ago";
        }
        else if (hourDifference > 0)
        {
            diff += hourDifference + "h ago";
        }
        else if (minDifference > 0)
        {
            diff += minDifference + "m ago";
        }
        else
        {
            diff += secondDifference + "s ago";
        }
        holder.time.setText(diff);

        Picasso.with(holder.itemView.getContext())
                .load(image)
                .error(R.drawable.fallback_logo)
                .placeholder(R.drawable.fallback_logo)

                .into(holder.mPoster);
        holder.mPoster.setClipToOutline(true);
        // Test if this card is in bookmark
        Context context = holder.itemView.getContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("id_list", Context.MODE_PRIVATE);
        // Retrieve the values
        Set<String> pre = sharedPreferences.getStringSet("id_list", null);

        if (pre != null) {
            for (String s : pre) {
                String[] storage = s.split("###");
                if (id.equals(storage[0])) {
                    holder.bookmark.setBackgroundResource(R.drawable.bookmark_selected48_foreground);
                    return;
                }
            }
        }
        holder.bookmark.setBackgroundResource(R.drawable.bookmark_unselected48_foreground);
    }

    @Override
    public int getItemCount() {
        if(latestORheadline.equals("latest"))
        {
            return 10;
        }
        return cur_image.length;
    }
}
