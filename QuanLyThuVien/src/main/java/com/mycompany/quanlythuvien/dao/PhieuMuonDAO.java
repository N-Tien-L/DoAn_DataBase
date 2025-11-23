package com.mycompany.quanlythuvien.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.Statement;
import com.mycompany.quanlythuvien.model.ChiTietPhieuMuon;
import java.util.ArrayList;
import java.util.List;

import com.mycompany.quanlythuvien.model.PhieuMuon;
import com.mycompany.quanlythuvien.util.DBConnector;

/**
 *
 * @author Tien
 */
public class PhieuMuonDAO {
    // Function for getting data
    private PhieuMuon mapRow(ResultSet rs) throws Exception {
        int idPM = rs.getInt("idPM");
        int idBD = rs.getInt("idBD");
        String emailNguoiLap = rs.getString("EmailNguoiLap");

        Date sqlNgayMuon = rs.getDate("NgayMuon");
        Date sqlHanTra = rs.getDate("HanTra");

        // Convert to Java Date type (from Date in SQL to LocalDate in Java)
        LocalDate ngayMuon = sqlNgayMuon != null ? sqlNgayMuon.toLocalDate() : null;
        LocalDate hanTra = sqlHanTra != null ? sqlHanTra.toLocalDate() : null;

        return new PhieuMuon(idPM, idBD, emailNguoiLap, ngayMuon, hanTra);
    }

    // 1. Simple CRUD: list / create / update / delete

    // Get all PhieuMuon
    public List<PhieuMuon> getAll() throws Exception {
        List<PhieuMuon> list = new ArrayList<>();
        String sql = "SELECT IdPM, IdBD, EmailNguoiLap, NgayMuon, HanTra FROM PHIEUMUON";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PhieuMuon pm = mapRow(rs);
                list.add(pm);
            }

            return list;
        } catch (SQLException ex) {
            throw new Exception("Failed to fetch all PhieuMuon: " + ex.getMessage(), ex);
        }
    }
    // Create new PhieuMuon
    public boolean createNew(PhieuMuon pm) throws Exception {
        String sql = "INSERT INTO PHIEUMUON (IdBD, EmailNguoiLap, NgayMuon, HanTra) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pm.getIdBD());
            ps.setString(2, pm.getEmailNguoiLap());

            LocalDate lm = pm.getNgayMuon();
            LocalDate lh = pm.getHanTra();

            ps.setDate(3, lm != null ? Date.valueOf(lm) : null);
            ps.setDate(4, lh != null ? Date.valueOf(lh) : null);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                return false;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    pm.setIdPM(generatedId);
                }
            }
            return true;
        } catch (SQLException ex) {
            throw new Exception("Failed to create new PhieuMuon: " + ex.getMessage(), ex);
        }
    }

    // Find PhieuMuon by IdPM
    public PhieuMuon findById(int idPM) throws Exception {
        String sql = "SELECT IdPM, IdBD, EmailNguoiLap, NgayMuon, HanTra FROM PHIEUMUON WHERE IdPM = ?";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPM);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        } catch (SQLException ex) {
            throw new Exception("Failed to find PhieuMuon by id: " + ex.getMessage(), ex);
        }
    }

    // Check whether a reader currently has any open (unreturned) borrowed copies
    public boolean hasOpenLoans(int idBD) throws Exception {
        String sql = "SELECT COUNT(*) FROM CT_PM c JOIN PHIEUMUON pm ON c.IdPM = pm.IdPM WHERE pm.IdBD = ? AND c.NgayTraThucTe IS NULL";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idBD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException ex) {
            throw new Exception("Failed to check open loans: " + ex.getMessage(), ex);
        }
    }

    // Extend due date of a PhieuMuon (ensure newDue >= NgayMuon)
    public boolean extendDueDate(int idPM, LocalDate newDue) throws Exception {
        PhieuMuon existing = findById(idPM);
        if (existing == null) throw new Exception("Phiếu mượn không tồn tại");
        if (newDue.isBefore(existing.getNgayMuon())) throw new Exception("Hạn trả mới phải >= Ngày mượn");

        String sql = "UPDATE PHIEUMUON SET HanTra = ? WHERE IdPM = ?";
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(newDue));
            ps.setInt(2, idPM);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new Exception("Failed to extend due date: " + ex.getMessage(), ex);
        }
    }

    // Create a PhieuMuon and its ChiTiet entries in a single transaction
    public boolean createWithDetails(PhieuMuon pm, List<ChiTietPhieuMuon> details) throws Exception {
        String insertPM = "INSERT INTO PHIEUMUON (IdBD, EmailNguoiLap, NgayMuon, HanTra) VALUES (?, ?, ?, ?)";
        String insertCT = "INSERT INTO CT_PM (IdPM, MaBanSao, NgayTraThucTe, TinhTrangKhiTra, EmailNguoiNhan) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnector.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement psPM = con.prepareStatement(insertPM, Statement.RETURN_GENERATED_KEYS)) {
                psPM.setInt(1, pm.getIdBD());
                psPM.setString(2, pm.getEmailNguoiLap());
                psPM.setDate(3, pm.getNgayMuon() != null ? Date.valueOf(pm.getNgayMuon()) : null);
                psPM.setDate(4, pm.getHanTra() != null ? Date.valueOf(pm.getHanTra()) : null);

                int affected = psPM.executeUpdate();
                if (affected == 0) {
                    con.rollback();
                    return false;
                }

                int generatedId;
                try (ResultSet keys = psPM.getGeneratedKeys()) {
                    if (keys.next()) generatedId = keys.getInt(1);
                    else {
                        con.rollback();
                        return false;
                    }
                }

                try (PreparedStatement psCT = con.prepareStatement(insertCT)) {
                    for (ChiTietPhieuMuon c : details) {
                        psCT.setInt(1, generatedId);
                        psCT.setInt(2, c.getMaBanSao());
                        psCT.setDate(3, c.getNgayTraThucTe() != null ? Date.valueOf(c.getNgayTraThucTe()) : null);
                        psCT.setString(4, c.getTinhTrangKhiTra());
                        psCT.setString(5, c.getEmailNguoiNhan());
                        psCT.addBatch();
                    }
                    psCT.executeBatch();
                }

                con.commit();
                pm.setIdPM(generatedId);
                return true;
            } catch (SQLException ex) {
                con.rollback();
                throw new Exception("Failed to create PhieuMuon with details: " + ex.getMessage(), ex);
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
    // Update PhieuMuon
    public boolean update(PhieuMuon pm) throws Exception {
        LocalDate ngayMuon = pm.getNgayMuon();
        LocalDate hanTra = pm.getHanTra();
        if (hanTra.isBefore(ngayMuon)) {
            throw new Exception("The due date must be greater than or equal to the borrowing date");
        }
        String sql = """
            UPDATE PHIEUMUON
            SET IdBD=?, EmailNguoiLap=?, NgayMuon=?, HanTra=?
            WHERE IdPM=?
        """;
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, pm.getIdBD());
            ps.setString(2, pm.getEmailNguoiLap());
            ps.setDate(3, Date.valueOf(ngayMuon));
            ps.setDate(4, Date.valueOf(hanTra));
            ps.setInt(5, pm.getIdPM());


            int affected = ps.executeUpdate();
            return affected > 0;

        } catch (SQLException ex) {
            throw new Exception("Failed to update PhieuMuon: " + ex.getMessage(), ex);
        }
    }
    // Delete PhieuMuon 
    public boolean delete(int IdPM) throws Exception {
        String sql = "DELETE FROM PHIEUMUON WHERE IdPM=?";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, IdPM);

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new Exception("Failed to delete PhieuMuon: " + ex.getMessage(), ex);
        }
    }

    // 2. Search / Filter

    public List<PhieuMuon> findByIdBD(int IdBD) throws Exception {
        String sql = "SELECT * FROM PHIEUMUON WHERE IdBD = ?";
        List<PhieuMuon> list = new ArrayList<>();
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ps.setInt(1, IdBD);

            while (rs.next()) {
                PhieuMuon pm = mapRow(rs);
                list.add(pm);
            }
            return list;
        } catch (SQLException ex) {
            throw new Exception("Failed to fetch PhieuMuon (IdBD): " + ex.getMessage(), ex);
        }
    }
    public List<PhieuMuon> findByNgayMuon(LocalDate from, LocalDate to) throws Exception {
        String sql = "SELECT * FROM PHIEUMUON WHERE NgayMuon BETWEEN ? AND ?";
        List<PhieuMuon> list = new ArrayList<>();

        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ps.setDate(1, from != null ? Date.valueOf(from) : null);
            ps.setDate(2, to != null ? Date.valueOf(to) : null);

            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException ex) {
            throw new Exception("Failed to fetch PhieuMuon (Date): " + ex.getMessage(), ex);
        }
    }

    public List<PhieuMuon> findCurrentBorrowed() throws Exception {
        String sql = """
            SELECT DISTINCT pm.*
            FROM PHIEUMUON pm
            JOIN CT_PM ct ON pm.IdPM = ct.IdPM
            WHERE ct.NgayTraThucTe IS NULL
        """;

        List<PhieuMuon> list = new ArrayList<>();

        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException ex) {
            throw new Exception("Failed to find PhieuMuon (borrowed): " + ex.getMessage(), ex);
        }
    }

    // Count PhieuMuon matching filters (emailBanDoc = BANDOC.Email, emailNguoiLap = PHIEUMUON.EmailNguoiLap)
    public int countSearch(String emailBanDoc, String emailNguoiLap, LocalDate from, LocalDate to, String status) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(DISTINCT pm.IdPM) FROM PHIEUMUON pm ");
        sb.append("LEFT JOIN BANDOC bd ON pm.IdBD = bd.IdBD ");
        sb.append("LEFT JOIN CT_PM ct ON pm.IdPM = ct.IdPM ");
        sb.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (emailBanDoc != null && !emailBanDoc.isBlank()) {
            sb.append(" AND LOWER(bd.Email) = LOWER(?) ");
            params.add(emailBanDoc.trim());
        }
        if (emailNguoiLap != null && !emailNguoiLap.isBlank()) {
            sb.append(" AND LOWER(pm.EmailNguoiLap) = LOWER(?) ");
            params.add(emailNguoiLap.trim());
        }
        if (from != null && to != null) {
            sb.append(" AND pm.NgayMuon BETWEEN ? AND ? ");
            params.add(Date.valueOf(from));
            params.add(Date.valueOf(to));
        } else if (from != null) {
            sb.append(" AND pm.NgayMuon >= ? ");
            params.add(Date.valueOf(from));
        } else if (to != null) {
            sb.append(" AND pm.NgayMuon <= ? ");
            params.add(Date.valueOf(to));
        }

        if (status != null) {
            if ("returned".equalsIgnoreCase(status)) {
                // all copies returned => no CT_PM with NgayTraThucTe IS NULL
                sb.append(" AND NOT EXISTS (SELECT 1 FROM CT_PM c WHERE c.IdPM = pm.IdPM AND c.NgayTraThucTe IS NULL) ");
            } else if ("unreturned".equalsIgnoreCase(status)) {
                sb.append(" AND EXISTS (SELECT 1 FROM CT_PM c WHERE c.IdPM = pm.IdPM AND c.NgayTraThucTe IS NULL) ");
            }
        }

        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sb.toString())) {
            int idx = 1;
            for (Object p : params) {
                if (p instanceof Date) ps.setDate(idx++, (Date)p);
                else ps.setString(idx++, p.toString());
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw new Exception("Failed to count search results: " + ex.getMessage(), ex);
        }
        return 0;
    }

    // Search with pagination (pageIndex is 1-based)
    public List<PhieuMuon> searchPaginated(String emailBanDoc, String emailNguoiLap, LocalDate from, LocalDate to, String status, int pageIndex, int pageSize) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT DISTINCT pm.IdPM, pm.IdBD, pm.EmailNguoiLap, pm.NgayMuon, pm.HanTra FROM PHIEUMUON pm ");
        sb.append("LEFT JOIN BANDOC bd ON pm.IdBD = bd.IdBD ");
        sb.append("LEFT JOIN CT_PM ct ON pm.IdPM = ct.IdPM ");
        sb.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (emailBanDoc != null && !emailBanDoc.isBlank()) {
            sb.append(" AND LOWER(bd.Email) = LOWER(?) ");
            params.add(emailBanDoc.trim());
        }
        if (emailNguoiLap != null && !emailNguoiLap.isBlank()) {
            sb.append(" AND LOWER(pm.EmailNguoiLap) = LOWER(?) ");
            params.add(emailNguoiLap.trim());
        }
        if (from != null && to != null) {
            sb.append(" AND pm.NgayMuon BETWEEN ? AND ? ");
            params.add(Date.valueOf(from));
            params.add(Date.valueOf(to));
        } else if (from != null) {
            sb.append(" AND pm.NgayMuon >= ? ");
            params.add(Date.valueOf(from));
        } else if (to != null) {
            sb.append(" AND pm.NgayMuon <= ? ");
            params.add(Date.valueOf(to));
        }

        if (status != null) {
            if ("returned".equalsIgnoreCase(status)) {
                sb.append(" AND NOT EXISTS (SELECT 1 FROM CT_PM c WHERE c.IdPM = pm.IdPM AND c.NgayTraThucTe IS NULL) ");
            } else if ("unreturned".equalsIgnoreCase(status)) {
                sb.append(" AND EXISTS (SELECT 1 FROM CT_PM c WHERE c.IdPM = pm.IdPM AND c.NgayTraThucTe IS NULL) ");
            }
        }

        sb.append(" ORDER BY pm.NgayMuon DESC, pm.IdPM DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        List<PhieuMuon> list = new ArrayList<>();
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sb.toString())) {
            int idx = 1;
            for (Object p : params) {
                if (p instanceof Date) ps.setDate(idx++, (Date)p);
                else ps.setString(idx++, p.toString());
            }
            int offset = Math.max(0, (pageIndex - 1) * pageSize);
            ps.setInt(idx++, offset);
            ps.setInt(idx++, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new Exception("Failed to search paginated: " + ex.getMessage(), ex);
        }
        return list;
    }

    // Find by EmailNguoiLap
    public List<PhieuMuon> findByEmailNguoiLap(String email) throws Exception {
        String sql = "SELECT IdPM, IdBD, EmailNguoiLap, NgayMuon, HanTra FROM PHIEUMUON WHERE LOWER(EmailNguoiLap) = LOWER(?)";
        List<PhieuMuon> list = new ArrayList<>();
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new Exception("Failed to find by EmailNguoiLap: " + ex.getMessage(), ex);
        }
        return list;
    }

    // Search by date range and status: status = "all" | "returned" | "unreturned"
    public List<PhieuMuon> searchByDateAndStatus(LocalDate from, LocalDate to, String status) throws Exception {
        String base = "SELECT DISTINCT pm.IdPM, pm.IdBD, pm.EmailNguoiLap, pm.NgayMuon, pm.HanTra FROM PHIEUMUON pm LEFT JOIN CT_PM ct ON pm.IdPM = ct.IdPM WHERE pm.NgayMuon BETWEEN ? AND ?";
        if (status == null) status = "all";
        if ("returned".equalsIgnoreCase(status)) {
            base += " AND ct.NgayTraThucTe IS NOT NULL";
        } else if ("unreturned".equalsIgnoreCase(status)) {
            base += " AND ct.NgayTraThucTe IS NULL";
        }

        List<PhieuMuon> list = new ArrayList<>();
        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(base)) {
            ps.setDate(1, from != null ? Date.valueOf(from) : null);
            ps.setDate(2, to != null ? Date.valueOf(to) : null);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new Exception("Failed to search by date and status: " + ex.getMessage(), ex);
        }
        return list;
    }
}