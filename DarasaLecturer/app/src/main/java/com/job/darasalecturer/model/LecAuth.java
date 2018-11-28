package com.job.darasalecturer.model;

import android.support.annotation.Keep;

/**
 * Created by Job on Wednesday : 8/8/2018.
 */

@Keep
public class LecAuth {
    private String localpasscode;

    public LecAuth() {
    }

    public LecAuth(String localpasscode) {
        this.localpasscode = localpasscode;
    }

    public String getLocalpasscode() {
        return localpasscode;
    }

    public void setLocalpasscode(String localpasscode) {
        this.localpasscode = localpasscode;
    }

    @Override
    public String toString() {
        return "LecAuth{" +
                "localpasscode='" + localpasscode + '\'' +
                '}';
    }
}
