package com.alabama.bamboofinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
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
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.Date;

public class ObservationDetailActivity extends ActionBarActivity {
    private static final String TAG = "ObservationDetail";

    public static final String EXTRA_OBSERVATION = "observation";
    public static final String EXTRA_USER_LATITUDE = "latitude";
    public static final String EXTRA_USER_LONGITUDE = "longitude";
    public static final String BASE_URL = "www.inaturalist.org";
    private static final int ADD_OBSERVATION = 0;
    private static final int EDIT_OBSERVATION = 1;
    private static final int CAMERA_REQUEST = 1888;

    private static Uri imageUri;
    private static int mMode;
    private ImageView mImageView;
    private EditText mSpeciesText;
    private EditText mDescriptionText;
    private Button mSaveButton;
    private Button mCancelButton;
    private Button mValidateButton;
    private ApiManager api;
    private Observation mObservation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation_detail);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);

        mDescriptionText = (EditText) findViewById(R.id.descriptionEditText);
        mSpeciesText = (EditText) findViewById(R.id.speciesEditText);
        mImageView = (ImageView)findViewById(R.id.observationImage);
        api = new ApiManager();

        //Check if adding or editing
        Intent i = getIntent();
        double latitude = i.getDoubleExtra(EXTRA_USER_LATITUDE, -1);
        if(latitude == -1) {
            mMode = EDIT_OBSERVATION;

            mObservation = (Observation) i.getSerializableExtra(EXTRA_OBSERVATION);
            if(!mObservation.getMediumUrl().equals("")) {
                Picasso.with(getApplicationContext())
                        .load(mObservation.getMediumUrl())
                        .resize(864, 486)
                        .centerCrop()
                        .into(mImageView);
            } else {
                Log.e("ImageView Error", "Observation created without a picture");
                mImageView.setVisibility(View.GONE); // Remove the imageView if there is no picture for it
            }
            mSpeciesText.setText(mObservation.getSpeciesGuess());
            mDescriptionText.setText(mObservation.getDescription());
        }
        else
            mMode = ADD_OBSERVATION;


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

                switch(mMode) {
                    case(EDIT_OBSERVATION):
                        //TODO update observation fields in the intent.
                        break;
                    case(ADD_OBSERVATION):
                        Observation o = new Observation();
                        double latitude = intent.getDoubleExtra(EXTRA_USER_LATITUDE, -1);
                        double longitude = intent.getDoubleExtra(EXTRA_USER_LONGITUDE, -1);
                        LatLng location = new LatLng(latitude, longitude);
                        o.setLocation(location);
                        o.setSpeciesGuess(mSpeciesText.getText().toString());
                        o.setDescription(mDescriptionText.getText().toString());
                        o.setTimeStamp(new Date());

                        SharedPreferences prefs = ObservationDetailActivity.this.getSharedPreferences(
                                "com.alabama.bamboofinder", Context.MODE_PRIVATE);
                        String token = prefs.getString("token", "Empty Token");
                        //AsyncTask postObservation = new PostObservationsTask().execute(
                        //        mObservation, token, imageUri.toString());
                        setResult(RESULT_OK);
                        finish();
                }
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
                //finish();
                return true;
            case R.id.menu_item_new_picture:
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            imageUri = data.getData();
            Log.i("Result Image URI", imageUri.toString());
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
