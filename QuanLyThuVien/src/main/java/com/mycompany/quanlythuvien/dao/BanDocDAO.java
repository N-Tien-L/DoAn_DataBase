package com.mycompany.quanlythuvien.dao;

import com.mycompany.quanlythuvien.exceptions.BanDocException;
import com.mycompany.quanlythuvien.model.BanDoc;
import com.mycompany.quanlythuvien.util.DBConnector;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BanDocDAO {

    // ---------------- SQL constants ----------------
    private static final String SQL_ADD =
        "INSERT INTO BANDOC (HoTen, Email, DiaChi, SDT) VALUES (?, ?, ?, ?)";

    private static final String SQL_READ =
        "SELECT * FROM BANDOC";

    private static final String SQL_DELETE =
        "DELETE FROM BANDOC WHERE IdBD = ?";

    private static final String SQL_UPDATE =
        "UPDATE BANDOC SET HoTen = ?, Email = ?, DiaChi = ?, SDT = ? WHERE IdBD = ?";

    private static final String SQL_GET_BY_ID =
        "SELECT * FROM BANDOC WHERE IdBD = ?";

    private static final String SQL_SOLAN_MUON =
        "SELECT count(*) FROM PHIEUMUON WHERE IdBD = ?";

    private static final String SQL_SOSACH_DANG_MUON =
        "SELECT count(*) FROM PHIEUMUON pm, CT_PM ctpm WHERE pm.IdBD = ? AND pm.IdPM = ctpm.IdPM AND ctpm.NgayTraThucTe IS NULL";

    private static final String SQL_SOSACH_DA_MUON =
        "SELECT count(*) FROM PHIEUMUON pm, CT_PM ctpm WHERE pm.IdBD = ? AND pm.IdPM = ctpm.IdPM";

    private static final String SQL_SOPHIEU_PHAT =
        "SELECT count(*) FROM PHIEUMUON pm, PHAT p WHERE pm.IdBD = ? AND pm.IdPM = p.IdPM";

    private static final String SQL_SOTIEN_PHAT_CHUA_DONG =
        "SELECT COALESCE(SUM(p.SoTien), 0) FROM PHIEUMUON pm JOIN PHAT p ON pm.IdPM = p.IdPM WHERE pm.IdBD = ? AND p.TrangThai = 'Chua dong'";

    private static final String SQL_SOTIEN_PHAT_DA_DONG =
        "SELECT COALESCE(SUM(p.SoTien), 0) FROM PHIEUMUON pm JOIN PHAT p ON pm.IdPM = p.IdPM WHERE pm.IdBD = ? AND p.TrangThai = 'Da dong'";

    private static final String SQL_SOME_INFO_SACH_BY_MABANSAO =
        "SELECT bs.ISBN, s.TenSach, tg.TenTacGia, tl.TenTheLoai, s.NamXuatBan, nxb.TenNXB " +
        "FROM BANSAO bs " +
        "JOIN SACH s ON bs.ISBN = s.ISBN " +
        "JOIN TACGIA tg ON s.MaTacGia = tg.MaTacGia " +
        "JOIN THELOAI tl ON s.MaTheLoai = tl.MaTheLoai " +
        "JOIN NHAXUATBAN nxb ON s.MaNXB = nxb.MaNXB " +
        "WHERE bs.MaBanSao = ?";

    private static final String SQL_SOME_INFO_SACH_BY_IDPHAT =
        "SELECT s.ISBN, s.TenSach, tg.TenTacGia, tl.TenTheLoai, s.NamXuatBan, nxb.TenNXB " +
        "FROM PHAT p " +
        "JOIN BANSAO bs ON bs.MaBanSao = p.MaBanSao " +
        "JOIN SACH s ON bs.ISBN = s.ISBN " +
        "JOIN TACGIA tg ON s.MaTacGia = tg.MaTacGia " +
        "JOIN THELOAI tl ON s.MaTheLoai = tl.MaTheLoai " +
        "JOIN NHAXUATBAN nxb ON s.MaNXB = nxb.MaNXB " +
        "WHERE p.IdPhat = ?";

    private static final String SQL_GET_MABANSAO_BY_IDPHAT =
        "SELECT p.MaBanSao FROM PHAT p WHERE p.IdPhat = ?";

    // PHIEU MUON paging
    private static final String SQL_PHIEU_MUON_PAGE_NOSEARCH =
        "SELECT TOP (%d) pm.IdPM, pm.EmailNguoiLap, pm.NgayMuon, pm.HanTra, " +
        "ct.MaBanSao, ct.NgayTraThucTe, ct.TinhTrangKhiTra, ct.EmailNguoiNhan " +
        "FROM PHIEUMUON pm JOIN CT_PM ct ON pm.IdPM = ct.IdPM " +
        "WHERE pm.IdBD = ? AND (? IS NULL OR pm.IdPM > ?) " +
        "GROUP BY pm.IdPM, pm.EmailNguoiLap, pm.NgayMuon, pm.HanTra, " +
        "         ct.MaBanSao, ct.NgayTraThucTe, ct.TinhTrangKhiTra, ct.EmailNguoiNhan " +
        "ORDER BY pm.IdPM ASC";


    private static final String SQL_PHIEU_MUON_PAGE_SEARCH =
        "SELECT TOP (%d) pm.IdPM, pm.EmailNguoiLap, pm.NgayMuon, pm.HanTra, " +
        "ct.MaBanSao, ct.NgayTraThucTe, ct.TinhTrangKhiTra, ct.EmailNguoiNhan " +
        "FROM PHIEUMUON pm JOIN CT_PM ct ON pm.IdPM = ct.IdPM " +
        "WHERE pm.IdBD = ? AND (pm.EmailNguoiLap LIKE ? OR CAST(pm.IdPM AS VARCHAR(50)) LIKE ? OR CAST(ct.MaBanSao AS VARCHAR(50)) LIKE ?) " +
        "AND (? IS NULL OR pm.IdPM > ?) " +
        "GROUP BY pm.IdPM, pm.EmailNguoiLap, pm.NgayMuon, pm.HanTra, " +
        "         ct.MaBanSao, ct.NgayTraThucTe, ct.TinhTrangKhiTra, ct.EmailNguoiNhan " +
        "ORDER BY pm.IdPM ASC";


    // PHIEU PHAT paging
    private static final String SQL_PHIEU_PHAT_PAGE_NOSEARCH =
        "SELECT TOP (%d) p.IdPhat, pm.IdPM, pm.EmailNguoiLap, pm.NgayMuon, " +
        "p.LoaiPhat, p.SoTien, p.NgayGhiNhan, p.TrangThai " +
        "FROM PHIEUMUON pm JOIN PHAT p ON pm.IdPM = p.IdPM " +
        "WHERE pm.IdBD = ? AND (? IS NULL OR p.IdPhat > ?) " +
        "GROUP BY p.IdPhat, pm.IdPM, pm.EmailNguoiLap, pm.NgayMuon, " +
        "         p.LoaiPhat, p.SoTien, p.NgayGhiNhan, p.TrangThai " +
        "ORDER BY p.IdPhat ASC";
    

    private static final String SQL_PHIEU_PHAT_PAGE_SEARCH =
        "SELECT TOP (%d) p.IdPhat, pm.IdPM, pm.EmailNguoiLap, pm.NgayMuon, " +
        "p.LoaiPhat, p.SoTien, p.NgayGhiNhan, p.TrangThai " +
        "FROM PHIEUMUON pm JOIN PHAT p ON pm.IdPM = p.IdPM " +
        "WHERE pm.IdBD = ? AND (CAST(p.IdPhat AS VARCHAR(50)) LIKE ? OR CAST(pm.IdPM AS VARCHAR(50)) LIKE ? OR pm.EmailNguoiLap LIKE ? OR p.LoaiPhat LIKE ?) " +
        "AND (? IS NULL OR p.IdPhat > ?) " +
        "GROUP BY p.IdPhat, pm.IdPM, pm.EmailNguoiLap, pm.NgayMuon, " +
        "         p.LoaiPhat, p.SoTien, p.NgayGhiNhan, p.TrangThai " +
        "ORDER BY p.IdPhat ASC";




    // SQL constants (giữ nguyên)
    private static final String SQL_BANDOC_PAGE_SEARCH_COLUMN =
        "SELECT TOP (%d) IdBD, HoTen, Email, DiaChi, SDT " +
        "FROM BANDOC " +
        "WHERE %s LIKE ? " +
        "AND (? IS NULL OR IdBD > ?) " +
        "ORDER BY IdBD ASC";

    private static final String SQL_BANDOC_PAGE_SEARCH_ID =
        "SELECT TOP (%d) IdBD, HoTen, Email, DiaChi, SDT " +
        "FROM BANDOC " +
        "WHERE CAST(IdBD AS VARCHAR(50)) LIKE ? " +
        "AND (? IS NULL OR IdBD > ?) " +
        "ORDER BY IdBD ASC";

    // ---------- helpers ----------
    private static String removeControlAndTrim(String s) {
        if (s == null) return null;
        // remove control chars (tabs, newlines, zero-width, etc.)
        s = s.replaceAll("\\p{C}", ""); // \p{C} = invisible/control chars
        // replace NBSP, collapse whitespace
        s = s.replace('\u00A0', ' ');
        s = s.replaceAll("\\s+", " ").trim();
        return s;
    }

    private static String removeDiacritics(String s) {
        if (s == null) return null;
        String tmp = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        return tmp.replaceAll("\\p{M}", "");
    }

    private static String digitsOnly(String s) {
        if (s == null) return "";
        return s.replaceAll("[^0-9]", "");
    }

    // robust normalizeSearchBy: trả về canonical token: ID / HOTEN / EMAIL / SDT / DIACHI
    private static String normalizeSearchByRobust(String raw) {
        if (raw == null) return null;
        // 1) remove control chars & trim
        String t = removeControlAndTrim(raw);
        if (t.isEmpty()) return null;

        // 2) replace Đ/đ with D (explicit)
        t = t.replace('\u0110', 'D').replace('\u0111', 'D');

        // 3) remove diacritics and uppercase
        t = removeDiacritics(t).toUpperCase();

        // 4) remove not-letter/digit so we get a compact token
        String compact = t.replaceAll("[^A-Z0-9]", "");

        // 5) map by 'contains' rules (catch partial/missing letters like "IACHI", "ST", ...)
        if (compact.contains("ID") || compact.contains("IDBD") || compact.contains("MABANDOC") || compact.contains("MASO")) {
            return "ID";
        }
        if (compact.contains("HOTEN") || compact.contains("HOVATEN") || compact.contains("TEN")) {
            return "HOTEN";
        }
        if (compact.contains("EMAIL") || compact.contains("MAIL")) {
            return "EMAIL";
        }
        // phone: accept SDT, SĐT, DT, PHONE and common short forms like ST or single letters often used
        // use equals/contains to catch "ST", "SĐT", "SDT", "S D T", "DT", "PHONE"
        if (compact.contains("SDT") || compact.contains("DT") || compact.contains("PHONE") || compact.equals("ST") || compact.contains("ST")) {
            return "SDT";
        }
        // address: DIACHI or parts like IACHI, CHI, ADDRESS
        if (compact.contains("DIACHI") || compact.contains("IACHI") || compact.contains("CHI") || compact.contains("ADDRESS")) {
            return "DIACHI";
        }

        // fallback: return compact (caller can choose default)
        return compact;
    }

    // normalize search text for LIKE
    private static String normalizeSearchText(String raw) {
        if (raw == null) return null;
        String t = removeControlAndTrim(raw);
        return t;
    }

    // ---------- main method ----------
    public List<BanDoc> getPageById(int limit, String searchBy, String searchText, Integer lastId) throws SQLException, Exception {
        List<BanDoc> result = new ArrayList<>();

        String token = normalizeSearchByRobust(searchBy); // robust token
        if (token == null) token = "HOTEN"; // default fallback

        boolean isId = "ID".equals(token);
        boolean isHoten = "HOTEN".equals(token);
        boolean isEmail = "EMAIL".equals(token);
        boolean isSdt = "SDT".equals(token);
        boolean isDiaChi = "DIACHI".equals(token);

        boolean hasSearch = (searchText != null && !searchText.trim().isEmpty());
        String cleanedText = hasSearch ? normalizeSearchText(searchText) : "";

        String sql;
        String col = null;

        if (isId) {
            sql = String.format(SQL_BANDOC_PAGE_SEARCH_ID, limit);
        } else {
            if (isHoten) col = "HoTen";
            else if (isEmail) col = "Email";
            else if (isSdt) {
                // DB-side normalize: remove spaces, dashes, parentheses so digits-only LIKE works.
                // Nếu DB cột SDT chứa dấu +, bạn có thể thêm REPLACE(SDT, '+', '') nữa.
                col = "REPLACE(REPLACE(REPLACE(REPLACE(SDT, ' ', ''), '-', ''), '(', ''), ')', '')";
            }
            else if (isDiaChi) col = "DiaChi";
            else col = "HoTen"; // fallback

            sql = String.format(SQL_BANDOC_PAGE_SEARCH_COLUMN, limit, col);
        }

        // prepare param for LIKE
        String likeParam;
        if (!hasSearch) {
            likeParam = "%";
        } else if (isSdt) {
            // for phone, use digits-only matching (DB side normalized via REPLACE)
            String digits = digitsOnly(cleanedText);
            if (digits.isEmpty()) {
                // nếu user nhập toàn ký tự đặc biệt mà không có số nào -> không tìm gì
                likeParam = "%"; // hoặc bạn có thể chọn return empty list; mình giữ wildcard để show all
            } else {
                likeParam = "%" + digits + "%";
            }
        } else {
            likeParam = "%" + cleanedText + "%";
        }

        // DEBUG logs (remove in production)
        System.out.println("DEBUG normalizeSearchByRobust(raw)='" + searchBy + "' -> token=" + token + " col=" + col + " isId=" + isId);
        System.out.println("DEBUG cleanedText='" + cleanedText + "' likeParam='" + likeParam + "' lastId=" + lastId);
        System.out.println("DEBUG SQL: " + sql);

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // bind params
            ps.setString(1, likeParam);
            if (lastId == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, lastId);
                ps.setInt(3, lastId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BanDoc b = new BanDoc();
                    b.setIdBD(rs.getInt("IdBD"));
                    b.setHoTen(rs.getString("HoTen"));
                    b.setEmail(rs.getString("Email"));
                    b.setDiaChi(rs.getString("DiaChi"));
                    b.setSdt(rs.getString("SDT"));
                    result.add(b);
                }
            }
        }

        return result;
    }


    // ---------------------- Paging & list methods ----------------------

    public ArrayList<Object> getPhieuMuonPageByBanDoc(int idBD, int pageSizeRequest, Integer lastIdPM, String searchText) throws Exception {
        ArrayList<Object> result = new ArrayList<>();

        boolean hasSearch = (searchText != null && !searchText.trim().isEmpty());
        String sql = hasSearch
            ? String.format(SQL_PHIEU_MUON_PAGE_SEARCH, pageSizeRequest)
            : String.format(SQL_PHIEU_MUON_PAGE_NOSEARCH, pageSizeRequest);

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            ps.setInt(idx++, idBD);

            if (hasSearch) {
                String like = "%" + searchText.trim() + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }

            if (lastIdPM == null) {
                ps.setNull(idx++, java.sql.Types.INTEGER);
                ps.setNull(idx++, java.sql.Types.INTEGER);
            } else {
                ps.setInt(idx++, lastIdPM);
                ps.setInt(idx++, lastIdPM);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt("IdPM"));
                    result.add(rs.getString("EmailNguoiLap"));
                    result.add(rs.getDate("NgayMuon"));
                    result.add(rs.getDate("HanTra"));
                    int maBanSao = rs.getInt("MaBanSao");
                    if (rs.wasNull()) result.add(null); else result.add(maBanSao);
                    result.add(rs.getDate("NgayTraThucTe"));
                    result.add(rs.getString("TinhTrangKhiTra"));
                    result.add(rs.getString("EmailNguoiNhan"));
                }
            }
        }
        return result;
    }

    public ArrayList<Object> getPhieuPhatPageByBanDoc(int idBD, int pageSizeRequest, Integer lastIdPhat, String searchText) throws Exception {
        ArrayList<Object> result = new ArrayList<>();

        boolean hasSearch = (searchText != null && !searchText.trim().isEmpty());
        String sql = hasSearch
            ? String.format(SQL_PHIEU_PHAT_PAGE_SEARCH, pageSizeRequest)
            : String.format(SQL_PHIEU_PHAT_PAGE_NOSEARCH, pageSizeRequest);

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            ps.setInt(idx++, idBD);

            if (hasSearch) {
                String like = "%" + searchText.trim() + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }

            if (lastIdPhat == null) {
                ps.setNull(idx++, java.sql.Types.INTEGER);
                ps.setNull(idx++, java.sql.Types.INTEGER);
            } else {
                ps.setInt(idx++, lastIdPhat);
                ps.setInt(idx++, lastIdPhat);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt("IdPhat"));
                    result.add(rs.getInt("IdPM"));
                    result.add(rs.getString("EmailNguoiLap"));
                    result.add(rs.getDate("NgayMuon"));
                    result.add(rs.getString("LoaiPhat"));
                    result.add(rs.getDouble("SoTien"));
                    result.add(rs.getDate("NgayGhiNhan"));
                    result.add(rs.getString("TrangThai"));
                }
            }
        }
        return result;
    }


    // ---------------------- CRUD + helpers ----------------------

    public Boolean deleteDAO(BanDoc cur) throws Exception {
        if (cur == null) return false;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, cur.getIdBD());
            int affected = ps.executeUpdate();
            return affected > 0;

        } catch (SQLException ex) {
            Map<String, String> violations = parseConstraintExceptionOnDelete(ex);
            if (!violations.isEmpty()) {
                throw new BanDocException(violations);
            }
            throw ex;
        }
    }

    public Boolean updateDAO(BanDoc cur) throws Exception {
        if (cur == null) return false;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, cur.getHoTen());
            ps.setString(2, cur.getEmail());
            ps.setString(3, cur.getDiaChi());
            ps.setString(4, cur.getSdt());
            ps.setInt(5, cur.getIdBD());

            int affected = ps.executeUpdate();
            return affected > 0;

        } catch (SQLException ex) {
            Map<String, String> violations = parseConstraintException(ex);
            if (!violations.isEmpty()) {
                throw new BanDocException(violations);
            }
            throw ex;
        }
    }

    public Boolean addDAO(BanDoc cur) throws Exception {
        if (cur == null) return false;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_ADD, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cur.getHoTen());
            ps.setString(2, cur.getEmail());
            ps.setString(3, cur.getDiaChi());
            ps.setString(4, cur.getSdt());

            int affected = ps.executeUpdate();
            if (affected == 0) return false;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    cur.setIdBD(keys.getInt(1));
                }
            }

            return true;

        } catch (SQLException ex) {
            Map<String, String> violations = parseConstraintException(ex);
            if (!violations.isEmpty()) {
                throw new BanDocException(violations);
            }
            throw ex;
        }
    }

    public Boolean readDAO(ArrayList<BanDoc> dsBanDoc) throws Exception {
        if (dsBanDoc == null) return false;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_READ);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                BanDoc bd = new BanDoc();
                bd.setIdBD(rs.getInt("IdBD"));
                bd.setHoTen(rs.getString("HoTen"));
                bd.setEmail(rs.getString("Email"));
                bd.setDiaChi(rs.getString("DiaChi"));
                bd.setSdt(rs.getString("SDT"));
                dsBanDoc.add(bd);
            }
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public BanDoc getBanDocById(int id) throws Exception {
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BanDoc bd = new BanDoc();
                    bd.setIdBD(rs.getInt("IdBD"));
                    bd.setHoTen(rs.getString("HoTen"));
                    bd.setEmail(rs.getString("Email"));
                    bd.setDiaChi(rs.getString("DiaChi"));
                    bd.setSdt(rs.getString("SDT"));
                    return bd;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public BanDoc findByEmail(String email) throws Exception {
        if (email == null || email.trim().isEmpty()) return null;

        String sql = "SELECT * FROM BANDOC WHERE Email = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BanDoc bd = new BanDoc();
                    bd.setIdBD(rs.getInt("IdBD"));
                    bd.setHoTen(rs.getString("HoTen"));
                    bd.setEmail(rs.getString("Email"));
                    bd.setDiaChi(rs.getString("DiaChi"));
                    bd.setSdt(rs.getString("SDT"));
                    return bd;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw ex;
        }
        return null;
    }

    // ---------------------- Aggregate helpers ----------------------

    public int getSoLanMuonCuaBanDoc(int IdBD) throws Exception {
        int soPhieu = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SOLAN_MUON)) {

            ps.setInt(1, IdBD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) soPhieu = rs.getInt(1);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return soPhieu;
    }

    public int getSoSachDangMuonCuaBanDoc(int IdBD) throws Exception {
        int ans = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SOSACH_DANG_MUON)) {

            ps.setInt(1, IdBD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) ans = rs.getInt(1);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return ans;
    }

    public int getSoSachDaMuonCuaBanDoc(int IdBD) throws Exception {
        int ans = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SOSACH_DA_MUON)) {

            ps.setInt(1, IdBD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) ans = rs.getInt(1);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return ans;
    }

    public int getSoPhieuPhatBanDoc(int IdBD) throws Exception {
        int ans = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SOPHIEU_PHAT)) {

            ps.setInt(1, IdBD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) ans = rs.getInt(1);
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return ans;
    }

    public int getSoTienPhatChuaDongBanDoc(int IdBD) throws Exception {
        int ans = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SOTIEN_PHAT_CHUA_DONG)) {

            ps.setInt(1, IdBD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) ans = (int) Math.round(rs.getDouble(1));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return ans;
    }

    public int getSoTienPhatDaDongBanDoc(int IdBD) throws Exception {
        int ans = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SOTIEN_PHAT_DA_DONG)) {

            ps.setInt(1, IdBD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) ans = (int) Math.round(rs.getDouble(1));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        return ans;
    }

    public ArrayList<Object> getSomeInfoSachByMaBanSao(int maBanSao) {
        ArrayList<Object> ans = new ArrayList<>();
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SOME_INFO_SACH_BY_MABANSAO)) {

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
        } catch (Exception ex) { ex.printStackTrace(); }
        return ans;
    }

    public ArrayList<Object> getSomeInfoSachByIdPhat(int IdPhat) {
        ArrayList<Object> ans = new ArrayList<>();
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SOME_INFO_SACH_BY_IDPHAT)) {

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
        } catch (Exception ex) { ex.printStackTrace(); }
        return ans;
    }

    public int getMaBanSaoByIdPhat(int IdPhat) {
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_MABANSAO_BY_IDPHAT)) {

            ps.setInt(1, IdPhat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("MaBanSao");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return Integer.MIN_VALUE;
    }

    // ---------------------- Constraint parsers ----------------------

    private Map<String,String> parseConstraintException(SQLException ex) {
        Map<String,String> map = new HashMap<>();
        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();

        if (msg.contains("cannot insert the value null") && msg.contains("hoten")) {
            map.put("HoTen", "Họ Tên không được để trống.");
        }
        if (msg.contains("email") && (msg.contains("unique") || msg.contains("uq") || msg.contains("duplicate"))) {
            map.put("Email", "Email đã tồn tại (phải là duy nhất).");
        } else if (msg.contains("email") && msg.contains("varchar") && msg.contains("too long")) {
            map.put("Email", "Email vượt quá độ dài tối đa.");
        }
        if (msg.contains("sdt") && (msg.contains("unique") || msg.contains("uq") || msg.contains("duplicate"))) {
            map.put("SDT", "Số điện thoại đã tồn tại (phải là duy nhất).");
        }
        if (msg.contains("fk_bandoc_createdby") || (msg.contains("createdby") && msg.contains("reference"))) {
            map.put("CreatedBy", "Người tạo (CreatedBy) không tồn tại trong bảng TAIKHOAN.");
        }

        String sqlState = ex.getSQLState();
        if ((sqlState != null && sqlState.startsWith("23")) && map.isEmpty()) {
            map.put("database", "Ràng buộc dữ liệu vi phạm: " + ex.getMessage());
        }

        return map;
    }

    private Map<String,String> parseConstraintExceptionOnDelete(SQLException ex) {
        Map<String,String> map = new HashMap<>();
        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        if (msg.contains("reference") || msg.contains("conflicted with the reference constraint") || msg.contains("foreign key")) {
            map.put("delete", "Không thể xóa bạn đọc này vì có tham chiếu (ví dụ: phiếu mượn/phiếu phạt). Hãy xóa/tách các bản ghi liên quan trước.");
        }
        if (!map.isEmpty()) return map;
        return parseConstraintException(ex);
    }
}
