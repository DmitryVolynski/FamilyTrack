package com.volynski.familytrack.utils;

import timber.log.Timber;

/**
 * Created by DmitryVolynski on 20.10.2017.
 */

public class MyDebugTree extends Timber.DebugTree {
    @Override
    protected String createStackElementTag(StackTraceElement element) {
        return String.format("[L:%s] [M:%s] [C:%s]",
                element.getLineNumber(),
                element.getMethodName(),
                super.createStackElementTag(element));
    }
}
