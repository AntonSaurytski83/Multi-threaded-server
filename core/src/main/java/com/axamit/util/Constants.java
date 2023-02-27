package com.axamit.util;

public final class Constants {

    private Constants() {
    }

    public static final String HTTP_1_1_405_METHOD_NOT_ALLOWED = "HTTP/1.1 405 Method Not Allowed\r\n";
    public static final String CONTENT_TYPE_TEXT_PLAIN = "Content-Type: text/plain\r\n";
    public static final String CONNECTION_CLOSE = "Connection: close\r\n\r\n";
    public static final String CONTENT_LENGTH_0 = "Content-Length: 0\r\n\r\n";
    public static final String HTTP_1_1_400_BAD_REQUEST = "HTTP/1.1 400 Bad Request\r\n";
    public static final String HTTP_1_1_404_NOT_FOUND = "HTTP/1.1 404 Not Found\r\n\r\n";
    public static final String HTTP_1_1_200_OK = "HTTP/1.1 200 OK";
    public static final String CONTENT_TYPE = "\r\nContent-Type: ";
    public static final String CONTENT_LENGTH = "\r\nContent-Length: ";
    public static final String CONNECTION_KEEP_ALIVE = "\r\nConnection: keep-alive\r\n\r\n";

    public static final String HTTP_1_1_201_CREATED = "HTTP/1.1 201 Created\r\n";
    public static final String LOCATION = "Location: ";
    public static final String HTTP_1_1_500_INTERNAL_SERVER_ERROR = "HTTP/1.1 500 Internal Server Error\r\n";

}
