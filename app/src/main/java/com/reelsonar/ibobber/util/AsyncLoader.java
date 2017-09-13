// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.util;

import android.content.AsyncTaskLoader;
import android.content.Context;

/**
 * Created by james on 9/22/14.
 * A small wrapper class to force the loading to actually occur.
 * (See http://stackoverflow.com/questions/10524667/android-asynctaskloader-doesnt-start-loadinbackground
 * for further info.)
 */
public abstract class AsyncLoader<D> extends AsyncTaskLoader<D> {

    private D _result;

    protected AsyncLoader(final Context context) {
        super(context);
        onContentChanged();
    }

    @Override
    public void onContentChanged() {
        _result = null;
        super.onContentChanged();
    }

    @Override
    public void deliverResult(final D result) {
        _result = result;
        super.deliverResult(result);
    }

    @Override
    protected void onStartLoading() {
        if (_result != null) {
            deliverResult(_result);
        }

        if (takeContentChanged()) {
            forceLoad();
        }
    }
}
