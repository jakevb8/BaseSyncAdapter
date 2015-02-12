package com.example.jake.common;

import android.net.Uri;

import com.example.jake.common.accounts.AccountInfo;

import java.util.ArrayList;

public interface IReadableCloudStorage {
    public String getDisplayName();

    public void addAccount();

    public void initialize(CloudStorageConfiguration configuration);

    public ArrayList<FileInfo> getFiles(AccountInfo accountInfo, FileInfo fileInfo);

    public ArrayList<CloudFileInfo> getFiles(AccountInfo accountInfo, String parentDirectory);

    public void openFile(AccountInfo accountInfo, FileInfo fileInfo);

    public void openFiles(AccountInfo accountInfo, ArrayList<FileInfo> fileInfos, int position,
                          ArrayList<FileInfo> pageSubset, int pageSubsetPosition);

    public CloudFileUrlInfo getUrlInfo(AccountInfo accountInfo, String fileId);

    public void downloadFile(AccountInfo accountInfo, FileInfo fileInfo, String downloadPath);


    public StorageQuotaInfo getStorageQuotaInfo(AccountInfo accountInfo);
}
