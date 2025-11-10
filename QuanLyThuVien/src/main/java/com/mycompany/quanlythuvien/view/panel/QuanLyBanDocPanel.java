package com.mycompany.quanlythuvien.view.panel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class QuanLyBanDocPanel extends JPanel {
    public QuanLyBanDocPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Đây là trang Quản Lý Bạn Đọc (Placeholder)", JLabel.CENTER), BorderLayout.CENTER);
    }
}
