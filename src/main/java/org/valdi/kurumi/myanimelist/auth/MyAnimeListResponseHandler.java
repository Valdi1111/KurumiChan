package org.valdi.kurumi.myanimelist.auth;

import org.valdi.kurumi.auth.AuthResponseHandler;

public class MyAnimeListResponseHandler implements AuthResponseHandler {
    private static final String HTML = "<!DOCTYPE html><html><head><title>MyAnimeList Authenticator</title><style>html,body{width:100%;height:100%;-webkit-user-select: none;-ms-user-select: none;user-select: none;}body{display:flex;align-items:center;justify-content:center;background-color:#2E51A2;margin:0px;*{width:100%}}*{font-family:Helvetica,Arial,sans-serif;color:white;text-align:center}</style></head><body><div><h1>Authentication {{ state }}</h1><p title=\"{{ hint }}\">{{ message }}</p></div></body></html>";

    private static final String OK = "&#10004;&#65039;";
    private static final String FAIL = "&#10060;";

    @Override
    public String getResponse(final String code, final String error, final String message, final String hint) {
        final String pass = code == null ? "Failed " + FAIL : "Completed " + OK;
        final String err = error == null ? "" : error;
        final String msg = code == null
                ? "<b>" + err.substring(0, 1).toUpperCase() + err.substring(1).replace('_', ' ') + "</b>: " + (message == null ? "" : message)
                : "You may now close the window.";

        return HTML
                .replace("{{ state }}", pass)
                .replace("{{ hint }}", hint != null ? hint : "")
                .replace("{{ message }}", msg);
    }
}
