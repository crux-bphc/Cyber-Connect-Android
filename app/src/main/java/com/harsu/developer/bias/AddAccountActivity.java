package com.harsu.developer.bias;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import helper.AccountsSet;
import helper.LoginController;

public class AddAccountActivity extends AppCompatActivity implements LoginController.ConnectionListener {

    TextInputLayout username, password;
    CheckBox preferred;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account_activity);
        Button login = (Button) findViewById(R.id.login);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);


        username = (TextInputLayout) findViewById(R.id.username);
        password = (TextInputLayout) findViewById(R.id.password);
        preferred = (CheckBox) findViewById(R.id.preferred);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValid()) {
                    LoginController.login(new AccountsSet(-1, username.getEditText().getText().toString(), preferred.isChecked() ? 1 : 0,
                            password.getEditText().getText().toString(), 0), AddAccountActivity.this, AddAccountActivity.this);
                    progressDialog.show();
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
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
        setResult(RESULT_OK);
        finish();
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
