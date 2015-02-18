package com.example.jake.common.db;

import com.example.jake.basesyncadapter.provider.FileContract;

/**
 * Created by jvanburen on 2/16/2015.
 */
public class Entry {
    public int Id;
    public String EntryId;
    public String EntryName;
    public String ParentId;
    public int FileType = FileContract.FileType.Unknown;
}
