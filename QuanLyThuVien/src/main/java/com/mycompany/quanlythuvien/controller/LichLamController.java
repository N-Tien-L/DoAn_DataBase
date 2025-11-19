package com.mycompany.quanlythuvien.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.mycompany.quanlythuvien.dao.LichLamDAO;
import com.mycompany.quanlythuvien.exceptions.LichLamException;
import com.mycompany.quanlythuvien.middleware.AuthMiddleware;
import com.mycompany.quanlythuvien.model.LichLam;
import com.mycompany.quanlythuvien.model.TaiKhoan;

/**
 * Controller xử lý nghiệp vụ quản lý lịch làm việc
 * @author Tien
 */
public class LichLamController {

    private final LichLamDAO lichLamDAO = new LichLamDAO();
    
    /**
     * Tạo ca làm việc mới cho thủ thư
     * 
     * @param currentUser Tài khoản đang đăng nhập (phải là Admin)
     * @param EmailThuThu Email của thủ thư được xếp ca
     * @param Ngay Ngày làm việc (không được trong quá khứ)
     * @param GioBatDau Giờ bắt đầu ca (nếu là hôm nay, phải sau giờ hiện tại)
     * @param GioKetThuc Giờ kết thúc ca (phải sau giờ bắt đầu)
     * @param GhiChu Ghi chú cho ca làm (có thể null)
     * @return true nếu tạo thành công
     * @throws Exception Lỗi phân quyền, validate hoặc trùng lặp/chồng lấn ca
     */
    public boolean createShift(TaiKhoan currentUser, String EmailThuThu, LocalDate Ngay, LocalTime GioBatDau, LocalTime GioKetThuc, String GhiChu) throws Exception {
        
        AuthMiddleware.requireAdmin(currentUser);

        if(EmailThuThu == null || EmailThuThu.isEmpty()) {
            throw new LichLamException("Email thủ thư không được để trống");
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 1. Kiểm tra ngày: Không được nhỏ hơn hôm nay
        if(Ngay.isBefore(today)) {
           throw new LichLamException("Ngày làm việc không được ở trong quá khứ");
        }

        // 2. Kiểm tra giờ: Nếu là hôm nay, giờ bắt đầu phải lớn hơn giờ hiện tại
        if(Ngay.equals(today) && GioBatDau.isBefore(now)) {
            throw new LichLamException("Giờ bắt đầu đã qua, vui lòng chọn giờ trong tương lai");
        }

        if(GioKetThuc.isBefore(GioBatDau)) {
            throw new LichLamException("Giờ kết thúc phải sau giờ bắt đầu");
        }

        Date sqlDate = Date.valueOf(Ngay);

        return lichLamDAO.createShift(EmailThuThu, sqlDate, GioBatDau, GioKetThuc, GhiChu, currentUser.getEmail());
    }

    /**
     * Lấy danh sách ca làm của một thủ thư trong khoảng thời gian
     * 
     * @param currentUser Tài khoản đang đăng nhập (Admin hoặc chính chủ)
     * @param EmailThuThu Email thủ thư cần xem lịch
     * @param FromDate Ngày bắt đầu
     * @param ToDate Ngày kết thúc (phải >= FromDate)
     * @return Danh sách ca làm sắp xếp theo ngày, giờ
     * @throws Exception Lỗi phân quyền hoặc validate
     */
    public List<LichLam> getShiftsByEmailBetween(TaiKhoan currentUser, String EmailThuThu, Date FromDate, Date ToDate) throws Exception {

        if(currentUser == null) {
            throw new com.mycompany.quanlythuvien.exceptions.AuthException("Chưa đăng nhập");
        }

        // Chỉ Admin hoặc chính chủ mới được xem lịch
        if (!currentUser.getRole().equalsIgnoreCase("Admin") && !currentUser.getEmail().equals(EmailThuThu)) {
            throw new com.mycompany.quanlythuvien.exceptions.AuthException("Không có quyền xem lịch làm của người khác");
        }

        if(EmailThuThu == null || EmailThuThu.isEmpty()) {
             throw new LichLamException("Email thủ thư không được để trống");
        }

        if(ToDate.before(FromDate)) {
             throw new LichLamException("Ngày kết thúc phải sau hoặc cùng ngày bắt đầu");
        }

        return lichLamDAO.getShiftsByEmailBetween(EmailThuThu, FromDate, ToDate);
    }

    /**
     * Hủy ca làm (đánh dấu trạng thái 'Cancelled', không xóa khỏi DB)
     * Dùng khi muốn giữ lại lịch sử audit
     * 
     * @param currentUser Tài khoản đang đăng nhập (phải là Admin)
     * @param idLich ID ca làm cần hủy
     * @return true nếu hủy thành công
     * @throws Exception Lỗi phân quyền hoặc không tìm thấy ca
     */
    public boolean cancelShift(TaiKhoan currentUser, int idLich) throws Exception {
        AuthMiddleware.requireAdmin(currentUser);
        return lichLamDAO.cancelShift(idLich);
    }
    
    /**
     * Lấy toàn bộ lịch làm trong khoảng thời gian (dành cho Admin xem tổng quan)
     * 
     * @param currentUser Tài khoản đang đăng nhập (phải là Admin)
     * @param fromDate Ngày bắt đầu
     * @param toDate Ngày kết thúc (phải >= fromDate)
     * @return Danh sách tất cả ca làm của mọi thủ thư, sắp xếp theo ngày, giờ
     * @throws Exception Lỗi phân quyền hoặc validate
     */
    public List<LichLam> getSchedulesByDateRange(TaiKhoan currentUser, LocalDate fromDate, LocalDate toDate) throws Exception {
        
        AuthMiddleware.requireAdmin(currentUser);
        
        if (toDate.isBefore(fromDate)) {
            throw new LichLamException("Ngày kết thúc phải sau hoặc cùng ngày bắt đầu");
        }
        
        Date sqlFromDate = Date.valueOf(fromDate);
        Date sqlToDate = Date.valueOf(toDate);
        
        return lichLamDAO.getSchedulesByDateRange(sqlFromDate, sqlToDate);
    }
    
    /**
     * Xóa vĩnh viễn ca làm khỏi DB
     * Nên dùng cho ca chưa diễn ra; với ca đã qua hoặc đang diễn ra, ưu tiên cancelShift
     * 
     * @param currentUser Tài khoản đang đăng nhập (phải là Admin)
     * @param idLich ID ca làm cần xóa
     * @return true nếu xóa thành công
     * @throws Exception Lỗi phân quyền hoặc không tìm thấy ca
     */
    public boolean deleteSchedule(TaiKhoan currentUser, int idLich) throws Exception {
        AuthMiddleware.requireAdmin(currentUser);
        return lichLamDAO.deleteSchedule(idLich);
    }
    
    /**
     * Cập nhật thông tin ca làm (email thủ thư, ngày giờ, trạng thái, ghi chú)
     * 
     * @param currentUser Tài khoản đang đăng nhập (phải là Admin)
     * @param idLich ID ca làm cần cập nhật
     * @param emailThuThu Email thủ thư mới (có thể thay đổi người làm)
     * @param ngay Ngày làm việc mới
     * @param gioBatDau Giờ bắt đầu mới
     * @param gioKetThuc Giờ kết thúc mới (phải sau giờ bắt đầu)
     * @param trangThai Trạng thái mới (Scheduled, Done, Cancelled,...)
     * @param ghiChu Ghi chú mới
     * @return true nếu cập nhật thành công
     * @throws Exception Lỗi phân quyền, validate hoặc trùng lặp/chồng lấn ca
     */
    public boolean updateSchedule(TaiKhoan currentUser, int idLich, String emailThuThu, 
                                 LocalDate ngay, LocalTime gioBatDau, LocalTime gioKetThuc,
                                 String trangThai, String ghiChu) throws Exception {
        AuthMiddleware.requireAdmin(currentUser);
        
        if (emailThuThu == null || emailThuThu.isEmpty()) {
            throw new LichLamException("Email thủ thư không được để trống");
        }
        
        if (gioKetThuc.isBefore(gioBatDau)) {
            throw new LichLamException("Giờ kết thúc phải sau giờ bắt đầu");
        }
        
        Date sqlDate = Date.valueOf(ngay);
        
        return lichLamDAO.updateSchedule(idLich, emailThuThu, sqlDate, gioBatDau, gioKetThuc, 
                                        trangThai, ghiChu);
    }
}
