package com.mycompany.quanlythuvien.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.mycompany.quanlythuvien.controller.TaiKhoanController;
import com.mycompany.quanlythuvien.model.TaiKhoan;

/**
 * Dialog hiển thị thông tin hồ sơ cá nhân
 * @author Tien
 */
public class HoSoDialog extends JDialog {
    private final TaiKhoanController controller = new TaiKhoanController();
    private TaiKhoan currentTaiKhoan;

    private JTextField txtEmail;
    private JTextField txtHoTen;
    private JTextField txtRole;

    public HoSoDialog(Window parent, TaiKhoan taiKhoan) {
        super(parent, "Hồ Sơ Cá Nhân", ModalityType.APPLICATION_MODAL);
        this.currentTaiKhoan = taiKhoan;

        initComponents();
        loadData();
        
        setSize(450, 300);
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Title Panel
        JPanel titlePanel = new JPanel();
        JLabel lblTitle = new JLabel("THÔNG TIN HỒ SƠ");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(lblTitle);
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Email
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("📧 Email:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtEmail = new JTextField();
        txtEmail.setEditable(false);
        txtEmail.setBackground(getBackground());
        txtEmail.setPreferredSize(new Dimension(250, 30));
        formPanel.add(txtEmail, gbc);
        
        // Họ tên
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("👤 Họ tên:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtHoTen = new JTextField();
        txtHoTen.setEditable(false);
        txtHoTen.setBackground(getBackground());
        txtHoTen.setPreferredSize(new Dimension(250, 30));
        formPanel.add(txtHoTen, gbc);
        
        // Vai trò
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("🔑 Vai trò:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtRole = new JTextField();
        txtRole.setEditable(false);
        txtRole.setBackground(getBackground());
        txtRole.setPreferredSize(new Dimension(250, 30));
        formPanel.add(txtRole, gbc);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        JButton btnChangePassword = new JButton("🔐 Đổi mật khẩu");
        JButton btnClose = new JButton("Đóng");
        
        btnChangePassword.setPreferredSize(new Dimension(150, 35));
        btnClose.setPreferredSize(new Dimension(100, 35));
        
        btnChangePassword.addActionListener(e -> handleChangePassword());
        btnClose.addActionListener(e -> dispose());
        
        buttonPanel.add(btnChangePassword);
        buttonPanel.add(btnClose);
        
        // Add panels to dialog
        add(titlePanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadData() {
        if (currentTaiKhoan != null) {
            txtEmail.setText(currentTaiKhoan.getEmail());
            txtHoTen.setText(currentTaiKhoan.getHoTen());
            txtRole.setText(currentTaiKhoan.getRole());
        }
    }
    
    private void handleChangePassword() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JPasswordField txtOldPassword = new JPasswordField(20);
        JPasswordField txtNewPassword = new JPasswordField(20);
        JPasswordField txtConfirmPassword = new JPasswordField(20);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Mật khẩu cũ:"), gbc);
        gbc.gridx = 1;
        panel.add(txtOldPassword, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Mật khẩu mới:"), gbc);
        gbc.gridx = 1;
        panel.add(txtNewPassword, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Xác nhận mật khẩu:"), gbc);
        gbc.gridx = 1;
        panel.add(txtConfirmPassword, gbc);
        
        int result = JOptionPane.showConfirmDialog(
            this,
            panel,
            "Đổi mật khẩu",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            String oldPassword = new String(txtOldPassword.getPassword());
            String newPassword = new String(txtNewPassword.getPassword());
            String confirmPassword = new String(txtConfirmPassword.getPassword());
            
            // Validation
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng điền đầy đủ thông tin!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Mật khẩu xác nhận không khớp!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(
                    this,
                    "Mật khẩu mới phải có ít nhất 6 ký tự!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            boolean success = controller.changePassword(
                currentTaiKhoan.getEmail(),
                oldPassword,
                newPassword
            );
            
            if (success) {
                JOptionPane.showMessageDialog(
                    this,
                    "Đổi mật khẩu thành công!",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "Đổi mật khẩu thất bại!\nVui lòng kiểm tra lại mật khẩu cũ.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
