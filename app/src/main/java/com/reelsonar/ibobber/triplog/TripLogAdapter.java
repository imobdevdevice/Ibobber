// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.triplog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.model.triplog.TripLog;
import com.reelsonar.ibobber.util.RestConstants;
import com.reelsonar.ibobber.util.Style;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripLogAdapter extends BaseAdapter {

    protected Activity activity;
    private List<TripLog> tripLogs;
    private SimpleDateFormat dateFormat;
//    public TripLogAdapter(Activity activity, List<TripLog> items, Integer itemToHighlight) {
//        super();
//        this.activity = activity;
//        this.items = items;
//        _dateFormat = android.text.format.DateFormat.getDateFormat(activity);
//        _itemToHighlight = itemToHighlight;
//    }

    public TripLogAdapter(Activity activity, List<TripLog> tripLogs) {
        super();
        dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        this.activity = activity;
        this.tripLogs = tripLogs;
    }

    @Override
    public int getCount() {
        return tripLogs.size();
    }

    @Override
    public TripLog getItem(int position) {
        return tripLogs.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.list_triplog, null);
        }

        final TripLog tripLog = tripLogs.get(position);
        Typeface tf = Style.formTypeface(activity);
        final TextView txtItemTrip = (TextView) vi.findViewById(R.id.txtItemTrip);
        final ImageView tripImage = (ImageView) vi.findViewById(R.id.tripImage);

        if (tripLog.getTitle().equalsIgnoreCase(RestConstants.NETFISH_CATCH_TITLE)) {
            tripImage.setImageResource(R.drawable.netfish_catch_icon);
        } else {
            tripImage.setImageResource(R.drawable.arrow_right);
        }

//        String untitledLog = BobberApp.getContext().getResources().getString(R.string.trip_log_untitled);
//        String title = (tripLog.getAdTitle() != null && !tripLog.getAdTitle().isEmpty() ? tripLog.getAdTitle() : untitledLog);
        String itemTrip;
        if (tripLog.getDate() != null)
            itemTrip = getDate(tripLog.getDate()) + " &#8226; " + tripLog.getTitle();
        else
            itemTrip = "-- &#8226; " + tripLog.getTitle();

        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            txtItemTrip.setText(Html.fromHtml(itemTrip, Html.FROM_HTML_MODE_COMPACT));
        } else {
            txtItemTrip.setText(Html.fromHtml(itemTrip));
        }
        txtItemTrip.setTypeface(tf);

        return vi;
    }


    public void removeItem(final int position) {
        tripLogs.remove(position);
        notifyDataSetChanged();
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("MM/dd/yyyy", cal).toString();
        return date;
    }

    private String getDate(Date date) {
        String dateStr = DateFormat.format("MM/dd/yyyy", date).toString();
        return dateStr;
    }

    public void editTripLog(final int position) {
        TripLog tripLog = tripLogs.get(position);
        long idTrip = tripLog.getNetFishId();
        Intent tripDetail = new Intent(activity, TripLogDetailActivity.class);
        tripDetail.putExtra("idTrip", idTrip);
        activity.startActivity(tripDetail);
    }
}
