package com.mycompany.quanlythuvien.controller;

import java.util.List;

import com.mycompany.quanlythuvien.dao.ThongBaoDAO;
import com.mycompany.quanlythuvien.exceptions.ThongBaoException;
import com.mycompany.quanlythuvien.middleware.AuthMiddleware;
import com.mycompany.quanlythuvien.model.TaiKhoan;
import com.mycompany.quanlythuvien.model.ThongBaoAdminListResult;
import com.mycompany.quanlythuvien.model.ThongBaoListResult;
import com.mycompany.quanlythuvien.model.ThongBaoNguoiNhan;

/**
 * @author Tien
 */
public class ThongBaoController {
    private final ThongBaoDAO dao = new ThongBaoDAO();
    
    /**
     * @param currentUser Người dùng hiện tại (phải là admin)
     * @param title Tiêu đề thông báo
     * @param content Nội dung thông báo
     * @param recipients Danh sách email người nhận
     * @return ID của thông báo vừa tạo
     * @throws Exception Nếu validation fail hoặc lỗi database
     */
    public int createAnnouncement(TaiKhoan currentUser, String title, String content, List<String> recipients) throws Exception {
        AuthMiddleware.requireAdmin(currentUser);
        
        // Validations
        if (title == null || title.trim().isEmpty()) {
            throw new ThongBaoException("Tiêu đề không được để trống");
        }
        if (title.length() > 200) {
            throw new ThongBaoException("Tiêu đề không được vượt quá 200 ký tự");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new ThongBaoException("Nội dung không được để trống");
        }
        if (content.length() > 2000) {
            throw new ThongBaoException("Nội dung không được vượt quá 2000 ký tự");
        }

        if (recipients == null || recipients.isEmpty()) {
            throw new ThongBaoException("Danh sách người nhận không được để trống");
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        for (String email : recipients) {
            if (email == null || !email.matches(emailRegex)) {
                throw new ThongBaoException("Email không hợp lệ: " + email);
            }
        }
        
        // Tạo thông báo
        return dao.createAnnouncement(title.trim(), content.trim(), recipients, currentUser.getEmail());
    }
    
    /**
     * @param currentUser Người dùng hiện tại
     * @param unreadOnly true = chỉ lấy chưa đọc, false = lấy tất cả
     * @param lastIdCursor ID thông báo cuối từ page trước (null = page đầu)
     * @param pageSize Số lượng thông báo trên 1 page (1-100)
     * @return ThongBaoListResult chứa danh sách thông báo và thông tin phân trang
     * @throws Exception Nếu validation fail hoặc lỗi database
     */
    public ThongBaoListResult listByReceiver(TaiKhoan currentUser, boolean unreadOnly, Integer lastIdCursor, int pageSize) throws Exception {
        // Validate pageSize
        if (pageSize < 1 || pageSize > 100) {
            throw new ThongBaoException("Page size phải từ 1 đến 100");
        }
        
        return dao.listByReceiver(currentUser.getEmail(), unreadOnly, lastIdCursor, pageSize);
    }
    
    /**
     * @param currentUser Người dùng hiện tại
     * @param idThongBao ID của thông báo cần đánh dấu đọc
     * @return true nếu thành công
     * @throws Exception Nếu không tìm thấy thông báo hoặc người dùng không có quyền
     */
    public boolean markRead(TaiKhoan currentUser, int idThongBao) throws Exception {
        if (idThongBao <= 0) {
            throw new ThongBaoException("ID thông báo không hợp lệ");
        }
        
        return dao.markRead(idThongBao, currentUser.getEmail());
    }
    
    /**
     * @param currentUser Người dùng hiện tại
     * @return Số lượng thông báo chưa đọc
     * @throws Exception Nếu có lỗi database
     */
    public int getUnreadCount(TaiKhoan currentUser) throws Exception {
        return dao.getUnreadCount(currentUser.getEmail());
    }
    
    /**
     * @param currentUser Người dùng hiện tại
     * @return Số lượng thông báo đã được đánh dấu đọc
     * @throws Exception Nếu có lỗi database
     */
    public int markAllRead(TaiKhoan currentUser) throws Exception {
        return dao.markAllRead(currentUser.getEmail());
    }
    
    /**
     * @param currentUser Người dùng hiện tại (phải là admin)
     * @param lastIdCursor ID thông báo cuối từ page trước (null = page đầu)
     * @param pageSize Số lượng thông báo trên 1 page (1-100)
     * @return ThongBaoAdminListResult chứa danh sách và thông tin phân trang
     * @throws Exception Nếu validation fail hoặc lỗi database
     */
    public ThongBaoAdminListResult listAllForAdmin(TaiKhoan currentUser, Integer lastIdCursor, int pageSize) throws Exception {
        // Check quyền admin
        AuthMiddleware.requireAdmin(currentUser);
        
        // Validate pageSize
        if (pageSize < 1 || pageSize > 100) {
            throw new ThongBaoException("Page size phải từ 1 đến 100");
        }
        
        return dao.listAllForAdmin(lastIdCursor, pageSize);
    }
    
    /** 
     * @param currentUser Người dùng hiện tại (phải là admin)
     * @param idThongBao ID của thông báo cần xem chi tiết
     * @return List các ThongBaoNguoiNhan
     * @throws Exception Nếu validation fail hoặc lỗi database
     */
    public List<ThongBaoNguoiNhan> getRecipientsByAnnouncementId(TaiKhoan currentUser, int idThongBao) throws Exception {
        // Check quyền admin
        AuthMiddleware.requireAdmin(currentUser);
        
        if (idThongBao <= 0) {
            throw new ThongBaoException("ID thông báo không hợp lệ");
        }
        
        return dao.getRecipientsByAnnouncementId(idThongBao);
    }
}
