package com.reelsonar.ibobber.util;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Manoj Singh
 */

public interface CallBackOld {

//    <T> void onResponse(Call call, Response response, String msg, Class<T> clazz);

    void onResponse(Call call, Response response, String msg);

    void onFail(Call call, Throwable e);

    void onSocketTimeout(Call call, Throwable e);


}
