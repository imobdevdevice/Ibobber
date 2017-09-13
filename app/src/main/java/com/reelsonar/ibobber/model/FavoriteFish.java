// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.model;

import android.os.Parcel;
import android.os.Parcelable;

public class FavoriteFish implements Parcelable {

    public static final Parcelable.Creator<FavoriteFish> CREATOR = new Parcelable.Creator<FavoriteFish>() {
        @Override
        public FavoriteFish createFromParcel(Parcel parcel) {
            int id = parcel.readInt();
            String name = parcel.readString();
            boolean selected = parcel.readByte() == 1;
            return new FavoriteFish(id, name, selected);
        }

        @Override
        public FavoriteFish[] newArray(int size) {
            return new FavoriteFish[size];
        }
    };

    private int _id;
    private String _name;
    private boolean _selected;

    public FavoriteFish(int id, String name, boolean selected) {
        _id = id;
        _name= name;
        _selected = selected;
    }

    public int getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public boolean isSelected() {
        return _selected;
    }

    public void setSelected(boolean selected) {
        this._selected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flag) {
        parcel.writeInt(_id);
        parcel.writeString(_name);
        parcel.writeByte(_selected ? (byte)1 : (byte)0);
    }
}
