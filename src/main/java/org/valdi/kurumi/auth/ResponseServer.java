package org.valdi.kurumi.auth;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResponseServer {
    private ExecutorService exec;
    private HttpServer server;
    private final int port;

    public ResponseServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        this.exec = Executors.newSingleThreadExecutor();
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(exec);
        this.server.start();
    }

    public void close() {
        this.exec.shutdownNow();
        this.server.stop(0);
        this.exec = null;
        this.server = null;
    }

    public void registerHandler(String context, AuthHandler handler) {
        this.server.createContext("/" + context, handler);
    }

    public void unregisterHandler(String context) {
        this.server.removeContext("/" + context);
    }
}
