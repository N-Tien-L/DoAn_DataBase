package com.mycompany.quanlythuvien.model;

import java.time.LocalDate;

/**
 *
 * @author Tien
 */
public class ChiTietPhieuMuon {
    private int idPM;
    private int maBanSao;
    private LocalDate ngayTraThucTe;
    private String tinhTrangKhiTra;
    private String emailNguoiNhan;

    public ChiTietPhieuMuon() {
    }

    public ChiTietPhieuMuon(int idPM, int maBanSao, LocalDate ngayTraThucTe, String tinhTrangKhiTra, String emailNguoiNhan) {
        this.idPM = idPM;
        this.maBanSao = maBanSao;
        this.ngayTraThucTe = ngayTraThucTe;
        this.tinhTrangKhiTra = tinhTrangKhiTra;
        this.emailNguoiNhan = emailNguoiNhan;
    }

    public int getIdPM() {
        return idPM;
    }

    public void setIdPM(int idPM) {
        this.idPM = idPM;
    }

    public int getMaBanSao() {
        return maBanSao;
    }

    public void setMaBanSao(int maBanSao) {
        this.maBanSao = maBanSao;
    }

    public LocalDate getNgayTraThucTe() {
        return ngayTraThucTe;
    }

    public void setNgayTraThucTe(LocalDate ngayTraThucTe) {
        this.ngayTraThucTe = ngayTraThucTe;
    }

    public String getTinhTrangKhiTra() {
        return tinhTrangKhiTra;
    }

    public void setTinhTrangKhiTra(String tinhTrangKhiTra) {
        this.tinhTrangKhiTra = tinhTrangKhiTra;
    }

    public String getEmailNguoiNhan() {
        return emailNguoiNhan;
    }

    public void setEmailNguoiNhan(String emailNguoiNhan) {
        this.emailNguoiNhan = emailNguoiNhan;
    }
    
    
}
