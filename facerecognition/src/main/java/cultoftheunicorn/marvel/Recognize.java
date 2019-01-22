package cultoftheunicorn.marvel;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
import java.util.HashSet;
import java.util.Set;

import ServiceAPI.AlertUtil;
import ServiceAPI.CheckAttendance;
import ServiceAPI.SimpleLocation;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Recognize extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    public static final int SEARCHING= 1;
    public static final int IDLE= 2;
    SimpleLocation mLocation;
    private static final int frontCam =1;
    private static final int backCam =2;
    ProgressDialog progressDialog;
    String textToDisplay,onename;

    public boolean cameraid =  true;

    private int faceState=IDLE;
    public Button camerachange;


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

    private ImageView Iv;
    Bitmap mBitmap;
    Handler mHandler;
    String IMEI_Number_Holder = "";
    PersonRecognizer fr;
    ToggleButton scan;

    TelephonyManager telephonyManager;
    Set<String> uniqueNames = new HashSet<String>();

    String UNIQNAMES;
    // max number of people to detect in a session
    String[] uniqueNamesArray = new String[10];
    Button submit;
    static final long MAXIMG = 10;

    Labels labelsFile;
    static {
        OpenCVLoader.initDebug();
        System.loadLibrary("opencv_java");
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
                   //OpenCvCameraView.setDisplayOrientation();


                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;


            }
        }
    };

    public Recognize() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);

        scan = (ToggleButton) findViewById(R.id.scan);
        final TextView results = (TextView) findViewById(R.id.results);
        submit= (Button) findViewById(R.id.submit);
     //   submit.setVisibility(View.GONE);
        mLocation = new SimpleLocation(Recognize.this);
        mLocation.setBlurRadius(5000);
        camerachange = (Button) findViewById(R.id.camchange);
        camerachange.setText("Front Camera");
         telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        if (!mLocation.hasLocationEnabled()) {
            // ask the user to enable location access
            SimpleLocation.openSettings(Recognize.this);
        }
        mLocation.setListener(new SimpleLocation.Listener() {
            public void onPositionChanged() {
                // new location data has been received and can be accessed
            }

        });
        mOpenCvCameraView = (Tutorial3View) findViewById(R.id.tutorial3_activity_java_surface_view);
//        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);



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
//
//        camerachange.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//
//                   // mOpenCvCameraView.enableView();
//
//                mOpenCvCameraView.setCamBack();
//
//                    // The toggle is enabled
//                } else {
//
////                    mOpenCvCameraView.enableView();
//
//                    mOpenCvCameraView.setCamFront();
//                    // The toggle is disabled
//                }
//            }
//        });
        //mPath=getFilesDir()+"/facerecogOCV/";
        mPath = Environment.getExternalStorageDirectory()+"/facerecogOCV/";

        Log.e("Path", mPath);

        labelsFile= new Labels(mPath);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                /*
                    display a newline separated list of individual names
                 */
                String tempName = msg.obj.toString();
                Log.e("onename",tempName);
                if (!(tempName.equals("Unknown"))) {
                   // tempName = capitalize(tempName);

                    Log.e("onename2",tempName);
                    onename = tempName;
//                    uniqueNames.add(tempName);
//                    uniqueNamesArray = uniqueNames.toArray(new String[uniqueNames.size()]);
//                    StringBuilder strBuilder = new StringBuilder();
//                    for (int i = 0; i < uniqueNamesArray.length; i++) {
//                        strBuilder.append(uniqueNamesArray[i] + "\n");
//                    }
//                    textToDisplay  = strBuilder.toString();
//                    results.setText(textToDisplay);

                    results.setText(onename);

                    if (!results.getText().toString().isEmpty()){

                        submit.setVisibility(View.VISIBLE);
                    }
                    else {

                        submit.setVisibility(View.GONE);
                    }
                }
            }
        };

        scan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
//                    if(!fr.canPredict()) {
//                        scan.setChecked(false);
//                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.SCanntoPredic), Toast.LENGTH_LONG).show();
//                        return;
//                    }
                    faceState = SEARCHING;
                }
                else {
                    faceState = IDLE;
                }
            }
        });

        boolean success=(new File(mPath)).mkdirs();
        if (!success)
        {
            Log.e("Error","Error creating directory");
        }


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                IMEI_Number_Holder = telephonyManager.getDeviceId();

                invokeFingerAuthetication(IMEI_Number_Holder,String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude()));
                Log.e("invokeFinger",IMEI_Number_Holder );
                //                if(uniqueNames.size() > 0) {
////                    Intent intent = new Intent(Recognize.this, ReviewResults.class);
////                    intent.putExtra("list", uniqueNamesArray);
////                    startActivity(intent);
//
//                    Log.e("invokeFinger",onename);
//
//                    invokeFingerAuthetication(onename,String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude()));
//
//
//                }
//                else {
//                    Toast.makeText(Recognize.this, "Empty list cannot be sent further", Toast.LENGTH_LONG).show();
//                }
            }
        });

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

        if ((facesArray.length>0) && (faceState==SEARCHING))
        {
            Mat m;
            m=mRgbaT.submat(facesArray[0]);
            mBitmap = Bitmap.createBitmap(m.width(),m.height(), Bitmap.Config.ARGB_8888);


            Utils.matToBitmap(m, mBitmap);
            Message msg = new Message();
            String textTochange = "IMG";
            msg.obj = textTochange;
            //mHandler.sendMessage(msg);

            textTochange = fr.predict(m);
            mLikely=fr.getProb();
            msg = new Message();
            msg.obj = textTochange;
            mHandler.sendMessage(msg);

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

    private void invokeFingerAuthetication(String FingerPrint, String Latitude, String Longitude) {
        progressDialog = ProgressDialog.show(Recognize.this, "", "Capturing Attendance....", true);
        Log.e("values", FingerPrint + Latitude + Longitude);
        try {
            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(getString(R.string.URL))
                    .addConverterFactory(GsonConverterFactory.create());
            Retrofit retrofit = builder.build();
            CheckAttendance moduleInterface = retrofit.create(CheckAttendance.class);
            Call<ResponseBody> apirespone = moduleInterface.CheckAttendanceResponse(FingerPrint, Latitude, Longitude /*FingerPrint*/);
            apirespone.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String respones = response.body().string();
                        String Name = "";
                        progressDialog.dismiss();
                        if (!respones.isEmpty()) {
                            JSONObject jsonObject = new JSONObject(respones);
                            try {
                                Name = jsonObject.getString("EmpName");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            AlertUtil.showDialog(Recognize.this, "Success", Name + " Your attendance Updated Succesfully", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            AlertUtil.showDialog(Recognize.this, "Failure", "Not found,Please conatct admin for your attendance", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(Recognize.this,MainActivity.class);
                                    startActivity(intent);
                                }
                            });
                        }
                    } catch (Exception ex) {
                        progressDialog.dismiss();
                        AlertUtil.showDialog(Recognize.this, "Failure", "Not found,Please contact admin for your attendance", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(Recognize.this,MainActivity.class);
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
        } catch (Exception ex) {
            progressDialog.dismiss();
            AlertUtil.showDialog(Recognize.this, "Error", "Something went wrong ,please try after sometime", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent intent = new Intent(Recognize.this,MainActivity.class);
                    startActivity(intent);
                }
            });
            ex.printStackTrace();
        }
    }
//    because capitalize is the new black
    private String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
