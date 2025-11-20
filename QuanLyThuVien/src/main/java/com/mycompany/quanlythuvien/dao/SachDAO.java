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
        SELECT TOP (?) S.ISBN, S.TenSach, TG.TenTacGia, NXB.TenNXB, S.NamXuatBan, TL.TenTheLoai,
                        S.CreatedAt, S.CreatedBy
        FROM SACH AS S
        LEFT JOIN TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
        LEFT JOIN NHAXUATBAN AS NXB ON S.MaNXB = NXB.MaNXB
        LEFT JOIN THELOAI AS TL ON S.MaTheLoai = TL.MaTheLoai
        ORDER BY S.ISBN ASC                            
    """;

    private static final String SQL_GET_NEXT_PAGE = """
        SELECT TOP (?) S.ISBN, S.TenSach, TG.TenTacGia, NXB.TenNXB, S.NamXuatBan, TL.TenTheLoai,
                        S.CreatedAt, S.CreatedBy
        FROM SACH AS S
        LEFT JOIN TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
        LEFT JOIN NHAXUATBAN AS NXB ON S.MaNXB = NXB.MaNXB
        LEFT JOIN THELOAI AS TL ON S.MaTheLoai = TL.MaTheLoai
        WHERE S.ISBN > ?
        ORDER BY S.ISBN ASC
    """;
    
    private static final String SQL_COUNT_TOTAL = "SELECT COUNT(*) FROM SACH";

    private static final String SQL_SEARCH_BOOKS = """
        SELECT TOP (?) S.ISBN, S.TenSach, TG.TenTacGia, NXB.TenNXB, S.NamXuatBan, TL.TenTheLoai
        FROM SACH AS S
        LEFT JOIN TACGIA AS TG ON S.MaTacGia = TG.MaTacGia
        LEFT JOIN NHAXUATBAN AS NXB ON S.MaNXB = NXB.MaNXB
        LEFT JOIN THELOAI AS TL ON S.MaTheLoai = TL.MaTheLoai
        WHERE (
            (? = 'Tất cả' AND (S.TenSach LIKE ? OR TG.TenTacGia LIKE ? OR NXB.TenNXB LIKE ? OR TL.TenTheLoai LIKE ? OR S.ISBN LIKE ?))
            OR (? = 'Tên sách' AND S.TenSach LIKE ?)
            OR (? = 'Tác giả' AND TG.TenTacGia LIKE ?)
            OR (? = 'Nhà xuất bản' AND NXB.TenNXB LIKE ?)
            OR (? = 'Thể loại' AND TL.TenTheLoai LIKE ?)
            OR (? = 'ISBN' AND S.ISBN LIKE ?)
        )
        AND (? IS NULL OR S.ISBN > ?)                                              
        ORDER BY S.ISBN ASC
    """;
    
    // them sach
    public boolean insert(Sach s, String createdBy) throws Exception {
        // SoLuongTon được quản lý tự động bởi trigger TRG_BANSAO_Update_SoLuongTon
        String sql = "INSERT INTO SACH (ISBN, TenSach, MaTacGia, MaTheLoai, NamXuatBan, DinhDang, MoTa, MaNXB, GiaBia, SoTrang, CreatedBy) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

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
            ps.setString(11, createdBy);
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
                        rs.getObject("SoTrang", Integer.class),
                            
                        rs.getTimestamp("CreatedAt"),
                        rs.getString("CreatedBy")
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
                ps.setInt(1, pageSize);
            }else {
                ps.setInt(1, pageSize);
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
                    s.setCreatedAt(rs.getTimestamp("CreatedAt"));
                    s.setCreatedBy(rs.getString("CreatedBy"));
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
    public List<Sach> search(String keyword, String tieuChi, String lastISBNCursor, int pageSize) {
        List<Sach> books = new ArrayList<>();
        String searchKeyword = (keyword == null ? "" : keyword.trim());
        String likePattern = "%" + searchKeyword + "%";

        String sql = null;
        switch (tieuChi) {
            case "Tất cả":
                sql = """
                    SELECT TOP (?) S.ISBN, S.TenSach, TG.TenTacGia, NXB.TenNXB, S.NamXuatBan, TL.TenTheLoai
                    FROM SACH S
                    LEFT JOIN TACGIA TG ON S.MaTacGia = TG.MaTacGia
                    LEFT JOIN NHAXUATBAN NXB ON S.MaNXB = NXB.MaNXB
                    LEFT JOIN THELOAI TL ON S.MaTheLoai = TL.MaTheLoai
                    WHERE (S.TenSach LIKE ? OR TG.TenTacGia LIKE ? OR NXB.TenNXB LIKE ? OR TL.TenTheLoai LIKE ? OR S.ISBN LIKE ? OR CAST(S.NamXuatBan AS VARCHAR) LIKE ?)
                      AND (? IS NULL OR S.ISBN > ?)
                    ORDER BY S.ISBN ASC
                """;
                break;
            case "Tên sách":
                sql = """
                    SELECT TOP (?) S.ISBN, S.TenSach, TG.TenTacGia, NXB.TenNXB, S.NamXuatBan, TL.TenTheLoai
                    FROM SACH S
                    LEFT JOIN TACGIA TG ON S.MaTacGia = TG.MaTacGia
                    LEFT JOIN NHAXUATBAN NXB ON S.MaNXB = NXB.MaNXB
                    LEFT JOIN THELOAI TL ON S.MaTheLoai = TL.MaTheLoai
                    WHERE S.TenSach LIKE ?
                      AND (? IS NULL OR S.ISBN > ?)
                    ORDER BY S.ISBN ASC
                """;
                break;
            case "Tác giả":
                sql = """
                    SELECT TOP (?) S.ISBN, S.TenSach, TG.TenTacGia, NXB.TenNXB, S.NamXuatBan, TL.TenTheLoai
                    FROM SACH S
                    LEFT JOIN TACGIA TG ON S.MaTacGia = TG.MaTacGia
                    LEFT JOIN NHAXUATBAN NXB ON S.MaNXB = NXB.MaNXB
                    LEFT JOIN THELOAI TL ON S.MaTheLoai = TL.MaTheLoai
                    WHERE TG.TenTacGia LIKE ?
                      AND (? IS NULL OR S.ISBN > ?)
                    ORDER BY S.ISBN ASC
                """;
                break;
            case "Nhà xuất bản":
                sql = """
                    SELECT TOP (?) S.ISBN, S.TenSach, TG.TenTacGia, NXB.TenNXB, S.NamXuatBan, TL.TenTheLoai
                    FROM SACH S
                    LEFT JOIN TACGIA TG ON S.MaTacGia = TG.MaTacGia
                    LEFT JOIN NHAXUATBAN NXB ON S.MaNXB = NXB.MaNXB
                    LEFT JOIN THELOAI TL ON S.MaTheLoai = TL.MaTheLoai
                    WHERE NXB.TenNXB LIKE ?
                      AND (? IS NULL OR S.ISBN > ?)
                    ORDER BY S.ISBN ASC
                """;
                break;
            case "Thể loại":
                sql = """
                    SELECT TOP (?) S.ISBN, S.TenSach, TG.TenTacGia, NXB.TenNXB, S.NamXuatBan, TL.TenTheLoai
                    FROM SACH S
                    LEFT JOIN TACGIA TG ON S.MaTacGia = TG.MaTacGia
                    LEFT JOIN NHAXUATBAN NXB ON S.MaNXB = NXB.MaNXB
                    LEFT JOIN THELOAI TL ON S.MaTheLoai = TL.MaTheLoai
                    WHERE TL.TenTheLoai LIKE ?
                      AND (? IS NULL OR S.ISBN > ?)
                    ORDER BY S.ISBN ASC
                """;
                break;
            case "ISBN":
                sql = """
                    SELECT TOP (?) S.ISBN, S.TenSach, TG.TenTacGia, NXB.TenNXB, S.NamXuatBan, TL.TenTheLoai
                    FROM SACH S
                    LEFT JOIN TACGIA TG ON S.MaTacGia = TG.MaTacGia
                    LEFT JOIN NHAXUATBAN NXB ON S.MaNXB = NXB.MaNXB
                    LEFT JOIN THELOAI TL ON S.MaTheLoai = TL.MaTheLoai
                    WHERE S.ISBN LIKE ?
                      AND (? IS NULL OR S.ISBN > ?)
                    ORDER BY S.ISBN ASC
                """;
                break;
            case "Năm":
                sql = """
                    SELECT TOP (?) S.ISBN, S.TenSach, TG.TenTacGia, NXB.TenNXB, S.NamXuatBan, TL.TenTheLoai
                    FROM SACH S
                    LEFT JOIN TACGIA TG ON S.MaTacGia = TG.MaTacGia
                    LEFT JOIN NHAXUATBAN NXB ON S.MaNXB = NXB.MaNXB
                    LEFT JOIN THELOAI TL ON S.MaTheLoai = TL.MaTheLoai
                    WHERE CAST(S.NamXuatBan AS VARCHAR) LIKE ?
                      AND (? IS NULL OR S.ISBN > ?)
                    ORDER BY S.ISBN ASC
                """;
                break;
            default:
                return books; // ko tìm tiêu chí lạ
        }

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            ps.setInt(idx++, pageSize);

            if ("Tất cả".equals(tieuChi)) {
                ps.setString(idx++, likePattern); // S.TenSach
                ps.setString(idx++, likePattern); // TG.TenTacGia
                ps.setString(idx++, likePattern); // NXB.TenNXB
                ps.setString(idx++, likePattern); // TL.TenTheLoai
                ps.setString(idx++, likePattern); // S.ISBN
                ps.setString(idx++, likePattern); // S.NamXuatBan
            } else {
                ps.setString(idx++, likePattern);
            }

            // Cursor pagination
            if (lastISBNCursor == null || lastISBNCursor.isEmpty()) {
                ps.setNull(idx++, java.sql.Types.VARCHAR);
                ps.setNull(idx++, java.sql.Types.VARCHAR);
            } else {
                ps.setString(idx++, lastISBNCursor);
                ps.setString(idx++, lastISBNCursor);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Sach s = new Sach();
                s.setISBN(rs.getString("ISBN"));
                s.setTenSach(rs.getString("TenSach"));
                s.setTenTacGia(rs.getString("TenTacGia"));
                s.setTenNXB(rs.getString("TenNXB"));
                s.setNamXuatBan(rs.getInt("NamXuatBan"));
                s.setTenTheLoai(rs.getString("TenTheLoai"));
                books.add(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return books;
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
    
    public ArrayList<Object> getSomeInfoSachByMaBanSao(int maBanSao) { // lede vibe coding
        String sql = "SELECT "
                + "bs.ISBN, "
                + "s.TenSach, "
                + "tg.TenTacGia, "
                + "tl.TenTheLoai, "
                + "s.NamXuatBan, "
                + "nxb.TenNXB\n" +
                "FROM BANSAO bs\n" +
                "JOIN SACH s ON bs.ISBN = s.ISBN\n" +
                "JOIN TACGIA tg ON s.MaTacGia = tg.MaTacGia\n" +
                "JOIN THELOAI tl ON s.MaTheLoai = tl.MaTheLoai\n" +
                "JOIN NHAXUATBAN nxb ON s.MaNXB = nxb.MaNXB\n" +
                "WHERE bs.MaBanSao = ?;";
        ArrayList<Object> ans = new ArrayList<Object>();
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, maBanSao);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ans.add(rs.getString("ISBN"));
                    ans.add(rs.getString("TenSach"));
                    ans.add(rs.getString("TenTacGia"));
                    ans.add(rs.getString("TenTheLoai"));
                    ans.add(rs.getString("TenNXB"));
                    ans.add(rs.getInt("NamXuatBan"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ans;
    }
    public ArrayList<Object> getSomeInfoSachByIdPhat(int IdPhat) { // lede vibe coding
        String sql = "SELECT "
                + "s.ISBN, "
                + "s.TenSach, "
                + "tg.TenTacGia, "
                + "tl.TenTheLoai, "
                + "s.NamXuatBan, "
                + "nxb.TenNXB\n" +
                "FROM PHAT p\n" +
                "JOIN BANSAO bs ON bs.MaBanSao = p.MaBanSao\n" +
                "JOIN SACH s ON bs.ISBN = s.ISBN\n" +
                "JOIN TACGIA tg ON s.MaTacGia = tg.MaTacGia\n" +
                "JOIN THELOAI tl ON s.MaTheLoai = tl.MaTheLoai\n" +
                "JOIN NHAXUATBAN nxb ON s.MaNXB = nxb.MaNXB\n" +
                "WHERE p.IdPhat = ?;";
        ArrayList<Object> ans = new ArrayList<Object>();
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, IdPhat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ans.add(rs.getString("ISBN"));
                    ans.add(rs.getString("TenSach"));
                    ans.add(rs.getString("TenTacGia"));
                    ans.add(rs.getString("TenTheLoai"));
                    ans.add(rs.getString("TenNXB"));
                    ans.add(rs.getInt("NamXuatBan"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ans;
    }
    
    public int getMaBanSaoByIdPhat(int IdPhat) { // lede vibe coding
        String sql = "SELECT p.MaBanSao\n"
                + "FROM PHAT p\n"
                + "WHERE p.IdPhat = ?;";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, IdPhat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("MaBanSao");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Integer.MIN_VALUE;
    }
}
