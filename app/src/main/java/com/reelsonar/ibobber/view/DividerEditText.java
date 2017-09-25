package com.reelsonar.ibobber.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import com.reelsonar.ibobber.R;

import static com.reelsonar.ibobber.R.id.editText;


/**
 * Created by krupal on 1/9/16.
 */

public class DividerEditText extends EditText {
    private DividerViewController mDividerViewController;

    public DividerEditText(Context context) {
        super(context);
        init(null);
    }

    public DividerEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DividerEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    private void init(AttributeSet attrs) {
        mDividerViewController = new DividerViewController();
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DividerTextView);
            mDividerViewController.init(a);
            a.recycle();
        }
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        mDividerViewController.onFocusChange(focused, this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mDividerViewController != null)
            mDividerViewController.onMeasure(getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDividerViewController != null) {
            mDividerViewController.onDraw(canvas);
        }
    }
//    @Override
//    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
//            clearFocus();
//            return false;
//        }
//        return false;
//    }
//    public void revalidateEditText() {
//        // Dismiss your origial error dialog
//        setError(null);
//        // figure out which EditText it is, you already have this code
//        // call your validator like in the Q
////        validate(editText); // or whatever your equivalent is
//    }
}
