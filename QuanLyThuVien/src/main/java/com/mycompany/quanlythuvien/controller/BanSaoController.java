package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.BanSaoDAO;
import com.mycompany.quanlythuvien.model.BanSao;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
    public void insert(BanSao bansao) throws Exception {
        validateAndSetDefault(bansao);
        banSaoDAO.insert(bansao);
    }
    
    // Cập nhật bản sao
    public void update(BanSao bansao) throws Exception {
        validateAndSetDefault(bansao);
        if (bansao.getMaBanSao() <= 0) {
            throw new Exception("Bản sao chưa tồn tại, không thể cập nhật!");
        }
        banSaoDAO.update(bansao);
    }
    // Lưu bản sao (insert hoặc update tùy)
    public void save(BanSao bansao) throws Exception {
        if (bansao.getMaBanSao() <= 0) insert(bansao);
        else update(bansao);
    }

    // Validate dữ liệu bản sao
    private void validateAndSetDefault(BanSao b) throws Exception {
        if (b == null) throw new Exception("Dữ liệu bản sao không hợp lệ!");

        if (b.getISBN() == null || b.getISBN().isBlank())
            throw new Exception("ISBN không được để trống!");

        if (b.getNgayNhapKho() == null)
            throw new Exception("Ngày nhập không được để trống!");

        if (b.getTinhTrang() == null || b.getTinhTrang().isBlank())
            throw new Exception("Tình trạng bản sao không được để trống!");
    }
    
    //String -> LocalDate
    public LocalDate parseDate(String input) throws Exception {
        if (input == null || input.isEmpty()) return null;
        try {
            return LocalDate.parse(input);
        } catch (DateTimeParseException ex) {
            throw new Exception("Ngày nhập không đúng định dạng yyyy-MM-dd");
        }
    }
}
