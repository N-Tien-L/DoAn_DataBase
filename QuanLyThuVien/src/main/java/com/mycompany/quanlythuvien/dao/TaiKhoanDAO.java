package com.mycompany.quanlythuvien.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.mycompany.quanlythuvien.model.TaiKhoan;
import com.mycompany.quanlythuvien.model.TaiKhoanProfile;
import com.mycompany.quanlythuvien.util.DBConnector;
import com.mycompany.quanlythuvien.util.PasswordUtil;

/**
 *
 * @author Tien
 */
public class TaiKhoanDAO {

    private static final String SQL_LOGIN = "SELECT * FROM TAIKHOAN WHERE Email = ?";
    private static final String SQL_CREATE_ACCOUNT = "INSERT INTO TAIKHOAN (Email, [Password], HoTen, [Role], CreatedBy) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_COUNT_ACCOUNTS = "SELECT COUNT(*) as Total FROM TAIKHOAN";
    private static final String SQL_UPDATE = "UPDATE TAIKHOAN SET HoTen = ?, [Role] = ? WHERE Email = ?";
    private static final String SQL_DELETE = "DELETE FROM TAIKHOAN WHERE Email = ?";
    private static final String SQL_UPDATE_PASSWORD = "UPDATE TAIKHOAN SET [Password] = ? WHERE Email = ?";
    private static final String SQL_GET_ACCOUNTS_FIRST_PAGE = "SELECT TOP (?) Email, HoTen, [Role] FROM TAIKHOAN ORDER BY Email ASC";
    private static final String SQL_GET_ACCOUNTS_NEXT_PAGE = "SELECT TOP (?) Email, HoTen, [Role] FROM TAIKHOAN WHERE Email > ? ORDER BY Email ASC";
    
    // Search tài khoản theo keyword (Email hoặc HoTen)
    private static final String SQL_SEARCH_ACCOUNTS = 
        "SELECT TOP (?) Email, HoTen, [Role], Status " +
        "FROM TAIKHOAN " +
        "WHERE (Email LIKE ? OR HoTen LIKE ?) " +
        "  AND (? IS NULL OR Email > ?) " +
        "ORDER BY Email ASC";
    
    // Lấy profile chi tiết từ view
    private static final String SQL_GET_PROFILE = "SELECT * FROM VW_TAIKHOAN_ProfileStats WHERE Email = ?";

    public TaiKhoan checkLogin(String email, String password) {
        // try-with-resource (tự động đóng conn, rs, ps)
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_LOGIN);

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("Password");

                if(PasswordUtil.checkPassword(password, hashedPassword)) {
                        System.out.println("Login successfully");
                        return new TaiKhoan(
                        rs.getString("Email"),
                        rs.getString("HoTen"),
                        rs.getString("Role")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Login failed");
        return null;
    }

    public boolean createAccount(TaiKhoan taiKhoan, String createdBy) {
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_CREATE_ACCOUNT);

            ps.setString(1, taiKhoan.getEmail());
            ps.setString(2, taiKhoan.getPassword());
            ps.setString(3, taiKhoan.getHoTen());
            ps.setString(4, taiKhoan.getRole());
            ps.setString(5, createdBy);
            int rowAffected = ps.executeUpdate();

            if(rowAffected > 0) {
                System.out.println("Insert account successfully (created by: " + createdBy + ")");
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Cập nhật thông tin tài khoản (chỉ HoTen và Role)
     * Không update Password - dùng updatePassword() riêng
     */
    public boolean updateAccount(TaiKhoan taiKhoan) {
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_UPDATE);

            ps.setString(1, taiKhoan.getHoTen());
            ps.setString(2, taiKhoan.getRole());
            ps.setString(3, taiKhoan.getEmail());
            
            int rowAffected = ps.executeUpdate();

            if(rowAffected > 0) {
                System.out.println("Update account successfully");
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    /**
     * Cập nhật mật khẩu (chỉ update password, tối ưu hơn)
     * @param email Email của tài khoản
     * @param hashedPassword Mật khẩu đã được hash
     * @return true nếu update thành công
     */
    public boolean updatePassword(String email, String hashedPassword) {        
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_PASSWORD);

            ps.setString(1, hashedPassword);
            ps.setString(2, email);
            
            int rowAffected = ps.executeUpdate();

            if(rowAffected > 0) {
                System.out.println("Update password successfully for: " + email);
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    public boolean deleteAccount(String email) {
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_DELETE);
            ps.setString(1, email);
            
            int rowAffected = ps.executeUpdate();

            if(rowAffected > 0) {
                System.out.println("Delete account successfully");
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    public java.util.List<TaiKhoan> getAllAccounts(String lastEmailCursor, int pageSize) {
        java.util.List<TaiKhoan> accounts = new java.util.ArrayList<>();
        
        boolean isFirstPage = (lastEmailCursor == null || lastEmailCursor.trim().isEmpty());
        String sql = isFirstPage ? SQL_GET_ACCOUNTS_FIRST_PAGE : SQL_GET_ACCOUNTS_NEXT_PAGE;

        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);

            if(isFirstPage) {
                ps.setInt(1, pageSize);
            } else {
                ps.setInt(1, pageSize);
                ps.setString(2, lastEmailCursor);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                TaiKhoan taiKhoan = new TaiKhoan(
                    rs.getString("Email"),
                    rs.getString("HoTen"),
                    rs.getString("Role")
                );
                accounts.add(taiKhoan);
            }

            System.out.println("Retrieved " + accounts.size() + " accounts (cursor: " + lastEmailCursor + ")");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return accounts;
    }
    
    public int getTotalAccounts() {
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_COUNT_ACCOUNTS);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("Total");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Tìm kiếm tài khoản theo keyword với cursor pagination
     * @param keyword Từ khóa tìm kiếm (Email hoặc HoTen)
     * @param lastEmailCursor Email cuối cùng của trang trước (null = trang đầu)
     * @param pageSize Số lượng record trên 1 trang
     * @return Danh sách tài khoản
     */
    public java.util.List<TaiKhoan> searchAccounts(String keyword, String lastEmailCursor, int pageSize) {
        java.util.List<TaiKhoan> accounts = new java.util.ArrayList<>();
        String searchKeyword = (keyword == null ? "" : keyword.trim());
        String likePattern = "%" + searchKeyword + "%";
        
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_SEARCH_ACCOUNTS);
            
            ps.setInt(1, pageSize);
            ps.setString(2, likePattern);
            ps.setString(3, likePattern);
            
            // Cursor pagination
            if (lastEmailCursor == null || lastEmailCursor.trim().isEmpty()) {
                ps.setNull(4, java.sql.Types.VARCHAR);
                ps.setNull(5, java.sql.Types.VARCHAR);
            } else {
                ps.setString(4, lastEmailCursor);
                ps.setString(5, lastEmailCursor);
            }
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                TaiKhoan taiKhoan = new TaiKhoan(
                    rs.getString("Email"),
                    rs.getString("HoTen"),
                    rs.getString("Role")
                );
                accounts.add(taiKhoan);
            }
            
            System.out.println("Search found " + accounts.size() + " accounts (keyword: '" + searchKeyword + ")");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return accounts;
    }
    
    /**
     * Lấy profile chi tiết của 1 tài khoản (bao gồm thống kê)
     * @param email Email của tài khoản
     * @return Map chứa thông tin chi tiết từ view VW_TAIKHOAN_ProfileStats
     */
    public TaiKhoanProfile getAccountProfile(String email) {
        TaiKhoanProfile profile = new TaiKhoanProfile();
        
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_GET_PROFILE);
            ps.setString(1, email);
            
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                profile = new TaiKhoanProfile(
                    rs.getString("Email"),
                    rs.getString("HoTen"),
                    rs.getString("Role"),
                    rs.getString("Status"),
                    rs.getTimestamp("CreatedAt"),
                    rs.getString("CreatedBy"),
                    rs.getInt("SoBanDocTao"),
                    rs.getInt("SoSachThem"),
                    rs.getInt("SoBanSaoNhap"),
                    rs.getInt("SoPhieuMuonLap"),
                    rs.getInt("SoBanSaoDangChoMuon")
                );
                
                System.out.println("Retrieved profile for: " + email);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return profile;
    }
}
