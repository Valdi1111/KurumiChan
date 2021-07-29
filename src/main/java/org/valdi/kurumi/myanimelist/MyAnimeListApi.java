package org.valdi.kurumi.myanimelist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.valdi.kurumi.auth.AuthToken;
import org.valdi.kurumi.auth.TokenRetrieveException;
import org.valdi.kurumi.myanimelist.auth.MyAnimeListAuthenticator;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.valdi.kurumi.myanimelist.MyAnimeListConstants.USER_ANIME_LIST_URL;

public class MyAnimeListApi {
    private final MyAnimeListAuthenticator authenticator;

    public MyAnimeListApi(final MyAnimeListAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void start() throws TokenRetrieveException {
        authenticator.createToken();
    }

    public void start(final AuthToken token) throws TokenRetrieveException {
        authenticator.fromToken(token);
    }

    public void close() {

    }

    public void refreshToken() throws TokenRetrieveException {
        authenticator.refreshToken();
    }

    public AuthToken getToken() {
        return authenticator.getToken();
    }

    public List<MalAnime> queryAnimeList(String name) throws URISyntaxException, IOException, InterruptedException {
        // custom executor
        final ExecutorService exec = Executors.newSingleThreadExecutor();
        HttpClient client = HttpClient.newBuilder()
                .executor(exec)
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // uri
        URI uri = new URIBuilder(String.format(USER_ANIME_LIST_URL, name))
                .addParameter("fields", "list_status")
                .addParameter("limit", "1000")
                .build();
        // Request body from a String
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", authenticator.getToken().getHeaderAuthorization())
                .build();


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        MalAnimeList list = gson.fromJson(response.body(), MalAnimeList.class);
        // TODO query for more than 1000 animes
        return list.getData();
    }
}
