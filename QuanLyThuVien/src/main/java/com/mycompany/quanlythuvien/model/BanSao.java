package com.mycompany.quanlythuvien.model;

import java.sql.Timestamp;
import java.time.LocalDate;

/**
 *
 * @author Tien
 */
public class BanSao {

    private int maBanSao;
    private String ISBN;
    private int soThuTuTrongKho;
    private String tinhTrang;
    private boolean lendable;
    private LocalDate ngayNhapKho;
    private String viTriLuuTru;
    private Timestamp createdAt;
    private String createdBy;
    
    public BanSao() {
    }

    public BanSao(int maBanSao, String ISBN, int soThuTuTrongKho, String tinhTrang, boolean lendable, LocalDate ngayNhapKho, String viTriLuuTru, Timestamp createdAt, String createdBy) {
        this.maBanSao = maBanSao;
        this.ISBN = ISBN;
        this.soThuTuTrongKho = soThuTuTrongKho;
        this.tinhTrang = tinhTrang;
        this.lendable = lendable;
        this.ngayNhapKho = ngayNhapKho;
        this.viTriLuuTru = viTriLuuTru;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    public int getMaBanSao() {
        return maBanSao;
    }

    public void setMaBanSao(int maBanSao) {
        this.maBanSao = maBanSao;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public int getSoThuTuTrongKho() {
        return soThuTuTrongKho;
    }

    public void setSoThuTuTrongKho(int soThuTuTrongKho) {
        this.soThuTuTrongKho = soThuTuTrongKho;
    }

    public String getTinhTrang() {
        return tinhTrang;
    }

    public void setTinhTrang(String tinhTrang) {
        this.tinhTrang = tinhTrang;
    }

    public LocalDate getNgayNhapKho() {
        return ngayNhapKho;
    }

    public void setNgayNhapKho(LocalDate ngayNhapKho) {
        this.ngayNhapKho = ngayNhapKho;
    }

    public String getViTriLuuTru() {
        return viTriLuuTru;
    }

    public void setViTriLuuTru(String viTriLuuTru) {
        this.viTriLuuTru = viTriLuuTru;
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

    public boolean isLendable() {
        return lendable;
    }

    public void setLendable(boolean lendable) {
        this.lendable = lendable;
    }
}
