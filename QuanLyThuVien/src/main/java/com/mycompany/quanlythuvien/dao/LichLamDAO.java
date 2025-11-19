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
 * Data Access Object xử lý truy vấn DB cho bảng LICHLAM
 * @author Tien
 */
public class LichLamDAO {
    
    private static final String SQL_CREATE_LICHLAM = "INSERT INTO LICHLAM (EmailThuThu, Ngay, GioBatDau, GioKetThuc, GhiChu, CreatedBy) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_GET_SHIFTS_BY_EMAIL_BETWEEN = "SELECT Id, EmailThuThu, Ngay, GioBatDau, GioKetThuc, TrangThai, GhiChu, CreatedBy, CreatedAt FROM LICHLAM WHERE EmailThuThu = ? AND Ngay BETWEEN ? AND ? ORDER BY Ngay, GioBatDau";
    private static final String SQL_CANCEL_SHIFT = "UPDATE LICHLAM SET TrangThai = 'Cancelled' WHERE Id = ?";
    private static final String SQL_GET_BY_DATE_RANGE = "SELECT Id, EmailThuThu, Ngay, GioBatDau, GioKetThuc, TrangThai, GhiChu, CreatedBy, CreatedAt FROM LICHLAM WHERE Ngay BETWEEN ? AND ? ORDER BY Ngay, GioBatDau";
    private static final String SQL_DELETE_SCHEDULE = "DELETE FROM LICHLAM WHERE Id = ?";
    private static final String SQL_UPDATE_SCHEDULE = "UPDATE LICHLAM SET EmailThuThu = ?, Ngay = ?, GioBatDau = ?, GioKetThuc = ?, TrangThai = ?, GhiChu = ? WHERE Id = ?";

    /**
     * Tạo ca làm mới trong DB
     * Trigger TRG_LICHLAM_NoOverlap và constraint UX_LICHLAM_UniqueSlot sẽ tự động kiểm tra trùng lặp
     * 
     * @param EmailThuThu Email thủ thư
     * @param Ngay Ngày làm việc
     * @param GioBatDau Giờ bắt đầu
     * @param GioKetThuc Giờ kết thúc
     * @param GhiChu Ghi chú (nullable)
     * @param CreatedBy Email người tạo ca
     * @return true nếu insert thành công
     * @throws LichLamException Lỗi trùng lặp, chồng lấn hoặc lỗi DB
     */
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

    /**
     * Lấy danh sách ca làm của một thủ thư trong khoảng thời gian
     * 
     * @param EmailThuThu Email thủ thư
     * @param FromDate Ngày bắt đầu (inclusive)
     * @param ToDate Ngày kết thúc (inclusive)
     * @return Danh sách LichLam sắp xếp theo Ngay, GioBatDau
     * @throws LichLamException Lỗi query DB
     */
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

    /**
     * Hủy ca làm bằng cách đổi trạng thái thành 'Cancelled'
     * Không xóa record, giữ lại lịch sử cho audit
     * 
     * @param idLich ID ca làm cần hủy
     * @return true nếu update thành công
     * @throws LichLamException Lỗi không tìm thấy ca hoặc lỗi DB
     */
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
    
    /**
     * Lấy tất cả ca làm (của mọi thủ thư) trong khoảng thời gian
     * Dùng cho Admin xem tổng quan lịch làm
     * 
     * @param fromDate Ngày bắt đầu (inclusive)
     * @param toDate Ngày kết thúc (inclusive)
     * @return Danh sách LichLam sắp xếp theo Ngay, GioBatDau
     * @throws LichLamException Lỗi query DB
     */
    public List<LichLam> getSchedulesByDateRange(Date fromDate, Date toDate) throws LichLamException {
        List<LichLam> schedules = new ArrayList<>();
        
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_GET_BY_DATE_RANGE);
            ps.setDate(1, fromDate);
            ps.setDate(2, toDate);
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
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
                schedules.add(lichLam);
            }
        } catch (Exception e) {
            throw new LichLamException("Lỗi lấy danh sách lịch làm: " + e.getMessage(), e);
        }
        
        return schedules;
    }
    
    /**
     * Xóa vĩnh viễn ca làm khỏi DB
     * Nên chỉ dùng cho ca chưa diễn ra; với ca đã qua ưu tiên cancelShift để giữ audit
     * 
     * @param idLich ID ca làm cần xóa
     * @return true nếu delete thành công
     * @throws LichLamException Lỗi không tìm thấy ca hoặc lỗi DB
     */
    public boolean deleteSchedule(int idLich) throws LichLamException {
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_DELETE_SCHEDULE);
            ps.setInt(1, idLich);
            int rowAffected = ps.executeUpdate();
            
            if (rowAffected > 0) {
                System.out.println("Schedule with id: " + idLich + " deleted successfully");
                return true;
            }
            
            throw new LichLamException("Không tìm thấy lịch làm với ID: " + idLich);
        } catch (Exception e) {
            throw new LichLamException("Lỗi xóa lịch làm: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cập nhật thông tin ca làm
     * Cho phép thay đổi thủ thư, ngày giờ, trạng thái, ghi chú
     * Trigger TRG_LICHLAM_NoOverlap và constraint UX_LICHLAM_UniqueSlot sẽ kiểm tra trùng lặp
     * 
     * @param idLich ID ca làm cần cập nhật
     * @param emailThuThu Email thủ thư mới
     * @param ngay Ngày làm việc mới
     * @param gioBatDau Giờ bắt đầu mới
     * @param gioKetThuc Giờ kết thúc mới
     * @param trangThai Trạng thái mới (Scheduled, Done, Cancelled,...)
     * @param ghiChu Ghi chú mới
     * @return true nếu update thành công
     * @throws LichLamException Lỗi trùng lặp, chồng lấn, không tìm thấy hoặc lỗi DB
     */
    public boolean updateSchedule(int idLich, String emailThuThu, Date ngay, 
                                 LocalTime gioBatDau, LocalTime gioKetThuc,
                                 String trangThai, String ghiChu) throws LichLamException {
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_SCHEDULE);
            
            ps.setString(1, emailThuThu);
            ps.setDate(2, ngay);
            ps.setTime(3, Time.valueOf(gioBatDau));
            ps.setTime(4, Time.valueOf(gioKetThuc));
            ps.setString(5, trangThai);
            ps.setString(6, ghiChu);
            ps.setInt(7, idLich);
            
            int rowAffected = ps.executeUpdate();
            
            if (rowAffected > 0) {
                System.out.println("Schedule with id: " + idLich + " updated successfully");
                return true;
            }
            
            throw new LichLamException("Không tìm thấy lịch làm với ID: " + idLich);
        } catch (Exception e) {
            // Xử lý lỗi unique constraint
            if (e.getMessage().contains("UX_LICHLAM_UniqueSlot")) {
                throw new LichLamException("Ca làm bị trùng với ca khác");
            }
            
            // Xử lý lỗi trigger overlap
            if (e.getMessage().contains("TRG_LICHLAM_NoOverlap") || e.getMessage().contains("chồng lấn")) {
                throw new LichLamException("Ca làm bị chồng lấn với ca khác");
            }
            
            throw new LichLamException("Lỗi cập nhật lịch làm: " + e.getMessage(), e);
        }
    }
}
