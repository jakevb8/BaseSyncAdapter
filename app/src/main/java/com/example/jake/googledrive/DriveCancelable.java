package com.example.jake.googledrive;

import com.example.jake.common.ICancelable;

public class DriveCancelable implements ICancelable {
    private Thread _thread;

    public DriveCancelable(Thread thread) {
        _thread = thread;
    }

    @Override
    public boolean cancel() {
        _thread.interrupt();
        return true;
    }

}
