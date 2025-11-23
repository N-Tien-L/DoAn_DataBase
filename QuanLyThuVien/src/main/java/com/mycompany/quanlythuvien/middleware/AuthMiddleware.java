package com.mycompany.quanlythuvien.middleware;

import com.mycompany.quanlythuvien.exceptions.AuthException;
import com.mycompany.quanlythuvien.model.TaiKhoan;

public class AuthMiddleware {

    private AuthMiddleware() {}

    public static void requireAdmin(TaiKhoan currentUser) throws AuthException {
        if(currentUser == null) {
            throw new AuthException("Chưa đăng nhập");
        }

        if(!currentUser.getRole().equals("Admin")) {
            throw new AuthException("Cần quyền Admin để thực hiện");
        }
    }
}
