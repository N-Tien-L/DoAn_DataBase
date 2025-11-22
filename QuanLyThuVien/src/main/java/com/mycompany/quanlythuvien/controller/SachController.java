package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.NhaXuatBanDAO;
import com.mycompany.quanlythuvien.dao.SachDAO;
import com.mycompany.quanlythuvien.dao.TacGiaDAO;
import com.mycompany.quanlythuvien.dao.TheLoaiDAO;
import com.mycompany.quanlythuvien.model.BanSao;
import com.mycompany.quanlythuvien.model.NhaXuatBan;
import com.mycompany.quanlythuvien.model.Sach;
import com.mycompany.quanlythuvien.model.TacGia;
import com.mycompany.quanlythuvien.model.TheLoai;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tien
 */
public class SachController {
    private final SachDAO sachDAO = new SachDAO();
    private TacGiaDAO tacGiaDAO = new TacGiaDAO();
    private NhaXuatBanDAO nxbDAO = new NhaXuatBanDAO();
    private TheLoaiDAO theLoaiDAO = new TheLoaiDAO();
    private TacGiaController tacGiaController = new TacGiaController();
    private NhaXuatBanController nxbController = new NhaXuatBanController();
    private TheLoaiController theLoaiController = new TheLoaiController();
    
    //lấy toàn bộ ds sách (cho table)
    public List<Sach> getAllForTable(String lastISBNCursor, int pageSize) {
        try {
            return sachDAO.getAllForTable(lastISBNCursor, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public int countTotal(){
        return sachDAO.countTotal();
    }
    
    // Thêm sách
    public void insert(Sach sach, String createdBy) throws Exception {
        validateAndSetDefault(sach);
        if (sachDAO.existsByISBN(sach.getISBN())) {
            throw new Exception("ISBN đã tồn tại!");
        }
        sachDAO.insert(sach, createdBy);
    }

    // Cập nhật sách
    public void update(Sach sach) throws Exception {
        validateAndSetDefault(sach);
        sachDAO.update(sach);
    }
    
    //chưa xong vì còn ràng buộc ct phiếu mượn
    public boolean delete(String isbn) {
        try {
            if (isbn == null || isbn.isBlank()) return false;
            
            //kiem tra con ban sao ko
            List<BanSao> listBanSao = new BanSaoController().getAllByISBN(isbn);
            if (!listBanSao.isEmpty()) {
                throw new Exception("Không thể xóa sách vì còn bản sao!");
            }
            
            return sachDAO.delete(isbn);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public Sach findByISBN(String isbn) {
        try {
            return sachDAO.findByISBN(isbn);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Integer getBanSaoHienCo(String isbn) {
        try {
            return sachDAO.getBanSaoHienCo(isbn);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<Sach> search(String keyword, Integer maTheLoai, Integer maNXB, Integer maTacGia, 
                             Integer namBatDau, Integer namKetThuc, int pageNumber, int pageSize) {
        try {
            // 1. Validate Pagination
            if (pageSize < 1 || pageSize > 100) pageSize = 20;
            if (pageNumber < 1) pageNumber = 1;

            // 2. Validate Logic Năm (Swap nếu Start > End)
            if (namBatDau != null && namKetThuc != null && namBatDau > namKetThuc) {
                int temp = namBatDau;
                namBatDau = namKetThuc;
                namKetThuc = temp;
            }
            
            // 3. Validate Keyword
            if (keyword != null) {
                keyword = keyword.trim();
            }
            
            return sachDAO.search(keyword, maTheLoai, maNXB, maTacGia, namBatDau, namKetThuc, pageNumber, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    private void validateAndSetDefault(Sach s) throws Exception {
    if (s == null) throw new Exception("Dữ liệu sách không hợp lệ!");

        // BẮT BUỘC PHẢI CÓ
        if (s.getISBN() == null || s.getISBN().isBlank())
            throw new Exception("ISBN không được để trống!");

        if (s.getTenSach() == null || s.getTenSach().isBlank())
            throw new Exception("Tên sách không được để trống!");

        if (s.getMaTacGia() == null || s.getMaTacGia() == 0)
            throw new Exception("Vui lòng chọn tác giả!");

        if (s.getMaNXB() == null || s.getMaNXB() == 0)
            throw new Exception("Vui lòng chọn nhà xuất bản!");

        if (s.getMaTheLoai() == null || s.getMaTheLoai() == 0)
            throw new Exception("Vui lòng chọn thể loại!");

        if (s.getNamXuatBan() == null || s.getNamXuatBan() <= 0)
            throw new Exception("Năm xuất bản không hợp lệ!");

        if (s.getDinhDang() == null || s.getDinhDang().isBlank())
            throw new Exception("Vui lòng chọn định dạng!");

        // Riêng bản in -> phải có số trang
        if ("Bản in".equalsIgnoreCase(s.getDinhDang())) {
            if (s.getSoTrang() == null || s.getSoTrang() <= 0)
                throw new Exception("Số trang phải > 0!");
        }

        if (s.getGiaBia() == null || s.getGiaBia().compareTo(BigDecimal.ZERO) <= 0)
            throw new Exception("Giá bìa phải lớn hơn 0!");

        // Các field tùy chọn
        if (s.getMoTa() == null) s.setMoTa("");
    }
    
    public List<TacGia> getAllTacGia(int pageSize) {
        try {
            int firstPageCursor = 0;
            return tacGiaDAO.getAll(firstPageCursor, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public int getTotalTacGia() {
        return tacGiaController.getTotalTacGia();
    }
    
    public List<NhaXuatBan> getAllNXB(int pageSize) {
        try {
            int firstPageCursor = 0;
            return nxbDAO.getAll(firstPageCursor, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public int getTotalNXB() {
        return nxbController.getTotalNXB();
    }
    
    public List<TheLoai> getAllTheLoai(int pageSize) {
        try {
            int firstPageCursor = 0;
            return theLoaiDAO.getAll(firstPageCursor, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public int getTotalTheLoai() {
        return theLoaiController.getTotalTheLoai();
    }

    public List<TacGia> getAllTacGiaNoPaging() {
        return tacGiaController.getAllTacGiaNoPaging();
    }
    
    public List<NhaXuatBan> getAllNXBNoPaging() {
        return nxbController.getAllNXBNoPaging();
    }
    
    public List<TheLoai> getAllTheLoaiNoPaging() {
        return theLoaiController.getAllTheLoaiNoPaging();
    }
}
