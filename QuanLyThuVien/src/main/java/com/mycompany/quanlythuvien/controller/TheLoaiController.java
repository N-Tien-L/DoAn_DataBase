package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.TheLoaiDAO;
import com.mycompany.quanlythuvien.model.TheLoai;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Tien
 */
public class TheLoaiController {
    private final TheLoaiDAO theLoaiDAO;
    
    public TheLoaiController(){
        this.theLoaiDAO = new TheLoaiDAO();
    }
    
    public List<TheLoai> getAllTheLoaiNoPaging() {
        return theLoaiDAO.findAll();
    }

    public List<TheLoai> getAllTheLoai(int lastMaTLCursor, int pageSize) {
        if (pageSize < 1 || pageSize > 100) pageSize = 10;
        return theLoaiDAO.getAll(lastMaTLCursor, pageSize);
    }
    public int getTotalTheLoai() {
        return theLoaiDAO.getTotalTL();
    }
    
    public int getTotalPages(int pageSize) {
        int total = getTotalTheLoai();
        if (total <= 0) return 0;
        if (pageSize <= 0) pageSize = 10;
        return (int) Math.ceil((double) total / pageSize);
    }
    
    public boolean addTheLoai(TheLoai tl) throws Exception {
        if (tl.getTenTheLoai() == null || tl.getTenTheLoai().trim().isEmpty()){
            throw new Exception("Tên thể loại không được để trống!");
        }
        return theLoaiDAO.insert(tl);
    }
    
    public boolean updateTheLoai(TheLoai tl) throws Exception {
        if (tl.getMaTheLoai() <= 0) {
            throw new Exception("Mã thể loại không hợp lệ!");
        }
        
        if (tl.getTenTheLoai() == null || tl.getTenTheLoai().trim().isEmpty()) {
            throw new Exception("Tên thể loại không được để trống!");
        }
        return theLoaiDAO.update(tl);
    }
    
    public boolean deleteTheLoai(int maTheLoai) throws Exception {
        if (maTheLoai <= 0) {
            throw new Exception("Mã thể loại không hợp lệ!");
        }
        return theLoaiDAO.delete(maTheLoai);
    }
    
    public List<TheLoai> searchTheLoai(String keyword, String column, Integer lastMaTheLoaiCursor, int pageSize) {
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }
        return theLoaiDAO.search(keyword, column, lastMaTheLoaiCursor, pageSize);
    }
    
    public Optional<TheLoai> getTheLoaiById(int id) {
        if (id <= 0) return Optional.empty();
        return theLoaiDAO.getById(id);
    }
}
