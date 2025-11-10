package com.mycompany.quanlythuvien.view.panel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 *
 * @author Tien
 */
public class QuanLySachPanel extends JPanel {

    public QuanLySachPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Đây là trang Quản Lý Sách (Placeholder)", JLabel.CENTER), BorderLayout.CENTER);
    }
}
