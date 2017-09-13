// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import com.reelsonar.ibobber.calendar.CalendarAdapter;
import com.reelsonar.ibobber.drawer.DeviceDrawerFragment;
import com.reelsonar.ibobber.drawer.HomeDrawerFragment;
import com.reelsonar.ibobber.model.triplog.TripLog;
import com.reelsonar.ibobber.triplog.TripLogDetailActivity;
import com.reelsonar.ibobber.triplog.TripLogService;
import com.reelsonar.ibobber.util.Actions;

import java.util.*;

public class CalendarActivity extends Activity implements LoaderManager.LoaderCallbacks<List<TripLog>> {

    private final static String TAG = "CalendarActivity";

    private static Date mLastDateTapped;
    private static View mFragmentView;
    public static GregorianCalendar mMonth;
    public GregorianCalendar mItemMonth;
    public static CalendarAdapter mCalendarAdapter;
    public ArrayList<String> mItems;            // We can remove this list
    public static CalendarFragment mCalendarFrag;

    public static GridView mCalendarGridView;

    public static Paint mPaint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mCalendarFrag = new CalendarFragment();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, mCalendarFrag)
                    .add(R.id.container, new DeviceDrawerFragment())
                    .add(R.id.container, new HomeDrawerFragment(), "HomeDrawer")
                    .commit();
        }

        mMonth = (GregorianCalendar) GregorianCalendar.getInstance();
        mItemMonth = (GregorianCalendar) mMonth.clone();
        mItems = new ArrayList<String>();

        mPaint = new Paint();

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(4);


    }

    @Override
    public void onResume() {

        Log.i(TAG, "onResume");

        super.onResume();

        mFragmentView.setVisibility(View.VISIBLE);

    }

    @Override
    public void onPause() {

        super.onPause();

        Log.i(TAG, "onPause");

    }

    public void hideCalendarView() {

        mFragmentView.setVisibility(View.INVISIBLE);
    }

    public void onNextClick(View v) {
        // Perform action on click
        Log.i(TAG, "next month button selected");
        mCalendarFrag.setNextMonth();
        mCalendarFrag.refreshCalendar();
    }

    public void onPrevClick(View v) {
        // Perform action on click
        Log.i(TAG, "previous month button selected");
        mCalendarFrag.setPreviousMonth();
        mCalendarFrag.refreshCalendar();
    }


    public static class CalendarFragment extends Fragment {

        public CalendarFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);

            mFragmentView = rootView;

            mCalendarGridView = (GridView) mFragmentView.findViewById(R.id.calendarGridView);
            if (mCalendarGridView != null) {
                mCalendarAdapter = new CalendarAdapter(this.getActivity(), mMonth);
                if (mCalendarAdapter != null) {
                    mCalendarGridView.setAdapter(mCalendarAdapter);
                }

                // Update the header to show the current month and year

                TextView monthTitle = (TextView) mFragmentView.findViewById(R.id.monthYearTextView);
                if (monthTitle != null) {
                    monthTitle.setText(mCalendarAdapter.getMonth());
                }

                // Set the current day button click handler
                mCalendarGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {

                        if (mCalendarAdapter.getCurDayPos() == pos) {
                            Log.d(TAG, "Current day was selected");
                            // Launch current day activity

                            ((CalendarActivity) getActivity()).hideCalendarView();

                            Intent intent = new Intent(getActivity(), TodayActivity.class);
                            startActivity(intent);
                        } else {
                            Date dateForCell = mCalendarAdapter.getDateForCell(pos);
                            ((CalendarActivity)getActivity()).requestTripLogsForDate(dateForCell.getTime());
                            mLastDateTapped = dateForCell;

                        }
                    }
                });

            }

            return rootView;
        }

        protected void setNextMonth() {

            if (mMonth.get(GregorianCalendar.MONTH) == mMonth.getActualMaximum(GregorianCalendar.MONTH)) {
                mMonth.set((mMonth.get(GregorianCalendar.YEAR) + 1),
                        Calendar.JANUARY, 1);
            } else {
                mMonth.set(GregorianCalendar.MONTH, mMonth.get(GregorianCalendar.MONTH) + 1);
            }

        }

        protected void setPreviousMonth() {

            if (mMonth.get(GregorianCalendar.MONTH) == mMonth.getActualMinimum(GregorianCalendar.MONTH)) {
                mMonth.set((mMonth.get(GregorianCalendar.YEAR) - 1),
                        Calendar.DECEMBER, 1);
            } else {
                mMonth.set(GregorianCalendar.MONTH, mMonth.get(GregorianCalendar.MONTH) - 1);
            }

        }

        public void refreshCalendar() {

            Log.i(TAG, "refreshCalendar");

            if (mCalendarAdapter != null) {
                mCalendarAdapter.refreshDays();
                mCalendarAdapter.notifyDataSetChanged();
            }

            TextView monthTitle = (TextView) mFragmentView.findViewById(R.id.monthYearTextView);
            if (monthTitle != null) {
                monthTitle.setText(mCalendarAdapter.getMonth());
            }

        }

    }

    public void requestTripLogsForDate(long date) {

        Bundle bundle = new Bundle();
        bundle.putLong("date", date);
        getLoaderManager().restartLoader(0, bundle, this);

    }

    @Override
    public Loader onCreateLoader(final int id, final Bundle args) {
        return TripLogService.getInstance(this).tripLogsForDate(args.getLong("date"));
    }

    @Override
    public void onLoadFinished(Loader<List<TripLog>> listLoader, List<TripLog> tripLogs) {

        Log.i(TAG, "logs for date:" + tripLogs);

        if (tripLogs.size() > 1) {
            Intent intent = new Intent(Actions.TRIPLOG);
            intent.putExtra("dateToHighlight", mLastDateTapped);
            startActivity(intent);
        }

        if (tripLogs.size() == 1) {

            Intent tripDetail = new Intent(this, TripLogDetailActivity.class);
            tripDetail.putExtra("idTrip",tripLogs.get(0).getIdTrip());
            startActivity(tripDetail);
        }

        getLoaderManager().destroyLoader(0);

    }

    @Override
    public void onLoaderReset(Loader<List<TripLog>> listLoader) {

    }


}
