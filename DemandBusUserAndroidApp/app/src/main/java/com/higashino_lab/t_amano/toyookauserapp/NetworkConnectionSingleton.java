package com.higashino_lab.t_amano.toyookauserapp;

import android.content.Context;
import android.util.AndroidRuntimeException;
import android.util.Base64;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NetworkConnectionSingleton {

    public static final String URL = ***
    public static final String BASIC_USER_NAME = ***
    public static final String BASIC_PASSWORD = ***
    public Map<String, String> basicAuthHeaders;

    private static NetworkConnectionSingleton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private NetworkConnectionSingleton(Context context) {
        mCtx = context;
        initBasicAuthHeaders();
        mRequestQueue = getRequestQueue();
    }

    public static synchronized NetworkConnectionSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NetworkConnectionSingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            final HurlStack stack = new HurlStack(null, getAllAllowsSocketFactory());
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext(), stack);
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    private void initBasicAuthHeaders() {
        String userpassword = NetworkConnectionSingleton.BASIC_USER_NAME + ":" + NetworkConnectionSingleton.BASIC_PASSWORD;
        final String encoded =
                new String(Base64.encode(userpassword.getBytes(), Base64.DEFAULT));
        this.basicAuthHeaders = new HashMap<String, String>();
        this.basicAuthHeaders.put("Authorization", "Basic " + encoded);
    }

    private static SSLSocketFactory getAllAllowsSocketFactory() {
        try {
            // ホスト名検証をスキップする
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            // 証明書検証スキップする空の TrustManager
            final TrustManager[] manager = {new X509TrustManager() {

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    // do nothing
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    // do nothing
                }
            }};
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, manager, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new AndroidRuntimeException(e);
        }
    }

}
