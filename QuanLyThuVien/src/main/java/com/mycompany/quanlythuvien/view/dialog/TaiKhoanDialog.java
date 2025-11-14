package com.mycompany.quanlythuvien.view.dialog;

import com.mycompany.quanlythuvien.controller.TaiKhoanController;
import com.mycompany.quanlythuvien.model.TaiKhoan;
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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Dialog để thêm mới hoặc chỉnh sửa tài khoản
 * @author Tien
 */
public class TaiKhoanDialog extends JDialog {
    
    public enum Mode {
        ADD, EDIT
    }
    
    private final TaiKhoanController controller;
    private final String currentUserRole;
    private final Mode mode;
    private TaiKhoan taiKhoan;
    
    // UI Components
    private JTextField txtEmail;
    private JTextField txtHoTen;
    private JComboBox<String> cmbRole;
    private JButton btnSave, btnCancel;
    
    // Result
    private boolean success = false;
    
    /**
     * Constructor cho chế độ ADD
     */
    public TaiKhoanDialog(Window parent, String currentUserRole) {
        this(parent, currentUserRole, Mode.ADD, null);
    }
    
    /**
     * Constructor cho chế độ EDIT
     */
    public TaiKhoanDialog(Window parent, String currentUserRole, TaiKhoan taiKhoan) {
        this(parent, currentUserRole, Mode.EDIT, taiKhoan);
    }
    
    /**
     * Constructor chính
     */
    private TaiKhoanDialog(Window parent, String currentUserRole, Mode mode, TaiKhoan taiKhoan) {
        super(parent, mode == Mode.ADD ? "Thêm tài khoản mới" : "Chỉnh sửa tài khoản", ModalityType.APPLICATION_MODAL);
        
        this.controller = new TaiKhoanController();
        this.currentUserRole = currentUserRole;
        this.mode = mode;
        this.taiKhoan = taiKhoan;
        
        initComponents();
        
        if (mode == Mode.EDIT && taiKhoan != null) {
            loadData();
        }
        
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(450, 300));
        
        // Form Panel
        add(createFormPanel(), BorderLayout.CENTER);
        
        // Button Panel
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        pack();
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Email
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(lblEmail, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        txtEmail = new JTextField(20);
        txtEmail.setFont(new Font("Arial", Font.PLAIN, 14));
        if (mode == Mode.EDIT) {
            txtEmail.setEditable(false); // Email là primary key, không cho sửa
            txtEmail.setBackground(new java.awt.Color(240, 240, 240));
        }
        formPanel.add(txtEmail, gbc);
        
        // Họ tên
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel lblHoTen = new JLabel("Họ tên:");
        lblHoTen.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(lblHoTen, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        txtHoTen = new JTextField(20);
        txtHoTen.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(txtHoTen, gbc);
        
        // Vai trò
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel lblRole = new JLabel("Vai trò:");
        lblRole.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(lblRole, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        String[] roles = {"Admin", "ThuThu"};
        cmbRole = new JComboBox<>(roles);
        cmbRole.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(cmbRole, gbc);
        
        // Note (chỉ hiện khi ADD)
        if (mode == Mode.ADD) {
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            JLabel lblNote = new JLabel("<html><i>ℹ️ Mật khẩu sẽ được tạo tự động (6 số) và gửi qua email</i></html>");
            lblNote.setFont(new Font("Arial", Font.ITALIC, 12));
            lblNote.setForeground(new java.awt.Color(100, 100, 100));
            formPanel.add(lblNote, gbc);
        }
        
        return formPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        btnSave = new JButton(mode == Mode.ADD ? "💾 Tạo tài khoản" : "💾 Cập nhật");
        btnCancel = new JButton("✖️ Hủy");
        
        btnSave.setFont(new Font("Arial", Font.BOLD, 13));
        btnCancel.setFont(new Font("Arial", Font.PLAIN, 13));
        
        btnSave.addActionListener(e -> handleSave());
        btnCancel.addActionListener(e -> handleCancel());
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        return buttonPanel;
    }
    
    private void loadData() {
        if (taiKhoan != null) {
            txtEmail.setText(taiKhoan.getEmail());
            txtHoTen.setText(taiKhoan.getHoTen());
            
            String role = taiKhoan.getRole();
            if (role != null) {
                cmbRole.setSelectedItem(role);
            }
        }
    }
    
    private void handleSave() {
        // Validate input
        String email = txtEmail.getText().trim();
        String hoTen = txtHoTen.getText().trim();
        String role = (String) cmbRole.getSelectedItem();
        
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Email không được để trống!",
                "Lỗi validation",
                JOptionPane.ERROR_MESSAGE);
            txtEmail.requestFocus();
            return;
        }
        
        // Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            JOptionPane.showMessageDialog(this,
                "Email không hợp lệ!",
                "Lỗi validation",
                JOptionPane.ERROR_MESSAGE);
            txtEmail.requestFocus();
            return;
        }
        
        if (hoTen.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Họ tên không được để trống!",
                "Lỗi validation",
                JOptionPane.ERROR_MESSAGE);
            txtHoTen.requestFocus();
            return;
        }
        
        // Disable buttons during processing
        btnSave.setEnabled(false);
        btnCancel.setEnabled(false);
        
        boolean result;
        
        if (mode == Mode.ADD) {
            result = controller.createAccount(currentUserRole, email, hoTen, role);
            
            if (result) {
                JOptionPane.showMessageDialog(this,
                    "Tạo tài khoản thành công!\n" +
                    "Mật khẩu đã được gửi đến email: " + email,
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Tạo tài khoản thất bại!\n" +
                    "Email có thể đã tồn tại hoặc có lỗi xảy ra.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        } else {
            result = controller.updateAccount(currentUserRole, email, hoTen, role);
            
            if (result) {
                JOptionPane.showMessageDialog(this,
                    "Cập nhật tài khoản thành công!",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Cập nhật tài khoản thất bại!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        if (result) {
            this.success = true;
            dispose();
        } else {
            // Re-enable buttons if failed
            btnSave.setEnabled(true);
            btnCancel.setEnabled(true);
        }
    }
    
    private void handleCancel() {
        this.success = false;
        dispose();
    }
    
    /**
     * Kiểm tra xem thao tác có thành công không
     * @return true nếu đã save thành công
     */
    public boolean isSuccess() {
        return success;
    }
}
