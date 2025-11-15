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
import java.util.Optional;

/**
 *
 * @author ASUS
 */
public class TacGiaController {
    private final TacGiaDAO dao;

    public TacGiaController() {
        this.dao = new TacGiaDAO();
    }
    
    public List<TacGia> getAllTacGia(int lastMaTacGiaCursor, int pageSize) {
        return dao.getAll(lastMaTacGiaCursor, pageSize);
    }
    public int getTotalTacGia() {
        return dao.getTotalTacGia();
    }
    
    public boolean insertTacGia(TacGia tg) {
        if (tg.getTenTacGia() == null || tg.getTenTacGia().isEmpty()) {
            System.out.println("Tên tác giả không được để trống");
            return false;
        }
        
        if (tg.getTenTacGia().length() > 100) {
            System.out.println("Tên tác giả quá dài, tối đa 100 ký tự");
            return false;
        }
        
        if (tg.getWebsite() != null && tg.getWebsite().length() > 255) {
            System.out.println("Website quá dài, tối đa 255 ký tự");
            return false;
        }
        
        if (tg.getWebsite() != null && !tg.getWebsite().isEmpty()) {
            try {
                new URL(tg.getWebsite());
            } catch (MalformedURLException e) {
                System.out.println("Website không hợp lệ");
                return false;
            }
        }
        return dao.insert(tg);
    }
    
    public boolean updateTacGia(TacGia tg) {
        if (tg.getMaTacGia() <= 0) {
            System.out.println("ID tác giả không hợp lệ!");
            return false;
        }
        
        if (tg.getTenTacGia() == null || tg.getTenTacGia().isEmpty()) {
            System.out.println("Tên tác giả không được để trống!");
            return false;
        }
        
        if (tg.getTenTacGia().length() > 100) {
            System.out.println("Tên tác giả quá dài, tối đa 100 kí tự");
            return false;
        }
        
        if (tg.getWebsite() != null && tg.getWebsite().length() > 255) {
            System.out.println("Website quá dài, tối đa 255 ký tự!");
            return false;
        }
        
        if (tg.getWebsite() != null && !tg.getWebsite().isEmpty()) {
            try {
                new URL(tg.getWebsite());
            } catch (MalformedURLException e) {
                System.out.println("Website không hợp lệ");
                return false;
            }
        }
        
        return dao.update(tg);
    }
    
    public boolean deleteTacGia(int maTG) {
        if (maTG <= 0) {
            System.out.println("Mã tác giả không hợp lệ!");
            return false;
        }
        return dao.delete(maTG);
    }
    
    public List<TacGia> searchTacGia(String keyword, String column) {
        if (keyword == null || keyword.isEmpty()) {
            return dao.getAll(0, getTotalTacGia());
        }
        
        return dao.search(keyword, column);
    }
    
    public Optional<TacGia> getTacGiaById(int maTG){
        if (maTG <= 0) return Optional.empty();
        return dao.getById(maTG);
    }
}
