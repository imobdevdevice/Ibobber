// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import com.reelsonar.ibobber.settings.Language;
import com.reelsonar.ibobber.util.Style;

public class LanguageAdapter extends BaseAdapter implements RadioButton.OnCheckedChangeListener {

    private Language[] _languages = Language.values();
    private Typeface _typeface;
    private Context _context;
    private Language _selectedLanguage;

    public LanguageAdapter(final Context context, final Language selectedLanguage) {
        _context = context;
        _selectedLanguage = selectedLanguage;
        _typeface = Style.formTypeface(context);
    }

    public Language getSelectedLanguage() {
        return _selectedLanguage;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(_context);
            view = inflater.inflate(R.layout.form_radio, null);
        }

        RadioButton radioButton = (RadioButton)view.findViewById(R.id.formRadio);
        radioButton.setTypeface(_typeface);

        Language language = _languages[position];
        radioButton.setOnCheckedChangeListener(null);
        radioButton.setText(language.getName());
        radioButton.setTag(language);
        radioButton.setChecked(language == _selectedLanguage);
        radioButton.setOnCheckedChangeListener(this);

        return view;
    }

    @Override
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
        Language language = (Language)buttonView.getTag();
        if (isChecked && language != _selectedLanguage) {
            _selectedLanguage = language;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return _languages.length;
    }

    @Override
    public Object getItem(final int position) {
        return null;
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }
}
