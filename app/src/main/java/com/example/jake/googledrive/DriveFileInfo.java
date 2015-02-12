package com.example.jake.googledrive;

import com.example.jake.common.FileInfo;
import com.example.jake.common.ICloudFile;
import com.example.jake.common.IReadableCloudStorage;
import com.example.jake.common.accounts.AccountInfo;
import com.google.api.services.drive.model.File;

import java.io.InputStream;

public class DriveFileInfo extends FileInfo implements ICloudFile {
    private File _file;
    ;
    private IReadableCloudStorage _cloudStorage;

    public DriveFileInfo(String id, String name, String path, int type, long size, long date,
                         boolean directory) {
        super(id, name, path, type, size, date, directory);
    }

    public File getDriveFile() {
        return _file;
    }

    public void setDriveFile(File file) {
        _file = file;
    }

    @Override
    public IReadableCloudStorage getCloudStorage() {
        return _cloudStorage;
    }

    @Override
    public void setCloudStorage(IReadableCloudStorage cloudStorage) {
        _cloudStorage = cloudStorage;
    }

    @Override
    public String getThumbnailLink(AccountInfo accountInfo) {
        if (_file.getThumbnailLink() != null && super.isPhoto()) {
            return _file.getThumbnailLink();
        }
        return "";
    }

    @Override
    public InputStream getThumbnailInputStream(AccountInfo accountInfo) {
        return null;
    }
}
