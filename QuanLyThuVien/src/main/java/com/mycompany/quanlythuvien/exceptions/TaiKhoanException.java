package com.mycompany.quanlythuvien.exceptions;

public class TaiKhoanException extends Exception {
    public TaiKhoanException(String message) {
        super(message);
    }

    public TaiKhoanException(String message, Throwable cause) {
        super(message, cause);
    }
}
