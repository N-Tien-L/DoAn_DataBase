package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.ChiTietPhieuMuonDAO;
import com.mycompany.quanlythuvien.dao.PhieuMuonDAO;

/*
Simple borrowing policy checks (configurable limits).
*/
public class BorrowingPolicyService {
    private final ChiTietPhieuMuonDAO ctDao = new ChiTietPhieuMuonDAO();
    private final PhieuMuonDAO pmDao = new PhieuMuonDAO();

    /*
     Determine whether a reader can borrow new items based on current borrowed count and overdue count.
     @param idBD reader id
     @param maxTotalBorrow maximum total simultaneous borrowed copies allowed
     @param maxOverdue maximum allowed overdue count
    */
    public boolean canBorrow(int idBD, int maxTotalBorrow, int maxOverdue) {
        try {
            if (idBD <= 0) return false;
            // count currently borrowed copies
            // find all CT_PM for this reader with NgayTraThucTe IS NULL
            java.util.List<com.mycompany.quanlythuvien.model.ChiTietPhieuMuon> unreturned = ctDao.findByBandocAndStatus(idBD, false);
            int currentlyBorrowed = unreturned.size();
            if (currentlyBorrowed >= maxTotalBorrow) return false;

            int overdue = ctDao.countOverdueByBanDoc(idBD);
            if (overdue > maxOverdue) return false;

            // also if has unpaid fines (optional) could check via PhatDAO
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
