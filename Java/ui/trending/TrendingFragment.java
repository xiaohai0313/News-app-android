package com.example.hw93.ui.trending;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hw93.R;
import com.example.hw93.ui.home.HomeFragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class TrendingFragment extends Fragment {

    private LineChart mChart;
    private ProgressBar spinner;
    private TextView progress;
    @Override
    //protected void onCreate(View view, Bundle savedInstanceState){
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        View view = inflater.inflate(R.layout.fragment_trending, container, false);
        mChart = (LineChart) view.findViewById(R.id.linechart);
        mChart.setVisibility(View.GONE);
        spinner = view.findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);
        progress = view.findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);
        fetchdata("coronavirus");
        EditText editText = (EditText) view.findViewById(R.id.search_form);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                    //sendMessage();
                    spinner.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.VISIBLE);
                    mChart.setVisibility(View.GONE);
                    fetchdata(v.getText().toString());
                    //fetchdata(getView().findViewById(R.id.search_form).gett().toString());
                    handled = true;
                }
                return handled;
            }
        });

        return view;

    }


    public void fetchdata(final String search) {
        RequestQueue queue = Volley.newRequestQueue(getContext());
        String url = "https://theh9backend.wm.r.appspot.com/api/trending?data=" + search;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            spinner.setVisibility(View.GONE);
                            progress.setVisibility(View.GONE);
                            mChart.setVisibility(View.VISIBLE);
                            manipulateData(response,search);
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

    public void manipulateData(JSONObject info, String search) throws JSONException {
        JSONObject defa = info.getJSONObject("default");
        JSONArray timelineData = defa.getJSONArray("timelineData");
        ArrayList<Integer> data = new ArrayList<Integer>();
        for (int i = 0; i < timelineData.length(); i++) {
            data.add(Integer.valueOf(timelineData.getJSONObject(i).getJSONArray("value").getString(0)));
        }

        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(false);
        //mChart.setDrawGridBackground(false);
        //mChart.setDrawBorders(false);
        //mChart.getXAxis().setEnabled(false);      // x bar
        mChart.getAxisLeft().setDrawGridLines(false);   // hor grid line from left bar
        //mChart.getAxisLeft().setDrawZeroLine(false);    // ?
        mChart.getAxisLeft().setDrawAxisLine(false);    // left bar hide line
        //mChart.getAxisLeft().setEnabled(false);     //left bar
        mChart.getXAxis().setDrawGridLines(false);  // vertical grid
        mChart.getAxisRight().setDrawGridLines(false);      // horizontal grid


        ArrayList<Entry> yValues = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {

            yValues.add(new Entry(Integer.valueOf(i), Integer.valueOf(data.get(i))));
        }

        LineDataSet set1 = new LineDataSet(yValues, "Trending Chart for " + search);
        set1.setColor(requireActivity().getColor(R.color.colorPrimaryDark));
        set1.setCircleColor(requireActivity().getColor(R.color.colorPrimaryDark));
        set1.setCircleHoleColor(requireActivity().getColor(R.color.colorPrimaryDark));
        set1.setFormSize(15);   //label size

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        LineData data_two = new LineData(dataSets);
        //data_two.setValueFormatter(new myvalueformatter());
        mChart.setData(data_two);
        mChart.invalidate();        //refresh
    }
}

