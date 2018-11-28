package com.job.darasalecturer.service;

import androidx.work.ListenableWorker;

/**
 * Created by Job on Friday : 10/19/2018.
 */
public interface MyResultCallback {
    ListenableWorker.Result onResultCallback(ListenableWorker.Result result);
}
