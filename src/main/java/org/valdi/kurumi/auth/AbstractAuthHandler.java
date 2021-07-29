package org.valdi.kurumi.auth;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPOutputStream;

public class AbstractAuthHandler implements AuthHandler {
    private final CountDownLatch latch;
    private final AuthResponseHandler handler;

    public AbstractAuthHandler(final AuthResponseHandler handler) {
        this.latch = new CountDownLatch(1);
        this.handler = handler;
    }

    public void await(long timeout) throws InterruptedException {
        latch.await(timeout, TimeUnit.SECONDS);
    }

    private transient final AtomicReference<String> auth = new AtomicReference<>();
    private transient final AtomicReference<String> state = new AtomicReference<>();

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final Map<String, String> query = parseWwwFormEnc(exchange.getRequestURI().getRawQuery());
        state.set(query.get("state"));
        auth.set(query.get("code"));

        /* send */
        {
            exchange.getResponseHeaders().set("Accept-Encoding", "gzip");
            exchange.getResponseHeaders().set("Content-Encoding", "gzip");
            exchange.getResponseHeaders().set("Connection", "keep-alive");
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            try (GZIPOutputStream out = new GZIPOutputStream(exchange.getResponseBody())) {
                out.write(
                        handler.getResponse(
                                query.get("code"),
                                query.get("error"),
                                query.get("message"),
                                query.get("hint")
                        ).getBytes(StandardCharsets.UTF_8)
                );
                out.finish();
                out.flush();
            }
        }

        latch.countDown();
    }

    private Map<String, String> parseWwwFormEnc(final String query) {
        final Map<String, String> out = new HashMap<>();
        final String[] pairs = query.split("&");

        for (final String pair : pairs) {
            if (pair.contains("=")) {
                final String[] kv = pair.split("=");
                out.put(
                        URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        kv.length == 2 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : null
                );
            }
        }
        return out;
    }

    @Override
    public String getAuth() {
        return auth.get();
    }

    @Override
    public String getState() {
        return state.get();
    }
}
