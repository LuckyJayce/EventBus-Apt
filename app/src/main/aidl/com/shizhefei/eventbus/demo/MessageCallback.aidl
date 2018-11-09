// MessageCallback.aidl
package com.shizhefei.eventbus.demo;

// Declare any non-default types here with import statements

interface MessageCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onSuccess(int code, String resultData);
}
