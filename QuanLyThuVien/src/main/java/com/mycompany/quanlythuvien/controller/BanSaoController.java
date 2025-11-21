package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.BanSaoDAO;
import com.mycompany.quanlythuvien.model.BanSao;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tien
 */
public class BanSaoController {
    private BanSaoDAO banSaoDAO = new BanSaoDAO();
    
    // Lấy bản sao theo ISBN với phân trang
    public List<BanSao> getPage(String isbn, int pageSize, Integer lastMaBanSao) {
        if (isbn == null || isbn.isBlank()) return new ArrayList<>();
        if (pageSize < 1 || pageSize > 100) pageSize = 10;
        return banSaoDAO.getPage(isbn, pageSize, lastMaBanSao);
    }
    
    // Lấy tổng số trang theo ISBN
    public int getTotalPages(String isbn, int pageSize) {
        if (isbn == null || isbn.isBlank()) return 0;
        if (pageSize < 1 || pageSize > 100) pageSize = 10;
        return banSaoDAO.getTotalPages(isbn, pageSize);
    }
    
    // Lấy tất cả bản sao theo ISBN
    public List<BanSao> getAllByISBN(String isbn) {
        if (isbn == null || isbn.isBlank()) return new ArrayList<>();
        return banSaoDAO.getAllByISBN(isbn);
    }

    // Tìm bản sao theo ID
    public BanSao findById(int id) {
        if (id <= 0) return null;
        try {
            return banSaoDAO.findById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Xóa bản sao
    public void delete(int id) throws Exception {
        if (id <= 0) throw new Exception("ID bản sao không hợp lệ!");
        banSaoDAO.delete(id);
    }
    
    // Thêm bản sao mới
    public void insert(BanSao bansao, String createdBy) throws Exception {
        validateAndSetDefault(bansao);
        bansao.setNgayNhapKho(null);
        bansao.setCreatedAt(null);
        banSaoDAO.insert(bansao, createdBy);
    }
    
    // Cập nhật bản sao
    public void update(BanSao bansao) throws Exception {
        validateAndSetDefault(bansao);
        if (bansao.getMaBanSao() <= 0) {
            throw new Exception("Bản sao chưa tồn tại, không thể cập nhật!");
        }
        bansao.setNgayNhapKho(null);
        bansao.setCreatedAt(null);
        banSaoDAO.update(bansao);
    }
    // Lưu bản sao (insert hoặc update tùy)
    public void save(BanSao bansao, String createdBy) throws Exception {
        if (bansao.getMaBanSao() <= 0) insert(bansao, createdBy);
        else update(bansao);
    }

    public List<BanSao> searchBanSao(String isbn, String tieuChi, String keyword, String keywordTo, Integer lastMaBanSao, int pageSize) {
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }
        
        if (isbn == null || isbn.isBlank()) {
            return new ArrayList<>();
        }
        
        try {
            if ("Ngày nhập".equals(tieuChi)) {
            // TH1: Tìm kiếm theo khoảng ngày (Range Search)
                if (keywordTo != null && !keywordTo.isBlank()) {
                    LocalDate fromDate = LocalDate.parse(keyword.trim());
                    LocalDate toDate = LocalDate.parse(keywordTo.trim());

                    if (fromDate.isAfter(toDate)) {
                        LocalDate tmp = fromDate;
                        fromDate = toDate;
                        toDate = tmp;
                    }

                    return banSaoDAO.searchByDateRange(isbn, fromDate, toDate, lastMaBanSao, pageSize);
                }
                return new ArrayList<>();
            }
            
            // Trường hợp 2: Tìm kiếm theo từ khóa
            if (("Mã bản sao".equals(tieuChi) || "Số thứ tự".equals(tieuChi))
                && (keyword == null || keyword.isBlank() || !keyword.matches("\\d+"))) {
                System.err.println("Lỗi: Tiêu chí Mã bản sao/Số thứ tự yêu cầu từ khóa phải là số.");
                return new ArrayList<>();
            }
            return banSaoDAO.search(isbn, keyword, tieuChi, lastMaBanSao, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public int insertBatch(String isbn, int soLuong, int soThuTuBatDau, String tinhTrang,
            String viTriLuuTru, String createdBy) throws Exception {
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng bản sao phải lớn hơn 0.");
        }
        return banSaoDAO.insertBatch(isbn, soLuong, soThuTuBatDau, tinhTrang, viTriLuuTru, createdBy);
    }
    
    // Validate dữ liệu bản sao
    private void validateAndSetDefault(BanSao b) throws Exception {
        if (b == null) throw new Exception("Dữ liệu bản sao không hợp lệ!");

        if (b.getISBN() == null || b.getISBN().isBlank())
            throw new Exception("ISBN không được để trống!");

        if (b.getTinhTrang() == null || b.getTinhTrang().isBlank())
            throw new Exception("Tình trạng bản sao không được để trống!");
    }
}
