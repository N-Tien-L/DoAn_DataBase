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
    public List<BanSao> getAll() throws Exception {
        
        //lay toan bo BAN SAO
        List<BanSao> list = new ArrayList<>();
        String sql = "SELECT * FROM BANSAO";
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery())
        {
            while (rs.next()) {
                BanSao b = new BanSao(
                        rs.getInt("MaBanSao"),
                        rs.getString("ISBN"),
                        rs.getInt("SoThuTuTrongKho"),
                        rs.getString("TinhTrang"),
                        (rs.getDate("NgayNhapKho") != null) ? rs.getDate("NgayNhapKho").toLocalDate() : null,
                        rs.getString("ViTriLuuTru")
                );
                list.add(b);
            }
        }
        
        return list;
    }
    
    //them ban sao moi
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
    
    //lay ds ban sao theo ISBN cua sach goc
    public List<BanSao> getByISBN(String isbn) throws Exception {
        List<BanSao> list = new ArrayList<>();
        String sql = "SELECT * FROM BANSAO WHERE ISBN = ?";
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BanSao b = new BanSao(
                            rs.getInt("MaBanSao"),
                            rs.getString("ISBN"),
                            rs.getInt("SoThuTuTrongKho"),
                            rs.getString("TinhTrang"),
                            (rs.getDate("NgayNhapKho") != null) ? rs.getDate("NgayNhapKho").toLocalDate() : null,
                            rs.getString("ViTriLuuTru")
                    );
                    list.add(b);
                }
            }
        }
        
        return list;
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
