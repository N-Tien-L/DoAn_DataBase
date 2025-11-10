package com.mycompany.quanlythuvien.view.panel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class ThongKePanel extends JPanel{
    public ThongKePanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Đây là trang Quản Lý Tài Khoản (Placeholder)", JLabel.CENTER), BorderLayout.CENTER);
    }
}
