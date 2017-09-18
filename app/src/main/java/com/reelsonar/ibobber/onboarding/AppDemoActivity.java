// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.onboarding;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.reelsonar.ibobber.BobberApp;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.databinding.FragmentAppDemoBinding;
import com.reelsonar.ibobber.model.Intro;
import com.reelsonar.ibobber.settings.ScreenSlidePageFragment;
import com.reelsonar.ibobber.util.Actions;
import com.reelsonar.ibobber.util.AppUtils;

import java.util.ArrayList;

public class AppDemoActivity extends AppCompatActivity {

    private final static String TAG = "AppDemoActivity";
    private static final int NUMBER_OF_DEMO_VIEWS = 13;

    public final static String INITIAL_DEMO_AFTER_REGISTER_KEY = "initialdemo";
    public final static int INITIAL_DEMO_IS_TRUE = 1;
    private boolean mIsInitialDemo = false;


    static private PageIndicator mPageIndicator;
    private ScreenSlidePagerAdapter mPagerAdapter;
    private ArrayList<Intro> introArray;
    private FragmentAppDemoBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_demo);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new AppDemoFragment())
                    .commit();
        }
        introArray = AppUtils.getIntroList(this);
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            if (bundle.getInt(INITIAL_DEMO_AFTER_REGISTER_KEY) == INITIAL_DEMO_IS_TRUE) {
                mIsInitialDemo = true;
            }
        }



    }


    public void onSkipButton(View v) {

        if (mIsInitialDemo == true) {
            Intent sonar = new Intent(Actions.SONAR_LIVE);
            sonar.addCategory(Actions.CATEGORY_INITIAL_DEMO);
            startActivity(sonar);
        }

        finish();
    }

    public void onNextButton(View v) {
        binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
    }

    public void onPrevButton(View v) {
        binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() - 1);
    }

    public class AppDemoFragment extends Fragment {

        public AppDemoFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            binding = FragmentAppDemoBinding.inflate(getLayoutInflater());

            mPageIndicator = new PageIndicator(getActivity());
            mPageIndicator.mCurrentPage = 0;
            binding.pageIndicatorLayout.addView(mPageIndicator);

            mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
            binding.viewPager.setAdapter(mPagerAdapter);
            binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    mPageIndicator.mCurrentPage = (introArray.get(position).getSelectedPos() - 1);
                    mPageIndicator.invalidate();
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            return binding.getRoot();
        }

    }

    public static class PageIndicator extends View {

        public int mTotalPages = 7;
        public int mCurrentPage = 0;

        private static int CURRENT_PAGE_COLOR = Color.WHITE;
        private static int OTHER_PAGE_COLOR = Color.GRAY;
        private static float DOT_RADIUS = 4 * BobberApp.getContext().getResources().getDisplayMetrics().density;

        public PageIndicator(Context context) {
            super(context);
        }

        Paint paint = new Paint();

        @Override
        public void onDraw(Canvas canvas) {

            int indicatorWidth = getWidth();
            int dotSpacing = indicatorWidth / mTotalPages;
            float indicatorStart = ((getWidth() / 2.0f) - (indicatorWidth / 2.0f)) + (DOT_RADIUS * 2.0f);

            for (int dots = 0; dots < mTotalPages; dots++) {
                paint.setColor(OTHER_PAGE_COLOR);
                if (dots == mCurrentPage) paint.setColor(CURRENT_PAGE_COLOR);
                canvas.drawCircle(indicatorStart + (dots * dotSpacing), getHeight() / 2, DOT_RADIUS, paint);
            }
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return new ScreenSlidePageFragment(position, introArray);
        }

        @Override
        public int getCount() {
            return NUMBER_OF_DEMO_VIEWS;
        }
    }
}


