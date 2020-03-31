package com.vsb.kru13.osmzhttpserver;

public class HttpStaticResponse {
    static String errorPage404 = "HTTP/1.1 404 NOT FOUND\n" +
            "Date: Mon, 27 Jul 2009 12:28:53 GMT\n" +
            "Server: Apache/2.2.14 (Win32)\n" +
            "Last-Modified: Wed, 22 Jul 2009 19:15:56 GMT\n" +
            "Content-Length: 88\n" +
            "Content-Type: text/html\n" +
            "Connection: Closed" +
            "\n\n" +
            "<html>\n" +
            "<body>\n" +
            "<h1>Error 404, not found!</h1>\n" +
            "</body>\n" +
            "</html>";
    static String isFolderPage = "HTTP/1.1 200 OK\n" +
            "Date: Mon, 27 Jul 2009 12:28:53 GMT\n" +
            "Server: Apache/2.2.14 (Win32)\n" +
            "Last-Modified: Wed, 22 Jul 2009 19:15:56 GMT\n" +
            "Content-Length: 88\n" +
            "Content-Type: text/html\n" +
            "Connection: Closed" +
            "\n\n" +
            "<html>\n" +
            "<body>\n" +
            "<h1>This is a directory.</h1>\n" +
            "</body>\n" +
            "</html>";
    public static String getErrorPage404(){
        return errorPage404;
    }
    public static String getIsFolderPage(){
        return isFolderPage;
    }
}
