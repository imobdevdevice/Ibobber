// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.reelsonar.ibobber.R;

import java.io.*;

public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "dbBobber";

    private static final int DB_VERSION = 4;  // Note: Increment DB_VERSION when needing to force call to onUpdate(), below.
                                              // Incremented to '2' for Feb 2016 trip log image update
                                              // Incremented to '3' for Jne 2016 new fields:  water temp, water depth, & air temp
                                              // Incremented to '4' pending fishCaugth , fishCatchId
    private static DBOpenHelper INSTANCE;
    public static synchronized DBOpenHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DBOpenHelper(context.getApplicationContext());
        }
        return INSTANCE;
    }

    private final Context _context;

    public DBOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        _context = context;
    }

    private String getCreateDBSQL() throws IOException {
        InputStream in = _context.getResources().openRawResource(R.raw.createdb_3);
        InputStreamReader reader = new InputStreamReader(in);
        StringWriter sql = new StringWriter(512);
        char[] buffer = new char[512];

        try {
            int n;
            while ((n = reader.read(buffer)) != -1) {
                sql.write(buffer, 0, n);
            }
        } finally {
            try { reader.close(); } catch (IOException ignored) { }
        }

        return sql.toString();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String sql = getCreateDBSQL();
            String[] statements = sql.split("\\s*;\\s*");
            try {
                db.beginTransaction();
                for (String statement : statements) {
                    db.execSQL(statement);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if( oldVersion < 2 ) {
            // Create tripLogImages table
            String tripLogImagesTableSQL = "CREATE TABLE tripLogImages (tripLogId INTEGER, filename TEXT, PRIMARY KEY(tripLogId, filename), FOREIGN KEY (tripLogId) REFERENCES tripLog(id))";
            try {
                db.execSQL( tripLogImagesTableSQL );
            }  catch (Exception ex) {
                Log.e("iBobber","Got db error: " + ex.toString() );
            }
        }

        if( oldVersion >= 1 && oldVersion < 4) {
            try {
                db.execSQL( "ALTER TABLE tripLog ADD COLUMN lureType INTEGER" );
                db.execSQL( "ALTER TABLE tripLog ADD COLUMN waterTemp NUMERIC" );
                db.execSQL( "ALTER TABLE tripLog ADD COLUMN waterDepth NUMERIC" );
                db.execSQL( "ALTER TABLE tripLog ADD COLUMN fishCaught TEXT" );
                db.execSQL( "ALTER TABLE tripLog ADD COLUMN netFishCatchId NUMERIC" );
            }  catch (Exception ex) {
                Log.e("iBobber","Got db error: " + ex.toString() );
            }
        }
    }

}
