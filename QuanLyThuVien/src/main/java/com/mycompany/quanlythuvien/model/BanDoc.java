package com.mycompany.quanlythuvien.model;

import java.util.Objects;

/**
 *
 * @author Tien
 */
public class BanDoc {

    private int idBD;
    private String hoTen;
    private String email;
    private String diaChi;
    private String sdt;

    public BanDoc() {
    }

    public BanDoc(int idBD, String hoTen, String email, String diaChi, String sdt) {
        this.idBD = idBD;
        this.hoTen = hoTen;
        this.email = email;
        this.diaChi = diaChi;
        this.sdt = sdt;
    }
    public BanDoc(String hoTen, String email, String diaChi, String sdt) {
        this.hoTen = hoTen;
        this.email = email;
        this.diaChi = diaChi;
        this.sdt = sdt;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BanDoc other = (BanDoc) obj;
        if (this.idBD != other.idBD) {
            return false;
        }
        if (!Objects.equals(this.hoTen, other.hoTen)) {
            return false;
        }
        if (!Objects.equals(this.email, other.email)) {
            return false;
        }
        if (!Objects.equals(this.diaChi, other.diaChi)) {
            return false;
        }
        return Objects.equals(this.sdt, other.sdt);
    }



    

}
