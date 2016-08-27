package helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.harsu.developer.bias.MainActivity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by harsu on 27-08-2016.
 */

public class LoginController {

    static String loginURL = "http://172.16.0.30:8090/login.xml";
    static String logoutURL= "http://172.16.0.30:8090/logout.xml";

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static void login(Context context, @Nullable final ConnectionListener listener) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.CREDENTIALS, Context.MODE_PRIVATE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, loginURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                //password incorrect
                //<?xml version='1.0' ?><requestresponse><status><![CDATA[LOGIN]]></status><message><![CDATA[The system could not log you on. Make sure your password is correct]]></message><logoutmessage><![CDATA[You have successfully logged off]]></logoutmessage><state><![CDATA[]]></state></requestresponse>

                //success
                //<?xml version='1.0' ?><requestresponse><status><![CDATA[LIVE]]></status><message><![CDATA[You have successfully logged in]]></message><logoutmessage><![CDATA[You have successfully logged off]]></logoutmessage><state><![CDATA[]]></state></requestresponse>

                //data limit error contains
                //Your data transfer has been exceeded, Please contact the administrator

                Log.e("response", s);
                if (listener != null) {
                    if (s.contains("You have successfully logged in")) {
                        listener.success();
                    } else if (s.contains("Your data transfer has been exceeded")) {
                        listener.error(Error.DATA_LIMIT);
                    } else if (s.contains("Your credentials were incorrect")) {
                        listener.error(Error.WRONG_CREDENTIALS);
                    } else if (s.contains("Server is not responding.")) {
                        listener.error(Error.SERVER_ERRROR);
                    }
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                if (listener != null) {
                    listener.error(Error.WRONG_WIFI);
                }
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> b = new HashMap<>();
                b.put("Content-Type", "application/x-www-form-urlencoded");
                return b;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("mode", "191");

                params.put("username", sharedPreferences.getString("username", ""));
                params.put("password", sharedPreferences.getString("password", ""));
                params.put("a", String.valueOf(Calendar.getInstance().getTimeInMillis()));
                params.put("producttype", "0");

                return params;

            }
        };
        app.VolleySingleton.getInstance().getRequestQueue().add(stringRequest);

    }


    public static void logout(Context context, @Nullable final ConnectionListener listener) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.CREDENTIALS, Context.MODE_PRIVATE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, logoutURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                //success
                //<?xml version='1.0' ?><requestresponse><status><![CDATA[LIVE]]></status><message><![CDATA[You have successfully logged in]]></message><logoutmessage><![CDATA[You have successfully logged off]]></logoutmessage><state><![CDATA[]]></state></requestresponse>

                Log.e("response", s);
                if (listener != null) {
                    if (s.contains("You have successfully logged off")) {
                        listener.success();
                    } else if (s.contains("Server is not responding.")) {
                        listener.error(Error.SERVER_ERRROR);
                    }
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                if (listener != null) {
                    listener.error(Error.WRONG_WIFI);
                }
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> b = new HashMap<>();
                b.put("Content-Type", "application/x-www-form-urlencoded");
                //Headers is not needed. It works with params alone supplied
                return b;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();
                params.put("mode", "193");
                params.put("username", sharedPreferences.getString("username", ""));
                //Other parameters such are time, producttype are not needed, even for login
                return params;

            }
        };
        app.VolleySingleton.getInstance().getRequestQueue().add(stringRequest);

    }    public static boolean containsData(Context context) {

        final SharedPreferences sharedPreferences = context.getSharedPreferences(MainActivity.CREDENTIALS, Context.MODE_PRIVATE);
        return !sharedPreferences.getString("username", "").isEmpty();
    }


    public interface ConnectionListener {
        public void success();

        public void error(int error);
    }

    public interface Error {
        int WRONG_WIFI = 0;
        int WRONG_CREDENTIALS = 1;
        int DATA_LIMIT = 2;
        int SERVER_ERRROR = 3;
    }
}
