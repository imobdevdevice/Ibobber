// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.onboarding.fish;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.model.FavoriteFish;

import java.util.List;

public class FishAdapter extends ArrayAdapter<FavoriteFish> implements CompoundButton.OnCheckedChangeListener {

    public FishAdapter(Context context, List<FavoriteFish> fish) {
        super(context, R.layout.chk_fish, fish);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CheckBox checkbox;

        if (!(convertView instanceof CheckBox)) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "Gotham-Medium.otf");
            checkbox = (CheckBox)inflater.inflate(R.layout.chk_fish, null);
            checkbox.setTypeface(tf);
        } else {
            checkbox = (CheckBox)convertView;
        }

        FavoriteFish fish = getItem(position);
        checkbox.setOnCheckedChangeListener(null);
        checkbox.setChecked(fish.isSelected());
        checkbox.setText(fish.getName());
        checkbox.setTag(position);
        checkbox.setOnCheckedChangeListener(this);

        return checkbox;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        int position = (Integer)compoundButton.getTag();
        FavoriteFish fish = getItem(position);
        fish.setSelected(checked);
    }
}
