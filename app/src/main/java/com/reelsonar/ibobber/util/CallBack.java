package com.reelsonar.ibobber.util;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Manoj Singh
 */

public interface CallBack {

    void onResponse(Call call, Response response, String msg);

    void onFail(Call call, Throwable e);

    void onSocketTimeout(Call call, Throwable e);


}
