// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.form;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.NumberPicker;
import android.widget.TextView;
import com.reelsonar.ibobber.R;

public class NumberField extends FormGroup {

    private Context _context;
    private String _label;
    private String _suffix;
    private int _value;
    private int _minValue;
    private int _maxValue;

    public NumberField(final Context context, final String label, final String suffix, final int value, final int minValue, final int maxValue) {
        _context = context;
        _label = label;
        _suffix = suffix;
        _value = value;
        _minValue = minValue;
        _maxValue = maxValue;
    }

    public NumberField(final Context context, final int label, final String suffix, final int value, final int minValue, final int maxValue) {
        this(context, context.getString(label), suffix, value, minValue, maxValue);
    }

    @Override
    public int getViewWrapperId() {
        return R.id.formNumberWrapper;
    }

    @Override
    public View getGroupView(final boolean isExpanded, final View convertView, final ViewGroup parent) {
        View view;
        if (convertView != null && convertView.getId() == getViewWrapperId()) {
            view = convertView;
        } else {
            view = LayoutInflater.from(_context).inflate(R.layout.triplog_form_fish, null);
        }

        TextView label = (TextView)view.findViewById(R.id.formLabel);
        label.setPadding(0, 0, 0, 0);
        label.setText(_label);
        label.setTypeface(getTypeface());
        updateValueField(view);

        return view;
    }

    public void onValueChanged(final int value) {

    }

    @Override
    public void onGroupClick(final View view, final boolean isExpanded) {
        AlertDialog.Builder builder = new AlertDialog.Builder(_context, AlertDialog.THEME_HOLO_LIGHT);

        View alertView = LayoutInflater.from(_context).inflate(R.layout.alert_dialog_number_picker, null);
        final NumberPicker picker = (NumberPicker)alertView.findViewById(R.id.numberPicker);
        picker.setMinValue(_minValue);
        picker.setMaxValue(_maxValue);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setValue(_value);

        builder.setTitle(_label);
        builder.setView(alertView);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                _value = picker.getValue();
                onValueChanged(_value);

                updateValueField(view);
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

    private void updateValueField(final View rootView) {
        StringBuilder value = new StringBuilder();
        value.append(_value);
        if (_suffix != null) {
            value.append(_suffix);
        }

        TextView field = (TextView)rootView.findViewById(R.id.formField);
        field.setText(value.toString());
    }
}
