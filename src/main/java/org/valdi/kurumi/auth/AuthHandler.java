package org.valdi.kurumi.auth;

import com.sun.net.httpserver.HttpHandler;

public interface AuthHandler extends HttpHandler {

    String getAuth();

    String getState();

}
