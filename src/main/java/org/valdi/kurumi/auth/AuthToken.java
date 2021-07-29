package org.valdi.kurumi.auth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuthToken {
    @Expose @SerializedName("token_type")
    private String tokenType;
    @Expose @SerializedName("expires_in")
    private long expiresIn;
    @Expose @SerializedName("access_token")
    private String accessToken;
    @Expose @SerializedName("refresh_token")
    private String refreshToken;

    public AuthToken() {

    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getHeaderAuthorization() {
        return tokenType + ' ' + accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
