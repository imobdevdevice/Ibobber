package com.reelsonar.ibobber.util;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.reelsonar.ibobber.util.AppUtils.logout;

/**
 * Created by Manoj Singh
 */

public class ApiLoader {

    public static void getLogin(final Context context, final HashMap<String, String> hashMap, final CallBack callBack) {
        Call<String> call = RestClient.getInstance().getLogin(hashMap);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String error = "";
//                if (response.code() == HttpURLConnection.HTTP_OK) {
//                    ResponseBody employeeHandler = response.body();
//                }
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    if (jsonObject.has("authentication")) {
                        if(jsonObject.optBoolean("authentication")){
                            Log.d("reponseLogout","Logout");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (callBack != null)
                    callBack.onResponse(call, response, error);
            }


            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if (callBack != null) {
                    if (t instanceof SocketTimeoutException)
                        callBack.onSocketTimeout(call, t);
                    else
                        callBack.onFail(call, t);
                }
            }
        });
    }

    public static void getRegister(final Context context, final HashMap<String, String> hashMap, final CallBack callBack) {

        Call<String> call = RestClient.getInstance().getRegister(hashMap);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String error = "";
//                if (response.code() == HttpURLConnection.HTTP_OK) {
//                    ResponseBody employeeHandler = response.body();
//                }
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    if (jsonObject.has("authentication")) {
                        if(jsonObject.optBoolean("authentication")){
                            Log.d("reponseLogout","Logout");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (callBack != null)
                    callBack.onResponse(call, response, error);
            }


            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if (callBack != null) {
                    if (t instanceof SocketTimeoutException)
                        callBack.onSocketTimeout(call, t);
                    else
                        callBack.onFail(call, t);
                }
            }
        });
    }


    public static void createTripLog(final Context context, final HashMap<String, String> hashMap, final CallBack callBack) {

        Call<String> call = RestClient.getInstance().createTripLog(hashMap);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String error = "";
//                if (response.code() == HttpURLConnection.HTTP_OK) {
//                    ResponseBody employeeHandler = response.body();
//                }
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    if (jsonObject.has("authentication")) {
                        if(!jsonObject.optBoolean("authentication")){
                            logout(context);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (callBack != null)
                    callBack.onResponse(call, response, error);
            }


            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if (callBack != null) {
                    if (t instanceof SocketTimeoutException)
                        callBack.onSocketTimeout(call, t);
                    else
                        callBack.onFail(call, t);
                }
            }
        });
    }

    public static void getTripLog(final Context context, final HashMap<String, String> hashMap, final CallBack callBack) {
        Call<String> call = RestClient.getInstance().getTripLog(hashMap);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String error = "";
//                if (response.code() == HttpURLConnection.HTTP_OK) {
//                    ResponseBody employeeHandler = response.body();
//                }
                if (callBack != null)
                    callBack.onResponse(call, response, error);
            }


            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if (callBack != null) {
                    if (t instanceof SocketTimeoutException)
                        callBack.onSocketTimeout(call, t);
                    else
                        callBack.onFail(call, t);
                }
            }
        });
    }

}
