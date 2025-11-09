package com.mycompany.quanlythuvien.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.mycompany.quanlythuvien.model.TaiKhoan;
import com.mycompany.quanlythuvien.util.DBConnector;

/**
 *
 * @author Tien
 */
public class TaiKhoanDAO {

    private static final String SQL_LOGIN = "SELECT * FROM TAIKHOAN WHERE Email = ? AND [Password] = ?";


    public TaiKhoan checkLogin(String email, String password) {
        // try-with-resource (tự động đóng conn, rs, ps)
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_LOGIN);

            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new TaiKhoan(
                    rs.getString("Email"),
                    rs.getString("Password"),
                    rs.getString("HoTen"),
                    rs.getString("Role")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
