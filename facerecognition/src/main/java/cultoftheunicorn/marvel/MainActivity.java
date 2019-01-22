package cultoftheunicorn.marvel;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.master.permissionhelper.PermissionHelper;

import org.opencv.cultoftheunicorn.marvel.R;

import ServiceAPI.Login;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    LinearLayout dashboarddata_layout, login_layout;
    Button capface,capfin,regface,regfin;
CardView loginbtn_layout;
    EditText username, password;
    ProgressDialog progressDialog;
    PermissionHelper permissionHelper;
    String typeof;
    boolean isback = false;
    boolean loginclick = false;
    boolean registerclick = false;
    Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        capface = (Button)findViewById(R.id.capfac);
        capfin = (Button)findViewById(R.id.capfin);
        regface = (Button)findViewById(R.id.regfac);
        regfin = (Button)findViewById(R.id.regfing);
        loginbtn_layout = (CardView)findViewById(R.id.loginbtn_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Marvel");
        }


        capface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, Recognize.class);

                startActivity(intent);
            }
        });
        capfin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                                Intent intent = new Intent(MainActivity.this, AttendanceActivity.class);
                startActivity(intent);
            }
        });
        regfin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    typeof = "regfing";
                isback = true;
                registerclick = true;
                dashboarddata_layout.setVisibility(View.GONE);
                login_layout.setVisibility(View.VISIBLE);

            }
        });
        regface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isback = true;
                registerclick = true;
                dashboarddata_layout.setVisibility(View.GONE);
                login_layout.setVisibility(View.VISIBLE);
                    typeof = "regface";

            }
        });


        dashboarddata_layout = (LinearLayout) findViewById(R.id.dashboarddata_layout);
        login_layout = (LinearLayout) findViewById(R.id.login_layout);
        Button recognizeButton = (Button) findViewById(R.id.recognizeButton);
        Button trainingButton = (Button) findViewById(R.id.trainingButton);
        CheckPermissions();

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

//        register_layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                isback = true;
//                registerclick = true;
//                dashboarddata_layout.setVisibility(View.GONE);
//                login_layout.setVisibility(View.VISIBLE);
//            }
//        });
//        captureattendance_layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, AttendanceActivity.class);
//                startActivity(intent);
//            }
//        });
        loginbtn_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                isback = true;
                loginclick = true;
                String Username = username.getText().toString();
                String Password = password.getText().toString();
                Log.e("clicked","clicked");
                if (!Username.isEmpty() && !Password.isEmpty()) {
                    progressDialog = ProgressDialog.show(MainActivity.this, "", "Please wait...", true);
                    InvokeLogin(Username, Password);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter Login credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
//
        recognizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Recognize.class));
            }
        });

        trainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NameActivity.class));
            }
        });



    }

    private void CheckPermissions() {
        permissionHelper = new PermissionHelper(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE}, 100);
        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                Log.d("TAG", "onPermissionGranted() called");
            }

            @Override
            public void onIndividualPermissionGranted(String[] grantedPermission) {
                Log.d("TAG", "onIndividualPermissionGranted() called with: grantedPermission = [" + TextUtils.join(",", grantedPermission) + "]");
            }

            @Override
            public void onPermissionDenied() {
                Log.d("TAG", "onPermissionDenied() called");
            }

            @Override
            public void onPermissionDeniedBySystem() {
                Log.d("TAG", "onPermissionDeniedBySystem() called");
            }
        });
    }

    private void InvokeLogin(String username, String password) {
        try {
            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(getString(R.string.URL))
                    .addConverterFactory(GsonConverterFactory.create());
            Retrofit retrofit = builder.build();
            Login moduleInterface = retrofit.create(Login.class);
            Call<ResponseBody> apirespone = moduleInterface.getLoginResponse(username, password);
            apirespone.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        String respones = response.body().string().replace("\"", "");
                        progressDialog.dismiss();
                        if (!respones.isEmpty()) {
                            if (respones.equalsIgnoreCase("SUCCESS")) {
                                Intent intent = new Intent(MainActivity.this, AttendanceActivity.class);
                                intent.putExtra("Login", "Login");
                                intent.putExtra("typeof",typeof);
                                startActivity(intent);
                            }
                        } else {
                            progressDialog.dismiss();
                            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                            alertDialog.setTitle("Failure");
                            alertDialog.setMessage("No data found,try with Valid Login credentials");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();

                        }
                    } catch (Exception ex) {
                        progressDialog.dismiss();

                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle("Failure");
                        alertDialog.setMessage("No data found,try with Valid Login credentials");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();

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

            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage("Something went wrong ,please try after sometime");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();

            ex.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (isback) {
            if (loginclick) {
                isback = false;
                dashboarddata_layout.setVisibility(View.VISIBLE);
                login_layout.setVisibility(View.GONE);
            } else {
                isback = false;
                dashboarddata_layout.setVisibility(View.VISIBLE);
                login_layout.setVisibility(View.GONE);
            }
        } else {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionHelper != null) {
            permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
