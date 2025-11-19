package com.mycompany.quanlythuvien.view.dialog;

import com.mycompany.quanlythuvien.controller.ThongBaoController;
import com.mycompany.quanlythuvien.model.TaiKhoan;
import com.mycompany.quanlythuvien.model.ThongBaoNguoiNhan;
import java.awt.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Dialog chi tiết thông báo phía thủ thư
 * @author Tien
 */
public class ChiTietThongBaoNguoiDungDialog extends JDialog {
    
    private final ThongBaoNguoiNhan notification;
    private final TaiKhoan currentUser;
    private final ThongBaoController controller;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    
    public ChiTietThongBaoNguoiDungDialog(Window owner, ThongBaoNguoiNhan notification, 
                                          TaiKhoan currentUser, ThongBaoController controller) {
        super(owner, "Chi Tiết Thông Báo", ModalityType.APPLICATION_MODAL);
        this.notification = notification;
        this.currentUser = currentUser;
        this.controller = controller;
        
        initComponents();
        markAsRead();
        
        setSize(650, 500);
        setLocationRelativeTo(owner);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Main content panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);
        
        // Status badge
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusPanel.setOpaque(false);
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblStatusBadge = new JLabel(notification.isDaDoc() ? " ĐÃ ĐỌC " : " MỚI ");
        lblStatusBadge.setFont(new Font("Arial", Font.BOLD, 11));
        lblStatusBadge.setForeground(Color.WHITE);
        lblStatusBadge.setBackground(notification.isDaDoc() ? 
            new Color(108, 117, 125) : new Color(0, 123, 255));
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBorder(new EmptyBorder(3, 8, 3, 8));
        statusPanel.add(lblStatusBadge);
        
        mainPanel.add(statusPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Title
        JLabel lblTitle = new JLabel(notification.getTieuDe());
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(lblTitle);
        
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Metadata panel
        JPanel metaPanel = new JPanel();
        metaPanel.setLayout(new BoxLayout(metaPanel, BoxLayout.Y_AXIS));
        metaPanel.setOpaque(false);
        metaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblSentBy = new JLabel("Gửi bởi: " + notification.getCreatedBy());
        lblSentBy.setFont(new Font("Arial", Font.PLAIN, 13));
        lblSentBy.setForeground(new Color(100, 100, 100));
        lblSentBy.setAlignmentX(Component.LEFT_ALIGNMENT);
        metaPanel.add(lblSentBy);
        
        JLabel lblCreatedAt = new JLabel("Ngày tạo: " + 
            dateFormat.format(java.sql.Timestamp.valueOf(notification.getCreatedAt())));
        lblCreatedAt.setFont(new Font("Arial", Font.PLAIN, 13));
        lblCreatedAt.setForeground(new Color(100, 100, 100));
        lblCreatedAt.setAlignmentX(Component.LEFT_ALIGNMENT);
        metaPanel.add(lblCreatedAt);
        
        if (notification.isDaDoc() && notification.getReadAt() != null) {
            JLabel lblReadAt = new JLabel("Đã đọc lúc: " + 
                dateFormat.format(java.sql.Timestamp.valueOf(notification.getReadAt())));
            lblReadAt.setFont(new Font("Arial", Font.PLAIN, 13));
            lblReadAt.setForeground(new Color(100, 100, 100));
            lblReadAt.setAlignmentX(Component.LEFT_ALIGNMENT);
            metaPanel.add(lblReadAt);
        }
        
        mainPanel.add(metaPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Separator
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        mainPanel.add(separator);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Content
        JLabel lblContentLabel = new JLabel("Nội dung:");
        lblContentLabel.setFont(new Font("Arial", Font.BOLD, 14));
        lblContentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(lblContentLabel);
        
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JTextArea txtContent = new JTextArea(notification.getNoiDung());
        txtContent.setFont(new Font("Arial", Font.PLAIN, 14));
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setEditable(false);
        txtContent.setBackground(new Color(248, 249, 250));
        txtContent.setBorder(new EmptyBorder(12, 12, 12, 12));
        
        JScrollPane scrollPane = new JScrollPane(txtContent);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.setPreferredSize(new Dimension(580, 200));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        mainPanel.add(scrollPane);
        
        // Wrap main panel in scroll pane
        JScrollPane mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setBorder(null);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScrollPane, BorderLayout.CENTER);
        
        // Bottom panel with close button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        
        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Arial", Font.PLAIN, 14));
        btnClose.setPreferredSize(new Dimension(120, 35));
        btnClose.setBackground(new Color(108, 117, 125));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        bottomPanel.add(btnClose);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void markAsRead() {
        if (!notification.isDaDoc()) {
            try {
                controller.markRead(currentUser, notification.getIdThongBao());
                notification.setDaDoc(true);
                notification.setReadAt(java.time.LocalDateTime.now());
            } catch (Exception ex) {
                // Silent fail - không cần thông báo lỗi cho người dùng
                System.err.println("Failed to mark as read: " + ex.getMessage());
            }
        }
    }
}
