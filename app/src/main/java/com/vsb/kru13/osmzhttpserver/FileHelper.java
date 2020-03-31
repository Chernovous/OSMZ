package com.vsb.kru13.osmzhttpserver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

public class FileHelper {

    public static String getFileContent (FileInputStream fis) throws IOException {
        BufferedReader br = new BufferedReader( new InputStreamReader(fis));

        StringBuilder sb = new StringBuilder();
        String line;
        while(( line = br.readLine()) != null ) {
            sb.append( line );
            sb.append( '\n' );
        }
        return sb.toString();
    }

    public static String fileNameExtensionToMimeType(String fileName){
        String extension = fileName.substring(fileName.lastIndexOf("."));
        extension = extension.replaceFirst(".","");
        Map map = new HashMap();
        map.put("txt", "text/plain");
        map.put("png", "image/png");
        map.put("jpg", "image/jpeg");
        map.put("mp4", "video/mp4");
        map.put("html", "text/html");

        Object result = map.get(extension);
        if(result !=null){
            return result.toString();
        }
        else{
            return "text/plain";
        }
    }
}
