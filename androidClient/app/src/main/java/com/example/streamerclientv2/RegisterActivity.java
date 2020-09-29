package com.example.streamerclientv2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterActivity extends AppCompatActivity {


    EditText username;
    EditText password;
    Button registerButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = (EditText) findViewById(R.id.register_username);
        password = (EditText) findViewById(R.id.register_password);
        registerButton = (Button) findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check if fields are empty
                if(username.getText().toString().equals("") || password.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "User and password cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }


                // take values from fields and pass them back to loginActivity
                // which registers them with the centralServer
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.putExtra("user", username.getText().toString());
                intent.putExtra("pass", password.getText().toString());
                setResult(Activity.RESULT_OK, intent);
                finish();



            }
        });



    }




}
