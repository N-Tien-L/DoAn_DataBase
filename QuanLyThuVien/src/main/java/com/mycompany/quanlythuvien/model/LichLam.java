package com.mycompany.quanlythuvien.model;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;

public class LichLam {

    private int idLich;
    private String emailThuThu;
    private Date ngay;
    private Time gioBatDau;
    private Time gioKetThuc;
    private String trangThai;
    private String ghiChu;
    private String createdBy;
    private LocalDateTime createdAt;

    public LichLam() {
    }

    public LichLam(int idLich, String emailThuThu, Date ngay, Time gioBatDau, Time gioKetThuc, String trangThai, String ghiChu, String createdBy, LocalDateTime createdAt) {
        this.idLich = idLich;
        this.emailThuThu = emailThuThu;
        this.ngay = ngay;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.trangThai = trangThai;
        this.ghiChu = ghiChu;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public int getIdLich() {
        return idLich;
    }

    public void setIdLich(int idLich) {
        this.idLich = idLich;
    }

    public String getEmailThuThu() {
        return emailThuThu;
    }

    public void setEmailThuThu(String emailThuThu) {
        this.emailThuThu = emailThuThu;
    }

    public Date getNgay() {
        return ngay;
    }

    public void setNgay(Date ngay) {
        this.ngay = ngay;
    }

    public Time getGioBatDau() {
        return gioBatDau;
    }

    public void setGioBatDau(Time gioBatDau) {
        this.gioBatDau = gioBatDau;
    }

    public Time getGioKetThuc() {
        return gioKetThuc;
    }

    public void setGioKetThuc(Time gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
