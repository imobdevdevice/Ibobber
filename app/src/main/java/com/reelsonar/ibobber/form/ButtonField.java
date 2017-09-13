// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.form;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.reelsonar.ibobber.R;

public class ButtonField extends FormGroup {

    private Context _context;
    private String _label;
    private String _secondaryLabel;
    private boolean _showButton;
    private View.OnClickListener _clickListener;

    public ButtonField(final Context context, final String label, final String secondaryLabel, final boolean showButton, final View.OnClickListener clickListener) {
        _context = context;
        _label = label;
        _secondaryLabel = secondaryLabel;
        _showButton = showButton;
        _clickListener = clickListener;
    }


    @Override
    public int getViewWrapperId() {
        return R.id.formButtonWrapper;
    }

    @Override
    public View getGroupView(final boolean isExpanded, final View convertView, final ViewGroup parent) {
        View view;
        if (convertView != null && convertView.getId() == getViewWrapperId()) {
            view = convertView;
        } else {
            view = LayoutInflater.from(_context).inflate(R.layout.form_button, null);
        }

        TextView label = (TextView)view.findViewById(R.id.formLabel);
        label.setText(_label);
        label.setTypeface(getTypeface());

        Button button = (Button)view.findViewById(R.id.formButton);

        TextView secondaryLabel = (TextView)view.findViewById(R.id.secondaryLabel);

        if (_showButton) {
            button.setVisibility(View.VISIBLE);
            secondaryLabel.setVisibility(View.GONE);
            button.setText(_secondaryLabel);
            button.setOnClickListener(_clickListener);
        } else {
            button.setVisibility(View.GONE);
            secondaryLabel.setVisibility(View.VISIBLE);
            secondaryLabel.setText(_secondaryLabel);
        }


        return view;
    }

}
