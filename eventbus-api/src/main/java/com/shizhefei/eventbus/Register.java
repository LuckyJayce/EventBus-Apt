package com.shizhefei.eventbus;

public class Register<EVENT> {
    private EVENT event;

    public Register(EVENT event) {
        this.event = event;
    }

    public EVENT getEvent() {
        return event;
    }

    public void setEvent(EVENT event){
        this.event = event;
    }
}