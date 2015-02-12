package com.example.jake.common.accounts;

import android.accounts.AccountAuthenticatorActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jake.basesyncadapter.CloudProviders;
import com.example.jake.basesyncadapter.R;
import com.example.jake.googledrive.DriveCloudStorage;


public class AddAccountActivity extends AccountAuthenticatorActivity {
    public static final String AUTHTOKEN_TYPE = "com.example.jake";
    public static final String EXTRA_ACCOUNT_ID = "account_id";
    public static final String EXTRA_ACCOUNT_NAME = "account_name";
    public static final String EXTRA_ACCESS_TOKEN = "access_token";
    public static final String EXTRA_REFRESH_TOKEN = "refresh_token";
    public static final String EXTRA_PROVIDER = "provider";

    public static final int AUTHENTICATION_COMPLETED = 10001;
    public static final int AUTHENTICATION_FAILED = 10002;

    private ListView _listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.add_account_choose_provider);

        _listView = new ListView(this);
        _listView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        _listView.setVisibility(View.VISIBLE);

        final AccountPickerListItem[] items = {
                new AccountPickerListItem(getResources().getText(R.string.google_drive_name).toString(),
                        R.drawable.drive_menu_item, CloudProviders.GoogleDrive),
                new AccountPickerListItem("OneDrive", R.drawable.onedrive_menu_item, CloudProviders.OneDrive)};

        ListAdapter adapter = new ArrayAdapter<AccountPickerListItem>(this,
                android.R.layout.select_dialog_item, android.R.id.text1, items) {
            public View getView(int position, View convertView, ViewGroup parent) {
                // User super class to create the View
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v.findViewById(android.R.id.text1);

                // Put the image on the TextView
                tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0);

                // Add margin between image and text (support various screen
                // densities)
                int dp5 = (int) (5 * getApplicationContext().getResources().getDisplayMetrics().density + 0.5f);
                tv.setCompoundDrawablePadding(dp5);

                return v;
            }
        };

        _listView.setAdapter(adapter);
        _listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                AccountPickerListItem accountPickerListItem = items[position];
                switch (accountPickerListItem.provider) {
                    case GoogleDrive:
                        new DriveCloudStorage(AddAccountActivity.this)
                                .addAccount();
                        break;
                }
            }

        });
        setContentView(_listView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                if (requestCode == AUTHENTICATION_COMPLETED) {
                    Toast.makeText(getApplicationContext(), R.string.log_in_successful, Toast.LENGTH_LONG).show();
                    CloudServiceAccountUtils.addAccount(getApplicationContext(), data.getStringExtra(EXTRA_ACCOUNT_ID),
                            data.getStringExtra(EXTRA_ACCOUNT_NAME), data.getStringExtra(EXTRA_ACCESS_TOKEN),
                            data.getStringExtra(EXTRA_REFRESH_TOKEN), (CloudProviders) data.getSerializableExtra(EXTRA_PROVIDER));
                }
                break;
            default:
                break;
        }
        // setResult(resultCode);
        finish();
    }

    private static class AccountPickerListItem {
        public final String text;
        public final int icon;
        public final CloudProviders provider;

        public AccountPickerListItem(String text, Integer icon, CloudProviders provider) {
            this.text = text;
            this.icon = icon;
            this.provider = provider;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
