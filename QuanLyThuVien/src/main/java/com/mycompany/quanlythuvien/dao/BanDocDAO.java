package com.mycompany.quanlythuvien.dao;
import com.mycompany.quanlythuvien.model.BanDoc;
import com.mycompany.quanlythuvien.util.DBConnector;
import java.sql.*;
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
"WHERE HoTen = ? AND Email = ? AND DiaChi = ? AND SDT = ?;";
    public Boolean deleteDAO(BanDoc cur) throws Exception {
        if (cur == null) return false;

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setString(1, cur.getHoTen());
            ps.setString(2, cur.getEmail());
            ps.setString(3, cur.getDiaChi());
            ps.setString(4, cur.getSdt());

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
}
