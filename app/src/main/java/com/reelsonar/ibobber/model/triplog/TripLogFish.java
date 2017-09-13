// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.model.triplog;

import android.content.res.Resources;
import android.util.Log;
import android.util.SparseIntArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import com.reelsonar.ibobber.BobberApp;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.model.FavoriteFish;
import com.reelsonar.ibobber.service.UserService;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Pattern;

public class TripLogFish {

    private SparseIntArray _fishIdsToQuantities;

    public TripLogFish() {
        _fishIdsToQuantities = new SparseIntArray();
    }

    public TripLogFish(final SparseIntArray fishIdsToQuantities) {
        _fishIdsToQuantities = fishIdsToQuantities;
    }

    public int getQuantityForFishId(final int fishId) {
        return _fishIdsToQuantities.get(fishId);
    }

    public void setQuantityForFishId(final int fishId, final int quantity) {
        _fishIdsToQuantities.put(fishId, quantity);
    }

    public int getTotalQuantity() {
        int totalQuantity = 0;
        for (int i = 0; i < _fishIdsToQuantities.size(); ++i) {
            totalQuantity += _fishIdsToQuantities.valueAt(i);
        }
        return totalQuantity;
    }

    public SparseIntArray getFishIdsToQuantities() {
        return _fishIdsToQuantities;
    }

    public ArrayList<Integer> getKeys() {
        ArrayList<Integer> keys = new ArrayList<Integer>();

          for (int i = 0; i < _fishIdsToQuantities.size(); ++i) {
              keys.add( _fishIdsToQuantities.keyAt(i));
          }

        return keys;
    }

    public String getNameForFishId(int fishId) {

        //TODO: This code should be consolidated with FavoriteFishLoader

        String fishName = "";
        InputStream is = BobberApp.getContext().getResources().openRawResource(R.raw.fish);

        try {
            NSDictionary fishDicts = (NSDictionary) PropertyListParser.parse(is);

            Pattern stringNamePattern = Pattern.compile("\\W+");

            for (Map.Entry<String, NSObject> entry : fishDicts.entrySet()) {
                int id = Integer.parseInt(entry.getKey());
                String name = entry.getValue().toString();
                String stringName = "fish_" + stringNamePattern.matcher(name).replaceAll("_").toLowerCase();
                int resourceId = BobberApp.getContext().getResources().getIdentifier(stringName, "string", BobberApp.getContext().getApplicationInfo().packageName);
                if (resourceId != 0) {
                    name = BobberApp.getContext().getResources().getString(resourceId);
                }

                if (id == fishId) fishName = name;
            }

        } catch (Exception ex) {
        } finally {
            try { is.close(); } catch (Exception ignored) { }
        }

        return fishName;
    }

}
