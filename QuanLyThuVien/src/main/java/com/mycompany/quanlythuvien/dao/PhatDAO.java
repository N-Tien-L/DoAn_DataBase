package com.mycompany.quanlythuvien.dao;

import com.mycompany.quanlythuvien.model.BanDocPhat;
import com.mycompany.quanlythuvien.model.Phat;
import com.mycompany.quanlythuvien.model.ChiTietPhieuMuonInfo;
import com.mycompany.quanlythuvien.model.PaginationResult;
import com.mycompany.quanlythuvien.util.DBConnector;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tien
 */
public class PhatDAO {

    // Lấy tất cả phiếu phạt
    public List<Phat> getAllPhat() throws Exception {
        List<Phat> list = new ArrayList<>();
        String sql = "SELECT * FROM PHAT ORDER BY NgayGhiNhan DESC";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Phat p = new Phat(
                        rs.getInt("IdPhat"),
                        rs.getInt("IdPM"),
                        rs.getInt("MaBanSao"),
                        rs.getString("LoaiPhat"),
                        rs.getBigDecimal("SoTien"),
                        rs.getDate("NgayGhiNhan").toLocalDate(),
                        rs.getString("TrangThai"));
                list.add(p);
            }
        }
        return list;
    }

    // Lấy tất cả phiếu phạt với phân trang (cursor-based)
    public PaginationResult<Phat> getAllPhatPaginated(int cursor, int pageSize) throws Exception {
        List<Phat> list = new ArrayList<>();

        // Lấy tổng số bản ghi
        int totalCount = getTotalPhatCount();

        // Nếu cursor < 0, bắt đầu từ 0
        if (cursor < 0)
            cursor = 0;

        // SQL lấy dữ liệu
        String sql = """
                    SELECT * FROM PHAT
                    ORDER BY NgayGhiNhan DESC, IdPhat DESC
                    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;

        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cursor);
            ps.setInt(2, pageSize + 1); // Lấy thêm 1 để biết có trang tiếp theo không

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next() && count < pageSize) {
                    Phat p = new Phat(
                            rs.getInt("IdPhat"),
                            rs.getInt("IdPM"),
                            rs.getInt("MaBanSao"),
                            rs.getString("LoaiPhat"),
                            rs.getBigDecimal("SoTien"),
                            rs.getDate("NgayGhiNhan").toLocalDate(),
                            rs.getString("TrangThai"));
                    list.add(p);
                    count++;
                }
            }
        }

        // Tính toán nextCursor và previousCursor
        int nextCursor = (cursor + pageSize < totalCount) ? cursor + pageSize : -1;
        int previousCursor = (cursor > 0) ? Math.max(0, cursor - pageSize) : -1;

        return new PaginationResult<>(list, nextCursor, previousCursor, totalCount, pageSize, cursor);
    }

    // Đếm tổng số vé phạt
    private int getTotalPhatCount() throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM PHAT";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    // Lấy phạt theo IdPM (Phiếu Mượn)
    public List<Phat> getPhatByIdPM(int idPM) throws Exception {
        List<Phat> list = new ArrayList<>();
        String sql = "SELECT * FROM PHAT WHERE IdPM = ? ORDER BY NgayGhiNhan DESC";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPM);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Phat p = new Phat(
                            rs.getInt("IdPhat"),
                            rs.getInt("IdPM"),
                            rs.getInt("MaBanSao"),
                            rs.getString("LoaiPhat"),
                            rs.getBigDecimal("SoTien"),
                            rs.getDate("NgayGhiNhan").toLocalDate(),
                            rs.getString("TrangThai"));
                    list.add(p);
                }
            }
        }
        return list;
    }

    // Tạo vé phạt
    public boolean createPhat(Phat p) throws Exception {
        String sql = "INSERT INTO PHAT (IdPM, MaBanSao, LoaiPhat, SoTien, NgayGhiNhan, TrangThai) VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getIdPM());
            ps.setInt(2, p.getMaBanSao());
            ps.setString(3, p.getLoaiPhat());
            ps.setBigDecimal(4, p.getSoTien());
            ps.setDate(5, Date.valueOf(p.getNgayGhiNhan()));
            ps.setString(6, p.getTrangThai());

            return ps.executeUpdate() > 0;
        }
    }

    // Cập nhật phạt
    public boolean update(Phat p) throws Exception {
        String sql = """
                    UPDATE PHAT
                    SET LoaiPhat=?, SoTien=?, TrangThai=?
                    WHERE IdPhat=?
                """;
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getLoaiPhat());
            ps.setBigDecimal(2, p.getSoTien());
            ps.setString(3, p.getTrangThai());
            ps.setInt(4, p.getIdPhat());

            return ps.executeUpdate() > 0;
        }
    }

    // Xóa vé phạt
    public boolean delete(int idPhat) throws Exception {
        String sql = "DELETE FROM PHAT WHERE IdPhat=?";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPhat);

            return ps.executeUpdate() > 0;
        }
    }

    // Lấy tổng tiền phạt chưa đóng
    public BigDecimal getTotalTienChuaDong(int idPM) throws Exception {
        String sql = "SELECT SUM(SoTien) AS TotalTien FROM PHAT WHERE IdPM=? AND TrangThai='Chua dong'";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPM);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("TotalTien");
                    return total != null ? total : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    // Lấy danh sách phạt chưa đóng
    public List<Phat> getPhatChuaDong() throws Exception {
        List<Phat> list = new ArrayList<>();
        String sql = "SELECT * FROM PHAT WHERE TrangThai='Chua dong' ORDER BY NgayGhiNhan ASC";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Phat p = new Phat(
                        rs.getInt("IdPhat"),
                        rs.getInt("IdPM"),
                        rs.getInt("MaBanSao"),
                        rs.getString("LoaiPhat"),
                        rs.getBigDecimal("SoTien"),
                        rs.getDate("NgayGhiNhan").toLocalDate(),
                        rs.getString("TrangThai"));
                list.add(p);
            }
        }
        return list;
    }

    public List<Phat> searchPhatByText(String text) throws Exception {
        List<Phat> list = new ArrayList<>();
        String sql = """
                    SELECT p.*, pm.IdBD AS IdBD
                    FROM PHAT p
                    JOIN PHIEUMUON pm ON p.IdPM = pm.IdPM
                    JOIN BANDOC bd ON pm.IdBD = bd.IdBD
                    WHERE CAST(p.IdPhat AS VARCHAR) LIKE ?
                       OR CAST(p.IdPM AS VARCHAR) LIKE ?
                       OR CAST(p.MaBanSao AS VARCHAR) LIKE ?
                       OR LOWER(bd.Email) LIKE LOWER(?)
                       OR LOWER(bd.HoTen) LIKE LOWER(?)
                       OR bd.SDT LIKE ?
                    ORDER BY p.NgayGhiNhan DESC
                """;

        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            String pattern = "%" + text + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, pattern);
            ps.setString(6, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Phat p = new Phat(
                            rs.getInt("IdPhat"),
                            rs.getInt("IdPM"),
                            rs.getInt("MaBanSao"),
                            rs.getString("LoaiPhat"),
                            rs.getBigDecimal("SoTien"),
                            rs.getDate("NgayGhiNhan").toLocalDate(),
                            rs.getString("TrangThai"));
                    p.setIdBD(rs.getInt("idBD"));
                    list.add(p);
                }
            }
        }
        return list;
    }

    // Tìm kiếm vé phạt với phân trang (cursor-based)
    public PaginationResult<Phat> searchPhatByTextPaginated(String text, int cursor, int pageSize) throws Exception {
        List<Phat> list = new ArrayList<>();

        // Lấy tổng số bản ghi khớp với tìm kiếm
        int totalCount = getTotalSearchPhatCount(text);

        // Nếu cursor < 0, bắt đầu từ 0
        if (cursor < 0)
            cursor = 0;

        String sql = """
                    SELECT p.*, pm.IdBD AS IdBD
                    FROM PHAT p
                    JOIN PHIEUMUON pm ON p.IdPM = pm.IdPM
                    JOIN BANDOC bd ON pm.IdBD = bd.IdBD
                    WHERE CAST(p.IdPhat AS VARCHAR) LIKE ?
                       OR CAST(p.IdPM AS VARCHAR) LIKE ?
                       OR CAST(p.MaBanSao AS VARCHAR) LIKE ?
                       OR LOWER(bd.Email) LIKE LOWER(?)
                       OR LOWER(bd.HoTen) LIKE LOWER(?)
                       OR bd.SDT LIKE ?
                    ORDER BY p.NgayGhiNhan DESC, p.IdPhat DESC
                    OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;

        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            String pattern = "%" + text + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, pattern);
            ps.setString(6, pattern);
            ps.setInt(7, cursor);
            ps.setInt(8, pageSize + 1); // Lấy thêm 1 để biết có trang tiếp theo không

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next() && count < pageSize) {
                    Phat p = new Phat(
                            rs.getInt("IdPhat"),
                            rs.getInt("IdPM"),
                            rs.getInt("MaBanSao"),
                            rs.getString("LoaiPhat"),
                            rs.getBigDecimal("SoTien"),
                            rs.getDate("NgayGhiNhan").toLocalDate(),
                            rs.getString("TrangThai"));
                    p.setIdBD(rs.getInt("idBD"));
                    list.add(p);
                    count++;
                }
            }
        }

        // Tính toán nextCursor và previousCursor
        int nextCursor = (cursor + pageSize < totalCount) ? cursor + pageSize : -1;
        int previousCursor = (cursor > 0) ? Math.max(0, cursor - pageSize) : -1;

        return new PaginationResult<>(list, nextCursor, previousCursor, totalCount, pageSize, cursor);
    }

    // Đếm tổng số vé phạt khớp với tìm kiếm
    private int getTotalSearchPhatCount(String text) throws Exception {
        String sql = """
                    SELECT COUNT(*) AS total
                    FROM PHAT p
                    JOIN PHIEUMUON pm ON p.IdPM = pm.IdPM
                    JOIN BANDOC bd ON pm.IdBD = bd.IdBD
                    WHERE CAST(p.IdPhat AS VARCHAR) LIKE ?
                       OR CAST(p.IdPM AS VARCHAR) LIKE ?
                       OR CAST(p.MaBanSao AS VARCHAR) LIKE ?
                       OR LOWER(bd.Email) LIKE LOWER(?)
                       OR LOWER(bd.HoTen) LIKE LOWER(?)
                       OR bd.SDT LIKE ?
                """;

        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            String pattern = "%" + text + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, pattern);
            ps.setString(6, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    // Tìm kiếm chi tiết phiếu mượn theo idPM, MaBanSao, tên bạn đọc, số điện thoại
    // Dùng cho gợi ý khi typing trong form tạo vé phạt
    public List<ChiTietPhieuMuonInfo> searchChiTietPhieuMuon(String text) throws Exception {
        List<ChiTietPhieuMuonInfo> list = new ArrayList<>();
        String sql = """
                    SELECT
                        pm.IdPM,
                        ctpm.MaBanSao,
                        bd.IdBD,
                        bd.HoTen,
                        bd.SDT,
                        pm.NgayMuon,
                        pm.HanTra
                    FROM PHIEUMUON pm
                    JOIN CT_PM ctpm ON pm.IdPM = ctpm.IdPM
                    JOIN BANDOC bd ON pm.IdBD = bd.IdBD
                    WHERE ctpm.NgayTraThucTe IS NULL
                      AND (CAST(pm.IdPM AS VARCHAR) LIKE ?
                           OR CAST(ctpm.MaBanSao AS VARCHAR) LIKE ?
                           OR LOWER(bd.HoTen) LIKE LOWER(?)
                           OR bd.SDT LIKE ?)
                    ORDER BY pm.NgayMuon DESC
                """;

        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            String pattern = "%" + text + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChiTietPhieuMuonInfo info = new ChiTietPhieuMuonInfo(
                            rs.getInt("IdPM"),
                            rs.getInt("MaBanSao"),
                            rs.getInt("IdBD"),
                            rs.getString("HoTen"),
                            rs.getString("SDT"),
                            rs.getDate("NgayMuon").toLocalDate(),
                            rs.getDate("HanTra").toLocalDate());
                    list.add(info);
                }
            }
        }

        return list;
    }

    // Lấy thông tin phạt của 1 bạn đọc theo IdBD
    public BanDocPhat getBanDocPhatByIdBD(int idBD) throws Exception {
        String sql = """
                    SELECT
                        bd.IdBD,
                        bd.HoTen,
                            bd.Email,
                        bd.DiaChi,
                        bd.SDT,
                        SUM(CASE WHEN p.LoaiPhat = 'Tre han' THEN 1 ELSE 0 END) AS SoLuongTreHan,
                        SUM(CASE WHEN p.LoaiPhat = 'Hong sach' THEN 1 ELSE 0 END) AS SoLuongHongSach,
                        SUM(CASE WHEN p.LoaiPhat = 'Mat sach' THEN 1 ELSE 0 END) AS SoLuongMatSach,
                        SUM(p.SoTien) AS TongTienPhat,
                        SUM(CASE WHEN p.TrangThai = 'Chua dong' THEN p.SoTien ELSE 0 END) AS TongTienChuaDong
                    FROM BANDOC bd
                    LEFT JOIN PHIEUMUON pm ON bd.IdBD = pm.IdBD
                    LEFT JOIN PHAT p ON pm.IdPM = p.IdPM
                    WHERE bd.IdBD = ?
                    GROUP BY bd.IdBD, bd.HoTen, bd.Email, bd.DiaChi, bd.SDT
                """;

        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idBD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BanDocPhat bdp = new BanDocPhat();
                    bdp.setIdBD(rs.getInt("IdBD"));
                    bdp.setHoTen(rs.getString("HoTen"));
                    bdp.setEmail(rs.getString("Email"));
                    bdp.setDiaChi(rs.getString("DiaChi"));
                    bdp.setSdt(rs.getString("SDT"));

                    bdp.setSoLuongTreHan(rs.getInt("SoLuongTreHan"));
                    bdp.setSoLuongHongSach(rs.getInt("SoLuongHongSach"));
                    bdp.setSoLuongMatSach(rs.getInt("SoLuongMatSach"));
                    bdp.setTongTienPhat(
                            rs.getBigDecimal("TongTienPhat") != null ? rs.getBigDecimal("TongTienPhat")
                                    : BigDecimal.ZERO);
                    bdp.setTongTienChuaDong(
                            rs.getBigDecimal("TongTienChuaDong") != null ? rs.getBigDecimal("TongTienChuaDong")
                                    : BigDecimal.ZERO);

                    return bdp;
                }
            }
        }

        return null;
    }

    // Cập nhật trạng thái "Da dong" cho tất cả vé phạt của 1 bạn đọc
    public boolean updateAllPhatToDaDongByIdBD(int idBD) throws Exception {
        String sql = """
                    UPDATE PHAT
                    SET TrangThai = 'Da dong'
                    WHERE IdPM IN (
                        SELECT IdPM FROM PHIEUMUON WHERE IdBD = ?
                    )
                    AND TrangThai = 'Chua dong'
                """;

        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idBD);
            return ps.executeUpdate() > 0;
        }
    }

    // Lấy chi tiết phiếu mượn từ IdPM và MaBanSao
    public ChiTietPhieuMuonInfo getChiTietPhieuMuonByIdPMAndMaBanSao(int idPM, int maBanSao) throws Exception {
        String sql = """
                    SELECT *
                    FROM PHIEUMUON
                    WHERE IdPM = ?
                """;

        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPM);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ChiTietPhieuMuonInfo info = new ChiTietPhieuMuonInfo(
                            rs.getInt("IdPM"),
                            maBanSao,
                            rs.getInt("IdBD"),
                            null,
                            null,
                            rs.getDate("NgayMuon").toLocalDate(),
                            rs.getDate("HanTra").toLocalDate());

                    return info;
                }
            }
        }

        return null;
    }
}
