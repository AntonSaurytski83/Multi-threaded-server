package com.axamit.http.hadler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

import static com.axamit.util.Constants.*;
import static com.axamit.config.ServerConfig.FILE_UPLOAD_DIRECTORY;
import static com.axamit.config.ServerConfig.WEB_ROOT;

public final class PutReqHandler {

    private PutReqHandler() {
    }

    public static void handleReq(Reader in, OutputStream out, String[] requestTokens, String fileName) throws IOException {
        // read request body and write to file
        File fileToUpload = new File(WEB_ROOT + FILE_UPLOAD_DIRECTORY, fileName);
        try (Writer fileWriter = Files.newBufferedWriter(fileToUpload.toPath(), StandardCharsets.UTF_8)) {
            // read request body and write to file
            int contentLength = Integer.parseInt(Objects.requireNonNull(extractHeaderValue(requestTokens[2], "Content-Length")));
            char[] buffer = new char[4096];
            int charsRead = 0;
            while (contentLength > 0 && (charsRead = in.read(buffer, 0, Math.min(buffer.length, contentLength))) != -1) {
                fileWriter.write(buffer, 0, charsRead);
                contentLength -= charsRead;
            }
            // create response with status code 201 (Created) and Location header
            String response = HTTP_1_1_201_CREATED;
            response += LOCATION + fileName + "\r\n";
            response += CONTENT_LENGTH_0;
            response += CONNECTION_CLOSE;
            out.write(response.getBytes());
        } catch (IOException e) {
            // handle file write error
            String errorResponse = HTTP_1_1_500_INTERNAL_SERVER_ERROR;
            errorResponse += CONTENT_TYPE_TEXT_PLAIN;
            errorResponse += CONNECTION_CLOSE;
            errorResponse += "Error writing file: " + e.getMessage() + "\r\n";
            out.write(errorResponse.getBytes());
        } catch (NumberFormatException e) {
            // handle invalid Content-Length header
            String errorResponse = HTTP_1_1_400_BAD_REQUEST;
            errorResponse += CONTENT_TYPE_TEXT_PLAIN;
            errorResponse += CONNECTION_CLOSE;
            errorResponse += "Invalid Content-Length header: " + e.getMessage() + "\r\n";
            out.write(errorResponse.getBytes());
        }
    }

    private static String extractHeaderValue(String header, String key) {
        if (header == null || key == null) {
            return null;
        }
        String[] tokens = header.split(":");
        if (tokens.length != 2) {
            return null;
        }
        if (!tokens[0].trim().equalsIgnoreCase(key)) {
            return null;
        }
        return tokens[1].trim();
    }

}
