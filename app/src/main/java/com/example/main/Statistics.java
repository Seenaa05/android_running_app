package com.example.main;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Statistics extends AppCompatActivity {


    private TextView recordDistance;
    private TextView distanceToday;
    private TextView timeToday;
    private TextView distanceAllTime;
    private TextView dateText;
    private TextView temp;

    private DatePickerDialog.OnDateSetListener dateListener;


    private Handler postBack = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        try {
            dateText = findViewById(R.id.Statistics_selectDate);
            recordDistance = findViewById(R.id.Statistics_recordDistance);
            distanceAllTime = findViewById(R.id.Statistics_distanceAllTime);
            distanceToday = findViewById(R.id.Statistics_distanceToday);
            timeToday = findViewById(R.id.Statistics_timeToday);



            Date date = new Date();
            SimpleDateFormat formatter;
            formatter = new SimpleDateFormat("dd/MM/yyyy");
            String str = formatter.format(date);
            displayStatsOnDate(str);
            setUpDateDialogue();
        }catch(Exception e){
            Log.e("Error","Failed to start");
        }


    }

    private void setUpDateDialogue() {
        dateText.setOnClickListener(view -> {
            int dd;
            int yyyy;

            int mm;


            // if first time selecting date choose current date, else last selected date
            if (!dateText.getText().toString().equalsIgnoreCase("select date")) {
                String[] dateParts;
                dateParts = dateText.getText().toString().split("/");
                dd = Integer.parseInt(dateParts[0]);
                yyyy = Integer.parseInt(dateParts[2]);
                mm = Integer.parseInt(dateParts[1]) - 1;

            } else {
                Calendar calender;
                calender = Calendar.getInstance();
                dd = calender.get(Calendar.DAY_OF_MONTH);
                yyyy = calender.get(Calendar.YEAR);
                mm = calender.get(Calendar.MONTH);

            }

            DatePickerDialog dialog;
            dialog = new DatePickerDialog(
                    Statistics.this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    dateListener,
                    yyyy,
                    mm,
                    dd);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        dateListener = (datePicker, yyyy, mm, dd) -> {
            // user has selected a date on which to get statistics about
            mm = mm + 1;
            String date = mm < 10 ? dd + "/0" + mm + "/" + yyyy : dd + "/" + mm + "/" + yyyy;

            // format the date so its like dd/mm/yyyy

            if(dd < 10) {
                date = "0" + date;
            }

            dateText.setText(date);
            displayStatsOnDate(date);
        };
    }
    public void onClickBack(View v){
        Intent main = new Intent(Statistics.this, MainActivity.class);
        startActivity(main);
    }

    @SuppressLint("Range")
    private void displayStatsOnDate(String d) {

        final String[] dateParts = d.split("/");
        final String date = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];


        new Thread(() -> {
            // get the time today and distance today
            Cursor c;
            c = getContentResolver().query(JPC.JOURNEY_URI,
                    null, JPC.J_DATE + " = ?", new String[] {date}, null);
            double distanceTodayKM;
            distanceTodayKM = 0;
            long   timeTodayS;
            timeTodayS  = 0;
            try {
                while(c.moveToNext()) {
                    timeTodayS     += c.getLong(c.getColumnIndex(JPC.J_DURATION));
                    distanceTodayKM += c.getDouble(c.getColumnIndex(JPC.J_distance));

                }
            } finally {
                c.close();
            }

            final long hours;
            hours = timeTodayS / 3600;
            final long minutes;
            minutes = (timeTodayS % 3600) / 60;
            final long seconds;
            seconds = timeTodayS % 60;
            final double distanceTodayKM_ = distanceTodayKM;


            // calculate record distance in 1 day and total distance travelled all time
            c = getContentResolver().query(JPC.JOURNEY_URI,
                    null, null, null, null);
            double totalDistanceKM;
            totalDistanceKM = 0;
            double recordDistanceKM;
            recordDistanceKM = 0;
            try {
                while(c.moveToNext()) {
                    double d1;
                    d1 = c.getDouble(c.getColumnIndex(JPC.J_distance));
                    if(recordDistanceKM < d1) recordDistanceKM = d1;
                    totalDistanceKM += d1;
                }
            } finally {
                c.close();
            }

            final double totalDistanceKM_  = totalDistanceKM;
            final double recordDistanceKM_ = recordDistanceKM;




            // post back text view updates to UI thread
            postBack.post(new Runnable() {
                public void run() {
                    recordDistance.setText(String.format("%.2f KM", recordDistanceKM_));
                    distanceAllTime.setText(String.format("%.2f KM", totalDistanceKM_));
                    distanceToday.setText(String.format("%.2f KM", distanceTodayKM_));
                    timeToday.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));



                }
            });
        }).start();

    }



}
