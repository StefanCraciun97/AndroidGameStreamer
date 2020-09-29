package com.example.streamerclientv2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.renderscript.ScriptGroup;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static android.util.Log.d;

public class TCPclient implements Runnable, Serializable {
        // Serializable is to enable sharing this object between activities through intents

        private Socket clientSocket;
        private OutputStream out;
        private InputStream in;
        private String IP;
        private int port;
        private byte buffer[];
        private int bufferDataLen;
        private Context context;

        // this is for sending TCP messages
        // previously, I just created a new Thread from SendMessage class that implements Runnable
        // problem is, by creating a new Thread to send each message, race conditions occured
        // i.e. messages were send in wrong order sometimes
        // so I now use this executor to send them, as it guarantees tasks are done sequentially
        // because it uses a single thread
        private ExecutorService executor; // SingleThreadExecutor

        private Buffer b2;











        private class SendMessage implements Runnable {

            String msg;

            public SendMessage(String msg){
                this.msg = msg;
            }

            @Override
            public void run() {
                Log.d("len", byteArrayToHexString(ByteBuffer.allocate(4).putInt(msg.length()).array(),4));
                try {
                    out.write(intToByteArray(msg.length()),0,4);
                    //out.flush();
                    out.write(msg.getBytes(),0,msg.length());
                    out.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }






        private class RecvMessage implements Runnable{

            @Override
            public void run() {
                try{
                    in.read(buffer,0,4);
                    bufferDataLen = ((0xFF & buffer[0]) << 24) | ((0xFF & buffer[1]) << 16) |
                            ((0xFF & buffer[2]) << 8) | (0xFF & buffer[3]);
                    //in.read(buffer,0,bufferDataLen);
                    b2.write(in,bufferDataLen);


                }
                catch (IOException e){
                    d("I/O exception", e.toString());


                }
            }
        }





        private class Buffer{

            private byte[] bufferr;
            private boolean canRead;
            private int dataLen;

            public Buffer(){

                canRead = false;
                this.bufferr = new byte[1000];
                dataLen = 0;

            }

            public synchronized void write(InputStream in, int len){

                while(canRead){
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        Log.d("Thread interrupted", "thr");
                    }
                }


                canRead = true;
                try{
                    in.read(bufferr,0,len);
                    dataLen = len;
                }
                catch (IOException e){
                    d("I/O exception", e.toString());
                }
                notifyAll();

            }

            public synchronized byte[] read(){

                while(!canRead){
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        Log.d("Thread interrupted", "thr");
                    }
                }

                canRead = false;
                notifyAll();
                return bufferr;
            }

            public synchronized int getDataLen(){
                return dataLen;
            }


        }

        public class GetCurrentUserTable implements Callable {

            @Override
            public Database call() throws Exception{

                String line;
                String[] cells;
                Database d = new Database(context);
                d.remakeMachinesTable();

                while(true){
                    line = recvMessage();
                    if (line.equals("end_table"))
                        break;
                    Log.d("line", line);
                    cells = line.split(",");
                    d.insertRecord(new Database.DatabaseRecord(cells[0], cells[1], cells[2], cells[3]));
                }

                int j = 0;
                for(Database.DatabaseRecord i: d.getAllRows())
                    Log.d("database record", i.toString());

                return d;


            }


        }




        public TCPclient(Context context, String IP, int port) {

            this.context = context;
            this.IP = IP;
            this.port = port;
            buffer = new byte[1000];
            b2 = new Buffer();

        }

        public static String byteArrayToHexString(final byte[] bytes, int nr) {
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<nr;i++){
                sb.append(String.format("%02x ", bytes[i]&0xff));
            }
            return sb.toString();
        }

        public static final int byteArrayToInt(byte[] arr){

            return ((0xFF & arr[0]) << 24) | ((0xFF & arr[1]) << 16) |
                    ((0xFF & arr[2]) << 8) | (0xFF & arr[3]);
        }

        public static final byte[] intToByteArray(int value) {
            return new byte[] {
                    (byte)(value >>> 24),
                    (byte)(value >>> 16),
                    (byte)(value >>> 8),
                    (byte)value};
        }


    private void handshake(){

            try{

                // initialize executor for sending messages
                executor = Executors.newSingleThreadExecutor();

                clientSocket = new Socket(IP, port);
                in =  clientSocket.getInputStream();
                out = clientSocket.getOutputStream();
                //in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            }
            catch (UnknownHostException e){
                d("Unknown host exception", e.toString());
            }
            catch (IOException e){
                d("I/O exception", e.toString());
            }

        }


        public void sendMessage(String msg) {

            // !!! don't know yet if this is a good solution
            // downside: thread freezes if I forget to call handshake()
            while(out == null); // out is null if handshake() has not finished running yet
                                // i.e. TCP connection hasn't been established
                                // so we have to wait before sending messages

            // this is what I used before, but it created race conditions (messages sent out of order)
            // new Thread(new SendMessage(msg)).start();

            executor.execute(new SendMessage(msg));

        }

        /*
        public void recvMessage(){

            try{
                in.read(buffer,0,4);
                bufferDataLen = ((0xFF & buffer[0]) << 24) | ((0xFF & buffer[1]) << 16) |
                        ((0xFF & buffer[2]) << 8) | (0xFF & buffer[3]);
                in.read(buffer,0,bufferDataLen);

            }
            catch (IOException e){
                d("I/O exception", e.toString());
            }
        }

        */


        public String recvMessage(){

            new Thread(new RecvMessage()).start();
            return new String(b2.read(),0,b2.getDataLen());

        }



        public void stopConnection() {

            try{
                in.close();
                out.close();
                clientSocket.close();
            }
            catch (IOException e){
                d("I/O exception", e.toString());
            }

        }

        public void handleResponse(String command){

            if (command == "download_user_table"){


                getCurrentUserTable();

            }

        }

        public Database getCurrentUserTable(){


//            String line;
//            String[] cells;
//            Database d = new Database(context);
//            d.remakeTable("user");
//
//            while(true){
//                line = recvMessage();
//                if (line.equals("end_table"))
//                    break;
//                Log.d("line", line);
//                cells = line.split(",");
//                d.insertRecord(new Database.DatabaseRecord(cells[1], cells[2]));
//            }
//
//            int j = 0;
//            for(Database.DatabaseRecord i: d.getAllRows())
//                Log.d("database record", i.toString());
//
//
//
//            return d;

            // tell central server to start sending table with machines for current user
            sendMessage("download_user_table");


            Callable callable = new TCPclient.GetCurrentUserTable();
            FutureTask<Database> ftask = new FutureTask<Database>(callable);
            // running function to receive table line by line in a separate thread
            Thread t = new Thread(ftask);
            t.start();

            try {
                // FutureTask.get() returns only after callable is done running (whole table has been received)
                return ftask.get();
            } catch (Exception e) {
                Log.d("future exception", e.toString());
                return null;
            }

        }

        public void run(){


            handshake();




            //Database d = getCurrentUserTable();


            //sendMessage("download_user_table");
            //getCurrentUserTable();
            //sendMessage("salutare");
            //Log.d("resp", recvMessage());
            //handleResponse("download_user_table");

            //stopConnection();

//            Database d = new Database(context);
//            d.remakeMachinesTable();
//            d.insertRecord(new Database.DatabaseRecord("1", "akon_m1", "1.1.1.1"));
//            d.insertRecord(new Database.DatabaseRecord("1", "akon_m2", "2.2.2.2"));
//
//            for(Database.DatabaseRecord i: d.getAllRows()){
//                Log.d("database record", i.toString());
//            }


        }



}
