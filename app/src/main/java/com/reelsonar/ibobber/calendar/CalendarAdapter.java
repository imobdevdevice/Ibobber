// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.calendar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.weather.WeatherService;

import java.text.SimpleDateFormat;
import java.util.*;


public class CalendarAdapter extends BaseAdapter {

    private static final String TAG = "CalendarAdapter";

    private Context mContext;
    public static List<String> mDayString;
    private Calendar mMonth;
    public GregorianCalendar mPrevMonth;

    public GregorianCalendar mPrevMonthMaxSet;
    private GregorianCalendar mSelectedDate;
    private int mFirstDay;
    private int mMaxWeekNumber;
    private int mMaxP;
    private int mCalMaxP;
    private int mCurDayPos = -1;
    private int mMonthLength;
    private String mItemValue;
    private String mCurDateString;
    private String mHeaderText;
    private SimpleDateFormat mDateFormat;
    private View mPrevView;

    private static int mNumOfPhases = 30;

    //---------------------------------------------------------------------------------------------
    // CalendarAdapter
    //
    //---------------------------------------------------------------------------------------------
    public CalendarAdapter(Context context, GregorianCalendar monthCalendar) {

        CalendarAdapter.mDayString = new ArrayList<String>();
        mMonth = monthCalendar;
        mSelectedDate = (GregorianCalendar) monthCalendar.clone();
        mContext = context;
        mMonth.set(GregorianCalendar.DAY_OF_MONTH, 1);
        Locale locale = mContext.getResources().getConfiguration().locale;
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd", locale);
        mCurDateString = mDateFormat.format(mSelectedDate.getTime());
        refreshDays();

    } /* End of CalendarAdapter */

    //---------------------------------------------------------------------------------------------
    // getCount
    //---------------------------------------------------------------------------------------------

    @Override
    public int getCount() {

        return mDayString.size();
    } /* End of getCount */

    //---------------------------------------------------------------------------------------------
    // getItem
    //---------------------------------------------------------------------------------------------

    @Override
    public Object getItem(int position) {

        return mDayString.get(position);
    } /* End of getItem */

    //---------------------------------------------------------------------------------------------
    // getItemId
    //---------------------------------------------------------------------------------------------

    @Override
    public long getItemId(int position) {
        return 0;
    } /* End of getItemId */

    //---------------------------------------------------------------------------------------------
    // getMonth
    //
    //  Returns a string that represents the current month and year the calendar is set to.
    //---------------------------------------------------------------------------------------------

    public String getMonth () {
        return mHeaderText;
    } /* End of getMonth */

    //---------------------------------------------------------------------------------------------
    // getCurDayPos
    //
    //  Returns the position index of the current day.  -1 is returned if the current day is not
    //  in the month being viewed
    //---------------------------------------------------------------------------------------------

    public int getCurDayPos () {
        return mCurDayPos;
    } /* End of getCurDayPos */



    public Date getDateForCell (int cellTapped)
    {

        GregorianCalendar dateTapped = (GregorianCalendar) mMonth.clone();
        dateTapped.set(GregorianCalendar.DAY_OF_MONTH, ((mCalMaxP + 1)- mMaxP) + cellTapped);

        return dateTapped.getTime();
    }


    //---------------------------------------------------------------------------------------------
    // getView
    //
    // Handles the presentation of the specific day by creating a view that has the day number
    // in the uppoer left-hand corner, and the moon phase in the bottom right.
    //
    // It draws boarders around the edges of each day.
    //
    // TODO: Calandars views showing 6 weeks need to be handled
    //---------------------------------------------------------------------------------------------

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        TextView dayView;
        ImageView phaseView;

        if (convertView == null) { // if it's not recycled, initialize some
            // attributes
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.calendar_item, null);

        }
        dayView = (TextView) v.findViewById(R.id.date);
        // separates daystring into parts.
        String[] separatedTime = mDayString.get(position).split("-");
        // taking last part of date. ie; 2 from 2012-12-02
        String gridvalue = separatedTime[2].replaceFirst("^0*", "");
        // checking whether the day is in current month or not.
        if ((Integer.parseInt(gridvalue) > 1) && (position < mFirstDay)) {
            // setting offdays to white color.
            dayView.setTextColor(Color.LTGRAY);
            dayView.setClickable(false);
            dayView.setFocusable(false);
        } else if ((Integer.parseInt(gridvalue) < 7) && (position > 28)) {
            dayView.setTextColor(Color.LTGRAY);
            dayView.setClickable(false);
            dayView.setFocusable(false);
        } else {
            dayView.setTextColor(Color.WHITE);
        }

        if (mDayString.get(position).equals(mCurDateString)) {
            v.setBackgroundResource(R.drawable.hover_tile);
            mCurDayPos = position;
            mPrevView = v;
        } else {
            v.setBackgroundColor(Color.TRANSPARENT);
            if (getCount() - position < 8) {
                v.setBackgroundResource(R.drawable.calendar_edge_right);
                //last day has no background - but needs frame forced to the same size
                if (position == getCount() - 1) {
                    Drawable transparentDrawable = new ColorDrawable(Color.TRANSPARENT);
                    transparentDrawable.setBounds(v.getBackground().getBounds());
                    Drawable clone = v.getBackground().mutate();
                    clone.setAlpha(0);
                    v.setBackground(clone);
                }
            } else {
                if ((position % 7) == 6) {
                    v.setBackgroundResource(R.drawable.calendar_edge_bottom_left);
                } else {
                    v.setBackgroundResource(R.drawable.calendar_edge_bottom_right);
                }
            }
        }

        dayView.setText(gridvalue);

        int year = Integer.valueOf(separatedTime[0]);
        int month = Integer.valueOf(separatedTime[1]);
        int day = Integer.valueOf(separatedTime[2]);

        String moonStr = moonPhaseImageForDate(day, month, year);
        phaseView = (ImageView) v.findViewById(R.id.phase);

        int resID = mContext.getResources().getIdentifier(moonStr, "drawable", mContext.getPackageName());
        if ( resID != 0 ) {
            phaseView.setImageResource(resID);
        }


        return v;
    }


    public void refreshDays() {

//        Log.i(TAG, "refreshDays");

        mCurDayPos = -1;
        mDayString.clear();

        mPrevMonth = (GregorianCalendar) mMonth.clone();
        // month start day. ie; sun, mon, etc
        mFirstDay = mMonth.get(GregorianCalendar.DAY_OF_WEEK);
        // finding number of weeks in current month.
        mMaxWeekNumber = mMonth.getActualMaximum(GregorianCalendar.WEEK_OF_MONTH);
        // allocating maximum row number for the gridview.
        mMonthLength = mMaxWeekNumber * 7;
        mMaxP = getMaxP(); // previous month maximum day 31,30....
        mCalMaxP = mMaxP - (mFirstDay - 1);// calendar offday starting 24,25 ...
        /**
         * Calendar instance for getting a complete gridview including the three
         * month's (previous,current,next) dates.
         */
        mPrevMonthMaxSet = (GregorianCalendar) mPrevMonth.clone();
        /**
         * setting the start date as previous month's required date.
         */
        mPrevMonthMaxSet.set(GregorianCalendar.DAY_OF_MONTH, mCalMaxP + 1);

        /**
         * filling calendar gridview.
         */
        for (int n = 0; n < mMonthLength; n++) {

            mItemValue = mDateFormat.format(mPrevMonthMaxSet.getTime());
            mPrevMonthMaxSet.add(GregorianCalendar.DATE, 1);
            mDayString.add(mItemValue);

        }

        Locale locale = mContext.getResources().getConfiguration().locale;
        mHeaderText = mMonth.getDisplayName(Calendar.MONTH, Calendar.LONG, locale) + " " + mMonth.get(Calendar.YEAR);
    }

    private int getMaxP() {
        int maxP;

        if ( mMonth.get(GregorianCalendar.MONTH) == Calendar.JANUARY ) {
            mPrevMonth.set(GregorianCalendar.MONTH, Calendar.DECEMBER);
            mPrevMonth.set(GregorianCalendar.YEAR, mMonth.get(GregorianCalendar.YEAR) - 1);

            Log.e(TAG, "Returning prior month / year of December");
        } else {
            mPrevMonth.set(GregorianCalendar.MONTH, mMonth.get(GregorianCalendar.MONTH) - 1);
        }

        maxP = mPrevMonth.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        return maxP;
    }


    private String moonPhaseImageForDate(int day, int month, int year) {

        double phase = WeatherService.moonPhaseForDate(day, month, year);
        Integer phaseValue = ((int) Math.floor(phase)) % mNumOfPhases;

        String moonStr = "moon" + phaseValue +"";

        return moonStr;

    }

}
