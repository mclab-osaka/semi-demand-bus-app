package com.higashino_lab.t_amano.toyookauserapp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AskingActivity extends Activity implements
        Button.OnClickListener,
        View.OnSystemUiVisibilityChangeListener,
        AskingStopFragment.OnAskingStopFragmentInteractionListener,
        AskingTimeFragment.OnAskingTimeFragmentInteractionListener {

    private final static String TAG = "AskingActivity";
    private final static String ASKING_REQUEST_TAG = "request asking";
    public final static String JSON_KEY_NAME = "name";
    public final static String JSON_KEY_ID = "id";

    private Map<String, String> basicAuthHeaders;

    private String area;
    private int userId;
    private RESERVATION_TYPE reservationType;

    private RequestQueue requestQueue;

    private JSONArray stopLocationJsonList;
    private JSONArray stopJsonTodayList;
    private JSONArray stopJsonTomorrowList;
    private JSONArray stopOutageJsonList;

    private String askingStopMessage;
    private String askingTimeMessage;

    private int selectedStopLocationId;
    private int selectedStopId;
    private String selectedStopName;
    private boolean isTomorrowReservation;
    private String busType;

    private SimpleDateFormat sdf_date;
    private SimpleDateFormat sdf_time;
    private SimpleDateFormat sdf_datetime;

    private Button backButton;

    private Timer finishTimer;

    private int remainingRequestCount = 0;

    private void getArgments() {
        Intent argIntent = getIntent();
        reservationType = (RESERVATION_TYPE) argIntent.getSerializableExtra(MainActivity.INTENT_KEY_RESERVATION_TYPE);
        area = argIntent.getStringExtra(MainActivity.INTENT_KEY_AREA);
        userId = argIntent.getIntExtra(MainActivity.INTENT_KEY_USER_ID, -1);
    }

    private void startFinishTimer() {

        if (finishTimer != null) {
            finishTimer.cancel();
        }

        finishTimer = new Timer();
        finishTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                backToMainActivityWithFailed();
                Log.d(TAG, "asking activity finished");
                if (requestQueue != null) {
                    requestQueue.cancelAll(ASKING_REQUEST_TAG);
                }
            }
        }, 1000*120, 1000*120);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asking);

        backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(this);

        requestQueue = NetworkConnectionSingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        basicAuthHeaders = NetworkConnectionSingleton.getInstance(getApplicationContext()).basicAuthHeaders;

        getArgments();
        sdf_date = new SimpleDateFormat("yyyy-MM-dd");
        sdf_time = new SimpleDateFormat("HH:mm:ss");
        sdf_datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String targetArea = "";
        if (reservationType == RESERVATION_TYPE.GOING) {
            targetArea = this.area;
            this.busType = "going";
            this.askingStopMessage = "どこから行きのバスに乗りますか？";
        } else if (reservationType == RESERVATION_TYPE.RETURNING) {
            targetArea = "mokutekiti";
            this.busType = "returning";
            this.askingStopMessage = "帰りのバスはどこから乗りますか？";
        }
        remainingRequestCount = 1;
        getStopLocations(targetArea);
        startFinishTimer();
    }

    private void getStopLocations(String targetArea) {

        String url = NetworkConnectionSingleton.URL +  "/api/stoplocations/";
        String uri = Uri
                .parse(url)
                .buildUpon()
                .appendQueryParameter("area", targetArea)
                .build().toString();

        Log.d(TAG, uri);
        JsonArrayRequest req = new JsonArrayRequest(uri,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        stopLocationJsonList = response;
                        remainingRequestCount--;
                        if (remainingRequestCount <= 0) {
                            remainingRequestCount = 0;
                            showStopLocationSelect();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AskingActivity.this, "通信エラー", Toast.LENGTH_SHORT).show();
                        backToMainActivityWithFailed();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return basicAuthHeaders != null ? basicAuthHeaders : super.getHeaders();
            }
        };
        req.setTag(ASKING_REQUEST_TAG);
        requestQueue.add(req);
    }

    private void getStopOutages() {
        String url = NetworkConnectionSingleton.URL +  "/api/stopoutage/";
        String uri = Uri
                .parse(url)
                .buildUpon()
                .appendQueryParameter("stop_location", String.valueOf(selectedStopLocationId))
                .build().toString();

        Log.d(TAG, uri);
        JsonArrayRequest req = new JsonArrayRequest(uri,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        stopOutageJsonList = response;
                        getStops();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AskingActivity.this, "通信エラー", Toast.LENGTH_SHORT).show();
                        backToMainActivityWithFailed();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return basicAuthHeaders != null ? basicAuthHeaders : super.getHeaders();
            }
        };
        req.setTag(ASKING_REQUEST_TAG);
        requestQueue.add(req);
    }

    private void getStops() {

        String url = NetworkConnectionSingleton.URL +  "/api/stops/";
        String uri = Uri
                .parse(url)
                .buildUpon()
                .appendQueryParameter("stop_location",  String.valueOf(selectedStopLocationId))
                .appendQueryParameter("bus_type",  busType)
                .build().toString();

        Log.d(TAG, uri);
        JsonArrayRequest req = new JsonArrayRequest(uri,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        stopJsonTomorrowList = selectTomorrowEnabledStops(response);
                        stopJsonTodayList = selectTodayEnabledStops(response);
                        showTimeSelect();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AskingActivity.this, "通信エラー", Toast.LENGTH_SHORT).show();
                        backToMainActivityWithFailed();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return basicAuthHeaders != null ? basicAuthHeaders : super.getHeaders();
            }
        };
        req.setTag(ASKING_REQUEST_TAG);
        requestQueue.add(req);
    }



    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_button) {
            backToMainActivityWithFailed();
        }
    }

    @Override
    public void onStopLocationButtonClicked(int stopLocationId, final String stopName) {
        startFinishTimer();
        Log.d(TAG, "clicked stop id: " + stopLocationId);
        this.selectedStopLocationId = stopLocationId;
        this.selectedStopName = stopName;

        getStopOutages();
    }

    @Override
    public void onTimeButtonClicked(boolean isTomorrowReservation, int stopId) {
        startFinishTimer();
        this.isTomorrowReservation = isTomorrowReservation;
        this.selectedStopId = stopId;
        postReservation();
    }

    private JSONArray selectTodayEnabledStops(JSONArray stopJsonList) {
        JSONArray resultJsonArray = new JSONArray();
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();

        if (isOutageDate(today)) {
            return resultJsonArray;
        }

        try {
            for (int i = 0; i < stopJsonList.length(); i++) {
                JSONObject stopJson = stopJsonList.getJSONObject(i);
                String reservation_enable_time = stopJson.getString("reservation_enable_time");
                String reservatoin_disable_time = stopJson.getString("reservation_disable_time");
                if (isTimeIncluded(reservation_enable_time, reservatoin_disable_time)) {
                    resultJsonArray.put(stopJson);
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSON Exceptin: " + e.getMessage());
        }
        return resultJsonArray;
    }

    private JSONArray selectTomorrowEnabledStops(JSONArray stopJsonList) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = cal.getTime();
        if (isOutageDate(tomorrow)) {
            return new JSONArray();
        } else {
            return stopJsonList;
        }
    }

    private void postReservation() {

        Calendar cal  = Calendar.getInstance();
        if (this.isTomorrowReservation) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        String dateString = sdf_date.format(cal.getTime());

        String uri = NetworkConnectionSingleton.URL + "/api/reservations/";
        Log.d(TAG, "url: " + uri);
        JSONObject postJson = new JSONObject();
        try {
            postJson.put("user", this.userId);
            postJson.put("stop_location", this.selectedStopLocationId);
            postJson.put("stop", this.selectedStopId);
            postJson.put("driver", 1);
            postJson.put("date", dateString);
            postJson.put("bus_type", busType);
            postJson.put("state", "reserved");
        } catch (JSONException e) {

        }
        Log.d(TAG, "data: " + postJson.toString());

        JsonObjectRequest postRequest  = new JsonObjectRequest(Request.Method.POST, uri, postJson,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        backToMainActivity();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "error");
                        backToMainActivityWithFailed();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return basicAuthHeaders != null ? basicAuthHeaders : super.getHeaders();
            }
        };
        postRequest.setTag(ASKING_REQUEST_TAG);
        requestQueue.add(postRequest);
    }

    private void showStopLocationSelect() {

        AskingStopFragment askingStopFragment = AskingStopFragment.newInstance(stopLocationJsonList, this.askingStopMessage);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.asking_fragment_container, askingStopFragment);
        fragmentTransaction.commit();
    }

    private void showTimeSelect() {

        if (stopJsonTodayList.length() == 0 && stopJsonTomorrowList.length() == 0) {
            askingTimeMessage = "今日・明日は予約できる" + selectedStopName + "発のバスがありません。";

        } else {
            if (reservationType == RESERVATION_TYPE.GOING) {
                askingTimeMessage = selectedStopName + "の何時発の行きのバスに乗りますか？";
            } else if (reservationType == RESERVATION_TYPE.RETURNING) {
                askingTimeMessage = selectedStopName + "の何時発の帰りのバスに乗りますか？";
            }
        }

        AskingTimeFragment askingTimeFragmnet = AskingTimeFragment.newInstance(stopJsonTodayList, stopJsonTomorrowList, this.askingTimeMessage);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.asking_fragment_container, askingTimeFragmnet);
        fragmentTransaction.commit();
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                    .getSystemService(this.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    protected void onPause() {
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                    .getSystemService(this.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void backToMainActivity() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(MainActivity.INTENT_KEY_RESERVATION_TYPE, this.reservationType);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void backToMainActivityWithFailed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(MainActivity.INTENT_KEY_RESERVATION_TYPE, this.reservationType);
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();
    }

    private boolean isOutageDate(Date date) {
        try {
            for (int i = 0; i < stopOutageJsonList.length(); i++) {
                JSONObject stopOutageJson = stopOutageJsonList.getJSONObject(i);
                String outageStartDate = stopOutageJson.getString("outage_start_date");
                String outageEndDate = stopOutageJson.getString("outage_end_date");
                if (isDateIncluded(date, outageStartDate, outageEndDate)) {
                    return true;
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "json e: " + e.getMessage());
        }
        return false;
    }

    private boolean isTimeIncluded(String start_time, String end_time) {
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        String currentDate = sdf_date.format(now);
        try {
            Date startTime = sdf_datetime.parse(currentDate + " " + start_time);
            Date endTime = sdf_datetime.parse(currentDate + " " + end_time);

            if (now.compareTo(startTime) >= 0 && now.compareTo(endTime) <= 0) {
                return true;
            }
        } catch (ParseException e) {
            Log.d(TAG, "ParseException: " +e.getMessage());
        }
        return false;
    }

    private boolean isDateIncluded(Date date, String start_date, String end_date) {
        Date now = date;
        try {
            Date startDate = sdf_date.parse(start_date);
            Date endDate = sdf_datetime.parse(end_date + " " + "23:59:59");

            if (now.compareTo(startDate) >= 0 && now.compareTo(endDate) <= 0) {
                return true;
            }
        } catch (ParseException e) {
            Log.d(TAG, "ParseException: " +e.getMessage());
        }
        return false;
    }
}
