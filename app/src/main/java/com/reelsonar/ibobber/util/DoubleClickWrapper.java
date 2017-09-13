package com.reelsonar.ibobber.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.ToggleButton;

/**
 * Created by james on 9/15/14.
 */
public class DoubleClickWrapper implements View.OnClickListener, Runnable {

    public interface DoubleClickListener extends View.OnClickListener {
        void onDoubleClick(View view);
    }

    public static DoubleClickWrapper wrap(final View view, final DoubleClickListener listener) {
        DoubleClickWrapper wrapper = new DoubleClickWrapper(view, listener);
        view.setOnClickListener(wrapper);
        return wrapper;
    }

    private static final long DELAY = 250;

    private View _view;
    private DoubleClickListener _listener;
    private Handler _handler;
    private long _lastClickTime;

    public DoubleClickWrapper(final View view, final DoubleClickListener listener) {
        _view = view;
        _listener = listener;
        _handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onClick(View view) {
        if (_lastClickTime > 0 && SystemClock.uptimeMillis() - _lastClickTime < DELAY) {
            _lastClickTime = 0;
            _listener.onDoubleClick(view);
            _handler.removeCallbacks(this);
        } else {
            _lastClickTime = SystemClock.uptimeMillis();
            _handler.postDelayed(this, DELAY);
        }

        if (view instanceof ToggleButton) {
            ToggleButton toggleButton = (ToggleButton)view;
            toggleButton.setChecked(!toggleButton.isChecked());
        }
    }

    @Override
    public void run() {
        _lastClickTime = 0;
        if (_view instanceof ToggleButton) {
            ToggleButton toggleButton = (ToggleButton)_view;
            toggleButton.setChecked(!toggleButton.isChecked());
        }
        _listener.onClick(_view);
    }
}
