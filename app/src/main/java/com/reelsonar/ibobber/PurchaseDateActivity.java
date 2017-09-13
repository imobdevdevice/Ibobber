package com.reelsonar.ibobber;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import com.reelsonar.ibobber.bluetooth.BTService;
import com.reelsonar.ibobber.service.UserService;

import java.util.Calendar;
import java.util.Date;

public class PurchaseDateActivity extends Activity {

    private static final String TAG = "PurchaseDateActivity";

    private  DatePicker datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_date);

        datePicker = (DatePicker) findViewById(R.id.purchaseDatePicker);
        datePicker.setMaxDate( new Date().getTime() );

    }

    public void doneButtonClicked( final View view ) {

        Calendar newDate = Calendar.getInstance();
        newDate.set( datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        Date selectedDate = newDate.getTime();

        BTService.getSingleInstance().setDatePurchased( selectedDate );
        UserService.getInstance(this).persistBobberInfo();

        finish();
    }

     public void cancelButtonClicked( final View view ) {

         finish();
    }
}
