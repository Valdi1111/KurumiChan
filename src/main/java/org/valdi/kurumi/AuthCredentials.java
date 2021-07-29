package org.valdi.kurumi;

public class AuthCredentials {
    private String awClientId;
    private String awApiKey;

    private String discordToken;

    private String malClientId;
    private String malClientSecret;

    private String alClientId;
    private String alClientSecret;

    public String getAwClientId() {
        return awClientId;
    }

    public String getAwApiKey() {
        return awApiKey;
    }

    public String getDiscordToken() {
        return discordToken;
    }

    public String getMalClientId() {
        return malClientId;
    }

    public String getMalClientSecret() {
        return malClientSecret;
    }

    public String getAlClientId() {
        return alClientId;
    }

    public String getAlClientSecret() {
        return alClientSecret;
    }

    public AuthCredentials setAwClientId(String awClientId) {
        this.awClientId = awClientId;
        return this;
    }

    public AuthCredentials setAwApiKey(String awApiKey) {
        this.awApiKey = awApiKey;
        return this;
    }

    public AuthCredentials setDiscordToken(String discordToken) {
        this.discordToken = discordToken;
        return this;
    }

    public AuthCredentials setMalClientId(String malClientId) {
        this.malClientId = malClientId;
        return this;
    }

    public AuthCredentials setMalClientSecret(String malClientSecret) {
        this.malClientSecret = malClientSecret;
        return this;
    }

    public AuthCredentials setAlClientId(String alClientId) {
        this.alClientId = alClientId;
        return this;
    }

    public AuthCredentials setAlClientSecret(String alClientSecret) {
        this.alClientSecret = alClientSecret;
        return this;
    }
}
