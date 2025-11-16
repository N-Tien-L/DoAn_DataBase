package com.mycompany.quanlythuvien.dao;

import com.mycompany.quanlythuvien.model.Sach;
import com.mycompany.quanlythuvien.util.DBConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bố
 */
public class SachDAO {
    private static final String SQL_GET_FIRST_PAGE = """
        SELECT TOP (?) S.ISBN, S.TenSach, TG.TenTacGia, NXB.TenNXB, S.NamXuatBan, TL.TenTheLoai
        FROM SACH AS S
        LEFT JOIN TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
        LEFT JOIN NHAXUATBAN AS NXB ON S.MaNXB = NXB.MaNXB
        LEFT JOIN THELOAI AS TL ON S.MaTheLoai = TL.MaTheLoai
        ORDER BY S.ISBN ASC                            
    """;

    private static final String SQL_GET_NEXT_PAGE = """
        SELECT TOP (?) S.ISBN, S.TenSach, TG.TenTacGia, NXB.TenNXB, S.NamXuatBan, TL.TenTheLoai
        FROM SACH AS S
        LEFT JOIN TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
        LEFT JOIN NHAXUATBAN AS NXB ON S.MaNXB = NXB.MaNXB
        LEFT JOIN THELOAI AS TL ON S.MaTheLoai = TL.MaTheLoai
        WHERE S.ISBN > ?
        ORDER BY S.ISBN ASC
    """;
    
    private static final String SQL_COUNT_TOTAL = "SELECT COUNT(*) FROM SACH";
    
    
    // them sach
    public boolean insert(Sach s) throws Exception {
        // SoLuongTon được quản lý tự động bởi trigger TRG_BANSAO_Update_SoLuongTon
        String sql = "INSERT INTO SACH (ISBN, TenSach, MaTacGia, MaTheLoai, NamXuatBan, DinhDang, MoTa, MaNXB, GiaBia, SoTrang) VALUES (?,?,?,?,?,?,?,?,?,?)";

        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
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

    // cap nhat sach
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

    // Xoa sach
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
                return 0;
            }
        }
    }

    // Tim sach theo ID
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
                PreparedStatement ps = con.prepareStatement(sql)) {
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
    public List<Sach> getAllForTable(String lastISBNCursor, int pageSize) {
        boolean isFirstPage = (lastISBNCursor == null || lastISBNCursor.isEmpty());
        String sql = isFirstPage ? SQL_GET_FIRST_PAGE : SQL_GET_NEXT_PAGE;
        List<Sach> list = new ArrayList<>();
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            if (isFirstPage) {
                ps.setInt(1, pageSize + 1);
            }else {
                ps.setInt(1, pageSize + 1);
                ps.setString(2, lastISBNCursor);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    public int countTotal() {
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(SQL_COUNT_TOTAL);
            ResultSet rs = ps.executeQuery())
        {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    //Tim kiem theo tieu chi (All/Tac gia/Ten sach/NXB/The Loai/ISBN)
    public List<Sach> search(String keyword, String tieuChi) throws Exception {
        List<Sach> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT S.ISBN, S.TenSach, TG.TenTacGia, NXB.TenNXB, S.NamXuatBan, TL.TenTheLoai ");
        sql.append("FROM SACH AS S ");
        sql.append("LEFT JOIN TACGIA AS TG ON S.MaTacGia = TG.MaTacGia ");
        sql.append("LEFT JOIN NHAXUATBAN AS NXB ON S.MaNXB = NXB.MaNXB ");
        sql.append("LEFT JOIN THELOAI AS TL ON S.MaTheLoai = TL.MaTheLoai ");

        List<String> conditions = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            keyword = "%" + keyword.trim() + "%";
            switch (tieuChi) {
                case "Tất cả":
                    conditions.add("S.TenSach LIKE ?");
                    conditions.add("TG.TenTacGia LIKE ?");
                    conditions.add("NXB.TenNXB LIKE ?");
                    conditions.add("TL.TenTheLoai LIKE ?");
                    conditions.add("S.ISBN LIKE ?");
                    break;
                case "Tên sách": conditions.add("S.TenSach LIKE ?"); break;
                case "Tác giả": conditions.add("TG.TenTacGia LIKE ?"); break;
                case "Nhà xuất bản": conditions.add("NXB.TenNXB LIKE ?"); break;
                case "Thể loại": conditions.add("TL.TenTheLoai LIKE ?"); break;
                case "ISBN": conditions.add("S.ISBN LIKE ?"); break;
            }
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(String.join(" OR ", conditions));
        }

        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) 
        {
            if (!conditions.isEmpty()) {
                for (int i = 0; i < conditions.size(); i++) {
                    ps.setString(i + 1, keyword);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
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
    
    // Kiểm tra ISBN đã tồn tại chưa
    public boolean existsByISBN(String isbn) {
        String sql = "SELECT COUNT(*) FROM Sach WHERE ISBN = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0; // true nếu ISBN đã tồn tại
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false; // nếu có lỗi coi như chưa tồn tại
    }

}
