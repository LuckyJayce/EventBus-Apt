package com.shizhefei.eventbus;

public class Register<EVENT> {
    private EVENT receiver;

    public Register(EVENT receiver) {
        this.receiver = receiver;
    }

    public EVENT getReceiver() {
        return receiver;
    }

    public void setReceiver(EVENT receiver){
        this.receiver = receiver;
    }
}