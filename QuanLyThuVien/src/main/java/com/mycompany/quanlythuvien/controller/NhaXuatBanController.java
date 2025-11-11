package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.NhaXuatBanDAO;
import com.mycompany.quanlythuvien.model.NhaXuatBan;
import java.util.List;

/**
 *
 * @author Tien
 */
public class NhaXuatBanController {
    private final NhaXuatBanDAO nxbDAO = new NhaXuatBanDAO();
    
    public List<NhaXuatBan> getAllNXB(){
        try {
            return nxbDAO.getAll();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean themNXB(NhaXuatBan nxb) {
        try {
            return nxbDAO.insert(nxb);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean capNhatNXB(NhaXuatBan nxb) {
        try {
            return nxbDAO.update(nxb);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean xoaNXB(int maNXB) {
        try {
            return nxbDAO.delete(maNXB);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public NhaXuatBan timNXBTheoId(int id) {
        try {
            return nxbDAO.findById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
