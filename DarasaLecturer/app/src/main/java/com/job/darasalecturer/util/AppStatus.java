package com.job.darasalecturer.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.job.darasalecturer.appexecutor.DefaultExecutorSupplier;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Job on Friday : 8/3/2018.
 */


public class AppStatus {

    public static final String TAG = "AppStatus";
    private Boolean networking = false;

    static Context context;
    /**
     * We use this class to determine if the application has been connected to either WIFI Or Mobile
     * Network, before we make any network request to the server.
     * <p>
     * The class uses two permission - INTERNET and ACCESS NETWORK STATE, to determine the user's
     * connection stats
     */

    private static AppStatus instance = new AppStatus();
    ConnectivityManager connectivityManager;
    NetworkInfo wifiInfo, mobileInfo;
    boolean connected = false;

    public static AppStatus getInstance(Context ctx) {
        context = ctx.getApplicationContext();
        return instance;
    }

    /*
     * This only checks for status of WIFI and Mobile
     * Internet could be off!
     * */
    public boolean isOnline() {
        try {
            connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Log.v(TAG, e.toString());
        }
        return connected;
    }

    public boolean isNetworkAvailable() {
        if (isOnline()) {

            DefaultExecutorSupplier.getInstance().forBackgroundTasks()
                    .execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                HttpURLConnection urlc = (HttpURLConnection)
                                        (new URL("https://www.google.com/")
                                                .openConnection());

                                urlc.setRequestProperty("User-Agent", "Android");
                                urlc.setRequestProperty("Connection", "close");
                                urlc.setConnectTimeout(1500);
                                urlc.connect();
                                Log.d(TAG, "isNetworkAvailable: " + (urlc.getResponseCode() == 204 && urlc.getContentLength() == 0));

                                networking = (urlc.getResponseCode() == 204 &&
                                        urlc.getContentLength() == 0);
                            } catch (IOException e) {
                                Log.e(TAG, "Error checking internet connection", e);
                            }
                        }
                    });

            return networking;

        } else {
            Log.d(TAG, "No network available!");
        }
        return false;
    }
}
