package com.example.streamerclientv2;



import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.net.InetSocketAddress;


public class EnhancedSurface extends RelativeLayout {


    private final float sensitivity = 2.5f;


    private SurfaceView surfaceView;
    private View controlArea;
    FrameLayout controlAreaContainer;
    FrameLayout surfaceContainer;
    Context cont;
    private UDPClickSender moveSender;

    public EnhancedSurface(Context context) {
        super(context);
        cont = context;
        init();
    }

    public EnhancedSurface(Context context, AttributeSet attrs){
        super(context, attrs);
        cont = context;
        init();
    }

    public EnhancedSurface(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        cont = context;
        init();
    }

    public void init() {
        //inflate(getContext(), R.layout.enhanced_surface, this);

        LayoutInflater mInflater = LayoutInflater.from(cont);
        mInflater.inflate(R.layout.enhanced_surface, this, true);

        controlArea = findViewById(R.id.controlArea);
        surfaceView = findViewById(R.id.surface1);
        moveSender = new UDPClickSender(new InetSocketAddress("192.168.0.164", 20011));



        // following code is for right half of the screen, which emulates mouse
        controlArea.setOnTouchListener(new View.OnTouchListener() {

            private VelocityTracker velocityTracker = null;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int index = event.getActionIndex();
                int action = event.getActionMasked();
                int pointerId = event.getPointerId(index);

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        if (velocityTracker == null) {
                            // Retrieve a new VelocityTracker object to watch the
                            // velocity of a motion.
                            velocityTracker = VelocityTracker.obtain();
                        } else {
                            // Reset the velocity tracker
                            velocityTracker.clear();
                        }
                        // Add a user's movement to the tracker.
                        //velocityTracker.addMovement(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        velocityTracker.addMovement(event);
                        // When you want to determine the velocity, call
                        // computeCurrentVelocity(). Then call getXVelocity()
                        // and getYVelocity() to retrieve the velocity for each pointer ID.
                        velocityTracker.computeCurrentVelocity(10);
                        // Log velocity of pixels per second
                        // Best practice to use VelocityTrackerCompat where possible.
                        Log.d("", "X velocity: " + (short)velocityTracker.getXVelocity(pointerId));
                        Log.d("", "Y velocity: " + (short)velocityTracker.getYVelocity(pointerId));
                        moveSender.moveMouse((short) (sensitivity * velocityTracker.getXVelocity(pointerId)), (short) (sensitivity * velocityTracker.getYVelocity(pointerId)));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // return a VelocityTracker object back sa others can use it again
                        try {
                            velocityTracker.recycle();
                        } catch (IllegalStateException e) { // for "already in the pool exception"
                            //Log.d("Already", "in pool");
                            break;
                        }
                        //velocityTracker = null; // added because otherwise throws "already in the pool"
                        // exception from time to time

                        break;
                }


                return true;

            }
        });


    }


    public SurfaceView getSurfaceView(){
        return this.surfaceView;
    }

}
