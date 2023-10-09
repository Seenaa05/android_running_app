package com.example.main;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Arrays;

public class DBCreator extends SQLiteOpenHelper {
    public DBCreator(Context context) {
        super(context, "localDB", null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String s : Arrays.asList("CREATE TABLE journey (" +
                "journeyID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "image varchar(256)," +
                "distance REAL NOT NULL," +
                "rating INTEGER NOT NULL DEFAULT 1," +
                "duration BIGINT NOT NULL," +
                "name varchar(256) NOT NULL DEFAULT 'Recorded Journey'," +
                "comment varchar(256) NOT NULL DEFAULT ''," +
                "date DATETIME NOT NULL);", "CREATE TABLE location (" +
                " longitude REAL NOT NULL," +
                " locationID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                " journeyID INTEGER NOT NULL," +
                " altitude REAL NOT NULL," +
                " latitude REAL NOT NULL," +
                " CONSTRAINT fk1 FOREIGN KEY (journeyID) REFERENCES journey (journeyID) ON DELETE CASCADE);")) {
            db.execSQL(s);
        }

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
