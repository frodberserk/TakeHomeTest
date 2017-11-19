package com.demo.takehometest.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.demo.takehometest.R;
import com.demo.takehometest.controller.JourneyListController;
import com.demo.takehometest.model.Journey;
import com.demo.takehometest.listener.JourneyQueryCallback;
import com.demo.takehometest.view.adapter.JourneyAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Activity to display list of completed/ongoing journeys.
 */

public class JourneyListActivity extends AppCompatActivity {


    /**
     * View to display journey list.
     */
    @BindView(R.id.rv_journey)
    RecyclerView rvJourney;

    private Unbinder mUnbinder;

    /**
     * Controller object for this view.
     */
    private JourneyListController mController;

    private JourneyAdapter mJourneyAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_list);

        //Bind views
        mUnbinder = ButterKnife.bind(this);

        //Initialize controller
        mController = new JourneyListController(this);

        //Initialize adapter
        mJourneyAdapter = new JourneyAdapter();

        //Add layout manager to RecyclerView
        rvJourney.setLayoutManager(new LinearLayoutManager(this));

        //Set adapter to RecyclerView
        rvJourney.setAdapter(mJourneyAdapter);

        displayData();
    }

    /**
     * Display the journey data provided by controller.
     */
    private void displayData() {
        mController.fetchJourneyData(new JourneyQueryCallback() {
            @Override
            public void onQuerySuccessful(List<Journey> journeys) {
                mJourneyAdapter.setData(journeys);
            }
        });
    }
}