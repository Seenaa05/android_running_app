package com.example.main;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ViewJourneys extends ListActivity {
    private TextView dateText;
    private DatePickerDialog.OnDateSetListener dateListener;

    private ListView journeyList;
    private JourneyAdapter adapter;
    private ArrayList<JourneyItem> journeyNames;

    private class JourneyItem {
        private String name;
        private String strUri;
        private long _id;

        public String getName() {
            return name;
        }
        public long get_id() {
            return _id;
        }


        public void setStrUri(String strUri) {
            this.strUri = strUri;
        }

        public String getStrUri() {
            return strUri;
        }

        public void set_id(long _id) {
            this._id = _id;
        }
        public void setName(String name) {
            this.name = name;
        }


    }


    private class JourneyAdapter extends ArrayAdapter<JourneyItem> {
        //
        private ArrayList<JourneyItem> items;

        public JourneyAdapter(Context context, int textViewResourceId, ArrayList<JourneyItem> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            v = convertView;
            if (v == null) {
                LayoutInflater vi;
                vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.journeylist, null);
            }

            JourneyItem item;
            item = items.get(position);
            if (item == null) {
                return v;
            }
            TextView text;
            text = v.findViewById(R.id.singleJourney);
            ImageView img;
            img = v.findViewById(R.id.journeyList_journeyImg);
            if (text != null) {
                text.setText(item.getName());
            }
            if (img == null) {
                return v;
            }
            String strUri = item.getStrUri();
            if (strUri == null) {
                img.setImageDrawable(getResources().getDrawable(R.drawable.running));
            } else {
                try {
                    final Uri imageUri;
                    imageUri = Uri.parse(strUri);
                    final InputStream imageStream;
                    imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    img.setImageBitmap(selectedImage);
                } catch (Exception e) {
                    Log.d("Error","Cant set image");
                }
            }

            return v;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_journeys);

        journeyNames = new ArrayList<JourneyItem>();
        adapter = new JourneyAdapter(this, R.layout.journeylist, journeyNames);
        setListAdapter(adapter);
        setUpDateDialogue();

        journeyList.setClickable(true);
        journeyList.setOnItemClickListener((arg0, arg1, position, arg3) -> {
            JourneyItem o = (JourneyItem) journeyList.getItemAtPosition(position);
            long journeyID;
            journeyID = o.get_id();


            // start the single journey activity sending it the journeyID
            Bundle b = new Bundle();
            b.putLong("journeyID", journeyID);
            Intent singleJourney;
            singleJourney = new Intent(ViewJourneys.this, ViewSingleJourney.class);
            singleJourney.putExtras(b);
            startActivity(singleJourney);
        });
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String str = formatter.format(date);
        listJourneys(str);
    }

    @Override
    public void onResume() {
        super.onResume();
        // update the view in-case title or image was changed
        String date;
        date = dateText.getText().toString();
        if (date.toLowerCase().equals("select date")) {
            return;
        }
        listJourneys(date);
    }
    public void onClickBack(View v){
        Intent main = new Intent(ViewJourneys.this, MainActivity.class);
        startActivity(main);
    }

    private void setUpDateDialogue() {
        dateText = findViewById(R.id.selectDateText);
        journeyList = getListView();
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
                    ViewJourneys.this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    dateListener,
                    yyyy,
                    mm,
                    dd);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        dateListener = (datePicker, yyyy, mm, dd) -> {

            mm = mm + 1;
            String date = mm < 10 ? dd + "/0" + mm + "/" + yyyy : dd + "/" + mm + "/" + yyyy;



            if(dd < 10) {
                date = "0" + date;
            }
            listJourneys(date);
            dateText.setText(date);


        };
    }

    /* Query database to get all journeys in specified date in dd/mm/yyyy format and display them in listview */
    @SuppressLint("Range")
    private void listJourneys(String date) {
        // sqlite server expects yyyy-mm-dd
        String[] dateParts;
        dateParts = date.split("/");
        date = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];


        // put cursor items into ArrayList and add those items to the adapter
        try (Cursor c = getContentResolver().query(JPC.JOURNEY_URI,
                new String[]{JPC.J_ID + " _id", JPC.J_NAME, JPC.J_IMAGE}, JPC.J_DATE + " = ?", new String[]{date}, JPC.J_NAME + " ASC")) {
            journeyNames = new ArrayList<JourneyItem>();
            adapter.notifyDataSetChanged();
            adapter.clear();
            adapter.notifyDataSetChanged();
            while (c.moveToNext()) {
                JourneyItem i = new JourneyItem();
                i.set_id(c.getLong(c.getColumnIndex("_id")));
                i.setStrUri(c.getString(c.getColumnIndex(JPC.J_IMAGE)));
                i.setName(c.getString(c.getColumnIndex(JPC.J_NAME)));
                journeyNames.add(i);
            }
        } finally {
            if (journeyNames != null && journeyNames.size() > 0) {
                adapter.notifyDataSetChanged();
                int i = 0;
                while (i < journeyNames.size()) {
                    adapter.add(journeyNames.get(i));
                    i++;
                }
            }
            adapter.notifyDataSetChanged();
        }

    }
}
