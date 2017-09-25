package com.reelsonar.ibobber.onboarding;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.databinding.FragmentThanksBinding;
import com.reelsonar.ibobber.util.Actions;


/**
 * Created by rujul on 18/9/17.
 */

public class ThanksFragment extends Fragment implements View.OnClickListener {
    private FragmentThanksBinding binding;
    public static String STATE = "state";
    public static String IS_REG = "is_reg";
    public static Integer START_STATE = 0;
    public static Integer FINISH_STATE = 1;
    public boolean IS_START = false;
    private Runnable runnable;
    private boolean isRegister;

    public static Fragment getInstance(int state, boolean isRegister) {
        Fragment thanksFragment = new ThanksFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(STATE, state);
        bundle.putBoolean(IS_REG, isRegister);
        thanksFragment.setArguments(bundle);
        return thanksFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Handler handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                setViews(false);
            }
        };

        binding = FragmentThanksBinding.inflate(inflater);
        if (getArguments() != null) {
            isRegister = getArguments().getBoolean(IS_REG);
            if ((getArguments().getInt(STATE)) == START_STATE) {
                setViews(true);
                handler.postDelayed(runnable, 3000);
                IS_START = true;
                binding.tvTitle.setText(getString(R.string.intro_start_conclusion));
                binding.tvDes.setText(getString(R.string.intro_start_salutation));
                binding.btnFinish.setText(getString(R.string.intro_start));

            } else if ((getArguments().getInt(STATE)) == FINISH_STATE) {

                setViews(false);
                IS_START = false;
                binding.tvTitle.setText(getString(R.string.intro_finish_conclusion));
                binding.tvDes.setText(getString(R.string.intro_finish_salutation));
                binding.btnFinish.setText(getString(R.string.intro_finish));

            }

            binding.btnFinish.setOnClickListener(this);
            binding.tvClose.setOnClickListener(this);

        }
        return binding.getRoot();
    }

    private void setViews(boolean isWelcome) {
        if (!isWelcome) {
            binding.welcomeLinear.setVisibility(View.GONE);
            binding.startLinear.setVisibility(View.VISIBLE);
            binding.tvClose.setVisibility(View.VISIBLE);
        } else {
            binding.welcomeLinear.setVisibility(View.VISIBLE);
            binding.startLinear.setVisibility(View.GONE);
            binding.tvClose.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_close:
                navigate();
                break;
            case R.id.btnFinish:
                navigate();
                break;
        }

    }

    private void navigate() {
        // registeration and start screen
        if (isRegister && IS_START) {
            getActivity().getFragmentManager().popBackStack();
        }
        // Registeration and finish screen
        else if (isRegister && !IS_START) {
            Intent sonar = new Intent(Actions.SONAR_LIVE);
            sonar.addCategory(Actions.CATEGORY_INITIAL_DEMO);
            startActivity(sonar);
            getActivity().finish();
        }
        // Comes from App Tour Button
        else {
            getActivity().finish();
        }
    }
}
