package com.demo.takehometest.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.demo.takehometest.R;
import com.demo.takehometest.model.Journey;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class to populate views for journey list.
 */

public class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.JourneyViewHolder> {

    /**
     * List of journeys.
     */
    private ArrayList<Journey> mJourneyList = new ArrayList<>();

    /**
     * Format to display time in table.
     */
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, hh:mm aa");

    @Override
    public JourneyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Create and return ViewHolder
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new JourneyViewHolder(inflater.inflate(R.layout.row_journey,
                parent, false));
    }

    @Override
    public void onBindViewHolder(JourneyViewHolder holder, int position) {
        //Set data on view for a particular row @position.
        Journey journey = mJourneyList.get(position);
        holder.tvId.setText(String.valueOf(journey.getId()));

        holder.tvStartTime.setText(niceTime(journey.getStartTime()));
        holder.tvEndTime.setText(niceTime(journey.getEndTime()));
    }

    @Override
    public int getItemCount() {
        return mJourneyList.size();
    }

    public void setData(List<Journey> data) {
        mJourneyList.clear();
        mJourneyList.addAll(data);
        notifyDataSetChanged();
    }

    /**
     * Returns readable time as String.
     *
     * @param millis Time in millis to convert.
     * @return Converted time in string.
     */
    private String niceTime(long millis) {
        if (millis == 0) {
            //Unknown time
            return "-";
        } else {
            return simpleDateFormat.format(new Date(millis));
        }
    }

    /**
     * ViewHolder class. It will save the reference of views in each journey list row.
     */
    class JourneyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_id)
        TextView tvId;
        @BindView(R.id.tv_start_time)
        TextView tvStartTime;
        @BindView(R.id.tv_end_time)
        TextView tvEndTime;

        JourneyViewHolder(View itemView) {
            super(itemView);

            //Bind views
            ButterKnife.bind(this, itemView);
        }
    }
}