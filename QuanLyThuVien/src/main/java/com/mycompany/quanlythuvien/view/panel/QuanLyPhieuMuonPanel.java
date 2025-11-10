package com.mycompany.quanlythuvien.view.panel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class QuanLyPhieuMuonPanel extends JPanel {
    public QuanLyPhieuMuonPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Đây là trang Quản Lý Phiếu Mượn (Placeholder)", JLabel.CENTER), BorderLayout.CENTER);
    }
}
