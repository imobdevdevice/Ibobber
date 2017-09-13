// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.form;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.reelsonar.ibobber.R;

import java.util.ArrayList;
import java.util.List;

public class SpinnerField<E extends Enum & IdName> extends FormGroup implements Spinner.OnItemSelectedListener {

    private Context _context;
    private String _label;
    private E[] _enumConstants;
    private E _selectedConstant;

    public SpinnerField(final Context context, final String label, final Class<E> enumClass, final E selectedConstant) {
        _context = context;
        _label = label;
        _enumConstants = enumClass.getEnumConstants();
        _selectedConstant = selectedConstant;
    }

    public SpinnerField(final Context context, final int label, final Class<E> enumClass, final E selectedConstant) {
        this(context, context.getString(label), enumClass, selectedConstant);
    }

    @Override
    public int getViewWrapperId() {
        return R.id.formSpinnerWrapper;
    }

    @Override
    public View getGroupView(final boolean isExpanded, final View convertView, final ViewGroup parent) {
        View view;
        if (convertView != null && convertView.getId() == getViewWrapperId()) {
            view = convertView;
        } else {
            view = LayoutInflater.from(_context).inflate(R.layout.form_spinner, null);
        }

        TextView label = (TextView)view.findViewById(R.id.formLabel);
        label.setTypeface(getTypeface());
        label.setText(_label);

        Spinner spinner = (Spinner)view.findViewById(R.id.formSpinner);
        List<String> names = new ArrayList<>(_enumConstants.length + 1);
        names.add("");
        int selectedIndex = 0;
        for (E constant : _enumConstants) {
            if (constant == _selectedConstant) {
                selectedIndex = names.size();
            }
            names.add(_context.getString(constant.getName()));
        }
        SpinnerFieldAdapter adapter = new SpinnerFieldAdapter(names);
        spinner.setAdapter(adapter);
        spinner.setSelection(selectedIndex);
        spinner.setOnItemSelectedListener(this);

        return view;
    }

    public void onConstantChanged(final E constant) {

    }

    @Override
    public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
        if (position > 0) {
            E selectedConstant = _enumConstants[position - 1];
            if (selectedConstant != _selectedConstant) {
                onConstantChanged(selectedConstant);
            }

            _selectedConstant = selectedConstant;

        }
    }

    @Override
    public void onNothingSelected(final AdapterView<?> parent) {

    }

    private class SpinnerFieldAdapter extends ArrayAdapter<String> {
        private SpinnerFieldAdapter(List<String> names) {
            super(_context, android.R.layout.simple_spinner_item, names);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        @Override
        public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
            parent.setVerticalScrollBarEnabled(false);
            boolean isSpecialCaseView = (convertView != null && convertView.getTag() == this);

            if (position == 0) {
                if (isSpecialCaseView) {
                    return convertView;
                } else {
                    TextView tv = new TextView(_context);
                    tv.setHeight(0);
                    tv.setVisibility(View.GONE);
                    tv.setTag(this);
                    return tv;
                }
            } else {
                return super.getDropDownView(position, isSpecialCaseView ? null : convertView, parent);
            }
        }
    }

}
