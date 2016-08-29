package helper;

import android.support.annotation.Nullable;

/**
 * Created by admin on 29-08-2016.
 */

public class StatusStorer {

    static StatusStorer mInstance;
    public int status = Status.DISCONNECTED;
    StatusListener listener;
    private int error;

    private StatusStorer() {

    }

    public static StatusStorer getInstance() {
        if (mInstance == null) {
            mInstance = new StatusStorer();
        }
        return mInstance;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        if (this.status == Status.LOGGED_IN || this.status == Status.ERROR_LOGGING || this.status == Status.ALREADY_LOGGED_IN) {
            if (status == Status.CONNECTED || status == Status.ALREADY_LOGGED_IN) {
                return;
            }
        }
        this.status = status;
        if (listener != null) {
            listener.newStatus(getStatusText());
        }
    }

    public String getStatusText() {
        switch (status) {
            case Status.ERROR_LOGGING:
                return "Error Occurred While Logging In";
            case Status.NOT_BITS_NETWORK:
                return "Not A Bits Network";
            case Status.CONNECTED:
                return "Connected to a Wifi Network";
            case Status.LOGGING_IN:
                return "Attempting To Log In";
            case Status.LOGGED_IN:
                return "Successfully Signed In";
            case Status.DISCONNECTED:
                return "Not Connected To A Wifi Network";
            case Status.ALREADY_LOGGED_IN:
                return "The network is Active";
        }
        return "";
    }

    public void setStatusListener(@Nullable StatusListener listener) {
        this.listener = listener;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public void setState(int st) {
        state = st;
        if (listener != null) {
            listener.newState(st);
        }
    }

    public int getState() {
        return state;
    }

    private int state = State.active;

    public interface State {
        int active = 1;
        int dormant = 2;
    }

    public interface Status {
        int ERROR_LOGGING = -1;
        int NOT_BITS_NETWORK = 0;
        int CONNECTED = 1;
        int LOGGING_IN = 2;
        int LOGGED_IN = 3;
        int DISCONNECTED = 4;
        int ALREADY_LOGGED_IN = 5;
    }

    public interface StatusListener {
        void newStatus(String status);

        void newState(int state);
    }
}
