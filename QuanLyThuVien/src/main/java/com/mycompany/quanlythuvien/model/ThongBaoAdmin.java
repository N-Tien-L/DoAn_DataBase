package com.mycompany.quanlythuvien.model;

import java.sql.Timestamp;

/**
 * @author Tien
 */
public class ThongBaoAdmin {
    private int idThongBao;
    private String tieuDe;
    private String noiDung;
    private String createdBy;
    private Timestamp createdAt;
    private int recipientCount;     // Tổng số người nhận
    private int readCount;          // Số người đã đọc
    private int unreadCount;        // Số người chưa đọc

    public ThongBaoAdmin() {
    }

    public ThongBaoAdmin(int idThongBao, String tieuDe, String noiDung, String createdBy, 
                            Timestamp createdAt, int recipientCount, int readCount) {
        this.idThongBao = idThongBao;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.recipientCount = recipientCount;
        this.readCount = readCount;
        this.unreadCount = recipientCount - readCount;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public int getRecipientCount() {
        return recipientCount;
    }

    public void setRecipientCount(int recipientCount) {
        this.recipientCount = recipientCount;
        this.unreadCount = recipientCount - readCount;
    }

    public int getReadCount() {
        return readCount;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
        this.unreadCount = recipientCount - readCount;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
