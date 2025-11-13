package com.mycompany.quanlythuvien.model;

import java.time.LocalDate;

/**
 * Model chứa thông tin chi tiết phiếu mượn kèm thông tin bạn đọc
 * Dùng cho typing gợi ý khi tạo vé phạt
 * 
 * @author Tien
 */
public class ChiTietPhieuMuonInfo {
    private int idPM;
    private int maBanSao;
    private int idBD;
    private String hoTen;
    private String sdt;
    private LocalDate ngayMuon;
    private LocalDate ngayHenTra;

    public ChiTietPhieuMuonInfo() {
    }

    public ChiTietPhieuMuonInfo(int idPM, int maBanSao, int idBD, String hoTen, String sdt, LocalDate ngayMuon,
            LocalDate ngayHenTra) {
        this.idPM = idPM;
        this.maBanSao = maBanSao;
        this.idBD = idBD;
        this.hoTen = hoTen;
        this.sdt = sdt;
        this.ngayMuon = ngayMuon;
        this.ngayHenTra = ngayHenTra;
    }

    // Getters and Setters
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

    public int getIdBD() {
        return idBD;
    }

    public void setIdBD(int idBD) {
        this.idBD = idBD;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public LocalDate getNgayMuon() {
        return ngayMuon;
    }

    public void setNgayMuon(LocalDate ngayMuon) {
        this.ngayMuon = ngayMuon;
    }

    public LocalDate getNgayHenTra() {
        return ngayHenTra;
    }

    public void setNgayHenTra(LocalDate ngayHenTra) {
        this.ngayHenTra = ngayHenTra;
    }

    @Override
    public String toString() {
        return "ChiTietPhieuMuonInfo{" +
                "idPM=" + idPM +
                ", maBanSao=" + maBanSao +
                ", idBD=" + idBD +
                ", hoTen='" + hoTen + '\'' +
                ", sdt='" + sdt + '\'' +
                ", ngayMuon=" + ngayMuon +
                ", ngayHenTra=" + ngayHenTra +
                '}';
    }
}
