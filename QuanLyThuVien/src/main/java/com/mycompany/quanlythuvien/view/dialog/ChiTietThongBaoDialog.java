package com.mycompany.quanlythuvien.view.dialog;

import com.mycompany.quanlythuvien.controller.ThongBaoController;
import com.mycompany.quanlythuvien.model.TaiKhoan;
import com.mycompany.quanlythuvien.model.ThongBaoNguoiNhan;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Dialog chi tiết thông báo
 * @author Tien
 */
public class ChiTietThongBaoDialog extends JDialog {
    
    private final ThongBaoController controller;
    private final TaiKhoan currentUser;
    private final int idThongBao;
    private final String tieuDe;
    
    private JTable tblRecipients;
    private DefaultTableModel tableModel;
    private JLabel lblReadCount, lblUnreadCount;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    public ChiTietThongBaoDialog(Window parent, TaiKhoan currentUser, int idThongBao, String tieuDe) {
        super(parent, "Chi tiết thông báo", ModalityType.APPLICATION_MODAL);
        this.controller = new ThongBaoController();
        this.currentUser = currentUser;
        this.idThongBao = idThongBao;
        this.tieuDe = tieuDe;
        
        initComponents();
        loadData();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setSize(700, 550);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(Color.WHITE);
        
        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setBackground(new Color(0, 123, 255));
        
        JLabel lblTitle = new JLabel("CHI TIẾT THÔNG BÁO #" + idThongBao);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);
        
        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Info Section
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Tiêu đề
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblTieuDeLabel = new JLabel("Tiêu đề:");
        lblTieuDeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTieuDeLabel.setForeground(Color.GRAY);
        infoPanel.add(lblTieuDeLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        JLabel lblTieuDeVal = new JLabel(tieuDe);
        lblTieuDeVal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        infoPanel.add(lblTieuDeVal, gbc);
        
        // Stats
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statsPanel.setBackground(Color.WHITE);
        
        lblReadCount = createBadge("Đã đọc: 0", new Color(220, 255, 220), new Color(40, 167, 69));
        lblUnreadCount = createBadge("Chưa đọc: 0", new Color(255, 220, 220), new Color(220, 53, 69));
        
        statsPanel.add(lblReadCount);
        statsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        statsPanel.add(lblUnreadCount);
        infoPanel.add(statsPanel, gbc);
        
        contentPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Table Section
        JPanel tablePanel = new JPanel(new BorderLayout(0, 5));
        tablePanel.setBackground(Color.WHITE);
        
        JLabel lblList = new JLabel("Danh sách người nhận:");
        lblList.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablePanel.add(lblList, BorderLayout.NORTH);
        
        // Table setup
        String[] columns = {"Email người nhận", "Trạng thái", "Thời gian đọc"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tblRecipients = new JTable(tableModel);
        tblRecipients.setRowHeight(35);
        tblRecipients.setShowVerticalLines(false);
        tblRecipients.setIntercellSpacing(new Dimension(0, 0));
        
        tblRecipients.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblRecipients.getTableHeader().setBackground(new Color(248, 249, 250));
        tblRecipients.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        tblRecipients.getTableHeader().setPreferredSize(new Dimension(0, 35));
        
        tblRecipients.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Column widths
        tblRecipients.getColumnModel().getColumn(0).setPreferredWidth(300);
        tblRecipients.getColumnModel().getColumn(1).setPreferredWidth(100);
        tblRecipients.getColumnModel().getColumn(2).setPreferredWidth(150);
        
        // Custom renderer
        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (column == 1) {
                    if ("Đã đọc".equals(value)) {
                        c.setForeground(new Color(40, 167, 69));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(Color.GRAY);
                        setFont(getFont().deriveFont(Font.PLAIN));
                    }
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        tblRecipients.getColumnModel().getColumn(1).setCellRenderer(statusRenderer);
        
        JScrollPane scrollPane = new JScrollPane(tblRecipients);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        footerPanel.setBackground(new Color(248, 249, 250));
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));
        
        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setBackground(new Color(108, 117, 125));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        footerPanel.add(btnClose);
        
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JLabel createBadge(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(fg);
        lbl.setBackground(bg);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return lbl;
    }
    
    private void loadData() {
        try {
            List<ThongBaoNguoiNhan> recipients = controller.getRecipientsByAnnouncementId(currentUser, idThongBao);
            
            tableModel.setRowCount(0);
            
            int readCount = 0;
            int unreadCount = 0;
            
            for (ThongBaoNguoiNhan r : recipients) {
                String status = r.isDaDoc() ? "Đã đọc" : "Chưa đọc";
                String readTime = r.isDaDoc() && r.getReadAt() != null 
                    ? dateFormat.format(java.sql.Timestamp.valueOf(r.getReadAt()))
                    : "-";
                
                Object[] row = {
                    r.getEmail(),
                    status,
                    readTime
                };
                tableModel.addRow(row);
                
                if (r.isDaDoc()) {
                    readCount++;
                } else {
                    unreadCount++;
                }
            }
            
            lblReadCount.setText("Đã đọc: " + readCount);
            lblUnreadCount.setText("Chưa đọc: " + unreadCount);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Không thể tải danh sách người nhận: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
