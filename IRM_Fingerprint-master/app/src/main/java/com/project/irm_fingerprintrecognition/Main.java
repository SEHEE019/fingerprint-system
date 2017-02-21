package com.project.irm_fingerprintrecognition;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class Main extends AppCompatActivity {

    FingerprintEnroll fingerEnroll;
    Main mainContext;

//    private String m_strUserName = "";
    private String[] pEnrolledUser = new String[50];
    private int nenrolled = 0;
    private byte[][] ptemplate1 = new byte[50][1024];
    private int[][] ntemplateSize1 = new int[50][4];
    private boolean isname= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button BtnEnrollmentPage = (Button)findViewById(R.id.BtnEnrollPage);
        Button BtnLookupPage = (Button)findViewById(R.id.BtnLookupPage);

        BtnEnrollmentPage.setOnClickListener(BtnEnrollmentPageListner);
        BtnLookupPage.setOnClickListener(BtnLookupPageListner);

    }
//    private void insertName(){
//        Context mContext = getApplicationContext();
//
//        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
//        final View layout = inflater.inflate(custom_dialog,(ViewGroup)findViewById(R.id.layout_root));
//
//        AlertDialog.Builder aDialog = new AlertDialog.Builder(Main.this);
//        aDialog.setTitle("Insert your name");
//        aDialog.setView(layout);
//
//        aDialog.setPositiveButton("OK", new DialogInterface.OnClickListener(){
//            public void onClick(DialogInterface dialog, int whichButton){
//                EditText tv = (EditText)layout.findViewById(R.id.EditText01);
//
//                m_strUserName = tv.getText().toString();
//                if(tv.getText().toString().equals("")||tv.getText().toString() == null){
//                    Toast.makeText(mainContext, "please insert User Name again",Toast.LENGTH_SHORT).show();
//                    return;
//                }else{
//                    pEnrolledUser[nenrolled] = tv.getText().toString();
//                    isname = true;
//                }
//            }
//        });
//        aDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
//            public void onClick(DialogInterface dialog, int whichButton){}
//        });
//        AlertDialog ad = aDialog.create();
//        ad.show();
//    }

    View.OnClickListener BtnEnrollmentPageListner = new View.OnClickListener(){
      @Override
        public void onClick(View v){
          Intent intent = new Intent(Main.this,SignView.class);
          startActivity(intent);
      }
    };

    View.OnClickListener BtnLookupPageListner = new View.OnClickListener(){
      @Override
        public void onClick(View v){
          Intent intent = new Intent(Main.this,FingerprintMatch.class);
          startActivity(intent);
      }
    };
}