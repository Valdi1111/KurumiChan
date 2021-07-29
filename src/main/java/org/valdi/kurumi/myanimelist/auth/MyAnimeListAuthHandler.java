package org.valdi.kurumi.myanimelist.auth;

import org.valdi.kurumi.auth.AbstractAuthHandler;
import org.valdi.kurumi.auth.AuthResponseHandler;

public class MyAnimeListAuthHandler extends AbstractAuthHandler {

    public MyAnimeListAuthHandler(AuthResponseHandler handler) {
        super(handler == null ? new MyAnimeListResponseHandler() : handler);
    }

}
