package com.mycompany.quanlythuvien.dao;

import com.mycompany.quanlythuvien.util.DBConnector;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * DAO cho thống kê chung
 */
public class ThongKeDAO {

    // ============= THỐNG KÊ BẠN ĐỌC =============

    /**
     * Lấy tổng số bạn đọc
     */
    public int getTotalBanDoc() throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM BANDOC";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    /**
     * Lấy số lượng bạn đọc có mượn sách
     */
    public int getTotalBanDocCouMuon() throws Exception {
        String sql = """
                SELECT COUNT(DISTINCT pm.IdBD) AS total
                FROM PHIEUMUON pm
                """;
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    // ============= THỐNG KÊ TÀI KHOẢN =============

    /**
     * Lấy số lượng tài khoản admin
     */
    public int getTotalTaiKhoanAdmin() throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM TAIKHOAN WHERE [Role] = 'Admin'";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    /**
     * Lấy số lượng tài khoản thủ thư
     */
    public int getTotalTaiKhoanThuThu() throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM TAIKHOAN WHERE [Role] = 'ThuThu'";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    // ============= THỐNG KÊ SÁCH =============

    /**
     * Lấy top 10 sách được mượn nhiều nhất (Sách phổ biến)
     */
    public java.util.List<Object[]> getTop10SachPhoBien() throws Exception {
        java.util.List<Object[]> result = new java.util.ArrayList<>();
        String sql = """
                SELECT TOP 10
                    s.TenSach,
                    COUNT(ctpm.IdPM) AS SoLanMuon
                FROM SACH s
                INNER JOIN BANSAO bs ON s.ISBN = bs.ISBN
                INNER JOIN CT_PM ctpm ON bs.MaBanSao = ctpm.MaBanSao
                GROUP BY s.ISBN, s.TenSach
                ORDER BY SoLanMuon DESC
                """;
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Object[] { rs.getString("TenSach"), rs.getInt("SoLanMuon") });
            }
        }
        return result;
    }

    /**
     * Lấy tổng số bản sao
     */
    public int getTotalBanSao() throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM BANSAO";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    /**
     * Lấy số bản sao đang được mượn
     */
    public int getTotalBanSaoDangMuon() throws Exception {
        String sql = """
                SELECT COUNT(DISTINCT ctpm.MaBanSao) AS total
                FROM CT_PM ctpm
                WHERE ctpm.NgayTraThucTe IS NULL
                """;
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    /**
     * Lấy số bản sao có sẵn (chưa được mượn hoặc đã trả)
     */
    public int getTotalBanSaoCoSan() throws Exception {
        int total = getTotalBanSao();
        int dangMuon = getTotalBanSaoDangMuon();
        return total - dangMuon;
    }

    // ============= THỐNG KÊ SÁCH (DANH MỤC) =============

    /**
     * Lấy số lượt mượn theo Thể loại
     */
    public List<Object[]> getSoLuotMuonTheoTheLoai() throws Exception {
        List<Object[]> result = new ArrayList<>();
        String sql = """
                SELECT
                    tl.TenTheLoai,
                    COUNT(ctpm.IdPM) AS SoLuotMuon
                FROM THELOAI tl
                LEFT JOIN SACH s ON tl.MaTheLoai = s.MaTheLoai
                LEFT JOIN BANSAO bs ON s.ISBN = bs.ISBN
                LEFT JOIN CT_PM ctpm ON bs.MaBanSao = ctpm.MaBanSao
                GROUP BY tl.MaTheLoai, tl.TenTheLoai
                ORDER BY SoLuotMuon DESC
                """;
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Object[] { rs.getString("TenTheLoai"), rs.getInt("SoLuotMuon") });
            }
        }
        return result;
    }

    /**
     * Lấy số lượt mượn theo Tác giả
     */
    public List<Object[]> getSoLuotMuonTheoTacGia() throws Exception {
        List<Object[]> result = new ArrayList<>();
        String sql = """
                SELECT
                    tg.TenTacGia,
                    COUNT(ctpm.IdPM) AS SoLuotMuon
                FROM TACGIA tg
                LEFT JOIN SACH s ON tg.MaTacGia = s.MaTacGia
                LEFT JOIN BANSAO bs ON s.ISBN = bs.ISBN
                LEFT JOIN CT_PM ctpm ON bs.MaBanSao = ctpm.MaBanSao
                GROUP BY tg.MaTacGia, tg.TenTacGia
                ORDER BY SoLuotMuon DESC
                """;
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Object[] { rs.getString("TenTacGia"), rs.getInt("SoLuotMuon") });
            }
        }
        return result;
    }

    // ============= THỐNG KÊ MƯỢN - TRẢ (XU HƯỚNG) =============

    /**
     * Lấy số lượt mượn theo Ngày (30 ngày gần đây)
     */
    public List<Object[]> getSoLuotMuonTheoNgay() throws Exception {
        List<Object[]> result = new ArrayList<>();
        String sql = """
                SELECT
                    CAST(pm.NgayMuon AS DATE) AS Ngay,
                    COUNT(pm.IdPM) AS SoLuotMuon
                FROM PHIEUMUON pm
                WHERE pm.NgayMuon >= DATEADD(DAY, -30, CAST(GETDATE() AS DATE))
                GROUP BY CAST(pm.NgayMuon AS DATE)
                ORDER BY Ngay ASC
                """;
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Object[] { rs.getDate("Ngay"), rs.getInt("SoLuotMuon") });
            }
        }
        return result;
    }

    /**
     * Lấy số lượt mượn theo Tháng (12 tháng gần đây)
     */
    public List<Object[]> getSoLuotMuonTheoThang() throws Exception {
        List<Object[]> result = new ArrayList<>();
        String sql = """
                SELECT
                    YEAR(pm.NgayMuon) AS Nam,
                    MONTH(pm.NgayMuon) AS Thang,
                    COUNT(pm.IdPM) AS SoLuotMuon
                FROM PHIEUMUON pm
                WHERE pm.NgayMuon >= DATEADD(MONTH, -12, CAST(GETDATE() AS DATE))
                GROUP BY YEAR(pm.NgayMuon), MONTH(pm.NgayMuon)
                ORDER BY Nam ASC, Thang ASC
                """;
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Object[] { rs.getInt("Nam"), rs.getInt("Thang"), rs.getInt("SoLuotMuon") });
            }
        }
        return result;
    }

    /**
     * Lấy số lượt mượn theo Năm (tất cả)
     */
    public List<Object[]> getSoLuotMuonTheoNam() throws Exception {
        List<Object[]> result = new ArrayList<>();
        String sql = """
                SELECT
                    YEAR(pm.NgayMuon) AS Nam,
                    COUNT(pm.IdPM) AS SoLuotMuon
                FROM PHIEUMUON pm
                GROUP BY YEAR(pm.NgayMuon)
                ORDER BY Nam ASC
                """;
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Object[] { rs.getInt("Nam"), rs.getInt("SoLuotMuon") });
            }
        }
        return result;
    }

    // ============= THỐNG KÊ MƯỢN - TRẢ (VI PHẠM) =============

    /**
     * Lấy tổng số phiếu đang trễ hẹn
     */
    public int getTotalPhieuTreHan() throws Exception {
        String sql = """
                SELECT COUNT(DISTINCT pm.IdPM) AS total
                FROM PHIEUMUON pm
                JOIN CT_PM ctpm ON pm.IdPM = ctpm.IdPM
                WHERE pm.HanTra < CAST(GETDATE() AS DATE)
                  AND ctpm.NgayTraThucTe IS NULL
                """;
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    /**
     * Lấy tổng số sách (bản sao) đang trễ hẹn
     */
    public int getTotalSachTreHan() throws Exception {
        String sql = """
                SELECT COUNT(DISTINCT ctpm.MaBanSao) AS total
                FROM CT_PM ctpm
                JOIN PHIEUMUON pm ON ctpm.IdPM = pm.IdPM
                WHERE pm.HanTra < CAST(GETDATE() AS DATE)
                  AND ctpm.NgayTraThucTe IS NULL
                """;
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    // ============= THỐNG KÊ TÀI CHÍNH =============

    /**
     * Lấy tổng tiền phạt đã đóng
     */
    public BigDecimal getTotalTienPhatDaDong() throws Exception {
        String sql = "SELECT SUM(SoTien) AS total FROM PHAT WHERE TrangThai = 'Da dong'";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total");
                return total != null ? total : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Lấy tổng tiền phạt chưa đóng
     */
    public BigDecimal getTotalTienPhatChuaDong() throws Exception {
        String sql = "SELECT SUM(SoTien) AS total FROM PHAT WHERE TrangThai = 'Chua dong'";
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total");
                return total != null ? total : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Lấy tổng tiền phạt (cả đã đóng và chưa đóng)
     */
    public BigDecimal getTotalTienPhat() throws Exception {
        BigDecimal daDong = getTotalTienPhatDaDong();
        BigDecimal chuaDong = getTotalTienPhatChuaDong();
        return daDong.add(chuaDong);
    }

    // ============= THỐNG KÊ VI PHẠM =============

    /**
     * Lấy số lượng vi phạm theo loại (Trễ hạn, Hỏng, Mất)
     */
    public List<Object[]> getSoLuongViPhamTheoLoai() throws Exception {
        List<Object[]> result = new ArrayList<>();
        String sql = """
                SELECT
                    LoaiPhat,
                    COUNT(*) AS SoLuong
                FROM PHAT
                GROUP BY LoaiPhat
                ORDER BY SoLuong DESC
                """;
        try (Connection con = DBConnector.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Object[] { rs.getString("LoaiPhat"), rs.getInt("SoLuong") });
            }
        }
        return result;
    }
}