package com.mycompany.quanlythuvien.dao;

import com.mycompany.quanlythuvien.model.Sach;
import com.mycompany.quanlythuvien.util.DBConnector;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tien
 */
public class SachDAO {
    //lay toan bo sach
    public List<Sach> getAll() throws Exception {
        List<Sach> list = new ArrayList<>();
        String sql = "SELECT * FROM SACH";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) 
        {
            while (rs.next()) {
                Sach s = new Sach(
                    rs.getString("ISBN"),
                    rs.getString("TenSach"),
                    rs.getObject("MaTacGia", Integer.class),
                    rs.getObject("MaTheLoai", Integer.class),
                    rs.getObject("NamXuatBan", Integer.class),
                    rs.getString("DinhDang"),
                    rs.getString("MoTa"),
                    rs.getObject("MaNXB", Integer.class),
                    rs.getBigDecimal("GiaBia"),
                    rs.getObject("SoLuongTon", Integer.class),
                    rs.getObject("SoTrang", Integer.class)
                );
                list.add(s);
            }
        }
        return list;
    }
    
    //them sach
    public boolean insert(Sach s) throws Exception {
        // SoLuongTon được quản lý tự động bởi trigger TRG_BANSAO_Update_SoLuongTon
        String sql = "INSERT INTO SACH (ISBN, TenSach, MaTacGia, MaTheLoai, NamXuatBan, DinhDang, MoTa, MaNXB, GiaBia, SoTrang) VALUES (?,?,?,?,?,?,?,?,?,?)";

        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, s.getISBN());
            ps.setString(2, s.getTenSach());
            ps.setObject(3, s.getMaTacGia(), java.sql.Types.INTEGER);
            ps.setObject(4, s.getMaTheLoai(), java.sql.Types.INTEGER);
            ps.setObject(5, s.getNamXuatBan(), java.sql.Types.INTEGER);
            ps.setString(6, s.getDinhDang());
            ps.setString(7, s.getMoTa());
            ps.setObject(8, s.getMaNXB(), java.sql.Types.INTEGER);
            ps.setBigDecimal(9, s.getGiaBia());
            ps.setObject(10, s.getSoTrang(), java.sql.Types.INTEGER);
            
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException ex) {
            //trung isbn
            throw new Exception("ISBN đã tồn tại trong hệ thống!");
        }
    }
    
    //cap nhat sach
    public boolean update(Sach s) throws Exception {
        // SoLuongTon được quản lý tự động bởi trigger TRG_BANSAO_Update_SoLuongTon
        String sql = """
            UPDATE SACH
            SET TenSach=?, MaTacGia=?, MaTheLoai=?, NamXuatBan=?,
                DinhDang=?, MoTa=?, MaNXB=?, GiaBia=?, SoTrang=?
            WHERE ISBN=?
        """;
        
        try(Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, s.getTenSach());
            ps.setObject(2, s.getMaTacGia(), java.sql.Types.INTEGER);
            ps.setObject(3, s.getMaTheLoai(), java.sql.Types.INTEGER);
            ps.setObject(4, s.getNamXuatBan(), java.sql.Types.INTEGER);
            ps.setString(5, s.getDinhDang());
            ps.setString(6, s.getMoTa());
            ps.setObject(7, s.getMaNXB(), java.sql.Types.INTEGER);
            ps.setBigDecimal(8, s.getGiaBia());
            ps.setObject(9, s.getSoTrang(), java.sql.Types.INTEGER);
            ps.setString(10, s.getISBN());
            
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new Exception("Lỗi ràng buộc dữ liệu khi cập nhật!");
        }
    }
    
    //Xoa sach
    public boolean delete(String isbn) throws Exception {
        String check = "SELECT COUNT(*) FROM BANSAO WHERE ISBN = ?";
        String sql = "DELETE FROM SACH WHERE ISBN = ?";
        try (Connection con = DBConnector.getConnection()){
            //rang buoc ban sao
            try (PreparedStatement psCheck = con.prepareStatement(check)) {
                psCheck.setString(1, isbn);
                ResultSet rs = psCheck.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new Exception("Không thể xóa vì sách vẫn còn bản sao trong kho!");
                }
            }
            
            //khong co ban sao -> duoc phep xoa
            try (PreparedStatement psDelete = con.prepareStatement(sql)) {
                psDelete.setString(1, isbn);
                return psDelete.executeUpdate() > 0;
            }
        }
    }
    
    //Lay so luong ban sao hien co
    public Integer getBanSaoHienCo(String isbn) throws Exception {
        String sql = "SELECT COUNT(*) FROM BANSAO WHERE ISBN = ? AND TinhTrang = N'Có sẵn'";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return null;
            }
        }
    }
    
    //Tim sach theo ID
    public Sach findByISBN(String isbn) throws Exception {
        String sql = """
            SELECT S.*, TG.TenTacGia, NXB.TenNXB, TL.TenTheLoai
            FROM SACH AS S
            LEFT JOIN TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
            LEFT JOIN NHAXUATBAN AS NXB ON S.MaNXB = NXB.MaNXB
            LEFT JOIN THELOAI AS TL ON S.MaTheLoai = TL.MaTheLoai
            WHERE S.ISBN = ?
                     """;
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Sach s = new Sach(
                        rs.getString("ISBN"),
                        rs.getString("TenSach"),
                        rs.getObject("MaTacGia", Integer.class),
                        rs.getObject("MaTheLoai", Integer.class),
                        rs.getObject("NamXuatBan", Integer.class),
                        rs.getString("DinhDang"),
                        rs.getString("MoTa"),
                        rs.getObject("MaNXB", Integer.class),
                        rs.getBigDecimal("GiaBia"),
                        rs.getObject("SoLuongTon", Integer.class),
                        rs.getObject("SoTrang", Integer.class)
                    );
                    s.setTenTacGia(rs.getString("TenTacGia"));
                    s.setTenNXB(rs.getString("TenNXB"));
                    s.setTenTheLoai(rs.getString("TenTheLoai"));
                    return s;
                }
            }
        }
        return null;
    }
    
    // du lieu de hien thi len table
    public List<Sach> getAllForTable() throws Exception {
        List<Sach> list = new ArrayList<>();
        
        String sql = """
            SELECT S.ISBN, S.TenSach, TG.TenTacGia, NXB.TenNXB, S.NamXuatBan, TL.TenTheLoai
            FROM SACH AS S
            LEFT JOIN TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
            LEFT JOIN NHAXUATBAN AS NXB ON S.MaNXB = NXB.MaNXB
            LEFT JOIN THELOAI AS TL ON S.MaTheLoai = TL.MaTheLoai
            ORDER BY S.TenSach
                     """;
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) 
        {
            while (rs.next()) {
                Sach s = new Sach();
                s.setISBN(rs.getString("ISBN"));
                s.setTenSach(rs.getString("TenSach"));
                s.setTenTacGia(rs.getString("TenTacGia"));
                s.setTenNXB(rs.getString("TenNXB"));
                s.setNamXuatBan(rs.getInt("NamXuatBan"));
                s.setTenTheLoai(rs.getString("TenTheLoai"));
                list.add(s);
            }
        }
        return list;
    }
    
    //Tim kiem theo tieu chi (All/Tac gia/Ten sach/NXB/The Loai/ISBN)
    public List<Sach> search(String keyword, String tieuChi) throws Exception {
        List<Sach> list = new ArrayList<>();
        String sql = """
            SELECT S.ISBN, S.TenSach, TG.TenTacGia, NXB.TenNXB, S.NamXuatBan, TL.TenTheLoai
            FROM SACH AS S
            LEFT JOIN TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
            LEFT JOIN NHAXUATBAN AS NXB ON S.MaNXB = NXB.MaNXB
            LEFT JOIN THELOAI AS TL ON S.MaTheLoai = TL.MaTheLoai
            WHERE 1 = 1
                     """;
        
        if (keyword != null && !keyword.isBlank()) {
            keyword = "%" + keyword.trim() + "%";
            switch (tieuChi) {
                case "Tên sách" -> sql += " AND S.TenSach LIKE ?";
                case "Tác giả" -> sql += " AND TG.TenTacGia LIKE ?";
                case "Nhà xuất bản" -> sql += " AND NXB.TenNXB LIKE ?";
                case "Thể loại" -> sql += " AND TL.TenTheLoai LIKE ?";
                case "ISBN" -> sql += " AND S.ISBN LIKE ?";
                default -> sql += """
                    AND (
                        S.ISBN LIKE ? OR S.TenSach LIKE ? OR TG.TenTacGia LIKE ?
                        OR NXB.TenNXB LIKE ? OR TL.TenTheLoai LIKE ?
                        )
                    """;
            }
        }
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            if (keyword != null && !keyword.isBlank()) {
                if ("Tất cả".equals(tieuChi)) {
                    for (int i = 1; i <= 5; i++) {
                        ps.setString(i, keyword);
                    }
                } else {
                        ps.setString(1, keyword);
                    }
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Sach s = new Sach();
                    s.setISBN(rs.getString("ISBN"));
                    s.setTenSach(rs.getString("TenSach"));
                    s.setTenTacGia(rs.getString("TenTacGia"));
                    s.setTenNXB(rs.getString("TenNXB"));
                    s.setNamXuatBan(rs.getInt("NamXuatBan"));
                    s.setTenTheLoai(rs.getString("TenTheLoai"));
                    list.add(s);
                }
            }
        }
        return list;
    }
}
