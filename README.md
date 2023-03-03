Multi-core multi-threaded web server with thread-pooling

Task: A multi-threaded (e.g. file-based) web server with thread-pooling implemented in Java.

Solution: We can implement a web server in Java using the following approach:

○ Use a socket to listen for incoming connections from clients.\
○ When a client connects, accept the connection and create a new thread to handle it.\
○ In the new thread, read the HTTP request from the client and parse it.\
○ Determine the requested resource (e.g., HTML page or image file) and locate it on the server's file system.\
○ Send the requested resource to the client in an HTTP response.\
○ Close the connection and exit the thread.\
○ To implement thread-pooling, we can use a fixed-size thread pool that will handle incoming requests. When a client connection is accepted, we can assign it to an available thread in the pool. If all threads are busy, the incoming connection will be queued until a thread becomes available.

We can split this task into several OOP concepts as follows:

○ Networking: We will use the Java socket API to listen for incoming connections and accept them. We will also use the HTTP protocol to communicate with clients.\
○ Multi-threading: We will use threads to handle incoming connections concurrently. We will also use a thread pool to limit the number of threads that are created.\
○ File I/O: We will read and write files from the server's file system in order to serve requested resources.\
○ HTTP: We will parse HTTP requests and construct HTTP responses to communicate with clients.

To add proper HTTP/1.1 keep-alive behavior to our implementation based on the http-client's capabilities exposed through its request headers, we need to modify the RequestHandler to parse the Connection header in the HTTP request and honor the client's preference for keep-alive behavior.

With this implementation, we have completed a multi-threaded, file-based web server with thread-pooling implemented in Java, with proper HTTP/1.1 keep-alive behavior based on the request headers. This implementation should be able to handle a high volume of incoming requests and provide a scalable solution for serving web content. However, as with any production-ready system, it's important to thoroughly test and validate the implementation before deploying it to a production environment.\

<p align="center"><img src="https://i.ibb.co/1QNXc6B/BENCH-10-100.png"/></p>
