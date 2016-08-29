package com.harsu.developer.bias;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import helper.LoginController;
import helper.StatusStorer;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    StatusStorer statusStorer;
    Button actionB;
    TextView statusTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView account = (TextView) findViewById(R.id.account);
        final SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.CREDENTIALS, MODE_PRIVATE);
        account.setText(sharedPreferences.getString("username", ""));
        boolean autoConnect = sharedPreferences.getBoolean("autoConnect", true);
        SwitchCompat autoConnectSwitch = (SwitchCompat) findViewById(R.id.autoConnect);
        autoConnectSwitch.setChecked(autoConnect);
        ImageView deleteUser = (ImageView) findViewById(R.id.delete);
        deleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setMessage("Are you sure you want to delete the account?");
                dialog.setTitle("Delete Account");
                dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", "");
                        editor.putString("password", "");
                        editor.putBoolean("autoConnect", true);
                        editor.commit();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        MainActivity.this.finish();
                    }
                });
                dialog.setNegativeButton("Cancel", null);
                dialog.show();

            }
        });

        autoConnectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("autoConnect", b);
                editor.apply();
            }
        });


        actionB = (Button) findViewById(R.id.actionButton);
        statusStorer = StatusStorer.getInstance();
        if(statusStorer.getStatus()== StatusStorer.Status.DISCONNECTED){
            LoginController.analyseNetwork(this);
        }

        statusTV = (TextView) findViewById(R.id.status);
        statusTV.setText(statusStorer.getStatusText());
        setAction(statusStorer.getStatus());
        statusStorer.setStatusListener(new StatusStorer.StatusListener() {
            @Override
            public void newStatus(String status) {
                statusTV.setText(status);
                setAction(statusStorer.getStatus());
            }
        });
        actionB.setOnClickListener(this);
    }

    private void setAction(int status) {
        switch (status) {
            case StatusStorer.Status.ERROR_LOGGING:
                actionB.setText("retry");
                actionB.setVisibility(View.VISIBLE);
                break;
            case StatusStorer.Status.NOT_BITS_NETWORK:
                actionB.setText("");
                actionB.setVisibility(View.GONE);
                break;
            case StatusStorer.Status.CONNECTED:
                actionB.setText("Login");
                actionB.setVisibility(View.VISIBLE);
                break;
            case StatusStorer.Status.LOGGING_IN:
                actionB.setText("");
                actionB.setVisibility(View.GONE);
                break;
            case StatusStorer.Status.LOGGED_IN:
                actionB.setText("Logout");
                actionB.setVisibility(View.VISIBLE);
                break;
            case StatusStorer.Status.DISCONNECTED:
                actionB.setText("");
                actionB.setVisibility(View.GONE);
                break;
            case StatusStorer.Status.ALREADY_LOGGED_IN:
                actionB.setText("Logout");
                actionB.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        statusStorer.setStatusListener(null);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.actionButton) {
            switch (statusStorer.getStatus()) {
                case StatusStorer.Status.ERROR_LOGGING:
                case StatusStorer.Status.CONNECTED:
                    LoginController.login(this, new LoginController.ConnectionListener() {
                        @Override
                        public void success() {
                        }

                        @Override
                        public void error(int error) {
                            statusStorer.setStatus(StatusStorer.Status.ERROR_LOGGING);
                        }
                    });
                    break;

                case StatusStorer.Status.LOGGED_IN:
                case StatusStorer.Status.ALREADY_LOGGED_IN:
                    LoginController.logout(this, new LoginController.ConnectionListener() {
                        @Override
                        public void success() {
                            statusStorer.status = StatusStorer.Status.CONNECTED;
                            statusStorer.setStatus(StatusStorer.Status.CONNECTED);
                            statusTV.setText("Logged Out");
                        }

                        @Override
                        public void error(int error) {
                            statusTV.setText("Error Logging Out");
                        }
                    });
                    break;
                case StatusStorer.Status.NOT_BITS_NETWORK:
                case StatusStorer.Status.DISCONNECTED:
                case StatusStorer.Status.LOGGING_IN:
                    break;
            }
        }
    }
}
