package com.mycompany.quanlythuvien.dao;

import com.mycompany.quanlythuvien.exceptions.BanDocException;
import com.mycompany.quanlythuvien.model.BanDoc;
import com.mycompany.quanlythuvien.util.DBConnector;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BanDocDAO {
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

    private static final String SQL_GET_ALL_PHIEUMUON_BANDOC =
        "SELECT pm.IdPM, pm.EmailNguoiLap, pm.NgayMuon, pm.HanTra, " +
        "ct.MaBanSao, ct.NgayTraThucTe, ct.TinhTrangKhiTra, ct.EmailNguoiNhan " +
        "FROM PHIEUMUON pm " +
        "JOIN CT_PM ct ON pm.IdPM = ct.IdPM " +
        "WHERE pm.IdBD = ? " +
        "ORDER BY pm.NgayMuon DESC";

    private static final String SQL_GET_ALL_PHIEUPHAT_BANDOC =
        "SELECT p.IdPhat, pm.IdPM, pm.EmailNguoiLap, pm.NgayMuon, " +
        "p.LoaiPhat, p.SoTien, p.NgayGhiNhan, p.TrangThai " +
        "FROM PHIEUMUON pm " +
        "JOIN PHAT p ON pm.IdPM = p.IdPM " +
        "WHERE pm.IdBD = ? " +
        "ORDER BY p.NgayGhiNhan DESC";
    // PHIEU MUON
    private static final String SQL_PHIEU_MUON_PAGE_NOSEARCH =
        "SELECT TOP (%d) pm.IdPM, pm.EmailNguoiLap, pm.NgayMuon, pm.HanTra, " +
        "ct.MaBanSao, ct.NgayTraThucTe, ct.TinhTrangKhiTra, ct.EmailNguoiNhan " +
        "FROM PHIEUMUON pm LEFT JOIN CT_PM ct ON pm.IdPM = ct.IdPM " +
        "WHERE pm.IdBD = ? AND (? IS NULL OR pm.IdPM > ?) " +
        "ORDER BY pm.IdPM ASC";

    private static final String SQL_PHIEU_MUON_PAGE_SEARCH =
        "SELECT TOP (%d) pm.IdPM, pm.EmailNguoiLap, pm.NgayMuon, pm.HanTra, " +
        "ct.MaBanSao, ct.NgayTraThucTe, ct.TinhTrangKhiTra, ct.EmailNguoiNhan " +
        "FROM PHIEUMUON pm LEFT JOIN CT_PM ct ON pm.IdPM = ct.IdPM " +
        "WHERE pm.IdBD = ? AND (pm.EmailNguoiLap LIKE ? OR CAST(pm.IdPM AS VARCHAR(50)) LIKE ? OR CAST(ct.MaBanSao AS VARCHAR(50)) LIKE ?) " +
        "AND (? IS NULL OR pm.IdPM > ?) " +
        "ORDER BY pm.IdPM ASC";

    // PHIEU PHAT
    private static final String SQL_PHIEU_PHAT_PAGE_NOSEARCH =
        "SELECT TOP (%d) p.IdPhat, pm.IdPM, pm.EmailNguoiLap, pm.NgayMuon, " +
        "p.LoaiPhat, p.SoTien, p.NgayGhiNhan, p.TrangThai " +
        "FROM PHIEUMUON pm JOIN PHAT p ON pm.IdPM = p.IdPM " +
        "WHERE pm.IdBD = ? AND (? IS NULL OR p.IdPhat > ?) " +
        "ORDER BY p.IdPhat ASC";

    private static final String SQL_PHIEU_PHAT_PAGE_SEARCH =
        "SELECT TOP (%d) p.IdPhat, pm.IdPM, pm.EmailNguoiLap, pm.NgayMuon, " +
        "p.LoaiPhat, p.SoTien, p.NgayGhiNhan, p.TrangThai " +
        "FROM PHIEUMUON pm JOIN PHAT p ON pm.IdPM = p.IdPM " +
        "WHERE pm.IdBD = ? AND (CAST(p.IdPhat AS VARCHAR(50)) LIKE ? OR CAST(pm.IdPM AS VARCHAR(50)) LIKE ? OR pm.EmailNguoiLap LIKE ? OR p.LoaiPhat LIKE ?) " +
        "AND (? IS NULL OR p.IdPhat > ?) " +
        "ORDER BY p.IdPhat ASC";

    // BAN DOC paging by IdBD
    private static final String SQL_BANDOC_PAGE_NOSEARCH =
        "SELECT TOP (%d) IdBD, HoTen, Email, DiaChi, SDT " +
        "FROM BANDOC " +
        "WHERE (? IS NULL OR IdBD > ?) " +
        "ORDER BY IdBD ASC";

    private static final String SQL_BANDOC_PAGE_SEARCH =
        "SELECT TOP (%d) IdBD, HoTen, Email, DiaChi, SDT " +
        "FROM BANDOC " +
        "WHERE (Email LIKE ? OR HoTen LIKE ? OR SDT LIKE ? OR DiaChi LIKE ? OR CAST(IdBD AS VARCHAR(50)) LIKE ?) " +
        "AND (? IS NULL OR IdBD > ?) " +
        "ORDER BY IdBD ASC";



    // ---------------------- Methods ----------------------

    public ArrayList<Object> getPhieuMuonPageByBanDoc(int idBD, int pageSizeRequest, Integer lastIdPM, String searchText) throws Exception {
        ArrayList<Object> result = new ArrayList<>();

        boolean hasSearch = (searchText != null && !searchText.trim().isEmpty());
        String sql = hasSearch
            ? String.format(SQL_PHIEU_MUON_PAGE_SEARCH, pageSizeRequest)
            : String.format(SQL_PHIEU_MUON_PAGE_NOSEARCH, pageSizeRequest);

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            // common first param: IdBD
            ps.setInt(idx++, idBD);

            if (hasSearch) {
                String like = "%" + searchText.trim() + "%";
                ps.setString(idx++, like); // pm.EmailNguoiLap LIKE ?
                ps.setString(idx++, like); // CAST(pm.IdPM AS VARCHAR(50)) LIKE ?
                ps.setString(idx++, like); // CAST(ct.MaBanSao AS VARCHAR(50)) LIKE ?
            }

            // lastId cursor param: (? IS NULL OR pm.IdPM > ?)
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
                ps.setString(idx++, like); // CAST(p.IdPhat AS VARCHAR(50)) LIKE ?
                ps.setString(idx++, like); // CAST(pm.IdPM AS VARCHAR(50)) LIKE ?
                ps.setString(idx++, like); // pm.EmailNguoiLap LIKE ?
                ps.setString(idx++, like); // p.LoaiPhat LIKE ?
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

    public List<BanDoc> getPageById(int pageSizeRequest, String searchText, Integer lastId) throws SQLException, Exception {
        List<BanDoc> result = new ArrayList<>();

        boolean hasSearch = (searchText != null && !searchText.trim().isEmpty());
        String sql = hasSearch
            ? String.format(SQL_BANDOC_PAGE_SEARCH, pageSizeRequest)
            : String.format(SQL_BANDOC_PAGE_NOSEARCH, pageSizeRequest);

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;

            if (hasSearch) {
                String like = "%" + searchText.trim() + "%";
                ps.setString(idx++, like); // Email LIKE ?
                ps.setString(idx++, like); // HoTen LIKE ?
                ps.setString(idx++, like); // SDT LIKE ?
                ps.setString(idx++, like); // DiaChi LIKE ?
                ps.setString(idx++, like); // CAST(IdBD AS VARCHAR(50)) LIKE ?
            }

            if (lastId == null) {
                ps.setNull(idx++, java.sql.Types.INTEGER);
                ps.setNull(idx++, java.sql.Types.INTEGER);
            } else {
                ps.setInt(idx++, lastId);
                ps.setInt(idx++, lastId);
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
    public Boolean deleteDAO(BanDoc cur) throws Exception {
        if (cur == null) return false;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setInt(1, cur.getIdBD());
            int affected = ps.executeUpdate();
            return affected > 0;

        } catch (SQLException ex) {
            // phân tích lỗi ràng buộc (thường là FK khi xóa)
            Map<String, String> violations = parseConstraintExceptionOnDelete(ex);
            if (!violations.isEmpty()) {
                throw new BanDocException(violations);
            }
            // không phải ràng buộc, ném lại để caller xử lý
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
            if (affected == 0) {
                return false;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    cur.setIdBD(generatedId);
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
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
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

    public int getSoLanMuonCuaBanDoc(int IdBD) throws Exception {
        int soPhieu = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SOLAN_MUON)) {

            ps.setInt(1, IdBD);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    soPhieu = rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return soPhieu;
    }

    public int getSoSachDangMuonCuaBanDoc(int IdBD) throws Exception {
        int ans = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SOSACH_DANG_MUON)) {

            ps.setInt(1, IdBD);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ans = rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    public int getSoSachDaMuonCuaBanDoc(int IdBD) throws Exception {
        int ans = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SOSACH_DA_MUON)) {

            ps.setInt(1, IdBD);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ans = rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    public int getSoPhieuPhatBanDoc(int IdBD) throws Exception {
        int ans = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SOPHIEU_PHAT)) {

            ps.setInt(1, IdBD);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ans = rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    public int getSoTienPhatChuaDongBanDoc(int IdBD) throws Exception {
        int ans = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SOTIEN_PHAT_CHUA_DONG)) {

            ps.setInt(1, IdBD);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ans = (int) Math.round(rs.getDouble(1));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    public int getSoTienPhatDaDongBanDoc(int IdBD) throws Exception {
        int ans = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SOTIEN_PHAT_DA_DONG)) {

            ps.setInt(1, IdBD);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ans = (int) Math.round(rs.getDouble(1));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    public int getMaBanSaoByIdPhat(int IdPhat) {
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_MABANSAO_BY_IDPHAT)) {

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

    public ArrayList<Object> getAllPhieuMuonBanDoc(BanDoc x) throws Exception {
        ArrayList<Object> ans = new ArrayList<>();
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_GET_ALL_PHIEUMUON_BANDOC)) {

            ps.setInt(1, x.getIdBD());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idPM = rs.getInt("IdPM");
                    String EmailNguoiLap = rs.getString("EmailNguoiLap");

                    Date sqlNgayMuon = rs.getDate("NgayMuon");
                    LocalDate ngayMuon = sqlNgayMuon != null ? sqlNgayMuon.toLocalDate() : null;

                    Date sqlHanTra = rs.getDate("HanTra");
                    LocalDate HanTra = sqlHanTra != null ? sqlHanTra.toLocalDate() : null;

                    int maBanSao = rs.getInt("MaBanSao");

                    Date sqlNgayTra = rs.getDate("NgayTraThucTe");
                    LocalDate ngayTraThucTe = sqlNgayTra != null ? sqlNgayTra.toLocalDate() : null;

                    String tinhTrang = rs.getString("TinhTrangKhiTra");

                    String emailNguoiNhan = rs.getString("EmailNguoiNhan");

                    ans.add(idPM);
                    ans.add(EmailNguoiLap);
                    ans.add(ngayMuon);
                    ans.add(HanTra);
                    ans.add(maBanSao);
                    ans.add(ngayTraThucTe);
                    ans.add(tinhTrang);
                    ans.add(emailNguoiNhan);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ans;
    }

    public ArrayList<Object> getAllPhieuPhatBanDoc(BanDoc x) throws Exception {
        ArrayList<Object> ans = new ArrayList<>();
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL_GET_ALL_PHIEUPHAT_BANDOC)) {

            ps.setInt(1, x.getIdBD());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idPhat = rs.getInt("IdPhat");
                    int idPM = rs.getInt("IdPM");
                    String emailNguoiLap = rs.getString("EmailNguoiLap");

                    Date sqlNgayMuon = rs.getDate("NgayMuon");
                    LocalDate ngayMuon = sqlNgayMuon != null ? sqlNgayMuon.toLocalDate() : null;

                    String loaiPhat = rs.getString("LoaiPhat");
                    double soTien = rs.getDouble("SoTien");

                    Date sqlNgayGhi = rs.getDate("NgayGhiNhan");
                    LocalDate ngayGhiNhan = sqlNgayGhi != null ? sqlNgayGhi.toLocalDate() : null;

                    String trangThai = rs.getString("TrangThai");

                    ans.add(idPhat);
                    ans.add(idPM);
                    ans.add(emailNguoiLap);
                    ans.add(ngayMuon);
                    ans.add(loaiPhat);
                    ans.add(soTien);
                    ans.add(ngayGhiNhan);
                    ans.add(trangThai);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ans;
    }
    private Map<String,String> parseConstraintException(SQLException ex) {
        Map<String,String> map = new HashMap<>();
        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();

        // NOT NULL HoTen
        if (msg.contains("cannot insert the value null") && msg.contains("hoten")) {
            map.put("HoTen", "Họ Tên không được để trống.");
        }
        // Unique Email
        if (msg.contains("email") && (msg.contains("unique") || msg.contains("uq") || msg.contains("duplicate"))) {
            map.put("Email", "Email đã tồn tại (phải là duy nhất).");
        } else if (msg.contains("email") && msg.contains("varchar") && msg.contains("too long")) {
            map.put("Email", "Email vượt quá độ dài tối đa.");
        }
        // Unique SDT
        if (msg.contains("sdt") && (msg.contains("unique") || msg.contains("uq") || msg.contains("duplicate"))) {
            map.put("SDT", "Số điện thoại đã tồn tại (phải là duy nhất).");
        }
        // FK CreatedBy
        if (msg.contains("fk_bandoc_createdby") || (msg.contains("createdby") && msg.contains("reference"))) {
            map.put("CreatedBy", "Người tạo (CreatedBy) không tồn tại trong bảng TAIKHOAN.");
        }

        // nếu SQLState chỉ rõ integrity constraint
        String sqlState = ex.getSQLState();
        if ((sqlState != null && sqlState.startsWith("23")) && map.isEmpty()) {
            // generic integrity error, thêm message tổng quát
            map.put("database", "Ràng buộc dữ liệu vi phạm: " + ex.getMessage());
        }

        return map;
    }

    private Map<String,String> parseConstraintExceptionOnDelete(SQLException ex) {
        Map<String,String> map = new HashMap<>();
        String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        // Thường là foreign key reference khi xóa
        if (msg.contains("reference") || msg.contains("conflicted with the reference constraint") || msg.contains("foreign key")) {
            map.put("delete", "Không thể xóa bạn đọc này vì có tham chiếu (ví dụ: phiếu mượn/phiếu phạt). Hãy xóa/tách các bản ghi liên quan trước.");
        }
        if (!map.isEmpty()) return map;
        return parseConstraintException(ex);
    }
}

