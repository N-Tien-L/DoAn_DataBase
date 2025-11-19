package com.mycompany.quanlythuvien.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.mycompany.quanlythuvien.model.YeuCauResetMK;
import com.mycompany.quanlythuvien.util.DBConnector;

/**
 * @author Tien
 */
public class YeuCauResetMKDAO {
    
    private static final String SQL_CREATE = 
        "INSERT INTO YEUCAU_RESETMK (EmailThuThu, LyDo) VALUES (?, ?)";
    
    private static final String SQL_GET_PENDING = 
        "SELECT y.Id, y.EmailThuThu, y.LyDo, y.TrangThai, y.CreatedAt, t.HoTen as HoTenThuThu " +
        "FROM YEUCAU_RESETMK y " +
        "LEFT JOIN TAIKHOAN t ON y.EmailThuThu = t.Email " +
        "WHERE y.TrangThai = 'Pending' " +
        "ORDER BY y.CreatedAt ASC";
    
    private static final String SQL_UPDATE_STATUS = 
        "UPDATE YEUCAU_RESETMK " +
        "SET TrangThai = ?, XuLyBoi = ?, XuLyLuc = SYSUTCDATETIME() " +
        "WHERE Id = ? AND TrangThai = 'Pending'";
    
    private static final String SQL_CHECK_EMAIL_EXISTS = 
        "SELECT Email FROM TAIKHOAN WHERE Email = ?";

    /**
     * Tạo yêu cầu reset (không cần login)
     */
    public void createYeuCauResetMK(String email, String lyDo) throws Exception {
        // Kiểm tra email tồn tại
        boolean emailExists = false;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement psCheck = conn.prepareStatement(SQL_CHECK_EMAIL_EXISTS)) {
            
            psCheck.setString(1, email);
            ResultSet rs = psCheck.executeQuery();
            emailExists = rs.next();
        }
        
        // Chỉ tạo yêu cầu nếu email tồn tại
        // Không throw exception để attacker không biết email có tồn tại hay không
        if (emailExists) {
            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(SQL_CREATE)) {
                
                ps.setString(1, email);
                ps.setString(2, lyDo);
                ps.executeUpdate();
            }
        }
    }

    /**
     * Lấy danh sách yêu cầu chờ duyệt
     */
    public List<YeuCauResetMK> getPendingYeuCau() throws Exception {
        List<YeuCauResetMK> list = new ArrayList<>();
        
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_PENDING);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                YeuCauResetMK yc = new YeuCauResetMK();
                yc.setId(rs.getInt("Id"));
                yc.setEmailThuThu(rs.getString("EmailThuThu"));
                yc.setLyDo(rs.getString("LyDo"));
                yc.setTrangThai(rs.getString("TrangThai"));
                yc.setHoTenThuThu(rs.getString("HoTenThuThu"));
                
                Timestamp ts = rs.getTimestamp("CreatedAt");
                if (ts != null) yc.setCreatedAt(ts.toLocalDateTime());
                
                list.add(yc);
            }
        }
        
        return list;
    }

    /**
     * Cập nhật trạng thái (Done hoặc Rejected)
     */
    public boolean updateStatus(int id, String trangThai, String xuLyBoi) throws Exception {
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATUS)) {
            
            ps.setString(1, trangThai);
            ps.setString(2, xuLyBoi);
            ps.setInt(3, id);
            
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new Exception("Yêu cầu không tồn tại hoặc đã được xử lý");
            }
            
            return true;
        }
    }
}
