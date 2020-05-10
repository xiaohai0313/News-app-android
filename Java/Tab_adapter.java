package com.example.hw93;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


public class Tab_adapter extends RecyclerView.Adapter<Tab_adapter.ViewHolder> {


    private String[] list;
    private static RecyclerViewClickListener itemListener;

    public Tab_adapter(String[] tabs, RecyclerViewClickListener itemListener)
    {
        list = tabs;
        this.itemListener = itemListener;       // this line bind interface with fragment object
    }

    public interface RecyclerViewClickListener {
        void recyclerViewListClicked(View v, String tab);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public TextView tab;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            tab = (TextView) itemView.findViewById(R.id.headline_tab);
        }

        @Override
        public void onClick(View itemView) {
            itemListener.recyclerViewListClicked(itemView,this.tab.getText().toString());
            return ;
        }

        @Override
        public boolean onLongClick(View itemView) {
            itemListener.recyclerViewListClicked(itemView,this.tab.getText().toString());
            return false;
        }
    }

    @Override
    public Tab_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.headline_tab, parent, false);
        Tab_adapter.ViewHolder ViewHolder = new Tab_adapter.ViewHolder(view);

        return ViewHolder;
    }

    @Override
    public void onBindViewHolder(Tab_adapter.ViewHolder holder, int position) {
        String text = list[position];
        holder.tab.setText(text);
        if(position==0)
        {
            // Set world to be initial value, send callback to HeadlinesFragment initial bind preview with first holder
            holder.onClick(holder.tab);
            holder.tab.setBackgroundResource(R.drawable.background_headline_tab);
        }
    }

    @Override
    public int getItemCount() {

        return 6;
    }
}