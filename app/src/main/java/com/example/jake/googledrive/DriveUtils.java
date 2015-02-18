package com.example.jake.googledrive;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.example.jake.basesyncadapter.CloudProviders;
import com.example.jake.common.CloudFileUrlInfo;
import com.example.jake.common.CloudUtils;
import com.example.jake.common.FileInfo;
import com.example.jake.common.FileTypes;
import com.example.jake.common.FileUtils;
import com.example.jake.common.IWritableCloudStorage;
import com.example.jake.common.accounts.AccountInfo;
import com.example.jake.common.oauth2.OAuth2Utils;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Get;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class DriveUtils {
    public static String DrivePrefs = "drive_prefs";
    public static String Auth_Code = "auth_code";
    public static String Access_Token = "access_token";
    public static String Refresh_Token = "refresh_token";
    public static String Access_Expiration = "access_expiration";
    public static String CLIENT_ID = "826047193289-kqvqihd6ku61uq8n9j56t23m68vneurs.apps.googleusercontent.com";
    public static String CLIENT_SECRET = "_asO5seut0I3O7-cFt88d21T";
    public static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

    public static ArrayList<FileInfo> getFiles(Context context, AccountInfo accountInfo, final String folderId,
                                               final FileInfo parentFileInfo, String path) {
        ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();
        try {
            Drive driveService = getDriveService(context, accountInfo);
            if (driveService == null) {
                return null;
            }

            FileList fileList = driveService.files().list()
                    .setQ("'" + folderId + "' in parents and trashed=false").execute();

            for (File file : fileList.getItems()) {
                if (file.getMimeType().equalsIgnoreCase("application/vnd.google-apps.folder")) {
                    DriveDirectoryInfo directoryInfo = new DriveDirectoryInfo(file.getId(), file.getTitle(),
                            path, FileTypes.DIRECTORY, 0, file.getCreatedDate().getValue(), true);
                    directoryInfo.setParentFileInfo(parentFileInfo);
                    directoryInfo.setDriveFile(file);
                    fileInfos.add(directoryInfo);
                } else {
                    DriveFileInfo fileInfo = new DriveFileInfo(file.getId(), file.getTitle(), path,
                            FileUtils.getIconType(file.getFileExtension()), file.getQuotaBytesUsed(), file
                            .getCreatedDate().getValue(), false);
                    fileInfo.setParentFileInfo(parentFileInfo);
                    fileInfo.setDriveFile(file);
                    fileInfos.add(fileInfo);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileInfos;
    }

    public static About getAboutInfo(Context context, AccountInfo accountInfo) {
        About about = null;
        try {
            Drive driveService = getDriveService(context, accountInfo);
            if (driveService == null) {
                return null;
            }
            about = driveService.about().get().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return about;
    }

    public static void deleteFile(final Context context, final AccountInfo accountInfo, final File file) {
        // new Thread(new Runnable() {
        //
        // @Override
        // public void run() {
        // try {
        // getDriveService(accountInfo,
        // cloudBrowser).files().delete(file.getId()).execute();
        // cloudBrowser.getActivity().runOnUiThread(new Runnable() {
        // @Override
        // public void run() {
        // Toast.makeText(cloudBrowser.getContext(), cloudMessages.file_deleted,
        // Toast.LENGTH_SHORT).show();
        // }
        // });
        // }
        // catch (IOException e) {
        // e.printStackTrace();
        // }
        // }
        // }).start();
    }

    public static void downloadFile(Context context, final AccountInfo accountInfo, final File file,
                                    java.io.File downloadedFile) {
        downloadFile(context, accountInfo, file, downloadedFile, false);
    }

    public static void openFile(final Context context, final AccountInfo accountInfo, final File file) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Download file");
        builder.setMessage("Content does not allow streaming.  Would you like to download a temporary file?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                java.io.File tempFile = null;
                try {
                    tempFile = new java.io.File(CloudUtils.getAppTempDownloadedDirectory(), UUID.randomUUID()
                            .toString() + file.getTitle().substring(file.getTitle().indexOf(".")));
                    tempFile.createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                downloadFile(context, accountInfo, file, tempFile, true);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    public static String getThumbnailLink(Context context, final AccountInfo accountInfo, final File file) {
        try {
            Drive service = getDriveService(context, accountInfo);
            Get request = service.files().get(file.getId());
            File currentFile = request.execute();
            if (currentFile != null) {
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void streamFile(final Context context, final AccountInfo accountInfo, final File file) {
        if (file.getDownloadUrl() == null || file.getDownloadUrl().length() == 0) {
            Toast.makeText(context, "File is not available for download.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                CloudFileUrlInfo urlInfo = getUrlInfo(context, accountInfo, file.getId());
                if (urlInfo.Url != null) {
                    FileUtils.openFile(urlInfo.Url, file.getTitle(), context,
                            urlInfo.Headers, true);
                } else {
                    //Error
                }
            }
        });
        thread.start();
    }

    public static CloudFileUrlInfo getUrlInfo(Context context, AccountInfo accountInfo, String fileId) {
        CloudFileUrlInfo result = new CloudFileUrlInfo();
        result.ItemId = fileId;

        Drive service = getDriveService(context, accountInfo);
        try {
            Get request = service.files().get(fileId);
            File file = request.execute();
            for (String key : request.getRequestHeaders().keySet()) {
                result.Headers.put(key, request.getRequestHeaders().get(key).toString());
            }
            result.Headers.put("Authorization", "Bearer " + accountInfo.AccessToken);
            result.Url = file.getDownloadUrl();
            result.Title = file.getTitle();
            result.Size = file.getQuotaBytesUsed();
            result.DateLastModified = file.getModifiedDate().getValue();
            result.Type = 1;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void streamFiles(final Context context, final AccountInfo accountInfo, final ArrayList<FileInfo> fileInfos,
                                   int position) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Generating playlist...");
        progressDialog.show();

        for (int index = 0; index < fileInfos.size(); index++) {
            FileInfo fileInfo = fileInfos.get(index);
            File file = ((DriveFileInfo) fileInfo).getDriveFile();
            if (file.getDownloadUrl() == null || file.getDownloadUrl().length() == 0) {
                fileInfos.remove(index);
                position--;
            }
        }

        final int currentPosition = position;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Drive service = getDriveService(context, accountInfo);
                    ArrayList<String> urlList = new ArrayList<String>();
                    ArrayList<String> namesList = new ArrayList<String>();
                    HashMap<String, String> headers = null;
                    for (FileInfo fileInfo : fileInfos) {
                        File file = ((DriveFileInfo) fileInfo).getDriveFile();
                        if (headers == null) {
                            Get request = service.files().get(file.getId());

                            headers = new HashMap<String, String>();
                            for (String key : request.getRequestHeaders().keySet()) {
                                headers.put(key, request.getRequestHeaders().get(key).toString());
                            }
                        }
                        urlList.add(file.getDownloadUrl());
                        namesList.add(file.getTitle());
                    }
                    progressDialog.dismiss();
                    if (headers != null) {
                        headers.put("Authorization", "Bearer " + accountInfo.AccessToken);

                        FileUtils.openFiles(urlList, namesList, currentPosition, context,
                                headers, true);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private static void downloadFile(Context context, AccountInfo accountInfo, File file, java.io.File downloadedFile,
                                     boolean openFile) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(downloadedFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (outputStream == null) {
            return;
        }
        getFile(accountInfo, file, outputStream, openFile, downloadedFile, null, null);
    }

    private static void getFile(final AccountInfo accountInfo, final File file,
                                final OutputStream outputStream, final boolean openFile, final java.io.File downloadedFile,
                                final IWritableCloudStorage destinationStorage, final FileInfo destinationFolder) {
        // if (file.getDownloadUrl() == null && file.getDownloadUrl().length()
        // == 0) {
        // Toast.makeText(cloudBrowser.getContext(),
        // cloudMessages.file_not_available_for_download,
        // Toast.LENGTH_LONG).show();
        // return;
        // }
        // final CancelTransferDialog progressDialog = new
        // CancelTransferDialog(cloudBrowser.getContext());
        // progressDialog.setFileSize(file.getFileSize());
        // Thread thread = new Thread(new Runnable() {
        // @Override
        // public void run() {
        // try {
        // Drive service = getDriveService(accountInfo, cloudBrowser);
        // Get request = service.files().get(file.getId());
        //
        // MediaHttpDownloader downloader = new
        // MediaHttpDownloader(service.getRequestFactory()
        // .getTransport(), service.getRequestFactory().getInitializer());
        //
        // downloader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);
        // downloader.setProgressListener(new
        // MediaHttpDownloaderProgressListener() {
        //
        // @Override
        // public void progressChanged(final MediaHttpDownloader downloader)
        // throws IOException {
        // cloudBrowser.getActivity().runOnUiThread(new Runnable() {
        // @Override
        // public void run() {
        // switch (downloader.getDownloadState()) {
        // case MEDIA_IN_PROGRESS:
        // progressDialog.updateProgress(downloader.getNumBytesDownloaded());
        // break;
        // case MEDIA_COMPLETE:
        // progressDialog.dismiss();
        // if (openFile && downloadedFile.exists()) {
        // FileUtils.openFile(downloadedFile.getPath(),
        // downloadedFile.getName(), cloudBrowser.getContext());
        // }
        // else if (destinationStorage != null) {
        // // ByteArrayInputStream inputStream
        // // = new ByteArrayInputStream(
        // // ((ByteArrayOutputStream)
        // // outputStream).toByteArray());
        // // try {
        // // outputStream.close();
        // // }
        // // catch (IOException e) {
        // // e.printStackTrace();
        // // }
        // // destinationStorage.uploadFile(inputStream,
        // // file.getTitle(),
        // // file.getFileSize(),
        // // destinationFolder);
        // }
        // else {
        // Toast.makeText(cloudBrowser.getContext(),
        // cloudMessages.file_download_finished, Toast.LENGTH_SHORT)
        // .show();
        // }
        // break;
        // case NOT_STARTED:
        // break;
        // }
        // }
        // });
        // }
        // });
        //
        // downloader.download(new GenericUrl(file.getDownloadUrl()),
        // request.getRequestHeaders(),
        // new BufferedOutputStream(outputStream));
        // }
        // catch (IOException e) {
        // e.printStackTrace();
        // }
        // }
        // });
        // progressDialog.setCancelableObject(new DriveCancelable(thread));
        // progressDialog.show();
        // thread.start();
    }

    public static void uploadFile(final AccountInfo accountInfo, final String folderId,
                                  final String fileName, final InputStream inputStream, final String mimeType,
                                  final long fileSizeInBytes) {
        // final CancelTransferDialog progressDialog = new
        // CancelTransferDialog(cloudBrowser.getContext());
        // progressDialog.setFileSize(fileSizeInBytes);
        // Thread thread = new Thread(new Runnable() {
        // @Override
        // public void run() {
        // ParentReference parentReference = new ParentReference();
        // parentReference.setId(folderId);
        // try {
        // File file = new File();
        // file.setTitle(fileName);
        // file.setParents(Arrays.asList(new
        // ParentReference().setId(folderId)));
        //
        // InputStreamContent content = new InputStreamContent(mimeType, new
        // BufferedInputStream(
        // inputStream));
        // content.setLength(fileSizeInBytes);
        // Insert request = getDriveService(accountInfo,
        // cloudBrowser).files().insert(file, content);
        //
        // request.getMediaHttpUploader().setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);
        //
        // request.getMediaHttpUploader().setProgressListener(
        // new MediaHttpUploaderProgressListener() {
        //
        // @Override
        // public void progressChanged(final MediaHttpUploader uploader)
        // throws IOException {
        // cloudBrowser.getActivity().runOnUiThread(new Runnable() {
        // @Override
        // public void run() {
        // switch (uploader.getUploadState()) {
        // case MEDIA_IN_PROGRESS:
        // progressDialog.updateProgress(uploader.getNumBytesUploaded());
        // break;
        // case MEDIA_COMPLETE:
        // progressDialog.dismiss();
        // Toast.makeText(cloudBrowser.getContext(),
        // cloudMessages.file_uploaded_successfully,
        // Toast.LENGTH_LONG).show();
        // break;
        // case INITIATION_STARTED:
        // case INITIATION_COMPLETE:
        // case NOT_STARTED:
        // break;
        // }
        // }
        // });
        // }
        // });
        //
        // request.execute();
        // }
        // catch (IOException e) {
        // e.printStackTrace();
        // }
        // }
        // });
        // progressDialog.setCancelableObject(new DriveCancelable(thread));
        // progressDialog.show();
        // thread.start();
    }

    public static DriveUserInfo getUserInfo(String accessToken) {
        HttpGet get = new HttpGet("https://www.googleapis.com/oauth2/v3/userinfo");
        get.addHeader("Authorization", "Bearer " + accessToken);
        try {
            HttpResponse response = CloudUtils.getHttpClient().execute(get);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()));

            String body = "";
            String currentLine = null;
            while ((currentLine = reader.readLine()) != null) {
                body += currentLine;
            }
            return new Gson().fromJson(body, DriveUserInfo.class);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean accessTokenExpired(AccountInfo accountInfo) {
        HttpGet get = new HttpGet("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token="
                + accountInfo.AccessToken);
        try {
            HttpResponse response = CloudUtils.getHttpClient().execute(get);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()));

            String body = "";
            String currentLine = null;
            while ((currentLine = reader.readLine()) != null) {
                body += currentLine;
            }
            // Log.d("DriveUtils.accessTokenExpired", body);
            if (response.getStatusLine().getStatusCode() >= 400) {
                return true;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    public static void addAccount(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OAuth2Utils.authenticate(context, new Intent(context,
                                DriveAuthenticationActivity.class), "https://accounts.google.com/o/oauth2/token",
                        "https://accounts.google.com/o/oauth2/auth",
                        CloudProviders.GoogleDrive, CLIENT_ID, CLIENT_SECRET, Arrays.asList(
                                DriveScopes.DRIVE, "https://www.googleapis.com/auth/userinfo#email"),
                        REDIRECT_URI);
            }
        }).start();
    }

    private static Drive getDriveService(Context context, AccountInfo accountInfo) {
        if (accessTokenExpired(accountInfo)) {
            accountInfo = OAuth2Utils.refreshAccessToken(context, accountInfo,
                    "https://accounts.google.com/o/oauth2/token", CLIENT_ID, CLIENT_SECRET);
        }

        GoogleCredential credential = new GoogleCredential().setAccessToken(accountInfo.AccessToken);

        return new Drive.Builder(new NetHttpTransport(), new GsonFactory(), credential).build();
    }
}
