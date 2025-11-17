package com.mycompany.quanlythuvien.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.mycompany.quanlythuvien.exceptions.ThongBaoException;
import com.mycompany.quanlythuvien.model.ThongBaoAdmin;
import com.mycompany.quanlythuvien.model.ThongBaoAdminListResult;
import com.mycompany.quanlythuvien.model.ThongBaoListResult;
import com.mycompany.quanlythuvien.model.ThongBaoNguoiNhan;
import com.mycompany.quanlythuvien.util.DBConnector;

/**
 * @author Tien
 */
public class ThongBaoDAO {
    
    private static final String SQL_CREATE_ANNOUNCEMENT = "INSERT INTO THONGBAO (TieuDe, NoiDung, CreatedBy) VALUES (?, ?, ?)";
    private static final String SQL_INSERT_RECIPIENTS = "INSERT INTO THONGBAO_NGUOINHAN (IdThongBao, EmailNhan) VALUES (?, ?)";
    private static final String SQL_LIST_BY_RECEIVER = 
        """
            SELECT TOP (?)
            tb.IdThongBao, tb.TieuDe, tb.NoiDung, tb.CreatedBy, tb.CreatedAt, 
            tbn.EmailNhan, tbn.DaDoc, tbn.ReadAt
            FROM THONGBAO_NGUOINHAN tbn
            INNER JOIN THONGBAO tb ON tbn.IdThongBao = tb.IdThongBao
            WHERE tbn.EmailNhan = ?
            AND (? = 0 OR tbn.DaDoc = 0)
            AND (? IS NULL OR tb.IdThongBao < ?)
            ORDER BY tb.IdThongBao DESC
        """;
    private static final String SQL_MARK_READ = "UPDATE THONGBAO_NGUOINHAN SET DaDoc = 1, ReadAt = GETUTCDATE() WHERE IdThongBao = ? AND EmailNhan = ?";
    private static final String SQL_COUNT_UNREAD = "SELECT COUNT(*) FROM THONGBAO_NGUOINHAN WHERE EmailNhan = ? AND DaDoc = 0";
    private static final String SQL_MARK_ALL_READ = "UPDATE THONGBAO_NGUOINHAN SET DaDoc = 1, ReadAt = GETUTCDATE() WHERE EmailNhan = ? AND DaDoc = 0";
    private static final String SQL_ADMIN_LIST_ALL = 
        """
            SELECT TOP (?) 
                tb.IdThongBao,
                tb.TieuDe,
                tb.NoiDung,
                tb.CreatedBy,
                tb.CreatedAt,
                COUNT(r.EmailNhan) AS RecipientCount,
                SUM(CASE WHEN r.DaDoc = 1 THEN 1 ELSE 0 END) AS ReadCount
            FROM THONGBAO tb
            LEFT JOIN THONGBAO_NGUOINHAN r ON tb.IdThongBao = r.IdThongBao
            WHERE (? IS NULL OR tb.IdThongBao < ?)
            GROUP BY tb.IdThongBao, tb.TieuDe, tb.NoiDung, tb.CreatedBy, tb.CreatedAt
            ORDER BY tb.IdThongBao DESC
        """;
    private static final String SQL_GET_RECIPIENTS = 
        """
            SELECT EmailNhan, DaDoc, ReadAt
            FROM THONGBAO_NGUOINHAN
            WHERE IdThongBao = ?
            ORDER BY EmailNhan
        """;


    /**
     * @param TieuDe Tiêu đề thông báo
     * @param NoiDung Nội dung thông báo
     * @param recipients Danh sách email người nhận
     * @param CreatedBy Email người tạo thông báo
     * @return ID của thông báo vừa tạo
     * @throws ThongBaoException Nếu có lỗi trong quá trình tạo hoặc rollback
     */
    public int createAnnouncement(String TieuDe, String NoiDung, List<String> recipients, String CreatedBy) throws ThongBaoException {
        Connection conn = null;
        PreparedStatement psThongBao = null;
        PreparedStatement psNguoiNhan = null;

        try {
            conn = DBConnector.getConnection();

            // transaction
            conn.setAutoCommit(false);

            psThongBao = conn.prepareStatement(
                SQL_CREATE_ANNOUNCEMENT,
                PreparedStatement.RETURN_GENERATED_KEYS // return ID
            );

            psThongBao.setString(1, TieuDe);
            psThongBao.setString(2, NoiDung);
            psThongBao.setString(3, CreatedBy);

            int rowsAffected = psThongBao.executeUpdate();

            if(rowsAffected == 0) {
                throw new ThongBaoException("Không thể tạo thông báo");
            }

            int idThongBao = 0;
            ResultSet generatedKeys = psThongBao.getGeneratedKeys();
            if(generatedKeys.next()) {
                idThongBao = generatedKeys.getInt(1);
            } else {
                throw new ThongBaoException("Không lấy được ID thông báo");
            }

            psNguoiNhan = conn.prepareStatement(SQL_INSERT_RECIPIENTS);

            // bulk insert người nhận
            for (String email : recipients) {
                psNguoiNhan.setInt(1, idThongBao);
                psNguoiNhan.setString(2, email);
                psNguoiNhan.addBatch(); // thêm vào batch
            }

            psNguoiNhan.executeBatch();

            conn.commit();
            System.out.println("Thông báo ID " + idThongBao + " đã được tạo thành công");
    
            return idThongBao;
        } catch (Exception e) {
            if(conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Transaction rolled back");
                } catch (Exception rollbackEx) {
                    throw new ThongBaoException("Lỗi rollback: " + rollbackEx.getMessage());
                }
            }

            throw new ThongBaoException("Lỗi tạo thông báo " + e.getMessage());
        } finally {
            try {
                if (psNguoiNhan != null) psNguoiNhan.close();
                if (psThongBao != null) psThongBao.close();
                if(conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception closeEx) {
                System.err.println("Lỗi đóng connection: " + closeEx.getMessage());
            }
        }
    }

    /**
     * @param email Email của người nhận
     * @param unreadOnly true = chỉ lấy thông báo chưa đọc, false = lấy tất cả
     * @param lastIdCursor ID của thông báo cuối từ page trước (null nếu page đầu tiên)
     * @param pageSize Số lượng thông báo trên một trang
     * @return ThongBaoListResult chứa danh sách thông báo, hasMore, và nextCursor
     * @throws ThongBaoException Nếu có lỗi trong quá trình query
     */
    public ThongBaoListResult listByReceiver(String email, boolean unreadOnly, Integer lastIdCursor, int pageSize) throws ThongBaoException {

        List<ThongBaoNguoiNhan> items = new ArrayList<>();

        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_LIST_BY_RECEIVER);

            ps.setInt(1,  pageSize + 1);

            ps.setString(2, email);

            ps.setInt(3, unreadOnly ? 1 : 0);

            if(lastIdCursor == null) {
                ps.setNull(4, Types.INTEGER);
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(4, lastIdCursor);
                ps.setInt(5, lastIdCursor);
            }

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                ThongBaoNguoiNhan item = new ThongBaoNguoiNhan();
                item.setIdThongBao(rs.getInt("IdThongBao"));
                item.setTieuDe(rs.getString("TieuDe"));
                item.setNoiDung(rs.getString("NoiDung"));
                item.setCreatedBy(rs.getString("CreatedBy"));
                
                // Null check cho CreatedAt
                java.sql.Timestamp tsCreated = rs.getTimestamp("CreatedAt");
                item.setCreatedAt(tsCreated != null ? tsCreated.toLocalDateTime() : null);
                
                item.setEmail(rs.getString("EmailNhan"));
                item.setDaDoc(rs.getBoolean("DaDoc"));
                
                // Null check cho ReadAt
                java.sql.Timestamp tsRead = rs.getTimestamp("ReadAt");
                item.setReadAt(tsRead != null ? tsRead.toLocalDateTime() : null);

                items.add(item);
            }

            boolean hasMore = items.size() > pageSize;
            if(hasMore) {
                items.remove(items.size() - 1);
            }

            Integer nextCursor = null;
            if(!items.isEmpty() && hasMore) {
                nextCursor = items.get(items.size() - 1).getIdThongBao();
            }

            return new ThongBaoListResult(items, hasMore, nextCursor);
        } catch (Exception e) {
            throw new ThongBaoException("Lỗi lấy danh sách thông báo: " + e.getMessage(), e);
        }
    }

    /**
     * @param idThongBao ID của thông báo
     * @param email Email của người nhận
     * @return true nếu cập nhật thành công
     * @throws ThongBaoException Nếu không tìm thấy thông báo hoặc người dùng không có quyền
     */
    public boolean markRead(int idThongBao, String email) throws ThongBaoException {
        
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_MARK_READ);

            ps.setInt(1, idThongBao);
            ps.setString(2, email);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new ThongBaoException("Không tìm thấy thông báo hoặc bạn không có quyền");
            }

            System.out.println("Đã đánh dấu đọc thông báo ID " + idThongBao + " cho " + email);
            return true;
        } catch (Exception e) {
            throw new ThongBaoException("Lỗi đánh dấu đọc: " + e.getMessage(), e);
        }
    }

    /**
     * @param email Email của người dùng
     * @return Số lượng thông báo chưa đọc
     * @throws ThongBaoException Nếu có lỗi trong quá trình đếm
     */
    public int getUnreadCount(String email) throws ThongBaoException {
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_COUNT_UNREAD);
            ps.setString(1, email);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
            
        } catch (Exception e) {
            throw new ThongBaoException("Lỗi đếm thông báo: " + e.getMessage(), e);
        }
    }

    /**
     * @param email Email của người dùng
     * @return Số lượng thông báo đã được đánh dấu đọc
     * @throws ThongBaoException Nếu có lỗi trong quá trình cập nhật
     */
    public int markAllRead(String email) throws ThongBaoException {
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_MARK_ALL_READ);
            ps.setString(1, email);
            
            int rowsAffected = ps.executeUpdate();
            System.out.println("Đã đánh dấu đọc " + rowsAffected + " thông báo cho " + email);
            return rowsAffected;
            
        } catch (Exception e) {
            throw new ThongBaoException("Lỗi đánh dấu tất cả đọc: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lấy danh sách tất cả thông báo (cho admin) với thống kê số người nhận/đã đọc
     * @param lastIdCursor ID của thông báo cuối từ page trước (null = page đầu)
     * @param pageSize Số lượng thông báo trên 1 page
     * @return ThongBaoAdminListResult chứa danh sách thông báo với thống kê, hasMore, và nextCursor
     * @throws ThongBaoException Nếu có lỗi trong quá trình query
     */
    public ThongBaoAdminListResult listAllForAdmin(Integer lastIdCursor, int pageSize) throws ThongBaoException {
        List<ThongBaoAdmin> items = new ArrayList<>();
        
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_ADMIN_LIST_ALL);
            
            // Query pageSize + 1 để check hasMore
            ps.setInt(1, pageSize + 1);
            
            // Cursor pagination
            if (lastIdCursor == null) {
                ps.setNull(2, Types.INTEGER);
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(2, lastIdCursor);
                ps.setInt(3, lastIdCursor);
            }
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                ThongBaoAdmin dto = new ThongBaoAdmin();
                dto.setIdThongBao(rs.getInt("IdThongBao"));
                dto.setTieuDe(rs.getString("TieuDe"));
                dto.setNoiDung(rs.getString("NoiDung"));
                dto.setCreatedBy(rs.getString("CreatedBy"));
                dto.setCreatedAt(rs.getTimestamp("CreatedAt"));
                dto.setRecipientCount(rs.getInt("RecipientCount"));
                dto.setReadCount(rs.getInt("ReadCount"));
                // unreadCount tự động tính trong setter
                
                items.add(dto);
            }
            
            // Check hasMore và trim list
            boolean hasMore = items.size() > pageSize;
            if (hasMore) {
                items.remove(items.size() - 1);
            }
            
            // Get nextCursor
            Integer nextCursor = null;
            if (!items.isEmpty() && hasMore) {
                nextCursor = items.get(items.size() - 1).getIdThongBao();
            }
            
            System.out.println("Retrieved " + items.size() + " announcements for admin (cursor: " + lastIdCursor + ")");
            return new ThongBaoAdminListResult(items, hasMore, nextCursor);
            
        } catch (Exception e) {
            throw new ThongBaoException("Lỗi lấy danh sách thông báo admin: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lấy danh sách người nhận của 1 thông báo (cho admin xem chi tiết)
     * @param idThongBao ID của thông báo
     * @return List các ThongBaoNguoiNhan chỉ chứa thông tin người nhận
     */
    public List<ThongBaoNguoiNhan> getRecipientsByAnnouncementId(int idThongBao) throws ThongBaoException {
        List<ThongBaoNguoiNhan> recipients = new ArrayList<>();
        
        try (Connection conn = DBConnector.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(SQL_GET_RECIPIENTS);
            ps.setInt(1, idThongBao);
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                ThongBaoNguoiNhan recipient = new ThongBaoNguoiNhan();
                recipient.setIdThongBao(idThongBao);
                recipient.setEmail(rs.getString("EmailNhan"));
                recipient.setDaDoc(rs.getBoolean("DaDoc"));
                
                // Null check cho ReadAt
                java.sql.Timestamp tsRead = rs.getTimestamp("ReadAt");
                recipient.setReadAt(tsRead != null ? tsRead.toLocalDateTime() : null);
                
                recipients.add(recipient);
            }
            
            System.out.println("Retrieved " + recipients.size() + " recipients for announcement " + idThongBao);
            return recipients;
            
        } catch (Exception e) {
            throw new ThongBaoException("Lỗi lấy danh sách người nhận: " + e.getMessage(), e);
        }
    }
}
