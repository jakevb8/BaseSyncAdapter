package com.example.jake.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class FileUtils {

    public static String getExtension(String fileName) {
        String result = "";

        String[] fileParts = fileName.split("\\.");
        if (fileParts.length > 0) {
            result = fileParts[fileParts.length - 1];
        }

        return result;
    }

    public static final int getIconType(String extension) {
        return getIconType(extension, null);
    }

    public static final int getIconType(String name, File file) {
        if (file != null && file.isDirectory())
            return FileTypes.DIRECTORY;
        if (name == null) {
            return FileTypes.UNKNOWN;
        }
        name = name.toLowerCase(Locale.ENGLISH);
        if (name.equals("txt") || name.equals("doc") || name.equals("pdf")) {
            return FileTypes.TXT;
        } else if (name.equals("html") || name.equals("htm") || name.equals("chm") || name.equals("xml")) {
            return FileTypes.HTM;
        } else if (name.equals("jpeg") || name.equals("jpg") || name.equals("bmp") || name.equals("gif")
                || name.equals("png") || name.equals("tif")) {
            return FileTypes.PHOTO;
        } else if (name.equals("rmvb") || name.equals("rmb") || name.equals("avi") || name.equals("wmv")
                || name.equals("mp4") || name.equals("mkv") || name.equals("ts") || name.equals("mpg")
                || name.equals("3gp") || name.equals("flv") || name.equals("mov") || name.equals("mpeg")
                || name.equals("divx")) {
            return FileTypes.MOVIE;
        } else if (name.equals("mp3") || name.equals("wav") || name.equals("wma") || name.equals("midi")
                || name.equals("mid")) {
            return FileTypes.MUSIC;
        } else if (name.equals("apk")) {
            return FileTypes.APK;
        } else if (name.equals("zip") || name.equals("tar") || name.equals("bar") || name.equals("bz2")
                || name.equals("bz") || name.equals("gz") || name.equals("rar")) {
            return FileTypes.ZIP;
        }
        return FileTypes.UNKNOWN;
    }

    public static final int getType(String fileName) {
        if (fileName == null) {
            return FileTypes.UNKNOWN;
        }
        fileName = fileName.toLowerCase(Locale.ENGLISH);
        if (fileName.endsWith(".txt") || fileName.endsWith(".doc") || fileName.endsWith(".pdf")) {
            return FileTypes.TXT;
        } else if (fileName.endsWith(".html") || fileName.endsWith(".htm") || fileName.endsWith(".chm")
                || fileName.endsWith(".xml")) {
            return FileTypes.HTM;
        } else if (fileName.endsWith(".jpeg") || fileName.endsWith(".jpg") || fileName.endsWith(".bmp")
                || fileName.endsWith(".gif") || fileName.endsWith(".png") || fileName.endsWith(".tif")) {
            return FileTypes.PHOTO;
        } else if (fileName.endsWith(".rmvb") || fileName.endsWith(".rmb") || fileName.endsWith(".avi")
                || fileName.endsWith(".wmv") || fileName.endsWith(".mp4") || fileName.endsWith(".mkv")
                || fileName.endsWith(".ts") || fileName.endsWith(".mpg") || fileName.endsWith(".3gp")
                || fileName.endsWith(".flv") || fileName.endsWith(".mov") || fileName.endsWith(".mpeg")
                || fileName.endsWith(".divx")) {
            return FileTypes.MOVIE;
        } else if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".wma")
                || fileName.endsWith(".midi") || fileName.endsWith(".mid")) {
            return FileTypes.MUSIC;
        } else if (fileName.endsWith(".apk")) {
            return FileTypes.APK;
        } else if (fileName.endsWith(".zip") || fileName.endsWith(".tar") || fileName.endsWith(".bar")
                || fileName.endsWith(".bz2") || fileName.endsWith(".bz") || fileName.endsWith(".gz")
                || fileName.endsWith(".rar")) {
            return FileTypes.ZIP;
        }
        return FileTypes.UNKNOWN;
    }

    public static String getOpenType(String fileName) {
        String type = "";
        switch (getType(fileName)) {
            case FileTypes.MUSIC:
                type = "audio/*";
                break;
            case FileTypes.TXT:
                type = "text/*";
                break;
            case FileTypes.PHOTO:
                type = "image/*";
                break;
            case FileTypes.MOVIE:
                type = "video/*";
                break;
            case FileTypes.APK:
                type = "application/vnd.android.package-archive";
                break;
        }
        return type;
    }

    public static void openVideoFile(String source, String name, Context context) {
        openFile(source, name, context);
    }

    public static void openAudioFile(String source, String name, Context context) {
        openFile(source, name, context);
    }

    public static void openFile(String source, String name, Context context) {
        openFile(source, name, context, null);
    }

    public static void openFile(String source, String name, Context context, HashMap<String, String> headers) {
        openFile(source, name, context, headers, false);
    }

    public static void openFile(String source, String name, Context context, HashMap<String, String> headers,
                                boolean useHisenseMediaPlayer) {
        ArrayList<String> play_list = new ArrayList<String>(); // file
        // name
        ArrayList<String> url_list = new ArrayList<String>(); // http
        // url

        url_list.add(source);

        play_list.add(name);

        openFiles(url_list, play_list, 0, context, headers, useHisenseMediaPlayer);
    }

    public static void openFiles(ArrayList<String> url_list, ArrayList<String> nameList, int position,
                                 Context context, HashMap<String, String> headers, boolean useHisenseMediaPlayer) {
        // boolean hasHisenseMediaPlayer = true;
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle bl = new Bundle();

        if (headers != null) {
            // bl.putInt("play_source", 5);
            ArrayList<String> headerNames = new ArrayList<String>();
            ArrayList<String> headerValues = new ArrayList<String>();
            for (String key : headers.keySet()) {
                headerNames.add(key);
                headerValues.add(headers.get(key));
            }
            bl.putStringArrayList("header_names", headerNames);
            bl.putStringArrayList("header_values", headerValues);
        }

        int fileType = FileTypes.UNKNOWN;
        if (url_list.size() > 0) {
            fileType = getType(url_list.get(position));

            if (fileType == FileTypes.UNKNOWN && nameList.size() > 0) {
                fileType = getType(nameList.get(position));
            }
        }
        if (fileType == FileTypes.MOVIE) {
            // if (!useHisenseMediaPlayer) {
            // intent = new Intent(context, MediaPlayerActivity.class);
            // intent.putExtras(bl);
            // }
            // else {
            intent.setClassName("com.hisense.mmp.video", "com.hisense.mmp.video.VideoPlayerActivity");
            // }
        } else if (fileType == FileTypes.MUSIC) {
            // if (!useHisenseMediaPlayer) {
            // intent = new Intent(context, MediaPlayerActivity.class);
            // intent.putExtras(bl);
            // }
            // else {
            intent.setClassName("com.hisense.mmp.audio", "com.hisense.mmp.audio.MusicPlayerActivity");
            // }
        } else if (fileType == FileTypes.PHOTO) {
            intent.setClassName("com.hisense.mmp.photo", "com.hisense.mmp.photo.PhotoPlayerActivity");
        }

        //Filter list by the type of file indicated by 'position'
        int type = FileTypes.UNKNOWN;
        int size = url_list.size();
        for (int ix = size - 1; ix >= 0; ix--) {
            type = getType(url_list.get(ix));
            if ((type == FileTypes.UNKNOWN) && nameList.size() > ix) {
                type = getType(nameList.get(ix));
            }
            if (type != fileType) {
                url_list.remove(ix);
                nameList.remove(ix);
                if (ix < position) {
                    position--;
                }
            }
        }

        bl.putStringArrayList("play_list", nameList);
        bl.putInt("play_pos", position); // there is only one file,
        // position is 0.
        bl.putStringArrayList("url_list", url_list);

        bl.putInt("play_source", 5);// 1 is for local, 4 is for http
        bl.putString("external_way", "url_way"); // dlna video

        intent.putExtra("Headers", headers);
        intent.putExtras(bl);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            // hasHisenseMediaPlayer = false;
        }

        // if (!hasHisenseMediaPlayer) {
        // // if (source.toLowerCase(Locale.ENGLISH).startsWith("http")) {
        // // Uri uri = Uri.parse(source);
        // // intent = new Intent(Intent.ACTION_VIEW, uri);
        // // }
        // // else {
        // String type = getOpenType(source);
        // if (type == "") {
        // type = getOpenType(name);
        // }
        // Intent mediaTypeIntent = new Intent(Intent.ACTION_GET_CONTENT);
        // mediaTypeIntent.setType(type);
        // PackageManager packageManager = context.getPackageManager();
        // List<ResolveInfo> list =
        // packageManager.queryIntentActivities(mediaTypeIntent, 0);
        // if (list.size() > 0) {
        // Uri uri = null;
        // if (source.toLowerCase(Locale.ENGLISH).startsWith("http")) {
        // uri = Uri.parse(source);
        // }
        // else {
        // uri = Uri.parse("file://" + source);
        // }
        //
        // intent = new Intent(Intent.ACTION_VIEW);
        // intent.setDataAndType(uri, type);
        // }
        // // }
        //
        // context.startActivity(intent);
        // }
    }
}
