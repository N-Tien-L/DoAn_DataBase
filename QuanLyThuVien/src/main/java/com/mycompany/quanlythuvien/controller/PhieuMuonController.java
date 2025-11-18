package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.PhieuMuonDAO;
import com.mycompany.quanlythuvien.model.PhieuMuon;
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
