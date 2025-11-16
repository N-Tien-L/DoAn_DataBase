package com.mycompany.quanlythuvien.model;

import java.sql.Timestamp;

public class TaiKhoanProfile {
    private String email;
    private String hoTen;
    private String role;
    private String status;
    private Timestamp createdAt;
    private String createdBy;
    private int soBanDocTao;
    private int soSachThem;
    private int soBanSaoNhap;
    private int soPhieuMuonLap;
    private int soBanSaoDangChoMuon;

    public TaiKhoanProfile() {
    }

    public TaiKhoanProfile(String email, String hoTen, String role, String status, Timestamp createdAt, String createdBy, int soBanDocTao, int soSachThem, int soBanSaoNhap, int soPhieuMuonLap, int soBanSaoDangChoMuon) {
        this.email = email;
        this.hoTen = hoTen;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.soBanDocTao = soBanDocTao;
        this.soSachThem = soSachThem;
        this.soBanSaoNhap = soBanSaoNhap;
        this.soPhieuMuonLap = soPhieuMuonLap;
        this.soBanSaoDangChoMuon = soBanSaoDangChoMuon;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public int getSoBanDocTao() {
        return soBanDocTao;
    }

    public void setSoBanDocTao(int soBanDocTao) {
        this.soBanDocTao = soBanDocTao;
    }

    public int getSoSachThem() {
        return soSachThem;
    }

    public void setSoSachThem(int soSachThem) {
        this.soSachThem = soSachThem;
    }

    public int getSoBanSaoNhap() {
        return soBanSaoNhap;
    }

    public void setSoBanSaoNhap(int soBanSaoNhap) {
        this.soBanSaoNhap = soBanSaoNhap;
    }

    public int getSoPhieuMuonLap() {
        return soPhieuMuonLap;
    }

    public void setSoPhieuMuonLap(int soPhieuMuonLap) {
        this.soPhieuMuonLap = soPhieuMuonLap;
    }

    public int getSoBanSaoDangChoMuon() {
        return soBanSaoDangChoMuon;
    }

    public void setSoBanSaoDangChoMuon(int soBanSaoDangChoMuon) {
        this.soBanSaoDangChoMuon = soBanSaoDangChoMuon;
    }
    
    @Override
    public String toString() {
        return "TaiKhoanProfile{" +
                "email='" + email + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                ", soBanDocTao=" + soBanDocTao +
                ", soSachThem=" + soSachThem +
                ", soBanSaoNhap=" + soBanSaoNhap +
                ", soPhieuMuonLap=" + soPhieuMuonLap +
                ", soBanSaoDangChoMuon=" + soBanSaoDangChoMuon +
                '}';
    }
}
