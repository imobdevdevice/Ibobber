// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.form;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.reelsonar.ibobber.R;

public class LabelField extends FormGroup {

    private Context _context;
    private String _label;
    private String _value;
    private boolean _showDisclosure;

    public LabelField(final Context context, final String label, final String value, final boolean showDisclosure) {
        _context = context;
        _label = label;
        _value = value;
        _showDisclosure = showDisclosure;
    }

    public LabelField(final Context context, final int label, final String value, final boolean showDisclosure) {
        this(context, context.getString(label), value, showDisclosure);
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

        label.setText(_label);  
        label.setTypeface(getTypeface());

        if (_value != null) {
            field.setVisibility(View.VISIBLE);
            field.setText(_value);
            field.setTypeface(getTypeface());
        } else {
            field.setVisibility(View.GONE);
        }

        disclosure.setVisibility(_showDisclosure ? View.VISIBLE : View.GONE);
        disclosure.setRotation(0.f);

        return view;
    }
}
