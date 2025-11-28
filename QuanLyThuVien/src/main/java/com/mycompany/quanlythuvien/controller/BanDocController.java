package com.mycompany.quanlythuvien.controller;
import com.mycompany.quanlythuvien.model.BanDoc;
import com.mycompany.quanlythuvien.dao.BanDocDAO;

import java.util.ArrayList;
import java.util.List;
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
    
    // Thêm / thay thế method getPageById trong BanDocController
    // Controller: đảm bảo bọc wildcard cho "contains" và chuẩn hoá searchBy
    // Thay / thêm method này trong BanDocController
    public List<BanDoc> getPageById(int limit, String searchBy, String searchText, Integer lastId) throws Exception {
        BanDocDAO dao = new BanDocDAO();

        // canonical searchBy: trim, uppercase, no spaces
        String canonical = (searchBy == null) ? null : searchBy.trim().toUpperCase().replaceAll("\\s+", "");

        // normalize searchText: null nếu rỗng, ngược lại trim
        if (searchText != null) {
            searchText = searchText.trim();
            if (searchText.isEmpty()) searchText = null;
        }

        // Với tất cả trường text (HOTEN, EMAIL, SDT, DIACHI) và cả ID bây giờ -> tìm "contains"
        if (searchText != null) {
            // nếu caller chưa bọc wildcard, bọc thành %text% để tìm contains
            if (!searchText.contains("%")) {
                searchText = "%" + searchText + "%";
            }
        }

        // gửi canonical và searchText (đã bọc %...% nếu không null) xuống DAO
        return dao.getPageById(limit, canonical, searchText, lastId);
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
                return bd.getIdBD() == cur.getIdBD();
            });
            return true;
        }
        return false;
    }
    public Boolean update(BanDoc cur) throws Exception {
        if(dao.updateDAO(cur)) {
            this.dsBanDoc.removeIf(bd -> {
                return bd.getIdBD() == cur.getIdBD();
            });
            this.dsBanDoc.add(cur);
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
    
    public BanDoc findByEmail(String email) throws Exception {
        return dao.findByEmail(email);
    }
    public ArrayList<Object> getPhieuMuonPageByBanDoc(int idBD, int pageSizeRequest, Integer lastIdPM, String searchText) throws Exception {
        return (ArrayList<Object>) dao.getPhieuMuonPageByBanDoc(idBD, pageSizeRequest, lastIdPM, searchText);
    }

    public ArrayList<Object> getPhieuPhatPageByBanDoc(int idBD, int pageSizeRequest, Integer lastIdPhat, String searchText) throws Exception {
        return (ArrayList<Object>) dao.getPhieuPhatPageByBanDoc(idBD, pageSizeRequest, lastIdPhat, searchText);
    }

}
