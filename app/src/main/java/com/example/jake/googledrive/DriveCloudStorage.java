package com.example.jake.googledrive;

import android.content.Context;
import android.webkit.MimeTypeMap;

import com.example.jake.common.CloudFileInfo;
import com.example.jake.common.CloudFileUrlInfo;
import com.example.jake.common.CloudStorageConfiguration;
import com.example.jake.common.FileInfo;
import com.example.jake.common.IReadableCloudStorage;
import com.example.jake.common.IWritableCloudStorage;
import com.example.jake.common.StorageQuotaInfo;
import com.example.jake.common.accounts.AccountInfo;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;

import java.io.InputStream;
import java.util.ArrayList;

public class DriveCloudStorage implements IReadableCloudStorage, IWritableCloudStorage {
    private Context _context;

    public DriveCloudStorage(Context context) {
        _context = context;
    }

    @Override
    public ArrayList<FileInfo> getFiles(AccountInfo accountInfo, FileInfo fileInfo) {
        if (fileInfo != null && fileInfo instanceof DriveDirectoryInfo) {
            DriveDirectoryInfo directoryInfo = (DriveDirectoryInfo) fileInfo;
            return DriveUtils.getFiles(_context, accountInfo, directoryInfo.getDriveFile().getId(), null,
                    fileInfo.path() + fileInfo.name() + "\\");
        } else {
            return DriveUtils.getFiles(_context, accountInfo, "root", null, "\\");
        }
    }

    @Override
    public ArrayList<CloudFileInfo> getFiles(AccountInfo accountInfo, String parentDirectory) {
        ArrayList<CloudFileInfo> results = new ArrayList<>();
        ArrayList<FileInfo> fileInfos;
        if (parentDirectory != null && parentDirectory.trim().length() > 0) {
            fileInfos = DriveUtils.getFiles(_context, accountInfo, parentDirectory, null, "");
        } else {
            fileInfos = DriveUtils.getFiles(_context, accountInfo, "root", null, "");
        }
        for (FileInfo fileInfo : fileInfos) {
            CloudFileInfo cloudFileInfo = new CloudFileInfo();
            if (fileInfo instanceof DriveFileInfo) {
                cloudFileInfo.ItemId = ((DriveFileInfo) fileInfo).getDriveFile().getId();
            } else {
                cloudFileInfo.ItemId = ((DriveDirectoryInfo) fileInfo).getDriveFile().getId();
            }
            cloudFileInfo.Title = fileInfo.name();
            if (fileInfo.directory()) {
                cloudFileInfo.Type = 2;
            } else {
                cloudFileInfo.Type = 1;
            }
            cloudFileInfo.Size = fileInfo.size();
            cloudFileInfo.DateLastModified = fileInfo.date();

            results.add(cloudFileInfo);
        }
        return results;
    }

    @Override
    public void openFile(AccountInfo accountInfo, FileInfo fileInfo) {
        if (fileInfo instanceof DriveFileInfo) {
            File driveFile = ((DriveFileInfo) fileInfo).getDriveFile();
            DriveUtils.streamFile(_context, accountInfo, driveFile);
        }
    }

    @Override
    public CloudFileUrlInfo getUrlInfo(AccountInfo accountInfo, String fileId) {
        return DriveUtils.getUrlInfo(_context, accountInfo, fileId);
    }

    @Override
    public void delete(AccountInfo accountInfo, FileInfo fileInfo) {
        if (fileInfo instanceof DriveFileInfo) {
            DriveFileInfo driveFieInfo = (DriveFileInfo) fileInfo;
            DriveUtils.deleteFile(_context, accountInfo, driveFieInfo.getDriveFile());
        }
    }

    @Override
    public void downloadFile(AccountInfo accountInfo, FileInfo fileInfo, String downloadPath) {
        if (fileInfo instanceof DriveFileInfo) {
            DriveFileInfo driveFileInfo = (DriveFileInfo) fileInfo;
            DriveUtils.downloadFile(_context, accountInfo, driveFileInfo.getDriveFile(), new java.io.File(downloadPath,
                    driveFileInfo.getDriveFile().getTitle()));
        }
    }

    @Override
    public void uploadFile(AccountInfo accountInfo, InputStream inputStream, String fileName,
                           long fileSizeInBytes, FileInfo uploadDirectory) {
        if (uploadDirectory instanceof DriveDirectoryInfo) {
            String extension = fileName.substring(fileName.indexOf(".") + 1);
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            DriveDirectoryInfo driveDirectoryInfo = (DriveDirectoryInfo) uploadDirectory;
            DriveUtils.uploadFile(accountInfo, driveDirectoryInfo.getDriveFile().getId(), fileName,
                    inputStream, mimeType, fileSizeInBytes);
        }

    }

    @Override
    public void addAccount() {
        DriveUtils.addAccount(_context);
    }

    @Override
    public void initialize(CloudStorageConfiguration configuration) {
    }

    @Override
    public void openFiles(AccountInfo accountInfo, ArrayList<FileInfo> fileInfos, int position,
                          ArrayList<FileInfo> pageSubset, int pageSubsetPosition) {
        if (fileInfos.size() > 0) {
            if (fileInfos.size() > 1) {
                DriveUtils.streamFiles(_context, accountInfo, fileInfos, position);
            } else {
                if (fileInfos.get(0) instanceof DriveFileInfo) {
                    File driveFile = ((DriveFileInfo) fileInfos.get(0)).getDriveFile();
                    DriveUtils.streamFile(_context, accountInfo, driveFile);
                }
            }
        }
    }

    @Override
    public StorageQuotaInfo getStorageQuotaInfo(AccountInfo accountInfo) {
        About driveAbout = DriveUtils.getAboutInfo(_context, accountInfo);
        StorageQuotaInfo storageQuotaInfo = new StorageQuotaInfo();
        if (driveAbout != null) {
            storageQuotaInfo.totalQuota = driveAbout.getQuotaBytesTotal();
            storageQuotaInfo.usedQuota = driveAbout.getQuotaBytesUsed();
            storageQuotaInfo.availableQuota = storageQuotaInfo.totalQuota - storageQuotaInfo.usedQuota;
        }
        return storageQuotaInfo;
    }

    @Override
    public String getDisplayName() {
        return "Google Drive�";
    }
}
