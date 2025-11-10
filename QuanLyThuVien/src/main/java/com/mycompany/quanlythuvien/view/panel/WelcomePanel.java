package com.mycompany.quanlythuvien.view.panel;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Tien
 */
public class WelcomePanel extends JPanel {

    public WelcomePanel() {
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 245, 245));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 20, 20);
        
        // Icon hoặc logo
        JLabel iconLabel = new JLabel("≡");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 120));
        iconLabel.setForeground(new Color(0, 102, 204));
        add(iconLabel, gbc);
        
        // Tiêu đề chào mừng
        gbc.gridy = 1;
        JLabel titleLabel = new JLabel("Chào mừng đến với Hệ Thống Quản Lý Thư Viện");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 102, 204));
        add(titleLabel, gbc);
        
        // Hướng dẫn
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 20, 5, 20);
        JLabel guideLabel = new JLabel("<html><div style='text-align: center;'>" +
                "Vui lòng chọn một chức năng từ menu bên trái để bắt đầu" +
                "</div></html>");
        guideLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        guideLabel.setForeground(new Color(100, 100, 100));
        add(guideLabel, gbc);
        
        // Danh sách chức năng
        gbc.gridy = 3;
        gbc.insets = new Insets(30, 20, 20, 20);
        JPanel featuresPanel = createFeaturesPanel();
        add(featuresPanel, gbc);
    }
    
    private JPanel createFeaturesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        String[] features = {
            "▸ Quản Lý Sách - Thêm, sửa, xóa thông tin sách",
            "▸ Quản Lý Bạn Đọc - Quản lý thông tin độc giả",
            "▸ Quản Lý Phiếu Mượn - Theo dõi mượn/trả sách",
            "▸ Thống Kê - Xem báo cáo và thống kê",
            "▸ Quản Lý Tài Khoản (Admin only) - Quản lý người dùng hệ thống"
        };
        
        for (String feature : features) {
            JLabel featureLabel = new JLabel(feature);
            featureLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            featureLabel.setForeground(new Color(60, 60, 60));
            featureLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            featureLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(featureLabel);
        }
        
        return panel;
    }
}
