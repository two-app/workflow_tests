package com.two.workflow_tests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Tokens {

    private final String refreshToken;
    private final String accessToken;

    @JsonCreator
    public Tokens(@JsonProperty("refreshToken") String refreshToken, @JsonProperty("accessToken") String accessToken) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    @Override
    public String toString() {
        return "{ refreshToken: " + refreshToken + ", accessToken: " + accessToken + " }";
    }

}