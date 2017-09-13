package com.reelsonar.ibobber.service;

/**
 * Created by markc on 6/10/16.
 */
public interface FishidyServiceDelegate {

    void handleAuthSuccess();
    void handleAuthFailure();
    void handleUploadSuccess();
    void handleUploadFailure( Integer statusCode );
}
