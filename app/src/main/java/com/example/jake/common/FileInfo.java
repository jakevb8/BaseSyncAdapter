package com.example.jake.common;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * File infomation, about name ,icon, size, folder or not.
 */
public class FileInfo implements Comparable<FileInfo> {

    int type;
    String mFileId;
    String name = null;
    long size = 0;

    String path = null;
    long date = 0;
    boolean directory = false;
    boolean selected = false;
    Drawable dr = null;
    private View view = null;
    private FileInfo mParentFileInfo;
    private String mThumbnailUrl;

    // String date,
    public FileInfo(String fileId, String name, String path, int type, long size, long date, boolean directory) {
        this.mFileId = fileId;
        this.name = name;
        this.size = size;
        this.type = type;
        this.path = path;
        this.date = date;
        this.directory = directory;
    }

    public String getFileId() {
        return mFileId;
    }

    public final FileInfo getParentFileInfo() {
        return mParentFileInfo;
    }

    public void setParentFileInfo(FileInfo parentFileInfo) {
        mParentFileInfo = parentFileInfo;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        mThumbnailUrl = thumbnailUrl;
    }

    public final String name() {
        return this.name;
    }

    public final String path() {
        return this.path;
    }

    public void setPath(String newPath) {
        path = newPath;
    }

    public final void invertSelected() {
        selected = !selected;
    }

    public final boolean selectted() {
        return selected;
    }

    public final void setSelected(boolean s) {
        selected = s;
    }

    public final long size() {
        return this.size;
    }

    public final long date() {
        return this.date;
    }

    public final void setView(View v) {
        view = v;
    }

    public final View getView() {
        return view;
    }

    public final boolean isApk() {
        return type == FileTypes.APK;
    }

    public final boolean isPhoto() {
        return type == FileTypes.PHOTO;
    }

    public final synchronized void setDrawble(Drawable d) {
        dr = d;
    }

    public final synchronized Drawable getDrawable() {
        return dr;
    }

    public final boolean directory() {
        return this.directory;
    }

    public final int type() {
        return type;
    }

    @Override
    public int compareTo(FileInfo another) {
        if (another.directory) {
            if (!directory)
                return 1;
            return this.name.compareTo(another.name);
        }
        if (directory)
            return -1;
        return this.name.compareTo(another.name);
    }

}
