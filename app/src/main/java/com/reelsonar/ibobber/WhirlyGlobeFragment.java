package com.reelsonar.ibobber;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;
import com.mousebird.maply.*;
import com.reelsonar.ibobber.service.ActiveUsersService;
import com.reelsonar.ibobber.service.UserService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class WhirlyGlobeFragment extends GlobeMapFragment implements ActiveUsersService.ActiveUsersServiceDelegate {

    String TAG = "WhirlyGlobeFragment";

    ActiveUsersService activeUsersService;
    ArrayList<ActiveUsersService.UserLocation> userLocations;
    Timer timer;

    MaplyTexture textures[] = new MaplyTexture[7];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle inState) {
        super.onCreateView(inflater, container, inState);

        // Do app specific setup logic.

        userLocations = new ArrayList<ActiveUsersService.UserLocation>();

        Bitmap locationIcon0 = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.user_location_0 );
        Bitmap locationIcon1 = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.user_location_1 );
        Bitmap locationIcon2 = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.user_location_2 );
        Bitmap locationIcon3 = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.user_location_3 );

        textures[0] = globeControl.addTexture(locationIcon0,new MaplyBaseController.TextureSettings(), MaplyBaseController.ThreadMode.ThreadCurrent);
        textures[1] = globeControl.addTexture(locationIcon1,new MaplyBaseController.TextureSettings(), MaplyBaseController.ThreadMode.ThreadCurrent);
        textures[2] = globeControl.addTexture(locationIcon2,new MaplyBaseController.TextureSettings(), MaplyBaseController.ThreadMode.ThreadCurrent);
        textures[3] = globeControl.addTexture(locationIcon3,new MaplyBaseController.TextureSettings(), MaplyBaseController.ThreadMode.ThreadCurrent);
        textures[4] = globeControl.addTexture(locationIcon2,new MaplyBaseController.TextureSettings(), MaplyBaseController.ThreadMode.ThreadCurrent);
        textures[5] = globeControl.addTexture(locationIcon1,new MaplyBaseController.TextureSettings(), MaplyBaseController.ThreadMode.ThreadCurrent);
        textures[6] = globeControl.addTexture(locationIcon0,new MaplyBaseController.TextureSettings(), MaplyBaseController.ThreadMode.ThreadCurrent);

        return baseControl.getContentView();
    }

    @Override
    protected MapDisplayType chooseDisplayType() {
        return MapDisplayType.Globe;
    }

    @Override
    protected void controlHasStarted() {

        String cacheDirName = "nasa_tiles";
        File cacheDir = new File(getActivity().getCacheDir(), cacheDirName);
        cacheDir.mkdir();

        RemoteTileSource remoteTileSource = new RemoteTileSource(new RemoteTileInfo("https://tileserver.maptiler.com/nasa/", "png", 0, 18));
        remoteTileSource.setCacheDir(cacheDir);
        SphericalMercatorCoordSystem coordSystem = new SphericalMercatorCoordSystem();

        QuadImageTileLayer baseLayer = new QuadImageTileLayer(globeControl, coordSystem, remoteTileSource);
        baseLayer.setImageDepth(1);
        baseLayer.setSingleLevelLoading(false);
        baseLayer.setUseTargetZoomLevel(false);
        baseLayer.setCoverPoles(true);
        baseLayer.setHandleEdges(true);

        // add layer and position
        globeControl.addLayer(baseLayer);
        globeControl.setKeepNorthUp( true );
        globeControl.setAutoRotate( 0.1f, -5f );

        Point2d loc = Point2d.FromDegrees(-100.45, 30.5023056);
        globeControl.animatePositionGeo(loc.getX(), loc.getY(), 0.68, 1.0);

        addStars();

        Toast.makeText(getActivity(), "Retrieving Active iBobber Users", Toast.LENGTH_SHORT).show();

        activeUsersService = new ActiveUsersService();
        activeUsersService.setDelegate( this );

        activeUsersService.fetchActiveUsers();

        timer = new Timer();

        timer.schedule( new TimerTask()
            {
                public void run()
                {
                    activeUsersService.fetchActiveUsers();
                    Log.d(TAG, "Checking user locations ...");
                }
            }, 5000, 5000);
    }

    private void insertLocationMarkers( ArrayList<ActiveUsersService.UserLocation> locationList ) {

        Point2d markerSize = new Point2d(0.05, 0.05);
        ArrayList<Marker> markers = new ArrayList<Marker>();

        for(ActiveUsersService.UserLocation location : locationList ) {

            if( this.userLocations.contains( location ) )
                continue;

            userLocations.add( location );

            Marker marker = new Marker();
            marker.loc = Point2d.FromDegrees(location.longitude, location.latitude);
            marker.images= textures;
            marker.size = markerSize;
            marker.period = 0.5;

            markers.add( marker );
        }

        if( markers.size() > 0 ) {
            MarkerInfo markerInfo = new MarkerInfo();
            try {
                ComponentObject componentObject = globeControl.addMarkers(markers, markerInfo, MaplyBaseController.ThreadMode.ThreadAny);
            } catch ( Exception exc ) {
                Log.e(TAG, "insertLocationMarkers() got exception: " + exc.toString() );
            }
        }
    }

    private void addStars() {

        try {
            MaplyStarModel starModel = new MaplyStarModel( "starcatalog_orig.txt", "star_background.png", getActivity() );
            starModel.addToViewc( globeControl, MaplyBaseController.ThreadMode.ThreadAny);
        } catch ( IOException exc) {
            Log.e("WhirlyGlobeFragment", "Got error adding stars");
        }
    }

    @Override
    public void onStop() {

        if( timer != null ) {
            timer.cancel();
            timer = null;
        }
        super.onStop();
    }

    /*  ActiveUsersServiceDelegate methods */

    @Override
    public void handleDataReady(ArrayList<ActiveUsersService.UserLocation> locationData ) {

        if( locationData != null && locationData.size() > 0 )
            insertLocationMarkers( locationData );
    }

    @Override
    public void handleFailure() {
        Log.w(TAG, "Got data failure from ActiveUsersService");
    }
}
