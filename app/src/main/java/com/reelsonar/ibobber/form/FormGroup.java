// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.form;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;

public abstract class FormGroup {

    private Typeface _typeface;

    public Typeface getTypeface() {
        return _typeface;
    }

    public void setTypeface(final Typeface typeface) {
        _typeface = typeface;
    }

    public void onGroupClick(View view, final boolean isExpanded) {

    }

    public void onChildClick(View view, final int position) {

    }

    public int getChildrenCount() {
        return 0;
    }

    public View getChildView(int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return null;
    }

    public abstract View getGroupView(boolean isExpanded, View convertView, ViewGroup parent);

    public abstract int getViewWrapperId();

}
