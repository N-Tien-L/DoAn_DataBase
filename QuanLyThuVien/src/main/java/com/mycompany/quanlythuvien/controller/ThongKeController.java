package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.ThongKeDAO;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller cho thống kê chung
 *
 * @author Bố
 */
public class ThongKeController {

    private final ThongKeDAO thongKeDAO = new ThongKeDAO();

    // ============= THỐNG KÊ BẠN ĐỌC =============

    public int getTotalBanDoc() {
        try {
            return thongKeDAO.getTotalBanDoc();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getTotalBanDocCouMuon() {
        try {
            return thongKeDAO.getTotalBanDocCouMuon();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ============= THỐNG KÊ TÀI KHOẢN =============

    public int getTotalTaiKhoanAdmin() {
        try {
            return thongKeDAO.getTotalTaiKhoanAdmin();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getTotalTaiKhoanThuThu() {
        try {
            return thongKeDAO.getTotalTaiKhoanThuThu();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ============= THỐNG KÊ SÁCH =============

    public List<Object[]> getTop10SachPhoBien() {
        try {
            return thongKeDAO.getTop10SachPhoBien();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public int getTotalBanSao() {
        try {
            return thongKeDAO.getTotalBanSao();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getTotalBanSaoDangMuon() {
        try {
            return thongKeDAO.getTotalBanSaoDangMuon();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getTotalBanSaoCoSan() {
        try {
            return thongKeDAO.getTotalBanSaoCoSan();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ============= THỐNG KÊ SÁCH (DANH MỤC) =============

    public List<Object[]> getSoLuotMuonTheoTheLoai() {
        try {
            return thongKeDAO.getSoLuotMuonTheoTheLoai();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Object[]> getSoLuotMuonTheoTacGia() {
        try {
            return thongKeDAO.getSoLuotMuonTheoTacGia();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // ============= THỐNG KÊ MƯỢN - TRẢ (XU HƯỚNG) =============

    public List<Object[]> getSoLuotMuonTheoNgay() {
        try {
            return thongKeDAO.getSoLuotMuonTheoNgay();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Object[]> getSoLuotMuonTheoThang() {
        try {
            return thongKeDAO.getSoLuotMuonTheoThang();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Object[]> getSoLuotMuonTheoNam() {
        try {
            return thongKeDAO.getSoLuotMuonTheoNam();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // ============= THỐNG KÊ MƯỢN - TRẢ (VI PHẠM) =============

    public int getTotalPhieuTreHan() {
        try {
            return thongKeDAO.getTotalPhieuTreHan();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getTotalSachTreHan() {
        try {
            return thongKeDAO.getTotalSachTreHan();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ============= THỐNG KÊ TÀI CHÍNH =============

    public BigDecimal getTotalTienPhatDaDong() {
        try {
            return thongKeDAO.getTotalTienPhatDaDong();
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getTotalTienPhatChuaDong() {
        try {
            return thongKeDAO.getTotalTienPhatChuaDong();
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getTotalTienPhat() {
        try {
            return thongKeDAO.getTotalTienPhat();
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    // ============= THỐNG KÊ VI PHẠM =============

    public List<Object[]> getSoLuongViPhamTheoLoai() {
        try {
            return thongKeDAO.getSoLuongViPhamTheoLoai();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
