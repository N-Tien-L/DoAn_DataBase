package com.mycompany.quanlythuvien.model;

import java.time.LocalDateTime;

/**
 * @author Tien
 */
public class YeuCauResetMK {
    
    private int id;
    private String emailThuThu;
    private String lyDo;
    private String trangThai; // Pending, Approved, Rejected, Done
    private LocalDateTime createdAt;
    private String xuLyBoi;
    private LocalDateTime xuLyLuc;
    private String ghiChuXuLy;
    
    // Thông tin bổ sung (join với TAIKHOAN)
    private String hoTenThuThu;
    private String hoTenNguoiXuLy;

    public YeuCauResetMK() {
    }

    public YeuCauResetMK(String emailThuThu, String lyDo) {
        this.emailThuThu = emailThuThu;
        this.lyDo = lyDo;
        this.trangThai = "Pending";
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmailThuThu() {
        return emailThuThu;
    }

    public void setEmailThuThu(String emailThuThu) {
        this.emailThuThu = emailThuThu;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getXuLyBoi() {
        return xuLyBoi;
    }

    public void setXuLyBoi(String xuLyBoi) {
        this.xuLyBoi = xuLyBoi;
    }

    public LocalDateTime getXuLyLuc() {
        return xuLyLuc;
    }

    public void setXuLyLuc(LocalDateTime xuLyLuc) {
        this.xuLyLuc = xuLyLuc;
    }

    public String getGhiChuXuLy() {
        return ghiChuXuLy;
    }

    public void setGhiChuXuLy(String ghiChuXuLy) {
        this.ghiChuXuLy = ghiChuXuLy;
    }

    public String getHoTenThuThu() {
        return hoTenThuThu;
    }

    public void setHoTenThuThu(String hoTenThuThu) {
        this.hoTenThuThu = hoTenThuThu;
    }

    public String getHoTenNguoiXuLy() {
        return hoTenNguoiXuLy;
    }

    public void setHoTenNguoiXuLy(String hoTenNguoiXuLy) {
        this.hoTenNguoiXuLy = hoTenNguoiXuLy;
    }
    
    // Helper methods
    public boolean isPending() {
        return "Pending".equals(trangThai);
    }
    
    public boolean isApproved() {
        return "Approved".equals(trangThai);
    }
    
    public boolean isRejected() {
        return "Rejected".equals(trangThai);
    }
    
    public boolean isDone() {
        return "Done".equals(trangThai);
    }

    @Override
    public String toString() {
        return "YeuCauResetMK{" +
                "id=" + id +
                ", emailThuThu='" + emailThuThu + '\'' +
                ", trangThai='" + trangThai + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
