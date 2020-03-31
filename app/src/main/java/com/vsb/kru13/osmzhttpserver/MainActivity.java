package com.vsb.kru13.osmzhttpserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Picture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private SocketServer s;
    private static final int READ_EXTERNAL_STORAGE = 1;
    //private static final String MSG_VOLUME_KEY = "volume";
    private static final String MSG_LOG_KEY = "log";
    Camera camera;
    Picture Picture;
    private byte[] photoBytes;
    private boolean photoLoaded = true;
    private CameraPreview mPreview;


    Timer timerObj = new Timer();
    TimerTask timerTaskObj = new TimerTask() {
        public void run() {
            camera.takePicture(null, null, mPicture);
        }
    };

    @SuppressLint("HandlerLeak")
    private final Handler logHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String string = bundle.getString(MSG_LOG_KEY);
            final TextView logTextView = (TextView)findViewById(R.id.textView);
            logTextView.setText(logTextView.getText() + string);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn1 = (Button)findViewById(R.id.button1);
        Button btn2 = (Button)findViewById(R.id.button2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn1.performClick();
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button1) {

            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
            } else {
                s = new SocketServer(logHandler, this);
                s.start();
                Message msg = this.logHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString(MSG_LOG_KEY, new SimpleDateFormat("yyy-MM-dd HH:mm:ss").format(new Date()) + " Server: Starts\n---------\n");
                msg.setData(bundle);
                this.logHandler.sendMessage(msg);


                camera = Camera.open();
                List<Camera.Size> a = camera.getParameters().getSupportedPictureSizes();
                Camera.Parameters params = camera.getParameters();
                params.setPictureSize(a.get(a.size() - 1).width, a.get(a.size() - 1).height);

                mPreview = new CameraPreview(this, camera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                preview.addView(mPreview);
                timerObj.schedule(timerTaskObj, 0, 500);
            }


        }
        if (v.getId() == R.id.button2) {
            if(s == null){
                Message msg = this.logHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString(MSG_LOG_KEY, new SimpleDateFormat("yyy-MM-dd HH:mm:ss").format(new Date()) + " Server: Unable to stop\n");
                msg.setData(bundle);
                return;
            }
            s.close();

            try {
                Message msg = this.logHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString(MSG_LOG_KEY, new SimpleDateFormat("yyy-MM-dd HH:mm:ss").format(new Date()) + " Server: Stops\n");
                msg.setData(bundle);
                s.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (v.getId() == R.id.buttonClear) {

            TextView txt = findViewById(R.id.textView);
            Editable ed = txt.getEditableText();
            ed.clear();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case READ_EXTERNAL_STORAGE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    s = new SocketServer(logHandler, this);
                    s.start();
                }
                break;

            default:
                break;
        }
    }
    public synchronized boolean getIsPhotoLoaded(){
        return photoLoaded;
    }
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.startPreview();
            photoLoaded = false;
            photoBytes = data;
            photoLoaded = true;
        }
    };
    public synchronized byte[] getPhotoBytes(){
        return photoBytes;
    }
}
