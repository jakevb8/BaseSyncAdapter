package com.example.jake.common.oauth2;

import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import java.io.IOException;

public class OAuth2RefreshTokenRequest extends RefreshTokenRequest {
    public OAuth2RefreshTokenRequest(String tokenServerUrl, HttpTransport transport, JsonFactory jsonFactory,
                                     String refreshToken, String clientId, String clientSecret) {
        super(transport, jsonFactory, new GenericUrl(tokenServerUrl), refreshToken);

        setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret));
    }

    public OAuth2TokenResponse execute() throws IOException {
        return (OAuth2TokenResponse) executeUnparsed().parseAs(OAuth2TokenResponse.class);
    }
}
