// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.triplog.form;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.form.*;
import com.reelsonar.ibobber.model.FavoriteFish;
import com.reelsonar.ibobber.model.triplog.*;
import com.reelsonar.ibobber.service.UserService;
import com.reelsonar.ibobber.triplog.TripLogDetailActivity;
import com.reelsonar.ibobber.triplog.TripLogMapActivity;
import com.reelsonar.ibobber.util.MathUtil;
import com.reelsonar.ibobber.util.Style;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class TripLogFormAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {

    private Context _activity;
    private TripLog _tripLog;
    private TripLogImages _tripLogImages;

    private TripLogFish _tripLogFish;
    private List<FavoriteFish> _allFish;
    private FormGroup[] _formGroups;
    private List<Integer> _formGroupViewIds;

    public TripLogFormAdapter(final Activity activity, final TripLog tripLog,  final TripLogImages tripLogImages, final TripLogFish tripLogFish, final List<FavoriteFish> allFish) {
        _activity = activity;
        _tripLog = tripLog;
        if( tripLogImages != null )
            _tripLogImages = tripLogImages;
        else
            _tripLogImages = new TripLogImages();
        _tripLogFish = tripLogFish;
        _allFish = allFish;        

        generateFormGroups();
    }

    @Override
    public void notifyDataSetChanged() {
        generateFormGroups();
        super.notifyDataSetChanged();
    }

    View.OnClickListener photoShareClick = new View.OnClickListener() {
        public void onClick(View view) {
            Log.d("iBobber", "Photo Share");
            ((TripLogDetailActivity ) _activity).selectImageSource(view);
        }
    };

    View.OnClickListener imageDisplayClick = new View.OnClickListener() {
        public void onClick(View view) {
            int imageId = (int) view.getTag();
            if( imageId < 0 )
                return;

            String imageToDisplay = _tripLogImages.getImageByIndex(imageId);
            ((TripLogDetailActivity ) _activity).displayImage( imageId, imageToDisplay );
        }
    };

    private void generateFormGroups() {
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(_activity);
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(_activity);

        if( _tripLog.getWaterDepth() == 0 )
            _tripLog.setWaterDepth( 9999 );

        String waterTempString =  String.format("%.0f °C", _tripLog.getWaterTemp() );
        String depthString = String.format("%.2f meters", _tripLog.getWaterDepth() );
        String airTempString =  String.format("%.0f °C", _tripLog.getAirTemp() );

        if( ! UserService.getInstance(_activity).isMetric() ) {
            waterTempString = String.format("%.1f °F", ((_tripLog.getWaterTemp() * 9.0) / 5.0) + 32 );
            depthString = String.format("%.2f feet", _tripLog.getWaterDepth() * MathUtil.FEET_PER_METER );
            airTempString = String.format("%.1f °F", ((_tripLog.getAirTemp() * 9.0) / 5.0) + 32 );
        }

        final boolean hasLocation = _tripLog.getLatitude() != 0 || _tripLog.getLongitude() != 0;

        Drawable cameraIcon = _activity.getResources().getDrawable(R.drawable.icon_camera);

        _formGroups = new FormGroup[]{
                new TextField(_activity, R.string.trip_log_date, dateFormat.format(_tripLog.getDate()), 0, false),
                new TextField(_activity, R.string.trip_log_time, timeFormat.format(_tripLog.getDate()), 0, false),
                new TextField(_activity, R.string.trip_log_location, hasLocation ? String.format("%.6f", _tripLog.getLatitude())
                        + "," + String.format("%.6f", _tripLog.getLongitude()) : _activity.getResources().getString(R.string.trip_log_unknown_location), 0, false, hasLocation) {
                    @Override
                    public void onGroupClick(final View view, final boolean isExpanded) {
                        if (hasLocation) {
                            Intent intent = new Intent(_activity, TripLogMapActivity.class);
                            intent.putExtra("latitude", _tripLog.getLatitude());
                            intent.putExtra("longitude", _tripLog.getLongitude());
                            _activity.startActivity(intent);
                        }
                    }
                },

                new ImageGroupField(_activity,
                        R.string.trip_log_photos,
                        _tripLogImages.getImageFilenameList(),
                        cameraIcon,
                        true, photoShareClick, imageDisplayClick ),

                new TextField(_activity,  R.string.trip_log_water_temp, (_tripLog.getWaterTemp() == 9999) ? "-" : waterTempString, 0, false),

                new TextField(_activity,  R.string.trip_log_water_depth, (_tripLog.getWaterDepth() == 9999) ? "-" : depthString, 0, false),

                new TextField(_activity,  R.string.trip_log_air_temp, (_tripLog.getAirTemp() == 9999) ? "-" : airTempString , 0, false),

                new TextField(_activity, R.string.trip_log_title, _tripLog.getTitle(), R.string.trip_log_title_ph, true) {
                    @Override
                    public void onValueChange(final String value) {
                        _tripLog.setTitle(value);
                    }
                },
                 new SpinnerOtherField<LureType>(_activity, R.string.trip_log_lure, LureType.class, _tripLog.getLureType()) {
                    @Override
                    public void onConstantChanged(final LureType lureType) {
                        _tripLog.setLureType(lureType);
                    }
                },
                new SpinnerField<Condition>(_activity, R.string.trip_log_conditions, Condition.class, _tripLog.getCondition()) {
                    @Override
                    public void onConstantChanged(final Condition condition) {
                        _tripLog.setCondition(condition);
                    }
                },
                new SpinnerField<FishingType>(_activity, R.string.trip_log_type_of_fishing, FishingType.class, _tripLog.getFishingType()) {
                    @Override
                    public void onConstantChanged(final FishingType fishingType) {
                        _tripLog.setFishingType(fishingType);
                    }
                },
                new FishFormGroup(_activity, _tripLogFish, _allFish),
                new NotesField(_activity, _tripLog.getNotes(), R.string.trip_log_notes_ph) {
                    @Override
                    public void onValueChange(final String value) {
                        _tripLog.setNotes(value);
                    }
                }
        };

        _formGroupViewIds = new ArrayList<>(4);
        Typeface tf = Style.formTypeface(_activity);
        for (FormGroup formGroup : _formGroups) {
            formGroup.setTypeface(tf);

            if (!_formGroupViewIds.contains(formGroup.getViewWrapperId())) {
                _formGroupViewIds.add(formGroup.getViewWrapperId());
            }
        }
    }

    @Override
    public int getGroupCount() {
        return _formGroups.length;
    }

    @Override
    public int getChildrenCount(final int groupPosition) {
        return _formGroups[groupPosition].getChildrenCount();
    }

    @Override
    public long getGroupId(final int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(final int groupPosition, final int childPosition) {
        return childPosition;
    }

    @Override
    public int getGroupTypeCount() {
        return _formGroupViewIds.size();
    }

    @Override
    public int getGroupType(final int groupPosition) {
        return _formGroupViewIds.indexOf(_formGroups[groupPosition].getViewWrapperId());
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, final View convertView, final ViewGroup parent) {
        return _formGroups[groupPosition].getGroupView(isExpanded, convertView, parent);
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild, final View convertView, final ViewGroup parent) {
        return _formGroups[groupPosition].getChildView(childPosition, isLastChild, convertView, parent);
    }

    @Override
    public boolean isChildSelectable(final int groupPosition, final int childPosition) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public Object getGroup(final int groupPosition) {
        return null;
    }

    @Override
    public Object getChild(final int groupPosition, final int childPosition) {
        return null;
    }

    @Override
    public boolean onChildClick(final ExpandableListView parent, final View v, final int groupPosition, final int childPosition, final long id) {
        _formGroups[groupPosition].onChildClick(v, childPosition);
        return false;
    }

    @Override
    public boolean onGroupClick(final ExpandableListView parent, final View v, final int groupPosition, final long id) {
        _formGroups[groupPosition].onGroupClick(v, parent.isGroupExpanded(groupPosition));
        return false;
    }
}
