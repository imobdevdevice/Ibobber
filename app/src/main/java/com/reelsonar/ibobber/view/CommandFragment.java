// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.view;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;
import com.reelsonar.ibobber.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.reelsonar.ibobber.view.CommandFragment.CommandFragmentListener} interface
 * to handle interaction events.
 * Use the {@link CommandFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CommandFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_TITLE = "title";
    private static final String ARG_SUBTITLE = "subitle";
    private static final String ARG_SHOW_CANCEL = "showCancel";

    private CommandFragmentListener _listener;

    public static CommandFragment newInstance(String title,
                                              String subtitle,
                                              boolean showCancel) {
        CommandFragment fragment = new CommandFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_SUBTITLE, subtitle);
        args.putBoolean(ARG_SHOW_CANCEL, showCancel);
        fragment.setArguments(args);
        return fragment;
    }
    public CommandFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_command, container, false);

        Button ok = (Button) rootView.findViewById(R.id.commandOK);
        Button cancel = (Button) rootView.findViewById(R.id.commandCancel);

        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);

        if (getArguments() != null) {
            String title = getArguments().getString(ARG_TITLE);
            String subtitle = getArguments().getString(ARG_SUBTITLE);
            boolean showCancel = getArguments().getBoolean(ARG_SHOW_CANCEL);

            ((TextView)rootView.findViewById(R.id.commandTitle)).setText(title);
            ((TextView)rootView.findViewById(R.id.commandSubtitle)).setText(subtitle);
            if (!showCancel) {
                cancel.setVisibility(View.GONE);
            }
        }

        return rootView;
    }

    @Override
    public void onClick(View view) {
        if (_listener != null) {
            if (view.getId() == R.id.commandOK) {
                _listener.onOK(this);
            } else {
                _listener.onCancel(this);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof CommandFragmentListener) {
            _listener = (CommandFragmentListener) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _listener = null;
    }

    public interface CommandFragmentListener {
        void onOK(CommandFragment fragment);
        void onCancel(CommandFragment fragment);
    }

}
