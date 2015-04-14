package com.alabama.bamboofinder;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ObservationDetailActivity extends ActionBarActivity {
    private static final String TAG = "ObservationDetail";

    public static final String EXTRA_OBSERVATION = "observation";
    public static final String EXTRA_USER_LATITUDE = "latitude";
    public static final String EXTRA_USER_LONGITUDE = "longitude";

    private static final int CAMERA_REQUEST = 1888;
    private ImageView mImageView;
    private EditText mSpeciesText;
    private EditText mDescriptionText;
    private Button mSaveButton;
    private Button mCancelButton;
    private Button mValidateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation_detail);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);

        mDescriptionText = (EditText) findViewById(R.id.descriptionEditText);

        mCancelButton = (Button) findViewById(R.id.cancelButton);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //return to calling class with resultCode = RESULT_CANCELED
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        mSaveButton = (Button) findViewById(R.id.saveButton);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                Observation observation = new Observation();
                double latitude = intent.getDoubleExtra(EXTRA_USER_LATITUDE, -1);
                double longitude = intent.getDoubleExtra(EXTRA_USER_LONGITUDE, -1);
                LatLng location = new LatLng(latitude, longitude);

                observation.setLocation(location);
                observation.setSpeciesGuess(mSpeciesText.getText().toString());
                observation.setDescription(mDescriptionText.getText().toString());
                //set observation TimeStamp
                //set observation username?

                //if editing observation

                    //update observation on iNaturalist

                //if adding observation

                    //POST API call
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_observation_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.home:
                finish();
                return true;
            case R.id.menu_item_new_picture:
                mImageView = (ImageView)findViewById(R.id.observationImage);
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                bitmap = Bitmap.createScaledBitmap(bitmap, 864, 486, true);

                //Retrieve last image taken
                String[] projection = new String[]{
                        MediaStore.Images.ImageColumns._ID,
                        MediaStore.Images.ImageColumns.DATA,
                        MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                        MediaStore.Images.ImageColumns.DATE_TAKEN,
                        MediaStore.Images.ImageColumns.MIME_TYPE
                };
                final Cursor cursor = getContentResolver()
                        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                                null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
                if (cursor.moveToFirst()) {
                    String imageLocation = cursor.getString(1);
                    File imageFile = new File(imageLocation);
                    ExifInterface exifInterface = new ExifInterface(imageFile.getAbsolutePath());

                    //check orientation and rotate if needed
                    Matrix matrix = new Matrix();
                    float rotate = 0;
                    int orientation = exifInterface.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotate = 270;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotate = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotate = 90;
                            break;
                    }
                    matrix.postRotate(rotate);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                            bitmap.getHeight(), matrix, true);


                    mImageView.setImageBitmap(rotatedBitmap);
                }
            }
            catch (Exception e) {
                Log.e("Result Exception", e.toString());
            }
        }
    }

    class PostObservationsTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... objects) {
            // params are the observation, token, and photo file name
            Log.d(TAG, "in doInBackground");
            ApiManager.uploadObservation((Observation)objects[0], (String)objects[1], (String)objects[2]);
            return null;
        }
    }
}
