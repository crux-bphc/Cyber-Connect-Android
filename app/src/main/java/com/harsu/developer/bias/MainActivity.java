package com.harsu.developer.bias;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

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
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", "");
                editor.putString("password", "");
                editor.putBoolean("autoConnect", true);
                editor.commit();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                MainActivity.this.finish();
            }
        });
    }
}
