package helper;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import app.VolleySingleton;

import static com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;

/**
 * Created by harsu on 27-08-2016.
 */

public class LoginController {

    static String loginURL = "http://172.16.0.30:8090/login.xml";
    static String logoutURL = "http://172.16.0.30:8090/logout.xml";

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        SupplicantState supplicantState = wifiInfo.getSupplicantState();
        if (supplicantState == SupplicantState.COMPLETED) {
            if (netInfo != null && netInfo.isConnected())
                return true;

        }
        return false;
    }

    public static void login(Context context, @Nullable final ConnectionListener listener) {
        AccountsTableManager mTableManager = new AccountsTableManager(context);
        AccountsSet accountsSet = mTableManager.getPreferred();
        if (accountsSet != null)
            login(accountsSet, context, listener);
    }

    public static boolean containsData(Context context) {

        AccountsTableManager mTableManager = new AccountsTableManager(context);
        if (mTableManager.getPreferred() == null) {
            return false;
        } else
            return true;
    }

    public static void login(final AccountsSet accountsSet, final Context context, final ConnectionListener listener) {
        final StatusStorer statusStorer = StatusStorer.getInstance();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, loginURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                //password incorrect
                //<?xml version='1.0' ?><requestresponse><status><![CDATA[LOGIN]]></status><message><![CDATA[The system could not log you on. Make sure your password is correct]]></message><logoutmessage><![CDATA[You have successfully logged off]]></logoutmessage><state><![CDATA[]]></state></requestresponse>

                //success
                //<?xml version='1.0' ?><requestresponse><status><![CDATA[LIVE]]></status><message><![CDATA[You have successfully logged in]]></message><logoutmessage><![CDATA[You have successfully logged off]]></logoutmessage><state><![CDATA[]]></state></requestresponse>

                //data limit error contains
                //Your data transfer has been exceeded, Please contact the administrator

                //<?xml version='1.0' ?><requestresponse><status><![CDATA[LOGIN]]></status><message><![CDATA[The system could not log you on. Make sure your password is correct]]></message><logoutmessage><![CDATA[You have successfully logged off]]></logoutmessage><state><![CDATA[]]></state></requestresponse>


                Log.e("response", s);
                if (listener != null) {
                    if (s.contains("You have successfully logged in")) {
                        AccountsTableManager accountsTableManager = new AccountsTableManager(context);
                        if (accountsSet.getId() == -1)
                            accountsTableManager.addEntry(accountsSet.getUsername(), accountsSet.getPassword(), accountsSet.getPreference());
                        accountsTableManager.setLoggedIn(accountsSet.getUsername());
                        statusStorer.setStatus(StatusStorer.Status.LOGGED_IN, context);
                        listener.success();
                    } else if (s.contains("Your data transfer has been exceeded")) {
                        statusStorer.setStatus(StatusStorer.Status.ERROR_LOGGING, context);
                        listener.error(Error.DATA_LIMIT);
                    } else if (s.contains("Make sure your password is correct")) {
                        statusStorer.setStatus(StatusStorer.Status.ERROR_LOGGING, context);
                        listener.error(Error.WRONG_CREDENTIALS);
                    } else if (s.contains("Server is not responding.")) {
                        statusStorer.setStatus(StatusStorer.Status.ERROR_LOGGING, context);
                        listener.error(Error.SERVER_ERROR);
                    } else {
                        statusStorer.setStatus(StatusStorer.Status.ERROR_LOGGING, context);
                        listener.error(Error.UNKNOWN_ERROR);
                    }
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

                if (listener != null) {
                    listener.error(Error.WRONG_WIFI);
                    statusStorer.setStatus(StatusStorer.Status.NOT_BITS_NETWORK, context);
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
                params.put("username", accountsSet.getUsername());
                params.put("password", accountsSet.getPassword());
//                params.put("a", String.valueOf(Calendar.getInstance().getTimeInMillis()));
//                params.put("producttype", "0");

                return params;

            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0, DEFAULT_BACKOFF_MULT));
        app.VolleySingleton.getInstance().getRequestQueue().add(stringRequest);

    }

    public static void logout(final Context context, @Nullable final ConnectionListener listener) {
        final StatusStorer statusStorer = StatusStorer.getInstance();
        final AccountsTableManager mTableManager = new AccountsTableManager(context);
        /*if (mTableManager.getLoggedInAccount() == null) {
            listener.error(Error.UNKNOWN_ERROR);
            return;
        }*/
        StringRequest stringRequest = new StringRequest(Request.Method.POST, logoutURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                //success
                //<?xml version='1.0' ?><requestresponse><status><![CDATA[LIVE]]></status><message><![CDATA[You have successfully logged in]]></message><logoutmessage><![CDATA[You have successfully logged off]]></logoutmessage><state><![CDATA[]]></state></requestresponse>

                Log.e("response", s);
                if (listener != null) {
                    if (s.contains("You have successfully logged off")) {
                        statusStorer.setStatus(StatusStorer.Status.CONNECTED, context);
                        listener.success();
                    } else if (s.contains("Server is not responding.")) {
                        listener.error(Error.SERVER_ERROR);
                    }
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                statusStorer.setStatus(StatusStorer.Status.NOT_BITS_NETWORK, context);
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
                params.put("username", "xyz");
                //Other parameters such are time, producttype are not needed, even for login
                return params;

            }
        };
        app.VolleySingleton.getInstance().getRequestQueue().add(stringRequest);

    }

    public static boolean checkGoogleServer(final ConnectionListener listener) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://www.google.com/", new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.d("Google Response", s);
                listener.success();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.d("Google Error", volleyError.toString());
                listener.error(Error.SERVER_ERROR);
            }
        });
        stringRequest.setShouldCache(false);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0, DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
        return false;
    }

  /*  public static void analyseNetwork(final Context context) {
        final StatusStorer statusStorer = StatusStorer.getInstance();
        statusStorer.setState(StatusStorer.State.active);
        if (LoginController.isConnected(context))//wifi state is connected
        {
            statusStorer.setStatus(StatusStorer.Status.CONNECTED);
            LoginController.getLogInID(new ConnectionListener() {
                @Override
                public void success() {
                    statusStorer.setStatus(StatusStorer.Status.ALREADY_LOGGED_IN);
                    statusStorer.setState(StatusStorer.State.dormant);
                }

                @Override
                public void error(int error) {

                    statusStorer.setState(StatusStorer.State.dormant);
                    if (error == Error.WRONG_WIFI) {
                        statusStorer.setStatus(StatusStorer.Status.NOT_BITS_NETWORK);
                    }
                    //not signed in, or internet is extremely slow, nothing can be done
                }
            }, context);

        } else {
            statusStorer.setState(StatusStorer.State.dormant);
        }

    }*/

    private static void isBitsID(final ConnectionListener listener) {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, loginURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.e("resp", s);
                listener.success();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("Error", volleyError.toString());
                listener.error(Error.WRONG_WIFI);
            }
        });
        VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
    }

    public static void getLogInID(final ConnectionListener listener, final Context context) {
        isBitsID(new ConnectionListener() {
            @Override
            public void success() {

                //is Bits network, get ID by pinging miniclip.
                //I Hope MiniClip doesn't get un-blocked. :P
                StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://www.miniclip.com/", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        ///if not Logged in,
                        //location.href="http://172.16.0.30:8090/httpclient.html"
                        if (!s.contains("@bits-hyderabad.ac.in")) {
                            listener.error(Error.NOT_CONNECTED);     //not connected
                            return;
                        }

                        s = s.substring(0, s.indexOf("@bits-hyderabad.ac.in"));
                        s = s.substring(s.lastIndexOf('>') + 1);
                        Log.e("id", s);
                        AccountsTableManager mTableManager = new AccountsTableManager(context);
                        mTableManager.setLoggedIn(s);
                        listener.success();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        listener.error(Error.SERVER_ERROR);
                    }
                });
                VolleySingleton.getInstance().getRequestQueue().add(stringRequest);
            }

            @Override
            public void error(Error error) {
                listener.error(Error.WRONG_WIFI);
            }
        });

    }


    public enum Error {
        WRONG_WIFI,
        WRONG_CREDENTIALS,
        DATA_LIMIT,
        SERVER_ERROR,
        UNKNOWN_ERROR,
        NOT_CONNECTED,

    }

    public interface ConnectionListener {
        void success();

        void error(Error error);
    }
}
