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
    public boolean markReturned(int IdPM, int maBanSao, LocalDate ngayTra) throws Exception {
        String sql = """
            UPDATE CT_PM
            SET NgayTraThucTe=?, TinhTrangKhiTra=?
            WHERE IdPM=? AND MaBanSao=?
        """;

        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            Date sqlNgayTra = ngayTra != null ? Date.valueOf(ngayTra) : null;
            ps.setDate(1, sqlNgayTra);
            ps.setString(2, "Đã trả");

            ps.setInt(3, IdPM);
            ps.setInt(4, maBanSao);

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


}
