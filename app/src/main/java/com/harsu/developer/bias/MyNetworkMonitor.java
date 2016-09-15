package com.harsu.developer.bias;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import helper.AccountsTableManager;
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
        Intent i = new Intent(context, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent intent1 = new Intent(context, LoginActivity.class);
        intent1.putExtra("extra", 1);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final PendingIntent actionClick = PendingIntent.getActivity(context, 124, intent, PendingIntent.FLAG_CANCEL_CURRENT);

//        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 123, i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (LoginController.isConnected(context))//wifi state is connected
        {
            statusStorer.setStatus(StatusStorer.Status.CONNECTED, context);

            if (firstConnect) {
                statusStorer.setState(StatusStorer.State.active);
                LoginController.getLogInID(new LoginController.ConnectionListener() {
                    @Override
                    public void success() {
                        //already signed in

                        statusStorer.setState(StatusStorer.State.dormant);
                        if (statusStorer.getStatus() == StatusStorer.Status.CONNECTED) {
                            /*NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.notif_icon)
                                            .setPriority(NotificationCompat.PRIORITY_MAX)
                                            .setVibrate(new long[]{1, 1, 1})
                                            .setContentTitle("Cyber Connect")
                                            .setContentText("Network is Active")
                                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                            .setContentIntent(pendingIntent);
                            mNotifyMgr.notify(1, mBuilder.build());*/
                            firstConnect = false;
                        }
                        statusStorer.setStatus(StatusStorer.Status.ALREADY_LOGGED_IN, context);
                    }

                    @Override
                    public void error(LoginController.Error error) {
                        //not signed in, or internet is extremely slow, nothing can be done

                        if (error == LoginController.Error.WRONG_WIFI) {
                            statusStorer.setState(StatusStorer.State.dormant);
                            statusStorer.setStatus(StatusStorer.Status.NOT_BITS_NETWORK, context);
                            return;
                        }
                        if (!sharedPreferences.getBoolean("autoConnect", true)) {
                            statusStorer.setState(StatusStorer.State.dormant);
                            statusStorer.setStatus(StatusStorer.Status.CONNECTED, context);
                            return;
                        }
                        if (firstConnect)
                            attemptLogIn(context, pendingIntent, mNotifyMgr, actionClick);

                    }
                }, context);

            }
        } else {
            mNotifyMgr.cancel(1);
            statusStorer.setStatus(StatusStorer.Status.DISCONNECTED,context);
            firstConnect = true;
            AccountsTableManager accountsTableManager = new AccountsTableManager(context);
            accountsTableManager.setLoggedIn("");
            statusStorer.setState(StatusStorer.State.active);
        }
    }

    private void attemptLogIn(final Context context, final PendingIntent pendingIntent, final NotificationManager mNotifyMgr, final PendingIntent actionClick) {

        firstConnect = false;
        statusStorer.setStatus(StatusStorer.Status.LOGGING_IN,context);
        Log.d(TAG, "Logging in");
        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notif_icon)
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
                statusStorer.setState(StatusStorer.State.dormant);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.notif_icon)
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setVibrate(new long[]{1, 1, 1})
                                .setContentTitle("Cyber Connect")
                                .setContentText("Successfully signed in")
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setContentIntent(pendingIntent);

//                mBuilder.addAction(R.drawable.ic_close, "Logout", actionClick);
                mNotifyMgr.notify(1, mBuilder.build());
            }

            @Override
            public void error(LoginController.Error error) {
                statusStorer.setState(StatusStorer.State.dormant);
                firstConnect = false;
                statusStorer.setError(error);
                if (error == LoginController.Error.WRONG_WIFI) {
                    mNotifyMgr.cancel(1);
                } else {

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
                                    .setSmallIcon(R.drawable.notif_icon)
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                                    .setVibrate(new long[]{1, 1, 1})
                                    .setContentTitle("Cyber Connect")
                                    .setContentText(message)
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setContentIntent(pendingIntent);
//                    mBuilder.addAction(android.R.drawable.ic_menu_rotate, "Retry", actionClick);
                    mNotifyMgr.notify(1, mBuilder.build());
                }
            }
        });
    }


}