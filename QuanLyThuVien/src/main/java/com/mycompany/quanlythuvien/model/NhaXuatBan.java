package com.mycompany.quanlythuvien.model;

/**
 *
 * @author Tien
 */
public class NhaXuatBan {

    private int maNXB;
    private String tenNXB;

    public NhaXuatBan() {
    }

    public NhaXuatBan(int MaNXB, String tenNXB) {
        this.maNXB = MaNXB;
        this.tenNXB = tenNXB;
    }

    public int getMaNXB() {
        return maNXB;
    }

    public void setMaNXB(int maNXB) {
        this.maNXB = maNXB;
    }

    public String getTenNXB() {
        return tenNXB;
    }

    public void setTenNXB(String tenNXB) {
        this.tenNXB = tenNXB;
    }

    @Override
    public String toString() {
        return tenNXB != null ? tenNXB : "(Không có tên)";
    }
}
