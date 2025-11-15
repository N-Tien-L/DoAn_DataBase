package com.mycompany.quanlythuvien.view.panel;

import com.mycompany.quanlythuvien.controller.TaiKhoanController;
import com.mycompany.quanlythuvien.model.TaiKhoan;
import com.mycompany.quanlythuvien.view.dialog.TaiKhoanDialog;
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
 * Panel quản lý tài khoản
 * @author Tien
 */
public class QuanLyTaiKhoanPanel extends JPanel {
    
    // Controller
    private final TaiKhoanController controller;
    private final String currentUserRole;
    private final String currentUserEmail;
    
    // UI Components
    private JTable tblTaiKhoan;
    private TaiKhoanTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh, btnResetPassword;
    private JButton btnPrevious, btnNext;
    private JLabel lblPageInfo, lblTotalRecords;
    
    // Cursor-based pagination
    private String currentCursor = null; // Cursor used to load the current page
    private String lastEmailOnPage = null; // Email of last item on current page (becomes cursor for next page)
    private final int pageSize = 10;
    private boolean hasNextPage = false;
    private java.util.Stack<String> cursorHistory = new java.util.Stack<>(); // Stack to track cursor for each page
    
    public QuanLyTaiKhoanPanel(String currentUserRole, String currentUserEmail) {
        this.controller = new TaiKhoanController();
        this.currentUserRole = currentUserRole;
        this.currentUserEmail = currentUserEmail;
        
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header Panel
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Table Panel
        add(createTablePanel(), BorderLayout.CENTER);
        
        // Pagination Panel
        add(createPaginationPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        
        // Title
        JLabel lblTitle = new JLabel("QUẢN LÝ TÀI KHOẢN");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);
        
        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        btnAdd = new JButton("➡️ Thêm mới");
        btnEdit = new JButton("✏️ Sửa");
        btnDelete = new JButton("🗑️ Xóa");
        btnResetPassword = new JButton("🔐 Cấp lại mật khẩu");
        btnRefresh = new JButton("🔄 Làm mới");
        
        btnAdd.addActionListener(e -> handleAdd());
        btnEdit.addActionListener(e -> handleEdit());
        btnDelete.addActionListener(e -> handleDelete());
        btnResetPassword.addActionListener(e -> handleResetPassword());
        btnRefresh.addActionListener(e -> resetPaginationAndLoad());
        
        toolbarPanel.add(btnAdd);
        toolbarPanel.add(btnEdit);
        toolbarPanel.add(btnDelete);
        toolbarPanel.add(btnResetPassword);
        toolbarPanel.add(btnRefresh);
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JLabel lblSearch = new JLabel("🔍 Tìm kiếm:");
        txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Tìm");
        
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
        headerPanel.add(actionPanel);
        
        return headerPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        
        // Create table model and table
        tableModel = new TaiKhoanTableModel();
        tblTaiKhoan = new JTable(tableModel);
        
        // Table settings
        tblTaiKhoan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTaiKhoan.setRowHeight(30);
        tblTaiKhoan.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        tblTaiKhoan.setFont(new Font("Arial", Font.PLAIN, 13));
        
        // Add selection listener to update button states
        tblTaiKhoan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // Center align for Role column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblTaiKhoan.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        
        // Custom renderer to highlight current user row
        DefaultTableCellRenderer highlightRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                String emailAtRow = (String) table.getValueAt(row, 0);
                if (emailAtRow != null && emailAtRow.equals(currentUserEmail)) {
                    if (!isSelected) {
                        c.setBackground(new Color(255, 255, 200)); // Light yellow
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    }
                } else {
                    if (!isSelected) {
                        c.setBackground(Color.WHITE);
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                }
                return c;
            }
        };
        
        // Apply highlight renderer to all columns
        tblTaiKhoan.getColumnModel().getColumn(0).setCellRenderer(highlightRenderer);
        tblTaiKhoan.getColumnModel().getColumn(1).setCellRenderer(highlightRenderer);
        
        // For Role column, combine center alignment with highlighting
        DefaultTableCellRenderer centerHighlightRenderer = new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                
                String emailAtRow = (String) table.getValueAt(row, 0);
                if (emailAtRow != null && emailAtRow.equals(currentUserEmail)) {
                    if (!isSelected) {
                        c.setBackground(new Color(255, 255, 200)); // Light yellow
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    }
                } else {
                    if (!isSelected) {
                        c.setBackground(Color.WHITE);
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                }
                return c;
            }
        };
        tblTaiKhoan.getColumnModel().getColumn(2).setCellRenderer(centerHighlightRenderer);
        
        // Column widths
        tblTaiKhoan.getColumnModel().getColumn(0).setPreferredWidth(250); // Email
        tblTaiKhoan.getColumnModel().getColumn(1).setPreferredWidth(200); // Họ tên
        tblTaiKhoan.getColumnModel().getColumn(2).setPreferredWidth(100); // Vai trò
        
        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(tblTaiKhoan);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private JPanel createPaginationPanel() {
        JPanel paginationPanel = new JPanel(new BorderLayout(10, 10));
        paginationPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        // Total records label (left)
        lblTotalRecords = new JLabel("Tổng: 0 tài khoản");
        lblTotalRecords.setFont(new Font("Arial", Font.PLAIN, 13));
        
        // Pagination controls (right)
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        btnPrevious = new JButton("◄ Trước");
        btnNext = new JButton("Tiếp ►");
        lblPageInfo = new JLabel("Trang 0/0");
        lblPageInfo.setFont(new Font("Arial", Font.BOLD, 13));
        
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
        List<TaiKhoan> danhSach = controller.getAllAccounts(currentUserRole, currentCursor, pageSize + 1);
        
        if (danhSach != null) {
            // Check if there's a next page
            hasNextPage = danhSach.size() > pageSize;
            
            // If we got more than pageSize, remove the extra one (it's just for checking hasNext)
            if (hasNextPage) {
                danhSach.remove(danhSach.size() - 1);
            }
            
            // Store last email for next page navigation
            if (!danhSach.isEmpty()) {
                lastEmailOnPage = danhSach.get(danhSach.size() - 1).getEmail();
            }
            
            tableModel.setData(danhSach);
            
            // Update pagination info
            int totalRecords = controller.getTotalAccounts(currentUserRole);
            int currentPageNum = cursorHistory.size() + 1;
            
            lblTotalRecords.setText("Tổng: " + totalRecords + " tài khoản");
            lblPageInfo.setText("Trang " + currentPageNum + "/" + controller.getTotalPages(currentUserRole, pageSize));
            
            // Enable/disable navigation buttons
            btnPrevious.setEnabled(!cursorHistory.isEmpty());
            btnNext.setEnabled(hasNextPage);
            
            // Enable/disable action buttons based on selection
            updateButtonStates();
        } else {
            JOptionPane.showMessageDialog(this,
                "Không thể tải danh sách tài khoản!",
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
            currentUserRole
        );
        dialog.setVisible(true);
        
        if (dialog.isSuccess()) {
            resetPaginationAndLoad();
        }
    }
    
    private void handleEdit() {
        int selectedRow = tblTaiKhoan.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn một tài khoản để sửa!",
                "Cảnh báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        TaiKhoan selected = tableModel.getTaiKhoanAt(selectedRow);
        TaiKhoanDialog dialog = new TaiKhoanDialog(
            javax.swing.SwingUtilities.getWindowAncestor(this),
            currentUserRole,
            selected
        );
        dialog.setVisible(true);
        
        if (dialog.isSuccess()) {
            loadData(); // Refresh table nếu sửa thành công
        }
    }
    
    private void handleDelete() {
        int selectedRow = tblTaiKhoan.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn một tài khoản để xóa!",
                "Cảnh báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        TaiKhoan selected = tableModel.getTaiKhoanAt(selectedRow);
        
        // Prevent deleting own account
        if (selected.getEmail().equals(currentUserEmail)) {
            JOptionPane.showMessageDialog(this,
                "Bạn không thể xóa tài khoản của chính mình!",
                "Cảnh báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Xác nhận xóa
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn xóa tài khoản:\n" +
            "Email: " + selected.getEmail() + "\n" +
            "Họ tên: " + selected.getHoTen() + "?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = controller.deleteAccount(currentUserRole, selected.getEmail());
            
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Xóa tài khoản thành công!",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
                resetPaginationAndLoad();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Xóa tài khoản thất bại!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleResetPassword() {
        int selectedRow = tblTaiKhoan.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn một tài khoản để cấp lại mật khẩu!",
                "Cảnh báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        TaiKhoan selected = tableModel.getTaiKhoanAt(selectedRow);
        
        // Xác nhận reset
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc chắn muốn cấp lại mật khẩu cho tài khoản:\n" +
            "Email: " + selected.getEmail() + "\n" +
            "Họ tên: " + selected.getHoTen() + "?\n\n" +
            "Mật khẩu mới (6 số) sẽ được gửi qua email.",
            "Xác nhận cấp lại mật khẩu",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = controller.resetPassword(currentUserRole, selected.getEmail());
            
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Cấp lại mật khẩu thành công!\n" +
                    "Mật khẩu mới đã được gửi đến email: " + selected.getEmail(),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Cấp lại mật khẩu thất bại!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        
        if (keyword.isEmpty()) {
            resetPaginationAndLoad();
            return;
        }
        
        // TODO: Implement search in controller
        JOptionPane.showMessageDialog(this,
            "Chức năng tìm kiếm sẽ được implement sau\n" +
            "Từ khóa: " + keyword,
            "Thông báo",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updateButtonStates() {
        int selectedRow = tblTaiKhoan.getSelectedRow();
        boolean hasSelection = selectedRow >= 0;
        
        btnEdit.setEnabled(hasSelection);
        btnResetPassword.setEnabled(hasSelection);
        
        // Disable delete if current user is selected
        if (hasSelection) {
            TaiKhoan selected = tableModel.getTaiKhoanAt(selectedRow);
            boolean isCurrentUser = selected != null && selected.getEmail().equals(currentUserEmail);
            btnDelete.setEnabled(!isCurrentUser);
        } else {
            btnDelete.setEnabled(false);
        }
    }
}
