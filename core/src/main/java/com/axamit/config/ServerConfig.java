package com.axamit.config;

public final class ServerConfig {

    private ServerConfig() {
    }

    public static final int SERVER_PORT = 8080;

    public static final String WEB_ROOT = "web/";

    public static final String INDEX_FILE = "/index.html";

    public static final String FILE_UPLOAD_DIRECTORY = "/upload";

    public static final int NORMAL_REQ_LENGTH = 3;

    public static final int SOCKET_TIMEOUT = 5000; // 5 seconds timeout for each connection

}
