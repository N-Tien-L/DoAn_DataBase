package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.TaiKhoanDAO;
import com.mycompany.quanlythuvien.model.TaiKhoan;

/**
 *
 * @author Tien
 */
public class TaiKhoanController {
    private final TaiKhoanDAO dao = new TaiKhoanDAO();

    public TaiKhoan login(String email, String password) {
        return dao.checkLogin(email, password);
    }
}
