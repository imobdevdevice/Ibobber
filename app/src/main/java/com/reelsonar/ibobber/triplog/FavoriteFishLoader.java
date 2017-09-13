// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.triplog;

import android.content.Context;
import android.util.Log;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.model.FavoriteFish;
import com.reelsonar.ibobber.util.AsyncLoader;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

public class FavoriteFishLoader extends AsyncLoader<List<FavoriteFish>> {

    private static final String TAG = FavoriteFishLoader.class.getSimpleName();

    private Set<Integer> _selectedFishIds;

    public FavoriteFishLoader(final Context context) {
        this(context, null);
    }

    public FavoriteFishLoader(final Context context, final Set<Integer> selectedFishIds) {
        super(context);
        _selectedFishIds = selectedFishIds;
        onContentChanged();
    }

    @Override
    public List<FavoriteFish> loadInBackground() {
        List<FavoriteFish> fish = null;

        InputStream is = getContext().getResources().openRawResource(R.raw.fish);
        try {
            NSDictionary fishDicts = (NSDictionary) PropertyListParser.parse(is);
            fish = new ArrayList<>(fishDicts.count());

            Pattern stringNamePattern = Pattern.compile("\\W+");

            for (Map.Entry<String, NSObject> entry : fishDicts.entrySet()) {
                int id = Integer.parseInt(entry.getKey());
                String name = entry.getValue().toString();

                String stringName = "fish_" + stringNamePattern.matcher(name).replaceAll("_").toLowerCase();
                int resourceId = getContext().getResources().getIdentifier(stringName, "string", getContext().getApplicationInfo().packageName);
                if (resourceId != 0) {
                    name = getContext().getString(resourceId);

                    if (!name.isEmpty()) {
                        boolean selected = _selectedFishIds != null && _selectedFishIds.contains(id);
                        fish.add(new FavoriteFish(id, name, selected));
                    }
                }
            }

            Collections.sort(fish, new Comparator<FavoriteFish>() {
                @Override
                public int compare(FavoriteFish fish, FavoriteFish fish2) {
                    return fish.getName().compareTo(fish2.getName() );
                    }
            });


        } catch (Exception ex) {
            Log.e(TAG, "Error loading fish", ex);
            fish = Collections.emptyList();
        } finally {
            try { is.close(); } catch (Exception ignored) { }
        }

        return fish;
    }

}
