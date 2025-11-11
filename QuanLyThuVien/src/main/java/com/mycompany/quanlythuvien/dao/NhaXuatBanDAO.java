package com.mycompany.quanlythuvien.dao;

import com.mycompany.quanlythuvien.model.NhaXuatBan;
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
public class NhaXuatBanDAO {
    //Lay toan bo NXB
    public List<NhaXuatBan> getAll() throws Exception {
        List<NhaXuatBan> list = new ArrayList<>();
        String sql = "SELECT * FROM NHAXUATBAN";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery())
        {
            while (rs.next()) {
                list.add(new NhaXuatBan(
                        rs.getInt("MaNXB"),
                        rs.getString("TenNXB")
                ));
            }
        }
        return list;
    }
    
    //Them NXB
    public boolean insert(NhaXuatBan nxb) throws Exception {
        String sql = "INSERT INTO NHAXUATBAN (TenNXB) VALUES (?)";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, nxb.getTenNXB());
            return ps.executeUpdate() > 0;
        }
    }
    
    //Cap nhat NXB
    public boolean update(NhaXuatBan nxb) throws Exception {
        String sql = "UPDATE NHAXUATBAN SET TenNXB=? WHERE MaNXB=?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, nxb.getTenNXB());
            ps.setInt(2, nxb.getMaNXB());
            return ps.executeUpdate() > 0;
        }
    }
    
    //Xoa NXB
    public boolean delete(int maNXB) throws Exception {
        String sql = "DELETE FROM NHAXUATBAN WHERE MaNXB=?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, maNXB);
            return ps.executeUpdate() > 0;
        }
    }
    
    //Tim NXB theo ma
    public NhaXuatBan findById(int maNXB) throws Exception {
        String sql = "SELECT * FROM NHAXUATBAN WHERE MaNXB=?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setInt(1, maNXB);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new NhaXuatBan(
                            rs.getInt("MaNXB"),
                            rs.getString("TenNXB")
                    );
                }
            }
        }
        return null;
    }
}
