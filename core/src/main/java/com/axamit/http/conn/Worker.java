package com.axamit.http.conn;


import com.axamit.http.hadler.GetReqHandler;
import com.axamit.http.hadler.PutReqHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.MalformedInputException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.axamit.util.Constants.*;
import static com.axamit.config.ServerConfig.*;


public class Worker implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Worker.class.getName());
    private final Socket clientSocket;

    public Worker(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream())) {

            // enable HTTP/1.1 keep-alive behavior
            boolean keepAlive;
            String connectionHeader = StringUtils.EMPTY;
            do {
                // reset the flag at the beginning of each iteration
                keepAlive = false;
                // parse HTTP request
                String[] requestTokens = getRequestTokens(in, out);
                if (ArrayUtils.isEmpty(requestTokens)) {
                    break;
                }
                String method = requestTokens[0];
                String path = ("/").equals(requestTokens[1]) ? INDEX_FILE : requestTokens[1];
                String fileName = path.substring(path.lastIndexOf("/") + 1);
                LOGGER.log(Level.INFO,() -> "Request path: " + path);
                String contentType = guessContentType(fileName);
                if (("GET").equals(method)) {
                    keepAlive = GetReqHandler.handleReq(in, out, keepAlive, fileName, contentType, connectionHeader);
                    if(keepAlive){
                        clientSocket.setKeepAlive(true);
                    }
                } else if (("POST").equals(method)) {
                    PutReqHandler.handleReq(in, out, requestTokens, fileName);
                } else {
                    String errorResponse = HTTP_1_1_405_METHOD_NOT_ALLOWED;
                    errorResponse += CONTENT_TYPE_TEXT_PLAIN;
                    errorResponse += CONNECTION_CLOSE;
                    errorResponse += CONTENT_LENGTH_0;
                    out.write(errorResponse.getBytes());
                }
                out.flush();
            } while (keepAlive);
        } catch (MalformedInputException e) {
            LOGGER.log(Level.WARNING,() -> "Invalid characters in file: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.WARNING,() -> "Unsupported encoding: " + e.getMessage());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING,() -> "Error handling request: " + e.getLocalizedMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING,() -> "Error closing client socket: " + e.getLocalizedMessage());
            }
        }
    }

    private String[] getRequestTokens(BufferedReader in, BufferedOutputStream out) throws IOException {
        String[] requestTokens = new String[0];
        String requestLine = in.readLine();
        if (StringUtils.isBlank(requestLine)) {
            // handle empty request
            String response = HTTP_1_1_400_BAD_REQUEST;
            response += CONTENT_TYPE_TEXT_PLAIN;
            response += CONNECTION_CLOSE;
            out.write(response.getBytes());
            out.flush();
            return requestTokens;
        }
        requestTokens = requestLine.split(" ");
        if (requestTokens.length != NORMAL_REQ_LENGTH) {
            // handle malformed request line
            String response = HTTP_1_1_400_BAD_REQUEST;
            response += CONTENT_TYPE_TEXT_PLAIN;
            response += CONNECTION_CLOSE;
            out.write(response.getBytes());
            out.flush();
            return requestTokens;
        }
        return requestTokens;
    }

    private String guessContentType(String fileName) {
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
            extension = fileName.substring(dotIndex + 1).toLowerCase();
        }
        switch (extension) {
            case "html":
            case "htm":
                return "text/html";
            case "css":
                return "text/css";
            case "js":
                return "application/javascript";
            case "json":
                return "application/json";
            case "gif":
                return "image/gif";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            default:
                return "application/octet-stream";
        }
    }

}

