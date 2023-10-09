package com.example.main;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

import java.io.InputStream;

public class ViewSingleJourney extends AppCompatActivity  {
    private ImageView journeyImg;
    private TextView distanceTV;
    private TextView avgSpeedTV;
    private TextView timeTV;
    private TextView dateTV;
    private TextView ratingTV;
    private TextView commentTV;
    private TextView titleTV;
    private GoogleMap mMap;
    private long journeyID;

    private final Handler h = new Handler();

    // observer is notified when insert or delete in the given URI occurs
    protected class MyObserver extends ContentObserver {

        public MyObserver(Handler handler) {
            super(handler);
        }
        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {

            populateView();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_journey);
        Bundle bundle;
        bundle = getIntent().getExtras();
        commentTV  = findViewById(R.id.ViewSingleJourney_commentText);
        journeyID  = bundle.getLong("journeyID");
        distanceTV = findViewById(R.id.Statistics_recordDistance);
        avgSpeedTV = findViewById(R.id.Statistics_distanceToday);
        timeTV     = findViewById(R.id.Statistics_timeToday);
        dateTV     = findViewById(R.id.ViewSingleJourney_dateText);
        ratingTV   = findViewById(R.id.ViewSingleJourney_ratingText);
        titleTV    = findViewById(R.id.ViewSingleJourney_titleText);


        populateView();
        getContentResolver().registerContentObserver(
                JPC.ALL_URI, true, new MyObserver(h));
    }

    public void onClickEdit(View v) {
        // take to activity for editing the fields for this single journey
        Intent editActivity;
        editActivity = new Intent(ViewSingleJourney.this, EditJourney.class);
        Bundle b;
        b = new Bundle();
        b.putLong("journeyID", journeyID);
        editActivity.putExtras(b);
        startActivity(editActivity);
    }
    public void onClickBack(View v){

        Intent ViewJourney = new Intent(ViewSingleJourney.this, ViewJourneys.class);
        startActivity(ViewJourney);
    }

    public void onClickMap(View v) {

        Intent map;
        map = new Intent(ViewSingleJourney.this, MapsActivity.class);
        Bundle b;
        b = new Bundle();
        b.putLong("journeyID", journeyID);
        map.putExtras(b);
        startActivity(map);
    }

    @SuppressLint("Range")
    private void populateView() {
        // use content provider to load data from the database and display on the text views
        Cursor c;
        c = getContentResolver().query(Uri.withAppendedPath(JPC.JOURNEY_URI,
                journeyID + ""), null, null, null, null);

        if (!c.moveToFirst()) {
            return;
        }
        double distance = c.getDouble(c.getColumnIndex(JPC.J_distance));
        long time       = c.getLong(c.getColumnIndex(JPC.J_DURATION));
        double avgSpeed;
        avgSpeed = 0;

        if(time != 0) {
            avgSpeed = distance / (time / 3600.0);
        }

        long hours;
        hours = time / 3600;
        long minutes;
        minutes = (time % 3600) / 60;
        long seconds;
        seconds = time % 60;
        timeTV.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        distanceTV.setText(String.format("%.2f KM", distance));
        avgSpeedTV.setText(String.format("%.2f KM/H", avgSpeed));


        // date will be stored as yyyy-mm-dd, convert to dd-mm-yyyy
        String date;
        date = c.getString(c.getColumnIndex(JPC.J_DATE));
        String[] dateParts = date.split("-");
        date = dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0];

        dateTV.setText(date);
        titleTV.setText(c.getString(c.getColumnIndex(JPC.J_NAME)));
        ratingTV.setText(c.getInt(c.getColumnIndex(JPC.J_RATING)) + "");
        commentTV.setText(c.getString(c.getColumnIndex(JPC.J_COMMENT)));


        // if an image has been set by user display it, else default image is displayed

        String strUri = c.getString(c.getColumnIndex(JPC.J_IMAGE));
        if(strUri != null) {
            try {
                final Uri imageUri = Uri.parse(strUri);
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                journeyImg.setImageBitmap(selectedImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    }
