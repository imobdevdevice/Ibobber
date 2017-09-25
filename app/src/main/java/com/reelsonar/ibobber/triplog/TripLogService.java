// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.triplog;

import android.content.ContentValues;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseIntArray;

import com.reelsonar.ibobber.TaskListener;
import com.reelsonar.ibobber.bluetooth.BTService;
import com.reelsonar.ibobber.db.DBLoader;
import com.reelsonar.ibobber.db.DBOpenHelper;
import com.reelsonar.ibobber.dsp.SonarDataService;
import com.reelsonar.ibobber.model.triplog.CatchTripLogDetails;
import com.reelsonar.ibobber.model.triplog.Condition;
import com.reelsonar.ibobber.model.triplog.FishingType;
import com.reelsonar.ibobber.model.triplog.LureType;
import com.reelsonar.ibobber.model.triplog.TripLog;
import com.reelsonar.ibobber.model.triplog.TripLogFish;
import com.reelsonar.ibobber.model.triplog.TripLogImages;
import com.reelsonar.ibobber.service.LocationService;
import com.reelsonar.ibobber.weather.WeatherData;
import com.reelsonar.ibobber.weather.WeatherService;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.reelsonar.ibobber.util.RestConstants.NETFISH_CATCH_TITLE;

public class TripLogService {


    private static TripLogService INSTANCE;

    public static synchronized TripLogService getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new TripLogService(context.getApplicationContext());
        }
        return INSTANCE;
    }

    private Context _context;

    public TripLogService(Context context) {
        _context = context;
    }

    public Loader<List<TripLog>> tripLogsLoader() {
        return new DBLoader<List<TripLog>>(_context, "SELECT id,dateTrip,title FROM tripLog ORDER BY dateTrip ASC") {
            @Override
            public List<TripLog> loadFromCursor(final Cursor c) {
                List<TripLog> tripLogs = new ArrayList<>();
                while (c.moveToNext()) {
                    long id = c.getLong(0);
                    long dateTrip = c.getLong(1);
                    String title = c.getString(2);

                    tripLogs.add(new TripLog(id, new Date(dateTrip), title));
                }
                return tripLogs;
            }
        };
    }


    public Loader<List<TripLog>> tripLogsForDate(final long date) {

        String sql = "SELECT id,dateTrip,longitude,latitude,title,lure,condition,fishingType,notes FROM tripLog";

        return new DBLoader<List<TripLog>>(_context, sql) {
            @Override
            public List<TripLog> loadFromCursor(final Cursor c) {
                List<TripLog> tripLogs = new ArrayList<>();
                while (c.moveToNext()) {
                    long id = c.getLong(0);
                    String title = c.getString(2);

                    Date dateTrip = new Date(c.getLong(1));
                    Date dateSearchingFor = new Date(date);

                    Calendar cal1 = Calendar.getInstance();
                    Calendar cal2 = Calendar.getInstance();
                    cal1.setTime(dateTrip);
                    cal2.setTime(dateSearchingFor);
                    boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

                    if (sameDay) tripLogs.add(new TripLog(id, dateTrip, title));
                }
                return tripLogs;
            }
        };
    }

    public Loader<TripLog> tripLogLoader(final long id) {

        String sql = "SELECT id,dateTrip,longitude,latitude,title,lure, lureType, condition, fishingType, notes, COALESCE(waterTemp, 9999), COALESCE(waterDepth, 9999), COALESCE(airTemp, 9999) FROM tripLog WHERE id = ?";
        String[] args = {String.valueOf(id)};

        return new DBLoader<TripLog>(_context, sql, args) {
            @Override
            public TripLog loadFromCursor(final Cursor c) {
                if (c.moveToFirst()) {
                    long id = c.getLong(0);
                    long dateTrip = c.getLong(1);
                    double longitude = c.getDouble(2);
                    double latitude = c.getDouble(3);
                    String titleTrip = c.getString(4);
                    String lure = c.getString(5);
                    LureType lureType = TripLog.constantFromId(LureType.class, c.getInt(6));
                    Condition condition = TripLog.constantFromId(Condition.class, c.getInt(7));
                    FishingType fishing = TripLog.constantFromId(FishingType.class, c.getInt(8));
                    String notes = c.getString(9);
                    double waterTemp = c.getDouble(10);
                    double waterDepth = c.getDouble(11);
                    double airTemp = c.getDouble(12);

                    if (lure != null && lureType == null)
                        lureType = LureType.OTHER;

                    return new TripLog(id, new Date(dateTrip), longitude, latitude, titleTrip, lure, lureType, condition, fishing, notes, waterTemp, waterDepth, airTemp);
                } else {
                    return null;
                }
            }
        };
    }

    public Loader<TripLogFish> tripLogFishLoader(final long id) {
        String sql = "SELECT fishId, fishQty FROM fishCaught WHERE tripLogId = ? ORDER BY fishId ASC";
        String[] args = {String.valueOf(id)};

        return new DBLoader<TripLogFish>(_context, sql, args) {
            @Override
            public TripLogFish loadFromCursor(final Cursor c) {
                SparseIntArray fishIdsToQuantities = new SparseIntArray();
                while (c.moveToNext()) {
                    int fishId = c.getInt(0);
                    int quantity = c.getInt(1);

                    fishIdsToQuantities.append(fishId, quantity);
                }
                return new TripLogFish(fishIdsToQuantities);
            }
        };
    }

    public Loader<TripLogImages> tripLogImagesLoader(final long id) {
        String sql = "SELECT filename FROM tripLogImages WHERE tripLogId = ?";
        String[] args = {String.valueOf(id)};

        return new DBLoader<TripLogImages>(_context, sql, args) {
            @Override
            public TripLogImages loadFromCursor(final Cursor c) {
                ArrayList<String> imageFilenameList = new ArrayList<String>();
                while (c.moveToNext()) {
                    String filename = c.getString(0);
                    File file = new File(filename);
                    if (file.exists())
                        imageFilenameList.add(filename);
                }
                return new TripLogImages(imageFilenameList);
            }
        };
    }

    public TripLog saveTripLogAtCurrentLocation() {

        LocationService.getInstance(_context).setupLocationRequestsIfNeeded();

        Location location = LocationService.getInstance(_context).getLastLocation();

        WeatherService weatherService = WeatherService.getInstance(_context);
        WeatherData weatherData = weatherService.getWeatherData();
        Number airTemp = null;
        if (weatherData != null)
            airTemp = weatherService.getWeatherData().getTempC();

        TripLog tripLog = new TripLog();
        tripLog.setWaterTemp(9999);
        tripLog.setWaterDepth(9999);

        if (airTemp == null)
            tripLog.setAirTemp(9999);
        else
            tripLog.setAirTemp(airTemp.doubleValue());

        if (location != null && location != LocationService.UNKNOWN_LOCATION) {
            tripLog.setLatitude(location.getLatitude());
            tripLog.setLongitude(location.getLongitude());
        }

        BTService btService = BTService.getSingleInstance();
        int waterTemp = btService.getTempCelsius();
        tripLog.setWaterTemp(waterTemp);

        double depth = SonarDataService.getInstance(_context).getDepth();
        tripLog.setWaterDepth(depth);
        tripLog.setDate(new Date());
//        saveTripLog(tripLog, null, null);

        return tripLog;
    }

    public void saveTripLog(final TripLog tripLog, final TripLogImages tripLogImages, final TripLogFish fish) {

        if (tripLog == null) return;

        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                String title;
                if (tripLog.getTitle() != null) {
                    title = tripLog.getTitle();
                } else {
                    title = "untitled";
                }

                ContentValues contentValues = new ContentValues();
                contentValues.put("dateTrip", tripLog.getDate().getTime());
                contentValues.put("longitude", tripLog.getLongitude());
                contentValues.put("latitude", tripLog.getLatitude());
                contentValues.put("title", title);
                contentValues.put("lure", tripLog.getLure());
                if (tripLog.getLureType() != null)
                    contentValues.put("lureType", tripLog.getLureType().ordinal() + 1);
                contentValues.put("waterTemp", tripLog.getWaterTemp());
                contentValues.put("waterDepth", tripLog.getWaterDepth());
                contentValues.put("airTemp", tripLog.getAirTemp());

                if (tripLog.getCondition() != null) {
                    contentValues.put("condition", tripLog.getCondition().getId());
                }
                if (tripLog.getFishingType() != null) {
                    contentValues.put("fishingType", tripLog.getFishingType().getId());
                }
                contentValues.put("notes", tripLog.getNotes());

                SQLiteDatabase db = DBOpenHelper.getInstance(_context).getWritableDatabase();
                try {
                    db.beginTransaction();

                    long tripLogId = tripLog.getIdTrip();
                    if (tripLogId == -1) {
                        tripLogId = db.insert("tripLog", null, contentValues);
                    } else {
                        String[] args = new String[]{String.valueOf(tripLog.getIdTrip())};
                        db.update("tripLog", contentValues, "id = ?", args);
                        db.delete("fishCaught", "tripLogId = ?", args);
                        db.delete("tripLogImages", "tripLogId = ?", args);
                    }

                    if (fish != null) {
                        SparseIntArray fishArray = fish.getFishIdsToQuantities();
                        ContentValues fishValues = new ContentValues();
                        fishValues.put("tripLogId", tripLogId);
                        for (int i = 0; i < fishArray.size(); ++i) {
                            int quantity = fishArray.valueAt(i);
                            if (quantity > 0) {
                                int id = fishArray.keyAt(i);
                                fishValues.put("fishId", id);
                                fishValues.put("fishQty", quantity);
                                db.insert("fishCaught", null, fishValues);
                            }
                        }
                    }

                    if (tripLogImages != null) {
                        ArrayList<String> imageFilenameList = tripLogImages.getImageFilenameList();
                        if (imageFilenameList != null && imageFilenameList.size() > 0) {
                            ContentValues imageFilenameValues = new ContentValues();
                            imageFilenameValues.put("tripLogId", tripLogId);
                            for (int i = 0; i < imageFilenameList.size(); ++i) {
                                String imageFilename = imageFilenameList.get(i);
                                imageFilenameValues.put("filename", imageFilename);
                                db.insert("tripLogImages", null, imageFilenameValues);
                                Log.d("iBobber", "breakpoint");
                            }
                        }
                    }

                    db.setTransactionSuccessful();
                    EventBus.getDefault().post(new DBLoader.ContentChangedEvent());
                } finally {
                    db.endTransaction();
                }
            }
        });
    }

    public void saveTripLog(final List<CatchTripLogDetails> catchTripLog, final TaskListener callBack) {
//        synchronized (this) {
            for (int i = 0; i < catchTripLog.size(); i++) {
                CatchTripLogDetails tripLog = catchTripLog.get(i);
                String catchId = tripLog.getCatchId();
                ContentValues contentValues = new ContentValues();
                contentValues.put("dateTrip", Long.parseLong(tripLog.getCatchCreatedAt()) * 1000L);
//                    contentValues.put("dateTrip", 1495690225000L);
                contentValues.put("title", NETFISH_CATCH_TITLE);
                contentValues.put("netFishCatchId", Long.parseLong(tripLog.getCatchId()));
                SQLiteDatabase db = DBOpenHelper.getInstance(_context).getWritableDatabase();
                db.beginTransaction();
                if (isCatchCreated(catchId)) {
                    String[] args = new String[]{String.valueOf(catchId)};
                    db.update("tripLog", contentValues, " netFishCatchId = ?", args);
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    EventBus.getDefault().post(new DBLoader.ContentChangedEvent());
                } else {
                    Long id = db.insert("tripLog", null, contentValues);
                    db.setTransactionSuccessful();
                    db.endTransaction();
//                        db.close();
                    EventBus.getDefault().post(new DBLoader.ContentChangedEvent());
                }
//
//
//                    SQLiteDatabase db = DBOpenHelper.getInstance(_context).getWritableDatabase();
//                    db.beginTransaction();
//                    if (isCatchCreated(catchId)) {
//
//                        String[] args = new String[]{String.valueOf(catchId)};
//                        db.update("tripLog", contentValues, "id = ?", args);
//                        db.endTransaction();
//                        EventBus.getDefault().post(new DBLoader.ContentChangedEvent());
//////                            db.delete("fishCaught", "tripLogId = ?", args);
//////                            db.delete("tripLogImages", "tripLogId = ?", args);
//                    } else {
//                        Long id = db.insert("tripLog", null, contentValues);
//                        db.setTransactionSuccessful();
//                        db.endTransaction();
//                        db.close();
//                        EventBus.getDefault().post(new DBLoader.ContentChangedEvent());
//                    }
            }
            callBack.onTaskCompleted();
//        }

    }

    public List<TripLog> getCatchTripLog() {
        final List<TripLog> listCatchLog = new ArrayList<TripLog>();
        SQLiteDatabase db = DBOpenHelper.getInstance(_context).getReadableDatabase();
        db.beginTransaction();
        try {
            Cursor c = db.rawQuery("SELECT * FROM tripLog", null);
            if (c.moveToFirst()) {
                do {
                    TripLog obj = new TripLog();
                    //only one column
                    obj.setIdTrip(c.getLong(c.getColumnIndex("id")));
                    obj.setTitle(c.getString(c.getColumnIndex("title")));
                    try {
                        obj.setNetFishId(c.getLong(c.getColumnIndex("netFishCatchId")));
                        obj.setDate(new Date(c.getLong(c.getColumnIndex("dateTrip"))));
                    } catch (Exception e) {
                        e.printStackTrace();
                        obj.setNetFishId(0);
                    }
//                    obj.setDate(c.getString(c.getColumnIndex("")));
                    //you could add additional columns here..

                    listCatchLog.add(obj);
                } while (c.moveToNext());
                c.close();
                db.setTransactionSuccessful();
            } else {
                c.close();
                db.setTransactionSuccessful();
            }
        } finally {
            db.endTransaction();
        }
        return listCatchLog;
    }

    public boolean isCatchCreated(String catchId) {
        SQLiteDatabase db = DBOpenHelper.getInstance(_context).getReadableDatabase();
        db.beginTransaction();
        Cursor c = db.rawQuery("SELECT * FROM tripLog WHERE netFishCatchId='" + catchId + "'", null);
        if (c.moveToFirst()) {
            c.close();
            db.setTransactionSuccessful();
            db.endTransaction();
            return true;
        } else {
            c.close();
            db.setTransactionSuccessful();
            db.endTransaction();
            return false;
        }
    }

    public void deleteTripLog(final TripLog tripLog) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = DBOpenHelper.getInstance(_context).getWritableDatabase();
                try {
                    db.beginTransaction();
                    String[] args = new String[]{String.valueOf(tripLog.getIdTrip())};
                    db.delete("fishCaught", "tripLogId = ?", args);
                    db.delete("tripLogImages", "tripLogId = ?", args);
                    db.delete("tripLog", "id = ?", args);
                    db.setTransactionSuccessful();
                    EventBus.getDefault().post(new DBLoader.ContentChangedEvent());
                } finally {
                    db.endTransaction();

                }
            }
        });
    }
}
