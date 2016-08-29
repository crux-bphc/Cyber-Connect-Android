package com.harsu.developer.bias;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import helper.LoginController;
import helper.StatusStorer;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

public class MyNetworkMonitor extends BroadcastReceiver {
    private static final String TAG = "MyNetworkMonitor";

    private static boolean firstConnect = true;
    SharedPreferences sharedPreferences;

    StatusStorer statusStorer;

    @Override
    public void onReceive(final Context context, Intent intent) {

        Log.d(TAG, "firstConnect : " + firstConnect);
        if (!LoginController.containsData(context)) {
            return;
        }

        statusStorer = StatusStorer.getInstance();

        sharedPreferences = context.getSharedPreferences(LoginActivity.CREDENTIALS, MODE_PRIVATE);


        final NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent(context, LoginActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (LoginController.isConnected(context))//wifi state is connected
        {
            statusStorer.setStatus(StatusStorer.Status.CONNECTED);

            if (firstConnect) {
                LoginController.checkGoogleServer(new LoginController.ConnectionListener() {
                    @Override
                    public void success() {
                        //already signed in
                        if (statusStorer.getStatus() == StatusStorer.Status.CONNECTED)
                            firstConnect = false;
                        statusStorer.setStatus(StatusStorer.Status.ALREADY_LOGGED_IN);
                    }

                    @Override
                    public void error(int error) {
                        //not signed in, or internet is extremely slow, nothing can be done

                        if (!sharedPreferences.getBoolean("autoConnect", true))
                            return;
                        if (firstConnect)
                            attemptLogIn(context, pendingIntent, mNotifyMgr);

                    }
                });

            }
        } else {
            mNotifyMgr.cancel(1);
            statusStorer.setStatus(StatusStorer.Status.DISCONNECTED);
            firstConnect = true;
        }
    }

    private void attemptLogIn(final Context context, final PendingIntent pendingIntent, final NotificationManager mNotifyMgr) {
        firstConnect = false;
        statusStorer.setStatus(StatusStorer.Status.LOGGING_IN);
        Log.d(TAG, "Logging in");
        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setVibrate(new long[]{1, 1, 1})
                        .setContentTitle("Cyber Connect")
                        .setContentText("Attempting to login")
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentIntent(pendingIntent);
        mNotifyMgr.notify(1, mBuilder.build());
        LoginController.login(context, new LoginController.ConnectionListener() {
            @Override
            public void success() {
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setVibrate(new long[]{1, 1, 1})
                                .setContentTitle("Cyber Connect")
                                .setContentText("Successfully signed in")
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setContentIntent(pendingIntent);
                //todo add a logout button on notification
                mNotifyMgr.notify(1, mBuilder.build());
            }

            @Override
            public void error(int error) {

                firstConnect = false;
                statusStorer.setError(error);
                if (error == LoginController.Error.WRONG_WIFI) {
                    mNotifyMgr.cancel(1);
                    statusStorer.setStatus(StatusStorer.Status.NOT_BITS_NETWORK);

                } else {
                    statusStorer.setStatus(StatusStorer.Status.ERROR_LOGGING);
                    String message = "";
                    if (error == LoginController.Error.SERVER_ERROR) {
                        message = "Server error";
                    } else if (error == LoginController.Error.DATA_LIMIT) {
                        message = "Data limit exceeded";
                    } else if (error == LoginController.Error.WRONG_CREDENTIALS) {
                        message = "Wrong Credentials! and how is this possible. Right?";
                    }
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                                    .setVibrate(new long[]{1, 1, 1})
                                    .setContentTitle("Cyber Connect")
                                    .setContentText(message)
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setContentIntent(pendingIntent);
                    //todo add a retry Button on notification
                    mNotifyMgr.notify(1, mBuilder.build());
                }
            }
        });
    }


}