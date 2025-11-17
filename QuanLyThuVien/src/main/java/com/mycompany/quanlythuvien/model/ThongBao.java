package com.mycompany.quanlythuvien.model;

import java.sql.Timestamp;

/**
 * Model cho bảng THONGBAO
 * @author Tien
 */
public class ThongBao {
    private int idThongBao;
    private String tieuDe;
    private String noiDung;
    private String nguoiTao;
    private Timestamp createdAt;

    public ThongBao() {
    }

    public ThongBao(int idThongBao, String tieuDe, String noiDung, String nguoiTao, Timestamp createdAt) {
        this.idThongBao = idThongBao;
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.nguoiTao = nguoiTao;
        this.createdAt = createdAt;
    }

    public ThongBao(String tieuDe, String noiDung, String nguoiTao) {
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.nguoiTao = nguoiTao;
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

    public String getNguoiTao() {
        return nguoiTao;
    }

    public void setNguoiTao(String nguoiTao) {
        this.nguoiTao = nguoiTao;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
