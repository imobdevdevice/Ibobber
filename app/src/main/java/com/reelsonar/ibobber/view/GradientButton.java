package com.reelsonar.ibobber.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.StateSet;
import android.widget.TextView;

import com.reelsonar.ibobber.R;

public class GradientButton extends TextView {
  private int endColor;
  private int startColor;
  private int backColor;
  private boolean isGradient;
  private float radiusButton;

  public GradientButton(Context context) {
    super(context);
    if (!isInEditMode()) {
      init(null);
    }
  }

  public GradientButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    if (!isInEditMode()) {
      init(attrs);
    }
  }

  public GradientButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    if (!isInEditMode()) {
      init(attrs);
    }
  }

  protected void init(AttributeSet attrs) {
    if (attrs != null) {
      TypedArray a = getResources().obtainAttributes(attrs, R.styleable.GradientButton);
      backColor = a.getColor(R.styleable.GradientButton_backColor,
          ContextCompat.getColor(getContext(), R.color.loginEditTxtColor));
      startColor = a.getColor(R.styleable.GradientButton_startColorGradient,
          ContextCompat.getColor(getContext(), R.color.loginEditTxtColor));
      endColor = a.getColor(R.styleable.GradientButton_endColorGradient,
          ContextCompat.getColor(getContext(), R.color.loginEditTxtColor));
      isGradient = a.getBoolean(R.styleable.GradientButton_isGradient, false);
      radiusButton = a.getDimension(R.styleable.GradientButton_buttonRadius,
          getResources().getDimension(R.dimen._5sdp));
      Drawable drawable = a.getDrawable(R.styleable.GradientButton_setDrawable);
      if (drawable != null) {
        setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
      }
      a.recycle();
    }

    showSelector();
  }

  private void showSelector() {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      //For Ripple Animation
      int[] attrs = new int[] {android.R.attr.selectableItemBackground};
      TypedArray ta = getContext().obtainStyledAttributes(attrs);
      Drawable rippleDrawable = ta.getDrawable(0);
      ta.recycle();
      setForeground(rippleDrawable);
    }

    GradientDrawable btn_normal;
    GradientDrawable btn_pressed;

    if (isGradient) {
      btn_normal = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
          new int[] {startColor, endColor});
      btn_pressed = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
          new int[] {startColor, endColor});
    } else {
      btn_normal = new GradientDrawable();
      btn_normal.setColor(backColor);
      btn_pressed = new GradientDrawable();
      btn_pressed.setColor(backColor);
    }

    btn_normal.setCornerRadius(radiusButton);
    btn_normal.setAlpha(255);

    btn_pressed.setCornerRadius(radiusButton);
    btn_pressed.setAlpha(210);

    int grey = ContextCompat.getColor(getContext(), R.color.colorGrey);

    GradientDrawable disable =
        new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[] {grey, grey});
    disable.setCornerRadius(radiusButton);

    StateListDrawable drawable = new StateListDrawable();

    drawable.addState(new int[] {android.R.attr.state_pressed}, btn_pressed);
    drawable.addState(new int[] {-android.R.attr.state_enabled}, disable);
    drawable.addState(StateSet.WILD_CARD, btn_normal);

    if (Build.VERSION.SDK_INT < 16) {
      setBackgroundDrawable(drawable);
    } else {
      setBackground(drawable);
    }
  }
}
