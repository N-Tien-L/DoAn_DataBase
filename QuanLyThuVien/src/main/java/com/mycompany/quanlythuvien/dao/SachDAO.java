package com.mycompany.quanlythuvien.dao;

import com.mycompany.quanlythuvien.model.Sach;
import com.mycompany.quanlythuvien.util.DBConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bố
 */
public class SachDAO {
    // lay toan bo sach
    public List<Sach> getAll() throws Exception {
        List<Sach> list = new ArrayList<>();
        String sql = "SELECT * FROM SACH";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Sach s = new Sach(
                        rs.getString("ISBN"),
                        rs.getString("TenSach"),
                        rs.getString("TacGia"),
                        rs.getObject("MaTheLoai", Integer.class),
                        rs.getObject("NamXuatBan", Integer.class),
                        rs.getString("DinhDang"),
                        rs.getString("MoTa"),
                        rs.getObject("MaNXB", Integer.class),
                        rs.getBigDecimal("GiaBia"),
                        rs.getObject("SoLuongTon", Integer.class),
                        rs.getObject("SoTrang", Integer.class));
                list.add(s);
            }
        }
        return list;
    }

    // them sach
    public boolean insert(Sach s) throws Exception {
        // SoLuongTon được quản lý tự động bởi trigger TRG_BANSAO_Update_SoLuongTon
        String sql = "INSERT INTO SACH (ISBN, TenSach, TacGia, MaTheLoai, NamXuatBan, DinhDang, MoTa, MaNXB, GiaBia, SoTrang) VALUES (?,?,?,?,?,?,?,?,?,?)";

        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getISBN());
            ps.setString(2, s.getTenSach());
            ps.setString(3, s.getTacGia());
            ps.setObject(4, s.getMaTheLoai(), java.sql.Types.INTEGER);
            ps.setObject(5, s.getNamXuatBan(), java.sql.Types.INTEGER);
            ps.setString(6, s.getDinhDang());
            ps.setString(7, s.getMoTa());
            ps.setObject(8, s.getMaNXB(), java.sql.Types.INTEGER);
            ps.setBigDecimal(9, s.getGiaBia());
            ps.setObject(10, s.getSoTrang(), java.sql.Types.INTEGER);

            return ps.executeUpdate() > 0;
        }
    }

    // cap nhat sach
    public boolean update(Sach s) throws Exception {
        // SoLuongTon được quản lý tự động bởi trigger TRG_BANSAO_Update_SoLuongTon
        String sql = """
                    UPDATE SACH
                    SET TenSach=?, TacGia=?, MaTheLoai=?, NamXuatBan=?,
                        DinhDang=?, MoTa=?, MaNXB=?, GiaBia=?, SoTrang=?
                    WHERE ISBN=?
                """;

        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getTenSach());
            ps.setString(2, s.getTacGia());
            ps.setObject(3, s.getMaTheLoai(), java.sql.Types.INTEGER);
            ps.setObject(4, s.getNamXuatBan(), java.sql.Types.INTEGER);
            ps.setString(5, s.getDinhDang());
            ps.setString(6, s.getMoTa());
            ps.setObject(7, s.getMaNXB(), java.sql.Types.INTEGER);
            ps.setBigDecimal(8, s.getGiaBia());
            ps.setObject(9, s.getSoTrang(), java.sql.Types.INTEGER);
            ps.setString(10, s.getISBN());

            return ps.executeUpdate() > 0;
        }
    }

    // Xoa sach
    public boolean delete(String isbn) throws Exception {
        String sql = "DELETE FROM SACH WHERE ISBN = ?";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, isbn);
            return ps.executeUpdate() > 0;
        }
    }

    // Lay so luong ban sao hien co
    public Integer getBanSaoHienCo(String isbn) throws Exception {
        String sql = "SELECT COUNT(*) FROM BANSAO WHERE ISBN = ? AND TinhTrang = N'Có sẵn'";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return null;
            }
        }
    }

    // Tim sach theo ID
    public Sach findByISBN(String isbn) throws Exception {
        String sql = "SELECT * FROM SACH WHERE ISBN = ?";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Sach(
                            rs.getString("ISBN"),
                            rs.getString("TenSach"),
                            rs.getString("TacGia"),
                            rs.getObject("MaTheLoai", Integer.class),
                            rs.getObject("NamXuatBan", Integer.class),
                            rs.getString("DinhDang"),
                            rs.getString("MoTa"),
                            rs.getObject("MaNXB", Integer.class),
                            rs.getBigDecimal("GiaBia"),
                            rs.getObject("SoLuongTon", Integer.class),
                            rs.getObject("SoTrang", Integer.class));
                }
            }
        }
        return null;
    }
}
