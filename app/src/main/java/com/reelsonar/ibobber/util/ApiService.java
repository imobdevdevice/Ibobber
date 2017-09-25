package com.reelsonar.ibobber.util;


import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Created by Manoj Singh
 */

public interface ApiService {

    @FormUrlEncoded
    @POST("login")
    Call<String> getLogin(@FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("register")
    Call<String> getRegister(@FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("catch_add")
    Call<String> createTripLog(@FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST("catches_view")
    Call<String> getTripLog(@FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST
    Call<String> getPost(@FieldMap HashMap<String, String> hashMap, @Url String url);
}
