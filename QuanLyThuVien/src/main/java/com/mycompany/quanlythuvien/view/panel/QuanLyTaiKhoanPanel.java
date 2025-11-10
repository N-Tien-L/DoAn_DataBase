package com.mycompany.quanlythuvien.view.panel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class QuanLyTaiKhoanPanel extends JPanel {
    public QuanLyTaiKhoanPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Đây là trang Quản Lý Tài Khoản (Placeholder)", JLabel.CENTER), BorderLayout.CENTER);
    }
}
