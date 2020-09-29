package com.example.streamerclientv2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class VideoReceiver extends Thread {

    private byte[] buffer = new byte[640000];
    private byte[] data = new byte[1000000];
    private DatagramSocket socket;

    SurfaceView surface;


    Bitmap bitmap;
    SurfaceHolder holder;
    Canvas canvas;







    class DatagramReceiver implements Callable<byte[]> {

        DatagramSocket datagramSocket;

        public DatagramReceiver(DatagramSocket datagramSocket){
            this.datagramSocket = datagramSocket;
        }


        @Override
        public byte[] call() throws Exception {
            return reassembleDatagram(datagramSocket);
        }

    }





    public byte[] datagramAssembler(DatagramSocket datagramSocket){



        Callable callable = new DatagramReceiver(datagramSocket);
        FutureTask<byte[]> ftask = new FutureTask<byte[]>(callable);
        // running function to receive table line by line in a separate thread
        Thread t = new Thread(ftask);
        t.start();

        // executor.execute(ftask);

        try {
            // FutureTask.get() returns only after callable is done running (whole table has been received)
            return ftask.get();
        } catch (Exception e) {
            Log.d("future exception", e.toString());
            return null;
        }


    }














    public VideoReceiver(SurfaceView surface){

        this.surface = surface;

    }

    private byte[] reassembleDatagram(DatagramSocket socket){

        DatagramPacket datagram  = new DatagramPacket(buffer, buffer.length);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(500000);

        try{

            socket.receive(datagram);
            byte[] data = datagram.getData();

            short datagramID = ByteBuffer.wrap(new byte[]{data[0], data[1]}).getShort();
            byte currentNr = data[2];
            byte totalNr = data[3];
            outputStream.write(data,4,datagram.getLength()-4);


            byte i = 1;

            while(i == currentNr && i < totalNr){ // while segments arrive in order

                socket.receive(datagram);
                data = datagram.getData();
                datagramID = ByteBuffer.wrap(new byte[]{data[0], data[1]}).getShort();
                currentNr = data[2];
                //totalNr = data[3];
                outputStream.write(data,4,datagram.getLength()-4);

                i++;

            }

            if (i == totalNr)
                return outputStream.toByteArray();
            else
                return null;

        }

        catch (Exception e){
            Log.d("exxception", e.toString());
            return null;
        }

    }

    public void run(){

        try{

            //text.setText("thread started");

            DatagramSocket socket = new DatagramSocket(new InetSocketAddress("192.168.0.12", 20010));
            //text.setText("socket at: " + socket.getLocalAddress() + "   " + socket.getLocalPort());
            DatagramPacket datagram  = new DatagramPacket(buffer, buffer.length);
            int i = 0;
            long startTime;
            long endTime;

            startTime = System.currentTimeMillis();

            while(true) {


                data = datagramAssembler(socket);
                bitmap = null;
                if (data != null)
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                else { // segment was damaged so it is dropped and reassembleDatagram returns null
                    //Log.d("Frame damaged", new Integer(i).toString() );
                    continue;
                }
                holder = surface.getHolder();

                canvas = holder.lockCanvas();
                canvas.drawBitmap(bitmap, 0, 0, null);
                holder.unlockCanvasAndPost(canvas);
                i++;

                endTime = System.currentTimeMillis();

                if (endTime - startTime >= 1000){
                    Log.d("FPS: ", new Integer(i).toString());
                    startTime = endTime;
                    i=0;

                }

            }

        }
        catch (Exception e){
            Log.d("exxception", e.toString());
        }



    }


}