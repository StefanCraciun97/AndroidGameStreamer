package com.example.streamerclientv2;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.zerokol.views.joystickView.JoystickView;

import java.net.InetSocketAddress;


public class CustomJoystick extends JoystickView {


    public static final int JOYSTICK_POLLING_INTERVAL = 10; //default in JoystickView is 100ms, too long, joystick felt unresponsive
    byte sensitivity = 8;
    byte sensitivity_square = (byte) (Math.sqrt(sensitivity) + 1);
    int currentDirection = -1; // joystick is in center
    UDPClickSender keySender;




    public CustomJoystick(Context context) {

        super(context);
        //keySender = new UDPClickSender("192.168.0.164", 20011);
        setOnJoystickMoveListener(onJoystickMoveListenerV2, JOYSTICK_POLLING_INTERVAL);

    }

    public CustomJoystick(Context context, AttributeSet attrs){

        super(context, attrs);
        //keySender = new UDPClickSender("192.168.0.164", 20011);
        setOnJoystickMoveListener(onJoystickMoveListenerV2, JOYSTICK_POLLING_INTERVAL);
    }

    public CustomJoystick(Context context, AttributeSet attrs, int defaultStyle){

        super(context, attrs, defaultStyle);
        //keySender = new UDPClickSender("192.168.0.164", 20011);
        setOnJoystickMoveListener(onJoystickMoveListenerV2, JOYSTICK_POLLING_INTERVAL);
    }



    public void setKeySenderBindAddress(InetSocketAddress addr){

        keySender = new UDPClickSender(addr);

    }








    private void releaseCurrentPressedKey(){

        switch (currentDirection){
            case JoystickView.FRONT:
                keySender.sendKey(UDPClickSender.KEYBOARD_RELEASE, UDPClickSender.KEY_W );
                break;
            case JoystickView.FRONT_RIGHT:
                keySender.sendKey(UDPClickSender.KEYBOARD_RELEASE, UDPClickSender.KEY_W );
                keySender.sendKey(UDPClickSender.KEYBOARD_RELEASE, UDPClickSender.KEY_D );
                break;
            case JoystickView.RIGHT:
                keySender.sendKey(UDPClickSender.KEYBOARD_RELEASE, UDPClickSender.KEY_D );
                break;
            case JoystickView.RIGHT_BOTTOM:
                keySender.sendKey(UDPClickSender.KEYBOARD_RELEASE, UDPClickSender.KEY_S );
                keySender.sendKey(UDPClickSender.KEYBOARD_RELEASE, UDPClickSender.KEY_D );
                break;
            case JoystickView.BOTTOM:
                keySender.sendKey(UDPClickSender.KEYBOARD_RELEASE, UDPClickSender.KEY_S );
                break;
            case JoystickView.BOTTOM_LEFT:
                keySender.sendKey(UDPClickSender.KEYBOARD_RELEASE, UDPClickSender.KEY_S );
                keySender.sendKey(UDPClickSender.KEYBOARD_RELEASE, UDPClickSender.KEY_A );
                break;
            case JoystickView.LEFT:
                keySender.sendKey(UDPClickSender.KEYBOARD_RELEASE, UDPClickSender.KEY_A );
                break;
            case JoystickView.LEFT_FRONT:
                keySender.sendKey(UDPClickSender.KEYBOARD_RELEASE, UDPClickSender.KEY_W );
                keySender.sendKey(UDPClickSender.KEYBOARD_RELEASE, UDPClickSender.KEY_A );
                break;
            default: // joystick in center
                return;
        }

    }






    private OnJoystickMoveListener onJoystickMoveListenerV2 = new OnJoystickMoveListener() {
        @Override
        public void onValueChanged(int angle, int power, int direction) {

            switch (direction) {
                case JoystickView.FRONT:
                    if(currentDirection != JoystickView.FRONT){
                        releaseCurrentPressedKey();
                        keySender.sendKey(UDPClickSender.KEYBOARD_HOLD, UDPClickSender.KEY_W);
                        currentDirection = JoystickView.FRONT;
                    }
                    break;
                case JoystickView.FRONT_RIGHT:
                    if(currentDirection != JoystickView.FRONT_RIGHT){
                        releaseCurrentPressedKey();
                        keySender.sendKey(UDPClickSender.KEYBOARD_HOLD, UDPClickSender.KEY_W);
                        keySender.sendKey(UDPClickSender.KEYBOARD_HOLD, UDPClickSender.KEY_D);
                        currentDirection = JoystickView.FRONT_RIGHT;
                    }
                    break;
                case JoystickView.RIGHT:
                    if(currentDirection != JoystickView.RIGHT){
                        releaseCurrentPressedKey();
                        keySender.sendKey(UDPClickSender.KEYBOARD_HOLD, UDPClickSender.KEY_D);
                        currentDirection = JoystickView.RIGHT;
                    }
                    break;
                case JoystickView.RIGHT_BOTTOM:
                    if(currentDirection != JoystickView.RIGHT_BOTTOM){
                        releaseCurrentPressedKey();
                        keySender.sendKey(UDPClickSender.KEYBOARD_HOLD, UDPClickSender.KEY_S);
                        keySender.sendKey(UDPClickSender.KEYBOARD_HOLD, UDPClickSender.KEY_D);
                        currentDirection = JoystickView.RIGHT_BOTTOM;
                    }
                    break;

                case JoystickView.BOTTOM:
                    if(currentDirection != JoystickView.BOTTOM){
                        releaseCurrentPressedKey();
                        keySender.sendKey(UDPClickSender.KEYBOARD_HOLD, UDPClickSender.KEY_S);
                        currentDirection = JoystickView.BOTTOM;
                    }
                    break;
                case JoystickView.BOTTOM_LEFT:
                    if(currentDirection != JoystickView.BOTTOM_LEFT){
                        releaseCurrentPressedKey();
                        keySender.sendKey(UDPClickSender.KEYBOARD_HOLD, UDPClickSender.KEY_S);
                        keySender.sendKey(UDPClickSender.KEYBOARD_HOLD, UDPClickSender.KEY_A);
                        currentDirection = JoystickView.BOTTOM_LEFT;
                    }
                    break;
                case JoystickView.LEFT:
                    if(currentDirection != JoystickView.LEFT){
                        releaseCurrentPressedKey();
                        keySender.sendKey(UDPClickSender.KEYBOARD_HOLD, UDPClickSender.KEY_A);
                        currentDirection = JoystickView.LEFT;
                    }
                    break;
                case JoystickView.LEFT_FRONT:
                    if(currentDirection != JoystickView.LEFT_FRONT){
                        releaseCurrentPressedKey();
                        keySender.sendKey(UDPClickSender.KEYBOARD_HOLD, UDPClickSender.KEY_W);
                        keySender.sendKey(UDPClickSender.KEYBOARD_HOLD, UDPClickSender.KEY_A);
                        currentDirection = JoystickView.LEFT_FRONT;
                    }
                    break;
                default: // joystick in centru
                    releaseCurrentPressedKey();
                    currentDirection = -1;

            }
        }
    };



    public OnJoystickMoveListener onJoystickMoveListenerV3 = new OnJoystickMoveListener(){

        @Override
        public void onValueChanged(int angle, int power, int direction){

            switch (direction) {
                case JoystickView.FRONT:
                    keySender.moveMouse((byte) 0,(byte) (-1 * sensitivity));

                    break;
                case JoystickView.FRONT_RIGHT:
                    keySender.moveMouse(sensitivity, (byte) (-1 * sensitivity));
                    break;
                case JoystickView.RIGHT:
                    keySender.moveMouse(sensitivity,(byte) 0);
                    break;
                case JoystickView.RIGHT_BOTTOM:
                    keySender.moveMouse(sensitivity,sensitivity);
                    break;
                case JoystickView.BOTTOM:
                    keySender.moveMouse((byte) 0, sensitivity);
                    break;
                case JoystickView.BOTTOM_LEFT:
                    keySender.moveMouse((byte) (-1 * sensitivity), sensitivity);

                    break;
                case JoystickView.LEFT:
                    keySender.moveMouse((byte) (-1 * sensitivity),(byte) 0);
                    break;
                case JoystickView.LEFT_FRONT:
                    keySender.moveMouse((byte) (-1 * sensitivity), (byte) (-1 * sensitivity));
                    break;
                default: // joystick in centru
                    //releaseCurrentPressedKey();
                    //currentDirection = -1;
                    break;

            }
        }

    };

    public OnJoystickMoveListener onJoystickMoveListenerV4 = new OnJoystickMoveListener() {
        @Override
        public void onValueChanged(int angle, int power, int direction) {

            byte x = (byte) (sensitivity * Math.sin(Math.toRadians(angle)));
            byte y = (byte) (-1 * sensitivity * Math.cos(Math.toRadians(angle)));

            keySender.moveMouse(x,y);

        }
    };







}

