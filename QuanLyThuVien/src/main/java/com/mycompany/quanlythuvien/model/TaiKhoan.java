package com.mycompany.quanlythuvien.model;

/**
 *
 * @author Tien
 */
public class TaiKhoan {

    private String email;
    private String password;
    private String hoTen;
    private String role;

    public TaiKhoan() {
    }

    public TaiKhoan(String email, String password, String hoTen, String role) {
        this.email = email;
        this.password = password;
        this.hoTen = hoTen;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
