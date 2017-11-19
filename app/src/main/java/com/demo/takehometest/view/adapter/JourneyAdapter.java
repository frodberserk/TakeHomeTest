package com.demo.takehometest.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.demo.takehometest.R;
import com.demo.takehometest.database.Journey;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter class to populate views for journey list.
 */

public class JourneyAdapter extends RecyclerView.Adapter<JourneyAdapter.JourneyViewHolder> {

    private ArrayList<Journey> mJourneyList = new ArrayList<>();

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
        holder.tvStartTime.setText(String.valueOf(journey.getStartTime()));
        holder.tvEndTime.setText(String.valueOf(journey.getEndTime()));
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