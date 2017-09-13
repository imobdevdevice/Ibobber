package com.reelsonar.ibobber.triplog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.reelsonar.ibobber.R;

import java.util.ArrayList;

public class ShareActivity extends Activity  {

    private static final String TAG = "ShareActivity";

    String _catchDate;
    String _latitude;
    String _longitude;
    String _waterTemp, _waterDepth, _airTemp;
    String _lure;
    String _notes;
    String _title;
    Button _doneButton;
    private ArrayList<String> _tripLogFishList;

    private ArrayList<String> _fishUploadedList;

    private ListView _fishListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        _tripLogFishList = getIntent().getStringArrayListExtra("fishList");

        if( _tripLogFishList == null || _tripLogFishList.size() == 0  ) {
             Toast toast = Toast.makeText(ShareActivity.this, "No fish have been saved for this Trip.\n\nUnable to sync with Fishidy.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 200);
                    toast.show();
            return;
        }
        _catchDate = getIntent().getStringExtra("catch_date");
        _latitude = getIntent().getStringExtra("latitude");
        _longitude = getIntent().getStringExtra("longitude");
        _waterTemp = getIntent().getStringExtra("waterTemp");
        _waterDepth = getIntent().getStringExtra("waterDepth");
        _airTemp = getIntent().getStringExtra("airTemp");
        _lure = getIntent().getStringExtra("lure");
        _notes = getIntent().getStringExtra("notes");
        _title = getIntent().getStringExtra("title");

        _doneButton = (Button) findViewById(R.id.fishidyShareDoneButton);
        _doneButton.setEnabled(false);

        _fishUploadedList = new ArrayList<>();

        _fishListView = (ListView) findViewById(R.id.sharedFishListView);
        _fishListView.setClickable(true);

        SharedTripAdapter adapter = new SharedTripAdapter(this, _tripLogFishList);
        _fishListView.setAdapter(adapter);

        _fishListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

                if( _fishUploadedList.contains( _tripLogFishList.get( position ) ) ) {
                    Log.d(TAG, "Fish " + _tripLogFishList.get( position ) + " already uploaded");
                    return;
                }

                if( _latitude == null || _longitude ==  null ) {
                    Toast toast = Toast.makeText(ShareActivity.this, "No location information for this Trip.\n\nUnable to sync with Fishidy.", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 200);
                    toast.show();
                    return;
                }

                Intent intent = new Intent(ShareActivity.this, FishidyUploadActivity.class);
                intent.putExtra( "species", _tripLogFishList.get( position ) );
                intent.putExtra( "catch_date", _catchDate );
                intent.putExtra( "latitude", _latitude );
                intent.putExtra( "longitude", _longitude );

                if( _waterTemp != null )
                    intent.putExtra("waterTemp", _waterTemp );
                if( _waterDepth != null )
                    intent.putExtra("waterDepth", _waterDepth );
                if( _airTemp != null )
                    intent.putExtra("airTemp", _airTemp );
                if( _lure != null && _lure.length() > 0 )
                    intent.putExtra( "lure", _lure );
                if( _notes != null && _notes.length() > 0 )
                    intent.putExtra( "notes", _notes );
                if( _title != null && _title.length() > 0 )
                    intent.putExtra( "title", _title );

                startActivityForResult(intent, position );
			}
		});
    }

    public void cancelButtonClicked( final View view ) {

        finish();
    }

    public void doneButtonClicked( final View view ) {

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    // We're piggybacking the uploaded fish on the requestCode

        if (resultCode == RESULT_OK) {
            _fishUploadedList.add( _tripLogFishList.get( requestCode ) );
            _fishListView.invalidateViews();
            Log.d(TAG, "Fish " + requestCode + " upload OK");
            _doneButton.setEnabled(true);
        } else if (resultCode == RESULT_CANCELED ) {
            Log.d(TAG, "Fish " + requestCode + " upload Canceled");
        } else {
            Log.d(TAG, "Fish " + requestCode + " upload failed");
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

    private class SharedTripAdapter extends ArrayAdapter<String> {

        private  SharedTripAdapter(Context context, ArrayList<String> fishList) {
           super(context, 0, fishList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

           String fishName = _tripLogFishList.get( position );

           if (convertView == null) {
              convertView = LayoutInflater.from(getContext()).inflate(R.layout.shared_trip_list_item, parent, false);
           }

           TextView fishNameTextView = (TextView) convertView.findViewById(R.id.lblFishListItem);
           fishNameTextView.setText( fishName );

           ImageView fishUploadStatusImageView = (ImageView) convertView.findViewById(R.id.fishUploadStatusImageView);

            if( _fishUploadedList.contains( fishName ) )
                fishUploadStatusImageView.setImageDrawable( getResources().getDrawable( R.drawable.checked) );

           return convertView;
       }
    }
}
