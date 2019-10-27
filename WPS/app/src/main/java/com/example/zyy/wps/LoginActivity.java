package com.example.zyy.wps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {
    private EditText ed_name;
    private Button bt_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ed_name = findViewById(R.id.ed_name);
        bt_login = findViewById(R.id.bt_login);
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = ed_name.getText().toString();
//                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
//                editor.putString("username",name);
//                editor.apply();
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                intent.putExtra("username",name);
                startActivity(intent);
            }
        });
    }

}
