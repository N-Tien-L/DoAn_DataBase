/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlythuvien.model;

/**
 *
 * @author ASUS
 */
public class TacGia {
    private int maTacGia;
    private String tenTacGia;
    private String website;
    private String ghiChu;

    public TacGia() {
    }

    public TacGia(int maTacGia, String tenTacGia, String website, String ghiChu) {
        this.maTacGia = maTacGia;
        this.tenTacGia = tenTacGia;
        this.website = website;
        this.ghiChu = ghiChu;
    }

    public int getMaTacGia() {
        return maTacGia;
    }

    public void setMaTacGia(int maTacGia) {
        this.maTacGia = maTacGia;
    }

    public String getTenTacGia() {
        return tenTacGia;
    }

    public void setTenTacGia(String tenTacGia) {
        this.tenTacGia = tenTacGia;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
    
    
}
