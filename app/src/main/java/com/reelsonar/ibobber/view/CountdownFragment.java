package com.reelsonar.ibobber.view;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import com.reelsonar.ibobber.R;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.reelsonar.ibobber.view.CountdownFragment.CountdownListener} interface
 * to handle interaction events.
 *
 */
public class CountdownFragment extends Fragment implements Runnable {

    private CountdownListener _listener;
    private Handler _handler;
    private int _count = 3;

    public CountdownFragment() {
        _handler = new Handler(Looper.myLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_countdown, container, false);

        ((TextView)view.findViewById(R.id.countdownLabel)).setText(String.valueOf(_count));

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            _listener = (CountdownListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stop();
        _listener = null;
    }

    public void start() {
        _handler.postDelayed(this, 1000);
    }

    public void stop() {
        _handler.removeCallbacks(this);
    }

    @Override
    public void run() {
        --_count;
        if (_count > 0) {
            ((TextView)getView().findViewById(R.id.countdownLabel)).setText(String.valueOf(_count));
            _handler.postDelayed(this, 1000);
        } else {
            _listener.onCountdownComplete(this);
        }
    }

    public interface CountdownListener {
        public void onCountdownComplete(CountdownFragment fragment);
    }

}
