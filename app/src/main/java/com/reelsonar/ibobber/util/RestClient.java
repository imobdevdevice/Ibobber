package com.reelsonar.ibobber.util;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

import static com.reelsonar.ibobber.util.AppUtils.BASE_TEST_URL;

/**
 * Created by Manoj Singh
 */

public final class RestClient {

    private static final int TIME = 30;

    private static ApiService baseApiService;
    private static OkHttpClient httpClient = new OkHttpClient().newBuilder()
            .connectTimeout(TIME, TimeUnit.SECONDS)
            .readTimeout(TIME, TimeUnit.SECONDS)
            .writeTimeout(TIME, TimeUnit.SECONDS)
            .build();

    public static ApiService getInstance() {
        if (baseApiService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_TEST_URL)
                    .addConverterFactory(new ToStringConverterFactory())
                    .client(httpClient)
                    .build();

            baseApiService = retrofit.create(ApiService.class);
        }
        return baseApiService;
    }

}
