package com.mycompany.quanlythuvien.view.panel;

import com.mycompany.quanlythuvien.controller.ThongBaoController;
import com.mycompany.quanlythuvien.model.TaiKhoan;
import com.mycompany.quanlythuvien.model.ThongBaoListResult;
import com.mycompany.quanlythuvien.model.ThongBaoNguoiNhan;
import com.mycompany.quanlythuvien.view.dialog.ChiTietThongBaoNguoiDungDialog;
import java.awt.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Panel thông báo cá nhân
 * @author Tien
 */
public class ThongBaoNguoiDungPanel extends JPanel {
    
    private final ThongBaoController controller;
    private final TaiKhoan currentUser;
    
    // UI Components
    private JPanel pnlNotificationList;
    private JButton btnRefresh, btnMarkAllRead;
    private JButton btnPrevious, btnNext;
    private JLabel lblPageInfo, lblUnreadCount;
    private JCheckBox chkUnreadOnly;
    
    // Pagination
    private Integer currentCursor = null;
    private Integer nextCursor = null; // Store next cursor to avoid re-fetching
    private final int pageSize = 10;
    private boolean hasNextPage = false;
    private java.util.Stack<Integer> cursorHistory = new java.util.Stack<>();
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    public ThongBaoNguoiDungPanel(TaiKhoan currentUser) {
        this.controller = new ThongBaoController();
        this.currentUser = currentUser;
        
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Notification list (scrollable)
        pnlNotificationList = new JPanel();
        pnlNotificationList.setLayout(new BoxLayout(pnlNotificationList, BoxLayout.Y_AXIS));
        pnlNotificationList.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(pnlNotificationList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Footer with pagination
        add(createFooterPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Title
        JLabel lblTitle = new JLabel("Thông Báo Của Tôi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(0, 123, 255));
        panel.add(lblTitle, BorderLayout.WEST);
        
        // Right panel with controls
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(Color.WHITE);
        
        lblUnreadCount = new JLabel("Chưa đọc: 0");
        lblUnreadCount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUnreadCount.setForeground(new Color(220, 53, 69));
        rightPanel.add(lblUnreadCount);
        
        chkUnreadOnly = new JCheckBox("Chỉ chưa đọc");
        chkUnreadOnly.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkUnreadOnly.setBackground(Color.WHITE);
        chkUnreadOnly.setFocusPainted(false);
        chkUnreadOnly.addActionListener(e -> {
            currentCursor = null;
            cursorHistory.clear();
            loadData();
        });
        rightPanel.add(chkUnreadOnly);
        
        btnMarkAllRead = new JButton("Đánh dấu tất cả đã đọc");
        styleButton(btnMarkAllRead, new Color(23, 162, 184));
        btnMarkAllRead.addActionListener(e -> handleMarkAllRead());
        rightPanel.add(btnMarkAllRead);
        
        btnRefresh = new JButton("Làm mới");
        styleButton(btnRefresh, new Color(40, 167, 69));
        btnRefresh.addActionListener(e -> {
            currentCursor = null;
            cursorHistory.clear();
            loadData();
        });
        rightPanel.add(btnRefresh);
        
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void styleButton(JButton btn, Color color) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        panel.setBackground(Color.WHITE);
        
        btnPrevious = new JButton("◄ Trang trước");
        stylePaginationButton(btnPrevious);
        btnPrevious.setEnabled(false);
        btnPrevious.addActionListener(e -> loadPreviousPage());
        panel.add(btnPrevious);
        
        lblPageInfo = new JLabel("Trang 1");
        lblPageInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(lblPageInfo);
        
        btnNext = new JButton("Trang sau ►");
        stylePaginationButton(btnNext);
        btnNext.setEnabled(false);
        btnNext.addActionListener(e -> loadNextPage());
        panel.add(btnNext);
        
        return panel;
    }
    
    private void stylePaginationButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void loadData() {
        try {
            // Disable buttons during load
            btnRefresh.setEnabled(false);
            btnMarkAllRead.setEnabled(false);
            
            // Get unread count
            int unreadCount = controller.getUnreadCount(currentUser);
            lblUnreadCount.setText("Chưa đọc: " + unreadCount);
            
            // Load notifications
            ThongBaoListResult result = controller.listByReceiver(
                currentUser, 
                chkUnreadOnly.isSelected(), 
                currentCursor, 
                pageSize
            );
            
            hasNextPage = result.isHasMore();
            this.nextCursor = result.getNextCursor(); // Store for next page
            
            // Update pagination UI
            btnPrevious.setEnabled(!cursorHistory.isEmpty());
            btnNext.setEnabled(hasNextPage);
            lblPageInfo.setText("Trang " + (cursorHistory.size() + 1));
            
            // Clear and rebuild notification list
            pnlNotificationList.removeAll();
            
            if (result.getItems().isEmpty()) {
                JLabel lblEmpty = new JLabel(chkUnreadOnly.isSelected() ? 
                    "Không có thông báo chưa đọc" : "Chưa có thông báo nào");
                lblEmpty.setFont(new Font("Arial", Font.ITALIC, 14));
                lblEmpty.setForeground(Color.GRAY);
                lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
                lblEmpty.setBorder(new EmptyBorder(50, 0, 50, 0));
                pnlNotificationList.add(lblEmpty);
            } else {
                for (ThongBaoNguoiNhan notification : result.getItems()) {
                    pnlNotificationList.add(createNotificationCard(notification));
                    pnlNotificationList.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
            
            pnlNotificationList.revalidate();
            pnlNotificationList.repaint();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Lỗi tải thông báo: " + ex.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            btnRefresh.setEnabled(true);
            btnMarkAllRead.setEnabled(true);
        }
    }
    
    private JPanel createNotificationCard(ThongBaoNguoiNhan notification) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(15, 10));
        card.setBackground(notification.isDaDoc() ? Color.WHITE : new Color(240, 248, 255));
        
        // Modern border with left accent color
        Color accentColor = notification.isDaDoc() ? new Color(200, 200, 200) : new Color(0, 123, 255);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(15, 20, 15, 20)
            )
        ));
        
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Left: Icon
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        // Use a simple circle or checkmark
        JLabel lblIcon = new JLabel(notification.isDaDoc() ? "✓" : "●");
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblIcon.setForeground(accentColor);
        leftPanel.add(lblIcon, BorderLayout.NORTH);
        card.add(leftPanel, BorderLayout.WEST);
        
        // Center: Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        // Title
        JLabel lblTitle = new JLabel(notification.getTieuDe());
        lblTitle.setFont(new Font("Segoe UI", notification.isDaDoc() ? Font.PLAIN : Font.BOLD, 16));
        lblTitle.setForeground(new Color(33, 37, 41));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblTitle);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Preview text
        String preview = notification.getNoiDung();
        if (preview.length() > 120) {
            preview = preview.substring(0, 117) + "...";
        }
        JLabel lblPreview = new JLabel(preview);
        lblPreview.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPreview.setForeground(new Color(108, 117, 125));
        lblPreview.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblPreview);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        // Date
        String dateStr = "";
        if (notification.getCreatedAt() != null) {
            dateStr = dateFormat.format(java.sql.Timestamp.valueOf(notification.getCreatedAt()));
        }
        JLabel lblDate = new JLabel(dateStr);
        lblDate.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblDate.setForeground(new Color(150, 150, 150));
        lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(lblDate);
        
        card.add(contentPanel, BorderLayout.CENTER);
        
        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                handleViewDetail(notification);
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (notification.isDaDoc()) {
                    card.setBackground(new Color(248, 249, 250));
                } else {
                    card.setBackground(new Color(235, 245, 255));
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(notification.isDaDoc() ? Color.WHITE : new Color(240, 248, 255));
            }
        });
        
        return card;
    }
    
    private void handleViewDetail(ThongBaoNguoiNhan notification) {
        try {
            ChiTietThongBaoNguoiDungDialog dialog = new ChiTietThongBaoNguoiDungDialog(
                SwingUtilities.getWindowAncestor(this),
                notification,
                currentUser,
                controller
            );
            dialog.setVisible(true);
            
            // Refresh list after viewing (might be marked as read)
            loadData();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Lỗi: " + ex.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleMarkAllRead() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Đánh dấu tất cả thông báo là đã đọc?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.markAllRead(currentUser);
                JOptionPane.showMessageDialog(this,
                    "Đã đánh dấu tất cả thông báo là đã đọc",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Lỗi: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void loadNextPage() {
        if (hasNextPage && nextCursor != null) {
            cursorHistory.push(currentCursor);
            currentCursor = nextCursor;
            loadData();
        }
    }
    
    private void loadPreviousPage() {
        if (!cursorHistory.isEmpty()) {
            currentCursor = cursorHistory.pop();
            loadData();
        }
    }
}
