package com.mycompany.quanlythuvien.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 *
 * @author Tien
 */
public class Sach {
    private String ISBN;
    private String tenSach;
    private Integer maTacGia;
    private Integer maTheLoai;
    private Integer namXuatBan;
    private String dinhDang;
    private String moTa;
    private Integer maNXB;
    private BigDecimal giaBia;
    private Integer soLuongTon;
    private Integer soTrang;
    private Timestamp createdAt;
    private String createdBy;
    //Thêm thuộc tính phụ để HIỂN THỊ Tên tác giả, NXB, thể loại trong bảng rút gọn
    private String tenTacGia;
    private String tenTheLoai;
    private String tenNXB;
    
    public Sach() {
    }

    public Sach(String ISBN, String tenSach, Integer maTacGia, Integer maTheLoai, Integer namXuatBan, String dinhDang, String moTa, Integer maNXB, BigDecimal giaBia, Integer soLuongTon, Integer soTrang, Timestamp createdAt, String createdBy) {
        this.ISBN = ISBN;
        this.tenSach = tenSach;
        this.maTacGia = maTacGia;
        this.maTheLoai = maTheLoai;
        this.namXuatBan = namXuatBan;
        this.dinhDang = dinhDang;
        this.moTa = moTa;
        this.maNXB = maNXB;
        this.giaBia = giaBia;
        this.soLuongTon = soLuongTon;
        this.soTrang = soTrang;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
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

    public Integer getMaTacGia() {
        return maTacGia;
    }

    public void setMaTacGia(Integer maTacGia) {
        this.maTacGia = maTacGia;
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

    public String getTenTacGia() {
        return tenTacGia;
    }

    public void setTenTacGia(String tenTacGia) {
        this.tenTacGia = tenTacGia;
    }

    public String getTenTheLoai() {
        return tenTheLoai;
    }

    public void setTenTheLoai(String tenTheLoai) {
        this.tenTheLoai = tenTheLoai;
    }

    public String getTenNXB() {
        return tenNXB;
    }

    public void setTenNXB(String tenNXB) {
        this.tenNXB = tenNXB;
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
}
