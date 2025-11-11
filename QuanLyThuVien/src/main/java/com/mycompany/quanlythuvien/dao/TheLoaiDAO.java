package com.mycompany.quanlythuvien.dao;

import com.mycompany.quanlythuvien.model.TheLoai;
import com.mycompany.quanlythuvien.util.DBConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tien
 */
public class TheLoaiDAO {
    //Lay toan bo the loai
    public List<TheLoai> getAll() throws Exception {
        List<TheLoai> list = new ArrayList<>();
        String sql = "SELECT * FROM THE LOAI";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) 
        {
            while (rs.next()) {
                list.add(new TheLoai(rs.getInt("MaTheLoai"), 
                        rs.getString("TenTheLoai")));
            }
        }
        return list;
    }
    
    //Them the loai moi
    public boolean insert(TheLoai tl) throws Exception {
        String sql = "INSERT INTO THELOAI (TenTheLoai) VALUES(?)";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, tl.getTenTheLoai());
            return ps.executeUpdate() > 0;
        }
    }
    
    //Cap nhat the loai
    public boolean update(TheLoai tl) throws Exception {
        String sql = "UPDATE THELOAI SET TenTheLoai=? WHERE MaTheLoai=?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, tl.getTenTheLoai());
            ps.setInt(2, tl.getMaTheLoai());
            return ps.executeUpdate() > 0;
        }
    }
    
    //Xoa the loai
    public boolean delete(int maTheLoai) throws Exception {
        String sql = "DELETE FROM THELOAI WHERE MaTheLoai=?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setInt(1, maTheLoai);
            return ps.executeUpdate() > 0;
        }
    }
    
    //Tim the loai theo ma
    public TheLoai findById(int maTheLoai) throws Exception {
        String sql = "SELECT * FROM THELOAI WHERE MaTheLoai=?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setInt(1, maTheLoai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TheLoai(
                            rs.getInt("MaTheLoai"),
                            rs.getString("TenTheLoai"));
                }
            }
        }
        return null;
    }
}
