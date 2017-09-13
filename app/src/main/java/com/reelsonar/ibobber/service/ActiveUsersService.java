package com.reelsonar.ibobber.service;

/**
 * Created by markc on 11/10/16.
 */

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class ActiveUsersService {

    String TAG = "ActiveUsersService";

    private ActiveUsersServiceDelegate delegate;

	private ActiveUsersFetchTask activeUsersFetchTask;

	String webService = "http://reelsonar-services.com:8083/fetch_analytics";

	int timeoutConnection = 3000, timeoutSocket =  5000;

    public void setDelegate(ActiveUsersServiceDelegate delegate) {
        this.delegate = delegate;
    }

	private class ActiveUsersFetchTask extends AsyncTask<String, Integer, Boolean> {

		ArrayList<UserLocation> locationList;

		@Override
		protected Boolean doInBackground(String... sUrl) {
			try {
				Log.d(TAG, "ActiveUsersFetchTask.doInBackground() ...");

				HttpParams httpParameters = new BasicHttpParams();

				HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);

				HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

				HttpClient httpClient = new DefaultHttpClient(httpParameters);
				HttpContext localContext = new BasicHttpContext();

				HttpResponse response = null;
				HttpGet request;

				request = new HttpGet( webService );

				request.setHeader("Accept", "application/json");

				try {
					response = httpClient.execute(request, localContext);
					if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {

						ByteArrayOutputStream os = new ByteArrayOutputStream();

						response.getEntity().writeTo(os);

						String responseStr = os.toString();

						try {

							JSONObject jsonResult = new JSONObject( responseStr );

							String locations = jsonResult.getString( "user_locations" );

							if( locations.length() > 0 ) {

								locationList = new ArrayList<UserLocation>(locations.length());

								JSONArray locationArray = new JSONArray( locations );

								for( int i = 0; i < locationArray.length(); i++ ) {

									JSONArray locationItem = locationArray.getJSONArray(i);

									double latitude  = Double.parseDouble( locationItem.getString(0) );
									double longitude = Double.parseDouble( locationItem.getString(1) );
									if( latitude == 0 && longitude == 0 )  // Ignore locations with lat == 0, lon == 0
										continue;

									UserLocation userLocation = new UserLocation( latitude, longitude );
									locationList.add( userLocation );
								}

								return true;

							}  else {

								return false;
							}

						} catch (JSONException e) {

							Log.e(TAG, e.toString());
							e.printStackTrace();
						}
					} else {

						Log.e(TAG, "AuthManager got a bad status code from web service.");

						return false;
					}

				} catch( SocketTimeoutException timeoutExc ) {

					Log.e( TAG, "Got socket time out error: " + timeoutExc.toString() );
					return false;

				} catch( ConnectTimeoutException connectTimeoutExc ) {

					Log.e( TAG, "Got connect time out error: " + connectTimeoutExc.toString() );
					return false;

				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}

			} catch (Exception e) {
				Log.e(TAG,"Got error: " + e );
				return false;
			}
			return true;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			Log.d(TAG, "Progress: " + progress );
		}

		@Override
		protected void onPostExecute(final Boolean success) {

			activeUsersFetchTask = null;

			if(success && locationList != null && locationList.size() > 0 )
				delegate.handleDataReady( locationList ); //  .handleEventUpdate( locationData.toString() );
			else
				delegate.handleFailure();
		}
	}


	public void fetchActiveUsers() {

		activeUsersFetchTask = new ActiveUsersFetchTask();
		activeUsersFetchTask.execute();
	}

    public interface ActiveUsersServiceDelegate {

    void handleDataReady( ArrayList<UserLocation> data );
    void handleFailure();
	}

	public class UserLocation {

		public double latitude, longitude;

		public UserLocation( double latitude, double longitude ) {
			this.latitude = latitude;
			this.longitude = longitude;
		}

		@Override
		public boolean equals(Object object) {

			boolean sameSame = false;

			if (object != null && object instanceof UserLocation) {

				if( (this.latitude == ((UserLocation) object).latitude) && (this.longitude == ((UserLocation) object).longitude) )
					sameSame = true;
			}
			return sameSame;
		}
	}


}