package helper;

import android.content.Context;
import android.support.annotation.Nullable;

/**
 * Created by admin on 29-08-2016.
 */

public class StatusStorer {

    static StatusStorer mInstance;
    public Status status = Status.DISCONNECTED;
    StatusListener listener;
    private LoginController.Error error;
    private State state = State.dormant;

    private StatusStorer() {

    }

    public static StatusStorer getInstance() {
        if (mInstance == null) {
            mInstance = new StatusStorer();
        }
        return mInstance;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status, Context context) {

        if (status != Status.ALREADY_LOGGED_IN && status != Status.LOGGED_IN) {
            AccountsTableManager accountsTableManager = new AccountsTableManager(context);
            accountsTableManager.setLoggedIn("");
        }
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
            case ERROR_LOGGING:
                return "Error Occurred While Logging In";
            case NOT_BITS_NETWORK:
                return "Not A Bits Network";
            case CONNECTED:
                return "Connected to a Wifi Network";
            case LOGGING_IN:
                return "Attempting To Log In";
            case LOGGED_IN:
                return "Successfully Signed In";
            case DISCONNECTED:
                return "Not Connected To A Wifi Network";
            case ALREADY_LOGGED_IN:
                return "The network is Active";
        }
        return "";
    }

    public void setStatusListener(@Nullable StatusListener listener) {
        this.listener = listener;
    }

    public LoginController.Error getError() {
        return error;
    }

    public void setError(LoginController.Error error) {
        this.error = error;
    }

    public State getState() {
        return state;
    }

    public void setState(State st) {
        state = st;
        if (listener != null) {
            listener.newState(st);
        }
    }

    public enum State {
        active, dormant
    }

    public enum Status {
        ERROR_LOGGING,
        NOT_BITS_NETWORK,
        CONNECTED,
        LOGGING_IN,
        LOGGED_IN,
        DISCONNECTED,
        ALREADY_LOGGED_IN
    }

    public interface StatusListener {
        void newStatus(String status);

        void newState(State state);
    }
}
