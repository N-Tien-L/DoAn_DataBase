package com.mycompany.quanlythuvien.model;

import java.math.BigDecimal;

public class BanDocPhat {

    // thông tin cơ bản
    private int idBD;
    private String hoTen;
    private String email;
    private String diaChi;
    private String sdt;

    // thống kê phạt
    private int soLuongTreHan;
    private int soLuongHongSach;
    private int soLuongMatSach;
    private BigDecimal tongTienPhat;
    private BigDecimal tongTienChuaDong;

    // getter & setter
    public int getIdBD() {
        return idBD;
    }

    public void setIdBD(int idBD) {
        this.idBD = idBD;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public int getSoLuongTreHan() {
        return soLuongTreHan;
    }

    public void setSoLuongTreHan(int soLuongTreHan) {
        this.soLuongTreHan = soLuongTreHan;
    }

    public int getSoLuongHongSach() {
        return soLuongHongSach;
    }

    public void setSoLuongHongSach(int soLuongHongSach) {
        this.soLuongHongSach = soLuongHongSach;
    }

    public int getSoLuongMatSach() {
        return soLuongMatSach;
    }

    public void setSoLuongMatSach(int soLuongMatSach) {
        this.soLuongMatSach = soLuongMatSach;
    }

    public BigDecimal getTongTienPhat() {
        return tongTienPhat;
    }

    public void setTongTienPhat(BigDecimal tongTienPhat) {
        this.tongTienPhat = tongTienPhat;
    }

    public BigDecimal getTongTienChuaDong() {
        return tongTienChuaDong;
    }

    public void setTongTienChuaDong(BigDecimal tongTienChuaDong) {
        this.tongTienChuaDong = tongTienChuaDong;
    }
}
