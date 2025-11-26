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
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Thanh
 */
public class TacGiaDAO {
    public TacGia mapRow(ResultSet rs) throws SQLException {
        return new TacGia(
                rs.getInt("MaTacGia"),
                rs.getString("TenTacGia"),
                rs.getString("Website"),
                rs.getString("GhiChu")
        );
    }

    public List<TacGia> findAll() {
        List<TacGia> list = new ArrayList<>();
        String sql = "SELECT * FROM TACGIA ORDER BY TenTacGia ASC";
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
  
    public List<TacGia> getAll(int lastIdCursor, int pageSize) {
        List<TacGia> list = new ArrayList<>();
        boolean isFirstPage = lastIdCursor <= 0;
        
        String sqlFirstPage = "SELECT TOP (?) * FROM TACGIA ORDER BY MaTacGia ASC";
        String sqlNextPage = "SELECT TOP (?) * FROM TACGIA WHERE MaTacGia > ? ORDER BY MaTacGia ASC";
        String sql = isFirstPage ? sqlFirstPage : sqlNextPage;
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setInt(1, pageSize);
            if (!isFirstPage) {
                ps.setInt(2, lastIdCursor);
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
    
    public boolean insert(TacGia tg) throws Exception{
        String sql = "INSERT INTO TACGIA (TenTacGia, Website, GhiChu) VALUES (?,?,?)";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, tg.getTenTacGia());
            ps.setString(2, tg.getWebsite());
            ps.setString(3, tg.getGhiChu());
            
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new Exception("Lỗi: Tên tác giả đã tồn tại!", ex);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    public boolean update(TacGia tg) throws Exception {
        String sql = "UPDATE TACGIA SET TenTacGia=?, Website=?, GhiChu=? WHERE MaTacGia=?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, tg.getTenTacGia());
            ps.setString(2, tg.getWebsite());
            ps.setString(3, tg.getGhiChu());
            ps.setInt(4, tg.getMaTacGia());
            
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new Exception("Lỗi: Tên tác giả bị trùng!", ex);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    public boolean delete(int id) throws Exception {
        String sql = "DELETE FROM TACGIA WHERE MaTacGia=?";
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new Exception("Lỗi: Không thể xóa! Tác giả đang được sử dụng trong bảng SACH.", ex);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    public List<TacGia> search(String keyword, String column, Integer lastMaTacGia, int pageSize) {
        List<TacGia> list = new ArrayList<>();
        String likePattern = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String trimmedKeyword = keyword == null ? "" : keyword.trim(); // Dùng cho trường hợp MaTacGia
        
        String sql;
        switch (column) {
            case "MaTacGia":
                sql = "SELECT TOP (?) * FROM TACGIA WHERE MaTacGia = ? AND (? IS NULL OR MaTacGia > ?) ORDER BY MaTacGia ASC";
                break;
            case "TenTacGia":
                sql = "SELECT TOP (?) * FROM TACGIA WHERE TenTacGia LIKE ? AND (? IS NULL OR MaTacGia > ?) ORDER BY MaTacGia ASC";
                break;
            case "Website":
                sql = "SELECT TOP (?) * FROM TACGIA WHERE Website LIKE ? AND (? IS NULL OR MaTacGia > ?) ORDER BY MaTacGia ASC";
                break;
            case "GhiChu":
                sql = "SELECT TOP (?) * FROM TACGIA WHERE GhiChu LIKE ? AND (? IS NULL OR MaTacGia > ?) ORDER BY MaTacGia ASC";
                break;
            default:
                return list;
        }
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            int idx = 1;
            ps.setInt(idx++, pageSize);
            if ("MaTacGia".equals(column)) {
                try {
                    int id = Integer.parseInt(trimmedKeyword);
                    ps.setInt(idx++, id);
                } catch (NumberFormatException e) {
                    ps.setInt(idx++, -1);
                }
            } else {
                ps.setString(idx++, likePattern);
            }
            
            if (lastMaTacGia == null) {
                ps.setNull(idx++, java.sql.Types.INTEGER);
                ps.setNull(idx++, java.sql.Types.INTEGER);
            } else {
                ps.setInt(idx++, lastMaTacGia);
                ps.setInt(idx++, lastMaTacGia);
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
