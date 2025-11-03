package com.mycompany.quanlythuvien.model;

import java.time.LocalDate;

/**
 *
 * @author Tien
 */
public class PhieuMuon {

    private int idPM;
    private int idBD;
    private String emailNguoiLap;
    private LocalDate ngayMuon;
    private LocalDate hanTra;

    public PhieuMuon() {
    }

    public PhieuMuon(int idPM, int idBD, String emailNguoiLap, LocalDate ngayMuon, LocalDate hanTra) {
        this.idPM = idPM;
        this.idBD = idBD;
        this.emailNguoiLap = emailNguoiLap;
        this.ngayMuon = ngayMuon;
        this.hanTra = hanTra;
    }

    public int getIdPM() {
        return idPM;
    }

    public void setIdPM(int idPM) {
        this.idPM = idPM;
    }

    public int getIdBD() {
        return idBD;
    }

    public void setIdBD(int idBD) {
        this.idBD = idBD;
    }

    public String getEmailNguoiLap() {
        return emailNguoiLap;
    }

    public void setEmailNguoiLap(String emailNguoiLap) {
        this.emailNguoiLap = emailNguoiLap;
    }

    public LocalDate getNgayMuon() {
        return ngayMuon;
    }

    public void setNgayMuon(LocalDate ngayMuon) {
        this.ngayMuon = ngayMuon;
    }

    public LocalDate getHanTra() {
        return hanTra;
    }

    public void setHanTra(LocalDate hanTra) {
        this.hanTra = hanTra;
    }

}
