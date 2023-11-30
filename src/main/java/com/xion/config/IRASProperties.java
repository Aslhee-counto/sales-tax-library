package com.xion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "iras")
public class IRASProperties {

    private boolean corpPass;
    private String authBaseUrl;
    private String noAuthF5Url;
    private String noAuthTransactionsUrl;
    private String authF5Url;
    private String authF5UrlSingPass;
    private String authTransactionsUrl;
    private String authTransactionsUrlSingPass;
    private String callback;
    private String callbackSingPass;
    private String id;
    private String secret;
    private String idSingPass;
    private String secretSingPass;
    private String corpPassAuthUrl;
    private String corpPassTokenUrl;

    public boolean isCorpPass() {
        return corpPass;
    }

    public void setCorpPass(boolean corpPass) {
        this.corpPass = corpPass;
    }

    public String getAuthBaseUrl() {
        return authBaseUrl;
    }

    public void setAuthBaseUrl(String authBaseUrl) {
        this.authBaseUrl = authBaseUrl;
    }

    public String getNoAuthF5Url() {
        return noAuthF5Url;
    }

    public void setNoAuthF5Url(String noAuthF5Url) {
        this.noAuthF5Url = noAuthF5Url;
    }

    public String getNoAuthTransactionsUrl() {
        return noAuthTransactionsUrl;
    }

    public void setNoAuthTransactionsUrl(String noAuthTransactionsUrl) {
        this.noAuthTransactionsUrl = noAuthTransactionsUrl;
    }

    public String getAuthF5Url() {
        return authF5Url;
    }

    public void setAuthF5Url(String authF5Url) {
        this.authF5Url = authF5Url;
    }

    public String getAuthF5UrlSingPass() {
        return authF5UrlSingPass;
    }

    public void setAuthF5UrlSingPass(String authF5UrlSingPass) {
        this.authF5UrlSingPass = authF5UrlSingPass;
    }

    public String getAuthTransactionsUrl() {
        return authTransactionsUrl;
    }

    public void setAuthTransactionsUrl(String authTransactionsUrl) {
        this.authTransactionsUrl = authTransactionsUrl;
    }

    public String getAuthTransactionsUrlSingPass() {
        return authTransactionsUrlSingPass;
    }

    public void setAuthTransactionsUrlSingPass(String authTransactionsUrlSingPass) {
        this.authTransactionsUrlSingPass = authTransactionsUrlSingPass;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIdSingPass() {
        return idSingPass;
    }

    public void setIdSingPass(String idSingPass) {
        this.idSingPass = idSingPass;
    }

    public String getSecretSingPass() {
        return secretSingPass;
    }

    public void setSecretSingPass(String secretSingPass) {
        this.secretSingPass = secretSingPass;
    }

    public String getCallbackSingPass() {
        return callbackSingPass;
    }

    public void setCallbackSingPass(String callbackSingPass) {
        this.callbackSingPass = callbackSingPass;
    }

    public String getCorpPassAuthUrl() {
        return corpPassAuthUrl;
    }

    public void setCorpPassAuthUrl(String corpPassAuthUrl) {
        this.corpPassAuthUrl = corpPassAuthUrl;
    }

    public String getCorpPassTokenUrl() {
        return corpPassTokenUrl;
    }

    public void setCorpPassTokenUrl(String corpPassTokenUrl) {
        this.corpPassTokenUrl = corpPassTokenUrl;
    }
}
