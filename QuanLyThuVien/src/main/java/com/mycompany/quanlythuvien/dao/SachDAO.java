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
 * @author Thanh
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
    
    // them sach
    public boolean insert(Sach s, String createdBy) throws Exception {
        // SoLuongTon được quản lý tự động bởi trigger TRG_BANSAO_Update_SoLuongTon
        String sql = "INSERT INTO SACH (ISBN, TenSach, MaTacGia, MaTheLoai, NamXuatBan, DinhDang, MoTa, MaNXB, GiaBia, SoTrang, CreatedBy) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        // Mở kết nối CSDL bằng DBConnector (sử dụng try-with-resources để tự đóng kết nối sau khi dùng)
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            //Gán giá trị vào các tham số của câu lệnh INSERT
            ps.setString(1, s.getISBN()); //Chuỗi, không null
            ps.setString(2, s.getTenSach()); //Chuỗi, không null
            ps.setObject(3, s.getMaTacGia(), java.sql.Types.INTEGER); //Có thể null
            ps.setObject(4, s.getMaTheLoai(), java.sql.Types.INTEGER); //Có thể null
            ps.setObject(5, s.getNamXuatBan(), java.sql.Types.INTEGER); //Có thể null
            ps.setString(6, s.getDinhDang()); //Chuỗi, định dạng sách
            ps.setString(7, s.getMoTa()); //Chuỗi, mô tả sách
            ps.setObject(8, s.getMaNXB(), java.sql.Types.INTEGER); //Có thể null
            ps.setBigDecimal(9, s.getGiaBia()); //Số thực chính xác, giá bìa
            ps.setObject(10, s.getSoTrang(), java.sql.Types.INTEGER); //Có thể null
            ps.setString(11, createdBy); //Email người tạo

            //Thực thi câu lệnh INSERT, trả về true nếu thêm thành công
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException ex) {
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
    
    // Helper: Format từ khóa cho Full-Text Search
    private String getFtsQuery(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }

        // 1. Xóa ký tự đặc biệt có thể gây lỗi syntax cho CONTAINS
        String cleanKeyword = keyword.trim().replaceAll("['\"]", "");

        // 2. Tách từ
        String[] words = cleanKeyword.split("\\s+");
        
        if (words.length == 0) return null;

        StringBuilder queryBuilder = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            
            // Bỏ qua từ quá ngắn
            if (word.length() < 2) continue; 

            if (queryBuilder.length() > 0) {
                queryBuilder.append(" AND ");
            }

            // Bọc trong ngoặc kép để FTS hiểu là một cụm từ (literal)
            // Thêm * để tìm kiếm kiểu Prefix
            queryBuilder.append("\"").append(word).append("*\"");
        }

        return queryBuilder.toString().isEmpty() ? null : queryBuilder.toString();
    }

    /**
     * Tìm kiếm sách
     *
     * @param keyword       Từ khóa tìm kiếm trên Tên sách, Mô tả, Tên tác giả (sử dụng Full-Text Search).
     * @param maTheLoai     ID của thể loại để lọc.
     * @param maNXB         ID của nhà xuất bản để lọc.
     * @param maTacGia      ID của tác giả để lọc.
     * @param namBatDau     Năm xuất bản bắt đầu của khoảng tìm kiếm.
     * @param namKetThuc    Năm xuất bản kết thúc của khoảng tìm kiếm.
     * @param pageNumber    Trang hiện tại (bắt đầu từ 1).
     * @param pageSize      Số lượng kết quả tối đa trên một trang.
     * @return Danh sách sách thỏa mãn điều kiện.
     */
    public List<Sach> search(String keyword, Integer maTheLoai, 
                             Integer maNXB, Integer maTacGia, Integer namBatDau, 
                             Integer namKetThuc, int pageNumber, int pageSize) {
        List<Sach> books = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();

        // xử lý keyword
        String ftsKeyword = getFtsQuery(keyword);
        boolean hasSearch = (ftsKeyword != null);

        // SELECT
        sql.append("SELECT S.ISBN, S.TenSach, TG.TenTacGia, NXB.TenNXB, S.NamXuatBan, TL.TenTheLoai ");

        // nếu có search, select thêm score để log debug
        if(hasSearch) {
            sql.append(", (ISNULL(K_S.RANK, 0) + ISNULL(K_TG.RANK, 0)) AS Score ");
        }

        // join các bảng
        sql.append("FROM SACH AS S ");
        sql.append("LEFT JOIN TACGIA AS TG ON S.MaTacGia = TG.MaTacGia ");
        sql.append("LEFT JOIN NHAXUATBAN AS NXB ON S.MaNXB = NXB.MaNXB ");
        sql.append("LEFT JOIN THELOAI AS TL ON S.MaTheLoai = TL.MaTheLoai ");

        // join thêm CONTAINSTABLE nếu có search
        if (hasSearch) {
            // Join FTS Sách
            sql.append("LEFT JOIN CONTAINSTABLE(SACH, TenSach, ?) AS K_S ON S.ISBN = K_S.[KEY] ");
            params.add(ftsKeyword);
            
            // Join FTS Tác giả
            sql.append("LEFT JOIN CONTAINSTABLE(TACGIA, TenTacGia, ?) AS K_TG ON TG.MaTacGia = K_TG.[KEY] ");
            params.add(ftsKeyword);
        }

        sql.append("WHERE 1=1 "); // để nối các AND phía sau

        // điều kiện search
        if (hasSearch) {
            // Phải tìm thấy ở ít nhất 1 trong 2 bảng
            sql.append("AND (K_S.RANK IS NOT NULL OR K_TG.RANK IS NOT NULL) ");
        }

        // điều kiện filter
        if (maTheLoai != null && maTheLoai > 0) {
            sql.append(" AND S.MaTheLoai = ?");
            params.add(maTheLoai);
        }
        if (maNXB != null && maNXB > 0) {
            sql.append(" AND S.MaNXB = ?");
            params.add(maNXB);
        }
        if (maTacGia != null && maTacGia > 0) {
            sql.append(" AND S.MaTacGia = ?");
            params.add(maTacGia);
        }

        // Thêm điều kiện lọc theo khoảng năm (Range Picker)
        if (namBatDau != null && namBatDau > 0) {
            sql.append(" AND S.NamXuatBan >= ?");
            params.add(namBatDau);
        }
        if (namKetThuc != null && namKetThuc > 0) {
            sql.append(" AND S.NamXuatBan <= ?");
            params.add(namKetThuc);
        }

        // Order by để sắp xếp kết quả theo "điểm" trùng khớp
        if (hasSearch) {
            // Ưu tiên điểm cao nhất xếp trước
            sql.append("ORDER BY (ISNULL(K_S.RANK, 0) + ISNULL(K_TG.RANK, 0)) DESC, S.ISBN ASC ");
        } else {
            // Nếu không search từ khóa, xếp theo ngày tạo
            sql.append("ORDER BY S.CreatedAt DESC ");
        }

        // Pagination bằng OFFSET - FETCH
        // Sắp xếp theo rank thì không dùng cursor based được
        sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        int offset = (pageNumber - 1) * pageSize; // Trang 1 -> offset 0
        params.add(offset);
        params.add(pageSize);


        // Execute
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            // Tự động gán tham số vào PreparedStatement
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
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
                    books.add(s);
                }
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
    
}
