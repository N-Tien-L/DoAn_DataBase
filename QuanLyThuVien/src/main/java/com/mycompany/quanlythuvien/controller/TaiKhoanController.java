package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.TaiKhoanDAO;
import com.mycompany.quanlythuvien.exceptions.AuthException;
import com.mycompany.quanlythuvien.exceptions.TaiKhoanException;
import com.mycompany.quanlythuvien.middleware.AuthMiddleware;
import com.mycompany.quanlythuvien.model.TaiKhoan;
import com.mycompany.quanlythuvien.model.TaiKhoanProfile;
import com.mycompany.quanlythuvien.util.PasswordUtil;
import com.mycompany.quanlythuvien.util.EmailSender;

/**
 *
 * @author Tien
 */
public class TaiKhoanController {
    private final TaiKhoanDAO dao = new TaiKhoanDAO();

    public TaiKhoan login(String email, String password) throws Exception {
        if(email == null || email.isEmpty()) {
            throw new AuthException("Email không được để trống");
        }

        if(password == null || password.isEmpty()) {
            throw new AuthException("Mật khẩu không được để trống");
        }

        return dao.checkLogin(email, password);
        // return new TaiKhoan("admin@thuvien.com", "Seeded Admin", "Admin"); // bypass real login to SEED test account (admin)
    }
    
    public boolean createAccount(TaiKhoan currentUser, String email, String hoTen, String role) throws Exception {
        AuthMiddleware.requireAdmin(currentUser);
        
        // Validation
        if (email == null || email.trim().isEmpty()) {
            throw new TaiKhoanException("Email không được để trống");
        }
        
        if (hoTen == null || hoTen.trim().isEmpty()) {
            throw new TaiKhoanException("Họ tên không được để trống");
        }

        // Validate Role
        if (role == null || (!role.equalsIgnoreCase("Admin") && !role.equalsIgnoreCase("ThuThu"))) {
            throw new TaiKhoanException("Vai trò không hợp lệ. Chỉ chấp nhận 'Admin' hoặc 'ThuThu'");
        }
        
        // Generate random 6-digit password
        java.security.SecureRandom random = new java.security.SecureRandom();
        String generatedPassword = String.format("%06d", random.nextInt(1000000));
        
        // hash password
        String hashedPassword = PasswordUtil.hashPassword(generatedPassword);
        
        TaiKhoan taiKhoan = new TaiKhoan(email, hashedPassword, hoTen, role);
        boolean result = dao.createAccount(taiKhoan, currentUser.getEmail());
        
        // Gửi email nếu tạo tài khoản thành công
        if (result) {
            try {
                EmailSender emailSender = new EmailSender();
                String subject = "Tài khoản thư viện mới được tạo";
                String body = "Xin chào " + hoTen + ",\n\n" +
                             "Tài khoản thư viện của bạn đã được tạo thành công.\n\n" +
                             "Email: " + email + "\n" +
                             "Mật khẩu: " + generatedPassword + "\n\n" +
                             "Vui lòng đổi mật khẩu sau khi đăng nhập lần đầu.\n\n" +
                             "Trân trọng,\n" +
                             "Hệ thống quản lý thư viện";
                
                emailSender.sendEmail(email, subject, body);
                System.out.println("Đã gửi email thông báo tới " + email);
            } catch (Exception e) {
                System.err.println("Lỗi khi gửi email: " + e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * Cập nhật thông tin tài khoản (chỉ họ tên và vai trò)
     * Email là primary key nên không thể thay đổi
     * Password không được update qua hàm này - dùng changePassword hoặc resetPassword
     */
    public boolean updateAccount(TaiKhoan currentUser, String email, String hoTen, String role) throws Exception {
        
        AuthMiddleware.requireAdmin(currentUser);

        // Validation
        if (email == null || email.trim().isEmpty()) {
            throw new TaiKhoanException("Email không được trống");
        }
        
        if (hoTen == null || hoTen.trim().isEmpty()) {
            throw new TaiKhoanException("Họ tên không được trống");
        }

        // Validate Role
        if (role == null || (!role.equalsIgnoreCase("Admin") && !role.equalsIgnoreCase("ThuThu"))) {
            throw new TaiKhoanException("Vai trò không hợp lệ. Chỉ chấp nhận 'Admin' hoặc 'ThuThu'");
        }
        
        // Password = null nghĩa là không update password
        TaiKhoan taiKhoan = new TaiKhoan(email, hoTen, role);
        return dao.updateAccount(taiKhoan);
    }
    
    public boolean deleteAccount(TaiKhoan currentUser, String email) throws Exception {
        AuthMiddleware.requireAdmin(currentUser);
        
        // Validation
        if (email == null || email.trim().isEmpty()) {
            throw new TaiKhoanException("Email không được để trống");
        }

        // Prevent self-deletion
        if (email.equals(currentUser.getEmail())) {
            throw new TaiKhoanException("Không thể tự xóa tài khoản của chính mình");
        }
        
        return dao.deleteAccount(email);
    }
    
    /**
     * Đổi mật khẩu (user tự đổi - cần mật khẩu cũ để xác thực)
     * @param email Email của tài khoản
     * @param oldPassword Mật khẩu cũ (để xác thực)
     * @param newPassword Mật khẩu mới
     * @return true nếu đổi thành công
     */
    public boolean changePassword(String email, String oldPassword, String newPassword) throws TaiKhoanException {
        // Validation
        if (email == null || email.trim().isEmpty()) {
            throw new TaiKhoanException("Email không được để trống");
        }
        
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new TaiKhoanException("Mật khẩu cũ không được để trống");
        }
        
        if (newPassword == null || newPassword.length() < 6) {
            throw new TaiKhoanException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }
        
        // Xác thực mật khẩu cũ
        TaiKhoan taiKhoan = dao.checkLogin(email, oldPassword);
        if (taiKhoan == null) {
            throw new TaiKhoanException("Mật khẩu cũ không chính xác");
        }
        
        // Hash mật khẩu mới và update
        String hashedNewPassword = PasswordUtil.hashPassword(newPassword);
        boolean result = dao.updatePassword(email, hashedNewPassword);
        
        if (result) {
            System.out.println("Đổi mật khẩu thành công cho: " + email);
        }
        
        return result;
    }
    
    /**
     * Reset mật khẩu (admin thực hiện khi user quên mật khẩu)
     * Generate mật khẩu mới 6 số và gửi email cho user
     * @param currentUser Tài khoản hiện tại (phải là admin)
     * @param email Email của tài khoản cần reset
     * @return true nếu reset thành công
     */
    public boolean resetPassword(TaiKhoan currentUser, String email) throws Exception {
        AuthMiddleware.requireAdmin(currentUser);
        
        // Validation
        if (email == null || email.trim().isEmpty()) {
            throw new TaiKhoanException("Email không được để trống");
        } 

        // Generate random 6-digit password
        java.security.SecureRandom random = new java.security.SecureRandom();
        String newPassword = String.format("%06d", random.nextInt(1000000));
        
        // Hash password
        String hashedPassword = PasswordUtil.hashPassword(newPassword);
        
        // Update password (chỉ update mỗi password, tối ưu hơn)
        boolean result = dao.updatePassword(email, hashedPassword);
        
        // Gửi email nếu reset thành công
        if (result) {
            try {
                EmailSender emailSender = new EmailSender();
                String subject = "Cấp lại mật khẩu tài khoản thư viện";
                String body = "Xin chào!\n\n" +
                             "Mật khẩu tài khoản thư viện của bạn đã được cấp lại theo yêu cầu.\n\n" +
                             "Email: " + email + "\n" +
                             "Mật khẩu mới: " + newPassword + "\n\n" +
                             "Vui lòng đổi mật khẩu sau khi đăng nhập.\n\n" +
                             "Nếu bạn không yêu cầu cấp lại mật khẩu, vui lòng liên hệ admin ngay.\n\n" +
                             "Trân trọng,\n" +
                             "Hệ thống quản lý thư viện";
                
                emailSender.sendEmail(email, subject, body);
                System.out.println("Đã gửi email mật khẩu mới tới " + email);
            } catch (Exception e) {
                System.err.println("Lỗi khi gửi email: " + e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * Lấy danh sách tất cả tài khoản với phân trang (chỉ admin)
     * @param currentUser Tài khoản hiện tại
     * @param lastEmailCursor Email cuối cùng của trang trước
     * @param pageSize Số bản ghi mỗi trang
     * @return List các tài khoản
     */
    public java.util.List<TaiKhoan> getAllAccounts(TaiKhoan currentUser, String lastEmailCursor, int pageSize) throws Exception {
        AuthMiddleware.requireAdmin(currentUser);
        
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10; // Default page size
        }
        
        return dao.getAllAccounts(lastEmailCursor, pageSize);
    }
    
    /**
     * Lấy tổng số tài khoản (chỉ admin)
     * @param currentUser Tài khoản hiện tại
     * @return Tổng số tài khoản
     */
    public int getTotalAccounts(TaiKhoan currentUser) throws Exception {
        AuthMiddleware.requireAdmin(currentUser);
        
        return dao.getTotalAccounts();
    }
    
    /**
     * Tính tổng số trang
     * @param currentUser Tài khoản hiện tại
     * @param pageSize Số bản ghi mỗi trang
     * @return Tổng số trang
     */
    public int getTotalPages(TaiKhoan currentUser, int pageSize) throws Exception {
        int total = getTotalAccounts(currentUser);
        if (total <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / pageSize);
    }
    
    /**
     * Tìm kiếm tài khoản theo keyword với phân trang
     * @param currentUser Tài khoản hiện tại (phải là admin)
     * @param keyword Từ khóa tìm kiếm (Email hoặc HoTen)
     * @param lastEmailCursor Email cuối cùng của trang trước (null = trang đầu)
     * @param pageSize Số lượng record trên 1 trang
     * @return Danh sách tài khoản
     */
    public java.util.List<TaiKhoan> searchAccounts(TaiKhoan currentUser, String keyword, String lastEmailCursor, int pageSize) throws Exception {
        AuthMiddleware.requireAdmin(currentUser);
        
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10; // Default page size
        }
        
        return dao.searchAccounts(keyword, lastEmailCursor, pageSize);
    }
    
    /**
     * Lấy profile chi tiết của 1 tài khoản (bao gồm thống kê)
     * @param currentUser Tài khoản hiện tại (phải là admin)
     * @param email Email của tài khoản cần xem
     * @return Map chứa thông tin chi tiết
     */
    public TaiKhoanProfile getAccountProfile(TaiKhoan currentUser, String email) throws Exception {
        AuthMiddleware.requireAdmin(currentUser);
        
        return dao.getAccountProfile(email);
    }
    
    /**
     * Lấy danh sách rút gọn (Email, HoTen, Role) của tất cả tài khoản
     * Dùng cho các combobox chọn tài khoản
     */
    public java.util.List<TaiKhoan> getAllAccountsSimple(TaiKhoan currentUser) throws Exception {
        AuthMiddleware.requireAdmin(currentUser);
        return dao.getAllAccountsSimple();
    }
}
