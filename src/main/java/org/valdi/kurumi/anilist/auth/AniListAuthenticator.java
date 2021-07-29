package org.valdi.kurumi.anilist.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.valdi.kurumi.auth.*;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static org.valdi.kurumi.anilist.AniListConstants.*;

public class AniListAuthenticator {
    private final ResponseServer server;
    private final String client_id;
    private final String client_secret;

    private boolean openBrowser = false;
    private long timeout = 60 * 3;
    private String redirect_URI = null;

    private AuthResponseHandler responseHandler = null;
    private Consumer<String> urlCallback = null;

    private AuthToken token;

    /**
     * Instantiates a local server builder with a client id, client secret, and port.
     *
     * @param client_id     client id
     * @param client_secret client secret
     */
    public AniListAuthenticator(final ResponseServer server, final String client_id, final String client_secret, final String redirect_URI) {
        Objects.requireNonNull(server, "Server must not be null");
        Objects.requireNonNull(client_id, "Client ID must not be null");
        Objects.requireNonNull(client_secret, "Client secret must not be null");
        Objects.requireNonNull(redirect_URI, "Redirect URI must not be null");

        this.server = server;
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.redirect_URI = redirect_URI;
    }

    /**
     * Indicates that the authorization page should be opened in the user's browser automatically.
     *
     * @return builder
     * @see #openBrowser(boolean)
     */
    public final AniListAuthenticator openBrowser() {
        return openBrowser(true);
    }

    /**
     * If true, a browser will automatically be opened to the authorization page. Note that if this is false you will
     * have to go to the page manually using {@link #getAuthorizationURL(String, String)}.
     *
     * @param openBrowser if browser should be opened
     * @return builder
     * @see #openBrowser()
     */
    public final AniListAuthenticator openBrowser(final boolean openBrowser) {
        if (!canOpenBrowser())
            System.out.println("WARNING: System may not support openBrowser()");
        this.openBrowser = openBrowser;
        return this;
    }

    /**
     * Sets how long (in seconds) that the server will expire in. Once the timeout has passed a new
     * authentication builder must be used.
     *
     * @param timeout server timeout
     * @return builder
     */
    public final AniListAuthenticator setTimeout(final int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Sets the response handler.
     *
     * @param responseHandler response handler
     * @return builder
     * @see AuthResponseHandler
     */
    public final AniListAuthenticator setResponseHandler(final AuthResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
        return this;
    }

    /**
     * Sets the auth URL callback method. Used to retrieve the URL that generates the authorization code. Runs in its own thread.
     * <br>
     * This method can be used to handle the auth URL in the case that the system doesn't support the
     * {@link org.valdi.kurumi.myanimelist.auth.MyAnimeListAuthenticator#openBrowser()} method.
     *
     * @param consumer consumer
     * @return builder
     * @see Consumer
     */
    public final AniListAuthenticator setURLCallback(final Consumer<String> consumer) {
        this.urlCallback = consumer;
        return this;
    }

    public void fromToken(final AuthToken token) throws TokenRetrieveException {
        if(this.token != null) {
            throw new TokenRetrieveException("Cannot create from token, it already exists.");
        }

        this.token = token;
    }

    public AuthToken createToken() throws TokenRetrieveException {
        if(this.token != null) {
            throw new TokenRetrieveException("Cannot create token, it already exists.");
        }

        try {
            String auth = this.authorize();
            this.token = this.authenticate(auth);
            return token;
        } catch (IOException | InterruptedException e) {
            throw new TokenRetrieveException(e.getMessage(), e.getCause());
        }
    }

    // [authorization]
    private String authorize() throws IOException {
        final String url = getAuthorizationURL(client_id, redirect_URI);

        if (urlCallback != null)
            new Thread(() -> urlCallback.accept(url)).start();

        final AniListAuthHandler handler = new AniListAuthHandler(responseHandler);

        // Start
        server.registerHandler(CONTEXT, handler);

        if (openBrowser)
            if (!canOpenBrowser())
                System.out.println("Desktop is not supported on this operating system. Please go to this URL manually: '" + url + "' or use a URL callback");
            else
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (final URISyntaxException ignored) {
                    // Stop
                    server.unregisterHandler(CONTEXT);
                    throw new IllegalArgumentException("URL syntax was invalid (most likely the client id or redirect URI wasn't encoded correctly)");
                }

        try {
            handler.await(timeout);
        } catch (final InterruptedException ignored) {
        } // soft failure

        // Stop
        server.unregisterHandler(CONTEXT);

        if (handler.getAuth() == null)
            throw new NullPointerException("Failed to authorize request (server was closed before a response could be received)");

        return handler.getAuth();
    }

    private AuthToken authenticate(final String authorizationCode) throws IOException, InterruptedException {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        HttpClient client = HttpClient.newBuilder()
                .executor(exec)
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // uri
        URI uri = URI.create(TOKEN_URL);
        // form parameters
        Map<Object, Object> data = new HashMap<>();
        data.put("client_id", client_id);
        data.put("client_secret", client_secret);
        data.put("grant_type", "authorization_code");
        data.put("redirect_uri", redirect_URI);
        data.put("code", authorizationCode);

        // Request body from a String
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(ofFormData(data))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Accept", "application/json")
                .build();

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .disableHtmlEscaping()
                .create();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        AuthToken token = gson.fromJson(response.body(), AuthToken.class);
        return token;
    }

    public AuthToken refreshToken() throws TokenRetrieveException {
        if(this.token == null) {
            throw new TokenRetrieveException("Cannot refresh token, it doesn't exists.");
        }

        try {
            this.token = refreshToken0(token);
            return token;
        } catch (IOException | InterruptedException e) {
            throw new TokenRetrieveException(e.getMessage(), e.getCause());
        }
    }

    private AuthToken refreshToken0(final AuthToken oldToken) throws IOException, InterruptedException {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        HttpClient client = HttpClient.newBuilder()
                .executor(exec)
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // uri
        URI uri = URI.create(TOKEN_URL);
        // form parameters
        Map<Object, Object> data = new HashMap<>();
        data.put("grant_type", "refresh_token");
        data.put("refresh_token", oldToken.getRefreshToken());

        // Request body from a String
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(ofFormData(data))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Authorization", oldToken.getHeaderAuthorization())
                .build();

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .disableHtmlEscaping()
                .create();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        AuthToken token = gson.fromJson(response.body(), AuthToken.class);
        return token;
    }

    // Sample: 'password=123&custom=secret&username=abc&ts=1570704369823'
    private HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    /**
     * Returns if Java has permission to open the client browser.
     *
     * @return if Java can open the browser
     */
    private boolean canOpenBrowser() {
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
    }

    // URL

    /**
     * Returns the authorization URL for a client id.
     *
     * @param client_id           client id. <i>required</i>
     * @param redirect_URI        preregistered URI, only needed if you want to select a specific application redirect URI. <i>optional</i>
     * @return authorization URL
     * @throws NullPointerException if client ID or PKCE is null
     */
    private String getAuthorizationURL(final String client_id, final String redirect_URI) {
        Objects.requireNonNull(client_id, "Client ID must not be null");
        Objects.requireNonNull(redirect_URI, "PKCE must not be null");
        return String.format(AUTH_URL, client_id) +
                (redirect_URI != null ? String.format(AUTH_REDIRECT_URI, redirect_URI) : "");
    }

    public AuthToken getToken() {
        return token;
    }

}
