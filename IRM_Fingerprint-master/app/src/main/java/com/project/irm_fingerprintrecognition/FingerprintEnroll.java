package com.project.irm_fingerprintrecognition;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.biomini.BioMiniAndroid;
import com.android.biomini.IBioMiniCallback;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

//run the 'insert name' on the main acitivity and relay the data to each activity.
@SuppressWarnings("WrongConstant")
public class FingerprintEnroll extends Activity {

    SignView signView;
    FingerprintEnroll EnrollContext;

    public final static String TAG = "BioMini SDK";
    private static BioMiniAndroid mBioMiniHandle = null;

    String dbName = "fu_file.db";
    int dbVersion = 3;
    private DBHelper helper;
    private SQLiteDatabase db;
    String tag = "SQLite";
    String tableName = "FUSER";

    //Callback
    private final IBioMiniCallback mBioMiniCallbackHandler = new IBioMiniCallback() {
        private int mWidth;
        private int mHeight;

        @Override
        public void onCaptureCallback(final byte[] capturedimage, int width, int height, int resolution, boolean bfingeron) {
            mWidth = width;
            mHeight = height;
            e(String.valueOf("onCaptureCallback called!" + "width:" + width + " height:" + height + " fingerOn:" + bfingeron));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int width = mWidth;
                    int height = mHeight;
                    byte[] Bits = new byte[width * height * 4];
                    for (int i = 0; i < width * height; i++) {
                        Bits[i * 4] =
                                Bits[i * 4 + 1] =
                                        Bits[i * 4 + 2] = capturedimage[i];
                        Bits[i * 4 + 3] = -1;
                    }
                    Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    bm.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));
                    ImageView vv = (ImageView) findViewById(R.id.imageView);
                    vv.setImageBitmap(bm);

                    vv.invalidate();
                }
            });
        }

        @Override
        public void onErrorOccurred(String s) {

        }
    };

    private int ufa_res;
    private String errmsg = "Error";

    private static void l(Object msg) {
        Log.d(TAG, ">==< " + msg.toString() + " >==<");
    }

    private static void e(Object msg) {
        //Log.e(TAG, ">==<" + msg.toString() + ">==<");
    }

    private byte[] pImage = new byte[320 * 480];
    private byte[][] ptemplate1 = new byte[50][1024];
    private int[][] ntemplateSize1 = new int[50][4];
    private byte[] ptemplate2 = new byte[1024];
    private int[] ntemplateSize2 = new int[4];
    private int[] nquality = new int[4];
    private int sensitivity;
    private int timeout;
    private int securitylevel;
    private int fastmode;
    private String[] pEnrolledUser;
    private int nenrolled = 0;
    private boolean isname = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprintenroll);
        if (mBioMiniHandle == null) {
            mBioMiniHandle = new BioMiniAndroid(this);
        }
        signView = new SignView();
        Intent intent = getIntent();
        pEnrolledUser = intent.getStringArrayExtra("pEnrolledUser");
        nenrolled = intent.getExtras().getInt("nenrolled");
        isname = intent.getExtras().getBoolean("isname");

        Button ConfirmBtn = (Button) findViewById(R.id.BtnConfirm);
        Button FindDeviceBtn = (Button) findViewById(R.id.BtnFindDevice);
        final Button EnrollmentBtn = (Button) findViewById(R.id.BtnEnrollment);
        Button FinishBtn = (Button) findViewById(R.id.BtnFinish);

        checkDB();
//        deleteDB();

        //Finding device button
        FindDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBioMiniHandle == null) {
                    e(String.valueOf("BioMini SDK Handler with NULL!"));
                } else {
                    //find BioMini device and request permission
                    ufa_res = mBioMiniHandle.UFA_FindDevice();

                    errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                    TextView tv = (TextView) findViewById(R.id.txmessage);
                    tv.setText("UFA_FindDevice res: " + errmsg);
                    //SDK initialization
                    ufa_res = mBioMiniHandle.UFA_Init();
                    String errmsg1 = mBioMiniHandle.UFA_GetErrorString(ufa_res);

                    mBioMiniHandle.UFA_SetParameter(mBioMiniHandle.UFA_PARAM_SENSITIVITY, sensitivity);
                    mBioMiniHandle.UFA_SetParameter(mBioMiniHandle.UFA_PARAM_TIMEOUT, timeout * 1000);
                    mBioMiniHandle.UFA_SetParameter(mBioMiniHandle.UFA_PARAM_SECURITY_LEVEL, securitylevel);
                    mBioMiniHandle.UFA_SetParameter(mBioMiniHandle.UFA_PARAM_FAST_MODE, fastmode);
                }
            }
        });

        //Enrolling fingerprint button
        EnrollmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnrollmentBtn.setEnabled(false);
                if (mBioMiniHandle == null) {
                    e(String.valueOf("BioMini SDK Handler with NULL!"));
                } else {

                    //Check whether  the name is registered
                    if (!isname) {
                        TextView tv = (TextView) findViewById(R.id.txmessage);
                        tv.setText("there is no inserted user name");
                        return;
                    }


                    // capture fingerprint image
                    ufa_res = mBioMiniHandle.UFA_CaptureSingle(pImage);

                    if (ufa_res != 0) {
                        errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                        TextView tv = (TextView) findViewById(R.id.txmessage);
                        tv.setText("UFA_CaptureSingle res: " + errmsg);
                        return;
                    }

                    int width = mBioMiniHandle.getImageWidth();
                    int height = mBioMiniHandle.getImageHeight();
                    mBioMiniCallbackHandler.onCaptureCallback(pImage, width, height, 500, true);
                    // extract fingerpirnt template from captured image
                    // extracted template is saved in memory (ptemplate1: 2-D byte array)
                    ufa_res = mBioMiniHandle.UFA_ExtractTemplate(ptemplate1[nenrolled], ntemplateSize1[nenrolled], nquality, 1024);


                    if (ufa_res != 0) {
                        errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                        TextView tv = (TextView) findViewById(R.id.txmessage);
                        tv.setText("UFA_ExtractTemplate res: " + errmsg);
                        return;
                    } else {
                        errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                        TextView tv = (TextView) findViewById(R.id.txmessage);
                        tv.setText("UFA_ExtractTemplate res: " + errmsg);

                        TextView tv2 = (TextView) findViewById(R.id.txmessage);
                        tv2.setText(pEnrolledUser[nenrolled] + " is enrolled" + nenrolled + "," + ptemplate1[nenrolled].length + "," + ntemplateSize1[nenrolled][0]);

                        try {
                            writeFile(pEnrolledUser[nenrolled], ptemplate1[nenrolled], ntemplateSize1[nenrolled][0]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        isname = false;
                    }
                }
                EnrollmentBtn.setEnabled(true);
            }
        });
        FinishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Main.class);
                startActivity(intent);
            }
        });
    }
//    public void deleteDB(){
//        helper = new DBHelper(this, dbName, null, dbVersion);
//        db = helper.getWritableDatabase();
//        db.execSQL("DELETE FROM FUSER");
//        Log.d(tag, "delete complete");
//    }
    public void checkDB(){
        helper = new DBHelper(this, dbName, null, dbVersion);
        db = helper.getReadableDatabase();

        String sql = "SELECT * FROM "+tableName;
        Cursor cursor = db.rawQuery(sql,null);
        while(cursor.moveToNext()){
            int uKey = cursor.getInt(0);
            String uName = cursor.getString(1);
            String uSignature = cursor.getString(2);
            String uFinger = cursor.getString(3);

            Log.d(tag, "USERKEY:"+uKey+" ,USERNAME:"+uName+" ,USERSIGNATURE:"+uSignature+" ,USERFINGER:"+uFinger);

        }
    }
    //Save byte array as a file
    public void writeFile(String username, byte[] ptemplate, int size) throws IOException {
            FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/"+username+"_지문 파일");
            fos.write(ptemplate, 0, size);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    Toast.makeText(this, "저장성공", 0).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "저장실패", 0).show();
                }
            }
        }
    }
}
