package com.harsu.developer.bias;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import helper.LoginController;

public class MyNetworkMonitor extends BroadcastReceiver {
    private static boolean firstConnect = true;

    @Override
    public void onReceive(Context context, Intent intent) {


        if (!LoginController.containsData(context)) {
            return;
        }
        if (!LoginController.isOnline(context))//already logged in
        {
            firstConnect = true;
            return;
        }
        if (!firstConnect) {
            return;
        }
        firstConnect = false;
        LoginController.login(context, new LoginController.ConnectionListener() {
            @Override
            public void success() {

            }

            @Override
            public void error(int error) {

            }
        });


    }
}