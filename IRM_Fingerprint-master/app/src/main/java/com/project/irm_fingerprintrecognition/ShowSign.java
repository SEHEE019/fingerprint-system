package com.project.irm_fingerprintrecognition;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;


@SuppressWarnings("WrongConstant")
public class ShowSign extends Activity {


    private ShowSign signContext;
    FingerprintMatch fingerprintMatch;

    private String m_strUserName = "";
    private String[] pEnrolledUser = new String[50];
    private int nenrolled = 0;
    private boolean isname = false;

    DBHelper dbHelper = new DBHelper(ShowSign.this, "FUser", null, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showsign);



        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        signContext = this;

        Button BackBtn = (Button) findViewById(R.id.BtnBack);

        TextView tv = (TextView)findViewById(R.id.txmessage1);

        fingerprintMatch = new FingerprintMatch();
        Intent intent = getIntent();
        pEnrolledUser = intent.getStringArrayExtra("pEnrolledUser");
        nenrolled = intent.getExtras().getInt("nenrolled");
        isname = intent.getExtras().getBoolean("isname");

        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowSign.this,Main.class);
                startActivity(intent);
            }
        });

    }

    public void loadImage(File imgFile) {
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            ImageView myImage = (ImageView) findViewById(R.id.SignPad);
            myImage.setImageBitmap(myBitmap);

        }
    }
}
