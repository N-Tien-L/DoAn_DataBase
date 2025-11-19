package com.mycompany.quanlythuvien.exceptions;

public class ThongBaoException  extends Exception {
    public ThongBaoException(String message) {
        super(message);
    }

    public ThongBaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
