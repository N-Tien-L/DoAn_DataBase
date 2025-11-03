package com.mycompany.quanlythuvien.model;

import java.math.BigDecimal;

/**
 *
 * @author Tien
 */
public class Sach {

    private String ISBN;
    private String tenSach;
    private String tacGia;
    private Integer maTheLoai;
    private Integer namXuatBan;
    private String dinhDang;
    private String moTa;
    private Integer maNXB;
    private BigDecimal giaBia;
    private Integer soLuongTon;
    private Integer soTrang;

    public Sach() {
    }

    public Sach(String ISBN, String tenSach, String tacGia, Integer maTheLoai, Integer namXuatBan, String dinhDang, String moTa, Integer maNXB, BigDecimal giaBia, Integer soLuongTon, Integer soTrang) {
        this.ISBN = ISBN;
        this.tenSach = tenSach;
        this.tacGia = tacGia;
        this.maTheLoai = maTheLoai;
        this.namXuatBan = namXuatBan;
        this.dinhDang = dinhDang;
        this.moTa = moTa;
        this.maNXB = maNXB;
        this.giaBia = giaBia;
        this.soLuongTon = soLuongTon;
        this.soTrang = soTrang;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getTenSach() {
        return tenSach;
    }

    public void setTenSach(String tenSach) {
        this.tenSach = tenSach;
    }

    public String getTacGia() {
        return tacGia;
    }

    public void setTacGia(String tacGia) {
        this.tacGia = tacGia;
    }

    public Integer getMaTheLoai() {
        return maTheLoai;
    }

    public void setMaTheLoai(Integer maTheLoai) {
        this.maTheLoai = maTheLoai;
    }

    public Integer getNamXuatBan() {
        return namXuatBan;
    }

    public void setNamXuatBan(Integer namXuatBan) {
        this.namXuatBan = namXuatBan;
    }

    public String getDinhDang() {
        return dinhDang;
    }

    public void setDinhDang(String dinhDang) {
        this.dinhDang = dinhDang;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public Integer getMaNXB() {
        return maNXB;
    }

    public void setMaNXB(Integer maNXB) {
        this.maNXB = maNXB;
    }

    public BigDecimal getGiaBia() {
        return giaBia;
    }

    public void setGiaBia(BigDecimal giaBia) {
        this.giaBia = giaBia;
    }

    public Integer getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(Integer soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    public Integer getSoTrang() {
        return soTrang;
    }

    public void setSoTrang(Integer soTrang) {
        this.soTrang = soTrang;
    }

}
