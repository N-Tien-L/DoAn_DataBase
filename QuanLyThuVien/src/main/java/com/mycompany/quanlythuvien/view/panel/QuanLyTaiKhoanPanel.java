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
    
    // UI Components
    private JTable tblTaiKhoan;
    private TaiKhoanTableModel tableModel;
    private JTextField txtSearch;
    private JButton btnAdd, btnEdit, btnDelete, btnRefresh, btnResetPassword;
    private JButton btnPrevious, btnNext;
    private JLabel lblPageInfo, lblTotalRecords;
    
    // Pagination
    private int currentPage = 1;
    private final int pageSize = 10;
    private int totalPages = 0;
    
    public QuanLyTaiKhoanPanel(String currentUserRole) {
        this.controller = new TaiKhoanController();
        this.currentUserRole = currentUserRole;
        
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
        btnRefresh.addActionListener(e -> loadData());
        
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
        // Load data from controller
        List<TaiKhoan> danhSach = controller.getAllAccounts(currentUserRole, currentPage, pageSize);
        
        if (danhSach != null) {
            tableModel.setData(danhSach);
            
            // Update pagination info
            int totalRecords = controller.getTotalAccounts(currentUserRole);
            totalPages = controller.getTotalPages(currentUserRole, pageSize);
            
            lblTotalRecords.setText("Tổng: " + totalRecords + " tài khoản");
            lblPageInfo.setText("Trang " + currentPage + "/" + (totalPages > 0 ? totalPages : 1));
            
            // Enable/disable navigation buttons
            btnPrevious.setEnabled(currentPage > 1);
            btnNext.setEnabled(currentPage < totalPages);
            
            // Enable/disable action buttons based on selection
            updateButtonStates();
        } else {
            JOptionPane.showMessageDialog(this,
                "Không thể tải danh sách tài khoản!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadData();
        }
    }
    
    private void nextPage() {
        if (currentPage < totalPages) {
            currentPage++;
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
            loadData(); // Refresh table nếu thêm thành công
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
                loadData(); // Refresh table
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
            loadData(); // Load all if search is empty
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
        boolean hasSelection = tblTaiKhoan.getSelectedRow() >= 0;
        btnEdit.setEnabled(hasSelection);
        btnDelete.setEnabled(hasSelection);
        btnResetPassword.setEnabled(hasSelection);
    }
}
