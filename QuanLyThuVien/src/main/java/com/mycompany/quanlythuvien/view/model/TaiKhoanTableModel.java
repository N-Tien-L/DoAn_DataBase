package com.mycompany.quanlythuvien.view.model;

import com.mycompany.quanlythuvien.model.TaiKhoan;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * TableModel cho JTable hiển thị danh sách tài khoản
 * @author Tien
 */
public class TaiKhoanTableModel extends AbstractTableModel {
    
    private List<TaiKhoan> danhSachTaiKhoan;
    private final String[] columnNames = {"Email", "Họ tên", "Vai trò"};
    
    public TaiKhoanTableModel() {
        this.danhSachTaiKhoan = new ArrayList<>();
    }
    
    public TaiKhoanTableModel(List<TaiKhoan> danhSachTaiKhoan) {
        this.danhSachTaiKhoan = danhSachTaiKhoan != null ? danhSachTaiKhoan : new ArrayList<>();
    }
    
    @Override
    public int getRowCount() {
        return danhSachTaiKhoan.size();
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TaiKhoan taiKhoan = danhSachTaiKhoan.get(rowIndex);
        
        switch (columnIndex) {
            case 0:
                return taiKhoan.getEmail();
            case 1:
                return taiKhoan.getHoTen();
            case 2:
                return taiKhoan.getRole();
            default:
                return null;
        }
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Không cho phép edit trực tiếp trong table
        return false;
    }
    
    /**
     * Lấy tài khoản tại row được chỉ định
     * @param rowIndex Index của row
     * @return TaiKhoan object hoặc null nếu invalid
     */
    public TaiKhoan getTaiKhoanAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < danhSachTaiKhoan.size()) {
            return danhSachTaiKhoan.get(rowIndex);
        }
        return null;
    }
    
    /**
     * Cập nhật toàn bộ danh sách và refresh table
     * @param danhSachTaiKhoan Danh sách tài khoản mới
     */
    public void setData(List<TaiKhoan> danhSachTaiKhoan) {
        this.danhSachTaiKhoan = danhSachTaiKhoan != null ? danhSachTaiKhoan : new ArrayList<>();
        fireTableDataChanged();
    }
    
    /**
     * Thêm một tài khoản vào cuối danh sách
     * @param taiKhoan Tài khoản cần thêm
     */
    public void addTaiKhoan(TaiKhoan taiKhoan) {
        if (taiKhoan != null) {
            danhSachTaiKhoan.add(taiKhoan);
            int newRow = danhSachTaiKhoan.size() - 1;
            fireTableRowsInserted(newRow, newRow);
        }
    }
    
    /**
     * Cập nhật tài khoản tại row được chỉ định
     * @param rowIndex Index của row
     * @param taiKhoan Thông tin tài khoản mới
     */
    public void updateTaiKhoan(int rowIndex, TaiKhoan taiKhoan) {
        if (rowIndex >= 0 && rowIndex < danhSachTaiKhoan.size() && taiKhoan != null) {
            danhSachTaiKhoan.set(rowIndex, taiKhoan);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }
    
    /**
     * Xóa tài khoản tại row được chỉ định
     * @param rowIndex Index của row
     */
    public void removeTaiKhoan(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < danhSachTaiKhoan.size()) {
            danhSachTaiKhoan.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }
    
    /**
     * Xóa toàn bộ dữ liệu
     */
    public void clear() {
        danhSachTaiKhoan.clear();
        fireTableDataChanged();
    }
    
    /**
     * Lấy danh sách tài khoản hiện tại
     * @return List các tài khoản
     */
    public List<TaiKhoan> getAllTaiKhoan() {
        return new ArrayList<>(danhSachTaiKhoan);
    }
    
    /**
     * Kiểm tra danh sách có rỗng không
     * @return true nếu rỗng
     */
    public boolean isEmpty() {
        return danhSachTaiKhoan.isEmpty();
    }
}
