package com.vsb.kru13.osmzhttpserver;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;

public class SocketServer extends Thread {

    ServerSocket serverSocket;
    Handler logHandler;
    public final int port = 12345;
    boolean bRunning;
    MainActivity activity;

    public SocketServer(Handler logHandler, MainActivity activity){
        this.logHandler = logHandler;
        this.activity = activity;
    }

    public void close() {
        if(serverSocket == null){
            return;
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.d("SERVER", "Error, probably interrupted in accept(), see log");
            e.printStackTrace();
        }
        bRunning = false;
    }

    public void run() {
        try {
            Log.d("SERVER", "Creating Socket");
            serverSocket = new ServerSocket(port);
            bRunning = true;
            Semaphore semaphore = new Semaphore(5);
            while (bRunning) {
                Log.d("SERVER", "Socket Waiting for connection");
                Socket s = serverSocket.accept();
                Log.d("SERVER", "Socket Accepted");

                ThreadHandler th = new ThreadHandler(s, logHandler, activity);
                Thread t = new Thread(th);
                t.start();
            }
        }
        catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed())
                Log.d("SERVER", "Normal exit");
            else {
                Log.d("SERVER", "Error");
                e.printStackTrace();
            }
        }
        finally {
            serverSocket = null;
            bRunning = false;
        }
    }

}

