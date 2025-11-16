package com.mycompany.quanlythuvien.view.dialog;

import com.mycompany.quanlythuvien.model.TaiKhoanProfile;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.text.SimpleDateFormat;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/**
 * Dialog hiển thị thông tin chi tiết + thống kê của tài khoản
 * @author Tien
 */
public class TaiKhoanProfileDialog extends JDialog {
    
    private final TaiKhoanProfile profile;
    
    public TaiKhoanProfileDialog(Window parent, TaiKhoanProfile profile) {
        super(parent, "Chi tiết tài khoản", ModalityType.APPLICATION_MODAL);
        this.profile = profile;
        
        initComponents();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(600, 500));
        
        // Title Panel
        add(createTitlePanel(), BorderLayout.NORTH);
        
        // Content Panel
        add(createContentPanel(), BorderLayout.CENTER);
        
        // Button Panel
        add(createButtonPanel(), BorderLayout.SOUTH);
        
        pack();
    }
    
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        titlePanel.setBackground(new Color(41, 128, 185));
        
        JLabel lblTitle = new JLabel("THÔNG TIN TÀI KHOẢN");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        
        titlePanel.add(lblTitle, BorderLayout.CENTER);
        
        return titlePanel;
    }
    
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        contentPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // === THÔNG TIN CƠ BẢN ===
        addSectionHeader(contentPanel, gbc, row++, "[■] Thông tin cơ bản");
        
        addInfoRow(contentPanel, gbc, row++, "Email:", profile.getEmail());
        addInfoRow(contentPanel, gbc, row++, "Họ tên:", profile.getHoTen());
        addInfoRow(contentPanel, gbc, row++, "Vai trò:", profile.getRole());
        addInfoRow(contentPanel, gbc, row++, "Trạng thái:", profile.getStatus());
        
        // === METADATA ===
        addSeparator(contentPanel, gbc, row++);
        addSectionHeader(contentPanel, gbc, row++, "[●] Metadata");
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String createdAtStr = profile.getCreatedAt() != null ? sdf.format(profile.getCreatedAt()) : "N/A";
        addInfoRow(contentPanel, gbc, row++, "Thời gian tạo:", createdAtStr);
        addInfoRow(contentPanel, gbc, row++, "Tạo bởi:", profile.getCreatedBy() != null ? profile.getCreatedBy() : "N/A");
        
        // === THỐNG KÊ ===
        addSeparator(contentPanel, gbc, row++);
        addSectionHeader(contentPanel, gbc, row++, "[▲] Thống kê hoạt động");
        
        addStatRow(contentPanel, gbc, row++, "Số bạn đọc đã tạo:", profile.getSoBanDocTao(), "•");
        addStatRow(contentPanel, gbc, row++, "Số sách đã thêm:", profile.getSoSachThem(), "•");
        addStatRow(contentPanel, gbc, row++, "Số bản sao đã nhập:", profile.getSoBanSaoNhap(), "•");
        addStatRow(contentPanel, gbc, row++, "Số phiếu mượn đã lập:", profile.getSoPhieuMuonLap(), "•");
        addStatRow(contentPanel, gbc, row++, "Số sách đang cho mượn:", profile.getSoBanSaoDangChoMuon(), "•");
        
        return contentPanel;
    }
    
    private void addSectionHeader(JPanel panel, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        
        JLabel lblHeader = new JLabel(text);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 15));
        lblHeader.setForeground(new Color(41, 128, 185));
        
        panel.add(lblHeader, gbc);
        
        gbc.gridwidth = 1; // Reset
    }
    
    private void addSeparator(JPanel panel, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.insets = new Insets(15, 0, 15, 0);
        
        JSeparator separator = new JSeparator();
        panel.add(separator, gbc);
        
        gbc.gridwidth = 1; // Reset
        gbc.insets = new Insets(8, 10, 8, 10); // Reset insets
    }
    
    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        // Label
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.4;
        
        JLabel lblField = new JLabel(label);
        lblField.setFont(new Font("Arial", Font.BOLD, 13));
        lblField.setForeground(new Color(52, 73, 94));
        
        panel.add(lblField, gbc);
        
        // Value
        gbc.gridx = 1;
        gbc.weightx = 0.6;
        
        JLabel lblValue = new JLabel(value != null ? value : "N/A");
        lblValue.setFont(new Font("Arial", Font.PLAIN, 13));
        
        panel.add(lblValue, gbc);
    }
    
    private void addStatRow(JPanel panel, GridBagConstraints gbc, int row, String label, int value, String icon) {
        // Label
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.6;
        
        JLabel lblField = new JLabel(icon + " " + label);
        lblField.setFont(new Font("Arial", Font.PLAIN, 13));
        
        panel.add(lblField, gbc);
        
        // Value (số liệu)
        gbc.gridx = 1;
        gbc.weightx = 0.4;
        
        JLabel lblValue = new JLabel(String.valueOf(value));
        lblValue.setFont(new Font("Arial", Font.BOLD, 14));
        lblValue.setForeground(new Color(41, 128, 185));
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);
        
        panel.add(lblValue, gbc);
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Arial", Font.BOLD, 14));
        btnClose.setPreferredSize(new Dimension(120, 35));
        btnClose.addActionListener(e -> dispose());
        
        buttonPanel.add(btnClose);
        
        return buttonPanel;
    }
}
