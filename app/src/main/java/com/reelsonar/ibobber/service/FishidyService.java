package com.reelsonar.ibobber.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by markc on 6/9/16.
 */
public class FishidyService {

    private static final String TAG = "FishidyService";

    private static final String KEY_FISHIDY_AUTH_TOKEN = "fishidyAuthToken";

    // static String fishidyServerURI = "https://snapper.fishidy.com/api/v3";
    static String fishidyServerURI = "https://www.fishidy.com/api/v3";
    static String fishidyAuth = "account/signin";
    static String fishidyAddCatch = "catch/add";

    private Context context;

    private FishidyAuthTask fishidyAuthTask;
    private FishidyUploadCatchTask fishidyUploadCatchTask;

    private FishidyServiceDelegate delegate;

    private String authToken;

    public String getAuthToken() {
        return authToken;
    }

    private static FishidyService INSTANCE;

    public static synchronized FishidyService getInstance(Context context) {

        if (INSTANCE == null) {
            INSTANCE = new FishidyService(context.getApplicationContext());
        }
        return INSTANCE;
    }

    private FishidyService(Context context) {

        this.context = context;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        authToken = prefs.getString(KEY_FISHIDY_AUTH_TOKEN, null);
    }

    public void authenticate(FishidyAuthData authData) {

        fishidyAuthTask = new FishidyAuthTask();
        fishidyAuthTask.setAuthData( authData );

        fishidyAuthTask.execute();
    }

    public void setDelegate(FishidyServiceDelegate delegate) {
        this.delegate = delegate;
    }

    public void uploadCatch( FishidyCatch fishidyCatch ) {

        fishidyUploadCatchTask = new FishidyUploadCatchTask();
        fishidyUploadCatchTask.setFishidyCatch( fishidyCatch );
        fishidyUploadCatchTask.execute();
    }

    private class FishidyAuthTask extends AsyncTask<String, Integer, Boolean> {

        FishidyAuthData authData;

        public void setAuthData(FishidyAuthData authData) {
            this.authData = authData;
        }

        String tokenResult;

        @Override
        protected Boolean doInBackground(String... sUrl) {
            try {

                // Test:  curl -i -X POST -H "Accept: application/json"  -H Content-type:application/json --data "{\"EmailAddress\":\"conotech+dev@gmail.com\", \"Password\":\"bogus2013\"}"  http://snapper.fishidy.com/api/v3/account/signin

                URL url = new URL( fishidyServerURI + "/" + fishidyAuth );
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                StringBuilder result = new StringBuilder();

                try {
                    urlConnection.setConnectTimeout(1000 * 10); // Ten seconds
                    urlConnection.setDoOutput(true);
                    urlConnection.setUseCaches(false);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "application/json");

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("EmailAddress", authData.login);
                    jsonParam.put("Password", authData.password);

                    urlConnection.connect();

                    OutputStream os = urlConnection.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");

                    osw.write(jsonParam.toString());
                    osw.flush();
                    osw.close();

                    int status = urlConnection.getResponseCode();

                    switch (status) {
                        case 200:
                        case 201:
                            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                            String line;
                            while ((line = br.readLine()) != null) {
                                result.append(line + "\n");
                            }
                            br.close();
                            JSONTokener tokener = new JSONTokener(result.toString());
                            JSONObject jsonResult = new JSONObject(tokener);
                            Log.d(TAG, jsonResult.toString());
                            Boolean success = jsonResult.getBoolean("Success");
                            if( success ) {
                                Log.d(TAG, jsonResult.toString());
                                tokenResult = jsonResult.getString("Token");
                            } else {
                                return false;
                            }
                            break;
                        default:
                            Log.d(TAG, "Unknown status: " + status);
                    }
                } catch (Exception exc) {
                    Log.e(TAG, "Auth got error: " + exc.getLocalizedMessage());

                } finally {
                    urlConnection.disconnect();
                }

            } catch (Exception e) {
                Log.e(TAG,"AuthManager got error authenticating: " + e );
                return false;
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            Log.d(TAG, "AuthManager auth progress: " + progress );
        }

        @Override
        protected void onPostExecute(final Boolean result) {

            if( result ) {
                authToken = tokenResult;
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(KEY_FISHIDY_AUTH_TOKEN, authToken);
                editor.apply();

                if( delegate != null )
                    delegate.handleAuthSuccess();
            } else {
                authToken = null;
                if( delegate != null )
                    delegate.handleAuthFailure();
            }
            fishidyAuthTask = null;
//          reservationSubscriber.handleBikeReservationResult( result );
        }

    }

    private class FishidyUploadCatchTask extends AsyncTask<String, Integer, Boolean> {

        private FishidyCatch fishidyCatch;
        private Integer errorStatusCode;

        public void setFishidyCatch(FishidyCatch fishidyCatch) {
            this.fishidyCatch = fishidyCatch;
        }

        @Override
        protected Boolean doInBackground(String... sUrl) {

            Boolean returnResult = false;
            try {

                if( authToken == null || authToken.length() ==  0 ) {
                    Log.e(TAG, "FishidyUploadCatchTask found missing auth token.");
                    return false;
                }
                if( fishidyCatch == null  ) {
                    Log.e(TAG, "FishidyUploadCatchTask found null fishidyCatch.");
                    return false;
                }
                URL url = new URL( fishidyServerURI + "/" + fishidyAddCatch );
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                StringBuilder result = new StringBuilder();

                try {
                    urlConnection.setConnectTimeout(1000 * 10); // Ten seconds
                    urlConnection.setDoOutput(true);
                    urlConnection.setUseCaches(false);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setRequestProperty("Token", authToken );

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("CatchDateTime", this.fishidyCatch.catchDateTime);
                    jsonParam.put("Latitude", this.fishidyCatch.latitude);
                    jsonParam.put("Longitude", this.fishidyCatch.longitude);
                    jsonParam.put("Species", this.fishidyCatch.species);
                    jsonParam.put("Length", this.fishidyCatch.length);
                    jsonParam.put("Weight", this.fishidyCatch.weight);
                    if( fishidyCatch.waterTemp != null )
                        jsonParam.put("WaterTemperature", this.fishidyCatch.waterTemp);
                    if( fishidyCatch.waterDepth != null )
                        jsonParam.put("WaterDepth", this.fishidyCatch.waterDepth);
                    if( fishidyCatch.airTemp != null )
                        jsonParam.put("AirTemperature", this.fishidyCatch.airTemp);
                    jsonParam.put("PrivacyType", this.fishidyCatch.privacyCode);
                    if( fishidyCatch.lure != null )
                        jsonParam.put("Lure", this.fishidyCatch.lure);
                    if( fishidyCatch.notes != null )
                        jsonParam.put("Description", this.fishidyCatch.notes);
                    if( fishidyCatch.title != null )
                        jsonParam.put("LocationDescription", this.fishidyCatch.title);

                    long dataLength = jsonParam.toString().length();
                    urlConnection.setRequestProperty("Content-Length", "" + dataLength );

                    urlConnection.connect();

                    OutputStream os = urlConnection.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");

                    osw.write(jsonParam.toString());
                    osw.flush();
                    osw.close();

                    int status = urlConnection.getResponseCode();

                    switch (status) {
                        case 200:
                        case 201:
                            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                            String line;
                            while ((line = br.readLine()) != null) {
                                result.append(line + "\n");
                            }
                            br.close();
                            JSONTokener tokener = new JSONTokener(result.toString());
                            JSONObject jsonResult = new JSONObject(tokener);
                            Log.d(TAG, jsonResult.toString());
                            Boolean success = jsonResult.getBoolean("Success");
                            if( success ) {
                                Log.d(TAG, jsonResult.toString());
                                returnResult = true;
                            }
                            break;
                        case 401:
                            Log.d(TAG, "Unauthorized: " + status + " (Token has probably expired)");

                        default:
                            Log.d(TAG, "Unknown status: " + status);
                            errorStatusCode = status;

                    }
                } catch (Exception exc) {
                    Log.e(TAG, "Auth got error: " + exc.getLocalizedMessage());

                } finally {
                    urlConnection.disconnect();
                }

            } catch (Exception e) {
                Log.e(TAG,"AuthManager got error authenticating: " + e );
            }
            return returnResult;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            Log.d(TAG, "AuthManager auth progress: " + progress );
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if( result ) {
                if (delegate != null)
                    delegate.handleUploadSuccess();
            } else {

                if( delegate != null )
                    delegate.handleUploadFailure( errorStatusCode );
            }
            fishidyUploadCatchTask = null;
        }

    }
}

