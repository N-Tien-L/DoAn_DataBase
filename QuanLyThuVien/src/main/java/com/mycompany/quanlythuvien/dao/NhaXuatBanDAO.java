package com.mycompany.quanlythuvien.dao;

import com.mycompany.quanlythuvien.model.NhaXuatBan;
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
public class NhaXuatBanDAO {
    public NhaXuatBan mapRow(ResultSet rs) throws Exception {
        return new NhaXuatBan(
                rs.getInt("MaNXB"),
                rs.getString("TenNXB")
        );
    }

    public List<NhaXuatBan> findAll() {
        List<NhaXuatBan> list = new ArrayList<>();
        String sql = "SELECT * FROM NHAXUATBAN ORDER BY TenNXB ASC";
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
    
    //Lay toan bo NXB
    public List<NhaXuatBan> getAll(int lastMaNXBCursor, int pageSize) {
        List<NhaXuatBan> list = new ArrayList<>();
        boolean isFirstPage = lastMaNXBCursor <= 0;
        String sql = isFirstPage ? "SELECT TOP (?) * FROM NHAXUATBAN ORDER BY MaNXB ASC" 
                : "SELECT TOP (?) * FROM NHAXUATBAN WHERE MaNXB > ? ORDER BY MaNXB ASC";
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            if (isFirstPage) {
                ps.setInt(1, pageSize);
            } else {
                ps.setInt(1, pageSize);
                ps.setInt(2, lastMaNXBCursor);
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
    
    public int getTotalNXB() {
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) AS Total FROM NHAXUATBAN");
            ResultSet rs = ps.executeQuery()) 
        {
            if (rs.next()) return rs.getInt("Total");
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return 0;
    }
    
    //Them NXB
    public boolean insert(NhaXuatBan nxb) {
        String sql = "INSERT INTO NHAXUATBAN (TenNXB) VALUES (?)";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, nxb.getTenNXB());
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException ex) {
            System.out.println("Tên nhà xuất bản đã tồn tại!");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    //Cap nhat NXB
    public boolean update(NhaXuatBan nxb) {
        String sql = "UPDATE NHAXUATBAN SET TenNXB=? WHERE MaNXB=?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, nxb.getTenNXB());
            ps.setInt(2, nxb.getMaNXB());
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException ex) {
            System.out.println("Tên nhà xuất bản bị trùng!");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    //Xoa NXB
    public boolean delete(int maNXB) {
        String sql = "DELETE FROM NHAXUATBAN WHERE MaNXB=?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, maNXB);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException ex) {
            System.out.println("Không thể xóa! Nhà xuất bản này đang được dùng trong bảng SACH");
            return false;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    
    //Tim NXB theo ma
    public Optional<NhaXuatBan> getById(int maNXB) {
        String sql = "SELECT * FROM NHAXUATBAN WHERE MaNXB=?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setInt(1, maNXB);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    public List<NhaXuatBan> search(String keyword, String column, Integer lastMaNXB, int pageSize) {
        List<NhaXuatBan> list = new ArrayList<>();
        String likePattern = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        
        String sql;
        
        switch (column) {
            case "MaNXB":
                sql = "SELECT TOP (?) * FROM NHAXUATBAN WHERE CAST(MaNXB AS VARCHAR) LIKE ? AND (? IS NULL OR MaNXB > ?) ORDER BY MaNXB ASC";
                break;
            case "TenNXB":
                sql = "SELECT TOP (?) * FROM NHAXUATBAN WHERE TenNXB LIKE ? AND (? IS NULL OR MaNXB > ?) ORDER BY MaNXB ASC";
                break;
            default:
                return list;
        }
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            int idx = 1;
            ps.setInt(idx++, pageSize);
            
            ps.setString(idx++, likePattern);
            
            if (lastMaNXB == null) {
                ps.setNull(idx++, java.sql.Types.INTEGER);
                ps.setNull(idx++, java.sql.Types.INTEGER);
            } else {
                ps.setInt(idx++, lastMaNXB);
                ps.setInt(idx++, lastMaNXB);
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
