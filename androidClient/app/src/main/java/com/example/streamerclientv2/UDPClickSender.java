package com.example.streamerclientv2;


import android.provider.ContactsContract;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class UDPClickSender {

    // device ids
    static final byte MOUSE = 0x01;
    static final byte KEYBOARD = 0x02;

    // event types
    static final byte TAP = 0x01;
    static final byte HOLD = 0x02;
    static final byte RELEASE = 0x03;
    static final byte MOVE = 0x04;


    static final byte MOUSE_TAP = 0x11;
    static final byte MOUSE_HOLD = 0x12;
    static final byte MOUSE_RELEASE = 0x13;
    static final byte MOUSE_MOVE = 0x14;
    static final byte KEYBOARD_TAP = 0x21;
    static final byte KEYBOARD_HOLD = 0x22;
    static final byte KEYBOARD_RELEASE = 0x23;




    // virtual key codes

    static final byte BTN_LEFT = 0x3A;
    static final byte BTN_RIGHT = 0x3B;


//    static final byte KEY_A = 0x41;
//    static final byte KEY_B = 0x42;
//    static final byte KEY_C = 0x43;
//    static final byte KEY_D = 0x44;
//    static final byte KEY_E = 0x45;
//    static final byte KEY_F = 0x46;
//    static final byte KEY_G = 0x47;
//    static final byte KEY_H = 0x48;
//    static final byte KEY_I = 0x49;
//    static final byte KEY_J = 0x4A;
//    static final byte KEY_K = 0x4B;
//    static final byte KEY_L = 0x4C;
//    static final byte KEY_M = 0x4D;
//    static final byte KEY_N = 0x4E;
//    static final byte KEY_O = 0x4F;
//    static final byte KEY_P = 0x50;
//    static final byte KEY_Q = 0x51;
//    static final byte KEY_R = 0x52;
//    static final byte KEY_S = 0x53;
//    static final byte KEY_T = 0x54;
//    static final byte KEY_U = 0x55;
//    static final byte KEY_V = 0x56;
//    static final byte KEY_W = 0x57;
//    static final byte KEY_X = 0x58;
//    static final byte KEY_Y = 0x59;
//    static final byte KEY_Z = 0x5A;

    // real PC keyboard scan codes
    // not all keys are defined
    static final byte KEY_Q = 0x10;
    static final byte KEY_W = 0x11;
    static final byte KEY_E = 0x12;
    static final byte KEY_R = 0x13;
    static final byte KEY_T = 0x14;
    static final byte KEY_Y = 0x15;
    static final byte KEY_U = 0x16;
    static final byte KEY_I = 0x17;
    static final byte KEY_O = 0x18;
    static final byte KEY_P = 0x19;

    static final byte KEY_A = 0x1E;
    static final byte KEY_S = 0x1F;
    static final byte KEY_D = 0x20;
    static final byte KEY_F = 0x21;
    static final byte KEY_G = 0x22;
    static final byte KEY_H = 0x23;

    static final byte KEY_Z = 0x2C;
    static final byte KEY_X = 0x2D;
    static final byte KEY_C = 0x2E;
    static final byte KEY_V = 0x2F;
    static final byte KEY_B = 0x30;
    static final byte KEY_N = 0x31;
    static final byte KEY_M = 0x32;

    static final byte KEY_SPACE = 0x39;
    static final byte KEY_LEFT_SHIFT = 0x36;
    static final byte KEY_LEFT_CTRL = 0x1D;
    static final byte KEY_LEFT_ALT = 0x38;
    static final byte KEY_CAPS_LOCK = 0x3A;
    static final byte KEY_TAB = 0x0F;
    static final byte KEY_ESC = 0x01;

    static final byte KEY_1 = 0x02;
    static final byte KEY_2 = 0x03;
    static final byte KEY_3 = 0x04;
    static final byte KEY_4 = 0x05;
    static final byte KEY_5 = 0x06;
    static final byte KEY_6 = 0x07;
    static final byte KEY_7 = 0x08;
    static final byte KEY_8 = 0x09;
    static final byte KEY_9 = 0x0A;
    static final byte KEY_0 = 0x0B;



    // This class sends 3 byte long UDP packets. The bytes represent, respectively:
    // mouse/keyboard event      the exact key involved      tap/press/release


    private byte[] buffer = new byte[1000];
    DatagramSocket socket;
    DatagramPacket datagram;
    InetAddress dest;

    public UDPClickSender(String destAddr, int destPort){

        try{
            // it should not bind to some specific port, because if multiple instances of this class try to bind to same port, it might cause problems
            //socket = new DatagramSocket(new InetSocketAddress(localAddr, localPort))
            // need to make sure it binds to correct address (port doesn't matter)
            socket = new DatagramSocket();
            Log.d("SOCKET", "socket at: " + socket.getLocalAddress() + "   " + socket.getLocalPort());
            dest = InetAddress.getByName(destAddr);
            datagram  = new DatagramPacket(buffer, buffer.length, dest, destPort);


        }
        catch (Exception e){
            Log.d("Exception", e.toString());
        }

    }


    public UDPClickSender(InetSocketAddress destAddr){

        try{
            // it should not bind to some specific port, because if multiple instances of this class try to bind to same port, it might cause problems
            //socket = new DatagramSocket(new InetSocketAddress(localAddr, localPort))
            // need to make sure it binds to correct address (port doesn't matter)
            socket = new DatagramSocket();
            Log.d("SOCKET", "socket at: " + socket.getLocalAddress() + "   " + socket.getLocalPort());
            datagram = new DatagramPacket(buffer, buffer.length, destAddr);

        }
        catch (Exception e){
            Log.d("Exception", e.toString());
        }

    }



    @Deprecated
    public void sendKeyV1(byte device, byte keyCode, byte eventType){

        try{

            //byte[] idCode = new byte[]{code};
            byte[] data = new byte[]{device, keyCode, eventType};
            //datagram.setData(idCode);
            datagram.setData(data);
            socket.send(datagram);
        }
        catch (Exception e){
            Log.d("Exception", e.toString());
        }

    }

    public void sendKey(byte eventType, byte keyCode){

        try {
            // we send 4 byte long segments
            byte[] data = new byte[]{eventType, keyCode, 0x0, 0x0};
            //datagram.setData(idCode);
            datagram.setData(data);
            socket.send(datagram);
        }
        catch (Exception e){
            Log.d("Exception", e.toString());
        }
    }

    @Deprecated
    public void moveMouse(byte x, byte y){

        try {
            // we send 4 byte long segments
            byte[] data = new byte[]{MOUSE_MOVE, x, y, 0x0};
            datagram.setData(data);
            socket.send(datagram);
        }
        catch (Exception e){
            Log.d("Exxceptionn", e.toString());
        }

    }



    public void moveMouse(short x, short y){

        try {
            // we send 8 byte long segments
            byte[] data = new byte[]{MOUSE_MOVE,(byte) (x >> 8), (byte) x, (byte) (y >> 8), (byte) y, 0x0, 0x0, 0x0};
            datagram.setData(data);
            socket.send(datagram);
        }
        catch (Exception e){
            Log.d("Exxceptionn", e.toString());
        }

    }


}
