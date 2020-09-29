package com.example.streamerclientv2;


// https://www.androidauthority.com/how-to-store-data-locally-in-android-app-717190/
// http://stupidpythonideas.blogspot.com/2013/05/sockets-are-byte-streams-not-message.html

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zerokol.views.joystickView.JoystickView;

import java.net.InetSocketAddress;


public class StreamingActivity extends AppCompatActivity {

    UDPClickSender clickSender;
    CustomJoystick joystickLeft;
    EnhancedSurface surf;
    String windowsHostIP = "192.168.0.164";
    boolean adsActive = false;


    /** * Hide both the navigation bar and the status bar. */

    public void hideBottomBar() {
            getWindow().getDecorView().setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    );

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // hide the status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideBottomBar();
        setContentView(R.layout.activity_streaming);



        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        surf = (EnhancedSurface) findViewById(R.id.enh_surf);

        windowsHostIP = getIntent().getStringExtra("selectedMachineIP");


        //SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface);
        clickSender = new UDPClickSender(windowsHostIP, 20011);




        ImageButton reloadButton = (ImageButton) findViewById(R.id.reloadButton);
        ImageButton shootButton = (ImageButton)  findViewById(R.id.shootButton);

        Button adsButton = findViewById(R.id.adsButton);
        Button jumpButton = findViewById(R.id.jumpButton);
        Button gButton = findViewById(R.id.gButton);
        Button eButton = findViewById(R.id.eButton);

        Button Button1 = findViewById(R.id.Button1);
        Button Button2 = findViewById(R.id.Button2);
        Button Button3 = findViewById(R.id.Button3);
        Button Button4 = findViewById(R.id.Button4);

        Button exitButton = findViewById(R.id.exitButton);

        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Reload", "Reload" );
                clickSender.sendKey(UDPClickSender.KEYBOARD_TAP, UDPClickSender.KEY_R);
            }
        });

        shootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Shoot", "Shoot");
                clickSender.sendKey(UDPClickSender.MOUSE_TAP, UDPClickSender.BTN_LEFT);
            }
        });



        adsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!adsActive) {
                    clickSender.sendKey(UDPClickSender.MOUSE_HOLD, UDPClickSender.BTN_RIGHT);
                    adsActive = true;
                }
                else{
                    clickSender.sendKey(UDPClickSender.MOUSE_RELEASE, UDPClickSender.BTN_RIGHT);
                    adsActive = false;
                }

            }
        });

        jumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSender.sendKey(UDPClickSender.KEYBOARD_TAP, UDPClickSender.KEY_SPACE);
            }
        });



        gButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSender.sendKey(UDPClickSender.KEYBOARD_TAP, UDPClickSender.KEY_G);
            }
        });

        eButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSender.sendKey(UDPClickSender.KEYBOARD_TAP, UDPClickSender.KEY_E);
            }
        });




        Button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSender.sendKey(UDPClickSender.KEYBOARD_TAP, UDPClickSender.KEY_1);
            }
        });

        Button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSender.sendKey(UDPClickSender.KEYBOARD_TAP, UDPClickSender.KEY_2);
            }
        });

        Button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSender.sendKey(UDPClickSender.KEYBOARD_TAP, UDPClickSender.KEY_3);
            }
        });

        Button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSender.sendKey(UDPClickSender.KEYBOARD_TAP, UDPClickSender.KEY_4);
            }
        });

        Button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });





        joystickLeft = (CustomJoystick) findViewById(R.id.joystickLeft1);
        joystickLeft.setKeySenderBindAddress(new InetSocketAddress(windowsHostIP,20011));
        //joystickLeft.setOnJoystickMoveListener(joystickLeft.onJoystickMoveListenerV2, CustomJoystick.JOYSTICK_POLLING_INTERVAL);


        Log.d("All good", "sss");
        VideoReceiver udp = new VideoReceiver(surf.getSurfaceView());
        udp.start();
        Log.d("All good2", "sss");


    }





}
