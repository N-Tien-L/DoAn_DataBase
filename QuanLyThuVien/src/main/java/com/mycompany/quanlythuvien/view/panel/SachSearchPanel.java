package com.mycompany.quanlythuvien.view.panel;

import com.mycompany.quanlythuvien.controller.NhaXuatBanController;
import com.mycompany.quanlythuvien.controller.TacGiaController;
import com.mycompany.quanlythuvien.controller.TheLoaiController;
import com.mycompany.quanlythuvien.model.NhaXuatBan;
import com.mycompany.quanlythuvien.model.TacGia;
import com.mycompany.quanlythuvien.model.TheLoai;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Panel tìm kiếm sách với các tiêu chí lọc
 */
public class SachSearchPanel extends JPanel {
    
    // UI Components
    private JLabel lblTimKiem;
    private JTextField txtTimKiem;
    private JLabel lblTheLoai;
    private JComboBox<Object> cboLocTheLoai;
    private JLabel lblNXB;
    private JComboBox<Object> cboLocNXB;
    private JLabel lblTacGia;
    private JComboBox<Object> cboLocTacGia;
    private JLabel lblNam;
    private JTextField txtNamBatDau;
    private JLabel lblDash;
    private JTextField txtNamKetThuc;
    private JButton btnTim;
    
    // Controllers
    private TacGiaController tacGiaController;
    private NhaXuatBanController nxbController;
    private TheLoaiController theLoaiController;
    
    // Search listener
    private SearchListener searchListener;
    
    public SachSearchPanel() {
        tacGiaController = new TacGiaController();
        nxbController = new NhaXuatBanController();
        theLoaiController = new TheLoaiController();
        
        initComponents();
        loadComboBoxData();
    }
    
    private void initComponents() {
        // Sử dụng GridBagLayout để căn chỉnh đẹp hơn
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        
        // Padding giữa các component
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

        // --- Dòng 1: Từ khóa & Nút tìm ---
        
        // Label Từ khóa
        lblTimKiem = new JLabel("Từ khóa:");
        lblTimKiem.setFont(new java.awt.Font("Arial Unicode MS", 1, 14));
        gbc.gridx = 0; 
        gbc.gridy = 0;
        gbc.weightx = 0;
        add(lblTimKiem, gbc);
        
        // Text Field Từ khóa
        txtTimKiem = new JTextField();
        txtTimKiem.setFont(new java.awt.Font("Arial Unicode MS", 0, 14));
        txtTimKiem.setPreferredSize(new java.awt.Dimension(300, 30));
        txtTimKiem.addActionListener(e -> performSearch());
        gbc.gridx = 1; 
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Chiếm 2 cột
        gbc.weightx = 1.0; // Giãn ra
        add(txtTimKiem, gbc);
        
        // Button Tìm kiếm
        btnTim = new JButton("Tìm kiếm");
        btnTim.setFont(new java.awt.Font("Arial Unicode MS", 1, 14));
        btnTim.setBackground(new java.awt.Color(0, 102, 204));
        btnTim.setForeground(java.awt.Color.WHITE);
        btnTim.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnTim.addActionListener(e -> performSearch());
        gbc.gridx = 3; 
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        add(btnTim, gbc);

        // --- Dòng 2: Bộ lọc (Thể loại, Tác giả) ---
        
        // Label Thể loại
        lblTheLoai = new JLabel("Thể loại:");
        lblTheLoai.setFont(new java.awt.Font("Arial Unicode MS", 0, 14));
        gbc.gridx = 0; 
        gbc.gridy = 1;
        gbc.weightx = 0;
        add(lblTheLoai, gbc);
        
        // Combo Thể loại
        cboLocTheLoai = new JComboBox<>();
        cboLocTheLoai.setFont(new java.awt.Font("Arial Unicode MS", 0, 14));
        cboLocTheLoai.setPreferredSize(new java.awt.Dimension(150, 30));
        gbc.gridx = 1; 
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        add(cboLocTheLoai, gbc);
        
        // Label Tác giả
        lblTacGia = new JLabel("Tác giả:");
        lblTacGia.setFont(new java.awt.Font("Arial Unicode MS", 0, 14));
        gbc.gridx = 2; 
        gbc.gridy = 1;
        gbc.weightx = 0;
        add(lblTacGia, gbc);
        
        // Combo Tác giả
        cboLocTacGia = new JComboBox<>();
        cboLocTacGia.setFont(new java.awt.Font("Arial Unicode MS", 0, 14));
        cboLocTacGia.setPreferredSize(new java.awt.Dimension(150, 30));
        gbc.gridx = 3; 
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        add(cboLocTacGia, gbc);

        // --- Dòng 3: Bộ lọc (NXB, Năm) ---
        
        // Label NXB
        lblNXB = new JLabel("NXB:");
        lblNXB.setFont(new java.awt.Font("Arial Unicode MS", 0, 14));
        gbc.gridx = 0; 
        gbc.gridy = 2;
        gbc.weightx = 0;
        add(lblNXB, gbc);
        
        // Combo NXB
        cboLocNXB = new JComboBox<>();
        cboLocNXB.setFont(new java.awt.Font("Arial Unicode MS", 0, 14));
        cboLocNXB.setPreferredSize(new java.awt.Dimension(150, 30));
        gbc.gridx = 1; 
        gbc.gridy = 2;
        gbc.weightx = 0.5;
        add(cboLocNXB, gbc);
        
        // Label Năm
        lblNam = new JLabel("Năm XB:");
        lblNam.setFont(new java.awt.Font("Arial Unicode MS", 0, 14));
        gbc.gridx = 2; 
        gbc.gridy = 2;
        gbc.weightx = 0;
        add(lblNam, gbc);
        
        // Panel Năm (Start - End)
        JPanel pnlNam = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        pnlNam.setOpaque(false);
        
        txtNamBatDau = new JTextField();
        txtNamBatDau.setFont(new java.awt.Font("Arial Unicode MS", 0, 14));
        txtNamBatDau.setPreferredSize(new java.awt.Dimension(60, 30));
        
        lblDash = new JLabel("-");
        lblDash.setFont(new java.awt.Font("Arial Unicode MS", 1, 14));
        
        txtNamKetThuc = new JTextField();
        txtNamKetThuc.setFont(new java.awt.Font("Arial Unicode MS", 0, 14));
        txtNamKetThuc.setPreferredSize(new java.awt.Dimension(60, 30));
        
        pnlNam.add(txtNamBatDau);
        pnlNam.add(lblDash);
        pnlNam.add(txtNamKetThuc);
        
        gbc.gridx = 3; 
        gbc.gridy = 2;
        gbc.weightx = 0.5;
        add(pnlNam, gbc);
        
        // Border cho toàn bộ panel
        setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createEtchedBorder(), 
            "Tìm kiếm & Lọc Sách", 
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
            javax.swing.border.TitledBorder.DEFAULT_POSITION, 
            new java.awt.Font("Arial Unicode MS", 1, 14), 
            new java.awt.Color(0, 102, 204)
        ));
    }
    
    private void loadComboBoxData() {
        // Load Thể loại
        cboLocTheLoai.removeAllItems();
        cboLocTheLoai.addItem("Tất cả");
        List<TheLoai> listTL = theLoaiController.getAllTheLoaiNoPaging();
        for (TheLoai tl : listTL) {
            cboLocTheLoai.addItem(tl);
        }
        
        // Load NXB
        cboLocNXB.removeAllItems();
        cboLocNXB.addItem("Tất cả");
        List<NhaXuatBan> listNXB = nxbController.getAllNXBNoPaging();
        for (NhaXuatBan nxb : listNXB) {
            cboLocNXB.addItem(nxb);
        }
        
        // Load Tác giả
        cboLocTacGia.removeAllItems();
        cboLocTacGia.addItem("Tất cả");
        List<TacGia> listTG = tacGiaController.getAllTacGiaNoPaging();
        for (TacGia tg : listTG) {
            cboLocTacGia.addItem(tg);
        }
    }
    
    private void performSearch() {
        if (searchListener != null) {
            SearchCriteria criteria = getSearchCriteria();
            searchListener.onSearch(criteria);
        }
    }
    
    /**
     * Lấy tiêu chí tìm kiếm từ UI
     */
    public SearchCriteria getSearchCriteria() {
        SearchCriteria criteria = new SearchCriteria();
        
        criteria.setKeyword(txtTimKiem.getText());
        
        // Thể loại
        if (cboLocTheLoai.getSelectedItem() instanceof TheLoai) {
            criteria.setMaTheLoai(((TheLoai) cboLocTheLoai.getSelectedItem()).getMaTheLoai());
        }
        
        // NXB
        if (cboLocNXB.getSelectedItem() instanceof NhaXuatBan) {
            criteria.setMaNXB(((NhaXuatBan) cboLocNXB.getSelectedItem()).getMaNXB());
        }
        
        // Tác giả
        if (cboLocTacGia.getSelectedItem() instanceof TacGia) {
            criteria.setMaTacGia(((TacGia) cboLocTacGia.getSelectedItem()).getMaTacGia());
        }
        
        // Năm bắt đầu
        try {
            if (!txtNamBatDau.getText().isBlank()) {
                criteria.setNamBatDau(Integer.parseInt(txtNamBatDau.getText().trim()));
            }
        } catch (NumberFormatException e) {}
        
        // Năm kết thúc
        try {
            if (!txtNamKetThuc.getText().isBlank()) {
                criteria.setNamKetThuc(Integer.parseInt(txtNamKetThuc.getText().trim()));
            }
        } catch (NumberFormatException e) {}
        
        return criteria;
    }
    
    /**
     * Reset tất cả các trường tìm kiếm
     */
    public void clearSearch() {
        txtTimKiem.setText("");
        cboLocTheLoai.setSelectedIndex(0);
        cboLocNXB.setSelectedIndex(0);
        cboLocTacGia.setSelectedIndex(0);
        txtNamBatDau.setText("");
        txtNamKetThuc.setText("");
    }
    
    /**
     * Reload dữ liệu cho các combo box
     */
    public void reloadComboBoxData() {
        loadComboBoxData();
    }
    
    /**
     * Đăng ký listener để nhận sự kiện tìm kiếm
     */
    public void setSearchListener(SearchListener listener) {
        this.searchListener = listener;
    }
    
    /**
     * Interface cho listener tìm kiếm
     */
    public interface SearchListener {
        void onSearch(SearchCriteria criteria);
    }
    
    /**
     * Class chứa tiêu chí tìm kiếm
     */
    public static class SearchCriteria {
        private String keyword;
        private Integer maTheLoai;
        private Integer maNXB;
        private Integer maTacGia;
        private Integer namBatDau;
        private Integer namKetThuc;
        
        public String getKeyword() {
            return keyword;
        }
        
        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }
        
        public Integer getMaTheLoai() {
            return maTheLoai;
        }
        
        public void setMaTheLoai(Integer maTheLoai) {
            this.maTheLoai = maTheLoai;
        }
        
        public Integer getMaNXB() {
            return maNXB;
        }
        
        public void setMaNXB(Integer maNXB) {
            this.maNXB = maNXB;
        }
        
        public Integer getMaTacGia() {
            return maTacGia;
        }
        
        public void setMaTacGia(Integer maTacGia) {
            this.maTacGia = maTacGia;
        }
        
        public Integer getNamBatDau() {
            return namBatDau;
        }
        
        public void setNamBatDau(Integer namBatDau) {
            this.namBatDau = namBatDau;
        }
        
        public Integer getNamKetThuc() {
            return namKetThuc;
        }
        
        public void setNamKetThuc(Integer namKetThuc) {
            this.namKetThuc = namKetThuc;
        }

        @Override
        public String toString() {
            return "SearchCriteria{" +
                    "keyword='" + (keyword == null ? "" : keyword) + '\'' +
                    ", maTheLoai=" + maTheLoai +
                    ", maNXB=" + maNXB +
                    ", maTacGia=" + maTacGia +
                    ", namBatDau=" + namBatDau +
                    ", namKetThuc=" + namKetThuc +
                    '}';
        }
    }
}
