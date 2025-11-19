package com.mycompany.quanlythuvien.view.dialog;

import com.mycompany.quanlythuvien.controller.YeuCauResetMKController;
import com.mycompany.quanlythuvien.model.TaiKhoan;
import com.mycompany.quanlythuvien.model.YeuCauResetMK;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Dialog hiển thị các request reset mật khẩu hiện có
 * @author Tien
 */
public class YeuCauResetMKDialog extends JDialog {
    
    private final YeuCauResetMKController controller;
    private final TaiKhoan currentUser;
    private JPanel contentListPanel;
    private JLabel lblNoData;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private final Color PRIMARY_COLOR = new Color(37, 99, 235);
    private final Color SUCCESS_COLOR = new Color(22, 163, 74);
    private final Color DANGER_COLOR = new Color(220, 38, 38);
    private final Color BG_COLOR = new Color(243, 244, 246);
    private final Color TEXT_COLOR = new Color(31, 41, 55);
    private final Color SUBTEXT_COLOR = new Color(107, 114, 128);

    public YeuCauResetMKDialog(Window parent, TaiKhoan currentUser) {
        super(parent, "Quản lý Yêu cầu Reset Mật khẩu", ModalityType.APPLICATION_MODAL);
        this.controller = new YeuCauResetMKController();
        this.currentUser = currentUser;
        
        initComponents();
        loadData();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setSize(800, 600);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout());
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 5));
        titleBox.setBackground(Color.WHITE);
        
        JLabel lblTitle = new JLabel("Yêu cầu đặt lại mật khẩu");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_COLOR);
        
        JLabel lblSubtitle = new JLabel("Danh sách các yêu cầu đang chờ phê duyệt");
        lblSubtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSubtitle.setForeground(SUBTEXT_COLOR);
        
        titleBox.add(lblTitle);
        titleBox.add(lblSubtitle);
        
        JButton btnRefresh = createOutlineButton("Làm mới", PRIMARY_COLOR);
        btnRefresh.addActionListener(e -> loadData());
        
        headerPanel.add(titleBox, BorderLayout.CENTER);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        
        contentListPanel = new JPanel();
        contentListPanel.setLayout(new BoxLayout(contentListPanel, BoxLayout.Y_AXIS));
        contentListPanel.setBackground(BG_COLOR);
        contentListPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JPanel listWrapper = new JPanel(new BorderLayout());
        listWrapper.setBackground(BG_COLOR);
        listWrapper.add(contentListPanel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(listWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(BG_COLOR);
        
        lblNoData = new JLabel("Hiện không có yêu cầu nào.");
        lblNoData.setFont(new Font("Arial", Font.ITALIC, 16));
        lblNoData.setForeground(SUBTEXT_COLOR);
        lblNoData.setHorizontalAlignment(SwingConstants.CENTER);
        lblNoData.setVisible(false);
        
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JButton btnClose = createOutlineButton("Đóng", SUBTEXT_COLOR);
        btnClose.addActionListener(e -> dispose());
        footerPanel.add(btnClose);
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private void loadData() {
        contentListPanel.removeAll();
        
        try {
            List<YeuCauResetMK> danhSach = controller.getPendingYeuCau(currentUser);
            
            if (danhSach == null || danhSach.isEmpty()) {
                contentListPanel.add(Box.createVerticalStrut(50));
                contentListPanel.add(lblNoData);
                lblNoData.setVisible(true);
            } else {
                lblNoData.setVisible(false);
                for (YeuCauResetMK yc : danhSach) {
                    contentListPanel.add(createRequestCard(yc));
                    contentListPanel.add(Box.createVerticalStrut(15));
                }
            }
            
            contentListPanel.revalidate();
            contentListPanel.repaint();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + e.getMessage());
        }
    }
    
    private JPanel createRequestCard(YeuCauResetMK yc) {
        JPanel card = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2.setColor(new Color(229, 231, 235));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        
        String initial = "?";
        if (yc.getHoTenThuThu() != null && !yc.getHoTenThuThu().isEmpty()) {
            initial = String.valueOf(yc.getHoTenThuThu().charAt(0)).toUpperCase();
        }
        AvatarPanel avatar = new AvatarPanel(initial);
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel lblName = new JLabel(yc.getHoTenThuThu());
        lblName.setFont(new Font("Arial", Font.BOLD, 16));
        lblName.setForeground(TEXT_COLOR);
        
        JLabel lblEmail = new JLabel(yc.getEmailThuThu());
        lblEmail.setFont(new Font("Arial", Font.PLAIN, 14));
        lblEmail.setForeground(PRIMARY_COLOR);
        
        String timeStr = (yc.getCreatedAt() != null) ? yc.getCreatedAt().format(DATE_FORMATTER) : "N/A";
        JLabel lblTime = new JLabel("Gửi lúc: " + timeStr);
        lblTime.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTime.setForeground(SUBTEXT_COLOR);
        
        JLabel lblReason = new JLabel("<html><i>\"" + yc.getLyDo() + "\"</i></html>");
        lblReason.setFont(new Font("Arial", Font.PLAIN, 13));
        lblReason.setForeground(new Color(75, 85, 99));
        lblReason.setBorder(new EmptyBorder(5, 0, 0, 0));

        infoPanel.add(lblName);
        infoPanel.add(lblEmail);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblReason);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(lblTime);
        
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        btnPanel.setOpaque(false);
        
        JButton btnApprove = createSolidButton("Chấp nhận", SUCCESS_COLOR);
        btnApprove.addActionListener(e -> handleApprove(yc));
        
        JButton btnReject = createOutlineButton("Từ chối", DANGER_COLOR);
        btnReject.addActionListener(e -> handleReject(yc));
        
        btnPanel.add(btnApprove);
        btnPanel.add(btnReject);
        
        card.add(avatar, BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.EAST);
        
        return card;
    }
    
    private class AvatarPanel extends JPanel {
        private final String text;
        public AvatarPanel(String text) {
            this.text = text;
            setPreferredSize(new Dimension(60, 60));
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(new Color(219, 234, 254));
            g2.fill(new Ellipse2D.Double(0, 0, 60, 60));
            
            g2.setColor(PRIMARY_COLOR);
            g2.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g2.getFontMetrics();
            int x = (60 - fm.stringWidth(text)) / 2;
            int y = (60 - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, x, y);
            
            g2.dispose();
        }
    }

    private JButton createSolidButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 35));
        return btn;
    }

    private JButton createOutlineButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setForeground(color);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 35));
        
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        return btn;
    }
    
    private void handleApprove(YeuCauResetMK yc) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Xác nhận cấp mật khẩu mới cho: " + yc.getEmailThuThu() + "?",
            "Phê duyệt", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (controller.approveYeuCau(currentUser, yc.getId())) {
                    JOptionPane.showMessageDialog(this, "Đã gửi mật khẩu mới qua email!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleReject(YeuCauResetMK yc) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Từ chối yêu cầu của: " + yc.getEmailThuThu() + "?",
            "Từ chối", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
             try {
                if (controller.rejectYeuCau(currentUser, yc.getId())) {
                    JOptionPane.showMessageDialog(this, "Đã từ chối yêu cầu.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}