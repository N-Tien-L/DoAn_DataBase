package com.mycompany.quanlythuvien.view.panel;

import com.mycompany.quanlythuvien.controller.TaiKhoanController;
import com.mycompany.quanlythuvien.controller.YeuCauResetMKController;
import com.mycompany.quanlythuvien.model.TaiKhoan;
import com.mycompany.quanlythuvien.model.TaiKhoanProfile;
import com.mycompany.quanlythuvien.model.YeuCauResetMK;
import com.mycompany.quanlythuvien.view.dialog.TaiKhoanDialog;
import com.mycompany.quanlythuvien.view.dialog.TaiKhoanProfileDialog;
import com.mycompany.quanlythuvien.view.dialog.YeuCauResetMKDialog;
import com.mycompany.quanlythuvien.view.model.TaiKhoanTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Panel quản lý tài khoản tích hợp Lịch làm việc dạng Grid tuần
 * @author Tien
 */
public class QuanLyTaiKhoanPanel extends JPanel {
    
    // Controller
    private final TaiKhoanController controller;
    private final YeuCauResetMKController resetController;
    private final TaiKhoan currentUser;
    
    // UI Components - Account Management
    private JTable tblTaiKhoan;
    private TaiKhoanTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh, btnResetPassword, btnViewProfile, btnResetRequests;
    private JButton btnPrevious, btnNext;
    private JLabel lblPageInfo, lblTotalRecords, lblPendingRequests;
    
    // UI Components - Schedule Management
    private LichLamViecPanel lichLamViecPanel;
    
    // Cursor-based pagination
    private String currentCursor = null;
    private String lastEmailOnPage = null;
    private final int pageSize = 10;
    private boolean hasNextPage = false;
    private java.util.Stack<String> cursorHistory = new java.util.Stack<>();
    
    public QuanLyTaiKhoanPanel(TaiKhoan currentUser) {
        this.controller = new TaiKhoanController();
        this.resetController = new YeuCauResetMKController();
        this.currentUser = currentUser;
        
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header Panel
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Panel for user management (table + pagination)
        JPanel userManagementPanel = new JPanel(new BorderLayout(0, 10));
        userManagementPanel.add(createTablePanel(), BorderLayout.CENTER);
        userManagementPanel.add(createPaginationPanel(), BorderLayout.SOUTH);

        // Schedule Panel
        lichLamViecPanel = new LichLamViecPanel(currentUser);

        // Main content: user management (top) and schedule (bottom)
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.add(userManagementPanel, BorderLayout.NORTH);
        centerPanel.add(lichLamViecPanel, BorderLayout.CENTER);
        
        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        
        // Title
        JLabel lblTitle = new JLabel("QUẢN LÝ TÀI KHOẢN");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);
        
        // Reset Requests Notification Bar
        JPanel notificationPanel = new JPanel(new BorderLayout(10, 0));
        notificationPanel.setBackground(new Color(255, 243, 205));
        notificationPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 193, 7), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        notificationPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        lblPendingRequests = new JLabel("(!) Có yêu cầu reset mật khẩu đang chờ xử lý");
        lblPendingRequests.setFont(new Font("Arial", Font.BOLD, 15));
        lblPendingRequests.setForeground(new Color(133, 100, 4));
        
        btnResetRequests = new JButton("Xem yêu cầu »");
        btnResetRequests.setFont(new Font("Arial", Font.BOLD, 14));
        btnResetRequests.setBackground(new Color(255, 193, 7));
        btnResetRequests.setForeground(Color.WHITE);
        btnResetRequests.setFocusPainted(false);
        btnResetRequests.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnResetRequests.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnResetRequests.addActionListener(e -> handleViewResetRequests());
        
        notificationPanel.add(lblPendingRequests, BorderLayout.WEST);
        notificationPanel.add(btnResetRequests, BorderLayout.EAST);
        
        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        Font buttonFont = new Font("Arial", Font.PLAIN, 14);
        btnAdd = new JButton("[+] Thêm mới");
        btnAdd.setFont(buttonFont);
        btnEdit = new JButton("[✎] Sửa");
        btnEdit.setFont(buttonFont);
        btnDelete = new JButton("[-] Xóa");
        btnDelete.setFont(buttonFont);
        btnViewProfile = new JButton("[i] Xem chi tiết");
        btnViewProfile.setFont(buttonFont);
        btnResetPassword = new JButton("[…] Cấp lại mật khẩu");
        btnResetPassword.setFont(buttonFont);
        btnRefresh = new JButton("[↻] Làm mới");
        btnRefresh.setFont(buttonFont);
        
        btnAdd.addActionListener(e -> handleAdd());
        btnEdit.addActionListener(e -> handleEdit());
        btnDelete.addActionListener(e -> handleDelete());
        btnViewProfile.addActionListener(e -> handleViewProfile());
        btnResetPassword.addActionListener(e -> handleResetPassword());
        btnRefresh.addActionListener(e -> {
            resetPaginationAndLoad();
            updateResetRequestsNotification();
            lichLamViecPanel.loadWeekSchedule(); // Refresh cả lịch
        });
        
        toolbarPanel.add(btnAdd);
        toolbarPanel.add(btnEdit);
        toolbarPanel.add(btnDelete);
        toolbarPanel.add(btnViewProfile);
        toolbarPanel.add(btnResetPassword);
        toolbarPanel.add(btnRefresh);
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JLabel lblSearch = new JLabel("[⌕] Tìm kiếm:");
        lblSearch.setFont(new Font("Arial", Font.PLAIN, 14));
        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 14));
        JButton btnSearch = new JButton("Tìm");
        btnSearch.setFont(buttonFont);
        
        btnSearch.addActionListener(e -> handleSearch());
        txtSearch.addActionListener(e -> handleSearch());
        
        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        
        // Combine toolbar and search
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.add(toolbarPanel, BorderLayout.WEST);
        actionPanel.add(searchPanel, BorderLayout.EAST);
        
        headerPanel.add(lblTitle);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        headerPanel.add(notificationPanel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        headerPanel.add(actionPanel);
        
        updateResetRequestsNotification();
        
        return headerPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setPreferredSize(new Dimension(0, 300)); // Fix height cho bảng để dành chỗ cho lịch
        
        tableModel = new TaiKhoanTableModel();
        tblTaiKhoan = new JTable(tableModel);
        
        tblTaiKhoan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTaiKhoan.setRowHeight(30);
        tblTaiKhoan.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
        tblTaiKhoan.setFont(new Font("Arial", Font.PLAIN, 14));
        
        tblTaiKhoan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // Renderers
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Highlight current user's row
                String emailAtRow = (String) table.getValueAt(row, 0);
                if (emailAtRow != null && emailAtRow.equals(currentUser.getEmail())) {
                    if (!isSelected) {
                        c.setBackground(new Color(255, 255, 200));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    }
                } else {
                    if (!isSelected) {
                        c.setBackground(Color.WHITE);
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                }

                // Center align the 'Vai trò' column
                if (column == 2) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                
                return c;
            }
        };
        
        tblTaiKhoan.getColumnModel().getColumn(0).setCellRenderer(customRenderer);
        tblTaiKhoan.getColumnModel().getColumn(1).setCellRenderer(customRenderer);
        tblTaiKhoan.getColumnModel().getColumn(2).setCellRenderer(customRenderer);
        
        JScrollPane scrollPane = new JScrollPane(tblTaiKhoan);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }
    
    private JPanel createPaginationPanel() {
        JPanel paginationPanel = new JPanel(new BorderLayout(10, 10));
        paginationPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        lblTotalRecords = new JLabel("Tổng: 0 tài khoản");
        lblTotalRecords.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        Font pageNavFont = new Font("Arial", Font.PLAIN, 14);
        btnPrevious = new JButton("◄ Trước");
        btnPrevious.setFont(pageNavFont);
        btnNext = new JButton("Tiếp ►");
        btnNext.setFont(pageNavFont);
        lblPageInfo = new JLabel("Trang 0/0");
        lblPageInfo.setFont(new Font("Arial", Font.BOLD, 14));
        
        btnPrevious.addActionListener(e -> previousPage());
        btnNext.addActionListener(e -> nextPage());
        
        navPanel.add(btnPrevious);
        navPanel.add(lblPageInfo);
        navPanel.add(btnNext);
        
        paginationPanel.add(lblTotalRecords, BorderLayout.WEST);
        paginationPanel.add(navPanel, BorderLayout.EAST);
        
        return paginationPanel;
    }
    
    private void loadData() {
        try {
            List<TaiKhoan> danhSach = controller.getAllAccounts(currentUser, currentCursor, pageSize + 1);
            
            if (danhSach != null) {
                hasNextPage = danhSach.size() > pageSize;
                if (hasNextPage) {
                    danhSach.remove(danhSach.size() - 1);
                }
                
                if (!danhSach.isEmpty()) {
                    lastEmailOnPage = danhSach.get(danhSach.size() - 1).getEmail();
                }
                
                tableModel.setData(danhSach);
                
                try {
                    int totalRecords = controller.getTotalAccounts(currentUser);
                    int currentPageNum = cursorHistory.size() + 1;
                    int totalPages = controller.getTotalPages(currentUser, pageSize);
                    
                    lblTotalRecords.setText("Tổng: " + totalRecords + " tài khoản");
                    lblPageInfo.setText("Trang " + currentPageNum + "/" + totalPages);
                } catch (Exception e) {
                    int currentPageNum = cursorHistory.size() + 1;
                    lblTotalRecords.setText("Tổng: ? tài khoản");
                    lblPageInfo.setText("Trang " + currentPageNum + "/?");
                }
                
                btnPrevious.setEnabled(!cursorHistory.isEmpty());
                btnNext.setEnabled(hasNextPage);
                updateButtonStates();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Không thể tải danh sách tài khoản: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void resetPaginationAndLoad() {
        currentCursor = null;
        lastEmailOnPage = null;
        hasNextPage = false;
        cursorHistory.clear();
        loadData();
    }
    
    private void previousPage() {
        if (!cursorHistory.isEmpty()) {
            currentCursor = cursorHistory.pop();
            loadData();
        }
    }
    
    private void nextPage() {
        if (hasNextPage) {
            cursorHistory.push(currentCursor);
            currentCursor = lastEmailOnPage;
            loadData();
        }
    }
    
    private void handleAdd() {
        TaiKhoanDialog dialog = new TaiKhoanDialog(
            javax.swing.SwingUtilities.getWindowAncestor(this),
            currentUser
        );
        dialog.setVisible(true);
        
        if (dialog.isSuccess()) {
            resetPaginationAndLoad();
        }
    }
    
    private void handleEdit() {
        int selectedRow = tblTaiKhoan.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản để sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        TaiKhoan selected = tableModel.getTaiKhoanAt(selectedRow);
        TaiKhoanDialog dialog = new TaiKhoanDialog(
            javax.swing.SwingUtilities.getWindowAncestor(this),
            currentUser,
            selected
        );
        dialog.setVisible(true);
        
        if (dialog.isSuccess()) {
            loadData();
        }
    }
    
    private void handleDelete() {
        int selectedRow = tblTaiKhoan.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        TaiKhoan selected = tableModel.getTaiKhoanAt(selectedRow);
        
        if (selected.getEmail().equals(currentUser.getEmail())) {
            JOptionPane.showMessageDialog(this, "Bạn không thể xóa tài khoản của chính mình!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa tài khoản:\n" + selected.getEmail(),
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = controller.deleteAccount(currentUser, selected.getEmail());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Xóa tài khoản thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    resetPaginationAndLoad();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Xóa tài khoản thất bại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleViewProfile() {
        int selectedRow = tblTaiKhoan.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tài khoản!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        TaiKhoan selected = tableModel.getTaiKhoanAt(selectedRow);
        try {
            TaiKhoanProfile profile = controller.getAccountProfile(currentUser, selected.getEmail());
            if (profile != null && profile.getEmail() != null) {
                TaiKhoanProfileDialog dialog = new TaiKhoanProfileDialog(
                        javax.swing.SwingUtilities.getWindowAncestor(this), profile);
                dialog.setVisible(true);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handleResetPassword() {
        int selectedRow = tblTaiKhoan.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        TaiKhoan selected = tableModel.getTaiKhoanAt(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Cấp lại mật khẩu cho: " + selected.getEmail() + "?",
            "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = controller.resetPassword(currentUser, selected.getEmail());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Cấp lại mật khẩu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            resetPaginationAndLoad();
            return;
        }
        
        currentCursor = null;
        lastEmailOnPage = null;
        hasNextPage = false;
        cursorHistory.clear();
        
        try {
            List<TaiKhoan> danhSach = controller.searchAccounts(currentUser, keyword, currentCursor, pageSize + 1);
            if (danhSach != null) {
                hasNextPage = danhSach.size() > pageSize;
                if (hasNextPage) danhSach.remove(danhSach.size() - 1);
                if (!danhSach.isEmpty()) lastEmailOnPage = danhSach.get(danhSach.size() - 1).getEmail();
                
                tableModel.setData(danhSach);
                lblTotalRecords.setText("Tìm thấy: " + danhSach.size() + " kết quả");
                lblPageInfo.setText("Trang 1/?");
                btnPrevious.setEnabled(false);
                btnNext.setEnabled(hasNextPage);
                updateButtonStates();
                
                if (danhSach.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy kết quả nào.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateButtonStates() {
        int selectedRow = tblTaiKhoan.getSelectedRow();
        boolean hasSelection = selectedRow >= 0;
        
        btnEdit.setEnabled(hasSelection);
        btnViewProfile.setEnabled(hasSelection);
        btnResetPassword.setEnabled(hasSelection);
        
        if (hasSelection) {
            TaiKhoan selected = tableModel.getTaiKhoanAt(selectedRow);
            boolean isCurrentUser = selected != null && selected.getEmail().equals(currentUser.getEmail());
            btnDelete.setEnabled(!isCurrentUser);
        } else {
            btnDelete.setEnabled(false);
        }
    }
    
    private void handleViewResetRequests() {
        YeuCauResetMKDialog dialog = new YeuCauResetMKDialog(
            javax.swing.SwingUtilities.getWindowAncestor(this),
            currentUser
        );
        dialog.setVisible(true);
        updateResetRequestsNotification();
    }
    
    private void updateResetRequestsNotification() {
        try {
            List<YeuCauResetMK> pending = resetController.getPendingYeuCau(currentUser);
            if (pending != null && !pending.isEmpty()) {
                lblPendingRequests.setText("(!) Có " + pending.size() + " yêu cầu reset mật khẩu đang chờ xử lý");
                lblPendingRequests.getParent().setVisible(true);
            } else {
                lblPendingRequests.getParent().setVisible(false);
            }
        } catch (Exception e) {
            lblPendingRequests.getParent().setVisible(false);
        }
    }
}