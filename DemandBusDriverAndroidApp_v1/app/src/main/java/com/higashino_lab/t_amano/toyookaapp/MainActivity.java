package com.higashino_lab.t_amano.toyookaapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements ItemFragment.OnListFragmentInteractionListener, OnMapReadyCallback, AdapterView.OnItemSelectedListener, LocationListener {

    private static final String TAG = "MainActivity";
    private static final String REQUEST_TAG = "request";
    private static final String REQUEST_LOCATION_TAG = "requet location";
    private Map<String, String> authHeaders;

    private double longitude = 0.0;
    private double latitude = 0.0;

    // View Parameters
    private SupportMapFragment mapFragment;
    private ItemFragment itemFragment;
    private TextView headerTextView;

    private boolean spinnerInitialized;

    // Model/Controller Parameters
    private int driverId;
    private Timer syncTimer;
    private int numOfRemainingRequests;

    private ArrayList<ReservationModel> reservationModelList;
    private JSONArray reservationJsonList;
    private JSONArray reservatoinOnBusJsonList;
    private JSONArray stopJsonList;

    private JSONArray stopLocationJsonArray;
    private JSONArray operatingTimeWindowJsonArray;
    private JSONArray reservationJsonArray;
    private JSONArray reservationSelectedJsonArray;
    private JSONArray reservationTodayTotalJsonArray;
    private JSONArray reservationTomorrowTotalJsonArray;

    private final static String token = ***;

    private GoogleMap map;
    private Spinner spinner;

    private void initAuthHeaders() {
        this.authHeaders= new HashMap<String, String>();
        this.authHeaders.put("Authorization", "JWT " + token);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_list:
                    if (itemFragment != null && !itemFragment.isVisible()) {
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.hide(mapFragment);
                        transaction.show(itemFragment);
                        transaction.commit();
                    }
                    return true;
                case R.id.navigation_map:
                    if (mapFragment != null && !mapFragment.isVisible()) {
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.hide(itemFragment);
                        transaction.show(mapFragment);
                        transaction.commit();
                    }
                    return true;
            }
            return false;
        }
    };

    private void spinnerInitialization() {
        this.spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.time_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        spinnerInitialized = false;
    }

    private void setSpinner(Spinner spinner, String[] arr){
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arr);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinnerInitialized = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        // Navigation Bar Initialization
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        headerTextView = (TextView) findViewById(R.id.header);
        headerTextView.setText("データ読み込み中");

        // this.operatingTimeWindowJsonArray = new JSONArray();
        // this.stopLocationJsonArray = new JSONArray();

        initAuthHeaders();
        spinnerInitialization();
        fragmentInitialization();

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10*1000, 10, this);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        startSyncTimer();
    }

    private void fragmentInitialization() {
        Log.d(TAG, "fragmentInitialization");

        this.itemFragment = ItemFragment.newInstance();
        this.mapFragment = SupportMapFragment.newInstance();
        this.mapFragment.getMapAsync(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.linercontent, mapFragment);
        transaction.hide(mapFragment);
        transaction.add(R.id.linercontent, itemFragment);
        transaction.commit();
    }

    private void startSyncTimer() {
        Log.d(TAG, "startSyncTimer");

        int syncInterval = 1000*20;
        if (syncTimer != null) {
            syncTimer.cancel();
        }

        numOfRemainingRequests = 0;
        syncTimer = new Timer();
        syncTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getReservations();
                getStopLocations();
            }
        }, 0, syncInterval);
    }

    /*
    private void cancelSyncTimer() {
        // syncTimer.cancel();
        RequestQueue requestQueue = NetworkConnectionSingleton.getInstance(getApplicationContext()).getRequestQueue();
        requestQueue.cancelAll(REQUEST_TAG);
    }
    */

    /*
    private void sync() {

        Log.d(TAG, "sync");

        numOfRemainingRequests = 2;
        if (this.stopJsonList == null) {
            numOfRemainingRequests += 1;
        }
        if (this.stopLocationJsonList == null) {
            numOfRemainingRequests += 1;
        }

        if (this.stopLocationJsonList == null) {
            syncStopLocations();
        }
        if (this.stopJsonList == null) {
            syncStops();
        }
        syncReservations("reserved");
        syncReservations("on_bus");
    }
    */

    private void getOperatingTimeWindows() {
        String url = NetworkConnectionSingleton.URL +  "operating-time-window/";
        String uri = Uri.parse(url).buildUpon().build().toString();
        Log.d(TAG, uri);

        JsonArrayRequest req = new JsonArrayRequest(uri,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        operatingTimeWindowJsonArray = response;
                        operatingTimeWindowUpdate();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "getOperatingTimeWindows: " + error.getMessage());
                        Toast.makeText(MainActivity.this, "通信エラー。ネットワーク接続を確認してください。", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return authHeaders != null ? authHeaders : super.getHeaders();
            }
        };
        RequestQueue requestQueue = NetworkConnectionSingleton.getInstance(this).getRequestQueue();
        requestQueue.add(req);
    }

    private void operatingTimeWindowUpdate() {
        Log.d(TAG, this.operatingTimeWindowJsonArray.toString());
    }

    private void getStopLocations() {
        String url = NetworkConnectionSingleton.URL +  "stop-location/";
        String uri = Uri.parse(url).buildUpon().build().toString();
        Log.d(TAG, uri);

        JsonArrayRequest req = new JsonArrayRequest(uri,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        stopLocationJsonArray = response;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "getStopLocations: " + error.getMessage());
                        Toast.makeText(MainActivity.this, "通信エラー。ネットワーク接続を確認してください。", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return authHeaders != null ? authHeaders : super.getHeaders();
            }
        };
        RequestQueue requestQueue = NetworkConnectionSingleton.getInstance(this).getRequestQueue();
        requestQueue.add(req);
    }

    private void stopLocationUpdated() {
        Log.d(TAG, this.stopLocationJsonArray.toString());
        updateMap();
    }

     private void getReservations() {
        String url = NetworkConnectionSingleton.URL +  "reservation/";
        String uri = Uri.parse(url).buildUpon().build().toString();
        Log.d(TAG, uri);

        JsonArrayRequest req = new JsonArrayRequest(uri,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if (reservationJsonArray == null) {
                            reservationJsonArray = response;
                            reservationUpdated();

                        } else if (!reservationJsonArray.toString().equals(response.toString())) {
                            reservationJsonArray = response;
                            reservationUpdated();

                        } else {
                            Log.d(TAG,"same reservation");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "getStopLocations: " + error.getMessage());
                        Toast.makeText(MainActivity.this, "通信エラー。ネットワーク接続を確認してください。", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return authHeaders != null ? authHeaders : super.getHeaders();
            }
        };
        RequestQueue requestQueue = NetworkConnectionSingleton.getInstance(this).getRequestQueue();
        requestQueue.add(req);
    }

    private void reservationUpdated() {
        Log.d(TAG, "reservation updated: " +  this.reservationJsonArray.toString());

        int otwLength = reservationJsonArray.length();
        String[] spinnerItemStrings = new String[otwLength];

        for (int i=0; i<reservationJsonArray.length(); i++) {
            try {
                JSONObject otwJson = reservationJsonArray.getJSONObject(i);
                spinnerItemStrings[i] = otwJson.getString("time_window_name");

            } catch (JSONException e) {
                Log.d(TAG, "json e");
            }
        }
        setSpinner(this.spinner, spinnerItemStrings);

        try {
            JSONObject todayTotal = reservationJsonArray.getJSONObject(0);
            reservationTodayTotalJsonArray = todayTotal.getJSONArray("reservations");
            JSONObject tomorrowTotal = reservationJsonArray.getJSONObject(reservationJsonArray.length()-1);
            reservationTomorrowTotalJsonArray = tomorrowTotal.getJSONArray("reservations");

        } catch (JSONException e) {

        }
        this.reservationSelectedJsonArray = reservationTodayTotalJsonArray;

        updateHeaderText();
        updateList();
        updateMap();
    }


    /*
    private void updateUIComponents() {

        Log.d(TAG, "updateUIComponents");

        if (itemFragment == null) {
            fragmentInitialization();
        }
        updateReservations();
        updateMapComponents();
    }
    */

    private void patchReservation(int reservationId, String updateReservationType) {

        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String timeString = sdf.format(today);

        String url = NetworkConnectionSingleton.URL +  "reservation/" + reservationId ;
        String uri = Uri.parse(url).buildUpon().build().toString();
        Log.d(TAG, uri);
        JSONObject updateJson = new JSONObject();

        try {
            updateJson.put("state", updateReservationType);
            if (updateReservationType.equals("on_bus")) {
                updateJson.put("on_time", timeString);

            } else if (updateReservationType.equals("off_bus")) {
                updateJson.put("off_location_longitude", longitude);
                updateJson.put("off_location_latitude", latitude);
                updateJson.put("off_time", timeString);
            }
        } catch (JSONException e) {

        }

        JsonObjectRequest patchRequest = new JsonObjectRequest(Request.Method.PATCH, uri, updateJson,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(MainActivity.this, "登録完了", Toast.LENGTH_SHORT).show();
                        getReservations();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "syncStops: " + error.getMessage());
                        Toast.makeText(MainActivity.this, "通信エラー", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return authHeaders != null ? authHeaders : super.getHeaders();
            }
        };
        RequestQueue requestQueue = NetworkConnectionSingleton.getInstance(this).getRequestQueue();
        patchRequest.setTag(REQUEST_TAG);
        requestQueue.add(patchRequest);
    }

    public void onPickupButtonClicked(final JSONObject reservationJson) {

        try {

            String firstName = reservationJson.getJSONObject("user").getString("first_name");
            String lastName = reservationJson.getJSONObject("user").getString("last_name");
            String stopTime = reservationJson.getJSONObject("stop_time").getString("display_time");
            String stopLocationName = reservationJson.getJSONObject("stop_location").getString("name");

            final int reservationId = reservationJson.getInt("id");

            AlertDialog.Builder alertDlg = new AlertDialog.Builder(this).setCancelable(false);
            // alertDlg.setTitle(clickedReservationModel.stopName);
            alertDlg.setMessage(stopTime + " " + stopLocationName + " での " + firstName +" " +lastName + " さんの乗車が完了しましたか？");
            alertDlg.setPositiveButton("はい", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    patchReservation(reservationId, "on_bus");
                }
            });
            alertDlg.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int a) {
                }
            });
            alertDlg.create().show();

        } catch (JSONException e) {

        }
    }

    public void onAbsenceButtonClicked(final JSONObject reservationJson) {

        try {
            String firstName = reservationJson.getJSONObject("user").getString("first_name");
            String lastName = reservationJson.getJSONObject("user").getString("last_name");
            String stopTime = reservationJson.getJSONObject("stop_time").getString("display_time");
            String stopLocationName = reservationJson.getJSONObject("stop_location").getString("name");

            final int reservationId = reservationJson.getInt("id");

            AlertDialog.Builder alertDlg = new AlertDialog.Builder(this).setCancelable(false);
            alertDlg.setMessage(stopTime + " " + stopLocationName + " での " + firstName +" " +lastName + " さんが不在でしたか？");
            alertDlg.setPositiveButton("はい", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    patchReservation(reservationId, "absence");
                }
            });
            alertDlg.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int a) {
                }
            });
            alertDlg.create().show();

        } catch (JSONException e) {

        }
    }

    public void onOffBusButtonClicked(final JSONObject reservationJson) {

        try {
            String firstName = reservationJson.getJSONObject("user").getString("first_name");
            String lastName = reservationJson.getJSONObject("user").getString("last_name");
            String stopTime = reservationJson.getJSONObject("stop_time").getString("display_time");
            String stopLocationName = reservationJson.getJSONObject("stop_location").getString("name");

            final int reservationId = reservationJson.getInt("id");

            AlertDialog.Builder alertDlg = new AlertDialog.Builder(this).setCancelable(false);
            alertDlg.setMessage(firstName + " " +lastName + " さんの降車が完了しましたか？");
            alertDlg.setPositiveButton("はい", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    patchReservation(reservationId, "off_bus");
                }
            });
            alertDlg.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int a) {
                }
            });
            alertDlg.create().show();

        } catch (JSONException e) {

        }
    }


    @Override
    public void onListFragmentCreateViewReady() {
        if (reservationModelList == null)
            return;
        itemFragment.setNewReservationJsonArray(this.reservationSelectedJsonArray);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(TAG, "onMapReady");

        this.map = googleMap;

        LatLng Kinosaki = new LatLng(35.619213, 134.816186);
        map.moveCamera(CameraUpdateFactory.zoomTo(14.0f));
        map.moveCamera(CameraUpdateFactory.newLatLng(Kinosaki));

        updateMap();
    }

    private void updateHeaderText() {

        String headerText = "";

        if (this.reservationSelectedJsonArray == this.reservationTodayTotalJsonArray) {
            headerText = "今日の予約 " + this.reservationTodayTotalJsonArray.length() + " 件";
        } else if (this.reservationSelectedJsonArray == this.reservationTomorrowTotalJsonArray) {
            headerText = "明日以降の予約 " + this.reservationTomorrowTotalJsonArray.length() + " 件";
        } else {
            headerText = "この時間帯の予約は " + this.reservationSelectedJsonArray.length() + "件です。\n（本日全時間帯では " + this.reservationTodayTotalJsonArray.length()+ " 件）";
        }
        headerTextView.setText(headerText);
    }

    private void updateList() {
        itemFragment.setNewReservationJsonArray(this.reservationSelectedJsonArray);
    }

    private void updateMap() {

        Log.d(TAG, "updateMap");

        if (map == null) {
            return;
        }
        map.clear();

        IconGenerator iconGeneratorNormal = new IconGenerator(this);
        IconGenerator iconGeneratorAccent = new IconGenerator(this);
        iconGeneratorAccent.setStyle(IconGenerator.STYLE_RED);

        // Cuurent Location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }

        // Update Bus Stop Location
        Log.d(TAG, "check stopLocationJsonArray is null");
        if (stopLocationJsonArray == null) {
            Log.d(TAG, "stopLocationJsonArray is null");
            return;
        }
        Log.d(TAG, stopLocationJsonArray.toString());

        for (int i=0; i<stopLocationJsonArray.length(); i++) {
            try {
                JSONObject stopLocationJson = stopLocationJsonArray.getJSONObject(i);
                LatLng position = new LatLng(stopLocationJson.getDouble("latitude"), stopLocationJson.getDouble("longitude"));

                Circle circle = map.addCircle(new CircleOptions()
                        .center(position)
                        .radius(10)
                        .strokeWidth(10)
                        .strokeColor(Color.RED)
                        .fillColor(Color.RED)
                );
                circle.setVisible(true);

            } catch (JSONException e) {
                Log.d(TAG, "json e");
            }
        }

        // Update Reservation Point Location
        if (reservationSelectedJsonArray == null) {
            return;
        }

        for (int i=0; i<reservationSelectedJsonArray.length(); i++) {
            try {
                JSONObject reservationJson = reservationSelectedJsonArray.getJSONObject(i);
                String userName = reservationJson.getJSONObject("user").getString("first_name");
                String state = reservationJson.getString("state");
                JSONObject stopLocationJson = reservationJson.getJSONObject("stop_location");
                JSONObject stopTimeJson = reservationJson.getJSONObject("stop_time");
                double stopLocationLon = stopLocationJson.getDouble("longitude");
                double stopLocationLat = stopLocationJson.getDouble("latitude");
                String stopLocationName = stopLocationJson.getString("name");
                String stopTime = stopTimeJson.getString("display_time");

                if (!state.equals("reserved")) {
                    continue;
                }

                LatLng position = new LatLng(stopLocationLat, stopLocationLon);

                Bitmap iconBitmap;
                iconBitmap = iconGeneratorNormal.makeIcon(stopLocationName + "\n" + stopTime + " " + userName +"さん");

                Marker marker = map.addMarker(new MarkerOptions()
                        .position(position)
                        .icon(BitmapDescriptorFactory.fromBitmap(iconBitmap))
                        .alpha((float) 0.8)
                );
                marker.setVisible(true);
            } catch (JSONException e) {
                Log.d(TAG, "json e");
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {

        if (this.reservationJsonArray == null) {
            return;
        }

        try {
            JSONObject targetReservationOwtJson = this.reservationJsonArray.getJSONObject(pos);
            this.reservationSelectedJsonArray = targetReservationOwtJson.getJSONArray("reservations");

            updateList();
            updateHeaderText();
            updateMap();
        } catch (JSONException e) {

        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {
            this.longitude = location.getLongitude();
            this.latitude = location.getLatitude();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int i, Bundle intent) {}

    @Override
    public void onListFragmentInteraction(JSONObject reservationJson) {
    }

}

