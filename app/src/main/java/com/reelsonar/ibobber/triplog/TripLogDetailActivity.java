// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.triplog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.ExpandableListView;

import com.hamweather.aeris.model.Observation;
import com.reelsonar.ibobber.ImageDisplayActivity;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.model.FavoriteFish;
import com.reelsonar.ibobber.model.triplog.LureType;
import com.reelsonar.ibobber.model.triplog.TripLog;
import com.reelsonar.ibobber.model.triplog.TripLogFish;
import com.reelsonar.ibobber.model.triplog.TripLogImages;
import com.reelsonar.ibobber.service.LocationService;
import com.reelsonar.ibobber.triplog.form.TripLogFormAdapter;
import com.reelsonar.ibobber.weather.WeatherArchiveService;
import com.reelsonar.ibobber.weather.WeatherArchiveServiceDelegate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class TripLogDetailActivity extends Activity implements LoaderManager.LoaderCallbacks, WeatherArchiveServiceDelegate {

    private static final String TAG = TripLogDetailActivity.class.getSimpleName();

    private static final int LOADER_TRIP_LOG = 1;
    private static final int LOADER_TRIP_LOG_IMAGES = 2;
    private static final int LOADER_TRIP_LOG_FISH = 3;
    private static final int LOADER_ALL_FISH = 4;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_GALLERY_IMAGE = 2;
    private static final int REQUEST_IMAGE_DELETION = 3;

    private TripLog _tripLog;
    private TripLogImages _tripLogImages;
    private TripLogFish _tripLogFish;
    private List<FavoriteFish> _allFish;
    private TripLogFormAdapter _formAdapter;
    private boolean _isNewTripLog;
    private ExpandableListView _formView;

    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triplog_detail);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        long idTrip = getIntent().getLongExtra("idTrip", -1);
//        long idTrip = 2L;
        if (idTrip != -1) {
            Bundle bundle = new Bundle();
            bundle.putLong("idTrip", idTrip);
            getLoaderManager().initLoader(LOADER_TRIP_LOG, bundle, this);
            getLoaderManager().initLoader(LOADER_TRIP_LOG_IMAGES, bundle, this);
            getLoaderManager().initLoader(LOADER_TRIP_LOG_FISH, bundle, this);
        } else {
            _tripLog = new TripLog();
            _tripLog.setWaterTemp(9999);
            _tripLog.setWaterDepth(9999);
            _tripLog.setAirTemp(9999);
            _tripLogImages = new TripLogImages();
            _tripLogFish = new TripLogFish();
            _isNewTripLog = true;
        }

        WeatherArchiveService weatherArchiveService = new WeatherArchiveService(this);

        weatherArchiveService.test();

        getLoaderManager().initLoader(LOADER_ALL_FISH, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocationService.getInstance(this).setupLocationRequestsIfNeeded();

        if (_isNewTripLog) {
            LocationService.getInstance(this).registerForLocationUpdates(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (_isNewTripLog) {
            LocationService.getInstance(this).unregisterForLocationUpdates(this);
        }
    }

    @Override
    public Loader onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LOADER_TRIP_LOG:
                return TripLogService.getInstance(this).tripLogLoader(args.getLong("idTrip"));
            case LOADER_TRIP_LOG_IMAGES:
                return TripLogService.getInstance(this).tripLogImagesLoader(args.getLong("idTrip"));
            case LOADER_TRIP_LOG_FISH:
                return TripLogService.getInstance(this).tripLogFishLoader(args.getLong("idTrip"));
            case LOADER_ALL_FISH:
                return new FavoriteFishLoader(this);
        }

        throw new IllegalStateException("Got unknown loader id: " + id);
    }

    @Override
    public void onLoadFinished(final Loader loader, final Object data) {
        switch (loader.getId()) {
            case LOADER_TRIP_LOG:
                _tripLog = (TripLog) data;
                // Determine weather airTemp needs to be requested from Aeris (Botswana fish tournament use case)
                if (_tripLog != null) {
                    // _tripLog.setAirTemp( 9999 ); // Debugging ....
                    if (_tripLog.getAirTemp() == 9999) {
                        WeatherArchiveService weatherArchiveService = new WeatherArchiveService(this);
                        weatherArchiveService.setDelegate(this);
                        weatherArchiveService.loadArchivedWeather(_tripLog.getLatitude(), _tripLog.getLongitude(), _tripLog.getDate());
                    }
                }
                break;
            case LOADER_TRIP_LOG_IMAGES:
                _tripLogImages = (TripLogImages) data;
                break;
            case LOADER_TRIP_LOG_FISH:
                _tripLogFish = (TripLogFish) data;
                break;
            case LOADER_ALL_FISH:
                _allFish = (List<FavoriteFish>) data;
                break;
        }

        if (_formAdapter == null && _tripLog != null && _tripLogFish != null && _allFish != null) {
            _formView = (ExpandableListView) findViewById(R.id.formListView);
            _formAdapter = new TripLogFormAdapter(this, _tripLog, _tripLogImages, _tripLogFish, _allFish);
            _formView.setAdapter(_formAdapter);
            _formView.setOnChildClickListener(_formAdapter);
            _formView.setOnGroupClickListener(_formAdapter);
        }
    }

    @Override
    public void onLoaderReset(final Loader loader) {

    }

    public void onEventMainThread(final Location location) {
        _tripLog.setLatitude(location.getLatitude());
        _tripLog.setLongitude(location.getLongitude());
        if (_formAdapter != null) {
            _formAdapter.notifyDataSetChanged();
        }
    }

    public void shareTripLog(View v) {

        String subject = "";
        if (_tripLog.getTitle() != null) {
            if (!_tripLog.getTitle().isEmpty()) {
                subject = _tripLog.getTitle();
            }
        } else {
            subject = getResources().getString(R.string.trip_log_share);
        }

        PackageManager pm = getPackageManager();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");

        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<>();
        for (int i = 0; i < resInfo.size(); i++) {
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;
            if (packageName.contains("twitter") || packageName.contains("facebook") ||
                    packageName.contains("android.talk") || packageName.contains("apps.plus") ||
                    packageName.contains("mms") || packageName.contains("android.gm") ||
                    packageName.contains("email") ||
                    packageName.contains("com.reelsonar.ibobber")  // We serve as the go-tween for the Fishidy service
                    ) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                intent.putExtra(Intent.EXTRA_TEXT, getTripLogShareString(false));

                if (packageName.contains("twitter")) {
                    intent.putExtra(Intent.EXTRA_TEXT, getTripLogShareString(true));
                } else if (packageName.contains("facebook")) {
                    intent = new Intent(this, FacebookActivity.class);
                    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    intent.putExtra(Intent.EXTRA_TEXT, getTripLogShareString(false));
                } else if (packageName.contains("com.reelsonar.ibobber")) {

                    ArrayList<Integer> tripLogFishIds = _tripLogFish.getKeys();

                    if (tripLogFishIds.size() > 0) {

                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-d H:mm");
                        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                        String catchDateString = formatter.format(_tripLog.getDate());
                        intent.putExtra("catch_date", catchDateString);

                        intent.putExtra("latitude", String.valueOf(_tripLog.getLatitude()));
                        intent.putExtra("longitude", String.valueOf(_tripLog.getLongitude()));

                        if (_tripLog.getWaterTemp() != 9999)
                            intent.putExtra("waterTemp", String.format("%.1f", (_tripLog.getWaterTemp() * 1.8) + 32)); // This data is stored metric locally.  Fishidy uses Imperial.
                        if (_tripLog.getWaterDepth() != 9999)
                            intent.putExtra("waterDepth", String.format("%.2f", _tripLog.getWaterDepth() * 3.28));
                        if (_tripLog.getAirTemp() != 9999)
                            intent.putExtra("airTemp", String.format("%.1f", (_tripLog.getAirTemp() * 1.8) + 32));

                        String lure = "Other";
                        LureType lureType = _tripLog.getLureType();
                        if ((lureType != null) && (lureType != LureType.OTHER))
                            lure = getResources().getString(_tripLog.getLureType().getName());
                        intent.putExtra("lure", lure);

                        String notes = _tripLog.getNotes();
                        if (notes != null && notes.length() > 0)
                            intent.putExtra("notes", notes);

                        String title = _tripLog.getTitle();
                        if (title != null && title.length() > 0)
                            intent.putExtra("title", title);

                        ArrayList<String> fishSpeciesList = new ArrayList<String>(tripLogFishIds.size());
                        for (Integer fishId : tripLogFishIds) {
                            fishSpeciesList.add(_tripLogFish.getNameForFishId(fishId));
                        }

                        intent.putStringArrayListExtra("fishList", fishSpeciesList);
                    }
                }

                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }

        Intent openInChooser = Intent.createChooser(intentList.get(0), getResources().getString(R.string.trip_log_share));

        if (intentList.size() > 0) intentList.remove(0);

        LabeledIntent[] extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);

        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        startActivity(openInChooser);
    }

    public void selectImageSource(View v) {

        final CharSequence[] choice = {"Photo Gallery", "Camera"};

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Select Image Source");
        alert.setSingleChoiceItems(choice, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (choice[which] == "Camera") {
                    captureTripLogImage();
                    dialog.dismiss();
                } else if (choice[which] == "Photo Gallery") {
                    selectImageFromGalery();
                    dialog.dismiss();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    public void captureTripLogImage() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

                Log.e("iBobber", "Could not create image file");
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    public void selectImageFromGalery() {

        startActivityForResult(Intent.createChooser(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), "Choose an image"), REQUEST_GALLERY_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            _tripLogImages.addImage(mCurrentPhotoPath);
            _formAdapter.notifyDataSetChanged();
            return;
        }
        if (requestCode == REQUEST_GALLERY_IMAGE && resultCode == RESULT_OK) {

            if ((data != null) && (data.getData() != null)) {

                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {

                    Log.e("iBobber", "Could not create image file");
                    return;
                }
                Uri imageUri = data.getData();
                final int chunkSize = 1024;
                byte[] imageData = new byte[chunkSize];
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = getContentResolver().openInputStream(imageUri);
                    out = new FileOutputStream(photoFile);
                    int bytesRead;
                    while ((bytesRead = in.read(imageData)) > 0) {
                        out.write(Arrays.copyOfRange(imageData, 0, Math.max(0, bytesRead)));
                    }

                    _tripLogImages.addImage(mCurrentPhotoPath);

                    _formAdapter.notifyDataSetChanged();

                } catch (Exception exc) {
                    Log.e("Something went wrong.", exc.toString());
                } finally {
                    try {
                        in.close();
                        out.close();
                    } catch (Exception ioExc) {
                        Log.e(TAG, ioExc.toString());
                    }
                }
            }
            return;
        }
        if (requestCode == REQUEST_IMAGE_DELETION) {

            if (resultCode == RESULT_OK) {
                if ((data != null) && (data.getData() != null)) {
                    int imageId = data.getIntExtra("imageId", -1);
                    _tripLogImages.deleteImageAtIndex(imageId);
                    _formAdapter.notifyDataSetChanged();
                }
            }
            return;
        }
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IBOBBER_" + timeStamp + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageDir = new File(storageDir.toString() + File.separator + "ibobber");
        if (!imageDir.exists()) {
            imageDir.mkdir();
        }
        File imageFile = new File(imageDir.getPath() + File.separator + imageFileName);
        imageFile.createNewFile();
        mCurrentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
    }

    private String getTripLogShareString(boolean twitter) {

        String shareString;
        String header = getResources().getString(R.string.trip_log_share);
        String title = !(_tripLog.getTitle() == null) ? _tripLog.getTitle() : "";
        String lure = !(_tripLog.getLure() == null) ? _tripLog.getLure() : "";
        String notes = !(_tripLog.getNotes() == null) ? _tripLog.getNotes() : "";

        String conditions = "";
        if (_tripLog.getCondition() != null)
            conditions = getResources().getString(_tripLog.getCondition().getName());

        String fishingType = "";
        if (_tripLog.getFishingType() != null)
            fishingType = getResources().getString(_tripLog.getFishingType().getName());

        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this);

        String dateFormatted = dateFormat.format(_tripLog.getDate());
        String timeFormatted = timeFormat.format(_tripLog.getDate());

        final boolean hasLocation = _tripLog.getLatitude() != 0 || _tripLog.getLongitude() != 0;

        String locationAsString = hasLocation ? String.valueOf(_tripLog.getLatitude()) + "," + String.valueOf(_tripLog.getLongitude()) : getResources().getString(R.string.trip_log_unknown_location);

        shareString = header + "\n\n";

        if (!twitter) {
            shareString = shareString + String.format("%s: %s\n", getResources().getString(R.string.trip_log_date), dateFormatted);
            shareString = shareString + String.format("%s: %s\n", getResources().getString(R.string.trip_log_time), timeFormatted);
            shareString = shareString + String.format("%s: %s\n", getResources().getString(R.string.trip_log_location), locationAsString);
        }

        if (!title.isEmpty()) {
            if (!twitter) {
                shareString = shareString + String.format("%s: %s\n", getResources().getString(R.string.trip_log_title), title);
            } else {
                shareString = shareString + String.format("%s\n", title);
            }
        }

        if (!lure.isEmpty()) {
            shareString = shareString + String.format("%s: %s\n", getResources().getString(R.string.trip_log_lure), lure);
        }

        if (!conditions.isEmpty() && !twitter) {
            shareString = shareString + String.format("%s: %s\n", getResources().getString(R.string.trip_log_conditions), conditions);
        }

        if (!fishingType.isEmpty() && !twitter) {
            shareString = shareString + String.format("%s: %s\n", getResources().getString(R.string.trip_log_type_of_fishing), fishingType);
        }

        if (_tripLogFish != null) {
            shareString = shareString + String.format("%s: %d\n", getResources().getString(R.string.trip_log_fish_caught), _tripLogFish.getTotalQuantity());

            SparseIntArray fishArray = _tripLogFish.getFishIdsToQuantities();
            for (int i = 0; i < fishArray.size(); ++i) {
                int quantity = fishArray.valueAt(i);
                if (quantity > 0) {
                    int id = fishArray.keyAt(i);
                    shareString = shareString + String.format("  %d %s\n", quantity, _tripLogFish.getNameForFishId(id));
                }
            }
        }

        if (!notes.isEmpty()) {
            if (!twitter) {
                shareString = shareString + String.format("%s: %s\n", getResources().getString(R.string.trip_log_notes), notes);
            } else {
                shareString = shareString + String.format("%s\n", notes);
            }
        }

        if (!twitter) {
            shareString = shareString + String.format("\nwww.reelsonar.com");
        }

        return shareString;
    }

    public void displayImage(int imageId, String imageFilename) {

        Intent intent = new Intent(this, ImageDisplayActivity.class);
        intent.putExtra(ImageDisplayActivity.IMAGE_FILENAME, imageFilename);
        intent.putExtra(ImageDisplayActivity.IMAGE_ID, imageId);

        startActivityForResult(intent, REQUEST_IMAGE_DELETION);
    }

    /*
     WeatherArchiveServiceDelegate methods
     */
    @Override
    public void handleObservationFetchSuccess(Observation obs) {
        // Log.d(TAG, "Got weather observation:  " + obs.toString() );
        _tripLog.setAirTemp(obs.tempC.doubleValue());
        _formAdapter.notifyDataSetChanged();
    }

    @Override
    public void handleObservationFetchFailure(String errorMessage) {
        Log.e(TAG, errorMessage);
    }

    @Override
    public void onDestroy() {
        TripLogService.getInstance(this).saveTripLog(_tripLog, _tripLogImages, _tripLogFish);
        super.onDestroy();
    }
}
