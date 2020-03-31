package com.vsb.kru13.osmzhttpserver;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ThreadHandler implements Runnable {

    private Socket _socket;
    private Handler logHandler;
    private Bundle bundle = new Bundle();
    private Message logMsg;
    private MainActivity activity;


    ThreadHandler(Socket socket, Handler logHandler, MainActivity activity) {
        _socket = socket;
        this.logHandler = logHandler;
        this.logMsg = this.logHandler.obtainMessage();
        this.activity = activity;
    }

    public void run(){
        try{
            Socket s;
            s = _socket;
            OutputStream o = s.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String logMessage = getDateString() + " Client: Connected\n";
            logMessage += getDateString() + " Client: IP address:" + s.getInetAddress() + "\n";

            String tmp = in.readLine();
            String urlPath = "DEFAULT";
            Boolean exists = true;
            while (exists) {
                if (!(null == tmp)) {
                    if (!tmp.isEmpty()) {
                        String[] data = tmp.split("\\s+");
                        if (data[0].contains("GET")) {
                            urlPath = data[1];
                            Log.d("Data", data[1]);
                        }
                        tmp = in.readLine();
                    } else {
                        exists = false;
                    }
                } else {
                    exists = false;
                }
            }

            logMessage += getDateString() + " Client: Requested URL: " + urlPath + "\n";
            Date currentTime = Calendar.getInstance().getTime();
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            File file = new File(path, urlPath);
            Log.d("File", file.getAbsolutePath());

            if(urlPath.contains("/camera")){

                out.write("HTTP/1.0 200 OK\n");
                out.write("Date: " + currentTime.toString() + "\n");
                out.write("Content-Type: multipart/x-mixed-replace; boundary=OSMZ_BOUNDARY\n");
                out.write("\n");
                out.flush();

                while(!this.activity.getIsPhotoLoaded()){}
                while(this.activity.getPhotoBytes() == null){}

                Log.d("Bytes", this.activity.getPhotoBytes().toString());
                Log.d("info", "before loop");
                while(true){
                    out.write("--OSMZ_BOUNDARY\n");
                    out.write("Content-Length:" + this.activity.getPhotoBytes().length*8 + "\n");
                    out.write("Content-Type: image/jpg\n");
                    out.write("\n");
                    out.flush();

                    o.write(this.activity.getPhotoBytes());
                    o.flush();

                    while(this.activity.getIsPhotoLoaded()){}
                    while(this.activity.getPhotoBytes() == null){}
                }

            }

            if (file.exists() && !file.isDirectory()) {
                Log.d("File Exists!", file.getAbsolutePath());
                FileInputStream fileInputStream = new FileInputStream(file);

                String mimeType = FileHelper.fileNameExtensionToMimeType(file.getName());
                Log.d("Mime Type", mimeType);
                String generatedContent = "HTTP/1.0 200 OK\n" +
                        "Date: " + currentTime.toString() + "\n" +
                        "Content-Type:" + mimeType + "\n" +
                        "Content-Length:" + file.length() + "\n" +
                        "\n";
                out.write(generatedContent);
                out.flush();

                byte buffer[] = new byte[2048];
                int content;
                while ((content = fileInputStream.read(buffer)) != -1) {
                    o.write(buffer, 0, content);
                }
                o.flush();
                fileInputStream.close();
            }
            if (!file.exists() && !urlPath.contains("/camera")) {
                out.write(HttpStaticResponse.getErrorPage404());
            }
            if (file.exists() && file.isDirectory()) {
                File[] files = file.listFiles();
                //String generatedContent = HttpStaticResponse.getIsFolderPage();
                String generatedContent = "HTTP/1.0 200 OK\n" +
                        "Date: " + currentTime.toString() + "\n" +
                        "Content-Type:" + "text/html" + "\n" +
                        "\n";
                String html = "<html>" +
                        "<body>";
                if (!urlPath.equals("/")) {
                    html += "<p><b><a href=\"/" + file.getParentFile().getCanonicalPath().replace("/storage/emulated/0/", "") + "\">UP</a></b></p>";
                }

                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    Log.d("FileName", f.getName());
                    Log.d("Parent", f.getParent());
                    html += "<p><a href=\"/" + f.getCanonicalPath().replace("/storage/emulated/0/", "") + "\">" + f.getName() + "</a> ................" + f.length() + " KB</p>";
                }
                html += "</body>";
                html += "</html>";
                Log.d("HTML", html);
                Log.d("FilesCount", String.valueOf(files.length));
                out.write(generatedContent);
                out.write(html);
                out.flush();
            }
            o.flush();
            out.flush();
            s.close();
            Log.d("SERVER", "Socket Closed");

            logMessage += getDateString() + " Client: Disconnected\n";
            logMessage += "-----------------\n";
            sendMessage(logMessage);

        }
        catch (IOException e){
            if (_socket != null && _socket.isClosed())
                Log.d("SERVER", "Normal exit");
            else {
                Log.d("SERVER", "Error");
                e.printStackTrace();
                sendMessage(getDateString() + " Server Error: " + e.getMessage() + "\n");
            }
        }
        finally {
            _socket = null;
        }

    }

    private void sendMessage(String logMessage) {
        this.bundle.putString("log", logMessage);
        this.logMsg.setData(this.bundle);
        this.logHandler.sendMessage(this.logMsg);
    }

    private String getDateString(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
