/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlythuvien.dao;

import com.mycompany.quanlythuvien.model.TacGia;
import com.mycompany.quanlythuvien.util.DBConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ASUS
 */
public class TacGiaDAO {
    public List<TacGia> getAll() {
        List<TacGia> list = new ArrayList<>();
        String sql = "SELECT * FROM TacGia";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TacGia tg = new TacGia();
                tg.setMaTacGia(rs.getInt("maTacGia"));
                tg.setTenTacGia(rs.getString("tenTacGia"));
                list.add(tg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
