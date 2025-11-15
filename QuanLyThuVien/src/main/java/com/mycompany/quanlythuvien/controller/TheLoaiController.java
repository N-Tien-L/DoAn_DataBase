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
    
    public List<TheLoai> getAllTheLoai(int lastMaTLCursor, int pageSize) {
        return theLoaiDAO.getAll(lastMaTLCursor, pageSize);
    }
    public int getTotalTheLoai() {
        return theLoaiDAO.getTotalTL();
    }
    public boolean addTheLoai(TheLoai tl) {
        if (tl.getTenTheLoai() == null || tl.getTenTheLoai().trim().isEmpty()){
            System.out.println("Tên thể loại không được để trống!");
            return false;
        }
        return theLoaiDAO.insert(tl);
    }
    
    public boolean updateTheLoai(TheLoai tl) {
        if (tl.getMaTheLoai() <= 0) {
            System.out.println("Mã thể loại không hợp lệ!");
            return false;
        }
        
        if (tl.getTenTheLoai() == null || tl.getTenTheLoai().trim().isEmpty()) {
            System.out.print("Tên thể loại không được để trống!");
            return false;
        }
        return theLoaiDAO.update(tl);
    }
    
    public boolean deleteTheLoai(int maTheLoai) {
        if (maTheLoai <= 0) {
            System.out.println("Mã thể loại không hợp lệ!");
            return false;
        }
        return theLoaiDAO.delete(maTheLoai);
    }
    
    public List<TheLoai> searchTheLoai(String keyword, String column) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return theLoaiDAO.getAll(0, getTotalTheLoai());
        }
        return theLoaiDAO.search(keyword, column);
    }
    
    public Optional<TheLoai> getTheLoaiById(int id) {
        if (id <= 0) return Optional.empty();
        return theLoaiDAO.getById(id);
    }
}
