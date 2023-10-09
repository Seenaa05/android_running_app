package com.example.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import java.io.InputStream;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.content.ContentValues;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.app.AlertDialog;


public class EditJourney extends AppCompatActivity {
    private final int RESULT_LOAD_IMG = 1;

    private ImageView journeyImg;
    private EditText titleET;
    private EditText temp;
    private EditText commentET;
    private EditText ratingET;
    private long journeyID;

    private Uri selectedJourneyImg;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_journey);

        Bundle bundle = getIntent().getExtras();
        journeyID = bundle.getLong("journeyID");
        titleET = findViewById(R.id.titleEditText);
        ratingET = findViewById(R.id.ratingEditText);
        commentET = findViewById(R.id.commentEditText);
        journeyImg = findViewById(R.id.journeyImg);

        selectedJourneyImg = null;

        populateEditText();
    }

    /* Save the new title, comment, image and rating to the DB */
    public void onClickSave(View v) {
       int rating = Integer.parseInt(ratingET.getText().toString());

        Uri rowQueryUri = Uri.withAppendedPath(JPC.JOURNEY_URI, "" + journeyID);

        ContentValues cv = new ContentValues();
        cv.put(JPC.J_NAME, titleET.getText().toString());
        cv.put(JPC.J_RATING, rating);
        if(selectedJourneyImg != null) {
            cv.put(JPC.J_IMAGE, selectedJourneyImg.toString());
        }
        cv.put(JPC.J_COMMENT, commentET.getText().toString());

        try {
            getContentResolver().update(rowQueryUri, cv, null, null);
            finish();
        }catch (Exception e){
            Log.e("Error","failed to save");
        }
    }


    public void onClickChangeImage(View v) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        startActivityForResult(intent, RESULT_LOAD_IMG);

    }


    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        // get the URI from the selected image
        if (reqCode == RESULT_LOAD_IMG) {
            if (resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();

                    Log.d("URI","Adding Uri" );
                    getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    selectedJourneyImg = imageUri;
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    journeyImg.setImageBitmap(selectedImage);


                } catch (Exception e) {
                    Log.d("Error","Error uri image" );

                }

            } else {
                Toast.makeText(EditJourney.this, "You didn't pick an Image", Toast.LENGTH_LONG).show();
            }
        }
    }

    /* Give the edit texts some initial text from what they were, get this by accessing DB */
    @SuppressLint("Range")
    private void populateEditText() {
        @SuppressLint("Recycle") Cursor c = getContentResolver().query(Uri.withAppendedPath(JPC.JOURNEY_URI,
                journeyID + ""), null, null, null, null);

        if(c.moveToFirst()) {
            try {
                ratingET.setText(c.getString(c.getColumnIndex(JPC.J_RATING)));
                titleET.setText(c.getString(c.getColumnIndex(JPC.J_NAME)));
                commentET.setText(c.getString(c.getColumnIndex(JPC.J_COMMENT)));
            }catch(Exception e){
                Log.d("error","Null populate text");
            }


            // if an image has been set by user display it, else default image is displayed
            String imageSet = c.getString(c.getColumnIndex(JPC.J_IMAGE));
            if (imageSet == null) {
                return;
            }
            try {
                final Uri imageUri = Uri.parse(imageSet);
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                Log.d("image","default image set" );
                journeyImg.setImageBitmap(selectedImage);
            } catch (Exception e) {
                Log.d("error","Error getting image ");
            }
        }
    }
    public void onClickBack(View v){
        Bundle b = new Bundle();
        b.putLong("journeyID", journeyID);
        Intent ViewSingle = new Intent(EditJourney.this, ViewSingleJourney.class);
        ViewSingle.putExtras(b);
        startActivity(ViewSingle);
    }
    public void onClickDelete(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete");
        builder.setMessage("Are you sure you want to do this?");
        builder.setPositiveButton("YES", (dialog, which) -> {
            try {
                long id = journeyID;
                Uri uri = ContentUris.withAppendedId(JPC.JOURNEY_URI, id);
                getContentResolver().delete(uri, null, null);
                Intent main = new Intent(EditJourney.this, MainActivity.class);
                startActivity(main);
                Log.d("Success"," removed uri");
            }catch (Exception e){
                Log.e("Error","Error remove uri");
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();


    }

}

