package helper;

/**
 * Created by harsu on 14-09-2016.
 */

public class AccountsSet {
    private long id, preference, loggedIn;
    private String username, password;


    public AccountsSet(long id, String username, long preference, long loggedIn) {
        this.id = id;
        this.preference = preference;
        this.username = username;
        this.loggedIn = loggedIn;
        password = "";
    }

    public AccountsSet(long id, String username, long preference, String password, long loggedIn) {
        this.id = id;
        this.preference = preference;
        this.username = username;
        this.password = password;
        this.loggedIn = loggedIn;
    }

    public long getId() {
        return id;
    }

    public long getPreference() {
        return preference;
    }

    public  long getLoggedIn(){
        return loggedIn;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setID(long l) {
        
    }
}
