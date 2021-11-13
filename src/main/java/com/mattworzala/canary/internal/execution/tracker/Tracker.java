package com.mattworzala.canary.internal.execution.tracker;

public interface Tracker<T> {

    boolean canTrack(Object thing);

    void track(T t);

    void release();
}
