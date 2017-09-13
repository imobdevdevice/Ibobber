package com.reelsonar.ibobber.weather;

import com.hamweather.aeris.model.Observation;

/**
 * Created by markc on 6/16/16.
 */
public interface WeatherArchiveServiceDelegate {

    public void handleObservationFetchSuccess( Observation obs );
    public void handleObservationFetchFailure( String errorMessage );
}
