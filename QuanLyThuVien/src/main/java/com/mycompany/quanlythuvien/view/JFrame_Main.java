package com.mycompany.quanlythuvien.view;

import java.awt.CardLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;

import com.mycompany.quanlythuvien.model.TaiKhoan;
import com.mycompany.quanlythuvien.view.panel.QuanLyBanDocPanel;
import com.mycompany.quanlythuvien.view.panel.QuanLyPhieuMuonPanel;
import com.mycompany.quanlythuvien.view.panel.QuanLySachPanel;
import com.mycompany.quanlythuvien.view.panel.QuanLyTaiKhoanPanel;
import com.mycompany.quanlythuvien.view.panel.ThongKePanel;
import com.mycompany.quanlythuvien.view.panel.WelcomePanel;

/**
 * @author Tien
 */
public class JFrame_Main extends javax.swing.JFrame {

    private TaiKhoan currentUser;
    private CardLayout cardLayout;
    private List<JButton> menuButtons;
    private JButton selectedButton;

    // Định nghĩa các hằng số màu sắc
    private final Color MENU_BACKGROUND_COLOR = new Color(51, 51, 51);
    private final Color BTN_DEFAULT_COLOR = new Color(65, 65, 65);
    private final Color BTN_HOVER_COLOR = new Color(80, 80, 80);
    private final Color BTN_SELECTED_COLOR = new Color(0, 102, 153);
    private final Color CONTENT_BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Color LOGOUT_BTN_COLOR = new Color(255, 102, 102);
    private final Color TEXT_COLOR_LIGHT = new Color(255, 255, 255);

    /**
     * Creates new form JFrame_Main
     */
    public JFrame_Main(TaiKhoan user) {
        initComponents();
        this.currentUser = user;

        applyStyling();
        setupCardLayoutAndUI();
        authorizeRoles();
    }

    /**
     * Áp dụng tất cả styling cho các components.
     */
    private void applyStyling() {
        // Cài đặt Frame
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setLocationRelativeTo(null);

        // Cài đặt Panel Menu (ghi đè layout của NetBeans)
        jPanel_Menu.setPreferredSize(new java.awt.Dimension(320, 0));
        jPanel_Menu.setBackground(MENU_BACKGROUND_COLOR);
        jPanel_Menu.setLayout(new BoxLayout(jPanel_Menu, BoxLayout.Y_AXIS));

        // Label Chào mừng
        jLabel_Welcome.setFont(new java.awt.Font("Segoe UI", 1, 20));
        jLabel_Welcome.setForeground(TEXT_COLOR_LIGHT);
        jLabel_Welcome.setText("Xin chào, " + currentUser.getHoTen() + "!");
        jLabel_Welcome.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        // Style các nút Menu
        styleMenuButton(jButton_QuanLySach, "/icons/32x32/book.png");
        styleMenuButton(jButton_QuanLyBanDoc, "/icons/32x32/readers.png");
        styleMenuButton(jButton_QuanLyPhieuMuon, "/icons/32x32/borrow.png");
        styleMenuButton(jButton_ThongKe, "/icons/32x32/stats.png");
        styleMenuButton(jButton_QuanLyTaiKhoan, "/icons/32x32/admin.png");

        // Căn lề giữa cho các nút
        jButton_QuanLySach.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        jButton_QuanLyBanDoc.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        jButton_QuanLyPhieuMuon.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        jButton_ThongKe.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        jButton_QuanLyTaiKhoan.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        jButton_Logout.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        // Thêm lại các components vào Menu với BoxLayout
        jPanel_Menu.removeAll();
        jPanel_Menu.add(Box.createRigidArea(new java.awt.Dimension(0, 30)));
        jPanel_Menu.add(jLabel_Welcome);
        jPanel_Menu.add(Box.createRigidArea(new java.awt.Dimension(0, 60)));
        jPanel_Menu.add(jButton_QuanLySach);
        jPanel_Menu.add(Box.createRigidArea(new java.awt.Dimension(0, 15)));
        jPanel_Menu.add(jButton_QuanLyBanDoc);
        jPanel_Menu.add(Box.createRigidArea(new java.awt.Dimension(0, 15)));
        jPanel_Menu.add(jButton_QuanLyPhieuMuon);
        jPanel_Menu.add(Box.createRigidArea(new java.awt.Dimension(0, 15)));
        jPanel_Menu.add(jButton_ThongKe);
        jPanel_Menu.add(Box.createRigidArea(new java.awt.Dimension(0, 15)));
        jPanel_Menu.add(jButton_QuanLyTaiKhoan);
        jPanel_Menu.add(Box.createVerticalGlue()); // Đẩy nút Logout xuống dưới
        jPanel_Menu.add(jButton_Logout);
        jPanel_Menu.add(Box.createRigidArea(new java.awt.Dimension(0, 30)));

        // Style nút Đăng xuất
        jButton_Logout.setBackground(LOGOUT_BTN_COLOR);
        jButton_Logout.setForeground(TEXT_COLOR_LIGHT);
        jButton_Logout.setFont(new java.awt.Font("Segoe UI", 1, 18));
        jButton_Logout.setBorderPainted(false);
        jButton_Logout.setFocusPainted(false);
        jButton_Logout.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton_Logout.setPreferredSize(new java.awt.Dimension(280, 50));
        jButton_Logout.setMaximumSize(new java.awt.Dimension(280, 50));

        // Style Panel Nội dung
        jPanel_Content.setBackground(CONTENT_BACKGROUND_COLOR);
    }

    /**
     * Cài đặt CardLayout và các panel con.
     */
    private void setupCardLayoutAndUI() {
        cardLayout = new CardLayout();
        jPanel_Content.setLayout(cardLayout);

        // Thêm các panel vào CardLayout
        jPanel_Content.add(new WelcomePanel(), "Welcome");
        jPanel_Content.add(new QuanLySachPanel(), "QuanLySach");
        jPanel_Content.add(new QuanLyBanDocPanel(), "QuanLyBanDoc");
        jPanel_Content.add(new QuanLyPhieuMuonPanel(), "QuanLyPhieuMuon");
        jPanel_Content.add(new ThongKePanel(), "ThongKe");
        if(currentUser.getRole().equalsIgnoreCase("admin")) {
            jPanel_Content.add(new QuanLyTaiKhoanPanel(currentUser.getRole()), "QuanLyTaiKhoan");
        }

        // Hiển thị panel mặc định
        cardLayout.show(jPanel_Content, "Welcome");

        // Thêm các nút vào danh sách để quản lý hiệu ứng
        menuButtons = new ArrayList<>(Arrays.asList(
                jButton_QuanLySach,
                jButton_QuanLyBanDoc,
                jButton_QuanLyPhieuMuon,
                jButton_ThongKe,
                jButton_QuanLyTaiKhoan
        ));

        for (JButton button : menuButtons) {
            addHoverEffect(button);
        }
    }

    /**
     * Phương thức chung để style các nút menu.
     */
    private void styleMenuButton(JButton button, String iconPath) {
        button.setFont(new java.awt.Font("Segoe UI", 1, 16));
        button.setForeground(TEXT_COLOR_LIGHT);
        button.setBackground(BTN_DEFAULT_COLOR);

        // Layout (Icon bên trái, Chữ bên phải)
        button.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        button.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
        button.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        button.setIconTextGap(20); // Khoảng cách icon và chữ

        button.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 10));
        button.setFocusPainted(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        button.setPreferredSize(new java.awt.Dimension(280, 60));
        button.setMinimumSize(new java.awt.Dimension(280, 60));
        button.setMaximumSize(new java.awt.Dimension(280, 60));

        // Tải icon
        try {
            button.setIcon(new ImageIcon(getClass().getResource(iconPath)));
        } catch (Exception e) {
            System.err.println("Icon not found: " + iconPath);
        }
    }

    /**
     * Thêm hiệu ứng hover cho nút (loại trừ nút đang được chọn).
     */
    private void addHoverEffect(javax.swing.JButton button) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isVisible() && button != selectedButton) {
                    button.setBackground(BTN_HOVER_COLOR);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isVisible() && button != selectedButton) {
                    button.setBackground(BTN_DEFAULT_COLOR);
                }
            }
        });
    }

    /**
     * Đặt màu cho nút được chọn và reset các nút khác.
     */
    private void setSelectedButton(JButton button) {
        // Reset tất cả các nút
        for (JButton btn : menuButtons) {
            if (btn.isVisible()) {
                btn.setBackground(BTN_DEFAULT_COLOR);
            }
        }

        // Đặt màu cho nút được chọn
        selectedButton = button;
        if (selectedButton != null && selectedButton.isVisible()) {
            selectedButton.setBackground(BTN_SELECTED_COLOR);
        }
    }

    /**
     * Ẩn/hiện các chức năng dựa trên vai trò của người dùng.
     */
    private void authorizeRoles() {
        String role = currentUser.getRole();

        if (role == null) {
            // Ẩn tất cả nếu không có vai trò
            jButton_QuanLySach.setVisible(false);
            jButton_QuanLyBanDoc.setVisible(false);
            jButton_QuanLyPhieuMuon.setVisible(false);
            jButton_QuanLyTaiKhoan.setVisible(false);
            jButton_ThongKe.setVisible(false);
            return;
        }

        switch (role) {
            case "Admin":
                // Admin thấy tất cả
                break;
            case "ThuThu":
                // Thủ thư không thấy quản lý tài khoản
                jButton_QuanLyTaiKhoan.setVisible(false);
                break;
            default:
                // Các vai trò khác (ví dụ: Bạn đọc)
                jButton_QuanLySach.setVisible(false);
                jButton_QuanLyBanDoc.setVisible(false);
                jButton_QuanLyPhieuMuon.setVisible(false);
                jButton_QuanLyTaiKhoan.setVisible(false);
                jButton_ThongKe.setVisible(false);
                break;
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

        jPanel_Menu = new javax.swing.JPanel();
        jLabel_Welcome = new javax.swing.JLabel();
        jButton_QuanLyBanDoc = new javax.swing.JButton();
        jButton_QuanLyPhieuMuon = new javax.swing.JButton();
        jButton_QuanLySach = new javax.swing.JButton();
        jButton_ThongKe = new javax.swing.JButton();
        jButton_QuanLyTaiKhoan = new javax.swing.JButton();
        jButton_Logout = new javax.swing.JButton();
        jPanel_Content = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Library Management System");

        jPanel_Menu.setBackground(new java.awt.Color(51, 51, 51));

        jLabel_Welcome.setText("Xin chào");

        jButton_QuanLyBanDoc.setText("Quản Lý Bạn Đọc");
        jButton_QuanLyBanDoc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_QuanLyBanDocActionPerformed(evt);
            }
        });

        jButton_QuanLyPhieuMuon.setText("Quản Lý Phiếu Mượn");
        jButton_QuanLyPhieuMuon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_QuanLyPhieuMuonActionPerformed(evt);
            }
        });

        jButton_QuanLySach.setText("Quản Lý Sách");
        jButton_QuanLySach.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_QuanLySachActionPerformed(evt);
            }
        });

        jButton_ThongKe.setText("Thống Kê");
        jButton_ThongKe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_ThongKeActionPerformed(evt);
            }
        });

        jButton_QuanLyTaiKhoan.setText("Quản Lý Tài Khoản");
        jButton_QuanLyTaiKhoan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_QuanLyTaiKhoanActionPerformed(evt);
            }
        });

        jButton_Logout.setBackground(new java.awt.Color(255, 102, 102));
        jButton_Logout.setForeground(new java.awt.Color(255, 255, 255));
        jButton_Logout.setText("Đăng Xuất");
        jButton_Logout.setBorderPainted(false);
        jButton_Logout.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton_Logout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_LogoutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel_MenuLayout = new javax.swing.GroupLayout(jPanel_Menu);
        jPanel_Menu.setLayout(jPanel_MenuLayout);
        jPanel_MenuLayout.setHorizontalGroup(
            jPanel_MenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_MenuLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel_MenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton_QuanLySach, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton_QuanLyBanDoc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton_QuanLyPhieuMuon, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(jButton_ThongKe, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton_QuanLyTaiKhoan, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton_Logout, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel_Welcome, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        jPanel_MenuLayout.setVerticalGroup(
            jPanel_MenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_MenuLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel_Welcome, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addComponent(jButton_QuanLySach, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton_QuanLyBanDoc, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton_QuanLyPhieuMuon, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton_ThongKe, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton_QuanLyTaiKhoan, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 60, Short.MAX_VALUE)
                .addComponent(jButton_Logout, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
        );

        jPanel_Content.setBackground(new java.awt.Color(245, 245, 245));

        javax.swing.GroupLayout jPanel_ContentLayout = new javax.swing.GroupLayout(jPanel_Content);
        jPanel_Content.setLayout(jPanel_ContentLayout);
        jPanel_ContentLayout.setHorizontalGroup(
            jPanel_ContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 788, Short.MAX_VALUE)
        );
        jPanel_ContentLayout.setVerticalGroup(
            jPanel_ContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel_Menu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel_Content, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel_Menu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel_Content, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_QuanLyBanDocActionPerformed(java.awt.event.ActionEvent evt) {
        cardLayout.show(jPanel_Content, "QuanLyBanDoc");
        setSelectedButton(jButton_QuanLyBanDoc);
    }

    private void jButton_QuanLyPhieuMuonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_QuanLyPhieuMuonActionPerformed
        cardLayout.show(jPanel_Content, "QuanLyPhieuMuon");
        setSelectedButton(jButton_QuanLyPhieuMuon);
    }//GEN-LAST:event_jButton_QuanLyPhieuMuonActionPerformed

    private void jButton_QuanLySachActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_QuanLySachActionPerformed
        cardLayout.show(jPanel_Content, "QuanLySach");
        setSelectedButton(jButton_QuanLySach);
    }//GEN-LAST:event_jButton_QuanLySachActionPerformed

    private void jButton_ThongKeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_ThongKeActionPerformed
        cardLayout.show(jPanel_Content, "ThongKe");
        setSelectedButton(jButton_ThongKe);
    }//GEN-LAST:event_jButton_ThongKeActionPerformed

    private void jButton_QuanLyTaiKhoanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_QuanLyTaiKhoanActionPerformed
        cardLayout.show(jPanel_Content, "QuanLyTaiKhoan");
        setSelectedButton(jButton_QuanLyTaiKhoan);
    }//GEN-LAST:event_jButton_QuanLyTaiKhoanActionPerformed

    private void jButton_LogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_LogoutActionPerformed
        this.dispose();

        JFrame_Login loginFrame = new JFrame_Login();
        loginFrame.setVisible(true);
    }//GEN-LAST:event_jButton_LogoutActionPerformed

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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(JFrame_Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JFrame_Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JFrame_Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JFrame_Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Ví dụ: TaiKhoan testUser = new TaiKhoan("test", "pass", "Test User", "Admin");
                // new JFrame_Main(testUser).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_Logout;
    private javax.swing.JButton jButton_QuanLyBanDoc;
    private javax.swing.JButton jButton_QuanLyPhieuMuon;
    private javax.swing.JButton jButton_QuanLySach;
    private javax.swing.JButton jButton_QuanLyTaiKhoan;
    private javax.swing.JButton jButton_ThongKe;
    private javax.swing.JLabel jLabel_Welcome;
    private javax.swing.JPanel jPanel_Content;
    private javax.swing.JPanel jPanel_Menu;
    // End of variables declaration//GEN-END:variables
}