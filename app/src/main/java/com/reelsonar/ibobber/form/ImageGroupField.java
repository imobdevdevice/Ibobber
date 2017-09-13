// Copyright (c) 2014 ReelSonar. All rights reserved.

package com.reelsonar.ibobber.form;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.reelsonar.ibobber.R;
import com.reelsonar.ibobber.util.ImageUtil;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

public class ImageGroupField extends FormGroup {

    private Context _context;
    private String _label;
    private android.graphics.drawable.Drawable _icon;
    private ArrayList<String> _imageFilenameList;
    private boolean _showButton;
    private View.OnClickListener _photoClickListener;
    private View.OnClickListener _imageClickListener;

    public ImageGroupField(final Context context, final int label, final ArrayList<String> imageFilenameList, final android.graphics.drawable.Drawable icon, final boolean showButton, final View.OnClickListener photoClickListener, final View.OnClickListener imageClickListener) {
        _context = context;
        _label =  context.getResources().getString(label);
        _imageFilenameList = imageFilenameList;
        _icon = icon;
        _showButton = showButton;
        _photoClickListener = photoClickListener;
        _imageClickListener = imageClickListener;
    }

    @Override
    public int getViewWrapperId() {
        return R.id.formImageButtonWrapper;
    }

    @Override
    public View getGroupView(final boolean isExpanded, final View convertView, final ViewGroup parent) {
        final View view;
        if (convertView != null && convertView.getId() == getViewWrapperId()) {
            view = convertView;
        } else {
            view = LayoutInflater.from(_context).inflate(R.layout.form_imagegroup, null);
        }

        TextView label = (TextView)view.findViewById(R.id.formLabel);
        label.setText(_label);
        label.setTypeface(getTypeface());

        resetImageButtons(view);

        if(_imageFilenameList != null ) {

            if( _imageFilenameList.size() > 0 ) {
                String imageFilename = _imageFilenameList.get(0);
                ImageButton imageButton = (ImageButton) view.findViewById(R.id.tripLogImage0);
                imageButton.setTag( 0 );
                imageButton.setOnClickListener( _imageClickListener );
                configureImageButton(imageFilename, imageButton);
            }
            if( _imageFilenameList.size() > 1 ) {
                String imageFilename = _imageFilenameList.get(1);
                ImageButton imageButton = (ImageButton) view.findViewById(R.id.tripLogImage1);
                imageButton.setTag( 1 );
                imageButton.setOnClickListener( _imageClickListener );
                configureImageButton(imageFilename, imageButton);
            }
            if( _imageFilenameList.size() > 2 ) {
                String imageFilename = _imageFilenameList.get(2);
                ImageButton imageButton = (ImageButton) view.findViewById(R.id.tripLogImage2);
                imageButton.setTag( 2 );
                imageButton.setOnClickListener( _imageClickListener );
                configureImageButton(imageFilename, imageButton);
            }
        }
        ImageButton cameraButton = (ImageButton)view.findViewById(R.id.formImageButton);
        if( _icon != null )
            cameraButton.setImageDrawable( _icon );
        if (_showButton) {
            cameraButton.setVisibility(View.VISIBLE);
            cameraButton.setOnClickListener(_photoClickListener);
        } else {
            cameraButton.setVisibility(View.GONE);

        }

        return view;
    }

    void resetImageButtons(View view) {
        ImageButton imageButton = (ImageButton) view.findViewById(R.id.tripLogImage0);
        imageButton.setImageResource(R.drawable.image_placeholder);
        imageButton.setTag(-1);

        imageButton = (ImageButton) view.findViewById(R.id.tripLogImage1);
        imageButton.setImageResource(R.drawable.image_placeholder);
        imageButton.setTag(-1);

        imageButton = (ImageButton) view.findViewById(R.id.tripLogImage2);
        imageButton.setImageResource(R.drawable.image_placeholder);
        imageButton.setTag(-1);
    }

    void configureImageButton( final String fileName, final ImageButton imageButton ) {

        try {
            final float THUMBNAIL_PCT = 0.25f;
            imageButton.setImageDrawable(null);

            int rotation = ImageUtil.getCameraPhotoOrientation( fileName );

            Bitmap bitmap = BitmapFactory.decodeFile( fileName );
            Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * THUMBNAIL_PCT), (int) (bitmap.getHeight() * THUMBNAIL_PCT), false);

            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotatedThumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true);

            bitmap = null;
            imageButton.setImageBitmap(rotatedThumbnail);
            thumbnail = null;
            rotatedThumbnail = null;
        } catch (Exception exc) {

            Log.e("iBobber", "ImageButtonField could not set image from file: " + fileName + "\n" + exc.toString());
        }
    }
}
