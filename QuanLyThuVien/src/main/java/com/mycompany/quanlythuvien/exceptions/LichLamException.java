package com.mycompany.quanlythuvien.exceptions;

public class LichLamException extends Exception {
    public LichLamException(String message) {
        super(message);
    }
    
    public LichLamException(String message, Throwable cause) {
        super(message, cause);
    }
}
