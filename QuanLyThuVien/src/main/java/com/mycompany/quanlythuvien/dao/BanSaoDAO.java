package com.mycompany.quanlythuvien.dao;

import com.mycompany.quanlythuvien.model.BanSao;
import com.mycompany.quanlythuvien.util.DBConnector;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Thanh
 */
public class BanSaoDAO {    
    public List<BanSao> getPage(String isbn, int pageSize, Integer lastMaBanSao) {
        List<BanSao> list = new ArrayList<>();
        if (isbn == null || isbn.isBlank()) return list;
        if (pageSize < 1 || pageSize > 100) pageSize = 10;
        
        boolean isFirstPage = (lastMaBanSao == null);
        String sql = isFirstPage
                ? "SELECT TOP (?) * FROM BANSAO WHERE ISBN = ? ORDER BY MaBanSao ASC"
                : "SELECT TOP (?) * FROM BANSAO WHERE ISBN = ? AND MaBanSao > ? ORDER BY MaBanSao ASC";    
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, pageSize);
            ps.setString(2, isbn);
            if (lastMaBanSao != null) ps.setInt(3, lastMaBanSao);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BanSao b = new BanSao(
                            rs.getInt("MaBanSao"),
                            rs.getString("ISBN"),
                            rs.getInt("SoThuTuTrongKho"),
                            rs.getString("TinhTrang"),
                            rs.getBoolean("Lendable"),
                            rs.getDate("NgayNhapKho") != null ? rs.getDate("NgayNhapKho").toLocalDate() : null,
                            rs.getString("ViTriLuuTru"),
                            rs.getTimestamp("CreatedAt"),
                            rs.getString("CreatedBy")
                    );
                    list.add(b);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        
        return list;
    }

    public List<BanSao> getAllByISBN(String isbn) {
        List<BanSao> list = new ArrayList<>();
        if (isbn == null || isbn.isBlank()) return list;

        String sql = "SELECT * FROM BANSAO WHERE ISBN = ? ORDER BY MaBanSao ASC";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, isbn);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BanSao b = new BanSao(
                            rs.getInt("MaBanSao"),
                            rs.getString("ISBN"),
                            rs.getInt("SoThuTuTrongKho"),
                            rs.getString("TinhTrang"),
                            rs.getDate("NgayNhapKho") != null ? rs.getDate("NgayNhapKho").toLocalDate() : null,
                            rs.getString("ViTriLuuTru"),
                            rs.getTimestamp("CreatedAt"),
                            rs.getString("CreatedBy")
                    );
                    list.add(b);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    public boolean insert(BanSao b, String createdBy) throws Exception {
        String sql = """
            INSERT INTO BANSAO (ISBN, SoThuTuTrongKho, TinhTrang, ViTriLuuTru, CreatedBy)
            VALUES (?,?,?,?,?)
                     """;
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) 
        {
            ps.setString(1, b.getISBN());
            ps.setInt(2, b.getSoThuTuTrongKho());
            ps.setString(3, b.getTinhTrang());
            ps.setString(4, b.getViTriLuuTru());
            ps.setString(5, createdBy);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) return false;
            
            // Lấy MaBanSao vừa sinh
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    b.setMaBanSao(rs.getInt(1));
                }
            }

            b.setCreatedBy(createdBy);
            return true;
        }
    }
    
    //cap nhat BANSAO
    public boolean update (BanSao b) throws Exception {
        String sql = """
            UPDATE BANSAO
            SET ISBN = ?, SoThuTuTrongKho = ?, TinhTrang = ?, ViTriLuuTru = ?
            WHERE MaBanSao = ?
                     """;
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setString(1, b.getISBN());
            ps.setInt(2, b.getSoThuTuTrongKho());
            ps.setString(3, b.getTinhTrang());            
            ps.setString(4, b.getViTriLuuTru());
            ps.setInt(5, b.getMaBanSao());
            
            return ps.executeUpdate() > 0;
        } 
    }
    
    //xoa ban sao theo ID
    public boolean delete(int maBanSao) throws Exception {
        String sql = "DELETE FROM BANSAO WHERE MaBanSao = ?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            ps.setInt(1, maBanSao);
            return ps.executeUpdate() > 0;
        }
    }
    
    //Tim 1 ban sao theo ID
    public BanSao findById(int maBanSao) throws Exception {
        String sql = "SELECT * FROM BANSAO Where MaBanSao = ?";
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql))
        {
            ps.setInt(1, maBanSao);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new BanSao(
                            rs.getInt("MaBanSao"),
                            rs.getString("ISBN"),
                            rs.getInt("SoThuTuTrongKho"),
                            rs.getString("TinhTrang"),
                            rs.getBoolean("Lendable"),
                            (rs.getDate("NgayNhapKho") != null) ? rs.getDate("NgayNhapKho").toLocalDate() : null,
                            rs.getString("ViTriLuuTru"),
                            rs.getTimestamp("CreatedAt"),
                            rs.getString("CreatedBy")
                    );
                }
            }
        }
        return null;
    }
    

    public List<BanSao> search(String isbn, String keyword, String tieuChi, Integer lastMaBanSao, int pageSize) {
        List<BanSao> list = new ArrayList<>();

        if (isbn == null || isbn.isBlank()) return list; 
        String kw = keyword == null ? "" : keyword.trim();

        String sql = null;
        int keywordInt = -1; // Chỉ dùng cho Mã bản sao/Số thứ tự

        switch (tieuChi) {
            case "Mã bản sao":
                keywordInt = Integer.parseInt(kw); 
                sql = "SELECT TOP (?) * FROM BANSAO WHERE ISBN = ? AND MaBanSao = ?"
                    + " AND (? IS NULL OR MaBanSao > ?)"
                    + " ORDER BY MaBanSao ASC";
                break;
            case "Số thứ tự":
                keywordInt = Integer.parseInt(kw); 
                sql = "SELECT TOP (?) * FROM BANSAO WHERE ISBN = ? AND SoThuTuTrongKho = ?"
                    + " AND (? IS NULL OR MaBanSao > ?)"
                    + " ORDER BY MaBanSao ASC";
                break;
            case "Tình trạng":
                sql = "SELECT TOP (?) * FROM BANSAO WHERE ISBN = ? AND TinhTrang LIKE ?"
                    + " AND (? IS NULL OR MaBanSao > ?)"
                    + " ORDER BY MaBanSao ASC";
                break;
            case "Vị trí lưu trữ":
                sql = "SELECT TOP (?) * FROM BANSAO WHERE ISBN = ? AND ViTriLuuTru LIKE ?"
                    + " AND (? IS NULL OR MaBanSao > ?)"
                    + " ORDER BY MaBanSao ASC";
                break;
            default:
                return list;
        }

        try (Connection con = DBConnector.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int idx = 1;

            ps.setInt(idx++, pageSize);

            ps.setString(idx++, isbn);

            switch (tieuChi) {
                case "Mã bản sao":
                case "Số thứ tự":
                    ps.setInt(idx++, keywordInt); 
                    break;
                case "Tình trạng":
                case "Vị trí lưu trữ":
                    ps.setString(idx++, "%" + kw + "%");
                    break;
            }

            if (lastMaBanSao == null) {
                ps.setNull(idx++, Types.INTEGER); // Gán cho '?' IS NULL
                ps.setNull(idx++, Types.INTEGER); // Gán cho MaBanSao > '?'
            } else {
                ps.setInt(idx++, lastMaBanSao);
                ps.setInt(idx++, lastMaBanSao);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BanSao b = new BanSao(
                                rs.getInt("MaBanSao"),
                                rs.getString("ISBN"),
                                rs.getInt("SoThuTuTrongKho"),
                                rs.getString("TinhTrang"),
                                rs.getBoolean("Lendable"),
                                rs.getDate("NgayNhapKho") != null ? rs.getDate("NgayNhapKho").toLocalDate() : null,
                                rs.getString("ViTriLuuTru"),
                                rs.getTimestamp("CreatedAt"),
                                rs.getString("CreatedBy")
                            );
                    list.add(b);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    
    public List<BanSao> searchByDateRange(String isbn, LocalDate fromDate, LocalDate toDate, Integer lastMaBanSao, int pageSize) {
        List<BanSao> list = new ArrayList<>();
        
        String sql = "SELECT TOP (?) * FROM BANSAO WHERE ISBN = ? "
            + "AND NgayNhapKho BETWEEN ? AND ? " 
            + "AND (? IS NULL OR MaBanSao > ?) "
            + "ORDER BY MaBanSao ASC";
        
        try (Connection con = DBConnector.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) 
        {
            int idx = 1;
            ps.setInt(idx++, pageSize + 1);
            
            ps.setString(idx++, isbn);
            
            ps.setDate(idx++, Date.valueOf(fromDate));
            ps.setDate(idx++, Date.valueOf(toDate));
            
            if (lastMaBanSao == null) {
                ps.setNull(idx++, Types.INTEGER);
                ps.setNull(idx++, Types.INTEGER);
            } else {
                ps.setInt(idx++, lastMaBanSao);
                ps.setInt(idx++, lastMaBanSao);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BanSao bs = mapResultSetToBanSao(rs);
                    list.add(bs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    private BanSao mapResultSetToBanSao(ResultSet rs) throws SQLException {
        BanSao b = new BanSao();
        b.setMaBanSao(rs.getInt("MaBanSao"));
        b.setISBN(rs.getString("ISBN"));
        b.setSoThuTuTrongKho(rs.getInt("SoThuTuTrongKho"));
        b.setTinhTrang(rs.getString("TinhTrang"));
        b.setLendable(rs.getBoolean("Lendable"));
        
        if (rs.getDate("NgayNhapKho") != null) { 
            b.setNgayNhapKho(rs.getDate("NgayNhapKho").toLocalDate());
        }
        b.setViTriLuuTru(rs.getString("ViTriLuuTru"));
        return b;      
    }
    
    public int insertBatch(String isbn, int soLuong, int soThuTuBatDau, String tinhTrang,
                        String viTriLuuTru, String createdBy) throws Exception {
        String sql = "INSERT INTO BANSAO (ISBN, SoThuTuTrongKho, TinhTrang, ViTriLuuTru, CreatedBy) VALUES (?, ?, ?, ?, ?)";
        
        int totalInserted = 0;
        Connection con = null;
        try {
            con = DBConnector.getConnection();
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (int i = 0; i < soLuong; i++) {
                    int currentSoThuTu = soThuTuBatDau + i;

                    int idx = 1;
                    ps.setString(idx++, isbn);
                    ps.setInt(idx++, currentSoThuTu);
                    ps.setString(idx++, tinhTrang);
                    ps.setString(idx++, viTriLuuTru);
                    ps.setString(idx++, createdBy);

                    ps.addBatch();
                }

                int[] result = ps.executeBatch();
                for (int count : result) {
                    if (count > 0) {
                        totalInserted += count;
                    }
                }
                con.commit();
                return totalInserted;
            } catch (SQLIntegrityConstraintViolationException e) {
                if (con != null) {
                    con.rollback();
                }
                throw new Exception("Lỗi: Số thứ tự trong kho bị trùng lặp. Vui lòng kiểm tra lại số thứ tự bắt đầu.", e);
            }   
        } catch (Exception e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Lỗi rollback: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (Exception closeEx) {
                    System.err.println("Lỗi đóng connection: " + closeEx.getMessage());
                }
            }
        }
    }
}
