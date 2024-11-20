package com.messagingservice.backendservice.exception;

public class DuplicateEntryExcepion extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public DuplicateEntryExcepion(String msg) {
        super(msg);
    }
}
