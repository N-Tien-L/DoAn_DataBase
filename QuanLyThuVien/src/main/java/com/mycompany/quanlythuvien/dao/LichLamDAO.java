package com.mycompany.quanlythuvien.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.mycompany.quanlythuvien.exceptions.LichLamException;
import com.mycompany.quanlythuvien.model.LichLam;
import com.mycompany.quanlythuvien.util.DBConnector;

/**
 *
 * @author Tien
 */
public class LichLamDAO {
    
    private static final String SQL_CREATE_LICHLAM = "INSERT INTO LICHLAM (EmailThuThu, Ngay, GioBatDau, GioKetThuc, GhiChu, CreatedBy) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_GET_SHIFTS_BY_EMAIL_BETWEEN = "SELECT Id, EmailThuThu, Ngay, GioBatDau, GioKetThuc, TrangThai, GhiChu, CreatedBy, CreatedAt FROM LICHLAM WHERE EmailThuThu = ? AND Ngay BETWEEN ? AND ? ORDER BY Ngay, GioBatDau";
    private static final String SQL_CANCEL_SHIFT = "UPDATE LICHLAM SET TrangThai = 'Cancelled' WHERE Id = ?";

    public boolean createShift(String EmailThuThu, Date Ngay, LocalTime GioBatDau, LocalTime GioKetThuc, String GhiChu, String CreatedBy) throws LichLamException {
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_CREATE_LICHLAM);

            ps.setString(1, EmailThuThu);
            ps.setDate(2, Ngay);
            ps.setTime(3, Time.valueOf(GioBatDau));
            ps.setTime(4, Time.valueOf(GioKetThuc));
            ps.setString(5, GhiChu);
            ps.setString(6, CreatedBy);

            int rowAffected = ps.executeUpdate();

            if(rowAffected > 0) {
                System.out.println("Shift created successfully (created by: " + CreatedBy + ")");
                return true;
            }
            throw new LichLamException("Không thể tạo ca làm");

        } catch (Exception e) {
            // Xử lý lỗi unique constraint
            if(e.getMessage().contains("UX_LICHLAM_UniqueSlot")) {
                throw new LichLamException("Ca làm bị trùng với ca khác");
            }

            // Xử lý lỗi trigger overlap
            if(e.getMessage().contains("TRG_LICHLAM_NoOverlap") || e.getMessage().contains("chồng lấn")) {
                throw new LichLamException("Ca làm bị chồng lấn với ca khác");
            }
            throw new LichLamException("Lỗi tạo ca làm: " + e.getMessage(), e);
        }
    }

    public List<LichLam> getShiftsByEmailBetween(String EmailThuThu, Date FromDate, Date ToDate) throws LichLamException {
        List<LichLam> lsLichLams = new ArrayList<>();

        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_GET_SHIFTS_BY_EMAIL_BETWEEN);

            ps.setString(1, EmailThuThu);
            ps.setDate(2, FromDate);
            ps.setDate(3, ToDate);

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                LichLam lichLam = new LichLam(
                    rs.getInt("Id"),
                    rs.getString("EmailThuThu"),
                    rs.getDate("Ngay"),
                    rs.getTime("GioBatDau"),
                    rs.getTime("GioKetThuc"),
                    rs.getString("TrangThai"),
                    rs.getString("GhiChu"),
                    rs.getString("CreatedBy"),
                    rs.getTimestamp("CreatedAt").toLocalDateTime()
                );

                lsLichLams.add(lichLam);
            }
        } catch (Exception e) {
            throw new LichLamException("Lỗi lấy danh sách ca làm: " + e.getMessage(), e);
        }

        return lsLichLams;
    }

    public boolean cancelShift(int idLich) throws LichLamException {
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_CANCEL_SHIFT);
            ps.setInt(1, idLich);
            int rowAffected = ps.executeUpdate();
            
            if(rowAffected > 0) {
                System.out.println("Shift with id: " + idLich + "canceled successfully");
                return true;
            }

            throw new LichLamException("Không tìm thấy ca làm với ID: " + idLich);
        } catch(Exception e) {
            throw new LichLamException("Lỗi hủy ca làm: " + e.getMessage(), e);
        }
    }
}
