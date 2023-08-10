package com.higashino_lab.t_amano.toyookauserapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements
            GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private static final String SP_KEY = "UserData";
    private static final String SP_USER_ID_KEY = "userId";
    private static final String SP_USER_JSON_KEY = "userJson";

    private static  final String DUMMY_MAIL_ADDRESS = ***;

    public static final String INTENT_KEY_USER_JSON = "userJson";
    public static final int RC_SIGN_IN = 100;

    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences sharedPreferences;
    private Map<String, String> basicAuthHeaders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.basicAuthHeaders = NetworkConnectionSingleton.getInstance(this).basicAuthHeaders;
        goToMainIfAuthenticated();
    }

    private void goToMainIfAuthenticated() {

        // Check Account
        sharedPreferences = getSharedPreferences(SP_KEY, Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt(SP_USER_ID_KEY, -1);
        String userJsonString = sharedPreferences.getString(SP_USER_JSON_KEY, "");
        JSONObject userJson = new JSONObject();
        try {
            userJson = new JSONObject(userJsonString);

        } catch (JSONException e) {

        }
        if (userId != -1) {
            Log.d(TAG, "Already Authenticated, UserId: " + userId);
            // 認証済み
            Log.d(TAG, "Authenticated");
            goToMainActivity(userJson);
        } else {
<<<<<<< HEAD
            Log.d(TAG, "Not yet Authentication, First Time Laucnch");
=======
            Log.d(TAG, "Not Authenticated");
>>>>>>> 08b48b210c47d608430229e41480c0a528af0397
            // 未認証
            googleAuthRequest();
        }
    }

    private void googleAuthRequest() {
        // 未認証
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton button = (SignInButton) findViewById(R.id.sign_in_button);
        button.setOnClickListener(this);
        // LoginResult is catched by onActivityResult
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (!result.isSuccess()) {
                Log.d(TAG, "login failed" + result.getStatus().getStatusMessage());
                Toast.makeText(LoginActivity.this, "ログイン失敗", Toast.LENGTH_SHORT).show();

                return;
            }
            GoogleSignInAccount account = result.getSignInAccount();
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);

            syncUserId(account.getEmail());
            // syncUserId(DUMMY_MAIL_ADDRESS);
        }
    }

    private void syncUserId(String userMailAddress) {
        String url = NetworkConnectionSingleton.URL + "/api/users";
        String uri = Uri.parse(url)
                .buildUpon()
                .appendQueryParameter("mail", userMailAddress)
                .build().toString();

        RequestQueue requestQueue = NetworkConnectionSingleton.getInstance(getApplicationContext()).getRequestQueue();
        JsonArrayRequest req = new JsonArrayRequest(uri,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        handleUserDataResponse(response);
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
        requestQueue.add(req);
    }


    private void handleUserDataResponse(JSONArray jsonArray) {
        Log.d(TAG, jsonArray.toString());

        JSONObject userJson;
        int userId;

        if (jsonArray.length() == 0) {
            Toast.makeText(LoginActivity.this, "登録されていないユーザーです", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            userJson = jsonArray.getJSONObject(0);
            userId = userJson.getInt("id");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(SP_USER_ID_KEY, userId);
            editor.putString(SP_USER_JSON_KEY, userJson.toString());
            editor.apply();

            goToMainIfAuthenticated();

        } catch (JSONException e) {

        }
    }

    public void onClick(View view) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void goToMainActivity(JSONObject userJson) {
        Intent intent = new Intent(getApplication(), MainActivity.class);
        intent.putExtra(INTENT_KEY_USER_JSON, userJson.toString());
        startActivity(intent);
    }
}
