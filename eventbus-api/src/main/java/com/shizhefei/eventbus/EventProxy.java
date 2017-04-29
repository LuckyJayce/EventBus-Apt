package com.shizhefei.eventbus;

import java.util.Set;

public class EventProxy<IEVENT extends IEvent> implements IEvent {
    protected boolean isPostMainThread;
    protected Set<IEVENT> iEvents;

    void setPostMainThread(boolean postMainThread) {
        this.isPostMainThread = postMainThread;
    }

    public boolean isPostMainThread() {
        return isPostMainThread;
    }

    void setEvents(Set<IEVENT> events) {
        this.iEvents = events;
    }
}
