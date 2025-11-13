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
//            dsBanDoc
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
