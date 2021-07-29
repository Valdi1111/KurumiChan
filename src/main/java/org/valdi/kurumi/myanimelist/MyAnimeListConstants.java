package org.valdi.kurumi.myanimelist;

public class MyAnimeListConstants {
    // required fields
    public static final String AUTH_URL =
            "https://myanimelist.net/v1/oauth2/authorize" +
                    "?response_type=code" +
                    "&client_id=%s" +
                    "&code_challenge=%s" +
                    "&code_challenge_method=plain";

    // optional fields
    public static final String AUTH_STATE = "&state=%s";
    public static final String AUTH_REDIRECT_URI = "&redirect_uri=%s";

    public static final String CONTEXT = "myanimelist";

    public static final String TOKEN_URL = "https://myanimelist.net/v1/oauth2/token";
    public static final String USER_ANIME_LIST_URL = "https://api.myanimelist.net/v2/users/%s/animelist";

    public static final String PROFILE_LINK = "https://myanimelist.net/profile/%s";
    public static final String ANIME_LINK = "https://myanimelist.net/anime/%s";
}
