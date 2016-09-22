package com.harsu.developer.bias;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import helper.AccountsSet;
import helper.LoginController;

//import android.content.SharedPreferences;

public class LoginActivity extends AppCompatActivity implements LoginController.ConnectionListener {

    public static final String CREDENTIALS = "com.harsu.developer.bias.credentials";
    TextInputLayout username, password;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkSignedIn();
        setContentView(R.layout.activity_login);
        Button login = (Button) findViewById(R.id.login);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);


        username = (TextInputLayout) findViewById(R.id.username);
        password = (TextInputLayout) findViewById(R.id.password);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValid()) {

                    LoginController.login(new AccountsSet(-1, username.getEditText().getText().toString(), 1,
                            password.getEditText().getText().toString(), 0), LoginActivity.this, LoginActivity.this);
                    progressDialog.show();
                }
            }
        });

//        LoginController.isBitsID();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.info) {
            startActivity(new Intent(this, InfoActivity.class));
            return true;
        }else if (item.getItemId() == R.id.share) {
            final String appPackageName = getPackageName();
            Intent intent2 = new Intent();
            intent2.setAction(Intent.ACTION_SEND);
            intent2.setType("text/plain");
            intent2.putExtra(Intent.EXTRA_TEXT, "Hey view this app from CRUx BPHC, https://play.google.com/store/apps/details?id=" + appPackageName +
                    " . It allows you to automatically connect to CyberRoam. Do give it a try. :) ");
            startActivity(Intent.createChooser(intent2, "Share via"));
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean isValid() {
        boolean flag = true;
        if (username.getEditText().getText().toString().isEmpty()) {
            username.setError("This field cannot be Empty");
            flag = false;
        }
        if (password.getEditText().getText().toString().isEmpty()) {
            password.setError("This field cannot be Empty");
            flag = false;
        }
        return flag;
    }

    @Override
    public void success() {
        progressDialog.dismiss();
        Toast.makeText(this, "Successfully Signed in", Toast.LENGTH_SHORT).show();
        checkSignedIn();
    }

    private void checkSignedIn() {
        if (LoginController.containsData(this)) {
            Intent intent = new Intent(this, MainActivity.class);
            if (getIntent().getIntExtra("extra", 0) == 1) {
                intent.putExtra("extra", 1);
            }
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void error(LoginController.Error error) {
        progressDialog.dismiss();
        if (error == LoginController.Error.WRONG_CREDENTIALS)
            Toast.makeText(this, "Please check your credentials!", Toast.LENGTH_SHORT).show();
        else if (error == LoginController.Error.DATA_LIMIT) {
            Toast.makeText(this, "Data limit exceeded", Toast.LENGTH_SHORT).show();
        } else if (error == LoginController.Error.WRONG_WIFI) {
            Toast.makeText(this, "This connection has no Access to Bits network", Toast.LENGTH_SHORT).show();
        } else if (error == LoginController.Error.SERVER_ERROR)
            Toast.makeText(this, "Server error occured. Please Retry", Toast.LENGTH_SHORT).show();
        else if (error == LoginController.Error.UNKNOWN_ERROR) {
            Toast.makeText(this, "Unknown error occured. Please Retry", Toast.LENGTH_SHORT).show();
        }
    }
}
