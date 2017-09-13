// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.triplog.form;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.form.FormGroup;
import com.reelsonar.ibobber.model.FavoriteFish;
import com.reelsonar.ibobber.model.triplog.TripLogFish;

import java.util.List;

public class FishFormGroup extends FormGroup {

    private Context _context;
    private TripLogFish _tripLogFish;
    private List<FavoriteFish> _allFish;

    public FishFormGroup(final Context context, final TripLogFish tripLogFish, final List<FavoriteFish> allFish) {
        _context = context;
        _tripLogFish = tripLogFish;
        _allFish = allFish;
    }

    @Override
    public int getChildrenCount() {
        return _allFish.size();
    }

    @Override
    public int getViewWrapperId() {
        return R.id.formLabelWrapper;
    }

    @Override
    public View getGroupView(final boolean isExpanded, final View convertView, final ViewGroup parent) {
        View view;
        if (convertView != null && convertView.getId() == getViewWrapperId()) {
            view = convertView;
        } else {
            view = LayoutInflater.from(_context).inflate(R.layout.form_label, null);
        }

        TextView label = (TextView)view.findViewById(R.id.formLabel);
        TextView field = (TextView)view.findViewById(R.id.formField);
        ImageView disclosure = (ImageView)view.findViewById(R.id.formDisclosure);

        label.setText(_context.getString(R.string.trip_log_fish_caught) + ": " + _tripLogFish.getTotalQuantity());
        label.setTypeface(getTypeface());
        field.setText("");
        field.setHint("");

        disclosure.setVisibility(View.VISIBLE);
        disclosure.setRotation(isExpanded ? 90.f : 270.f);

        return view;
    }

    @Override
    public View getChildView(final int childPosition, final boolean isLastChild, final View convertView, final ViewGroup parent) {
        View view;
        if (convertView != null && convertView.getTag() != null && "triplog-fish-item".equals(convertView.getTag())) {
            view = convertView;
        } else {
            view = LayoutInflater.from(_context).inflate(R.layout.triplog_form_fish, null);
        }

        TextView label = (TextView)view.findViewById(R.id.formLabel);
        TextView field = (TextView)view.findViewById(R.id.formField);

        FavoriteFish fish = _allFish.get(childPosition);
        label.setText(fish.getName());
        field.setText(String.valueOf(_tripLogFish.getQuantityForFishId(fish.getId())));

        return view;
    }

    @Override
    public void onGroupClick(final View view, final boolean isExpanded) {
        ImageView disclosure = (ImageView)view.findViewById(R.id.formDisclosure);

        disclosure.animate().rotationBy(180.f);
    }

    @Override
    public void onChildClick(final View view, final int position) {
        final FavoriteFish fish = _allFish.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(_context, AlertDialog.THEME_HOLO_LIGHT);

        View alertView = LayoutInflater.from(_context).inflate(R.layout.alert_dialog_number_picker, null);
        final NumberPicker picker = (NumberPicker)alertView.findViewById(R.id.numberPicker);
        picker.setMinValue(0);
        picker.setMaxValue(Integer.MAX_VALUE);
        picker.setWrapSelectorWheel(false);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setValue(_tripLogFish.getQuantityForFishId(fish.getId()));

        builder.setTitle(fish.getName());
        builder.setView(alertView);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                int quantity = picker.getValue();
                _tripLogFish.setQuantityForFishId(fish.getId(), quantity);

                TextView field = (TextView)view.findViewById(R.id.formField);
                field.setText(String.valueOf(quantity));
            }
        });
        builder.setNegativeButton(R.string.button_cancel, null);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager)_context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(picker, 0);
            }
        });
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager)_context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(((Activity)_context).getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        alertDialog.show();
    }

}
