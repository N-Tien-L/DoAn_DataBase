package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.NhaXuatBanDAO;
import com.mycompany.quanlythuvien.model.NhaXuatBan;
import java.util.List;

/**
 *
 * @author Tien
 */
public class NhaXuatBanController {
    private final NhaXuatBanDAO nxbDAO;
    
    public NhaXuatBanController() {
        this.nxbDAO = new NhaXuatBanDAO();
    }
    
    public List<NhaXuatBan> getAllNXBNoPaging() {
        return nxbDAO.findAll();
    }

    public List<NhaXuatBan> getAllNXB(int lastMaNXBCursor, int pageSize){
        if (pageSize < 1 || pageSize > 100) pageSize = 10;
        return nxbDAO.getAll(lastMaNXBCursor, pageSize);
    }
    
    public boolean addNXB(NhaXuatBan nxb) throws Exception {
        if (nxb.getTenNXB() == null || nxb.getTenNXB().trim().isEmpty()) {
            throw new Exception("Tên nhà xuất bản không được để trống!");
        }
        return nxbDAO.insert(nxb);
    }
    
    public boolean updateNXB(NhaXuatBan nxb) throws Exception {
        if (nxb.getMaNXB() <= 0) {
            throw new Exception("Mã nhà xuất bản không hợp lệ!");
        }
        
        if (nxb.getTenNXB() == null || nxb.getTenNXB().trim().isEmpty()){
            throw new Exception("Tên nhà xuất bản không được để trống!");
        }
        return nxbDAO.update(nxb);
    }
    
    public boolean deleteNXB(int maNXB) throws Exception {
        if (maNXB <= 0) {
            throw new Exception("Mã nhà xuất bản không hợp lệ!");
        }
        return nxbDAO.delete(maNXB);
    }
    
    public List<NhaXuatBan> searchNXB(String keyword, String column, Integer lastMaNXBCursor, int pageSize) {
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
        
        return nxbDAO.search(keyword, column, lastMaNXBCursor, pageSize);
    }
}
