// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.model.triplog;

import android.util.Log;
import com.reelsonar.ibobber.form.IdName;

import java.util.ArrayList;
import java.util.Date;

public class TripLogImages {

    public final int IMAGE_LIST_MAXSIZE = 3;

    private long _tripId;

    private ArrayList<String> _imageFilenameList;

    public TripLogImages() {

        _imageFilenameList = new ArrayList<String>();
    }

     public TripLogImages(long idTrip ) {
         _tripId = idTrip;
        _imageFilenameList = new ArrayList<String>();
    }

    public TripLogImages(final ArrayList<String> imageFilenameList) {

        if (imageFilenameList != null)
            _imageFilenameList = imageFilenameList;
        else
            _imageFilenameList = new ArrayList<String>();

    }

    public TripLogImages(long idTrip, ArrayList<String> imageFilenameList) {

        _tripId = idTrip;

        if (imageFilenameList != null)
            _imageFilenameList = imageFilenameList;
        else
            _imageFilenameList = new ArrayList<String>();
    }

    public void setImageFilenameList(ArrayList<String> imageFilenameList) {

        if (imageFilenameList != null)
            _imageFilenameList = imageFilenameList;
        else
            _imageFilenameList = new ArrayList<String>();
    }

    public ArrayList<String> getImageFilenameList() {
        return _imageFilenameList;
    }

    public void addImage(String imageFileName) {

        if (_imageFilenameList == null) {
            _imageFilenameList = new ArrayList<String>();
        }

        _imageFilenameList.add(0, imageFileName);

        if (_imageFilenameList.size() > IMAGE_LIST_MAXSIZE) { // Drop off older items
            _imageFilenameList.remove(IMAGE_LIST_MAXSIZE);
        }
    }

    public String getImageByIndex(int imageIndex) {

        if (_imageFilenameList != null && _imageFilenameList.size() > imageIndex)
            return _imageFilenameList.get(imageIndex);

        return null;
    }

    public void deleteImageAtIndex( int imageIndex ) {

        _imageFilenameList.remove( imageIndex );
    }
}