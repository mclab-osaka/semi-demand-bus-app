package com.higashino_lab.t_amano.toyookaapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.net.ConnectivityManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

// Authenticaiton, Loading Data, ErrorCheck
public class StartActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private final String TAG = "StartActivity";
    private final int  LOCATION_REQUEST_CODE = 1000;

    private int driverId;
    private Timer syncTimer;
    private int numOfRemainingRequests;

    private JSONArray reservationJsonList;

    private boolean userDisabledLocationPermission = false; // ユーザが意図して位置情報を拒否した

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        // 位置情報OKかつオンラインかつログイン済みならMainActivityへ

        if (!isOnline()) {
            Toast.makeText(StartActivity.this, "ネットワークに接続できません．", Toast.LENGTH_LONG).show();
            return;
        }

        if ((!userDisabledLocationPermission) && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(StartActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_REQUEST_CODE);
            }
        } else {
            goToMainActivity();
        }
    }

    private void showProgressDialog() {
        this.progressDialog = new ProgressDialog(StartActivity.this);
        progressDialog.setTitle("通信中");
        progressDialog.setMessage("データを取得しています");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null)
            progressDialog.hide();
    }

    public void goToMainActivity() {
        Intent intent = new Intent(getApplication(), MainActivity.class);
        // intent.putExtra("json", jsonArray.toString());
        startActivity(intent);
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("debug","checkSelfPermission true");
                return;
            } else {
                // 位置情報が拒否されたときはとりあえず，MainActivityへ
                userDisabledLocationPermission = true;
            }
        }
    }
}
