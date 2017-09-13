package com.reelsonar.ibobber;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.reelsonar.ibobber.util.ImageUtil;

import java.io.File;


public class ImageDisplayActivity extends Activity {

    public final static String IMAGE_FILENAME = "com.reelsonar.ibobber.imagedisplay.IMAGE_FILENAME";
    public final static String IMAGE_ID       = "com.reelsonar.ibobber.imagedisplay.IMAGE_ID";

    String _imageFilename;
    int _imageId;

    final float IMAGE_SCALE_PCT = 0.50f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);
        Intent intent = getIntent();
        _imageFilename = intent.getStringExtra(IMAGE_FILENAME);
        _imageId = getIntent().getIntExtra(IMAGE_ID, -1);

        Boolean errorDetected = false;

        if( _imageFilename != null && !_imageFilename.isEmpty() ) {

            ImageView imageView = (ImageView) findViewById(R.id.imageDisplayImage);

            try {
                Bitmap bitmap = BitmapFactory.decodeFile(_imageFilename);

                int rotation = ImageUtil.getCameraPhotoOrientation( _imageFilename );
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                Bitmap rotatedThumbnail = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                bitmap = null;
                imageView.setImageBitmap(rotatedThumbnail);

            } catch (RuntimeException exc) {
                errorDetected = true;
            }
        } else {
            errorDetected = true;
        }

        if( errorDetected ) {
            Log.e("iBobber", "ImageDisplayActivity could not load image: " + _imageFilename );
            Toast.makeText(this, "Cannot display image.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void shareImage(View v) {

        if(_imageFilename == null)
            return;

        PackageManager pm = getPackageManager();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        File f = new File(_imageFilename);
        Uri contentUri = Uri.fromFile(f);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.setType("image/jpeg");

        Intent openInChooser = Intent.createChooser(shareIntent, "Share iBobber Trip Log Photo");

        startActivity(openInChooser);
    }

    public void deleteImage(View v) {

        if(_imageFilename == null)
            return;

        File f = new File(_imageFilename);
        final Uri contentUri = Uri.fromFile(f);

        AlertDialog.Builder alert = new AlertDialog.Builder(
                ImageDisplayActivity.this);
        alert.setTitle("Delete Trip Log Photo");
        alert.setMessage("Are you sure?");
        alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent result = new Intent();
                result.setData(contentUri);
                result.putExtra("imageId", _imageId  );
                setResult(Activity.RESULT_OK, result);
                finish();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_display, menu);
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
}
