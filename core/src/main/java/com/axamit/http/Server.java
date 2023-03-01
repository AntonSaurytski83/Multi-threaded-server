package com.axamit.http;

import com.axamit.http.conn.Worker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


import static com.axamit.config.ServerConfig.*;

/**

 The Server class creates and runs an SSL server that listens on a specific port.
 It accepts incoming client connections and creates a new thread to handle each request
 using a fixed thread pool of a specific number of threads.
 */

public class Server {

    //INFO:Number of threads = Number of Available Cores * Target CPU utilization * (1 + Wait time / Service time)
    // Number of threads / 0.055 =  the number of requests per second our server can handle with a stable response time ( 2000 on my setup )
    public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors() * (1 + 50 / 5);
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    /**
     * The singleton instance of the server.
     */
    private static Server server = null;
    private ServerSocket serverSocket = null;
    private volatile boolean isRunning = false;

    public Server() {
        //Server init
    }

    /**
     * Starts the server and begins accepting incoming client connections.
     * Prevents race conditions when called concurrently.
     */
    public static synchronized void start() {
        if (server != null && server.isRunning) {
            LOGGER.log(Level.WARNING, () -> "Server is already running!");
            return;
        }
        server = new Server();
        server.isRunning = true;

        ThreadPoolExecutor executorService = (ThreadPoolExecutor)Executors.newFixedThreadPool(NUM_THREADS);
        LOGGER.log(Level.INFO, () -> "Number threads " + NUM_THREADS);
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            server.serverSocket = serverSocket;
            server.serverSocket.setSoTimeout(SOCKET_TIMEOUT); // Set timeout to 5 second

            LOGGER.log(Level.INFO, () -> "Web server started on port : " + SERVER_PORT);
            while (server.isRunning) {
                try {
                    Socket clientSocket = server.serverSocket.accept();
                    LOGGER.log(Level.INFO, "Accepted connection from {0}", clientSocket.getInetAddress().getHostAddress());
                    // Submit new task to thread pool
                    executorService.submit(() -> {
                        new Worker(clientSocket).run();
                        LOGGER.log(Level.INFO, "Completed task, active threads: {0}, completed tasks: {1}", new Object[] { executorService.getActiveCount(), executorService.getCompletedTaskCount() });
                    });
                    LOGGER.log(Level.INFO, () -> "Pool " + executorService.getLargestPoolSize());
                } catch (SocketTimeoutException e) {
                    // Timeout occurred, check if server should stop
                    if (!server.isRunning) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, () -> "Error starting server" + e.getLocalizedMessage());
        } finally {
            if (executorService != null) {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Interrupted while waiting for executor to terminate: {0}", e.getLocalizedMessage());
                }
            }
            if (server != null) {
                server.stop();
            }
        }
    }

    //preventing race conditions
    public synchronized void stop() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, () -> "Error closing server socket" + e.getLocalizedMessage());
            }
        }
        isRunning = false;
    }

}
