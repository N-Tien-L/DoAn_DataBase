package com.mycompany.quanlythuvien.dao;

import com.mycompany.quanlythuvien.model.TheLoai;
import com.mycompany.quanlythuvien.util.DBConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Tien
 */
public class TheLoaiDAO {
    public TheLoai mapRow(ResultSet rs) throws Exception {
        return new TheLoai(
                rs.getInt("MaTheLoai"),
                rs.getString("TenTheLoai")
        );
    }
    
    //Lay toan bo the loai
    public List<TheLoai> getAll(int lastMaTheLoaiCursor, int pageSize) {
        List<TheLoai> list = new ArrayList<>();
        boolean isFirstPage = lastMaTheLoaiCursor <= 0;
        String sql = isFirstPage ? "SELECT TOP (?) * FROM THELOAI ORDER BY MaTheLoai ASC"
                : "SELECT TOP (?) * FROM THELOAI WHERE MaTheLoai > ? ORDER BY MaTheLoai ASC";
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            if (isFirstPage) {
                ps.setInt(1, pageSize);
            } else {
                ps.setInt(1, pageSize);
                ps.setInt(2, lastMaTheLoaiCursor);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public int getTotalTL(){
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) AS Total FROM THELOAI");
            ResultSet rs = ps.executeQuery())
        {
            if (rs.next()) return rs.getInt("Total");
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return 0;
    }
    
    //Them the loai moi
    public boolean insert(TheLoai tl) {
        String sql = "INSERT INTO THELOAI (TenTheLoai) VALUES(?)";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, tl.getTenTheLoai());
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException ex) {
            System.out.println("Tên thể loại đã tồn tại!");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    //Cap nhat the loai
    public boolean update(TheLoai tl) {
        String sql = "UPDATE THELOAI SET TenTheLoai=? WHERE MaTheLoai=?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, tl.getTenTheLoai());
            ps.setInt(2, tl.getMaTheLoai());
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException ex) {
            System.out.println("Tên thể loại bị trùng!");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    //Xoa the loai
    public boolean delete(int maTheLoai) {
        String sql = "DELETE FROM THELOAI WHERE MaTheLoai=?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setInt(1, maTheLoai);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException ex) {
            System.out.println("Không thể xóa! Thể loại đang được dùng trong bảng SACH");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    //Tim the loai theo ma
    public Optional<TheLoai> getById(int maTheLoai) {
        String sql = "SELECT * FROM THELOAI WHERE MaTheLoai=?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setInt(1, maTheLoai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    public List<TheLoai> search(String keyword, String column) {
        List<TheLoai> list = new ArrayList<>();
        if (!isValidColumn(column)) return list;
        
        String sql = "SELECT * FROM THELOAI WHERE " + column + " LIKE ? ORDER BY MaTheLoai ASC";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return list;
    }
    
    private boolean isValidColumn(String column) {
        return switch (column) {
            case "TenTheLoai" -> true;
            default -> false;
        };
    }
}
