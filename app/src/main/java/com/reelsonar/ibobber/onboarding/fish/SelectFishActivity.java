// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.onboarding.fish;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.widget.ListView;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.model.FavoriteFish;
import com.reelsonar.ibobber.triplog.FavoriteFishLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectFishActivity extends Activity implements LoaderManager.LoaderCallbacks<List<FavoriteFish>> {

    private static final String TAG = SelectFishActivity.class.getSimpleName();

    private List<FavoriteFish> _fish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_fish);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<List<FavoriteFish>> onCreateLoader(final int id, final Bundle args) {
        Set<Integer> selectedFishIds = new HashSet<>();
        List<FavoriteFish> selectedFish = getIntent().getParcelableArrayListExtra("fish");
        if (selectedFish != null) {
            for (FavoriteFish selected : selectedFish) {
                selectedFishIds.add(selected.getId());
            }
        }

        return new FavoriteFishLoader(this, selectedFishIds);
    }

    @Override
    public void onLoadFinished(final Loader<List<FavoriteFish>> loader, final List<FavoriteFish> fish) {
        _fish = fish;
        ListView listView = (ListView)findViewById(R.id.lstFish);
        FishAdapter fishAdapter = new FishAdapter(SelectFishActivity.this, fish);
        listView.setAdapter(fishAdapter);
    }

    @Override
    public void onLoaderReset(final Loader<List<FavoriteFish>> loader) {

    }

    private void returnSelectedFish() {
        ArrayList<FavoriteFish> selectedFish = new ArrayList<>();
        for (FavoriteFish fish : _fish) {
            if (fish.isSelected()) {
                selectedFish.add(fish);
            }
        }
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("fish", selectedFish);
        setResult(RESULT_OK, intent);
    }

    @Override
    public void onBackPressed() {
        returnSelectedFish();
        super.onBackPressed();
    }

}
