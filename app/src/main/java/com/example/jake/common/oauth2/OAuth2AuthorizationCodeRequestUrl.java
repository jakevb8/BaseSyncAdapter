package com.example.jake.common.oauth2;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.util.Key;

import java.util.List;

public class OAuth2AuthorizationCodeRequestUrl extends AuthorizationCodeRequestUrl {
    @Key("approval_prompt")
    private String approvalPrompt;

    @Key("access_type")
    private String accessType;

    public OAuth2AuthorizationCodeRequestUrl(String authorizationServerEncodedUrl, String clientId,
                                             String redirectUri, List<String> scopes) {
        super(authorizationServerEncodedUrl, clientId);
        setRedirectUri(redirectUri);
        if (scopes != null && scopes.size() > 0) {
            setScopes(scopes);
        }
    }

    public final String getApprovalPrompt() {
        return this.approvalPrompt;
    }

    public OAuth2AuthorizationCodeRequestUrl setApprovalPrompt(String approvalPrompt) {
        this.approvalPrompt = approvalPrompt;
        return this;
    }

    public final String getAccessType() {
        return this.accessType;
    }

    public OAuth2AuthorizationCodeRequestUrl setAccessType(String accessType) {
        this.accessType = accessType;
        return this;
    }

    public OAuth2AuthorizationCodeRequestUrl setRedirectUri(String redirectUri) {
        if (redirectUri != null && redirectUri.length() > 0) {
            return (OAuth2AuthorizationCodeRequestUrl) super.setRedirectUri(redirectUri);
        } else {
            return this;
        }
    }
}
