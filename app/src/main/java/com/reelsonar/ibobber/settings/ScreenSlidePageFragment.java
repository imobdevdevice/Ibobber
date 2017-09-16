package com.reelsonar.ibobber.settings;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.reelsonar.ibobber.databinding.FragmentScreenSlidePageBinding;
import com.reelsonar.ibobber.model.Intro;

import java.util.ArrayList;

/**
 * Created by rujul on 15/9/17.
 */

public class ScreenSlidePageFragment extends Fragment {

    private int currentPos;
    private ArrayList<Intro> introArray;
    FragmentScreenSlidePageBinding binding;

    public ScreenSlidePageFragment(int position, ArrayList<Intro> introArray) {
        currentPos = position;
        this.introArray = introArray;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentScreenSlidePageBinding.inflate(inflater, container, false);
        binding.setIntroModel(introArray.get(currentPos));
        return binding.getRoot();
    }

}
