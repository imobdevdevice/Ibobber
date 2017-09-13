package com.reelsonar.ibobber.weather;

/**
 * Created by markc on 6/15/16.
 */

import android.content.Context;
import android.util.Log;
import com.hamweather.aeris.communication.*;
import com.hamweather.aeris.communication.loaders.*;
import com.hamweather.aeris.communication.parameter.ParameterBuilder;
import com.hamweather.aeris.communication.parameter.PlaceParameter;
import com.hamweather.aeris.model.*;
import com.hamweather.aeris.response.ObArchiveResponse;
import com.reelsonar.ibobber.R;

import java.util.Date;
import java.util.List;

public class WeatherArchiveService {

    private static final String TAG = WeatherArchiveService.class.getSimpleName();

    public Context _context;

    private WeatherArchiveServiceDelegate delegate;

    public WeatherArchiveService(Context context) {
        this._context = context;

        String id = context.getString(R.string.aeris_client_id);
        String secret = context.getString(R.string.aeris_client_secret);

        AerisEngine.initWithKeys(id, secret, context);
        AerisEngine.getInstance().setDebugLogcatEnabled(true);
    }

    public void setDelegate(WeatherArchiveServiceDelegate delegate) {
        this.delegate = delegate;
    }

    public void loadArchivedWeather( final double latitude, final double longitude, final Date obsRequestDate) {

        PlaceParameter place = new PlaceParameter( latitude, longitude);

        ObservationsArchiveTask task = new ObservationsArchiveTask(_context, new ObservationsArchiveTaskCallback() {
            @Override
            public void onObArchivesLoaded(List<ObArchiveResponse> responses) {

                ObArchiveResponse obResponse = responses.get(0);
                List<ObservationPeriod> periods = obResponse.getPeriods();

                if( periods != null && periods.size() > 0 ) {

                    long smallestDifference = Long.MAX_VALUE;
                    Observation matchingObs = null;
                    for( ObservationPeriod period : periods ) {

                        Date obsDate = new Date( period.ob.timestamp.longValue() * 1000 );
                        long diff =  Math.abs( obsRequestDate.getTime() - obsDate.getTime() );
                        if( diff < smallestDifference ) {
                            smallestDifference = diff;
                            matchingObs = period.ob;
                        }
                    }
                    if( (matchingObs != null) && (WeatherArchiveService.this.delegate != null) )
                        WeatherArchiveService.this.delegate.handleObservationFetchSuccess( matchingObs );
                    else
                        WeatherArchiveService.this.delegate.handleObservationFetchFailure( "No matching weather observation" );
                }
            }

            @Override
            public void onObArchivesFailed(AerisError error) {
                Log.e(TAG, "onObservationsArchiveFailed error: " + error.description);
            }
        } );

        ParameterBuilder builder = new ParameterBuilder().withRadius("500mi");
        task.withDebug(true);
        task.requestClosest(place, builder.build());
    }


    public void test() {

        //PlaceParameter place = new PlaceParameter( 48.764137, -122.480984);  // Bellingham
        // PlaceParameter place = new PlaceParameter( -18.869697, 22.036160 );    // Northwest of Maun, Botswana (near Sepupa)
        // PlaceParameter place = new PlaceParameter( -19.968503, 23.432121 ); // Maun Airport

        // Date obsDate = new Date();

        // loadArchivedWeather( place, obsDate );
    }
}
