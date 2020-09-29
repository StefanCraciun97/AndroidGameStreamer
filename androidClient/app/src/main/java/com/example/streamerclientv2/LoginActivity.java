package com.example.streamerclientv2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {


    EditText username;
    EditText password;
    Button loginButton;
    TextView registerText;
    final String serverIP = "192.168.0.164";
    final int loginPort = 20000;
    TCPclient tcpClientRunnable;
    final private int LAUNCH_REGISTER_ACTIVITY = 1;


    public static String byteArrayToHexString(final byte[] bytes, int nr) {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<nr;i++){
            sb.append(String.format("%02x", bytes[i]&0xff));
        }
        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        registerText = findViewById(R.id.registerText);

        tcpClientRunnable = new TCPclient(getApplicationContext(),serverIP, loginPort);
        Thread tcpClient = new Thread(tcpClientRunnable);
        tcpClient.start();

        //Toast.makeText(getApplicationContext(), "Wrong user or password", Toast.LENGTH_SHORT).show();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {





                tcpClientRunnable.sendMessage("login_android");
                tcpClientRunnable.sendMessage(username.getText().toString());

                try {

                    // hashing the password with sha-256 before sending on network
                    MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                    String passHash = byteArrayToHexString(sha256.digest(password.getText().toString().getBytes()), 32);
                    tcpClientRunnable.sendMessage(passHash);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }


                // central server will now tell us if login attempt was successful
                String response = tcpClientRunnable.recvMessage();

                if (response.equals("login_success")){


                    int userID = -1;
                    //int userID = Integer.parseInt(tcpClientRunnable.recvMessage());
                    Log.d("uid", Integer.toString(userID));
                    // end TCP connection used for logging in
                    //tcpClientRunnable.stopConnection();
                    Intent intent = new Intent(getApplicationContext(), MachineListActivity.class);
                    intent.putExtra("userID", userID);
                    intent.putExtra("serverIP", serverIP);
                    startActivity(intent);
                }

                else{
                    Toast.makeText(getApplicationContext(), "Wrong user or password", Toast.LENGTH_SHORT).show();
                }


            }
        });


        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivityForResult(intent, LAUNCH_REGISTER_ACTIVITY);


            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == LAUNCH_REGISTER_ACTIVITY){
            if(resultCode == Activity.RESULT_OK){
                String registerUser = data.getStringExtra("user");
                String registerPass = data.getStringExtra("pass");

                MessageDigest sha256 = null;
                String passHash= null;
                try {
                    // hash password before sending to server
                    sha256 = MessageDigest.getInstance("SHA-256");
                    passHash = byteArrayToHexString(sha256.digest(registerPass.getBytes()), 32);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

                tcpClientRunnable.sendMessage("register_android");
                tcpClientRunnable.sendMessage(registerUser);
                tcpClientRunnable.sendMessage(passHash);

                // central server will now tell us if register attempt was successful
                String response = tcpClientRunnable.recvMessage();

                if(response.equals("register_success"))
                    Toast.makeText(getApplicationContext(), "Register successful", Toast.LENGTH_SHORT).show();
                else if(response.equals("register_failed_username_exists"))
                    Toast.makeText(getApplicationContext(), "Username already exists", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Register failed", Toast.LENGTH_SHORT).show();


            }


        }
    }

}
