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

import com.android.biomini.BioMiniAndroid;
import com.android.biomini.IBioMiniCallback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@SuppressWarnings("WrongConstant")
public class FingerprintMatch extends Activity {

    FingerprintMatch fingerprintMatch;
    FingerprintMatch MatchContext;

    public final static String TAG = "BioMini SDK";
    private static BioMiniAndroid mBioMiniHandle = null;

    private final IBioMiniCallback mBioMiniCallbackHandler = new IBioMiniCallback() {
        private int mWidth;
        private int mHeight;

        //SDK Callback
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
    private String m_strUserName = "";
    private String[] pEnrolledUser = new String[50];
    private int nenrolled = 0;
    private boolean isname = false;

    String dbName = "fu_file.db";
    int dbVersion = 3;
    private DBHelper helper;
    private SQLiteDatabase db;
    String tag = "SQLite";
    String tableName = "FUSER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprintmatch);
        if (mBioMiniHandle == null) {
            mBioMiniHandle = new BioMiniAndroid(this);
        }
        fingerprintMatch = new FingerprintMatch();

//        insertName();
        MatchContext = this;

        findName();

        Button FindDeviceBtn = (Button) findViewById(R.id.BtnFindDevice);
        final Button MatchingBtn = (Button) findViewById(R.id.BtnMatching);
        Button FinishBtn = (Button) findViewById(R.id.BtnFinish);

        //finding device button
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

        //Finger matching button
        MatchingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MatchingBtn.setEnabled(false);



                if (mBioMiniCallbackHandler == null) {
                    e(String.valueOf("BioMini SDK Handler with NULL"));
                } else {
                    TextView tv = (TextView) findViewById(R.id.txmessage);

                    ufa_res = mBioMiniHandle.UFA_CaptureSingle(pImage);

                    if (ufa_res != 0) {
                        errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                        tv.setText("UFA_CaptureSingle res: " + errmsg);
                        return;
                    }

                    int width = mBioMiniHandle.getImageWidth(), height = mBioMiniHandle.getImageHeight();
                    mBioMiniCallbackHandler.onCaptureCallback(pImage, width, height, 500, true);

                    int[] quality = new int[1];
                    if (mBioMiniHandle.UFA_GetFPQuality(pImage, width, height, quality, 1) == mBioMiniHandle.UFA_OK) {
                        tv.setText("Fingerprit quality: " + quality[0]);
                        l("Fingerprint qality: " + quality[0]);
                    }

                    // extract fingerprint template from captured image
                    ufa_res = mBioMiniHandle.UFA_ExtractTemplate(ptemplate2, ntemplateSize2, nquality, 1024);

                    if (ufa_res != 0) {
                        errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                        tv.setText("UFA_ExtractTemplate res: " + errmsg);
                        return;
                    }

                    int[] nVerificationResult = new int[4];
                    nVerificationResult[0] = 0;

                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + user_fingerprint);
                    if (!file.exists()) {
                        tv.setText("File not found");
                        return;
                    } else {
                        tv.setText("We found your file!!");
                    }
                    try {
                        ptemplate1[nenrolled] = readFile(file);
                        ntemplateSize1[nenrolled][0] = readFile(file).length;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    ufa_res = mBioMiniHandle.UFA_Verify(ptemplate1[nenrolled], ntemplateSize1[nenrolled][0], ptemplate2, ntemplateSize2[0], nVerificationResult);
                    l("--->UFA_Verify retValue: " + ufa_res);
                    if (nVerificationResult[0] == 1) {
                        tv.setText("Matching Complete!");
                    }
                    if (ufa_res != mBioMiniHandle.UFA_OK) {
                        errmsg = mBioMiniHandle.UFA_GetErrorString(ufa_res);
                        tv.setText("UFA_Verify res: " + errmsg);
                        return;
                    }
                    if (nVerificationResult[0] != 1) {
                        tv.setText("matching result: not matched");

                        TextView tv2 = (TextView) findViewById(R.id.txmessage);
                        tv2.setText("Identification fail");
                    }
                    Intent intent = new Intent(FingerprintMatch.this, ShowSign.class);
                    intent.putExtra("pEnrolledUser", pEnrolledUser);
                    intent.putExtra("nenrolled", nenrolled);
                    intent.putExtra("isname", isname);
                    intent.putExtra("m_strUserName", m_strUserName);
                    startActivity(intent);

                }
                MatchingBtn.setEnabled(true);
            }
        });

    }


    //read file data and change to byte array
    public static byte[] readFile(File file) throws IOException {
        FileInputStream fis = null;
        ByteArrayInputStream bis = null;
        ByteArrayOutputStream bos = null;

        fis = new FileInputStream(file);
        bos = new ByteArrayOutputStream();
        byte[] b = new byte[(int) file.length()];
        int i = 0;
        try {

            while ((i = fis.read(b)) != -1) {
                bos.write(b, 0, i);
            }
            byte[] fileArray = bos.toByteArray();
            bis = new ByteArrayInputStream(fileArray);

            while ((i = bis.read(b)) != -1) {
                System.out.write(b, 0, i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fis.close();
            bos.close();
            bis.close();
        }
        return b;
    }

    public String[] findName() {
        helper = new DBHelper(this, dbName, null, dbVersion);
        db = helper.getReadableDatabase();

        String sql = "SELECT USERFINGER FROM " + tableName;
        Cursor cursor = db.rawQuery(sql, null);

        while (cursor.moveToNext()) {
             String[] uFinger = {cursor.getString(0)};

            Log.d(tag, "USERFINGER:" + uFinger);
        }
        return
    }

//    //Insert name and find data
//    @Override
//    public void onPrepareDialog(int id, Dialog dls){
//        super.onPrepareDialog(id,dls);
//    }
//    private void insertName(){
//    final Context mContext = getApplicationContext();
//    LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
//    final View layout = inflater.inflate(custom_dialog,(ViewGroup)findViewById(R.id.layout_root));
//
//    AlertDialog.Builder aDialog = new AlertDialog.Builder(FingerprintMatch.this);
//    aDialog.setTitle("Insert your name");
//    aDialog.setView(layout);
//
//    aDialog.setPositiveButton("OK", new DialogInterface.OnClickListener(){
//        public void onClick(DialogInterface dialog, int whichButton){
//            EditText tv = (EditText)layout.findViewById(R.id.EditText01);
//
//            m_strUserName = tv.getText().toString();
//            Log.d("Insert: ",m_strUserName);
//            if(tv.getText().toString().equals("")&&tv.getText().toString() == null){
//                Toast.makeText(MatchContext, "please insert User Name again",Toast.LENGTH_SHORT).show();
//                return;
//            }else{
//                pEnrolledUser[nenrolled] = tv.getText().toString();
//                isname = true;
//                user_fingerprint = m_strUserName+"_지문 파일";
//
//            }
//
//        }
//    });
//    aDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
//        public void onClick(DialogInterface dialog, int whichButton){}
//    });
//    AlertDialog ad = aDialog.create();
//    ad.show();
//
//}
    }

