package com.higashino_lab.t_amano.toyookauserapp;

import android.app.Activity;
<<<<<<< HEAD
import android.app.ActivityManager;
=======

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
>>>>>>> 43689fac813e94199cb128e630bf1d41cacf12e0
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
<<<<<<< HEAD
import android.util.Base64;
=======
import android.provider.CalendarContract;
import android.speech.tts.TextToSpeech;
import android.text.format.DateFormat;
>>>>>>> 43689fac813e94199cb128e630bf1d41cacf12e0
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
<<<<<<< HEAD
=======
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.RemoteMessage;
>>>>>>> 43689fac813e94199cb128e630bf1d41cacf12e0

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
<<<<<<< HEAD
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity
        implements Button.OnClickListener, View.OnSystemUiVisibilityChangeListener {
=======
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class MainActivity extends Activity implements MessageFragment.OnListFragmentInteractionListener {
>>>>>>> 43689fac813e94199cb128e630bf1d41cacf12e0

    private static final String TAG = "MainActivity";

    public static final String INTENT_KEY_RESERVATION_TYPE = "reservation_type";
    public static final String INTENT_KEY_AREA = "area";
    public static final String INTENT_KEY_USER_ID = "user_id";
    public static final String REQUEST_TAG = "request";

    private int RESERVATION_REQUEST_CODE = 101;
    private Map<String, String> basicAuthHeaders;

<<<<<<< HEAD
    private SimpleDateFormat sdf_date;
    private SimpleDateFormat sdf_time;
    private SimpleDateFormat sdf_datetime;

    private RequestQueue requestQueue;
    private int numOfRemainingRequests;
    private Timer syncTimer;

    private int userId = -1;
    private boolean isDebugUser = false;
    private String userScreenName;
    private String userFullName;
    private String area;
    private JSONObject userJson;
    private JSONArray messageJsonList;
    private String normalMessageString = null;
    private String topMessageString = null;
    private JSONArray informationJsonList;
    private String informationString = null;
    private JSONArray reservationJsonList;
    private JSONObject goingReservationJson;
    private JSONObject returningReservationJson;

    private int numOfGoolgePlayStartButtonClicked;
    private boolean screenLocked = true;
    private boolean nightMode = false;

    TextView topMessageTextView;
    TextView normalMessageTextView;
    TextView informationTextView;
    TextView returningBusRecoomendMessage;
    TextView goingReservationTime;
    TextView goingReservationStop;
    TextView returningReservationTime;
    TextView returningReservationStop;
    View reservedGoingBusContainer;
    View reservedReturningBusContainer;
    Button goingBusReservationButton;
    Button goingBusCancelButton;
    Button returningBusReservationButton;
    Button returningBusCancelButton;

=======
>>>>>>> 43689fac813e94199cb128e630bf1d41cacf12e0
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sdf_date = new SimpleDateFormat("yyyy-MM-dd");
        sdf_time = new SimpleDateFormat("HH:mm:ss");
        sdf_datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        initUiComponents();
        getIntentArguments();

        basicAuthHeaders = NetworkConnectionSingleton.getInstance(getApplicationContext()).basicAuthHeaders;
        requestQueue = NetworkConnectionSingleton.getInstance(getApplicationContext()).getRequestQueue();
    }

    private void initUiComponents() {
        topMessageTextView = (TextView) findViewById(R.id.top_message);

        normalMessageTextView = (TextView) findViewById(R.id.recommend_message);
        returningBusRecoomendMessage = (TextView) findViewById(R.id.returning_recommend_message);
        informationTextView = (TextView) findViewById(R.id.warning_message);

        goingBusReservationButton = (Button) findViewById(R.id.going_bus_reserve_button);
        returningBusReservationButton = (Button) findViewById(R.id.returning_bus_reserve_button);
        goingBusReservationButton.setOnClickListener(this);
        returningBusReservationButton.setOnClickListener(this);

        reservedGoingBusContainer = findViewById(R.id.reserved_going_bus_container);
        reservedReturningBusContainer = findViewById(R.id.reserved_returning_bus_container);

        goingBusCancelButton = (Button) findViewById(R.id.reserved_going_bus_cancel_button);
        returningBusCancelButton = (Button) findViewById(R.id.reserved_returning_bus_cancel_button);
        goingBusCancelButton.setOnClickListener(this);
        returningBusCancelButton.setOnClickListener(this);

        goingReservationStop = (TextView) findViewById(R.id.reserved_going_bus_stop_name);
        goingReservationTime = (TextView) findViewById(R.id.reserved_going_bus_stop_time);
        returningReservationStop = (TextView) findViewById(R.id.reserved_returning_bus_stop_name);
        returningReservationTime = (TextView) findViewById(R.id.reserved_returning_bus_stop_time);
    }

    private void getIntentArguments() {
        Intent argIntent = getIntent();
        String userJsonString =  argIntent.getStringExtra(LoginActivity.INTENT_KEY_USER_JSON);
        try {
            JSONObject userJson = new JSONObject(userJsonString);
            this.userId = userJson.getInt("id");
            this.userScreenName = userJson.getString("screen_name");
            this.isDebugUser = userJson.getBoolean("debug_mode");
            this.area = userJson.getString("area");
            this.userFullName = userJson.getString("full_name");

        } catch (JSONException e) {
            Log.d(TAG, "cannot read user json from intent");
            finish();
        }
    }

<<<<<<< HEAD
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        View decor = this.getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        decor.setOnSystemUiVisibilityChangeListener(this);

        startSyncTimer();
    }

    private void startSyncTimer() {

        int syncInterval;
        if (isNowNight()) {
            syncInterval = 1000 * 60 * 10;
            nightMode = true;
        } else {
            syncInterval = 1000 * 60;
            nightMode = false;
        }
        cancelSyncTimer();
        numOfRemainingRequests = 0;
        syncTimer = new Timer();
        syncTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runProcess();
            }
        }, 0, syncInterval);
    }

    private void changeNightMode() {
        int syncInterval = 1000*60;

        if (nightMode == isNowNight()) {
            return;

        } else if (nightMode && !isNowNight()) {
            nightMode = false;
            syncInterval = 1000 * 60;

        } else if (!nightMode && isNowNight()) {
            nightMode = true;
            syncInterval = 1000 * 60 * 10;

        }
        syncTimer = new Timer();
        syncTimer.schedule(new TimerTask() {
            @Override
            public void run() {
            runProcess();
            }
        }, 0, syncInterval);
    }

    private void cancelSyncTimer() {
        if (syncTimer != null) {
            syncTimer.cancel();
        }
        if (requestQueue != null) {
            requestQueue.cancelAll(REQUEST_TAG);
        }
    }

    private void runProcess() {
        numOfGoolgePlayStartButtonClicked = 0;
        screenLocked = true;
        sync();
        changeNightMode();
    }


    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.going_bus_reserve_button) {
            Button clickedCancelButton = (Button) view;
            clickedCancelButton.setEnabled(false);
            clickedCancelButton.setText(R.string.reserving_button_text);
            goToAskingActivity(RESERVATION_TYPE.GOING);

        } else if (viewId == R.id.returning_bus_reserve_button) {
            Button clickedCancelButton = (Button) view;
            clickedCancelButton.setEnabled(false);
            clickedCancelButton.setText(R.string.reserving_button_text);
            goToAskingActivity(RESERVATION_TYPE.RETURNING);

        } else if (viewId == R.id.reserved_going_bus_cancel_button) {
            Button clickedCancelButton = (Button) view;

            if (goingReservationJson != null) {
                clickedCancelButton.setEnabled(false);
                clickedCancelButton.setText(R.string.canceling_button_text);
                cancelReservationRequest(goingReservationJson);
            }

        } else if (viewId == R.id.reserved_returning_bus_cancel_button) {
            Button clickedCancelButton = (Button) view;
            if (returningReservationJson != null) {
                clickedCancelButton.setEnabled(false);
                clickedCancelButton.setText(R.string.cancel_button_text);
                cancelReservationRequest(returningReservationJson);
            }
        }
    }

    private void goToAskingActivity(RESERVATION_TYPE reservationType) {
        syncTimer.cancel();

        Intent intent = new Intent(getApplication(), AskingActivity.class);
        intent.putExtra(INTENT_KEY_RESERVATION_TYPE, reservationType);
        intent.putExtra(INTENT_KEY_AREA, area);
        intent.putExtra(INTENT_KEY_USER_ID, userId);
        startActivityForResult(intent, RESERVATION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);
        Log.d(TAG, "onActivityResult");
        startSyncTimer();
    }

    private void sync() {
        numOfRemainingRequests = 4;
        syncUser();
        syncReservations();
        syncInformation();
        syncMessages();
    }

    private void updateUIComponents() {
        Log.d(TAG, "updateUIcomp queue count: " +numOfRemainingRequests + " " + requestQueue.getSequenceNumber() );
        updateUser();
        updateMessage();
        updateInformation();
        updateReservations();
        updateUIVisibility();
    }

    private void syncUser() {
        String url = NetworkConnectionSingleton.URL +  "/api/users/" + userId + "/";
        String uri = Uri
                .parse(url)
                .buildUpon()
                .build().toString();
        Log.d(TAG, uri);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, uri, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        userJson = response;
                        numOfRemainingRequests--;
                        if (numOfRemainingRequests <= 0) {
                            updateUIComponents();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return basicAuthHeaders != null ? basicAuthHeaders : super.getHeaders();
            }
        };
        req.setTag(REQUEST_TAG);
        requestQueue.add(req);
    }

    private void syncMessages() {

        String url = NetworkConnectionSingleton.URL +  "/api/messages/";
=======
        startService(new Intent(MainActivity.this, PushReceiveService.class));

        sendMessageListRequest();
    }


    private void sendBookingListRequest() {
        String url = "http://13.114.35.78:50080/api/bookings/";
>>>>>>> 43689fac813e94199cb128e630bf1d41cacf12e0
        String uri = Uri
                .parse(url)
                .buildUpon()
                .appendQueryParameter("user", String.valueOf(userId))
                .build().toString();

        Log.d(TAG, uri);
        JsonArrayRequest req = new JsonArrayRequest(uri,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        messageJsonList = response;
                        numOfRemainingRequests--;
                        if (numOfRemainingRequests <= 0) {
                            updateUIComponents();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isDebugUser) {
                            Toast.makeText(MainActivity.this, "通信エラー", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return basicAuthHeaders != null ? basicAuthHeaders : super.getHeaders();
            }
        };

        req.setTag(REQUEST_TAG);
        requestQueue.add(req);
    }

    private void syncInformation() {
        String url = NetworkConnectionSingleton.URL +  "/api/information/";
        String uri = Uri
                .parse(url)
                .buildUpon()
                .appendQueryParameter("user", String.valueOf(userId))
                .build().toString();
        Log.d(TAG, uri);

        JsonArrayRequest req = new JsonArrayRequest(uri,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        informationJsonList = response;
                        numOfRemainingRequests--;
                        if (numOfRemainingRequests <= 0) {
                            updateUIComponents();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isDebugUser) {
                            Toast.makeText(MainActivity.this, "通信エラー", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return basicAuthHeaders != null ? basicAuthHeaders : super.getHeaders();
            }
        };
        req.setTag(REQUEST_TAG);
        requestQueue.add(req);
    }

    private void syncReservations() {
        String url = NetworkConnectionSingleton.URL +  "/api/reservations/";
        String uri = Uri
                .parse(url)
                .buildUpon()
                .appendQueryParameter("user", String.valueOf(userId))
                .appendQueryParameter("state", "reserved")
                .build().toString();
        Log.d(TAG, uri);

        JsonArrayRequest req = new JsonArrayRequest(uri,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        reservationJsonList = response;
                        Log.d(TAG, "reservationJsonList: " + reservationJsonList.toString());
                        numOfRemainingRequests--;
                        if (numOfRemainingRequests <= 0) {
                            updateUIComponents();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isDebugUser) {
                            Toast.makeText(MainActivity.this, "通信エラー", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return basicAuthHeaders != null ? basicAuthHeaders : super.getHeaders();
            }
        };
        req.setTag(REQUEST_TAG);
        requestQueue.add(req);
    }

    private void updateUser() {
        try {
            this.userFullName = userJson.getString("full_name");
            this.userScreenName = userJson.getString("screen_name");
            this.area = userJson.getString("area");
            this.isDebugUser = userJson.getBoolean("debug_mode");

        } catch (JSONException e) {

        }
    }

    private void updateMessage() {
        Log.d(TAG, "updateMessage");

        String normalMessage = null;
        String topMessage = null;
        try {
            for (int i = 0; i < messageJsonList.length(); i++) {
                JSONObject messageJson = messageJsonList.getJSONObject(i);
                String startTime = messageJson.getString("start_time");
                String endTime = messageJson.getString("end_time");
                String startDate = messageJson.getString("start_date");
                String endDate = messageJson.getString("end_date");

                if ((!isEnableTime(startTime, endTime)) || (!isEnableDate(startDate, endDate))) {
                    continue;
                }

                String text = messageJson.getString("text");
                String message_type = messageJson.getString("message_type");
                if (message_type.equals("normal")) {
                    normalMessage = text;
                } else if (message_type.equals("top")) {
                    topMessage = text;
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSON Exception: parsing message json" + e.getMessage());
        }

        if (normalMessage != null) {
            normalMessageString = fillText(normalMessage);
            normalMessageTextView.setText(normalMessageString);
        } else {
            normalMessageString = null;
        }

        if (topMessage != null) {
            topMessageString = fillText(topMessage);
            topMessageTextView.setText(topMessageString);
        } else {
            topMessageString = null;
        }
    }

    private void updateInformation() {
        Log.d(TAG, "updateInformation");

        String informationText = null;
        Log.d(TAG, "info: " + informationJsonList.toString());

        try {
            for (int i = 0; i < informationJsonList.length(); i++) {
                JSONObject informationJson = informationJsonList.getJSONObject(i);
                String startDate = informationJson.getString("start_date");
                String endDate = informationJson.getString("end_date");
                String targetArea = informationJson.getString("area");

                if ((!isEnableDate(startDate, endDate))) {
                    continue;
                }
                Log.d(TAG, targetArea);
                if (targetArea.equals("null")) {
                    Log.d(TAG, "target area is string null");
                } else {
                    Log.d(TAG, "target area is not stringnull");
                }
                if (!targetArea.equals("null") && !targetArea.equals(area)) {
                    continue;
                }
                informationText = informationJson.getString("text");

            }
        } catch (JSONException e) {
            Log.d(TAG, "JSON Exception: parsing message json" + e.getMessage());
        }

        if (informationText != null) {
            informationString = fillText(informationText);
            informationTextView.setText(informationString);
        } else {
            informationString = null;
        }
    }

    private void updateReservations() {
        Log.d(TAG, "updateReservatinos: " + reservationJsonList);

        Calendar cal = Calendar.getInstance();
        String todayDateString = sdf_date.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, 1);
        String tomorrowDateString = sdf_date.format(cal.getTime());

        boolean goingReservationExists = false;
        boolean returningReservationExists = false;
        try {
            for (int i = 0; i < reservationJsonList.length(); i++) {
                JSONObject reservationJson = reservationJsonList.getJSONObject(i);
                String busType = reservationJson.getString("bus_type");

                Date reservationDate =  sdf_date.parse(reservationJson.getString("date"));
                String reservationDateString = sdf_date.format(reservationDate);
                String todayOrTomorrow = "";
                if (reservationDateString.equals(todayDateString)) {
                    todayOrTomorrow = "今日";
                } else if (reservationDateString.equals(tomorrowDateString)) {
                    todayOrTomorrow = "明日";
                } else {
                    Log.d(TAG, "reservation date not match");
                    continue;
                }
                if (busType.equals("going")) {
                    this.goingReservationJson = reservationJson;
                    goingReservationExists = true;
                    updateReservation(todayOrTomorrow, true);

                } else if (busType.equals("returning")) {
                    this.returningReservationJson = reservationJson;
                    returningReservationExists = true;
                    updateReservation(todayOrTomorrow, false);
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSONE: " + e.getMessage());
        } catch (ParseException e) {
            Log.d(TAG, "ParseException");
        };

        if (!goingReservationExists) goingReservationJson = null;
        if (!returningReservationExists) returningReservationJson = null;

        if (goingReservationExists) {
            goingBusCancelButton.setEnabled(true);
            goingBusCancelButton.setText(R.string.cancel_button_text);
        } else {
            goingBusReservationButton.setEnabled(true);
            goingBusReservationButton.setText(R.string.going_reservation_button_text);
        }

        if (returningReservationExists) {
            returningBusCancelButton.setEnabled(true);
            returningBusCancelButton.setText(R.string.cancel_button_text);
        } else {
            returningBusReservationButton.setEnabled(true);
            returningBusReservationButton.setText(R.string.returning_reservation_button_text);
        }

        if (goingReservationExists && (!returningReservationExists)) {
            // 行きのバスだけ予約している
            returningBusReservationButton.setText(R.string.returning_reservation_button_text_with_going);
        } else {
        }
    }

    private void updateReservation(String todayOrTomorrow, boolean isGoing) {

        String stopLocationString = "";
        String stopTimeString = "";

        try {
            JSONObject reservationJson;
            if (isGoing) {
                reservationJson = goingReservationJson;
            } else {
                reservationJson = returningReservationJson;
            }
            JSONObject stopLocationJson = reservationJson.getJSONObject("stop_location");
            JSONObject stopJson = reservationJson.getJSONObject("stop");
            stopLocationString = stopLocationJson.getString("name");
            String[] goingStopTimeList = stopJson.getString("stop_time").split(":");
            stopTimeString = todayOrTomorrow + " " + goingStopTimeList[0] + "時" + goingStopTimeList[1] + "分";

        } catch (JSONException e) {
            return;
        }

        if (isGoing) {
            goingReservationTime.setText(stopTimeString);
            goingReservationStop.setText(stopLocationString);
        } else {
            returningReservationTime.setText(stopTimeString);
            returningReservationStop.setText(stopLocationString);
        }
    }


    private void updateUIVisibility() {
        // メソッド下のほうにあるsetVisibilityのほうがより優先されることに注意

        boolean goingBusReserved = goingReservationJson != null;
        boolean returningBusReserved = returningReservationJson != null;
        boolean topMessageExists = topMessageString != null;
        boolean normalMessageExists = normalMessageString != null;
        boolean informationExists = informationString != null;

        if (topMessageExists) {
            topMessageTextView.setVisibility(View.VISIBLE);
        }
        if (normalMessageExists) {
            normalMessageTextView.setVisibility(View.VISIBLE);
        } else {
            normalMessageTextView.setVisibility(View.GONE);
        }
        if (informationExists) {
            informationTextView.setVisibility(View.VISIBLE);
        } else {
            informationTextView.setVisibility(View.GONE);
        }

        if (goingBusReserved || returningBusReserved) {
            normalMessageTextView.setVisibility(View.GONE);
        }

        if (goingBusReserved) {
            goingBusReservationButton.setVisibility(View.GONE);
            reservedGoingBusContainer.setVisibility(View.VISIBLE);
        } else {
            goingBusReservationButton.setVisibility(View.VISIBLE);
            reservedGoingBusContainer.setVisibility(View.GONE);
        }

        if (returningBusReserved) {
            returningBusReservationButton.setVisibility(View.GONE);
            reservedReturningBusContainer.setVisibility(View.VISIBLE);
        } else {
            returningBusReservationButton.setVisibility(View.VISIBLE);
            reservedReturningBusContainer.setVisibility(View.GONE);
        }

        if (goingBusReserved && !returningBusReserved) {
            returningBusRecoomendMessage.setVisibility(View.VISIBLE);
        } else {
            returningBusRecoomendMessage.setVisibility(View.GONE);
        }
    }

    private void cancelReservationRequest(JSONObject reservationJson) {

        cancelSyncTimer();
        int reservationId;
        try {
            reservationId = reservationJson.getInt("id");
        } catch (JSONException e) {
            return;
        }

        String url = NetworkConnectionSingleton.URL +  "/api/reservations/" + reservationId + "/";
        JSONObject updateJson = new JSONObject();
        try {
            updateJson.put("state", "canceled");
        } catch (JSONException e) {

        }
        String uri = Uri
                .parse(url)
                .buildUpon()
                .build().toString();
        Log.d(TAG, uri);

        JsonObjectRequest patchRequest  = new JsonObjectRequest(Request.Method.PATCH, uri, updateJson,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        startSyncTimer();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "error: " + error.getMessage());
                        startSyncTimer();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return basicAuthHeaders != null ? basicAuthHeaders : super.getHeaders();
            }
        };
        patchRequest.setTag(REQUEST_TAG);
        requestQueue.add(patchRequest);
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(TAG, "ACtion: " +  event.getAction() + " " +event.getKeyCode());
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if (!isDebugUser) {
                    return true;
                }
                numOfGoolgePlayStartButtonClicked++;
                if (numOfGoolgePlayStartButtonClicked >= 9) {
                    screenLocked = false;
                }
                if (numOfGoolgePlayStartButtonClicked >= 10) {
                    screenLocked = false;
                    Toast.makeText(MainActivity.this, "Start Goolge Play", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://play.google.com/store"));
                    startActivity(intent);
                    numOfGoolgePlayStartButtonClicked = 0;
                }
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!screenLocked)
            return;

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(this.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {

        if (!screenLocked)
            return;
        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                    .getSystemService(this.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    private String fillText(String originalTextString) {
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日 E曜日");
        String todayString = sdf.format(today);

        String target = originalTextString;
        target = target.replace("{screen_name}", userScreenName);
        target = target.replace("{area}", area);
        target = target.replace("{full_name}", userFullName);
        target = target.replace("{today}", todayString);
        return target;
    }

    private boolean isEnableTime(String start_time, String end_time) {
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

    private boolean isEnableDate(String start_date, String end_date) {
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
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

    private boolean isNowNight() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour <= 5 || hour >= 20) {
            return true;
        } else {
            return false;
        }

    }


}
