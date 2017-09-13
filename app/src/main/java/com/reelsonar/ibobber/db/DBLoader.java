// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.reelsonar.ibobber.util.AsyncLoader;
import de.greenrobot.event.EventBus;

public abstract class DBLoader<D> extends AsyncLoader<D> {

    public static class ContentChangedEvent { }

    private String _query;
    private String[] _selectionArgs;

    public DBLoader(final Context context, final String query, final String[] selectionArgs) {
        super(context);
        _query = query;
        _selectionArgs = selectionArgs;
    }

    public DBLoader(final Context context, final String query) {
        this(context, query, null);
    }

    @Override
    public D loadInBackground() {
        SQLiteDatabase db = DBOpenHelper.getInstance(getContext()).getReadableDatabase();
        Cursor c = db.rawQuery(_query, _selectionArgs);
        D result = loadFromCursor(c);
        c.close();
        return result;
    }

    @Override
    protected void onStartLoading() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        super.onStartLoading();
    }

    @Override
    protected void onReset() {
        EventBus.getDefault().unregister(this);
        super.onReset();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
    }

    public void onEventMainThread(final ContentChangedEvent changedEvent) {
        onContentChanged();
    }

    public abstract D loadFromCursor(final Cursor cursor);

}
