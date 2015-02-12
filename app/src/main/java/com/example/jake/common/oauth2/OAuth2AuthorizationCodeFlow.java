package com.example.jake.common.oauth2;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OAuth2AuthorizationCodeFlow extends AuthorizationCodeFlow {
    private final String approvalPrompt;
    private final String accessType;
    private final String clientSecret;

    public OAuth2AuthorizationCodeFlow(String tokenServerUrl, String authServerUrl, HttpTransport transport,
                                       JsonFactory jsonFactory, String clientId, String clientSecret, List<String> scopes) {
        this(new Builder(tokenServerUrl, authServerUrl, transport, jsonFactory, clientId, clientSecret,
                scopes));
    }

    protected OAuth2AuthorizationCodeFlow(Builder builder) {
        super(builder);
        this.accessType = builder.accessType;
        this.approvalPrompt = builder.approvalPrompt;
        this.clientSecret = builder.getClientSecret();
    }

    public AuthorizationCodeTokenRequest newTokenRequest(String authorizationCode) {
        Collection scopes = null;
        if (getScopes() != null && getScopes().size() > 0) {
            scopes = getScopes();
        }
        return new OAuth2AuthorizationCodeTokenRequest(getTransport(), getJsonFactory(),
                getTokenServerEncodedUrl(), getClientId(), clientSecret, authorizationCode, "")
                .setClientAuthentication(getClientAuthentication())
                .setRequestInitializer(getRequestInitializer()).setScopes(scopes);
    }

    public OAuth2AuthorizationCodeRequestUrl newAuthorizationUrl() {
        ArrayList<String> scopes = null;
        if (getScopes() != null && getScopes().size() > 0) {
            scopes = new ArrayList<String>(getScopes());
        }
        return new OAuth2AuthorizationCodeRequestUrl(getAuthorizationServerEncodedUrl(), getClientId(), "",
                scopes).setAccessType(this.accessType).setApprovalPrompt(this.approvalPrompt);
    }

    public final String getApprovalPrompt() {
        return this.approvalPrompt;
    }

    public final String getAccessType() {
        return this.accessType;
    }

    public static class Builder extends AuthorizationCodeFlow.Builder {
        String approvalPrompt;
        String accessType;
        String clientSecret;

        public Builder(String tokenServerUrl, String authServerUrl, HttpTransport transport,
                       JsonFactory jsonFactory, String clientId, String clientSecret, List<String> scopes) {
            super(BearerToken.queryParameterAccessMethod(), transport, jsonFactory, new GenericUrl(
                            tokenServerUrl), new ClientParametersAuthentication(clientId, clientSecret), clientId,
                    authServerUrl);
            if (scopes != null && scopes.size() > 0) {
                setScopes(Preconditions.checkNotNull(scopes));
            }
            this.clientSecret = clientSecret;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public OAuth2AuthorizationCodeFlow build() {
            return new OAuth2AuthorizationCodeFlow(this);
        }

        public Builder setApprovalPrompt(String approvalPrompt) {
            this.approvalPrompt = approvalPrompt;
            return this;
        }

        public final String getApprovalPrompt() {
            return this.approvalPrompt;
        }

        public Builder setAccessType(String accessType) {
            this.accessType = accessType;
            return this;
        }

        public final String getAccessType() {
            return this.accessType;
        }
    }
}
