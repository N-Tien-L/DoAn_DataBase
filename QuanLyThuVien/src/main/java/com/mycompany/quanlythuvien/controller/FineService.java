package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.ChiTietPhieuMuonDAO;
import com.mycompany.quanlythuvien.dao.PhatDAO;
import com.mycompany.quanlythuvien.model.ChiTietPhieuMuon;
import com.mycompany.quanlythuvien.model.Phat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/*
Service to generate fines for overdue items.
*/
public class FineService {
    private final ChiTietPhieuMuonDAO ctDao = new ChiTietPhieuMuonDAO();
    private final PhatDAO phatDAO = new PhatDAO();

    /*
    Generate fines for all overdue CT_PM entries. Rate is money-per-day (e.g. 5000 per day).
    Returns number of Phat records created.
    */
    public int generateFinesForAllOverdue(BigDecimal ratePerDay) {
        try {
            List<ChiTietPhieuMuon> overdue = ctDao.getOverDue();
            int created = 0;
            for (ChiTietPhieuMuon c : overdue) {
                int daysLate = (int) ctDao.getDaysLate(c.getIdPM(), c.getMaBanSao());
                if (daysLate <= 0) continue;

                BigDecimal amount = ratePerDay.multiply(BigDecimal.valueOf(daysLate));
                Phat p = new Phat();
                p.setIdPM(c.getIdPM());
                p.setMaBanSao(c.getMaBanSao());
                p.setLoaiPhat("Tre han");
                p.setSoTien(amount);
                p.setNgayGhiNhan(LocalDate.now());
                p.setTrangThai("Chua dong");

                boolean ok = phatDAO.createPhat(p);
                if (ok) created++;
            }
            return created;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /*
     Generate fines for a specific reader.
    */
    public int generateFinesForBanDoc(int idBD, BigDecimal ratePerDay) {
        try {
            List<com.mycompany.quanlythuvien.model.ChiTietPhieuMuon> list = new java.util.ArrayList<>();
            // find all overdue CT_PM for this reader
            List<com.mycompany.quanlythuvien.model.ChiTietPhieuMuon> allOverdue = ctDao.getOverDue();
            for (com.mycompany.quanlythuvien.model.ChiTietPhieuMuon c : allOverdue) {
                // need to check owner via PHIEUMUON
                com.mycompany.quanlythuvien.dao.PhieuMuonDAO pmDao = new com.mycompany.quanlythuvien.dao.PhieuMuonDAO();
                com.mycompany.quanlythuvien.model.PhieuMuon pm = pmDao.findById(c.getIdPM());
                if (pm != null && pm.getIdBD() == idBD) list.add(c);
            }

            int created = 0;
            for (ChiTietPhieuMuon c : list) {
                int daysLate = (int) ctDao.getDaysLate(c.getIdPM(), c.getMaBanSao());
                if (daysLate <= 0) continue;
                BigDecimal amount = ratePerDay.multiply(BigDecimal.valueOf(daysLate));
                Phat p = new Phat();
                p.setIdPM(c.getIdPM());
                p.setMaBanSao(c.getMaBanSao());
                p.setLoaiPhat("Tre han");
                p.setSoTien(amount);
                p.setNgayGhiNhan(LocalDate.now());
                p.setTrangThai("Chua dong");

                boolean ok = phatDAO.createPhat(p);
                if (ok) created++;
            }
            return created;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
