// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.form;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import com.reelsonar.ibobber.R;

public class NotesField extends FormGroup {

    private Context _context;
    private String _value;
    private String _placeholder;

    public NotesField(final Context context, final String value, final int placeholder) {
        this(context, value, context.getString(placeholder));
    }

    public NotesField(final Context context, final String value, final String placeholder) {
        _context = context;
        _value = value;
        _placeholder = placeholder;
    }

    @Override
    public int getViewWrapperId() {
        return R.id.formNotesWrapper;
    }

    @Override
    public View getGroupView(final boolean isExpanded, final View convertView, final ViewGroup parent) {
        View view;
        if (convertView != null && convertView.getId() == getViewWrapperId()) {
            view = convertView;
        } else {
            view = LayoutInflater.from(_context).inflate(R.layout.form_notes, null);
        }

        TextView label = (TextView)view.findViewById(R.id.formLabel);
        label.setTypeface(getTypeface());
        label.setHint(_placeholder);
        label.setText(_value);

        return view;
    }

    public void onValueChange(final String value) {

    }

    @Override
    public void onGroupClick(final View view, final boolean isExpanded) {
        AlertDialog.Builder builder = new AlertDialog.Builder(_context, AlertDialog.THEME_HOLO_LIGHT);

        final EditText field = new EditText(_context);
        field.setText(_value);
        field.setMinLines(5);
        field.setGravity(Gravity.TOP);
        float scale = _context.getResources().getDisplayMetrics().density;
        field.setWidth((int)(250 * scale + 0.5f));

        builder.setTitle(_placeholder);
        builder.setView(field);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                _value = field.getText().toString();

                TextView valueField = (TextView)view.findViewById(R.id.formLabel);
                valueField.setText(_value);

                onValueChange(_value);
            }
        });
        builder.setNegativeButton(R.string.button_cancel, null);

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager)_context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(field, 0);
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
