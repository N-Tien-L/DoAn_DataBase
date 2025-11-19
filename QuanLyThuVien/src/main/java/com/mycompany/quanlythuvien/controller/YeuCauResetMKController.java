package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.YeuCauResetMKDAO;
import com.mycompany.quanlythuvien.middleware.AuthMiddleware;
import com.mycompany.quanlythuvien.model.TaiKhoan;
import com.mycompany.quanlythuvien.model.YeuCauResetMK;
import java.util.List;

/**
 * @author Tien
 */
public class YeuCauResetMKController {
    
    private final YeuCauResetMKDAO dao = new YeuCauResetMKDAO();
    private final TaiKhoanController taiKhoanController = new TaiKhoanController();

    /**
     * Tạo yêu cầu reset mật khẩu (không cần login)
     */
    public void createYeuCau(String email, String lyDo) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("Email không được để trống");
        }
        
        if (lyDo == null || lyDo.trim().isEmpty()) {
            throw new Exception("Vui lòng nhập lý do");
        }
        
        dao.createYeuCauResetMK(email.trim(), lyDo.trim());
    }

    /**
     * Lấy danh sách yêu cầu chờ duyệt (Admin)
     */
    public List<YeuCauResetMK> getPendingYeuCau(TaiKhoan currentUser) throws Exception {
        AuthMiddleware.requireAdmin(currentUser);
        return dao.getPendingYeuCau();
    }

    /**
     * Chấp nhận yêu cầu - Reset mật khẩu ngay
     */
    public boolean approveYeuCau(TaiKhoan currentUser, int id) throws Exception {
        AuthMiddleware.requireAdmin(currentUser);
        
        // Lấy thông tin yêu cầu từ danh sách pending
        List<YeuCauResetMK> pending = dao.getPendingYeuCau();
        YeuCauResetMK yeuCau = pending.stream()
            .filter(yc -> yc.getId() == id)
            .findFirst()
            .orElseThrow(() -> new Exception("Yêu cầu không tồn tại hoặc đã được xử lý"));
        
        // Reset mật khẩu
        boolean resetSuccess = taiKhoanController.resetPassword(
            currentUser, 
            yeuCau.getEmailThuThu()
        );
        
        if (resetSuccess) {
            // Cập nhật trạng thái Done
            dao.updateStatus(id, "Done", currentUser.getEmail());
            return true;
        }
        
        throw new Exception("Reset mật khẩu thất bại");
    }

    /**
     * Từ chối yêu cầu
     */
    public boolean rejectYeuCau(TaiKhoan currentUser, int id) throws Exception {
        AuthMiddleware.requireAdmin(currentUser);
        return dao.updateStatus(id, "Rejected", currentUser.getEmail());
    }
}
