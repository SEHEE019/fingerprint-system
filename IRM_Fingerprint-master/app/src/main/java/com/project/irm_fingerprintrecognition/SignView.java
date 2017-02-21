package com.project.irm_fingerprintrecognition;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

import static com.project.irm_fingerprintrecognition.R.layout.custom_dialog;


@SuppressWarnings("WrongConstant")
public class SignView extends Activity {

    private SignView signContext;

    DrawPoint drawPoint;
    CanvasView mView;

    String dbName = "fu_file.db";
    int dbVersion = 3;
    private DBHelper helper;
    private SQLiteDatabase db;
    String tag = "SQLite";
    String tableName = "FUSER";

    private String m_strUserName = "";
    private String[] pEnrolledUser = new String[50];
    private int nenrolled = 0;
    private boolean isname= false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signview);


        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        mView = new CanvasView(this);
        drawPoint = new DrawPoint();
        signContext = this;

        helper = new DBHelper( this, dbName, null, dbVersion);
        try{
            db = helper.getWritableDatabase();
        }catch (SQLiteException e){
            e.printStackTrace();
            Log.e(tag, "Can not open DB");
        }

        LinearLayout linear = (LinearLayout)findViewById(R.id.SignPad);
        Button SaveBtn = (Button)findViewById(R.id.BtnSave);
        Button NextBtn = (Button)findViewById(R.id.BtnNext);
        linear.addView(mView,0);

        insertName();
        SaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSign();
            }
        });
        NextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(SignView.this,FingerprintEnroll.class);
                intent.putExtra("pEnrolledUser",pEnrolledUser);
                intent.putExtra("nenrolled",nenrolled);
                intent.putExtra("isname",isname);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //touch에 해당하는 evnet 발생 시
        switch (event.getAction()){
            //touch 된 점들을 drawpoint에서 관리
            case MotionEvent.ACTION_DOWN:
                drawPoint.addPoint(event.getRawX(), event.getRawY(),false);
                mView.invalidate();

                //view를 다시 그려줌
            case MotionEvent.ACTION_MOVE:
                drawPoint.addPoint(event.getRawX(),event.getRawY(),true);
                mView.invalidate();

        }

        return true;

    }

    //invalidate-onDraw-drawCanvas로 구현함. 모든 터치된 영역에 대하여 점을 그리고 선으로 이음
    protected  class CanvasView extends View {

        public CanvasView( Context context){super(context);}

        public void onDraw(Canvas canvas){drawCanvas(canvas);}

        public void drawCanvas(Canvas canvas){

            for(int i=0;i<drawPoint.arrayTouchPoint.size();i++)
                if(drawPoint.arrayTouchPoint.get(i).draw) {


                    canvas.drawLine(drawPoint.getX(i - 1), drawPoint.getY(i - 1)
                            , drawPoint.getX(i), drawPoint.getY(i), drawPoint.paint);
                }

                else{
                    canvas.drawPoint(drawPoint.getX(i), drawPoint.getY(i),drawPoint.paint);
                }

            this.invalidate();

        }

    }

    private void insertName(){
        Context mContext = getApplicationContext();
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(custom_dialog,(ViewGroup)findViewById(R.id.layout_root));

        AlertDialog.Builder aDialog = new AlertDialog.Builder(SignView.this);
        aDialog.setTitle("Insert your name");
        aDialog.setView(layout);

                aDialog.setPositiveButton("OK", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){
                        EditText tv = (EditText)layout.findViewById(R.id.EditText01);

                    m_strUserName = tv.getText().toString();
                    if(tv.getText().toString().equals("")||tv.getText().toString() == null){
                        Toast.makeText(signContext, "please insert User Name again",Toast.LENGTH_SHORT).show();
                        return;
                    }else {
                        pEnrolledUser[nenrolled] = tv.getText().toString();
                        isname = true;

                        String uName = pEnrolledUser[nenrolled];
                        String uSignature = m_strUserName + "_서명 파일.png";
                        String uFingerprint = m_strUserName + "_지문 파일";

                        if("".equals(uName)||"".equals(uSignature)||"".equals(uFingerprint)){
                            tv.setText("Insert Failed");
                            return;
                        }

                        dbInsert(uName,uSignature,uFingerprint);
                    }

            }
        });
        aDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){}
        });
        AlertDialog ad = aDialog.create();
        ad.show();

    }
    private void saveSign(){
        TextView tv = (TextView)findViewById(R.id.txmessage);

        if(m_strUserName.equals("")||m_strUserName.toString() == null){
            Toast.makeText(signContext,"please insert User Name",Toast.LENGTH_SHORT).show();
            return;
        }

        mView.setDrawingCacheEnabled(true);

        Bitmap screenshot = Bitmap.createBitmap(mView.getDrawingCache());
        mView.setDrawingCacheEnabled(false);

        File dir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if(!dir.exists())
            dir.mkdirs();
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(new File(dir, m_strUserName.toString()+"_서명 파일"+".png"));
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Toast.makeText(this, "저장 성공", 0).show();

        } catch (Exception e) {
            Log.e("phoro","그림저장오류",e);
            Toast.makeText(this, "저장 실패", 0).show();
        }
    }

    public void dbInsert(String uName, String uSignature, String uFinger){
        ContentValues values = new ContentValues();
        values.put("USERNAME",uName);
        values.put("USERSIGNATURE",uSignature);
        values.put("USERFINGER",uFinger);
        long result = db.insert(tableName,null,values);
        Log.d(tag, result + "번째 row insert 성공");

    }

}
