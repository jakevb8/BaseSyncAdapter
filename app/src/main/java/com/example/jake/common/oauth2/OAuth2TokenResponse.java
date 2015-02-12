package com.example.jake.common.oauth2;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;

import java.io.IOException;

public class OAuth2TokenResponse extends TokenResponse {

    @Key("id_token")
    private String idToken;

    public final String getIdToken() {
        return this.idToken;
    }

    public OAuth2TokenResponse setIdToken(String idToken) {
        this.idToken = ((String) Preconditions.checkNotNull(idToken));
        return this;
    }

    public GoogleIdToken parseIdToken() throws IOException {
        return GoogleIdToken.parse(getFactory(), getIdToken());
    }
}
