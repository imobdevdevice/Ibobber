// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.form;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.reelsonar.ibobber.R;

public class SwitchField extends FormGroup implements View.OnClickListener {

    private Context _context;
    private String _label;
    private boolean _checked;
    private ToggleButton sw;

    public SwitchField(final Context context, final String label, final boolean checked) {
        _context = context;
        _label = label;
        _checked = checked;
    }

    public  SwitchField(final Context context, final int label, final boolean checked) {
        this(context, context.getString(label), checked);
    }

    @Override
    public int getViewWrapperId() {
        return R.id.formSwitchWrapper;
    }

    @Override
    public View getGroupView(final boolean isExpanded, final View convertView, final ViewGroup parent) {
        View view;
        if (convertView != null && convertView.getId() == getViewWrapperId()) {
            view = convertView;
        } else {
            view = LayoutInflater.from(_context).inflate(R.layout.form_switch, null);
        }

        TextView label = (TextView)view.findViewById(R.id.formLabel);
        label.setText(_label);
        label.setTypeface(getTypeface());

        sw = (ToggleButton)view.findViewById(R.id.formSwitch);

        sw.setChecked(_checked);
        sw.setOnClickListener(this);

        return view;
    }

    public void onValueChanged(final boolean checked) {
    }

    @Override
    public void onClick(final View v) {
        _checked = ((ToggleButton)v).isChecked();
        onValueChanged(_checked);
    }
}
