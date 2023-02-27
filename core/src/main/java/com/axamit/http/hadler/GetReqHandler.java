package com.axamit.http.hadler;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import static com.axamit.util.Constants.*;
import static com.axamit.config.ServerConfig.WEB_ROOT;

public final class GetReqHandler {

    private GetReqHandler() {
    }

    public static boolean handleReq(BufferedReader in, OutputStream out, boolean keepAlive, String fileName, String contentType, String connectionHeader) throws IOException {
        File fileToSend = new File(WEB_ROOT + fileName);
        if (!fileToSend.exists()) {
            // handle 404 error
            out.write(HTTP_1_1_404_NOT_FOUND.getBytes());
        } else {
            // read file and send response to client
            byte[] fileBytes = Files.readAllBytes(fileToSend.toPath());
            String response = HTTP_1_1_200_OK;
            response += CONTENT_TYPE + contentType;
            response += CONTENT_LENGTH + fileBytes.length;

            //INFO:check for client's preference for keep-alive
            //  If the client does not include this header,
            //  the server will default to closing the connection after each request/response cycle.
            connectionHeader = getKeepHeader(in)[2].toLowerCase();
            if (connectionHeader.contains("keep-alive")) {
                keepAlive = true;
                response += CONNECTION_KEEP_ALIVE;
            } else if (connectionHeader.contains("close")) {
                response += CONNECTION_CLOSE;
            }
            out.write(response.getBytes());
            out.write(fileBytes);
        }
        return keepAlive;
    }

    private static String[] getKeepHeader(BufferedReader in) throws IOException {
        String[] requestTokens = new String[0];
        String requestLine = in.readLine();
        if (StringUtils.isBlank(requestLine)) {
            return requestTokens;
        }
        requestTokens = requestLine.split("\\s+");
        while (in.ready()) {
            String headerLine = in.readLine();
            if (StringUtils.isBlank(headerLine)) {
                break;
            }
            requestTokens = ArrayUtils.add(requestTokens, headerLine);
        }
        return requestTokens;
    }
}
