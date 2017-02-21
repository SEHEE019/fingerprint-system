package com.project.irm_fingerprintrecognition;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by kimsh019 on 2017-02-06.
 */
public class DBHelper extends SQLiteOpenHelper {
    String TAG = DBHelper.class.getCanonicalName();
    int mVersion;
    private Context context;

    //DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        createFingerprintbase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS FUser");

        onCreate(db);
    }

    //create FUser database
    public void createFingerprintbase(SQLiteDatabase db) {
        String FUSER = "CREATE TABLE FUSER(" +
                "USERKEY INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "USERNAME TEXT NOT NULL," +
                "USERSIGNATURE VARCHAR NOT NULL," +
                "USERFINGER VARCHAR NOT NULL" +
                ");";
        try {
            db.execSQL(FUSER);
            Toast.makeText(context, "DB 등록 완료", Toast.LENGTH_SHORT).show();
            Log.d("sqlite: ", "created db");
        }catch (SQLiteException e){
            e.printStackTrace();
        }
    }
}