package org.valdi.kurumi.anilist;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.valdi.kurumi.anilist.auth.AniListAuthenticator;
import org.valdi.kurumi.auth.AuthToken;
import org.valdi.kurumi.auth.TokenRetrieveException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.valdi.kurumi.anilist.AniListConstants.REQUEST_URL;

public class AniListApi {
    private final AniListAuthenticator authenticator;

    public AniListApi(final AniListAuthenticator authenticator) {
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

    public List<AlAnime> queryAnimeList(String name) throws URISyntaxException, IOException, InterruptedException {
        // custom executor
        final ExecutorService exec = Executors.newSingleThreadExecutor();
        HttpClient client = HttpClient.newBuilder()
                .executor(exec)
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        JsonObject body = new JsonObject();
        body.addProperty("query", "query ($name: String) { MediaListCollection (userName: $name type: ANIME) { lists { status entries { media { id } status } } } }");
        JsonObject variables = new JsonObject();
        variables.addProperty("name", name);
        body.add("variables", variables);

        // uri
        URI uri = URI.create(REQUEST_URL);
        // Request body from a String
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .setHeader("User-Agent", "Java 11 HttpClient Bot") // add request header
                .setHeader("Content-Type", "application/json")
                .setHeader("Accept", "application/json")
                .setHeader("Authorization", authenticator.getToken().getHeaderAuthorization())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject data = json.getAsJsonObject("data");
        JsonObject media = data.getAsJsonObject("MediaListCollection");
        JsonArray lists = media.getAsJsonArray("lists");

        List<AlAnimeList> list = gson.fromJson(lists, new TypeToken<ArrayList<AlAnimeList>>(){}.getType());
        List<AlAnime> animeList = new ArrayList<>();
        list.stream().map(AlAnimeList::getEntries).forEach(animeList::addAll);
        return animeList;
    }
}
