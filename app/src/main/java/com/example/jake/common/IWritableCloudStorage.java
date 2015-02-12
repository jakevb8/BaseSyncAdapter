package com.example.jake.common;

import com.example.jake.common.accounts.AccountInfo;

import java.io.InputStream;

public interface IWritableCloudStorage {
    public void delete(AccountInfo accountInfo, FileInfo fileInfo);

    public void uploadFile(AccountInfo accountInfo, InputStream inputStream, String fileName,
                           long fileSizeInBytes, FileInfo uploadDirectory);
}
