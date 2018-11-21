package com.shizhefei.eventbus;

import android.os.Bundle;

import java.util.Collection;
import java.util.List;
import java.util.Map;
/**
 * Created by LuckyJayce
 */
public abstract class EventProxy<EVENT extends IEvent> implements IEvent {
    protected boolean isPostMainThread;
    protected Collection<Register<EVENT>> registers;
    protected String processName;

    void setPostMainThread(boolean postMainThread) {
        this.isPostMainThread = postMainThread;
    }

    public boolean isPostMainThread() {
        return isPostMainThread;
    }

    void setEvents(Collection<Register<EVENT>> events) {
        this.registers = events;
    }

    void setProcessName(String processName) {
        this.processName = processName;
    }

    public void onRemoteEvent(Bundle eventRemoteData){}
}
