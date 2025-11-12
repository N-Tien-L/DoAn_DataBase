package com.mycompany.quanlythuvien.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author Tien
 */
public class Phat {

    private int idPhat;
    private int idPM;
    private int maBanSao;
    private String loaiPhat;
    private BigDecimal soTien;
    private LocalDate ngayGhiNhan;
    private String trangThai;

    // additional
    private int idBD;

    public Phat() {
    }

    public Phat(int idPhat, int idPM, int maBanSao, String loaiPhat, BigDecimal soTien, LocalDate ngayGhiNhan,
            String trangThai) {
        this.idPhat = idPhat;
        this.idPM = idPM;
        this.maBanSao = maBanSao;
        this.loaiPhat = loaiPhat;
        this.soTien = soTien;
        this.ngayGhiNhan = ngayGhiNhan;
        this.trangThai = trangThai;
    }

    public int getIdPhat() {
        return idPhat;
    }

    public void setIdPhat(int idPhat) {
        this.idPhat = idPhat;
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

    public String getLoaiPhat() {
        return loaiPhat;
    }

    public void setLoaiPhat(String loaiPhat) {
        this.loaiPhat = loaiPhat;
    }

    public BigDecimal getSoTien() {
        return soTien;
    }

    public void setSoTien(BigDecimal soTien) {
        this.soTien = soTien;
    }

    public LocalDate getNgayGhiNhan() {
        return ngayGhiNhan;
    }

    public void setNgayGhiNhan(LocalDate ngayGhiNhan) {
        this.ngayGhiNhan = ngayGhiNhan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public void setIdBD(int idBD) {
        this.idBD = idBD;
    }

    public int getIdBD() {
        return idBD;
    }

}
