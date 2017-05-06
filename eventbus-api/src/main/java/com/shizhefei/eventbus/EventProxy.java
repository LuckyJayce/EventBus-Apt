package com.shizhefei.eventbus;

import android.os.Bundle;

import java.util.Set;

public abstract class EventProxy<EVENT extends IEvent> implements IEvent {
    protected boolean isPostMainThread;
    protected Set<EVENT> iEvents;
    protected String processName;

    void setPostMainThread(boolean postMainThread) {
        this.isPostMainThread = postMainThread;
    }

    public boolean isPostMainThread() {
        return isPostMainThread;
    }

    void setEvents(Set<EVENT> events) {
        this.iEvents = events;
    }

    void setProcessName(String processName) {
        this.processName = processName;
    }

    public void onRemoteEvent(Bundle eventRemoteData){}
}
