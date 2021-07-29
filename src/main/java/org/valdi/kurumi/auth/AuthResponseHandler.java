package org.valdi.kurumi.auth;

import org.valdi.kurumi.myanimelist.auth.MyAnimeListAuthenticator;

/**
 * The response handler determines what the webpage will look like after the user authenticates with MyAnimeList.
 *
 * @see MyAnimeListAuthenticator
 * @see MyAnimeListAuthenticator#setResponseHandler(AuthResponseHandler)
 */
public interface AuthResponseHandler {

    /**
     * Sends the response HTML given the query parameters. Some unicode characters may not render properly, use HTML entities if you encounter this problem.
     *
     * @param code authorization code (null if error)
     * @param error error (nullable)
     * @param message error message (nullable)
     * @param hint error hint (nullable)
     * @return HTML string
     *
     * @since 1.1.0
     */
    String getResponse(final String code, final String error , final String message, final String hint);

}
