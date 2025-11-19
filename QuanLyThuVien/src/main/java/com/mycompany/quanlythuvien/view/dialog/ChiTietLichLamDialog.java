package com.mycompany.quanlythuvien.view.dialog;

import com.mycompany.quanlythuvien.model.LichLam;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Dialog chi tiết lịch làm
 * @author Tien
 */
public class ChiTietLichLamDialog extends JDialog {

    private final LichLam lichLam;
    private final Color PRIMARY_COLOR = new Color(0, 123, 255);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font TEXT_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public ChiTietLichLamDialog(Window parent, LichLam lichLam) {
        super(parent, "Chi Tiết Ca Làm", ModalityType.APPLICATION_MODAL);
        this.lichLam = lichLam;
        initComponents();
        setSize(500, 500); // Tăng kích thước cố định
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 1. Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setBackground(PRIMARY_COLOR);
        JLabel lblTitle = new JLabel("THÔNG TIN CA LÀM VIỆC");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);

        // 2. Content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 15, 10); // Padding bottom 15px
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Helper để format ngày giờ
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        // Row 1: Ngày
        addLabel(contentPanel, "Ngày làm việc:", 0, 0, gbc);
        addValue(contentPanel, lichLam.getNgay().toLocalDate().format(dateFmt), 1, 0, gbc);

        // Row 2: Thời gian
        addLabel(contentPanel, "Khung giờ:", 0, 1, gbc);
        String timeRange = lichLam.getGioBatDau().toLocalTime().format(timeFmt) + " - " + 
                           lichLam.getGioKetThuc().toLocalTime().format(timeFmt);
        addValue(contentPanel, timeRange, 1, 1, gbc);

        // Row 3: Trạng thái
        addLabel(contentPanel, "Trạng thái:", 0, 2, gbc);
        JLabel lblStatus = new JLabel(lichLam.getTrangThai());
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStatus.setOpaque(true);
        lblStatus.setBorder(new EmptyBorder(4, 8, 4, 8));
        
        // Màu sắc trạng thái
        if ("Done".equalsIgnoreCase(lichLam.getTrangThai())) {
            lblStatus.setBackground(new Color(220, 255, 220));
            lblStatus.setForeground(new Color(40, 167, 69));
        } else if ("Cancelled".equalsIgnoreCase(lichLam.getTrangThai())) {
            lblStatus.setBackground(new Color(255, 220, 220));
            lblStatus.setForeground(new Color(220, 53, 69));
        } else {
            lblStatus.setBackground(new Color(220, 240, 255));
            lblStatus.setForeground(PRIMARY_COLOR);
        }
        
        gbc.gridx = 1; gbc.gridy = 2;
        contentPanel.add(lblStatus, gbc);

        // Row 4: Ghi chú
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblNote = new JLabel("Ghi chú:");
        lblNote.setFont(LABEL_FONT);
        lblNote.setForeground(Color.GRAY);
        contentPanel.add(lblNote, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        JTextArea txtNote = new JTextArea(5, 30); // Set rows/cols để có kích thước mặc định tốt hơn
        txtNote.setText(lichLam.getGhiChu() == null ? "Không có ghi chú" : lichLam.getGhiChu());
        txtNote.setFont(TEXT_FONT);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        txtNote.setEditable(false);
        txtNote.setBackground(new Color(250, 250, 250));
        txtNote.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        contentPanel.add(txtNote, gbc);

        // 3. Footer Button
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(0, 20, 20, 20));

        JButton btnClose = new JButton("Đóng");
        styleButton(btnClose, new Color(108, 117, 125)); // Màu xám
        btnClose.addActionListener(e -> dispose());
        footerPanel.add(btnClose);

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void addLabel(JPanel panel, String text, int x, int y, GridBagConstraints gbc) {
        gbc.gridx = x; gbc.gridy = y;
        gbc.weightx = 0; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel lbl = new JLabel(text);
        lbl.setFont(LABEL_FONT);
        lbl.setForeground(Color.GRAY);
        panel.add(lbl, gbc);
    }

    private void addValue(JPanel panel, String text, int x, int y, GridBagConstraints gbc) {
        gbc.gridx = x; gbc.gridy = y;
        JLabel lbl = new JLabel(text);
        lbl.setFont(TEXT_FONT);
        lbl.setForeground(Color.BLACK);
        panel.add(lbl, gbc);
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
