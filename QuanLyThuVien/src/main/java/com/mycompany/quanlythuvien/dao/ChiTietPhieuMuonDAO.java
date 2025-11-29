package com.mycompany.quanlythuvien.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.mycompany.quanlythuvien.model.ChiTietPhieuMuon;
import com.mycompany.quanlythuvien.util.DBConnector;

/**
 *
 * @author Tien
 */
public class ChiTietPhieuMuonDAO {
    private ChiTietPhieuMuon mapRow(ResultSet rs) throws SQLException {
        int idPM = rs.getInt("IdPM");
        int maBanSao = rs.getInt("MaBanSao");

        Date sqlNgayTra = rs.getDate("NgayTraThucTe");
        LocalDate ngayTraThucTe = sqlNgayTra != null ? sqlNgayTra.toLocalDate() : null;

        String tinhTrang = rs.getString("TinhTrangKhiTra");
        String emailNguoiNhan = rs.getString("EmailNguoiNhan");

        return new ChiTietPhieuMuon(idPM, maBanSao, ngayTraThucTe, tinhTrang, emailNguoiNhan);
    }
    // Get All ChiTietPhieuMuon
    public List<ChiTietPhieuMuon> getAll() throws Exception {
        List<ChiTietPhieuMuon> list = new ArrayList<>();
        String sql = "SELECT * FROM CT_PM";

        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // Get all expired copies
    public List<ChiTietPhieuMuon> getOverDue() throws Exception {
        String sql = """
            SELECT ctp.*
            FROM CT_PM ctp
            JOIN PHIEUMUON pm ON ctp.IdPM = pm.IdPM
            WHERE ctp.NgayTraThucTe IS NULL
              AND pm.HanTra < ?
        """;

        List<ChiTietPhieuMuon> list = new ArrayList<>();
        LocalDate today = LocalDate.now();

        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(today));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

     public List<ChiTietPhieuMuon> findByBandocAndStatus(int IdBD, boolean isReturned) throws Exception {
        String sql = """
            SELECT ctp.*
            FROM CT_PM ctp
            JOIN PHIEUMUON pm ON ctp.IdPM = pm.IdPM
            WHERE pm.IdBD = ?
              AND ctp.NgayTraThucTe IS """ + (isReturned ? "NOT NULL" : "NULL");

        List<ChiTietPhieuMuon> list = new ArrayList<>();

        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, IdBD);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // Create new ChiTietPhieuMuon
    public boolean createNew(ChiTietPhieuMuon ctp) throws Exception {
        String sql = """
            INSERT INTO CT_PM (IdPM, MaBanSao, NgayTraThucTe, TinhTrangKhiTra, EmailNguoiNhan)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, ctp.getIdPM());
            ps.setInt(2, ctp.getMaBanSao());

            Date sqlNgayTra = ctp.getNgayTraThucTe() != null ? Date.valueOf(ctp.getNgayTraThucTe()) : null;
            ps.setDate(3, sqlNgayTra);
            ps.setString(4, ctp.getTinhTrangKhiTra());
            ps.setString(5, ctp.getEmailNguoiNhan());

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            throw new Exception("Failed to create new ChiTietPhieuMuon " + ex.getMessage(), ex);
        }
    }

    // Update ChiTietPhieuMuon
    public boolean markReturned(int IdPM, int maBanSao, LocalDate ngayTra, String tinhTrang, String emailNguoiNhan) throws Exception {
        String sql = """
            UPDATE CT_PM
            SET NgayTraThucTe=?, TinhTrangKhiTra=?, EmailNguoiNhan=?
            WHERE IdPM=? AND MaBanSao=?
        """;

        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            Date sqlNgayTra = ngayTra != null ? Date.valueOf(ngayTra) : null;
            ps.setDate(1, sqlNgayTra);
            ps.setString(2, tinhTrang);
            ps.setString(3, emailNguoiNhan);
            ps.setInt(4, IdPM);
            ps.setInt(5, maBanSao);

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            throw new Exception("Failed to update ChiTietPhieuMuon " + ex.getMessage(), ex);
        }
    }

    public boolean markReturnedBatch(int IdPM, List<Integer> maBanSaoList, LocalDate ngayTra) throws Exception {
        if (maBanSaoList == null || maBanSaoList.isEmpty()) {
            return false;
        }

        String sql = """
            UPDATE CT_PM
            SET NgayTraThucTe = ?, TinhTrangKhiTra = ?
            WHERE IdPM = ? AND MaBanSao = ?
        """;

        int totalUpdated = 0;

        try (Connection con = DBConnector.getConnection()) {
            con.setAutoCommit(false); 

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Integer maBanSao : maBanSaoList) {
                    ps.setDate(1, ngayTra != null ? Date.valueOf(ngayTra) : null);
                    ps.setString(2, "Đã trả"); 
                    ps.setInt(3, IdPM);
                    ps.setInt(4, maBanSao);

                    totalUpdated += ps.executeUpdate();
                }

                con.commit(); 
            } catch (SQLException ex) {
                con.rollback(); 
                throw new Exception("Book returned update failed: " + ex.getMessage(), ex);
            } finally {
                con.setAutoCommit(true);
            }
        }

        return totalUpdated > 0;
    }

    // Delete ChiTietPhieuMuon
    public boolean delete(int idPM, int maBanSao) throws Exception {
        String sql = "DELETE FROM CT_PM WHERE IdPM=? AND MaBanSao=?";

        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPM);
            ps.setInt(2, maBanSao);

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            throw new Exception("Failed to delete " + ex.getMessage(), ex);
        }
    }

    // Get all ChiTietPhieuMuon by IdPM
    public List<ChiTietPhieuMuon> findByIdPM(int idPM) throws Exception {
        String sql = "SELECT * FROM CT_PM WHERE IdPM = ?";
        List<ChiTietPhieuMuon> list = new ArrayList<>();
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // Get all unreturned copies
    public List<ChiTietPhieuMuon> findUnreturnedByIdPM(int idPM) throws Exception {
        String sql = """
            SELECT * 
            FROM CT_PM
            WHERE IdPM = ? AND NgayTraThucTe IS NULL
        """;
        List<ChiTietPhieuMuon> list = new ArrayList<>();
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public ChiTietPhieuMuon findByMaBanSao(int maBanSao) throws Exception {
        String sql = "SELECT * FROM CT_PM WHERE MaBanSao = ?";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);) {
            ps.setInt(1, maBanSao);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                } else {
                    return null; 
                }
            }
        }
    }
    // Check if a physical copy is currently borrowed (not yet returned)
    public boolean isMaBanSaoBorrowed(int maBanSao) throws Exception {
        String sql = "SELECT 1 FROM CT_PM WHERE MaBanSao = ? AND NgayTraThucTe IS NULL";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maBanSao);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new Exception("Failed to check borrowed status: " + ex.getMessage(), ex);
        }
    }

    // Get the active (unreturned) CT_PM record for a given MaBanSao, or null if none
    public ChiTietPhieuMuon getActiveByMaBanSao(int maBanSao) throws Exception {
        String sql = "SELECT * FROM CT_PM WHERE MaBanSao = ? AND NgayTraThucTe IS NULL";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maBanSao);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
                return null;
            }
        } catch (SQLException ex) {
            throw new Exception("Failed to get active loan by MaBanSao: " + ex.getMessage(), ex);
        }
    }

    // Count overdue items for a reader (IdBD)
    public int countOverdueByBanDoc(int idBD) throws Exception {
        String sql = "SELECT COUNT(*) FROM CT_PM c JOIN PHIEUMUON pm ON c.IdPM = pm.IdPM WHERE pm.IdBD = ? AND c.NgayTraThucTe IS NULL AND pm.HanTra < ?";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idBD);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                return 0;
            }
        } catch (SQLException ex) {
            throw new Exception("Failed to count overdue items: " + ex.getMessage(), ex);
        }
    }

    // Calculate number of days late for a given CT_PM (if returned use actual return date, otherwise use today)
    public long getDaysLate(int idPM, int maBanSao) throws Exception {
        String sql = "SELECT pm.HanTra, c.NgayTraThucTe FROM CT_PM c JOIN PHIEUMUON pm ON c.IdPM = pm.IdPM WHERE c.IdPM = ? AND c.MaBanSao = ?";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPM);
            ps.setInt(2, maBanSao);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date sqlHanTra = rs.getDate("HanTra");
                    Date sqlNgayTra = rs.getDate("NgayTraThucTe");
                    LocalDate hanTra = sqlHanTra != null ? sqlHanTra.toLocalDate() : null;
                    LocalDate ngayTra = sqlNgayTra != null ? sqlNgayTra.toLocalDate() : LocalDate.now();
                    if (hanTra == null) return 0;
                    long daysLate = java.time.temporal.ChronoUnit.DAYS.between(hanTra, ngayTra);
                    return Math.max(0, daysLate);
                }
                return 0;
            }
        } catch (SQLException ex) {
            throw new Exception("Failed to compute days late: " + ex.getMessage(), ex);
        }
    }



    // Đếm số sách quá hạn vào 1 ngày (ngày nhỏ hơn date và chưa trả)
    public int countOverdueOnDate(java.time.LocalDate date) throws Exception {
        String sql = "SELECT COUNT(*) FROM CT_PM c JOIN PHIEUMUON pm ON c.IdPM = pm.IdPM WHERE c.NgayTraThucTe IS NULL AND pm.HanTra < ?";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                return 0;
            }
        }
    }

    // Count CT_PM for an IdPM with filters for returned/unreturned and overdue/notoverdue
    public int countByIdPMWithFilters(int idPM, String returnedFilter, String overdueFilter) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*) FROM CT_PM c JOIN PHIEUMUON pm ON c.IdPM = pm.IdPM WHERE c.IdPM = ? ");
        List<Object> params = new ArrayList<>();
        params.add(idPM);

        if (returnedFilter != null) {
            if ("returned".equalsIgnoreCase(returnedFilter)) sb.append(" AND c.NgayTraThucTe IS NOT NULL ");
            else if ("unreturned".equalsIgnoreCase(returnedFilter)) sb.append(" AND c.NgayTraThucTe IS NULL ");
        }

        if (overdueFilter != null) {
            if ("overdue".equalsIgnoreCase(overdueFilter)) {
                sb.append(" AND ((c.NgayTraThucTe IS NOT NULL AND c.NgayTraThucTe > pm.HanTra) OR (c.NgayTraThucTe IS NULL AND pm.HanTra < ?)) ");
                params.add(Date.valueOf(LocalDate.now()));
            } else if ("notoverdue".equalsIgnoreCase(overdueFilter)) {
                sb.append(" AND NOT ((c.NgayTraThucTe IS NOT NULL AND c.NgayTraThucTe > pm.HanTra) OR (c.NgayTraThucTe IS NULL AND pm.HanTra < ?)) ");
                params.add(Date.valueOf(LocalDate.now()));
            }
        }

        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sb.toString())) {
            int idx = 1;
            for (Object p : params) {
                if (p instanceof Date) ps.setDate(idx++, (Date)p);
                else if (p instanceof Integer) ps.setInt(idx++, (Integer)p);
                else ps.setString(idx++, p.toString());
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    // Get paginated CT_PM for an IdPM with filters
    public List<ChiTietPhieuMuon> findByIdPMWithFiltersPaginated(int idPM, String returnedFilter, String overdueFilter, int pageIndex, int pageSize) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT c.* FROM CT_PM c JOIN PHIEUMUON pm ON c.IdPM = pm.IdPM WHERE c.IdPM = ? ");
        List<Object> params = new ArrayList<>();
        params.add(idPM);

        if (returnedFilter != null) {
            if ("returned".equalsIgnoreCase(returnedFilter)) sb.append(" AND c.NgayTraThucTe IS NOT NULL ");
            else if ("unreturned".equalsIgnoreCase(returnedFilter)) sb.append(" AND c.NgayTraThucTe IS NULL ");
        }

        if (overdueFilter != null) {
            if ("overdue".equalsIgnoreCase(overdueFilter)) {
                sb.append(" AND ((c.NgayTraThucTe IS NOT NULL AND c.NgayTraThucTe > pm.HanTra) OR (c.NgayTraThucTe IS NULL AND pm.HanTra < ?)) ");
                params.add(Date.valueOf(LocalDate.now()));
            } else if ("notoverdue".equalsIgnoreCase(overdueFilter)) {
                sb.append(" AND NOT ((c.NgayTraThucTe IS NOT NULL AND c.NgayTraThucTe > pm.HanTra) OR (c.NgayTraThucTe IS NULL AND pm.HanTra < ?)) ");
                params.add(Date.valueOf(LocalDate.now()));
            }
        }

        sb.append(" ORDER BY c.MaBanSao ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        List<ChiTietPhieuMuon> list = new ArrayList<>();
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sb.toString())) {
            int idx = 1;
            for (Object p : params) {
                if (p instanceof Date) ps.setDate(idx++, (Date)p);
                else if (p instanceof Integer) ps.setInt(idx++, (Integer)p);
                else ps.setString(idx++, p.toString());
            }
            int offset = Math.max(0, (pageIndex - 1) * pageSize);
            ps.setInt(idx++, offset);
            ps.setInt(idx++, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }
}