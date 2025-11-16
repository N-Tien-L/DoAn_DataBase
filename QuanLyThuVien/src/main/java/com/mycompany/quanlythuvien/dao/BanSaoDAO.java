package com.mycompany.quanlythuvien.dao;

import com.mycompany.quanlythuvien.model.BanSao;
import com.mycompany.quanlythuvien.util.DBConnector;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tien
 */
public class BanSaoDAO {    
    public List<BanSao> getPage(String isbn, int pageSize, Integer lastMaBanSao) {
        List<BanSao> list = new ArrayList<>();
        if (isbn == null || isbn.isBlank()) return list;
        if (pageSize < 1 || pageSize > 100) pageSize = 10;
        
        boolean isFirstPage = (lastMaBanSao == null);
        String sql = isFirstPage
                ? "SELECT TOP (?) * FROM BANSAO WHERE ISBN = ? ORDER BY MaBanSao ASC"
                : "SELECT TOP (?) * FROM BANSAO WHERE ISBN = ? AND MaBanSao > ? ORDER BY MaBanSao ASC";    
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, pageSize + 1);
            ps.setString(2, isbn);
            if (lastMaBanSao != null) ps.setInt(3, lastMaBanSao);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BanSao b = new BanSao(
                            rs.getInt("MaBanSao"),
                            rs.getString("ISBN"),
                            rs.getInt("SoThuTuTrongKho"),
                            rs.getString("TinhTrang"),
                            rs.getDate("NgayNhapKho") != null ? rs.getDate("NgayNhapKho").toLocalDate() : null,
                            rs.getString("ViTriLuuTru")
                    );
                    list.add(b);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        
        return list;
    }
    //tổng số bản sao theo ISBN
    public int getTotalCount(String isbn) {
        String sql = "SELECT COUNT(*) FROM BANSAO WHERE ISBN = ?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    //tổng số trang theo ISBN
    public int getTotalPages(String isbn, int pageSize) {
        int total = getTotalCount(isbn);
        if (total <= 0) return 0;
        return (int) Math.ceil((double) total / pageSize);
    }
    public List<BanSao> getAllByISBN(String isbn) {
        return getPage(isbn, Integer.MAX_VALUE, null);
    }
    public boolean insert(BanSao b) throws Exception {
        String sql = """
            INSERT INTO BANSAO (ISBN, SoThuTuTrongKho, TinhTrang, NgayNhapKho, ViTriLuuTru)
            VALUES (?,?,?,?,?)
                     """;
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, b.getISBN());
            ps.setInt(2, b.getSoThuTuTrongKho());
            ps.setString(3, b.getTinhTrang());
            
            if (b.getNgayNhapKho() != null) {
                ps.setDate(4, Date.valueOf(b.getNgayNhapKho()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            
            ps.setString(5, b.getViTriLuuTru());
            
            return ps.executeUpdate() > 0;
        }
    }
    
    //cap nhat BANSAO
    public boolean update (BanSao b) throws Exception {
        String sql = """
            UPDATE BANSAO
            SET ISBN = ?, SoThuTuTrongKho = ?, TinhTrang = ?, NgayNhapKho = ?, ViTriLuuTru = ?
            WHERE MaBanSao = ?
                     """;
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, b.getISBN());
            ps.setInt(2, b.getSoThuTuTrongKho());
            ps.setString(3, b.getTinhTrang());
            
            if (b.getNgayNhapKho() != null) {
                ps.setDate(4, Date.valueOf(b.getNgayNhapKho()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            
            ps.setString(5, b.getViTriLuuTru());
            ps.setInt(6, b.getMaBanSao());
            
            return ps.executeUpdate() > 0;
        } 
    }
    
    //xoa ban sao theo ID
    public boolean delete(int maBanSao) throws Exception {
        String sql = "DELETE FROM BANSAO WHERE MaBanSao = ?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setInt(1, maBanSao);
            return ps.executeUpdate() > 0;
        }
    }
    
    //Tim 1 ban sao theo ID
    public BanSao findById(int maBanSao) throws Exception {
        String sql = "SELECT * FROM BANSAO Where MaBanSao = ?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, maBanSao);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new BanSao(
                            rs.getInt("MaBanSao"),
                            rs.getString("ISBN"),
                            rs.getInt("SoThuTuTrongKho"),
                            rs.getString("TinhTrang"),
                            (rs.getDate("NgayNhapKho") != null) ? rs.getDate("NgayNhapKho").toLocalDate() : null,
                            rs.getString("ViTriLuuTru")
                    );
                }
            }
        }
        return null;
    }
}
