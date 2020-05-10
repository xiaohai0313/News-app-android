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
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

public class Bookmark_adapter extends RecyclerView.Adapter<Bookmark_adapter.ViewHolder> {

    static ArrayList<String> cur_image;
    static ArrayList<String> cur_title;
    static ArrayList<String> cur_section;
    static ArrayList<String> cur_time;
    static ArrayList<String> cur_bookmark;
    static ArrayList<String> cur_id;
    private static CardViewClickListener itemListener;
    private String[] monthShortNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun","Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    public Bookmark_adapter(ArrayList<String> link, ArrayList<String> title, ArrayList<String> section, ArrayList<String> time,  ArrayList<String> id, CardViewClickListener itemListener) {
        cur_image = link;
        cur_title = title;
        cur_section = section;
        cur_time = time;
        cur_id = id;
        this.itemListener = itemListener;
    }

    // callback to bookmark fragment
    public interface CardViewClickListener {
        void cardViewClickListener(View v, int position, String aLong);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
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
                    String this_id = cur_id.get(getAdapterPosition());
                    String this_img = cur_image.get(getAdapterPosition());
                    String this_title = cur_title.get(getAdapterPosition());
                    String this_section = cur_section.get(getAdapterPosition());
                    String this_time = cur_time.get(getAdapterPosition());
                    String storage = this_id + "###" + this_title + "###" + this_img + "###" + this_section + "###" + this_time;

                    Context context = v.getContext();
                    SharedPreferences sharedPreferences = context.getSharedPreferences("id_list", Context.MODE_PRIVATE);
                    // Retrieve the values
                    Set<String> pre = sharedPreferences.getStringSet("id_list", null);
                    // pre is null only can add

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

                            removeAt(getAdapterPosition());
                            if (pre.size() == 0) {
                                itemView.getRootView().findViewById(R.id.text_bookmark).setVisibility(View.VISIBLE);
                            }
                            return;
                        }
                    }
                }
            });
        }

        public void removeAt(int position) {
            cur_image.remove(position);
            cur_title.remove(position);
            cur_section.remove(position);
            cur_time.remove(position);
            cur_id.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cur_image.size());
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
    public Bookmark_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.bookmark_card, parent, false);
        Bookmark_adapter.ViewHolder ViewHolder = new Bookmark_adapter.ViewHolder(view);
        return ViewHolder;
    }

    @Override
    public void onBindViewHolder(Bookmark_adapter.ViewHolder holder, int position) {
        String image = cur_image.get(position);
        String title = cur_title.get(position);
        String section = cur_section.get(position);
        String time = cur_time.get(position);
        String id = cur_id.get(position);
        holder.section.setText(section);
        holder.title.setText(title);

        Instant timestamp = Instant.parse(time);
        ZonedDateTime articleTime = timestamp.atZone(ZoneId.of("America/Los_Angeles"));

        Instant instant = articleTime.toInstant();
        Date dateFormat = Date.from(instant);

        if(articleTime.getDayOfMonth() < 10)
        {
            holder.time.setText("0" + articleTime.getDayOfMonth() + " " + monthShortNames[dateFormat.getMonth()]);
        }
        else { holder.time.setText(articleTime.getDayOfMonth() + " " + monthShortNames[dateFormat.getMonth()]); }


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
        return cur_image.size();
    }
}
