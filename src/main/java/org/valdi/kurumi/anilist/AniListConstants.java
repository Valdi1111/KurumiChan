package org.valdi.kurumi.anilist;

public class AniListConstants {
    // required fields
    public static final String AUTH_URL =
            "https://anilist.co/api/v2/oauth/authorize" +
                    "?client_id=%s" +
                    "&response_type=code";

    // optional fields
    public static final String AUTH_REDIRECT_URI = "&redirect_uri=%s";

    public static final String CONTEXT = "anilist";

    public static final String TOKEN_URL = "https://anilist.co/api/v2/oauth/token";
    public static final String REQUEST_URL = "https://graphql.anilist.co";

    public static final String PROFILE_LINK = "https://anilist.co/user/%s";
    public static final String ANIME_LINK = "https://anilist.co/anime/%s";
}
