package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.PhieuMuonDAO;
import com.mycompany.quanlythuvien.model.PhieuMuon;
import com.mycompany.quanlythuvien.model.ChiTietPhieuMuon;
import java.util.Objects;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tien
 */
public class PhieuMuonController {
    private final PhieuMuonDAO phieuMuonDAO = new PhieuMuonDAO();

    // Lấy tất cả phiếu mượn (cho bảng)
    public List<PhieuMuon> getAll() {
        try {
            return phieuMuonDAO.getAll();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Thêm mới phiếu mượn (ném Exception nếu dữ liệu không hợp lệ)
    public void insert(PhieuMuon pm) throws Exception {
        validate(pm, true);
        phieuMuonDAO.createNew(pm);
    }

    // Cập nhật phiếu mượn
    public void update(PhieuMuon pm) throws Exception {
        validate(pm, false);
        phieuMuonDAO.update(pm);
    }

    // Xóa phiếu mượn
    public boolean delete(int idPM) {
        try {
            if (idPM <= 0) return false;
            return phieuMuonDAO.delete(idPM);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Tìm theo IdBD
    public List<PhieuMuon> findByIdBD(int idBD) {
        try {
            return phieuMuonDAO.findByIdBD(idBD);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Tìm theo khoảng ngày mượn
    public List<PhieuMuon> findByNgayMuon(LocalDate from, LocalDate to) {
        try {
            return phieuMuonDAO.findByNgayMuon(from, to);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Tìm các phiếu mượn hiện đang được mượn (chưa trả)
    public List<PhieuMuon> findCurrentBorrowed() {
        try {
            return phieuMuonDAO.findCurrentBorrowed();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Tìm phiếu mượn theo Id
    public PhieuMuon findById(int idPM) {
        try {
            if (idPM <= 0) return null;
            return phieuMuonDAO.findById(idPM);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Tìm theo EmailNgườiLập
    public List<PhieuMuon> findByEmailNguoiLap(String email) {
        try {
            if (email == null || email.isBlank()) return new ArrayList<>();
            return phieuMuonDAO.findByEmailNguoiLap(email.trim());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Tìm theo khoảng ngày và trạng thái trả
    public List<PhieuMuon> searchByDateAndStatus(LocalDate from, LocalDate to, String status) {
        try {
            return phieuMuonDAO.searchByDateAndStatus(from, to, status);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Page-indexed search (pageIndex = 1..N, pageSize fixed)
    public com.mycompany.quanlythuvien.model.PageResult<PhieuMuon> searchWithPagination(String emailBanDoc, String emailNguoiLap, LocalDate from, LocalDate to, String status, int pageIndex, int pageSize) {
        try {
            int total = phieuMuonDAO.countSearch(emailBanDoc, emailNguoiLap, from, to, status);
            List<PhieuMuon> data = phieuMuonDAO.searchPaginated(emailBanDoc, emailNguoiLap, from, to, status, pageIndex, pageSize);
            return new com.mycompany.quanlythuvien.model.PageResult<>(data, pageIndex, pageSize, total);
        } catch (Exception e) {
            e.printStackTrace();
            return new com.mycompany.quanlythuvien.model.PageResult<>(new ArrayList<>(), pageIndex, pageSize, 0);
        }
    }

    // Kiểm tra bạn đọc có khoản vay đang mở
    public boolean hasOpenLoans(int idBD) {
        try {
            if (idBD <= 0) return false;
            return phieuMuonDAO.hasOpenLoans(idBD);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Gia hạn hạn trả
    public boolean extendDueDate(int idPM, LocalDate newDue) {
        try {
            if (idPM <= 0 || Objects.isNull(newDue)) return false;
            return phieuMuonDAO.extendDueDate(idPM, newDue);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Tạo phiếu mượn cùng chi tiết trong 1 transaction
    public boolean createWithDetails(PhieuMuon pm, List<ChiTietPhieuMuon> details) throws Exception {
        validate(pm, true);
        if (details == null || details.isEmpty()) throw new Exception("Chi tiết phiếu mượn không hợp lệ");
        return phieuMuonDAO.createWithDetails(pm, details);
    }

    // Validate common rules. If isNew==true, idPM is not required.
    private void validate(PhieuMuon pm, boolean isNew) throws Exception {
        if (pm == null) throw new Exception("Dữ liệu phiếu mượn không hợp lệ!");

        if (!isNew) {
            if (pm.getIdPM() <= 0)
                throw new Exception("Id phiếu mượn không hợp lệ!");
        }

        if (pm.getIdBD() <= 0)
            throw new Exception("Vui lòng chọn bạn đọc!");

        if (pm.getEmailNguoiLap() == null || pm.getEmailNguoiLap().isBlank())
            throw new Exception("Email người lập không được để trống!");

        if (pm.getNgayMuon() == null)
            throw new Exception("Ngày mượn không được để trống!");

        if (pm.getHanTra() == null)
            throw new Exception("Hạn trả không được để trống!");

        if (pm.getHanTra().isBefore(pm.getNgayMuon()))
            throw new Exception("Hạn trả phải lớn hơn hoặc bằng ngày mượn!");
    }
}