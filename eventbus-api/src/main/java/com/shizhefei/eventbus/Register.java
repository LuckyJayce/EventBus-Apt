package com.shizhefei.eventbus;

public class Register<EVENT> {
    private EVENT event;
    private boolean isRegister;

    public Register(EVENT event, boolean isRegister) {
        this.event = event;
        this.isRegister = isRegister;
    }

    public EVENT getEvent() {
        return event;
    }

    public void setEvent(EVENT event){
        this.event = event;
    }

    public boolean isRegister() {
        return isRegister;
    }

    public void setIsRegister(boolean isRegister) {
        this.isRegister = isRegister;
    }
}