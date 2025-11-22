package com.mycompany.quanlythuvien.controller;

import com.mycompany.quanlythuvien.dao.NhaXuatBanDAO;
import com.mycompany.quanlythuvien.model.NhaXuatBan;
import java.util.List;
import java.util.Optional;

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
        if (pageSize < 1 || pageSize > 100) pageSize = 20;
        return nxbDAO.getAll(lastMaNXBCursor, pageSize);
    }
    public int getTotalNXB() {
        return nxbDAO.getTotalNXB();
    }
    
    public boolean addNXB(NhaXuatBan nxb) {
        if (nxb.getTenNXB() == null || nxb.getTenNXB().trim().isEmpty()) {
            System.out.println("Tên nhà xuất bản không được để trống!");
            return false;
        }
        return nxbDAO.insert(nxb);
    }
    
    public boolean updateNXB(NhaXuatBan nxb) {
        if (nxb.getMaNXB() <= 0) {
            System.out.println("Mã nhà xuất bản không hợp lệ!");
            return false;
        }
        
        if (nxb.getTenNXB() == null || nxb.getTenNXB().trim().isEmpty()){
            System.out.println("Tên nhà xuất bản không được để trống!");
            return false;
        }
        return nxbDAO.update(nxb);
    }
    
    public boolean deleteNXB(int maNXB) {
        if (maNXB <= 0) {
            System.out.println("Mã nhà xuất bản không hợp lệ!");
            return false;
        }
        return nxbDAO.delete(maNXB);
    }
    
    public List<NhaXuatBan> searchNXB(String keyword, String column, Integer lastMaNXBCursor, int pageSize) {
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 20;
        }
        
        return nxbDAO.search(keyword, column, lastMaNXBCursor, pageSize);
    }
    
    public Optional<NhaXuatBan> getNXBById(int id) {
        if (id <= 0) return Optional.empty();
        return nxbDAO.getById(id);
    }
}
