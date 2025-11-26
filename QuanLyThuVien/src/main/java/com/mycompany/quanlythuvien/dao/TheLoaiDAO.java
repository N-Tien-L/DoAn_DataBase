package com.mycompany.quanlythuvien.dao;

import com.mycompany.quanlythuvien.model.TheLoai;
import com.mycompany.quanlythuvien.util.DBConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Thanh
 */
public class TheLoaiDAO {
    public TheLoai mapRow(ResultSet rs) throws Exception {
        return new TheLoai(
                rs.getInt("MaTheLoai"),
                rs.getString("TenTheLoai")
        );
    }

    public List<TheLoai> findAll() {
        List<TheLoai> list = new ArrayList<>();
        String sql = "SELECT * FROM THELOAI ORDER BY TenTheLoai ASC";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
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
    
    //Them the loai moi
    public boolean insert(TheLoai tl) throws Exception {
        String sql = "INSERT INTO THELOAI (TenTheLoai) VALUES(?)";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, tl.getTenTheLoai());
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new Exception("Lỗi: Tên thể loại đã tồn tại!", ex);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
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
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new Exception("Lỗi: Tên thể loại bị trùng!", ex);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
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
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new Exception("Lỗi: Không thể xóa! Thể loại đang được dùng trong bảng SACH.", ex);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    public List<TheLoai> search(String keyword, String column, Integer lastMaTheLoai, int pageSize) {
        List<TheLoai> list = new ArrayList<>();
        String likePattern = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String trimmedKeyword = keyword == null ? "" : keyword.trim();
        
        String sql;
        
        switch (column) {
            case "MaTheLoai":
                sql = "SELECT TOP (?) * FROM THELOAI WHERE MaTheLoai = ? AND (? IS NULL OR MaTheLoai > ?) ORDER BY MaTheLoai ASC";
                break;
            case "TenTheLoai":
                sql = "SELECT TOP (?) * FROM THELOAI WHERE TenTheLoai LIKE ? AND (? IS NULL OR MaTheLoai > ?) ORDER BY MaTheLoai ASC";
                break;
            default:
                return list;
        }
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {

            int idx = 1;
            ps.setInt(idx++, pageSize);
            
            if ("MaTheLoai".equals(column)) {
                try {
                    int id = Integer.parseInt(trimmedKeyword);
                    ps.setInt(idx++, id);
                } catch (NumberFormatException e) {
                    ps.setInt(idx++, -1);
                }
            } else {
                ps.setString(idx++, likePattern);
            }
            
            if (lastMaTheLoai == null) {
                ps.setNull(idx++, java.sql.Types.INTEGER);
                ps.setNull(idx++, java.sql.Types.INTEGER);
            } else {
                ps.setInt(idx++, lastMaTheLoai);
                ps.setInt(idx++, lastMaTheLoai);
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
}
