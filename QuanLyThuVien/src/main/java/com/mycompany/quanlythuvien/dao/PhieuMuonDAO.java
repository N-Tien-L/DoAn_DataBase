package com.mycompany.quanlythuvien.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, pm.getIdPM());
            ps.setInt(2, pm.getIdBD());
            ps.setString(3, pm.getEmailNguoiLap());
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
}
