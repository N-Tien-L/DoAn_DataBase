package com.mycompany.quanlythuvien.dao;
import com.mycompany.quanlythuvien.model.BanDoc;
import com.mycompany.quanlythuvien.util.DBConnector;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;



/**
 *
 * @author Tien
 */
public class BanDocDAO {
    private static final String SQL_ADD =
        "INSERT INTO BANDOC (HoTen, Email, DiaChi, SDT) VALUES (?, ?, ?, ?)";

    public Boolean addDAO(BanDoc cur) throws Exception {
        if (cur == null) return false;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_ADD, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cur.getHoTen());
            ps.setString(2, cur.getEmail());
            ps.setString(3, cur.getDiaChi());
            ps.setString(4, cur.getSdt());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                return false;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    cur.setIdBD(generatedId); 
                }
            }


        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    

    private static final String SQL_READ =
        "SELECT * FROM BANDOC";

    public Boolean readDAO(ArrayList<BanDoc> dsBanDoc) throws Exception {
        if (dsBanDoc == null) return false;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_READ);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                BanDoc bd = new BanDoc();
                bd.setIdBD(rs.getInt("idbd"));
                bd.setHoTen(rs.getString("hoten"));
                bd.setEmail(rs.getString("email"));
                bd.setDiaChi(rs.getString("diachi"));
                bd.setSdt(rs.getString("sdt"));

                dsBanDoc.add(bd);
            }

            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    private static final String SQL_DELETE =
        "DELETE FROM BANDOC \n" +
"WHERE IdBD = ?";
    public Boolean deleteDAO(BanDoc cur) throws Exception {
        if (cur == null) return false;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setString(1, Integer.toString(cur.getIdBD()));


            int affected = ps.executeUpdate();
            if (affected == 0) {
                return false;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    private static final String SQL_UPDATE =
        "UPDATE BANDOC SET HoTen = ?, Email = ?, DiaChi = ?, SDT = ? WHERE IdBD = ?";

    public Boolean updateDAO(BanDoc cur) throws Exception {
        if (cur == null) return false;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, cur.getHoTen());
            ps.setString(2, cur.getEmail());
            ps.setString(3, cur.getDiaChi());
            ps.setString(4, cur.getSdt());
            ps.setInt(5, cur.getIdBD());  
            System.out.println(cur.getHoTen());

            int affected = ps.executeUpdate();

            return affected > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    
    private static final String SQL_GET_BY_ID =
        "SELECT * FROM BANDOC WHERE IdBD = ?";
    public BanDoc getBanDocById(int id) throws Exception {
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_GET_BY_ID)) {

            ps.setString(1, Integer.toString(id)); 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BanDoc bd = new BanDoc();
                    bd.setIdBD(rs.getInt("idbd"));
                    bd.setHoTen(rs.getString("hoten"));
                    bd.setEmail(rs.getString("email"));
                    bd.setDiaChi(rs.getString("diachi"));
                    bd.setSdt(rs.getString("sdt"));
                    return bd;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public int getSoLanMuonCuaBanDoc(int IdBD) throws Exception {
        String sql = "SELECT count(*) FROM PHIEUMUON WHERE IdBD = ?";
        int soPhieu = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, IdBD); 
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    soPhieu = rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return soPhieu;
    }
    public int getSoSachDangMuonCuaBanDoc(int IdBD) throws Exception {
        String sql = "SELECT count(*) FROM PHIEUMUON pm, CT_PM ctpm WHERE pm.IdBD = ? AND pm.IdPM = ctpm.IdPM AND ctpm.NgayTraThucTe IS NULL";
        int ans = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, IdBD); 
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ans = rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ans;
    }
    public int getSoSachDaMuonCuaBanDoc(int IdBD) throws Exception {
        String sql = "SELECT count(*) FROM PHIEUMUON pm, CT_PM ctpm WHERE pm.IdBD = ? AND pm.IdPM = ctpm.IdPM";
        int ans = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, IdBD); 
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ans = rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ans;
    }
    public int getSoPhieuPhatBanDoc(int IdBD) throws Exception {
        String sql = "SELECT count(*) FROM PHIEUMUON pm, PHAT p WHERE pm.IdBD = ? AND pm.IdPM = p.IdPM";
        int ans = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, IdBD); 
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ans = rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ans;
    }
    public int getSoTienPhatChuaDongBanDoc(int IdBD) throws Exception {
        String sql = "SELECT COALESCE(SUM(p.SoTien), 0)\n" 
                + "FROM PHIEUMUON pm\n" 
                + "JOIN PHAT p ON pm.IdPM = p.IdPM\n" 
                + "WHERE pm.IdBD = ?\n" 
                + "  AND p.TrangThai = 'Chua dong';";
        int ans = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, IdBD); 
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ans = rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ans;
    }
    public int getSoTienPhatDaDongBanDoc(int IdBD) throws Exception {
        String sql = "SELECT COALESCE(SUM(p.SoTien), 0)\n" 
                + "FROM PHIEUMUON pm\n" 
                + "JOIN PHAT p ON pm.IdPM = p.IdPM\n" 
                + "WHERE pm.IdBD = ?\n" 
                + "  AND p.TrangThai = 'Da dong';";
        int ans = 0;
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, IdBD); 
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ans = rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return ans;
    }

}
