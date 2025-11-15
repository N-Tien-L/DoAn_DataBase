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
import java.util.Optional;

/**
 *
 * @author ASUS
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
    
    public int getTotalTacGia(){
        String sql = "SELECT COUNT(*) AS Total FROM TACGIA";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery())
        {
            if (rs.next()) {
                return rs.getInt("Total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public boolean insert(TacGia tg) {
        String sql = "INSERT INTO TACGIA (TenTacGia, Website, GhiChu) VALUES (?,?,?)";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, tg.getTenTacGia());
            ps.setString(2, tg.getWebsite());
            ps.setString(3, tg.getGhiChu());
            
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException ex) {
            System.out.println("Tên tác giả đã tồn tại!");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean update(TacGia tg) {
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
            System.out.println("Tên tác giả bị trùng!");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean delete(int id) {
        String sql = "DELETE FROM TACGIA WHERE MaTacGia=?";
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException ex) {
            System.out.println("Không thể xóa! Tác giả đang được dùng trong bảng SACH");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public List<TacGia> search(String keyword, String column) {
        List<TacGia> list = new ArrayList<>();
        
        if (!isValidColumn(column)) return list;
        
        String sql = "SELECT * FROM TACGIA WHERE " + column + " LIKE ? ORDER BY MaTacGia ASC";
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
    
    public Optional<TacGia> getById(int id) {
        String sql = "SELECT * FROM TACGIA WHERE MaTacGia=?";
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    private boolean isValidColumn(String column) {
        return switch(column) {
            case "TenTacGia", "Website", "GhiChu" -> true;
            default -> false;
        };
    }
}
