package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.SachDAO;
import com.mycompany.quanlythuvien.model.Sach;
import java.util.List;

/**
 *
 * @author Tien
 */
public class SachController {
    private final SachDAO sachDAO = new SachDAO();
    
    public List<Sach> getAllSach(){
        try {
            return sachDAO.getAll();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); //ds rong
        }
    }
    
    public boolean themSach(Sach s) {
        try {
            return sachDAO.insert(s);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean capNhatSach(Sach s) {
        try {
            return sachDAO.update(s);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean xoaSach(String isbn) {
        try {
            return sachDAO.delete(isbn);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public Sach timSachTheoISBN(String isbn) {
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
    
    public List<Sach> search(String keyword, String tieuChi) {
        try {
            return sachDAO.search(keyword, tieuChi);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
