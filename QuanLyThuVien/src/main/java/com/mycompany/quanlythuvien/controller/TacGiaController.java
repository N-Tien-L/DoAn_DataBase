/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.TacGiaDAO;
import com.mycompany.quanlythuvien.model.TacGia;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 *
 * @author ASUS
 */
public class TacGiaController {
    private final TacGiaDAO dao;

    public TacGiaController() {
        this.dao = new TacGiaDAO();
    }
    
    public List<TacGia> getAllTacGiaNoPaging() {
        return dao.findAll();
    }

    public List<TacGia> getAllTacGia(int lastMaTacGiaCursor, int pageSize) {
        return dao.getAll(lastMaTacGiaCursor, pageSize);
    }
    
    public boolean insertTacGia(TacGia tg) throws Exception {
        if (tg.getTenTacGia() == null || tg.getTenTacGia().isEmpty()) {
            throw new Exception("Tên tác giả không được để trống");
        }
        
        if (tg.getTenTacGia().length() > 100) {
            throw new Exception("Tên tác giả quá dài, tối đa 100 ký tự");
        }
        
        if (tg.getWebsite() != null && tg.getWebsite().length() > 255) {
            throw new Exception("Website quá dài, tối đa 255 ký tự");
        }
        
        if (tg.getWebsite() != null && !tg.getWebsite().isEmpty()) {
            try {
                new URL(tg.getWebsite());
            } catch (MalformedURLException e) {
                throw new Exception("Website không hợp lệ: " + e.getMessage());
            }
        }
        return dao.insert(tg);
    }
    
    public boolean updateTacGia(TacGia tg) throws Exception {
        if (tg.getMaTacGia() <= 0) {
            throw new Exception("ID tác giả không hợp lệ!");
        }
        
        if (tg.getTenTacGia() == null || tg.getTenTacGia().isEmpty()) {
            throw new Exception("Tên tác giả không được để trống!");
        }
        
        if (tg.getTenTacGia().length() > 100) {
            throw new Exception("Tên tác giả quá dài, tối đa 100 kí tự");
        }
        
        if (tg.getWebsite() != null && tg.getWebsite().length() > 255) {
            throw new Exception("Website quá dài, tối đa 255 ký tự!");
        }
        
        if (tg.getWebsite() != null && !tg.getWebsite().isEmpty()) {
            try {
                new URL(tg.getWebsite());
            } catch (MalformedURLException e) {
                throw new Exception("Website không hợp lệ: " + e.getMessage());
            }
        }
        
        return dao.update(tg);
    }
    
    public boolean deleteTacGia(int maTG) throws Exception {
        if (maTG <= 0) {
            throw new Exception("Mã tác giả không hợp lệ!");
        }
        return dao.delete(maTG);
    }
    
    public List<TacGia> searchTacGia(String keyword, String column, Integer lastMaTacGiaCursor, int pageSize) {
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
        
        return dao.search(keyword, column, lastMaTacGiaCursor, pageSize);
    }
}
