package org.valdi.kurumi.anilist.auth;

import org.valdi.kurumi.auth.AbstractAuthHandler;
import org.valdi.kurumi.auth.AuthResponseHandler;

public class AniListAuthHandler extends AbstractAuthHandler {

    public AniListAuthHandler(AuthResponseHandler handler) {
        super(handler == null ? new AniListResponseHandler() : handler);
    }

}
