package com.shizhefei.eventbus;

import java.util.Set;

public class EventProxy<EVENT extends IEvent> implements IEvent {
    protected boolean isPostMainThread;
    protected Set<EVENT> iEvents;

    void setPostMainThread(boolean postMainThread) {
        this.isPostMainThread = postMainThread;
    }

    public boolean isPostMainThread() {
        return isPostMainThread;
    }

    void setEvents(Set<EVENT> events) {
        this.iEvents = events;
    }
}
