/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.mycompany.quanlythuvien.view.dialog;

import com.mycompany.quanlythuvien.view.dialog.ChiTietBanSaoDialog;
import com.mycompany.quanlythuvien.view.dialog.ThemHangLoatBanSaoDialog;
import com.mycompany.quanlythuvien.controller.BanSaoController;
import com.mycompany.quanlythuvien.model.BanSao;
import com.mycompany.quanlythuvien.model.TaiKhoan;
import java.util.List;
import java.util.Stack;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ASUS
 */
public class DanhSachBanSaoDialog extends javax.swing.JDialog {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DanhSachBanSaoDialog.class.getName());
    private String isbn; //id cua sach goc
    private BanSaoController banSaoController = new BanSaoController();
    
    private Integer currentCursor = null;
    private Integer lastMaBanSao = null;
    private int pageSize = 10;
    private Stack<Integer> cursorHistory = new Stack<>();
    private boolean hasNextPage = false;
    
    private boolean isSearching = false;
    private String searchKeyword = "";
    private String searchKeywordTo = "";
    private String searchTieuChi = "";
    private Integer lastSearchCursor = null;
    
    private TaiKhoan currentUser;
    /**
     * Creates new form DanhSachBanSaoDialog
     */
    public DanhSachBanSaoDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initBase();
    }

    public DanhSachBanSaoDialog(java.awt.Frame parent, boolean modal, String isbn, TaiKhoan currentUser) {
        this(parent, modal);
        this.isbn = isbn;
        this.currentUser = currentUser;
        lblTieuDe.setText("Danh sách bản sao của sách: " + isbn);
        resetPaginationAndLoadBanSao();
    }

    private void initTableBanSao() {
        DefaultTableModel model = new DefaultTableModel(new Object[][] {}, new String[] {
            "Mã Bản Sao", "Số Thứ Tự", "Tình Trạng", "Ngày Nhập", "Vị Trí Lưu Trữ"
        }) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblBanSao.setModel(model);
    }

    private void initBase() {
        initComponents();
        initTableBanSao();
        initComboBox();
        setLocationRelativeTo(null);
        txtTimKiemBSEnd.setVisible(false);
    }
    private void initComboBox(){
        cboTieuChiBS.removeAllItems();
        cboTieuChiBS.addItem("Mã bản sao");
        cboTieuChiBS.addItem("Số thứ tự");
        cboTieuChiBS.addItem("Tình trạng");
        cboTieuChiBS.addItem("Ngày nhập");
        cboTieuChiBS.addItem("Vị trí lưu trữ");
        cboTieuChiBS.setSelectedIndex(0);
        
        cboTieuChiBS.addActionListener(e -> {
            String selected = cboTieuChiBS.getSelectedItem() != null ? cboTieuChiBS.getSelectedItem().toString() : "";
            boolean isDateSearch = "Ngày nhập".equals(selected);
            
            txtTimKiemBSEnd.setVisible(isDateSearch);
            lblTimKiemBS.setText(isDateSearch ? "[⌕] Từ ngày - Đến ngày" : "[⌕] Tìm kiếm bản sao");
            
            jPanel6.revalidate();
            jPanel6.repaint();
        });
    }
    
    private void loadBanSaoPage() {
        try {
            List<BanSao> list = banSaoController.getPage(isbn, pageSize + 1, currentCursor);
            hasNextPage = list.size() > pageSize;
            if (hasNextPage) list.remove(list.size() - 1);
            
            DefaultTableModel model = (DefaultTableModel) tblBanSao.getModel();
            model.setRowCount(0);
            
            for (BanSao b : list) {
                String ngayNhapStr = b.getNgayNhapKho() != null ? b.getNgayNhapKho().toString() : "";
                model.addRow(new Object[]{
                        b.getMaBanSao(),
                        b.getSoThuTuTrongKho(),
                        b.getTinhTrang(),
                        ngayNhapStr,
                        b.getViTriLuuTru()     
                });
            }
            
            if (!list.isEmpty()) {
                lastMaBanSao = list.get(list.size() - 1).getMaBanSao();
            }
            
            btnBSSau.setEnabled(hasNextPage);
            btnBSTruoc.setEnabled(!cursorHistory.isEmpty());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách bản sao: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadSearchPage(){
        List<BanSao> list = banSaoController.searchBanSao(isbn, searchTieuChi, searchKeyword, searchKeywordTo, lastSearchCursor, pageSize + 1);
        hasNextPage = list.size() > pageSize;
        if (hasNextPage) {
            list.remove(list.size() - 1);
        }
        
        if (!list.isEmpty()) {
            lastSearchCursor = list.get(list.size() - 1).getMaBanSao();
        } else {
            lastSearchCursor = null;
        }
        
        loadDataToTable(list);
        
        btnBSTruoc.setEnabled(!cursorHistory.isEmpty());
        btnBSSau.setEnabled(hasNextPage);
        
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy bản sao nào!");
        }
    }
    
    private void loadDataToTable(List<BanSao> list) {
        DefaultTableModel model = (DefaultTableModel) tblBanSao.getModel();
        model.setRowCount(0);
        for (BanSao b : list) {
            String ngayNhapStr = b.getNgayNhapKho() != null ? b.getNgayNhapKho().toString() : "";
            model.addRow(new Object[]{
                b.getMaBanSao(),
                b.getSoThuTuTrongKho(),
                b.getTinhTrang(),
                ngayNhapStr,
                b.getViTriLuuTru()
            });
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblTieuDe = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        lblTimKiemBS = new javax.swing.JLabel();
        cboTieuChiBS = new javax.swing.JComboBox<>();
        txtTimKiemBS = new javax.swing.JTextField();
        txtTimKiemBSEnd = new javax.swing.JTextField();
        btnTimBS = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        btnBSTruoc = new javax.swing.JButton();
        btnBSSau = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBanSao = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        btnThem1BS = new javax.swing.JButton();
        btnThemHangLoat = new javax.swing.JButton();
        btnSua = new javax.swing.JButton();
        btnXoa = new javax.swing.JButton();
        btnLamMoi = new javax.swing.JButton();
        btnChiTiet = new javax.swing.JButton();
        btnDong = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        lblTieuDe.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTieuDe.setText("Danh sách bản sao của sách:");
        jPanel1.add(lblTieuDe);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

        jPanel3.setLayout(new java.awt.BorderLayout());

        lblTimKiemBS.setFont(new java.awt.Font("Arial Unicode MS", 0, 14)); // NOI18N
        lblTimKiemBS.setText("[⌕] Tìm kiếm bản sao");
        jPanel6.add(lblTimKiemBS);

        cboTieuChiBS.setFont(new java.awt.Font("Arial Unicode MS", 0, 14)); // NOI18N
        cboTieuChiBS.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel6.add(cboTieuChiBS);

        txtTimKiemBS.setMinimumSize(new java.awt.Dimension(64, 30));
        txtTimKiemBS.setPreferredSize(new java.awt.Dimension(120, 30));
        txtTimKiemBS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTimKiemBSActionPerformed(evt);
            }
        });
        jPanel6.add(txtTimKiemBS);

        txtTimKiemBSEnd.setMinimumSize(new java.awt.Dimension(64, 30));
        txtTimKiemBSEnd.setPreferredSize(new java.awt.Dimension(120, 30));
        jPanel6.add(txtTimKiemBSEnd);

        btnTimBS.setText("Tìm");
        btnTimBS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTimBSActionPerformed(evt);
            }
        });
        jPanel6.add(btnTimBS);

        jPanel3.add(jPanel6, java.awt.BorderLayout.PAGE_START);

        btnBSTruoc.setFont(new java.awt.Font("Arial Unicode MS", 0, 14)); // NOI18N
        btnBSTruoc.setText("◄ Trước");
        btnBSTruoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBSTruocActionPerformed(evt);
            }
        });
        jPanel5.add(btnBSTruoc);

        btnBSSau.setFont(new java.awt.Font("Arial Unicode MS", 0, 14)); // NOI18N
        btnBSSau.setText("Sau ►");
        btnBSSau.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBSSauActionPerformed(evt);
            }
        });
        jPanel5.add(btnBSSau);

        jPanel7.add(jPanel5);

        jPanel3.add(jPanel7, java.awt.BorderLayout.PAGE_END);

        jPanel8.setLayout(new javax.swing.BoxLayout(jPanel8, javax.swing.BoxLayout.LINE_AXIS));

        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.LINE_AXIS));

        tblBanSao.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblBanSao.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Mã Bản Sao", "Số Thứ Tự", "Tình Trạng", "Ngày Nhập", "Vị Trí Lưu Trữ"
            }
        ));
        jScrollPane1.setViewportView(tblBanSao);

        jPanel4.add(jScrollPane1);

        jPanel8.add(jPanel4);

        jPanel3.add(jPanel8, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        btnThem1BS.setFont(new java.awt.Font("Arial Unicode MS", 0, 14)); // NOI18N
        btnThem1BS.setText("[+] Thêm 1 bản sao");
        btnThem1BS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThem1BSActionPerformed(evt);
            }
        });
        jPanel2.add(btnThem1BS);

        btnThemHangLoat.setFont(new java.awt.Font("Arial Unicode MS", 0, 14)); // NOI18N
        btnThemHangLoat.setText("[+] Thêm hàng loạt");
        btnThemHangLoat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThemHangLoatActionPerformed(evt);
            }
        });
        jPanel2.add(btnThemHangLoat);

        btnSua.setFont(new java.awt.Font("Arial Unicode MS", 0, 14)); // NOI18N
        btnSua.setText("[✎] Sửa");
        btnSua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSuaActionPerformed(evt);
            }
        });
        jPanel2.add(btnSua);

        btnXoa.setFont(new java.awt.Font("Arial Unicode MS", 0, 14)); // NOI18N
        btnXoa.setText("[-] Xóa");
        btnXoa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnXoaActionPerformed(evt);
            }
        });
        jPanel2.add(btnXoa);

        btnLamMoi.setFont(new java.awt.Font("Arial Unicode MS", 0, 14)); // NOI18N
        btnLamMoi.setText("[↻] Làm mới");
        btnLamMoi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLamMoiActionPerformed(evt);
            }
        });
        jPanel2.add(btnLamMoi);

        btnChiTiet.setFont(new java.awt.Font("Arial Unicode MS", 0, 14)); // NOI18N
        btnChiTiet.setText("[i] Xem chi tiết");
        btnChiTiet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChiTietActionPerformed(evt);
            }
        });
        jPanel2.add(btnChiTiet);

        btnDong.setFont(new java.awt.Font("Arial Unicode MS", 0, 14)); // NOI18N
        btnDong.setText("[×] Đóng");
        btnDong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDongActionPerformed(evt);
            }
        });
        jPanel2.add(btnDong);

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnThem1BSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThem1BSActionPerformed
        // TODO add your handling code here:
        new ChiTietBanSaoDialog(this, true, isbn, null, currentUser).setVisible(true);
        resetPaginationAndLoadBanSao();
    }//GEN-LAST:event_btnThem1BSActionPerformed

    private void btnSuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuaActionPerformed
        // TODO add your handling code here:
        int row = tblBanSao.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 bản sao để sửa!");
            return;
        }
        
        int maBanSao = (int) tblBanSao.getValueAt(row, 0);
        try {
            BanSao b = banSaoController.findById(maBanSao);
            new ChiTietBanSaoDialog(this, true, isbn, b, currentUser).setVisible(true);
            resetPaginationAndLoadBanSao();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải bản sao: " + e.getMessage());
        }
    }//GEN-LAST:event_btnSuaActionPerformed

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaActionPerformed
        // TODO add your handling code here:
        int row = tblBanSao.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 bản sao để xóa!");
            return;
        }
        int maBanSao = (int) tblBanSao.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Xóa bản sao này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                banSaoController.delete(maBanSao);
                resetPaginationAndLoadBanSao();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnXoaActionPerformed

    private void btnDongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDongActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_btnDongActionPerformed

    
    private void btnBSTruocActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBSTruocActionPerformed
        // TODO add your handling code here:
        if (!cursorHistory.isEmpty()) {
            if (isSearching) {
                lastSearchCursor = cursorHistory.pop();
                loadSearchPage();
            } else {
                currentCursor = cursorHistory.pop();
                loadBanSaoPage();
            }
        }
    }//GEN-LAST:event_btnBSTruocActionPerformed

    private void resetPaginationAndLoadBanSao() {
        isSearching = false; 
        searchKeyword = "";
        searchTieuChi = "";
        lastSearchCursor = null;
        
        cursorHistory.clear();
        currentCursor = null;
        lastMaBanSao = null;
        hasNextPage = false;
        loadBanSaoPage();
    }
    
    private void btnBSSauActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBSSauActionPerformed
        // TODO add your handling code here:
        if (hasNextPage) {
            if (isSearching) {
                cursorHistory.push(lastSearchCursor);
                loadSearchPage();
            } else {
                cursorHistory.push(currentCursor);
                currentCursor = lastMaBanSao;
                loadBanSaoPage();
            }
        }
    }//GEN-LAST:event_btnBSSauActionPerformed

    private void btnChiTietActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChiTietActionPerformed
        // TODO add your handling code here:
        int r = tblBanSao.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bản sao để xem!");
            return;
        }
        
        try {
            int modelRow = tblBanSao.convertRowIndexToModel(r);
            int maBanSao = (int) tblBanSao.getModel().getValueAt(modelRow, 0);
            BanSao b = banSaoController.findById(maBanSao);
            
            if (b != null) {
                ChiTietBanSaoDialog dialog = new ChiTietBanSaoDialog(this, true, b.getISBN(), b, currentUser);
                dialog.setViewMode();
                dialog.setVisible(true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải chi tiết: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnChiTietActionPerformed

    private void btnTimBSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTimBSActionPerformed
        // TODO add your handling code here:
        try {
            String keyword = txtTimKiemBS.getText().trim();
            String tieuChi = cboTieuChiBS.getSelectedItem().toString();
            String keywordTo = txtTimKiemBSEnd.isVisible() ? txtTimKiemBSEnd.getText().trim() : "";
            
            if ("Ngày nhập".equals(tieuChi)) {
                if (keyword.isEmpty() && keywordTo.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ khoảng ngày (Từ ngày và Đến ngày) theo định dạng YYYY-MM-DD!",
                            "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
                    txtTimKiemBS.requestFocus();
                    return;
                }                

                String DATE_REGEX = "\\d{4}-\\d{2}-\\d{2}";
                boolean isDateFromValid = keyword.matches(DATE_REGEX);
                boolean isDateToValid = keywordTo.matches(DATE_REGEX);
                
                if (!isDateFromValid || !isDateToValid) {
                    JOptionPane.showMessageDialog(this, "Ngày nhập không đúng định dạng. Vui lòng sử dụng định dạng YYYY-MM-DD (ví dụ: 2025-11-19).",
                            "Lỗi định dạng ngày", JOptionPane.ERROR_MESSAGE);
                    return; 
                }    
            } 
            else if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập từ khóa để tìm kiếm!",
                        "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
                txtTimKiemBS.requestFocus();
                return;
            }
            

            
            //Reset trạng thái tìm kiếm
            isSearching = true;
            searchTieuChi = tieuChi;
            
            cursorHistory.clear();
            lastSearchCursor = null;
            currentCursor = null;
            
            searchKeyword = keyword;
            searchKeywordTo = keywordTo;
            
            loadSearchPage();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnTimBSActionPerformed

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoiActionPerformed
        // TODO add your handling code here:
        txtTimKiemBS.setText("");
        txtTimKiemBSEnd.setText("");
        cboTieuChiBS.setSelectedIndex(0);
        resetPaginationAndLoadBanSao();
    }//GEN-LAST:event_btnLamMoiActionPerformed

    private void btnThemHangLoatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemHangLoatActionPerformed
        // TODO add your handling code here:
        new ThemHangLoatBanSaoDialog(this, true, this.isbn, this.currentUser).setVisible(true);
        resetPaginationAndLoadBanSao();
    }//GEN-LAST:event_btnThemHangLoatActionPerformed

    private void txtTimKiemBSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTimKiemBSActionPerformed
        // TODO add your handling code here:
        btnTimBSActionPerformed(evt);
    }//GEN-LAST:event_txtTimKiemBSActionPerformed

    /**
     * @param args the command line arguments
     */
    

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                DanhSachBanSaoDialog dialog = new DanhSachBanSaoDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBSSau;
    private javax.swing.JButton btnBSTruoc;
    private javax.swing.JButton btnChiTiet;
    private javax.swing.JButton btnDong;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnSua;
    private javax.swing.JButton btnThem1BS;
    private javax.swing.JButton btnThemHangLoat;
    private javax.swing.JButton btnTimBS;
    private javax.swing.JButton btnXoa;
    private javax.swing.JComboBox<String> cboTieuChiBS;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTieuDe;
    private javax.swing.JLabel lblTimKiemBS;
    private javax.swing.JTable tblBanSao;
    private javax.swing.JTextField txtTimKiemBS;
    private javax.swing.JTextField txtTimKiemBSEnd;
    // End of variables declaration//GEN-END:variables
}
