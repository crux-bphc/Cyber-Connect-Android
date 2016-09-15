package helper;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.harsu.developer.bias.LoginActivity;

import java.util.ArrayList;

/**
 * Created by harsu on 14-09-2016.
 */

public class AccountsTableManager {
    public static final String KEY_ID = "id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";


    private static final String DATABASE_TABLE = "AccountsTable";
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "AccountsDatabase";
    private static final String PREFERENCE = "preference";
    private static final String LOGGED_IN = "logged_in";
    private Context context;
    private DBHelper ourHelper;
    private SQLiteDatabase ourDatabase;

    private SharedPreferences sharedPreferences;

    public AccountsTableManager(Context c) {
        context = c;
        sharedPreferences = c.getSharedPreferences(LoginActivity.CREDENTIALS, Context.MODE_PRIVATE);
    }

    public AccountsTableManager open() {
        ourHelper = new DBHelper(context);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        ourHelper.close();
        ourDatabase.close();
    }

    public AccountsSet getAccount(int id) {
        open();
        AccountsSet accountsSet = null;
        Cursor cursor = ourDatabase.rawQuery("Select * from " + DATABASE_TABLE + " where " + KEY_ID + "=" + id, null);
        if (cursor.moveToFirst()) {

            int key_id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
            accountsSet = new AccountsSet(
                    key_id,
                    cursor.getString(cursor.getColumnIndex(KEY_USERNAME)),
                    key_id == getPreference() ? 1 : 0,
                    cursor.getString(cursor.getColumnIndex(KEY_PASSWORD)),
                    key_id == getLoggedIn() ? 1 : 0);
        }
        cursor.close();
        close();
        return accountsSet;

    }

    private long getPreference() {
        return sharedPreferences.getLong(PREFERENCE, -1);
    }

    public void setPreference(long id) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREFERENCE, id);
        editor.apply();
    }

    private long getLoggedIn() {
        return sharedPreferences.getLong(LOGGED_IN, -1);

    }

    public void setLoggedIn(String username) {
        username = username.toLowerCase();
        if (username.isEmpty()) {
            setLoggedIn(-1);
        } else {
            int id = getId(username);
            setLoggedIn(id);
        }
    }

    private void setLoggedIn(long row_id) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(LOGGED_IN, row_id);
        editor.apply();
    }

    public long addEntry(String username,
                         String password,
                         long preference) {
        long success = -1;

        username = username.toLowerCase();
        ContentValues cv = new ContentValues();

        cv.put(KEY_USERNAME, username);
        cv.put(KEY_PASSWORD, password);

        open();
        success = ourDatabase.insert(DATABASE_TABLE, null, cv);
        close();
        int id = getId(username);
        setLoggedIn(id);     //if entry is being added, it means it has just got verified, hence logged in using that account
        if (preference == 1) {
            setPreference(id);
        }
        return success;
    }

    private int getId(String username) {
        int id = -1;
        open();
        Cursor c = ourDatabase.rawQuery("Select " + KEY_ID + " from " + DATABASE_TABLE + " where " + KEY_USERNAME + "='" + username + "'", null);
        if (c.moveToFirst()) {
            id = c.getInt(0);
        }
        c.close();
        close();
        return id;
    }

    public ArrayList<AccountsSet> getAccounts() {
        ArrayList<AccountsSet> accounts = new ArrayList<>();
        open();
        Cursor cursor = ourDatabase.rawQuery("SELECT * FROM " + DATABASE_TABLE,
                null);
        if (cursor.moveToFirst())
            do {
                int key_id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                AccountsSet accountsSet = new AccountsSet(
                        key_id,
                        cursor.getString(cursor.getColumnIndex(KEY_USERNAME)),
                        key_id == getPreference() ? 1 : 0,
                        cursor.getString(cursor.getColumnIndex(KEY_PASSWORD)),
                        key_id == getLoggedIn() ? 1 : 0);

                accounts.add(accountsSet);
            } while (cursor.moveToNext());
        cursor.close();
        close();
        return accounts;
    }

    public AccountsSet getPreferred() {
        open();
        AccountsSet accountsSet = null;
        if (getPreference() == -1) {
            return null;
        }
        Cursor cursor = ourDatabase.rawQuery("Select * from " + DATABASE_TABLE + " where " + KEY_ID + "=" + getPreference(), null);
        if (cursor.moveToFirst()) {

            int key_id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
            accountsSet = new AccountsSet(
                    key_id,
                    cursor.getString(cursor.getColumnIndex(KEY_USERNAME)),
                    key_id == getPreference() ? 1 : 0,
                    cursor.getString(cursor.getColumnIndex(KEY_PASSWORD)),
                    key_id == getLoggedIn() ? 1 : 0);
        }
        cursor.close();
        close();
        return accountsSet;

    }

    public AccountsSet getLoggedInAccount() {
        open();
        AccountsSet accountsSet = null;
        if (getLoggedIn() == -1) {
            return null;
        }
        Cursor cursor = ourDatabase.rawQuery("Select * from " + DATABASE_TABLE + " where " + KEY_ID + "=" + getLoggedIn(), null);
        if (cursor.moveToFirst()) {

            int key_id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
            accountsSet = new AccountsSet(
                    key_id,
                    cursor.getString(cursor.getColumnIndex(KEY_USERNAME)),
                    key_id == getPreference() ? 1 : 0,
                    cursor.getString(cursor.getColumnIndex(KEY_PASSWORD)),
                    key_id == getLoggedIn() ? 1 : 0);
        }
        cursor.close();
        close();
        return accountsSet;

    }

    public void delete(long id) {
        open();
        ourDatabase.delete(DATABASE_TABLE, KEY_ID + "=" + id, null);
        close();
    }

    private static class DBHelper extends SQLiteOpenHelper {

        DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            String query = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_USERNAME + " TEXT NOT NULL UNIQUE, " +
                    KEY_PASSWORD + " TEXT NOT NULL);";
            db.execSQL(query);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

}
