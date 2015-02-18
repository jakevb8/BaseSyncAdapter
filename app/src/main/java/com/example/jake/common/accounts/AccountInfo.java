package com.example.jake.common.accounts;

import com.example.jake.basesyncadapter.CloudProviders;

public class AccountInfo {
    public CloudProviders Provider;
    public String AccountId;
    public String AccountName;
    public String RefreshToken;
    public String AccessToken;

    public String getAndroidAccountName() {
        return Provider.toString() + ":" + AccountName;
    }
}
