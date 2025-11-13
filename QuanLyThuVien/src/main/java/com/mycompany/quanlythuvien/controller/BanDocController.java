package com.mycompany.quanlythuvien.controller;
import com.mycompany.quanlythuvien.model.BanDoc;
import com.mycompany.quanlythuvien.dao.BanDocDAO;

import java.util.ArrayList;
/**
 *
 * @author Tien
 */
public class BanDocController {
    private ArrayList<BanDoc> dsBanDoc = new ArrayList<BanDoc>();
    private final BanDocDAO dao = new BanDocDAO();
    public BanDocController() {}
    
    public ArrayList<BanDoc> getDsBanDoc() {
        return dsBanDoc;
    }

    public Boolean add(BanDoc cur) throws Exception {
        
        if(dao.addDAO(cur)) {
            this.dsBanDoc.add(cur);
            return true;
        }
        return false;
    }
    public Boolean delete(BanDoc cur) throws Exception {
        if(dao.deleteDAO(cur)) {
            this.dsBanDoc.removeIf(bd -> {
                String h = bd.getHoTen() == null ? "" : bd.getHoTen();
                String e = bd.getEmail() == null ? "" : bd.getEmail();
                String s = bd.getSdt() == null ? "" : bd.getSdt();
                String d = bd.getDiaChi() == null ? "" : bd.getDiaChi();
                return h.equals(cur.getHoTen()) && e.equals(cur.getEmail()) && s.equals(cur.getSdt()) && d.equals(cur.getDiaChi());
            });
            return true;
        }
        return false;
    }
    public Boolean init() throws Exception {
        if(dao.readDAO(dsBanDoc)) {
           return true;
        }
        return false;
    }
}
