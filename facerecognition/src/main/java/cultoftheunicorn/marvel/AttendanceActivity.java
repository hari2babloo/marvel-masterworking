package cultoftheunicorn.marvel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.multidots.fingerprintauth.AuthErrorCodes;
import com.multidots.fingerprintauth.FingerPrintAuthCallback;
import com.multidots.fingerprintauth.FingerPrintAuthHelper;
import com.multidots.fingerprintauth.FingerPrintUtils;

import org.json.JSONObject;
import org.opencv.cultoftheunicorn.marvel.R;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import ServiceAPI.AlertUtil;
import ServiceAPI.CheckAttendance;
import ServiceAPI.RegisterUser;
import ServiceAPI.SimpleLocation;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AttendanceActivity extends AppCompatActivity implements FingerPrintAuthCallback {

    private TextView mAuthMsgTv;
    private ViewSwitcher mSwitcher;
    private Button mGoToSettingsBtn;
    private FingerPrintAuthHelper mFingerPrintAuthHelper;
    ProgressDialog progressDialog;
    private static final String SECRET_MESSAGE = "Very secret message";

    ImageView search;
    Toolbar mToolbar;

    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    EditText emploee_id;
    private TextView textView;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    CardView SignUp_layout;
   ImageView cambtn,fingbtn;
    SimpleLocation mLocation;
    String employeeid = "";
    boolean isregistered = false;
    TelephonyManager telephonyManager;
    String IMEI_Number_Holder = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        mLocation = new SimpleLocation(AttendanceActivity.this);
        mLocation.setBlurRadius(5000);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        configuretoolbar();
        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        // if we can't access the location yet
        if (!mLocation.hasLocationEnabled()) {
            // ask the user to enable location access
            SimpleLocation.openSettings(AttendanceActivity.this);
        }
        mLocation.setListener(new SimpleLocation.Listener() {
            public void onPositionChanged() {
                // new location data has been received and can be accessed
            }

        });
        final Intent intent = getIntent();
        emploee_id = (EditText) findViewById(R.id.emploee_id);
        cambtn = (ImageView) findViewById(R.id.cambtn);
        cambtn.setVisibility(View.GONE);
        fingbtn = (ImageView)findViewById(R.id.fngrbtn);
        fingbtn.setVisibility(View.GONE);
        search = (ImageView)findViewById(R.id.search);



        search.setVisibility(View.GONE);
        if (intent.hasExtra("Login")) {
            isregistered = true;
            emploee_id.setVisibility(View.VISIBLE);
        } else {
            emploee_id.setVisibility(View.GONE);
            fingbtn.setVisibility(View.VISIBLE);
        }
        emploee_id.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() >= 2){

                    search.setVisibility(View.VISIBLE);
//                    if (intent.getStringExtra("typeof").equalsIgnoreCase("regfing")){
//
//
//                        cambtn.setVisibility(View.GONE);
//                        fingbtn.setVisibility(View.VISIBLE);
//
//
//
//                    }else if (intent.getStringExtra("typeof").equalsIgnoreCase("regface")){
//
//                        fingbtn.setVisibility(View.GONE);
//                        cambtn.setVisibility(View.VISIBLE);
//
//                    }
                }

                else {

                    search.setVisibility(View.GONE);
                }

            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (intent.getStringExtra("typeof").equalsIgnoreCase("regfing")){


                    cambtn.setVisibility(View.GONE);
                    fingbtn.setVisibility(View.VISIBLE);




                }else if (intent.getStringExtra("typeof").equalsIgnoreCase("regface")){

                    Intent inte = new Intent(AttendanceActivity.this,Training.class);
                        inte.putExtra("name", emploee_id.getText().toString().trim());
                        startActivity(inte);
//                    fingbtn.setVisibility(View.GONE);

                }
            }
        });



        mGoToSettingsBtn = (Button) findViewById(R.id.go_to_settings_btn);
        mGoToSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FingerPrintUtils.openSecuritySettings(AttendanceActivity.this);
            }
        });

//        cambtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intenttt = getIntent();
//                if (intenttt.hasExtra("Login")) {
//                    isregistered = true;
//                    emploee_id.setVisibility(View.VISIBLE);
//                    if (emploee_id.getText().toString().isEmpty()){
//
//                        Log.e("Make EmployeeID Visible","Make EmployeeID Visible");
//
//                        Toast.makeText(AttendanceActivity.this, "Enter Employee ID", Toast.LENGTH_SHORT).show();
//                    }
//                    else {
//
//                        Intent inte = new Intent(AttendanceActivity.this,Training.class);
//                        inte.putExtra("name", emploee_id.getText().toString().trim());
//                        startActivity(inte);
//
//                    }
//                } else {
//
//                    Intent inte = new Intent(AttendanceActivity.this,Recognize.class);
//                    //inte.putExtra("name", emploee_id.getText().toString().trim());
//                    startActivity(inte);
//
//                    emploee_id.setVisibility(View.GONE);
//                }
//
//
//                }
//        });
        mAuthMsgTv = (TextView) findViewById(R.id.auth_message_tv);

        mFingerPrintAuthHelper = FingerPrintAuthHelper.getHelper(this, this);

    }

    @SuppressLint("RestrictedApi")
    private void configuretoolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoToSettingsBtn.setVisibility(View.GONE);
        mAuthMsgTv.setVisibility(View.VISIBLE);
        mAuthMsgTv.setText("Place your finger on Scanner to scan");
        //start finger print authentication
        mFingerPrintAuthHelper.startAuth();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mFingerPrintAuthHelper.stopAuth();
        }
    }

    @Override
    public void onNoFingerPrintHardwareFound() {
        mAuthMsgTv.setText("Your device does not have finger print scanner. Please type 1234 to authenticate.");
    }


    @Override
    public void onNoFingerPrintRegistered() {

        mAuthMsgTv.setText("There are no finger prints registered on this device. Please register your finger from settings.");
        mGoToSettingsBtn.setVisibility(View.VISIBLE);

    }

    @Override
    public void onBelowMarshmallow() {
        mAuthMsgTv.setText("You are running older version of android that does not support finger print authentication. Please type 1234 to authenticate.");

    }

    @Override
    public void onAuthSuccess(FingerprintManager.CryptoObject cryptoObject) {
        try {
            if (ActivityCompat.checkSelfPermission(AttendanceActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ex.printStackTrace();
        }
        IMEI_Number_Holder = telephonyManager.getDeviceId();
        progressDialog = ProgressDialog.show(AttendanceActivity.this, "", "Checking....", true);
        if (isregistered) {
            employeeid = emploee_id.getText().toString();
            if (!employeeid.isEmpty()) {
                Registeruser(IMEI_Number_Holder/*tryEncrypt(cryptoObject.getCipher())*/, employeeid);
            } else {
                progressDialog.dismiss();


                AlertDialog alertDialog = new AlertDialog.Builder(AttendanceActivity.this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Please enter EmployeeId");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();

            }
        } else {
            invokeFingerAuthetication(IMEI_Number_Holder/*tryEncrypt(cryptoObject.getCipher())*/, String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude()));
        }
    }

    private void invokeFingerAuthetication(String FingerPrint, String Latitude, String Longitude) {
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
                            AlertUtil.showDialog(AttendanceActivity.this, "Success", Name + " Your attendance Updated Succesfully", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            AlertUtil.showDialog(AttendanceActivity.this, "Failure", "Not found,Please conatct admin for your attendance", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                        }
                    } catch (Exception ex) {
                        progressDialog.dismiss();
                        AlertUtil.showDialog(AttendanceActivity.this, "Failure", "Not found,Please conatct admin for your attendance", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
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
            AlertUtil.showDialog(AttendanceActivity.this, "Error", "Something went wrong ,please try after sometime", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            ex.printStackTrace();
        }
    }

    private void Registeruser(String FingerPrint, String EmployeId) {
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
                                AlertUtil.showDialog(AttendanceActivity.this, "Success", Name, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                            }
                        } else if (response.code() == 400) {
                            progressDialog.dismiss();
                            AlertUtil.showDialog(AttendanceActivity.this, "Failure", "User already taken", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                        }
                    } catch (Exception ex) {
                        progressDialog.dismiss();
                        AlertUtil.showDialog(AttendanceActivity.this, "Failure", "Not data found,please try after some time", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
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
            AlertUtil.showDialog(AttendanceActivity.this, "Error", "Something went wrong ,please try after sometime", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            ex.printStackTrace();
        }
        progressDialog.dismiss();
    }

    @Override
    public void onAuthFailed(int errorCode, String errorMessage) {
        switch (errorCode) {
            case AuthErrorCodes.CANNOT_RECOGNIZE_ERROR:
                mAuthMsgTv.setText("Cannot recognize your finger print. Please try again.");
                break;
            case AuthErrorCodes.NON_RECOVERABLE_ERROR:
                mAuthMsgTv.setText("Cannot initialize finger print authentication. Please type 1234 to authenticate.");
//                mSwitcher.showNext();
                break;
            case AuthErrorCodes.RECOVERABLE_ERROR:
                mAuthMsgTv.setText(errorMessage);
                break;
        }
    }
}
