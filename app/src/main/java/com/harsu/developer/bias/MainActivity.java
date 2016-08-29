package com.harsu.developer.bias;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import helper.StatusStorer;

public class MainActivity extends AppCompatActivity {

    StatusStorer statusStorer;

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
                AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
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
                dialog.setNegativeButton("Cancel",null);
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


        statusStorer = StatusStorer.getInstance();
        final TextView statusTV = (TextView) findViewById(R.id.status);
        statusTV.setText(statusStorer.getStatusText());
        statusStorer.setStatusListener(new StatusStorer.StatusListener() {
            @Override
            public void newStatus(String status) {
                statusTV.setText(status);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        statusStorer.setStatusListener(null);
    }
}
