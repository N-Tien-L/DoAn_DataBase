package com.mycompany.quanlythuvien.view.panel;

import com.mycompany.quanlythuvien.controller.ThongBaoController;
import com.mycompany.quanlythuvien.model.TaiKhoan;
import com.mycompany.quanlythuvien.model.ThongBaoAdmin;
import com.mycompany.quanlythuvien.model.ThongBaoAdminListResult;
import com.mycompany.quanlythuvien.view.dialog.TaoThongBaoDialog;
import com.mycompany.quanlythuvien.view.dialog.ChiTietThongBaoDialog;
import java.awt.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Panel admin quản lý thông báo
 * @author Tien
 */
public class QuanLyThongBaoPanel extends JPanel {
    
    private final ThongBaoController controller;
    private final TaiKhoan currentUser;
    
    // UI Components
    private JTable tblThongBao;
    private DefaultTableModel tableModel;
    private JButton btnCreate, btnViewDetail, btnRefresh;
    private JButton btnPrevious, btnNext;
    private JLabel lblPageInfo, lblTotalCount;
    
    // Pagination
    private Integer currentCursor = null;
    private final int pageSize = 15;
    private boolean hasNextPage = false;
    private java.util.Stack<Integer> cursorHistory = new java.util.Stack<>();
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    public QuanLyThongBaoPanel(TaiKhoan currentUser) {
        this.controller = new ThongBaoController();
        this.currentUser = currentUser;
        
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);
        
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createPaginationPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel lblTitle = new JLabel("QUẢN LÝ THÔNG BÁO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(0, 123, 255));
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);
        
        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        toolbarPanel.setBackground(Color.WHITE);
        
        btnCreate = new JButton("Tạo thông báo mới");
        btnViewDetail = new JButton("Xem chi tiết");
        btnRefresh = new JButton("Làm mới");
        
        styleButton(btnCreate, new Color(0, 123, 255));
        styleButton(btnViewDetail, new Color(108, 117, 125));
        styleButton(btnRefresh, new Color(40, 167, 69));
        
        btnViewDetail.setEnabled(false);
        btnViewDetail.addActionListener(e -> handleViewDetail());
        btnCreate.addActionListener(e -> handleCreate());
        btnRefresh.addActionListener(e -> resetPaginationAndLoad());
        
        toolbarPanel.add(btnCreate);
        toolbarPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        toolbarPanel.add(btnViewDetail);
        toolbarPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        toolbarPanel.add(btnRefresh);
        toolbarPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        headerPanel.add(lblTitle);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        headerPanel.add(toolbarPanel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        return headerPanel;
    }
    
    private void styleButton(JButton btn, Color color) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        
        // Table model
        String[] columns = {"ID", "Tiêu đề", "Ngày tạo", "Người tạo", "Người nhận", "Đã đọc", "Chưa đọc"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tblThongBao = new JTable(tableModel);
        tblThongBao.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblThongBao.setRowHeight(40);
        tblThongBao.setShowVerticalLines(false);
        tblThongBao.setIntercellSpacing(new Dimension(0, 0));
        
        // Header styling
        tblThongBao.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tblThongBao.getTableHeader().setBackground(new Color(248, 249, 250));
        tblThongBao.getTableHeader().setForeground(new Color(73, 80, 87));
        tblThongBao.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(222, 226, 230)));
        tblThongBao.getTableHeader().setPreferredSize(new Dimension(0, 40));
        
        // Cell styling
        tblThongBao.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblThongBao.setSelectionBackground(new Color(231, 241, 255));
        tblThongBao.setSelectionForeground(Color.BLACK);
        
        // Column widths
        tblThongBao.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tblThongBao.getColumnModel().getColumn(1).setPreferredWidth(300); // Tiêu đề
        tblThongBao.getColumnModel().getColumn(2).setPreferredWidth(140); // Ngày
        tblThongBao.getColumnModel().getColumn(3).setPreferredWidth(150); // Người tạo
        tblThongBao.getColumnModel().getColumn(4).setPreferredWidth(100);  // Người nhận
        tblThongBao.getColumnModel().getColumn(5).setPreferredWidth(80);  // Đã đọc
        tblThongBao.getColumnModel().getColumn(6).setPreferredWidth(80);  // Chưa đọc
        
        // Center align for number columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblThongBao.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblThongBao.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tblThongBao.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        tblThongBao.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        
        // Selection listener
        tblThongBao.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnViewDetail.setEnabled(tblThongBao.getSelectedRow() >= 0);
                if (tblThongBao.getSelectedRow() >= 0) {
                    btnViewDetail.setBackground(new Color(0, 123, 255)); // Active color
                } else {
                    btnViewDetail.setBackground(new Color(108, 117, 125)); // Disabled color
                }
            }
        });
        
        // Double click to view detail
        tblThongBao.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && tblThongBao.getSelectedRow() >= 0) {
                    handleViewDetail();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tblThongBao);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private JPanel createPaginationPanel() {
        JPanel paginationPanel = new JPanel(new BorderLayout(10, 10));
        paginationPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        paginationPanel.setBackground(Color.WHITE);
        
        lblTotalCount = new JLabel("Tổng: 0 thông báo");
        lblTotalCount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTotalCount.setForeground(Color.GRAY);
        
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navPanel.setBackground(Color.WHITE);
        
        btnPrevious = new JButton("◄ Trước");
        btnNext = new JButton("Tiếp ►");
        lblPageInfo = new JLabel("Trang 1");
        lblPageInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        stylePaginationButton(btnPrevious);
        stylePaginationButton(btnNext);
        
        btnPrevious.addActionListener(e -> previousPage());
        btnNext.addActionListener(e -> nextPage());
        
        navPanel.add(btnPrevious);
        navPanel.add(lblPageInfo);
        navPanel.add(btnNext);
        
        paginationPanel.add(lblTotalCount, BorderLayout.WEST);
        paginationPanel.add(navPanel, BorderLayout.EAST);
        
        return paginationPanel;
    }
    
    private void stylePaginationButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private void loadData() {
        try {
            ThongBaoAdminListResult result = controller.listAllForAdmin(currentUser, currentCursor, pageSize);
            
            if (result != null) {
                tableModel.setRowCount(0);
                
                for (ThongBaoAdmin tb : result.getItems()) {
                    Object[] row = {
                        tb.getIdThongBao(),
                        tb.getTieuDe(),
                        tb.getCreatedAt() != null ? dateFormat.format(tb.getCreatedAt()) : "N/A",
                        tb.getCreatedBy(),
                        tb.getRecipientCount(),
                        tb.getReadCount(),
                        tb.getUnreadCount()
                    };
                    tableModel.addRow(row);
                }
                
                hasNextPage = result.isHasMore();
                
                int currentPageNum = cursorHistory.size() + 1;
                lblPageInfo.setText("Trang " + currentPageNum);
                lblTotalCount.setText("Hiển thị " + result.getItems().size() + " thông báo");
                
                btnPrevious.setEnabled(!cursorHistory.isEmpty());
                btnNext.setEnabled(hasNextPage);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Không thể tải danh sách thông báo: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void resetPaginationAndLoad() {
        currentCursor = null;
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
            
            // Get last ID from current page
            int lastRow = tableModel.getRowCount() - 1;
            if (lastRow >= 0) {
                currentCursor = (Integer) tableModel.getValueAt(lastRow, 0);
            }
            
            loadData();
        }
    }
    
    private void handleCreate() {
        TaoThongBaoDialog dialog = new TaoThongBaoDialog(
            SwingUtilities.getWindowAncestor(this),
            currentUser
        );
        dialog.setVisible(true);
        
        if (dialog.isSuccess()) {
            resetPaginationAndLoad();
        }
    }
    
    private void handleViewDetail() {
        int selectedRow = tblThongBao.getSelectedRow();
        if (selectedRow < 0) return;
        
        int idThongBao = (Integer) tableModel.getValueAt(selectedRow, 0);
        String tieuDe = (String) tableModel.getValueAt(selectedRow, 1);
        
        ChiTietThongBaoDialog dialog = new ChiTietThongBaoDialog(
            SwingUtilities.getWindowAncestor(this),
            currentUser,
            idThongBao,
            tieuDe
        );
        dialog.setVisible(true);
    }
}
