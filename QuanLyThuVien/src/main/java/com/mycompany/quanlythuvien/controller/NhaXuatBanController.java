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
    
    public List<NhaXuatBan> getAllNXB(int lastMaNXBCursor, int pageSize){
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
    
    public List<NhaXuatBan> searchNXB(String keyword, String column) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return nxbDAO.getAll(0, getTotalNXB());
        }
        
        return nxbDAO.search(keyword, column);
    }
    
    public Optional<NhaXuatBan> getNXBBYId(int id) {
        if (id <= 0) return Optional.empty();
        return nxbDAO.getById(id);
    }
}
