package com.mycompany.quanlythuvien.model;

import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * Loan history entry combining PHIEUMUON, CT_PM and SACH info for reporting
 */
public class LoanHistoryEntry {
    private int idPM;
    private int maBanSao;
    private String isbn;
    private String tenSach;
    private LocalDate ngayMuon;
    private LocalDate hanTra;
    private LocalDate ngayTraThucTe;
    private String tinhTrangKhiTra;
    private BigDecimal phat; // optional associated fine

    public LoanHistoryEntry() {}

    public LoanHistoryEntry(int idPM, int maBanSao, String isbn, String tenSach, LocalDate ngayMuon, LocalDate hanTra,
            LocalDate ngayTraThucTe, String tinhTrangKhiTra, BigDecimal phat) {
        this.idPM = idPM;
        this.maBanSao = maBanSao;
        this.isbn = isbn;
        this.tenSach = tenSach;
        this.ngayMuon = ngayMuon;
        this.hanTra = hanTra;
        this.ngayTraThucTe = ngayTraThucTe;
        this.tinhTrangKhiTra = tinhTrangKhiTra;
        this.phat = phat;
    }


    public int getIdPM() { return idPM; }
    public void setIdPM(int idPM) { this.idPM = idPM; }
    public int getMaBanSao() { return maBanSao; }
    public void setMaBanSao(int maBanSao) { this.maBanSao = maBanSao; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getTenSach() { return tenSach; }
    public void setTenSach(String tenSach) { this.tenSach = tenSach; }
    public LocalDate getNgayMuon() { return ngayMuon; }
    public void setNgayMuon(LocalDate ngayMuon) { this.ngayMuon = ngayMuon; }
    public LocalDate getHanTra() { return hanTra; }
    public void setHanTra(LocalDate hanTra) { this.hanTra = hanTra; }
    public LocalDate getNgayTraThucTe() { return ngayTraThucTe; }
    public void setNgayTraThucTe(LocalDate ngayTraThucTe) { this.ngayTraThucTe = ngayTraThucTe; }
    public String getTinhTrangKhiTra() { return tinhTrangKhiTra; }
    public void setTinhTrangKhiTra(String tinhTrangKhiTra) { this.tinhTrangKhiTra = tinhTrangKhiTra; }
    public BigDecimal getPhat() { return phat; }
    public void setPhat(BigDecimal phat) { this.phat = phat; }
}
