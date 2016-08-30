package com.harsu.developer.bias;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import helper.LoginController;

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

                    LoginController.login(username.getEditText().getText().toString(),
                            password.getEditText().getText().toString(), LoginActivity.this);
                    progressDialog.show();
                }
            }
        });

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
        }
        return super.onOptionsItemSelected(item);
    }

    private void storeData() {
        SharedPreferences.Editor editor = getSharedPreferences(CREDENTIALS, MODE_PRIVATE).edit();
        editor.putString("username", username.getEditText().getText().toString());
        editor.putString("password", password.getEditText().getText().toString());
        editor.commit();
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
        storeData();
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
    public void error(int error) {
        progressDialog.dismiss();
        if (error == LoginController.Error.WRONG_CREDENTIALS)
            Toast.makeText(this, "Please check your credentials!", Toast.LENGTH_SHORT).show();
        else if (error == LoginController.Error.DATA_LIMIT) {
            Toast.makeText(this, "Data limit exceeded", Toast.LENGTH_SHORT).show();
        } else if (error == LoginController.Error.WRONG_WIFI) {
            Toast.makeText(this, "This connection has no Access to Bits network", Toast.LENGTH_SHORT).show();
        } else if (error == LoginController.Error.SERVER_ERROR)
            Toast.makeText(this, "Server error occured. Please Retry", Toast.LENGTH_SHORT).show();
    }
}
