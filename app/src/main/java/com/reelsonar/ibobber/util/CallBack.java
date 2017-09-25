package com.reelsonar.ibobber.util;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by rujul on 22/9/17.
 */

public interface GenericCallBack {
    <T> void onResponse(Call call, Response response, String msg, Object object);

    void onFail(Call call, Throwable e);

    void onSocketTimeout(Call call, Throwable e);

}
