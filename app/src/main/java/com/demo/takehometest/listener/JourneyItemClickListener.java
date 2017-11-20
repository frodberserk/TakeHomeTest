package com.demo.takehometest.listener;

import com.demo.takehometest.model.Journey;

/**
 * Listener for journey click events in list.
 */

public interface JourneyItemClickListener {

    /**
     * Called when item is clicked.
     *
     * @param position Position of clicked item.
     * @param journey  Journey object for clicked item.
     */
    void onItemClicked(int position, Journey journey);
}