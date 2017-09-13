// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.form;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.reelsonar.ibobber.R;

public class TextField extends FormGroup {

    private Context _context;
    private String _label;
    private String _value;
    private String _placeholder;
    private boolean _editable;
    private boolean _showDisclosure;
    private boolean _disableInputSuggestions;

    public TextField(final Context context, final String label, final String value, final String placeholder, final boolean editable) {
        this(context, label, value, placeholder, editable, false, false);
    }

    public TextField(final Context context, final int label, final String value, final int placeholder, final boolean editable) {
        this(context, label, value, placeholder, editable, false);
    }

    public TextField(final Context context, final int label, final String value, final int placeholder, final boolean editable, final boolean showDisclosure) {
        this(context, context.getString(label), value, placeholder != 0 ? context.getString(placeholder) : null, editable, showDisclosure, false);
    }

    public TextField(final Context context, final int label, final String value, final int placeholder, final boolean editable, final boolean showDisclosure, final boolean disableInputSuggestions) {
        this(context, context.getString(label), value, placeholder != 0 ? context.getString(placeholder) : null, editable, showDisclosure, disableInputSuggestions);
    }

    public TextField(final Context context, final String label, final String value, final String placeholder, final boolean editable, final boolean showDisclosure, final boolean disableInputSuggestions) {
        _context = context;
        _label = label;
        _value = value;
        _placeholder = placeholder;
        _editable = editable;
        _showDisclosure = showDisclosure;
        _disableInputSuggestions = disableInputSuggestions;
    }

    @Override
    public int getViewWrapperId() {
        return R.id.formFieldWrapper;
    }

    @Override
    public View getGroupView(boolean isExpanded, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null && convertView.getId() == getViewWrapperId()) {
            view = convertView;
        } else {
            view = LayoutInflater.from(_context).inflate(R.layout.form_field, null);
        }

        TextView label = (TextView) view.findViewById(R.id.formLabel);
        TextView field = (TextView) view.findViewById(R.id.formField);
        ImageView disclosure = (ImageView) view.findViewById(R.id.formDisclosure);

        label.setText(_label);
        label.setTypeface(getTypeface());

        field.setText(_value);
        field.setHint(_placeholder);
        field.setTypeface(getTypeface());

        disclosure.setVisibility(_showDisclosure ? View.VISIBLE : View.GONE);
        disclosure.setRotation(0.f);

        return view;
    }

    public void onValueChange(final String value) {

    }

    @Override
    public void onGroupClick(final View view, final boolean isExpanded) {
        if (_editable) {
            AlertDialog.Builder builder = new AlertDialog.Builder(_context, AlertDialog.THEME_HOLO_LIGHT);

            final EditText field = new EditText(_context);
            field.setSingleLine(true);
            field.setText(_value);
            field.setHint(_placeholder);
            float scale = _context.getResources().getDisplayMetrics().density;
            field.setWidth((int) (250 * scale + 0.5f));

            if (_disableInputSuggestions)
                field.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

            builder.setTitle(_label);
            builder.setView(field);
            builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    _value = field.getText().toString();
                    TextView valueField = (TextView) view.findViewById(R.id.formField);
                    valueField.setText(_value);
                    onValueChange(_value);
                }
            });

            builder.setNegativeButton(R.string.button_cancel, null);

            final AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                    InputMethodManager imm = (InputMethodManager) _context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(field, 0);
                }
            });
            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(final DialogInterface dialog) {
                    InputMethodManager imm = (InputMethodManager) _context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(((Activity) _context).getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            });

            field.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        _value = field.getText().toString();
                        TextView valueField = (TextView) view.findViewById(R.id.formField);
                        valueField.setText(_value);
                        onValueChange(_value);
                        alertDialog.dismiss();
                    }
                    return false;
                }
            });


            alertDialog.show();
        }
    }

    @Override
    public void onChildClick(View view, final int position) {

    }

    @Override
    public int getChildrenCount() {
        return 0;
    }

}
