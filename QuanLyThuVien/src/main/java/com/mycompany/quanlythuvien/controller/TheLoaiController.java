package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.TheLoaiDAO;
import com.mycompany.quanlythuvien.model.TheLoai;
import java.util.List;

/**
 *
 * @author Tien
 */
public class TheLoaiController {
    private final TheLoaiDAO theLoaiDAO = new TheLoaiDAO();
    
    public List<TheLoai> getAllTheLoai() {
        try {
            return theLoaiDAO.getAll();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean themTheLoai(TheLoai tl) {
        try {
            return theLoaiDAO.insert(tl);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean capNhatTheLoai(TheLoai tl) {
        try {
            return theLoaiDAO.update(tl);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean xoaTheLoai(int maTheLoai) {
        try {
            return theLoaiDAO.delete(maTheLoai);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public TheLoai timTheLoaiTheoId(int id) {
        try {
            return theLoaiDAO.findById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
