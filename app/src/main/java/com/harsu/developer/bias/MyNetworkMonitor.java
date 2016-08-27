package com.harsu.developer.bias;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import helper.LoginController;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

public class MyNetworkMonitor extends BroadcastReceiver {
    private static final String TAG = "MyNetworkMonitor";
    private static boolean firstConnect = true;

    @Override
    public void onReceive(final Context context, Intent intent) {

        Log.d(TAG, "firstConnect : " + firstConnect);
        if (!LoginController.containsData(context)) {
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(LoginActivity.CREDENTIALS, MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("autoConnect", true))
            return;
        final NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (LoginController.isConnected(context))//wifi state is connected
        {
            //todo check if can ping google
            if (firstConnect) {
                firstConnect = false;
                Log.d(TAG,"Logging in");
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("Cyber Client")
                                .setContentText("Attempting to login");
                mNotifyMgr.notify(1, mBuilder.build());
                LoginController.login(context, new LoginController.ConnectionListener() {
                    @Override
                    public void success() {
                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(context)
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentTitle("Cyber Client")
                                        .setContentText("Successfully signed in");
                        //todo add a logout button on notification
                        mNotifyMgr.notify(1, mBuilder.build());
                    }

                    @Override
                    public void error(int error) {
                        firstConnect = false;
                        if (error == LoginController.Error.WRONG_WIFI)
                            mNotifyMgr.cancel(1);
                        else {
                            String message = "";
                            if (error == LoginController.Error.SERVER_ERRROR) {

                                message = "Server error";
                            } else if (error == LoginController.Error.DATA_LIMIT) {
                                message = "Data limit exceeded";
                            } else if (error == LoginController.Error.WRONG_CREDENTIALS) {
                                message = "Wrong Credentials! and how is this possible. Right?";
                            }
                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.mipmap.ic_launcher)
                                            .setContentTitle("Cyber Client")
                                            .setContentText(message);
                            //todo add a retry Button on notification
                            mNotifyMgr.notify(1, mBuilder.build());
                        }
                    }
                });
            }
        } else {
            firstConnect = true;
        }


    }
}