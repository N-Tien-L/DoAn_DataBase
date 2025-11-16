package com.mycompany.quanlythuvien.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.mycompany.quanlythuvien.dao.LichLamDAO;
import com.mycompany.quanlythuvien.exceptions.LichLamException;
import com.mycompany.quanlythuvien.middleware.AuthMiddleware;
import com.mycompany.quanlythuvien.model.LichLam;
import com.mycompany.quanlythuvien.model.TaiKhoan;

public class LichLamController {

    private final LichLamDAO lichLamDAO = new LichLamDAO();
    
    public boolean createShift(TaiKhoan currentUser, String EmailThuThu, LocalDate Ngay, LocalTime GioBatDau, LocalTime GioKetThuc, String GhiChu) throws Exception {
        
        AuthMiddleware.requireAdmin(currentUser);

        if(EmailThuThu == null || EmailThuThu.isEmpty()) {
            throw new LichLamException("Email thủ thư không được để trống");
        }

        if(Ngay.isBefore(LocalDate.now())) {
           throw new LichLamException("Ngày phải là ngày trong tương lai");
        }

        if(GioKetThuc.isBefore(GioBatDau)) {
            throw new LichLamException("NGiờ kết thúc phải sau giờ bắt đầu");
        }

        Date sqlDate = Date.valueOf(Ngay);

        return lichLamDAO.createShift(EmailThuThu, sqlDate, GioBatDau, GioKetThuc, GhiChu, currentUser.getEmail());
    }

    public List<LichLam> getShiftsByEmailBetween(TaiKhoan currentUser, String EmailThuThu, Date FromDate, Date ToDate) throws Exception {
        
        if(!currentUser.getEmail().equals(EmailThuThu)) {
            AuthMiddleware.requireAdmin(currentUser);
        }

        if(EmailThuThu == null || EmailThuThu.isEmpty()) {
             throw new LichLamException("Email thủ thư không được để trống");
        }

        if(ToDate.before(FromDate)) {
             throw new LichLamException("Ngày kết thúc phải sau hoặc cùng ngày bắt đầu");
        }

        return lichLamDAO.getShiftsByEmailBetween(EmailThuThu, FromDate, ToDate);
    }

    public boolean cancelShift(TaiKhoan currentUser, int idLich, String adminEmail) throws Exception {
        AuthMiddleware.requireAdmin(currentUser);
        return lichLamDAO.cancelShift(idLich);
    }
}
