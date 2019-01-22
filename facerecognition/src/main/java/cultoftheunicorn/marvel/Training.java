package cultoftheunicorn.marvel;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.cultoftheunicorn.marvel.R;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ServiceAPI.AlertUtil;
import ServiceAPI.RegisterUser;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.opencv.cultoftheunicorn.marvel.R.id.emploee_id;

public class Training extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    public static final int TRAINING= 0;
    public static final int IDLE= 2;
    ProgressDialog  progressDialog;

    String IMEI_Number_Holder = "";
    private static final int frontCam =1;
    private static final int backCam =2;
    public boolean cameraid =  true;

    private int faceState=IDLE;

    private Mat                    mRgba;
    private Mat                    mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;

    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;
    private int mLikely=999;

    String mPath="";

    private Tutorial3View   mOpenCvCameraView;

    String text;
    private ImageView Iv;
    Bitmap mBitmap;
    Handler mHandler;
    TelephonyManager telephonyManager;

    PersonRecognizer fr;
    ToggleButton capture;

    static final long MAXIMG = 10;

    int countImages=0;

    Labels labelsFile;
    static {
        OpenCVLoader.initDebug();
        System.loadLibrary("opencv_java");
    }

    public Training() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    fr=new PersonRecognizer(mPath);
                    String s = getResources().getString(R.string.Straininig);
                    //Toast.makeText(getApplicationContext(),s, Toast.LENGTH_LONG).show();
                    fr.load();

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();



                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;


            }
        }
    };

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Training");
        }*/

        Intent intent = new Intent();
        text = getIntent().getStringExtra("name");
        Iv = (ImageView) findViewById(R.id.imagePreview);
        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        capture = (ToggleButton) findViewById(R.id.capture);
        capture.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                captureOnClick();
            }
        });

        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);

            final Button camerachange = (Button)findViewById(R.id.camchange);
        camerachange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cameraid==true){

                    mOpenCvCameraView.setCamFront();
                    camerachange.setText("Back Camera");
                    cameraid=false;
                }
                else if (cameraid==false){
                    mOpenCvCameraView.setCamBack();
                    camerachange.setText("Front Camera");
                    cameraid=true;

                }
            }
        });
       // mOpenCvCameraView.setRotation(360);
//        mOpenCvCameraView.setRotation(0);

        //mPath=getFilesDir()+"/facerecogOCV/";
        mPath = Environment.getExternalStorageDirectory()+"/facerecogOCV/";

        Log.e("Path", mPath);

        labelsFile= new Labels(mPath);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj=="IMG")
                {
                    Canvas canvas = new Canvas();
                    canvas.setBitmap(mBitmap);
                    Iv.setImageBitmap(mBitmap);
                    if (countImages>=MAXIMG-1)
                    {
                        capture.setChecked(false);//
                        captureOnClick();
                        Intent intent = getIntent();
                        String employeeid = intent.getStringExtra("name").toString();
                        Toast.makeText(Training.this, "completed", Toast.LENGTH_SHORT).show();
                        Log.e("Employee ID","COmplyed"+ employeeid);


                        //isregistered = true;

                        IMEI_Number_Holder = telephonyManager.getDeviceId();
                        Log.e("Registering User",employeeid +IMEI_Number_Holder);
                        Registeruser(IMEI_Number_Holder, employeeid);

                    }
                }
            }
        };

        boolean success=(new File(mPath)).mkdirs();

        if (!success)
            Log.e("Error","Error creating directory");

    }

    void captureOnClick()
    {
        if (capture.isChecked())
            faceState = TRAINING;
        else {


            countImages=0;
            faceState=IDLE;
    //        Iv.setDrawingCacheEnabled(true);

            Iv.setImageResource(R.drawable.user_image);

                //emploee_id.setVisibility(View.VISIBLE);




        }
    }

    private void Registeruser(String FingerPrint, String EmployeId) {
        progressDialog = ProgressDialog.show(Training.this, "", "Registering....", true);
        try {
            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(getString(R.string.URL))
                    .addConverterFactory(GsonConverterFactory.create());
            Retrofit retrofit = builder.build();
            RegisterUser moduleInterface = retrofit.create(RegisterUser.class);
            Call<ResponseBody> apirespone = moduleInterface.RegisterUserResponse(FingerPrint, EmployeId /*FingerPrint*/);
            apirespone.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String Name = "", Status = "";
                        progressDialog.dismiss();
                        if (response.code() == 200) {
                            String respones = response.body().string();
                            if (!respones.isEmpty()) {
                                JSONObject jsonObject = new JSONObject(respones);
                                try {
                                    Name = jsonObject.getString("EmpName");
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }

                                AlertDialog alertDialog = new AlertDialog.Builder(Training.this).create();
                                alertDialog.setTitle("Success ");
                                alertDialog.setMessage( "    "+Name+ "    "+
                                        "\n  Do you want to register another User ?");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "YES",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                Intent intent = new Intent(Training.this,AttendanceActivity.class);
                                                intent.putExtra("typeof","regface");
                                                startActivity(intent);
                                            }
                                        });
                                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Intent intent = new Intent(Training.this,MainActivity.class);
                                        startActivity(intent);
                                    }
                                });
                                alertDialog.show();
                            }
                        } else if (response.code() == 400) {
                            progressDialog.dismiss();
                            AlertUtil.showDialog(Training.this, "Failure", "User already taken", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                    Intent intent = new Intent(Training.this,MainActivity.class);
                                    startActivity(intent);
                                }
                            });
                        }
                    } catch (Exception ex) {
                        progressDialog.dismiss();
                        AlertUtil.showDialog(Training.this, "Failure", "Not data found,please try after some time", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(Training.this,MainActivity.class);
                                startActivity(intent);
                            }
                        });
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressDialog.dismiss();
                }
            });
        } catch (
                Exception ex)

        {
            progressDialog.dismiss();
            AlertUtil.showDialog(Training.this, "Error", "Something went wrong ,please try after sometime", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent intent = new Intent(Training.this,MainActivity.class);
                    startActivity(intent);
                }
            });
            ex.printStackTrace();
        }
        progressDialog.dismiss();
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        Mat mRgbaT = mRgba.t();

        if (cameraid==true){


            Core.flip(mRgba.t(), mRgbaT, 1);
            Imgproc.resize(mRgbaT, mRgbaT, mRgba.size());

            //front camera
        }

        else if (cameraid==false){

          //  Mat mRgbaT = mRgba.t();
            Core.flip(mRgba.t(), mRgbaT, 0);
            Imgproc.resize(mRgbaT, mRgbaT, mRgba.size());
        }

       // mRgba = inputFrame.rgba();


        if (mAbsoluteFaceSize == 0) {
            int height = mRgbaT.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            //  mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mRgbaT, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        else if (mDetectorType == NATIVE_DETECTOR) {
            /*if (mNativeDetector != null)
                mNativeDetector.detect(mGray, faces);*/
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();

        if ((facesArray.length==1)&&(faceState==TRAINING)&&(countImages<MAXIMG)&&(!text.equals("")))
        {


            Mat m;
            Rect r=facesArray[0];


            m=mRgbaT.submat(r);
            mBitmap = Bitmap.createBitmap(m.width(),m.height(), Bitmap.Config.ARGB_8888);


            Utils.matToBitmap(m, mBitmap);

            Message msg = new Message();
            String textTochange = "IMG";
            msg.obj = textTochange;
            mHandler.sendMessage(msg);
            if (countImages<MAXIMG)
            {
                fr.add(m, text);
                countImages++;
            }

        }
        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(mRgbaT, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return mRgbaT;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
