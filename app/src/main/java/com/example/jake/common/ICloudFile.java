package com.example.jake.common;

import com.example.jake.common.accounts.AccountInfo;

import java.io.InputStream;

public interface ICloudFile {
	public IReadableCloudStorage getCloudStorage();

	public void setCloudStorage(IReadableCloudStorage cloudStorage);

	public String getThumbnailLink(AccountInfo accountInfo);

	public InputStream getThumbnailInputStream(AccountInfo accountInfo);
}
