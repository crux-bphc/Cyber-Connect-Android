package app;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by harsu on 6/13/2015.
 */
public class VolleySingleton {
    private static VolleySingleton sInstance=null;
    private RequestQueue requestQueue   ;
    private VolleySingleton(){
        requestQueue= Volley.newRequestQueue(AppController.getInstance());
    }
    public RequestQueue getRequestQueue(){
        return requestQueue;
    }
    public static VolleySingleton getInstance(){
        if(sInstance==null){
            sInstance=new VolleySingleton();
        }
        return sInstance;
    }
}
