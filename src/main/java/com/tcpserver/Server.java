package com.tcpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static Server server;
    private static final int THREAD_POOL_SIZE = 10; // Define the pool size
    private ExecutorService threadPool;

    private Server(){
        threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public static Server getServer(){
        if(server == null){
            return new Server();
        }
        return server;
    }


    public void listenAndExecuteRequest() {
        try(ServerSocket serverSocket = new ServerSocket(8082)){
            while (true) {
                System.out.println("Listening on port 8082");
                // Wait for a client to connect
                Socket socket = serverSocket.accept();
                //use a thread from thread to execute the task
                threadPool.submit(() -> run(socket));
            }
        }catch (Exception e){
            System.out.println("Exception establishing connection "+e);
        }

    }

    public void run(Socket socket){
        try{

            System.out.println("Created new Thread "+Thread.currentThread().threadId()+" and new client connected");

            // Read the HTTP headers
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            int contentLength = getContentLengthFromRequest(socket,reader);

            // Read the body of the HTTP request
            readIncomingRequest(reader, contentLength);

            // Respond to the client with a valid HTTP/1.1 response
            sendResponseToClient(socket);

            // Close the connection with the client
            socket.close();
        }catch (Exception e){
            System.out.println("Exception caught during reading request and servinf response : "+e);
        }
    }

    private void sendResponseToClient(Socket socket) throws IOException {
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: text/plain");
        writer.println("Content-Length: 18"); // Length of the body ("Hello from server!")
        writer.println(); // Empty line between headers and body
        writer.println("Hello from server!");
    }

    private void readIncomingRequest(BufferedReader reader, int contentLength) throws IOException {
        char[] body = new char[contentLength];
        reader.read(body, 0, contentLength);
        String requestBody = new String(body);

        System.out.println("Message from client: " + requestBody);

    }

    private int getContentLengthFromRequest(Socket socket, BufferedReader reader) throws IOException {
        String line;
        int contentLength = 0;
        // Read headers and look for Content-Length to know how much data to read from body
        while (!(line = reader.readLine()).isEmpty()) {
//                System.out.println("Header: " + line); //uncomment to print headers
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.split(" ")[1]);
            }
        }
        return contentLength;
    }
}
