/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package com.mycompany.quanlythuvien.view.dialog;

import com.mycompany.quanlythuvien.controller.BanSaoController;
import com.mycompany.quanlythuvien.model.BanSao;
import com.mycompany.quanlythuvien.model.TaiKhoan;
import java.awt.Color;
import java.awt.Dimension;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.JOptionPane;

/**
 *
 * @author ASUS
 */
public class ChiTietBanSaoDialog extends javax.swing.JDialog {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ChiTietBanSaoDialog.class.getName());
    private final String DATE_FORMAT = "dd/MM/yyyy";
    private final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm";
    private String isbn;
    private BanSao bansao; //null -> them moi
    private BanSaoController controller = new BanSaoController();
    private TaiKhoan currentUser;
    /**
     * Creates new form ChiTietBanSaoDialog
     */
    public ChiTietBanSaoDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setSize(650, 450);
        setMinimumSize(new Dimension(650, 450));
        setLocationRelativeTo(null);
    }

    public ChiTietBanSaoDialog(java.awt.Dialog parent, boolean modal, String isbn, BanSao bansao, TaiKhoan currentUser){
        super(parent, modal);
        initComponents();
        setSize(650, 450);
        setMinimumSize(new Dimension(650, 450));
        setLocationRelativeTo(null);
        this.isbn = isbn;
        this.bansao = bansao;
        this.currentUser = currentUser;
        
        txtMaBanSao.setEnabled(false);
        txtMaBanSao.setDisabledTextColor(Color.BLACK);
        txtISBN.setEnabled(false);
        txtISBN.setText(isbn);
        txtISBN.setDisabledTextColor(Color.BLACK);
        txtNgayNhapKho.setEnabled(false);
        txtNgayNhapKho.setDisabledTextColor(Color.BLACK);
        
        txtCreatedAt.setEnabled(false);
        txtCreatedAt.setDisabledTextColor(Color.BLACK);

        txtCreatedBy.setEnabled(false);
        txtCreatedBy.setDisabledTextColor(Color.BLACK);

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATE_FORMAT);
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        if (bansao != null) {
            txtMaBanSao.setText(String.valueOf(bansao.getMaBanSao()));
            txtSoThuTuTrongKho.setText(String.valueOf(bansao.getSoThuTuTrongKho()));
            // Validate và set TinhTrang
            String tinhTrang = bansao.getTinhTrang();
            if (tinhTrang == null || tinhTrang.trim().isEmpty()) {
                txtTinhTrang.setText("Tốt"); // Giá trị mặc định
            } else if (tinhTrang.equals("Tốt") || tinhTrang.equals("Cũ") || 
                       tinhTrang.equals("Rất Cũ") || tinhTrang.equals("Hỏng")) {
                txtTinhTrang.setText(tinhTrang);
            } else {
                txtTinhTrang.setText("Tốt"); // Fallback nếu giá trị không hợp lệ
            }
            txtViTriLuuTru.setText(bansao.getViTriLuuTru());
            txtCreatedBy.setText(bansao.getCreatedBy());

            if (bansao.getNgayNhapKho() != null) {
                txtNgayNhapKho.setText(bansao.getNgayNhapKho().format(dateFormat));
            } else {
                txtNgayNhapKho.setText("");
            }
            
            // Format CreatedAt
            if (bansao.getCreatedAt() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                txtCreatedAt.setText(dateTimeFormat.format(bansao.getCreatedAt()));
            } else {
                txtCreatedAt.setText("");
            }
        } else {
            txtMaBanSao.setText("Auto");
            
            txtNgayNhapKho.setText(LocalDate.now().format(dateFormat));
            txtNgayNhapKho.setEnabled(false);
            
            // Set giá trị mặc định cho TinhTrang khi tạo mới
            txtTinhTrang.setText("Tốt");
            
            txtCreatedBy.setText(currentUser.getEmail());
            txtCreatedAt.setText("");
        }
    }
    
    public void setViewMode() {
        txtSoThuTuTrongKho.setEditable(false);
        txtTinhTrang.setEditable(false);
        txtNgayNhapKho.setEditable(false);
        txtViTriLuuTru.setEditable(false);  
        
        txtMaBanSao.setEditable(false);
        txtISBN.setEditable(false);
        txtCreatedAt.setEnabled(false);
        txtCreatedBy.setEnabled(false);
        
        btnLuu.setVisible(false);
        btnHuy.setText("Đóng");
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
        btnLuu = new javax.swing.JButton();
        btnHuy = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        lblMaBanSao = new javax.swing.JLabel();
        txtMaBanSao = new javax.swing.JTextField();
        lblISBN = new javax.swing.JLabel();
        txtISBN = new javax.swing.JTextField();
        lblSoThuTuTrongKho = new javax.swing.JLabel();
        txtSoThuTuTrongKho = new javax.swing.JTextField();
        lblTinhTrang = new javax.swing.JLabel();
        txtTinhTrang = new javax.swing.JTextField();
        lblNgayNhapKho = new javax.swing.JLabel();
        txtNgayNhapKho = new javax.swing.JTextField();
        lblViTriLuuTru = new javax.swing.JLabel();
        txtViTriLuuTru = new javax.swing.JTextField();
        lblCreatedBy = new javax.swing.JLabel();
        txtCreatedBy = new javax.swing.JTextField();
        lblCreatedAt = new javax.swing.JLabel();
        txtCreatedAt = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        lblTieuDe.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTieuDe.setText("Chi tiết bản sao");
        jPanel1.add(lblTieuDe);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

        btnLuu.setFont(new java.awt.Font("Arial Unicode MS", 0, 14)); // NOI18N
        btnLuu.setText("[✓] Lưu");
        btnLuu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLuuActionPerformed(evt);
            }
        });
        jPanel3.add(btnLuu);

        btnHuy.setFont(new java.awt.Font("Arial Unicode MS", 0, 14)); // NOI18N
        btnHuy.setText("[×] Hủy");
        btnHuy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHuyActionPerformed(evt);
            }
        });
        jPanel3.add(btnHuy);

        getContentPane().add(jPanel3, java.awt.BorderLayout.PAGE_END);

        lblMaBanSao.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblMaBanSao.setText("Mã bản sao:");

        txtMaBanSao.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        lblISBN.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblISBN.setText("ISBN:");

        txtISBN.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        lblSoThuTuTrongKho.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblSoThuTuTrongKho.setText("Số thứ tự trong kho:");

        txtSoThuTuTrongKho.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        lblTinhTrang.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTinhTrang.setText("Tình trạng:");

        txtTinhTrang.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        lblNgayNhapKho.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblNgayNhapKho.setText("Ngày nhập kho:");

        txtNgayNhapKho.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        lblViTriLuuTru.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblViTriLuuTru.setText("Vị trí lưu trữ:");

        txtViTriLuuTru.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        lblCreatedBy.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblCreatedBy.setText("Người tạo:");

        txtCreatedBy.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        lblCreatedAt.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblCreatedAt.setText("Ngày tạo:");

        txtCreatedAt.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblISBN, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblMaBanSao, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblCreatedBy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblViTriLuuTru, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblNgayNhapKho, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                            .addComponent(lblTinhTrang, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblCreatedAt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(lblSoThuTuTrongKho, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(txtISBN, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtSoThuTuTrongKho, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtTinhTrang, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtNgayNhapKho, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(97, 97, 97))
                        .addComponent(txtMaBanSao, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtViTriLuuTru, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCreatedBy, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCreatedAt, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(139, 139, 139)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTinhTrang, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTinhTrang, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNgayNhapKho, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNgayNhapKho, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblViTriLuuTru, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtViTriLuuTru, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCreatedBy, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCreatedBy, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCreatedAt, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCreatedAt, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMaBanSao, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMaBanSao, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtISBN, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblISBN, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSoThuTuTrongKho, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSoThuTuTrongKho, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(251, 251, 251))
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnLuuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLuuActionPerformed
        // TODO add your handling code here:
        try {
            String tinhTrang = txtTinhTrang.getText().trim();
            
            // Validate TinhTrang
            if (!tinhTrang.equals("Tốt") && !tinhTrang.equals("Cũ") && 
                !tinhTrang.equals("Rất Cũ") && !tinhTrang.equals("Hỏng")) {
                JOptionPane.showMessageDialog(this, 
                    "Tình trạng phải là một trong các giá trị: Tốt, Cũ, Rất Cũ, Hỏng", 
                    "Lỗi Validation", 
                    JOptionPane.ERROR_MESSAGE);
                txtTinhTrang.requestFocus();
                return;
            }
            
            String viTri = txtViTriLuuTru.getText().trim();
            int soThuTu = Integer.parseInt(txtSoThuTuTrongKho.getText().trim());
            String createdBy = (bansao != null) ? bansao.getCreatedBy() : currentUser.getEmail();

            BanSao b = new BanSao(
                    (bansao != null) ? bansao.getMaBanSao() : 0,
                    isbn,
                    soThuTu,
                    tinhTrang,
                    true, // lendable - sẽ được tính lại bởi trigger
                    null,
                    viTri,
                    null,
                    createdBy
            );
            
            controller.save(b, currentUser.getEmail());
            
            BanSao inserted = controller.findById(b.getMaBanSao());
            this.bansao = inserted;
            b.setCreatedAt(inserted.getCreatedAt());
            b.setNgayNhapKho(inserted.getNgayNhapKho());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            txtMaBanSao.setText(String.valueOf(b.getMaBanSao()));
            txtCreatedBy.setText(b.getCreatedBy());
            txtCreatedAt.setText(sdf.format(b.getCreatedAt()));
            txtNgayNhapKho.setText(inserted.getNgayNhapKho().format(dateFormat));

            JOptionPane.showMessageDialog(this, "Lưu thành công");
            dispose();
        } catch (NumberFormatException nfEx) {
            JOptionPane.showMessageDialog(this, "Số thứ tự trong kho phải là số nguyên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException dtEx) {
            JOptionPane.showMessageDialog(this, "Ngày nhập kho không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi lưu: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnLuuActionPerformed

    private void btnHuyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHuyActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_btnHuyActionPerformed

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
                ChiTietBanSaoDialog dialog = new ChiTietBanSaoDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btnHuy;
    private javax.swing.JButton btnLuu;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel lblCreatedAt;
    private javax.swing.JLabel lblCreatedBy;
    private javax.swing.JLabel lblISBN;
    private javax.swing.JLabel lblMaBanSao;
    private javax.swing.JLabel lblNgayNhapKho;
    private javax.swing.JLabel lblSoThuTuTrongKho;
    private javax.swing.JLabel lblTieuDe;
    private javax.swing.JLabel lblTinhTrang;
    private javax.swing.JLabel lblViTriLuuTru;
    private javax.swing.JTextField txtCreatedAt;
    private javax.swing.JTextField txtCreatedBy;
    private javax.swing.JTextField txtISBN;
    private javax.swing.JTextField txtMaBanSao;
    private javax.swing.JTextField txtNgayNhapKho;
    private javax.swing.JTextField txtSoThuTuTrongKho;
    private javax.swing.JTextField txtTinhTrang;
    private javax.swing.JTextField txtViTriLuuTru;
    // End of variables declaration//GEN-END:variables
}
