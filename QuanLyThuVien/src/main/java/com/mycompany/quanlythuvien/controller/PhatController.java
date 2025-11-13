package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.PhatDAO;
import com.mycompany.quanlythuvien.model.Phat;
import com.mycompany.quanlythuvien.model.BanDocPhat;
import com.mycompany.quanlythuvien.model.ChiTietPhieuMuonInfo;
import com.mycompany.quanlythuvien.model.PaginationResult;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author Tien
 */
public class PhatController {
    private final PhatDAO phatDAO = new PhatDAO();

    // Lấy tất cả phạt
    public List<Phat> getAllPhat() {
        try {
            return phatDAO.getAllPhat();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // danh sách rỗng
        }
    }

    // Lấy danh sách phạt của một phiếu mượn
    public List<Phat> getPhatByIdPM(int idPM) {
        try {
            return phatDAO.getPhatByIdPM(idPM);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // Tạo phiếu phạt mới
    public boolean createPhat(Phat p) {
        try {
            return phatDAO.createPhat(p);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật phạt
    public boolean updatePhat(Phat p) {
        try {
            return phatDAO.update(p);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Xóa phạt
    public boolean deletePhat(int idPhat) {
        try {
            return phatDAO.delete(idPhat);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Lấy tổng tiền phạt chưa đóng của phiếu mượn
    public BigDecimal getTotalTienChuaDong(int idPM) {
        try {
            return phatDAO.getTotalTienChuaDong(idPM);
        } catch (Exception e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
    }

    // Lấy danh sách tất cả phạt chưa đóng
    public List<Phat> getPhatChuaDong() {
        try {
            return phatDAO.getPhatChuaDong();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // Tìm kiếm phiếu phạt thông qua thông tin bạn đọc (email, số điện thoại, họ
    // tên)
    public List<Phat> searchPhatByText(String text) {
        try {
            return phatDAO.searchPhatByText(text);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // Lấy tất cả phạt với phân trang (cursor-based)
    public PaginationResult<Phat> getAllPhatPaginated(int cursor, int pageSize) {
        try {
            return phatDAO.getAllPhatPaginated(cursor, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return new PaginationResult<>(List.of(), -1, -1, 0, pageSize, cursor);
        }
    }

    // Tìm kiếm phiếu phạt với phân trang (cursor-based)
    public PaginationResult<Phat> searchPhatByTextPaginated(String text, int cursor, int pageSize) {
        try {
            return phatDAO.searchPhatByTextPaginated(text, cursor, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return new PaginationResult<>(List.of(), -1, -1, 0, pageSize, cursor);
        }
    }

    // Tìm kiếm chi tiết phiếu mượn theo idPM, MaBanSao, tên bạn đọc, số điện thoại
    // Dùng cho gợi ý khi typing trong form tạo vé phạt
    public List<ChiTietPhieuMuonInfo> searchChiTietPhieuMuon(String text) {
        try {
            return phatDAO.searchChiTietPhieuMuon(text);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // Lấy thông tin phạt của 1 bạn đọc theo IdBD
    public BanDocPhat getBanDocPhatByIdBD(int idBD) {
        try {
            return phatDAO.getBanDocPhatByIdBD(idBD);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Cập nhật trạng thái "Da dong" cho tất cả vé phạt của 1 bạn đọc
    public boolean updateAllPhatToDaDongByIdBD(int idBD) {
        try {
            return phatDAO.updateAllPhatToDaDongByIdBD(idBD);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    } // Lấy chi tiết phiếu mượn từ IdPM và MaBanSao

    public ChiTietPhieuMuonInfo getChiTietPhieuMuonByIdPMAndMaBanSao(int idPM, int maBanSao) {
        try {
            return phatDAO.getChiTietPhieuMuonByIdPMAndMaBanSao(idPM, maBanSao);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
