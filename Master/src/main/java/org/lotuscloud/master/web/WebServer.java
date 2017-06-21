package org.lotuscloud.master.web;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Copyright (c) 2017 Lennart Heinrich
 * www.lennarth.com
 */
public class WebServer {

    private int port;
    private ServerSocket serverSocket;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private HashMap<String, WebHandler> handlers = new HashMap<>();

    public WebServer(int port) {
        this.port = port;
        try {
            serverSocket = new ServerSocket(port);
            new Thread(() -> {
                try {
                    while (serverSocket != null && !serverSocket.isClosed()) {
                        Socket socket = serverSocket.accept();

                        executor.submit(() -> {
                            try {
                                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                                String request = in.readLine();

                                while (in.ready())
                                    request += System.lineSeparator() + in.readLine();

                                out.write("HTTP/1.0 200 OK\r\n");
                                out.write("Content-Type: text/html\r\n");

                                String[] splittedRequest = request.split(" ");
                                String handlerName = splittedRequest[1].split("\\?")[0].substring(1);

                                String response;

                                if (handlers.containsKey(handlerName)) {
                                    String preRawRequest = splittedRequest[1];
                                    String rawRequest = preRawRequest.substring(preRawRequest.length() > 2 ? 2 : 1);
                                    String[] splittedRawRequest = rawRequest.split("&");

                                    HashMap<String, String> requestMap = new HashMap<>();

                                    for (String rawSplittedRawRequest : splittedRawRequest) {
                                        String[] splittedSplittedRawRequest = rawSplittedRawRequest.split("=", 2);

                                        if (splittedSplittedRawRequest.length >= 2)
                                            requestMap.put(splittedSplittedRawRequest[0], splittedSplittedRawRequest[1]);
                                    }

                                    response = handlers.get(handlerName).process(requestMap);
                                } else
                                    response = "<!DOCTYPE html><html><body>Der WebHandler '" + handlerName + "' wurde nicht gefunden</body></html>";

                                out.write("Content-Length: " + response.length() + "\r\n");
                                out.write("\r\n");

                                out.write(response);

                                out.flush();
                                in.close();
                                out.close();

                                socket.close();
                            } catch (NullPointerException ex) {
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                if (!socket.isClosed())
                                    try {
                                        socket.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                            }
                        });
                    }
                } catch (IOException ex) {
                }
            }).start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            serverSocket = null;
        }
    }

    public void registerHandler(String handlerName, WebHandler handler) {
        unregisterHandler(handlerName);
        handlers.put(handlerName, handler);
    }

    public void unregisterHandler(String handlerName) {
        if (handlers.containsKey(handlerName))
            handlers.remove(handlerName);
    }
}