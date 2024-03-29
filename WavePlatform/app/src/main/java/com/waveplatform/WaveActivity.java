package com.waveplatform;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;


public class WaveActivity extends Activity {

    WaveView Wave;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Wave = new WaveView(this);
        setContentView(Wave);
    }
    class WaveView extends SurfaceView implements Runnable {
        Thread gameThread = null; //game thread
        SurfaceHolder holder;
        volatile boolean playing; //are we playing?
        boolean paused = true;
        Canvas canvas;
        Paint paint;
        long fps;
        long timeThisFrame;
        private int lives = 3;
        private int score = 0;
        private int screenX;
        private int screenY;
        private int c = 0;
        ArrayList<Platform> platList = new ArrayList();
        Random r = new Random(System.currentTimeMillis());

        Note note;
        HalfRest halfRest;
        Platform platform;
        Bitmap n = BitmapFactory.decodeResource(this.getResources(), R.drawable.ball);
        Bitmap Sharp;

        public WaveView(Context context) {
            super(context);

            holder = getHolder();
            paint = new Paint();

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            screenX = size.x;
            screenY = size.y;

            note = new Note(n, screenX, screenY);
            halfRest = new HalfRest(n, screenX+200,screenY);
            for (int i = 0; i < 20; i++)
                newPlatform();
        }

        private void newPlatform(){
                if (c > 8)
                    c = 0;
                float y[] = {screenX, screenX-250, screenX-500, screenX-750, screenX-1000, screenX+750, screenX+500, screenX+250, screenX};
                platform = new Platform(screenX + 400, y[c]);
                c++;
                platList.add(platform);
                platform = null;
        }

        @Override
        public void run() {
            Looper.prepare();
            while (playing) {
                long startTimeFrame = System.currentTimeMillis();
                if (!paused)
                    update();//update movements
                draw();//draw objects
                timeThisFrame = System.currentTimeMillis() - startTimeFrame;
                if (timeThisFrame >= 1)
                    fps = 1000 / timeThisFrame;
            }
        }

        public void update() {
            note.update(fps);
            for (int i = 0; i < platList.size(); i++) {
                if (i == 0)
                    platList.get(i).update(fps);
                else if ((platList.get(i - 1).getPlatform().right - platList.get(i).getPlatform().left) > 350)
                    platList.get(i).update(fps);
                if (platList.get(i).getPlatform().right <= 0) {
                    platList.remove(i);
                    newPlatform();

                }

            }

        }

        public void draw() {
            if (holder.getSurface().isValid()) {
                canvas = holder.lockCanvas();

                canvas.drawColor(Color.argb(255,  26, 128, 182));

                canvas.drawBitmap(note.getNote(), note.getX(), note.getY(), paint);
                //canvas.drawBitmap(halfRest.getHalfRest(), halfRest.getX(), halfRest.getY(), paint);
                //paint.setColor(Color.argb(r.nextInt(255), r.nextInt(255), r.nextInt(255), r.nextInt(255)));
                for (int i = 0; i < platList.size(); i++)
                    canvas.drawRect(platList.get(i).getPlatform(), paint);


                holder.unlockCanvasAndPost(canvas);

            }
        }

        private boolean isAlive() {
            return lives > 0;
        }

        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error:", "joining thread");
            }
        }

        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    paused = false;
                    note.setMovement(2);
                    //if (motionEvent.getDownTime() >1000);
                        //note.setMovement(3);
                    break;
                case MotionEvent.ACTION_UP:
                    note.setMovement(3);
                    break;
            }
        return true;
    }
}
    public void onResume(){
        super.onResume();

       Wave.resume();
    }
    public void onPause(){
        super.onPause();

        Wave.pause();
    }
}
