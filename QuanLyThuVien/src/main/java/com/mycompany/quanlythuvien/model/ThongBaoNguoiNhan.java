package com.mycompany.quanlythuvien.model;

import java.time.LocalDateTime;

/**
 * @author Tien
 */
public class ThongBaoNguoiNhan {
    // Từ THONGBAO
    private int idThongBao;
    private String tieuDe;
    private String noiDung;
    private String createdBy;
    private LocalDateTime createdAt;
    
    // Từ THONGBAO_NGUOINHAN
    private String email;
    private boolean daDoc;
    private LocalDateTime readAt;

    public ThongBaoNguoiNhan() {
    }

    public ThongBaoNguoiNhan(int idThongBao, String tieuDe, String noiDung, String createdBy, 
                             LocalDateTime createdAt, String email, boolean daDoc, LocalDateTime readAt) {
        this.idThongBao = idThongBao;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.email = email;
        this.daDoc = daDoc;
        this.readAt = readAt;
    }

    public int getIdThongBao() {
        return idThongBao;
    }

    public void setIdThongBao(int idThongBao) {
        this.idThongBao = idThongBao;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isDaDoc() {
        return daDoc;
    }

    public void setDaDoc(boolean daDoc) {
        this.daDoc = daDoc;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}
