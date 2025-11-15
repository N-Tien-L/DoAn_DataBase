package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.TaiKhoanDAO;
import com.mycompany.quanlythuvien.model.TaiKhoan;
import com.mycompany.quanlythuvien.util.PasswordUtil;
import com.mycompany.quanlythuvien.util.EmailSender;
import java.util.Random;

/**
 *
 * @author Tien
 */
public class TaiKhoanController {
    private final TaiKhoanDAO dao = new TaiKhoanDAO();

    public TaiKhoan login(String email, String password) {
        return dao.checkLogin(email, password);
//         return new TaiKhoan(email, "Admin Tiên", "Admin"); // bypass real login to SEEDING test account (admin)
    }
    
    public boolean createAccount(String currentUserRole, String email, String hoTen, String role) {
        // Kiểm tra quyền admin
        if (currentUserRole == null || !currentUserRole.equalsIgnoreCase("admin")) {
            System.out.println("Chỉ admin mới có quyền tạo tài khoản");
            return false;
        }
        
        // Validation
        if (email == null || email.trim().isEmpty()) {
            System.out.println("Email không được để trống");
            return false;
        }
        
        // Generate random 6-digit password
        Random random = new Random();
        String generatedPassword = String.format("%06d", random.nextInt(1000000));
        
        // hash password
        String hashedPassword = PasswordUtil.hashPassword(generatedPassword);
        
        TaiKhoan taiKhoan = new TaiKhoan(email, hashedPassword, hoTen, role);
        boolean result = dao.createAccount(taiKhoan);
        
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
    public boolean updateAccount(String currentUserRole, String email, String hoTen, String role) {
        // Kiểm tra quyền admin
        if (currentUserRole == null || !currentUserRole.equalsIgnoreCase("admin")) {
            System.out.println("Chỉ admin mới có quyền cập nhật tài khoản");
            return false;
        }
        
        // Validation
        if (email == null || email.trim().isEmpty()) {
            System.out.println("Email không được để trống");
            return false;
        }
        
        if (hoTen == null || hoTen.trim().isEmpty()) {
            System.out.println("Họ tên không được để trống");
            return false;
        }
        
        // Password = null nghĩa là không update password
        TaiKhoan taiKhoan = new TaiKhoan(email, null, hoTen, role);
        return dao.updateAccount(taiKhoan);
    }
    
    public boolean deleteAccount(String currentUserRole, String email) {
        // Kiểm tra quyền admin
        if (currentUserRole == null || !currentUserRole.equalsIgnoreCase("admin")) {
            System.out.println("Chỉ admin mới có quyền xóa tài khoản");
            return false;
        }
        
        // Validation
        if (email == null || email.trim().isEmpty()) {
            System.out.println("Email không được để trống");
            return false;
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
    public boolean changePassword(String email, String oldPassword, String newPassword) {
        // Validation
        if (email == null || email.trim().isEmpty()) {
            System.out.println("Email không được để trống");
            return false;
        }
        
        if (oldPassword == null || oldPassword.isEmpty()) {
            System.out.println("Mật khẩu cũ không được để trống");
            return false;
        }
        
        if (newPassword == null || newPassword.length() < 6) {
            System.out.println("Mật khẩu mới phải có ít nhất 6 ký tự");
            return false;
        }
        
        // Xác thực mật khẩu cũ
        TaiKhoan taiKhoan = dao.checkLogin(email, oldPassword);
        if (taiKhoan == null) {
            System.out.println("Mật khẩu cũ không chính xác");
            return false;
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
     * @param currentUserRole Role của user hiện tại (phải là admin)
     * @param email Email của tài khoản cần reset
     * @return true nếu reset thành công
     */
    public boolean resetPassword(String currentUserRole, String email) {
        // Kiểm tra quyền admin
        if (currentUserRole == null || !currentUserRole.equalsIgnoreCase("admin")) {
            System.out.println("Chỉ admin mới có quyền reset mật khẩu");
            return false;
        }
        
        // Validation
        if (email == null || email.trim().isEmpty()) {
            System.out.println("Email không được để trống");
            return false;
        } 

        // Generate random 6-digit password
        Random random = new Random();
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
     * @param currentUserRole Role của user hiện tại
     * @param page Số trang (bắt đầu từ 1)
     * @param pageSize Số bản ghi mỗi trang
     * @return List các tài khoản, hoặc null nếu không có quyền
     */
    public java.util.List<TaiKhoan> getAllAccounts(String currentUserRole, String lastEmailCursor, int pageSize) {
        // Kiểm tra quyền admin
        if (currentUserRole == null || !currentUserRole.equalsIgnoreCase("admin")) {
            System.out.println("Chỉ admin mới có quyền xem danh sách tài khoản");
            return null;
        }
        
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10; // Default page size
        }
        
        return dao.getAllAccounts(lastEmailCursor, pageSize);
    }
    
    /**
     * Lấy tổng số tài khoản (chỉ admin)
     * @param currentUserRole Role của user hiện tại
     * @return Tổng số tài khoản, hoặc -1 nếu không có quyền
     */
    public int getTotalAccounts(String currentUserRole) {
        // Kiểm tra quyền admin
        if (currentUserRole == null || !currentUserRole.equalsIgnoreCase("admin")) {
            System.out.println("Chỉ admin mới có quyền xem thống kê tài khoản");
            return -1;
        }
        
        return dao.getTotalAccounts();
    }
    
    /**
     * Tính tổng số trang
     * @param currentUserRole Role của user hiện tại
     * @param pageSize Số bản ghi mỗi trang
     * @return Tổng số trang
     */
    public int getTotalPages(String currentUserRole, int pageSize) {
        int total = getTotalAccounts(currentUserRole);
        if (total <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) total / pageSize);
    }
}
