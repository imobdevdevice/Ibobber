package com.reelsonar.ibobber.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.reelsonar.ibobber.form.FormGroup;

/**
 * Created by Rujul Gandhi on 4/10/17.
 */

public class EditTextField extends FormGroup {

    private Context _context;
    private String _label;
    private String _value;
    private boolean _showDisclosure;

    public EditTextField(final Context context, final String label, final String value, final boolean showDisclosure) {
        _context = context;
        _label = label;
        _value = value;
        _showDisclosure = showDisclosure;
    }

    public EditTextField(final Context context, final int label, final String value, final boolean showDisclosure) {
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

        TextView label = (TextView) view.findViewById(R.id.formLabel);
        TextView field = (TextView) view.findViewById(R.id.formField);
        ImageView disclosure = (ImageView) view.findViewById(R.id.formDisclosure);

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

    @Override
    public void onGroupClick(final View view, boolean isExpanded) {
        super.onGroupClick(view, isExpanded);
        AlertDialog.Builder builder = new AlertDialog.Builder(_context, AlertDialog.THEME_HOLO_LIGHT);

        final EditText field = new EditText(_context);
        field.setSingleLine(true);
        field.setText(_value);
        field.setHint("");
        float scale = _context.getResources().getDisplayMetrics().density;
        field.setWidth((int) (250 * scale + 0.5f));

//        if (_disableInputSuggestions)
//            field.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

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

    @Override
    public void onChildClick(View view, final int position) {

    }

    @Override
    public int getChildrenCount() {
        return 0;
    }

    public void onValueChange(final String value) {

    }
}
