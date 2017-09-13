package com.reelsonar.ibobber.triplog;

import android.app.Activity;


import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.service.*;

public class FishidyUploadActivity extends Activity implements FishidyServiceDelegate {

    private static final String TAG = "FishidyUploadActivity";

    private static final String KEY_FISHIDY_AUTH_TOKEN = "fishidyAuthToken";

    private EditText _loginEditText, _passwordEditText;
    private View _authView;
    private ListView _fishListView;
    private EditText _lengthEditText, _weightEditText, _weightOuncesEditText;

    private View _metricDataView, _imperialDataView;

    private Spinner _sharingPrivacySpinner;
    private Button _syncButton;
    private ProgressBar _syncProgressIndicator;

    private Boolean _authenticated = false;

    private FishidyService _fishidyService;

    private String _fishSpecies;
    private String _catchDate;
    private String _latitude, _longitude;
    private String _waterTemp, _waterDepth, _airTemp;
    private String _lure;
    private String _notes;
    private String _title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fishidy_upload);

        _fishSpecies = getIntent().getStringExtra("species");
        _catchDate = getIntent().getStringExtra("catch_date");
        _latitude =  getIntent().getStringExtra("latitude");
        _longitude =  getIntent().getStringExtra("longitude");
        _waterTemp = getIntent().getStringExtra("waterTemp");
        _waterDepth = getIntent().getStringExtra("waterDepth");
        _airTemp = getIntent().getStringExtra("airTemp");
        _lure = getIntent().getStringExtra("lure"); // Todo:  handle null lure
        _notes = getIntent().getStringExtra("notes");
        _title = getIntent().getStringExtra("title");

        TextView fishTextView = (TextView) findViewById(R.id.fishidySpeciesTextview);
        fishTextView.setText( _fishSpecies );

        _sharingPrivacySpinner = (Spinner) findViewById(R.id.fishidySharingPrivacySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.fishidy_sharing_scope_array, android.R.layout.simple_list_item_1);

        _sharingPrivacySpinner.setAdapter(adapter);

        _syncProgressIndicator = (ProgressBar)findViewById(R.id.fishidySyncProgressBar);

        _authView = findViewById(R.id.authLoginView);
        _loginEditText = (EditText) findViewById(R.id.loginEditText);
        _passwordEditText = (EditText) findViewById(R.id.passwordEditText);

        _metricDataView = (View) findViewById(R.id.metricDataLinearLayout);

        _imperialDataView = (View) findViewById(R.id.imperialDataLinearLayout);

        if( UserService.getInstance(this).isMetric() ) {

            _metricDataView.setVisibility(View.VISIBLE);
            _imperialDataView.setVisibility(View.GONE);

            _lengthEditText = (EditText) findViewById(R.id.lengthCentimetersEditText);
            _weightEditText = (EditText) findViewById(R.id.weightKilogramsEditText);

        } else {
            _imperialDataView.setVisibility(View.VISIBLE);
             _metricDataView.setVisibility(View.GONE);

            _lengthEditText = (EditText) findViewById(R.id.lengthInchesEditText);
            _weightEditText = (EditText) findViewById(R.id.weightPoundsEditText);
            _weightOuncesEditText= (EditText) findViewById(R.id.weightOuncesEditText);

            _weightOuncesEditText.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                    if( (s.length() > 0) && _lengthEditText.getText().length() > 0 ) {
                        _syncButton.setEnabled(true);
                    } else {
                        _syncButton.setEnabled(false);
                    }
                }
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            });
        }

        _weightEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if( (s.length() > 0) && _lengthEditText.getText().length() > 0 ) {
                    _syncButton.setEnabled(true);
                } else {
                    _syncButton.setEnabled(false);
                }
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        });

        _lengthEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if( (s.length() > 0) && _weightEditText.getText().length() > 0 ) {
                    _syncButton.setEnabled(true);
                } else {
                    _syncButton.setEnabled(false);
                }
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        });

        _syncButton = (Button) findViewById(R.id.fishidySyncButton );
        _syncButton.setEnabled(false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String authToken = prefs.getString(KEY_FISHIDY_AUTH_TOKEN, null);
        if( authToken != null )
            _authenticated = true;

        _fishidyService = FishidyService.getInstance(this);
        _fishidyService.setDelegate( this );
    }

    public void authButtonClicked( final View view ) {
        FishidyAuthData authData = new FishidyAuthData();
        authData.setLogin( _loginEditText.getText().toString() );
        authData.setPassword( _passwordEditText.getText().toString() );

        _syncProgressIndicator.setVisibility(View.VISIBLE);

        _fishidyService.authenticate( authData);
    }

    public void authCancelButtonClicked( final View view ) {
        _authView.setVisibility(View.INVISIBLE);
    }

    public void syncButtonClicked( final View view ) {

        FishidyCatch fishidyCatch = new FishidyCatch();

        fishidyCatch.setSpecies( _fishSpecies );
        fishidyCatch.setCatchDateTime( _catchDate );
        fishidyCatch.setSpecies( _fishSpecies );
        fishidyCatch.setLatitude( _latitude );
        fishidyCatch.setLongitude( _longitude );

        if( UserService.getInstance(this).isMetric() ) {

            String centimetersValue = _lengthEditText.getText().toString();
            if( centimetersValue != null && !centimetersValue.isEmpty()) {
                double centimeters = Double.valueOf( centimetersValue) ;
                double inches = centimeters / 2.54;
                fishidyCatch.setLength("".valueOf(inches));
            }
            String kilogramsValue = _weightEditText.getText().toString();
            if( kilogramsValue != null && !kilogramsValue.isEmpty()) {
                double kilograms = Double.valueOf(kilogramsValue);
                double ounces = (kilograms * 2.2) * 16;
                fishidyCatch.setWeight("".valueOf(ounces));
            }

        } else {

            fishidyCatch.setLength( _lengthEditText.getText().toString() );

            double ounces = 0;
            String ouncesValue = _weightOuncesEditText.getText().toString();
            if( ouncesValue != null && !ouncesValue.isEmpty())
                ounces = Double.valueOf( ouncesValue );

            String poundsValue = _weightEditText.getText().toString();
            if( poundsValue != null && !poundsValue.isEmpty()) {
                double pounds = Double.valueOf( poundsValue );
                ounces += pounds * 16;
            }

            fishidyCatch.setWeight( "".valueOf( ounces ) );
        }

        if( _waterTemp != null)
            fishidyCatch.setWaterTemp(_waterTemp);
        if( _waterDepth != null)
            fishidyCatch.setWaterDepth(_waterDepth);
        if( _airTemp != null)
            fishidyCatch.setAirTemp(_airTemp);

        int privacyCode = _sharingPrivacySpinner.getSelectedItemPosition();
        if( privacyCode == 2) privacyCode = 3;  // Fishidy PrivacyType values are {0,1,3}  (Kind of curious where 2 went)
        fishidyCatch.setPrivacyCode( privacyCode );

        if( _lure != null )
            fishidyCatch.setLure( _lure );
        if( _notes != null )
            fishidyCatch.setNotes( _notes );
        if( _title != null )
            fishidyCatch.setTitle( _title );

        if( !_authenticated ) {
            _authView.setVisibility(View.VISIBLE);
            return;
        }

        _syncProgressIndicator.setVisibility(View.VISIBLE);

        _fishidyService.uploadCatch( fishidyCatch );
    }

    public void cancelButtonClicked( final View view ) {

        _syncProgressIndicator.setVisibility(View.GONE);

        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED,returnIntent);
        finish();
    }

    /** FishidyServiceDelegate Methods **/
    @Override
    public void handleAuthSuccess() {

        _syncProgressIndicator.setVisibility(View.GONE);

        _authenticated = true;

        _authView.setVisibility(View.INVISIBLE);
         Toast toast = Toast.makeText(this, "Fishidy Authorized.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 200);
            toast.show();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(_passwordEditText.getWindowToken(), 0);
    }
    @Override
    public void handleAuthFailure() {

        _syncProgressIndicator.setVisibility(View.GONE);

        _authenticated = false;

        Toast toast = Toast.makeText(this, "Authentication Failed", Toast.LENGTH_LONG );
        toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 200);
        toast.show();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(_passwordEditText.getWindowToken(), 0);
    }

    @Override
    public void handleUploadSuccess() {

        _syncProgressIndicator.setVisibility(View.GONE);

        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
    @Override
    public void handleUploadFailure( Integer statusCode ) {

        _syncProgressIndicator.setVisibility(View.GONE);

        if( (statusCode != null) && (statusCode == 401) ) {
            Toast toast = Toast.makeText(this, "Fishidy Auth Expired.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 200);
            toast.show();
            _authenticated = false;
            _authView.setVisibility(View.VISIBLE);
        } else {
            Toast toast = Toast.makeText(this, "Fishidy Sync Failed.  Check network connection.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 200);
            toast.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        _syncProgressIndicator.setVisibility(View.GONE);
        super.onDestroy();
    }
}
