package com.mycompany.quanlythuvien.view.panel;

import com.mycompany.quanlythuvien.controller.BanSaoController;
import com.mycompany.quanlythuvien.controller.ChiTietPhieuMuonController;
import com.mycompany.quanlythuvien.controller.PhieuMuonController;
import com.mycompany.quanlythuvien.dao.BanDocDAO;
import com.mycompany.quanlythuvien.dao.TaiKhoanDAO;
import com.mycompany.quanlythuvien.model.BanDoc;
import com.mycompany.quanlythuvien.model.BanSao;
import com.mycompany.quanlythuvien.model.PageResult;
import com.mycompany.quanlythuvien.model.PhieuMuon;
import com.mycompany.quanlythuvien.model.ChiTietPhieuMuon;
import com.mycompany.quanlythuvien.model.TaiKhoan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
 
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
 

/**
 * Panel quản lý Phiếu Mượn - cập nhật theo yêu cầu người dùng
 * - autocomplete gợi ý top-3 (fuzzy contains)
 * - format ngày dd-MM-yyyy, ràng buộc From <= To
 * - nút Tìm kiếm / Reset
 * - dialog Tạo mới cho phép thêm nhiều MaBanSao (hàng chờ) với kiểm tra
 * - dialog Cập nhật hỗ trợ gia hạn HanTra + trả bản sao
 * - phân trang 10 dòng/trang, Prev/Next only
 */
public class QuanLyPhieuMuonPanel extends JPanel {
    private final PhieuMuonController pmController = new PhieuMuonController();
    private final ChiTietPhieuMuonController ctController = new ChiTietPhieuMuonController();
    private final BanSaoController bsController = new BanSaoController();

    private final JComboBox<String> cbEmailBanDoc = new JComboBox<>();
    private final JComboBox<String> cbEmailNguoiLap = new JComboBox<>();

    // use formatted fields with dd-MM-yyyy
    private final JFormattedTextField tfFromDate;
    private final JFormattedTextField tfToDate;

    private final JButton btnSearch = new JButton("Tìm kiếm");
    private final JButton btnReset = new JButton("Reset");
    private final JProgressBar spinner = new JProgressBar();

    private final JTable table = new JTable();
    private final DefaultTableModel tableModel;

    // fixed page size per user's request
    private final int fixedPageSize = 10;
    private int currentPage = 1;
    private int totalPages = 1;

    private final JLabel lblPageInfo = new JLabel();
    private final JButton btnPrev = new JButton("Trước");
    private final JButton btnNext = new JButton("Tiếp");
    // private final JTextField tfJump = new JTextField(3);
    // private final JButton btnGo = new JButton("GO");

    private final JButton btnViewDetail = new JButton("Xem chi tiết");
    private final JButton btnNew = new JButton("Tạo mới");
    private final JButton btnEdit = new JButton("Cập nhật");
    private final JButton btnDelete = new JButton("Xóa");

    // Use dd-MM-yyyy as requested
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // suggestion lists removed to disable live autocompletion (performance)


    public QuanLyPhieuMuonPanel() {
        setLayout(new BorderLayout(8,8));

        // formatters - set to COMMIT to prevent auto-correction
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        sdf.setLenient(false); // Strict parsing
        DateFormatter dateFormatter = new DateFormatter(sdf);
        dateFormatter.setAllowsInvalid(true); // Allow invalid input temporarily
        dateFormatter.setOverwriteMode(false);
        DefaultFormatterFactory dff = new DefaultFormatterFactory(dateFormatter);

        tfFromDate = new JFormattedTextField(dff); 
        tfFromDate.setColumns(10);
        tfFromDate.setFocusLostBehavior(JFormattedTextField.COMMIT); // Don't auto-correct on focus lost
        
        tfToDate = new JFormattedTextField(dff); 
        tfToDate.setColumns(10);
        tfToDate.setFocusLostBehavior(JFormattedTextField.COMMIT); // Don't auto-correct on focus lost

        // Top: search controls
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.anchor = GridBagConstraints.WEST;
        
        // Set font size lớn hơn cho tất cả components
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 16);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 15);

        // Email bạn đọc
        JLabel lblEmailBanDoc = new JLabel("Email Bạn đọc:");
        lblEmailBanDoc.setFont(labelFont);
        c.gridx = 0; c.gridy = 0; top.add(lblEmailBanDoc, c);
        cbEmailBanDoc.setEditable(true);
        cbEmailBanDoc.setFont(fieldFont);
        cbEmailBanDoc.setPreferredSize(new Dimension(250, 32));
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 0.4; top.add(cbEmailBanDoc, c);

        // Email người lập
        JLabel lblEmailNguoiLap = new JLabel("Email Người lập:");
        lblEmailNguoiLap.setFont(labelFont);
        c.gridx = 2; c.gridy = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE; top.add(lblEmailNguoiLap, c);
        cbEmailNguoiLap.setEditable(true);
        cbEmailNguoiLap.setFont(fieldFont);
        cbEmailNguoiLap.setPreferredSize(new Dimension(250, 32));
        c.gridx = 3; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 0.4; top.add(cbEmailNguoiLap, c);

        // Date from (format dd-MM-yyyy)
        JLabel lblFromDate = new JLabel("Từ ngày (dd-MM-yyyy):");
        lblFromDate.setFont(labelFont);
        c.gridx = 0; c.gridy = 1; c.weightx = 0; c.fill = GridBagConstraints.NONE; top.add(lblFromDate, c);
        tfFromDate.setFont(fieldFont);
        tfFromDate.setPreferredSize(new Dimension(180, 32));
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; top.add(tfFromDate, c);

        // Date to (format dd-MM-yyyy)
        JLabel lblToDate = new JLabel("Đến ngày (dd-MM-yyyy):");
        lblToDate.setFont(labelFont);
        c.gridx = 2; c.fill = GridBagConstraints.NONE; top.add(lblToDate, c);
        tfToDate.setFont(fieldFont);
        tfToDate.setPreferredSize(new Dimension(180, 32));
        c.gridx = 3; c.fill = GridBagConstraints.HORIZONTAL; top.add(tfToDate, c);

        // Row buttons (search/reset)
        c.gridx = 0; c.gridy = 2; c.gridwidth = 4; c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.WEST;
        JPanel rowBtns = new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
        
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 15);
        btnSearch.setFont(buttonFont);
        btnSearch.setPreferredSize(new Dimension(120, 36));
        btnReset.setFont(buttonFont);
        btnReset.setPreferredSize(new Dimension(100, 36));
        
        rowBtns.add(btnSearch); rowBtns.add(btnReset); rowBtns.add(spinner);
        spinner.setVisible(false); spinner.setIndeterminate(true);
        spinner.setPreferredSize(new Dimension(150, 30));
        top.add(rowBtns, c);

        // Row: New / Edit / Delete
        c.gridx = 0; c.gridy = 3; c.gridwidth = 4; c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.CENTER;
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT,10,4));
        
        btnNew.setFont(buttonFont);
        btnNew.setPreferredSize(new Dimension(120, 36));
        btnEdit.setFont(buttonFont);
        btnEdit.setPreferredSize(new Dimension(120, 36));
        btnDelete.setFont(buttonFont);
        btnDelete.setPreferredSize(new Dimension(100, 36));
        btnViewDetail.setFont(buttonFont);
        btnViewDetail.setPreferredSize(new Dimension(140, 36));
        
        row2.add(btnNew); row2.add(btnEdit); row2.add(btnDelete); row2.add(btnViewDetail);
        top.add(row2, c);

        add(top, BorderLayout.NORTH);

        // Table
        /*String[] cols = {"Mã PM","Bạn Đọc","Email bạn đọc","Email Người Lập","Ngày Mượn","Hạn Trả"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table.setModel(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // ensure the table doesn't force the scrollpane to expand and hide the pagination
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(new Dimension(800, 220));
        table.getSelectionModel().addListSelectionListener((ListSelectionListener) e -> {
            boolean sel = table.getSelectedRow() >= 0 && !isNoDataRow(table.getSelectedRow());
            btnEdit.setEnabled(sel);
            btnDelete.setEnabled(sel);
            btnViewDetail.setEnabled(sel);
        });
        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        // Bottom: paging controls Prev/Next and page-jump similar to details dialog
        JPanel bottom = new JPanel(new BorderLayout());
        JPanel leftPaging = new JPanel(new FlowLayout(FlowLayout.CENTER));
        leftPaging.add(btnPrev); leftPaging.add(lblPageInfo); leftPaging.add(btnNext);

        // JPanel rightJump = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        // rightJump.add(new JLabel("Đến trang:")); rightJump.add(tfJump); rightJump.add(btnGo);

        bottom.add(leftPaging, BorderLayout.CENTER);
        // bottom.add(rightJump, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH); */

        String[] cols = {"Mã PM","Bạn Đọc","Email bạn đọc","Email Người Lập","Ngày Mượn","Hạn Trả"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table.setModel(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Tăng font size và row height cho table
        Font tableFont = new Font("Segoe UI", Font.PLAIN, 15);
        table.setFont(tableFont);
        table.setRowHeight(32);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        
        table.getSelectionModel().addListSelectionListener((ListSelectionListener) e -> {
            boolean sel = table.getSelectedRow() >= 0 && !isNoDataRow(table.getSelectedRow());
            btnEdit.setEnabled(sel);
            btnDelete.setEnabled(sel);
            btnViewDetail.setEnabled(sel);
        });
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(new Dimension(1100, 380));
        JScrollPane sp = new JScrollPane(table);

        // Create a panel that contains the table at top and pagination immediately below it
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        // put scroll pane (table) first
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setPreferredSize(new Dimension(800, 220));
        tablePanel.add(sp);

        // Bottom: paging controls placed directly under the table so it is always visible
        JPanel bottom = new JPanel(new BorderLayout());
        JPanel leftPaging = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        
        Font pagingFont = new Font("Segoe UI", Font.BOLD, 15);
        btnPrev.setFont(pagingFont);
        btnPrev.setPreferredSize(new Dimension(100, 36));
        btnNext.setFont(pagingFont);
        btnNext.setPreferredSize(new Dimension(100, 36));
        lblPageInfo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        
        leftPaging.add(btnPrev);
        leftPaging.add(lblPageInfo);
        leftPaging.add(btnNext);

        // give the bottom panel a fixed preferred height so it stays visible and nicely spaced
        bottom.setPreferredSize(new Dimension(0, 55));
        bottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        bottom.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        bottom.add(leftPaging, BorderLayout.WEST);

        bottom.setAlignmentX(Component.LEFT_ALIGNMENT);
        tablePanel.add(bottom);

        // finally add the tablePanel to the main panel CENTER (replacing previous add(sp, BorderLayout.CENTER))
        add(tablePanel, BorderLayout.CENTER);

        // Actions
        btnSearch.addActionListener(e -> doSearch(1));
        btnReset.addActionListener(e -> resetFilters());
        btnNew.addActionListener(e -> openCreateDialog());
        btnEdit.addActionListener(e -> openEditDialog());
        btnDelete.addActionListener(e -> performDelete());
        btnPrev.addActionListener(e -> doSearch(Math.max(1, currentPage-1)));
        btnNext.addActionListener(e -> doSearch(Math.min(totalPages, currentPage+1)));
        // tfJump.addActionListener(e -> { try { int p = Integer.parseInt(tfJump.getText().trim()); if (p<1 || p> totalPages) JOptionPane.showMessageDialog(this, "Trang không hợp lệ"); else doSearch(p); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Vui lòng nhập số trang"); } });
        // btnGo.addActionListener(e -> { try { int p = Integer.parseInt(tfJump.getText().trim()); if (p<1 || p> totalPages) JOptionPane.showMessageDialog(this, "Trang không hợp lệ"); else doSearch(p); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Vui lòng nhập số trang"); } });

        btnViewDetail.addActionListener(e -> openSelectedDetail());
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSelectedDetail();
            }
        });

        loadEmailsAsync();
        // initial empty search
        SwingUtilities.invokeLater(() -> doSearch(1));
    }

    private boolean isNoDataRow(int row) {
        Object o = tableModel.getValueAt(row,0);
        return o == null || (o instanceof String && ((String)o).startsWith("Không có dữ liệu"));
    }

    private void loadEmailsAsync() {
        spinner.setVisible(true);
        SwingWorker<Void,Void> w = new SwingWorker<>() {
            List<String> emailsBD = new ArrayList<>();
            List<String> emailsTK = new ArrayList<>();

            @Override protected Void doInBackground() throws Exception {
                try {
                    BanDocDAO bdDao = new BanDocDAO();
                    java.util.ArrayList<BanDoc> ds = new java.util.ArrayList<>();
                    bdDao.readDAO(ds);
                    for (BanDoc b: ds) if (b.getEmail() != null && !b.getEmail().isBlank()) emailsBD.add(b.getEmail());

                    TaiKhoanDAO tk = new TaiKhoanDAO();
                    List<com.mycompany.quanlythuvien.model.TaiKhoan> accs = tk.getAllAccountsSimple();
                    for (com.mycompany.quanlythuvien.model.TaiKhoan a: accs) if (a.getEmail() != null) emailsTK.add(a.getEmail());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override protected void done() {
                spinner.setVisible(false);
                cbEmailBanDoc.removeAllItems(); cbEmailBanDoc.addItem("");
                for (String s: emailsBD) cbEmailBanDoc.addItem(s);
                cbEmailNguoiLap.removeAllItems(); cbEmailNguoiLap.addItem("");
                for (String s: emailsTK) cbEmailNguoiLap.addItem(s);
            }
        };
        w.execute();
    }


    private void resetFilters() {
        // cbEmailBanDoc.setSelectedItem(""); cbEmailNguoiLap.setSelectedItem(""); tfFromDate.setValue(null); tfToDate.setValue(null);
        // currentPage = 1; totalPages = 1; tableModel.setRowCount(0); lblPageInfo.setText("");
        // doSearch(1);
        cbEmailBanDoc.setSelectedItem(""); cbEmailNguoiLap.setSelectedItem(""); tfFromDate.setValue(null); tfToDate.setValue(null);
        currentPage = 1; tableModel.setRowCount(0); lblPageInfo.setText("");


        // Khi reset, chúng ta muốn tổng số trang phản ánh bộ dữ liệu *không lọc*.
        // Vì vậy thực hiện một tìm kiếm không lọc (trong background) để lấy totalPages,
        // rồi gọi doSearch(1) để load trang đầu với trạng thái mặc định.
        spinner.setVisible(true);
        btnSearch.setEnabled(false);
        SwingWorker<PageResult<PhieuMuon>, Void> w = new SwingWorker<>() {
        @Override protected PageResult<PhieuMuon> doInBackground() throws Exception {
        // gọi searchWithPagination với tất cả filter = null để lấy tổng trang của dữ liệu gốc
            return pmController.searchWithPagination(null, null, null, null, null, 1, fixedPageSize);
        }
        @Override protected void done() {
            spinner.setVisible(false);
            btnSearch.setEnabled(true);
            try {
                PageResult<PhieuMuon> res = get();
                totalPages = Math.max(1, res.getTotalPages());
            } catch (Exception ex) {
                totalPages = 1;
            }
            // cuối cùng refresh bảng ở trang 1 với filter đã reset
            doSearch(1);
            }
        };
        w.execute();
    }

    private void doSearch(int page) {
        // validate dates
        LocalDate from = null, to = null;
        
        // Validate format dd-MM-yyyy using regex
        String dateRegex = "^\\d{2}-\\d{2}-\\d{4}$";
        
        try {
            String sf = tfFromDate.getText().trim();
            String st = tfToDate.getText().trim();
            
            // Kiểm tra format "Từ ngày"
            if (!sf.isEmpty()) {
                if (!sf.matches(dateRegex)) {
                    JOptionPane.showMessageDialog(this, 
                        "Từ ngày không đúng định dạng dd-MM-yyyy\nVí dụ: 26-11-2025", 
                        "Lỗi định dạng", 
                        JOptionPane.ERROR_MESSAGE);
                    tfFromDate.requestFocus(); // Focus vào ô bị lỗi
                    return;
                }
                from = LocalDate.parse(sf, dtf);
            }
            
            // Kiểm tra format "Đến ngày"
            if (!st.isEmpty()) {
                if (!st.matches(dateRegex)) {
                    JOptionPane.showMessageDialog(this, 
                        "Đến ngày không đúng định dạng dd-MM-yyyy\nVí dụ: 10-12-2025", 
                        "Lỗi định dạng", 
                        JOptionPane.ERROR_MESSAGE);
                    tfToDate.requestFocus(); // Focus vào ô bị lỗi
                    return;
                }
                to = LocalDate.parse(st, dtf);
            }
            
            // Nếu một ô trống thì gán bằng giá trị của ô còn lại
            if (from != null && to == null) {
                to = from; // Chỉ tìm kiếm ngày "from"
            } else if (from == null && to != null) {
                from = to; // Chỉ tìm kiếm ngày "to"
            }
            
            // Kiểm tra khoảng thời gian hợp lệ
            if (from != null && to != null && from.isAfter(to)) {
                JOptionPane.showMessageDialog(this, 
                    "Từ ngày phải nhỏ hơn hoặc bằng Đến ngày", 
                    "Lỗi khoảng thời gian", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (DateTimeParseException ex) { 
            JOptionPane.showMessageDialog(this, 
                "Ngày tháng không hợp lệ!\nVui lòng nhập đúng định dạng dd-MM-yyyy\nVí dụ: 26-11-2025", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE); 
            return; 
        }

        String emailBD = ((String)cbEmailBanDoc.getSelectedItem());
        String emailLap = ((String)cbEmailNguoiLap.getSelectedItem());
        if (emailBD != null && emailBD.isBlank()) emailBD = null;
        if (emailLap != null && emailLap.isBlank()) emailLap = null;

        final LocalDate fFrom = from, fTo = to; final String fEmailBD = emailBD, fEmailLap = emailLap;

        spinner.setVisible(true);
        btnSearch.setEnabled(false);

        SwingWorker<PageResult<PhieuMuon>, Void> worker = new SwingWorker<>() {
            @Override protected PageResult<PhieuMuon> doInBackground() throws Exception {
                int pageSize = fixedPageSize;
                return pmController.searchWithPagination(fEmailBD, fEmailLap, fFrom, fTo, null, page, pageSize);
            }

            @Override protected void done() {
                spinner.setVisible(false); btnSearch.setEnabled(true);
                try {
                    PageResult<PhieuMuon> res = get();
                    currentPage = res.getPageIndex();
                    totalPages = res.getTotalPages();
                    updateTable(res.getData());
                    lblPageInfo.setText("Trang " + currentPage + " / " + Math.max(1, totalPages) + " (Tổng: " + res.getTotalCount() + ")");
                    // enable/disable navigation
                    btnPrev.setEnabled(currentPage > 1);
                    btnNext.setEnabled(currentPage < totalPages);
                    btnEdit.setEnabled(table.getSelectedRow() >= 0 && !isNoDataRow(table.getSelectedRow()));
                    btnDelete.setEnabled(table.getSelectedRow() >= 0 && !isNoDataRow(table.getSelectedRow()));
                } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(QuanLyPhieuMuonPanel.this, "Lỗi khi tìm: " + ex.getMessage()); }
            }
        };
        worker.execute();
    }

    private void updateTable(List<PhieuMuon> data) {
        tableModel.setRowCount(0);
        if (data == null || data.isEmpty()) {
            tableModel.addRow(new Object[]{"Không có dữ liệu", "", "", "", "", ""});
            return;
        }
        for (PhieuMuon p: data) {
            String tenBD = "";
            String emailBD = "";
            try {
                BanDocDAO bdao = new BanDocDAO();
                java.util.ArrayList<BanDoc> tmp = new java.util.ArrayList<>();
                bdao.readDAO(tmp);
                for (BanDoc b: tmp) if (b.getIdBD() == p.getIdBD()) { tenBD = b.getHoTen(); emailBD = b.getEmail(); break; }
            } catch (Exception ex) {}
            if (tenBD == null || tenBD.isBlank()) tenBD = "#" + p.getIdBD();
            if (emailBD == null) emailBD = "";
            String nm = p.getNgayMuon() == null ? "" : p.getNgayMuon().format(dtf);
            String ht = p.getHanTra() == null ? "" : p.getHanTra().format(dtf);
            tableModel.addRow(new Object[]{p.getIdPM(), tenBD, emailBD, p.getEmailNguoiLap(), nm, ht});
        }
    }

    // Create dialog (improved): top = PhieuMuon info, bottom = add MaBanSao queue with checks
    private void openCreateDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Tạo mới Phiếu Mượn", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(720, 560);
        dlg.setLocationRelativeTo(this);
        
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 15);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);
        
        JPanel p = new JPanel(new GridBagLayout()); 
        GridBagConstraints c = new GridBagConstraints(); 
        c.insets = new Insets(10,10,10,10); 
        c.anchor = GridBagConstraints.WEST;

        // Bạn đọc
        JLabel lblBD = new JLabel("Bạn đọc (Email):");
        lblBD.setFont(labelFont);
        c.gridx=0; c.gridy=0; p.add(lblBD, c);
        JComboBox<String> cbBD = new JComboBox<>();
        cbBD.setEditable(true);
        cbBD.setFont(fieldFont);
        cbBD.setPreferredSize(new Dimension(350, 32));
        try { BanDocDAO bdao = new BanDocDAO(); java.util.ArrayList<BanDoc> list = new java.util.ArrayList<>(); bdao.readDAO(list); cbBD.addItem(""); for (BanDoc b: list) cbBD.addItem(b.getEmail()); } catch(Exception ex){ }
        c.gridx=1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx=1; p.add(cbBD, c);

        // Email Người lập
        JLabel lblNguoiLap = new JLabel("Email Người lập:");
        lblNguoiLap.setFont(labelFont);
        c.gridx=0; c.gridy=1; c.fill = GridBagConstraints.NONE; c.weightx=0; p.add(lblNguoiLap, c);
        JComboBox<String> cbEmailNguoiLap = new JComboBox<>();
        cbEmailNguoiLap.setEditable(true);
        cbEmailNguoiLap.setFont(fieldFont);
        cbEmailNguoiLap.setPreferredSize(new Dimension(350, 32));
        try { 
            TaiKhoanDAO tkDao = new TaiKhoanDAO(); 
            List<TaiKhoan> listTK = tkDao.getAllAccountsSimple(); 
            cbEmailNguoiLap.addItem(""); 
            for (TaiKhoan tk: listTK) cbEmailNguoiLap.addItem(tk.getEmail()); 
        } catch(Exception ex){ }
        c.gridx=1; c.fill = GridBagConstraints.HORIZONTAL; p.add(cbEmailNguoiLap, c);

        // Ngày mượn
        JLabel lblNgayMuon = new JLabel("Ngày mượn (dd-MM-yyyy):");
        lblNgayMuon.setFont(labelFont);
        c.gridx=0; c.gridy=2; c.fill = GridBagConstraints.NONE; p.add(lblNgayMuon, c);
        JTextField tfN = new JTextField(15);
        tfN.setFont(fieldFont);
        tfN.setPreferredSize(new Dimension(200, 32));
        tfN.setText(dtf.format(LocalDate.now()));
        c.gridx=1; c.fill = GridBagConstraints.HORIZONTAL; p.add(tfN, c);

        // Hạn trả
        JLabel lblHanTra = new JLabel("Hạn trả (dd-MM-yyyy):");
        lblHanTra.setFont(labelFont);
        c.gridx=0; c.gridy=3; c.fill = GridBagConstraints.NONE; p.add(lblHanTra, c);
        JTextField tfH = new JTextField(15);
        tfH.setFont(fieldFont);
        tfH.setPreferredSize(new Dimension(200, 32));
        tfH.setText(dtf.format(LocalDate.now().plusWeeks(1)));
        c.gridx=1; c.fill = GridBagConstraints.HORIZONTAL; p.add(tfH, c);

        // Separator
        c.gridx=0; c.gridy=4; c.gridwidth=2; p.add(new JSeparator(), c);

        // Bottom: MaBanSao input + queue
        JLabel lblMaBanSao = new JLabel("Mã Bản Sao:");
        lblMaBanSao.setFont(labelFont);
        c.gridx=0; c.gridy=5; c.gridwidth=1; c.fill = GridBagConstraints.NONE; p.add(lblMaBanSao, c);
        JTextField tfMaBanSao = new JTextField(15);
        tfMaBanSao.setFont(fieldFont);
        tfMaBanSao.setPreferredSize(new Dimension(200, 32));
        c.gridx=1; c.fill = GridBagConstraints.HORIZONTAL; p.add(tfMaBanSao, c);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnThem = new JButton("Thêm");
        btnThem.setFont(buttonFont);
        btnThem.setPreferredSize(new Dimension(100, 34));
        JButton btnKiemTra = new JButton("Kiểm tra");
        btnKiemTra.setFont(buttonFont);
        btnKiemTra.setPreferredSize(new Dimension(110, 34));
        btns.add(btnThem); btns.add(btnKiemTra);
        c.gridx=1; c.gridy=6; p.add(btns, c);

        // queue list
        DefaultListModel<Integer> queueModel = new DefaultListModel<>();
        JList<Integer> queueList = new JList<>(queueModel);
        queueList.setFont(fieldFont);
        queueList.setVisibleRowCount(6);

        JPanel bottomBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        JButton btnSave = new JButton("Tạo mới");
        btnSave.setFont(buttonFont);
        btnSave.setPreferredSize(new Dimension(120, 36));
        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(buttonFont);
        btnCancel.setPreferredSize(new Dimension(100, 36));
        bottomBtns.add(btnSave); bottomBtns.add(btnCancel);
        c.gridx=0; c.gridy=8; c.gridwidth=2; p.add(bottomBtns, c);

        dlg.setContentPane(p);

        // action: Thêm MaBanSao vào hàng chờ với checks
        btnThem.addActionListener(ae -> {
            try {
                String t = tfMaBanSao.getText().trim(); if (t.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Vui lòng nhập MaBanSao"); return; }
                int m = Integer.parseInt(t);
                // exists?
                // ChiTietPhieuMuon active = ctController.findByMaBanSao(m);
                BanSao bs = bsController.findById(m);
                if (bs == null) {
                    JOptionPane.showMessageDialog(dlg, "Bản sao không tồn tại trong kho"); return;
                }
                // check lendable status
                if (!bs.isLendable()) {
                    JOptionPane.showMessageDialog(dlg, "Bản sao không thể mượn (đang được mượn hoặc tình trạng quá xấu)", "Không thể mượn", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // check duplication in queue
                for (int i=0;i<queueModel.size();i++) if (queueModel.get(i) == m) { JOptionPane.showMessageDialog(dlg, "Bản sao hiện tại đang trong hàng chờ"); return; }
                // check borrowed
                if (ctController.isMaBanSaoBorrowed(m)) { JOptionPane.showMessageDialog(dlg, "Bản sao đang được mượn"); return; }
                // assume existence: ideally check BANSAO table; if fails, show message
                // add
                queueModel.addElement(m);
                tfMaBanSao.setText("");
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(dlg, "Mã phải là số nguyên"); }
        });

        // Kiem tra -> open a dialog listing queue with - buttons to remove
        btnKiemTra.addActionListener(ae -> {
            JDialog d2 = new JDialog(dlg, "Hàng chờ MaBanSao", Dialog.ModalityType.APPLICATION_MODAL);
            d2.setSize(300,300); d2.setLocationRelativeTo(dlg);
            JPanel pp = new JPanel(new BorderLayout());
            DefaultListModel<Integer> lm = new DefaultListModel<>(); for (int i=0;i<queueModel.size();i++) lm.addElement(queueModel.get(i));
            JList<Integer> jl = new JList<>(lm); jl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            pp.add(new JScrollPane(jl), BorderLayout.CENTER);
            JButton btnRemove = new JButton("-");
            btnRemove.addActionListener(ev -> { int idx = jl.getSelectedIndex(); if (idx>=0) { lm.remove(idx); queueModel.remove(idx); } });
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER)); bottom.add(btnRemove); JButton bclose = new JButton("Đóng"); bottom.add(bclose);
            bclose.addActionListener(ev -> d2.dispose()); pp.add(bottom, BorderLayout.SOUTH);
            d2.setContentPane(pp); d2.setVisible(true);
        });

        // Save -> commit: create PhieuMuon and details
        btnSave.addActionListener(ae -> {
            try {
                String sel = (String)cbBD.getSelectedItem(); 
                if (sel==null || sel.isBlank()) { 
                    JOptionPane.showMessageDialog(dlg, "Vui lòng nhập email bạn đọc"); 
                    return; 
                }
                
                // Kiểm tra email bạn đọc có tồn tại không
                BanDocDAO bdDao = new BanDocDAO();
                BanDoc banDoc = bdDao.findByEmail(sel.trim());
                if (banDoc == null) {
                    JOptionPane.showMessageDialog(dlg, "Email bạn đọc '" + sel + "' không tồn tại trong hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int idBD = banDoc.getIdBD();
                
                String email = ((String)cbEmailNguoiLap.getSelectedItem()); 
                if (email == null) email = "";
                email = email.trim();
                if (email.isBlank()) { 
                    JOptionPane.showMessageDialog(dlg, "Email người lập không được để trống"); 
                    return; 
                }
                
                // Kiểm tra email người lập có tồn tại không
                TaiKhoanDAO tkDao = new TaiKhoanDAO();
                TaiKhoan taiKhoan = tkDao.findByEmail(email);
                if (taiKhoan == null) {
                    JOptionPane.showMessageDialog(dlg, "Email người lập '" + email + "' không tồn tại trong hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Validate format ngày mượn
                String dateRegex = "^\\d{2}-\\d{2}-\\d{4}$";
                String ngayMuonStr = tfN.getText().trim();
                if (!ngayMuonStr.matches(dateRegex)) {
                    JOptionPane.showMessageDialog(dlg, "Ngày mượn không đúng định dạng dd-MM-yyyy\nVí dụ: 26-11-2025", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                    tfN.requestFocus();
                    return;
                }
                
                // Validate format hạn trả
                String hanTraStr = tfH.getText().trim();
                if (!hanTraStr.matches(dateRegex)) {
                    JOptionPane.showMessageDialog(dlg, "Hạn trả không đúng định dạng dd-MM-yyyy\nVí dụ: 03-12-2025", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                    tfH.requestFocus();
                    return;
                }
                
                LocalDate nm = LocalDate.parse(ngayMuonStr, dtf);
                LocalDate ht = LocalDate.parse(hanTraStr, dtf);
                
                if (ht.isBefore(nm)) { 
                    JOptionPane.showMessageDialog(dlg, "Hạn trả phải lớn hơn hoặc bằng Ngày Mượn", "Lỗi", JOptionPane.WARNING_MESSAGE); 
                    return; 
                }
                if (queueModel.isEmpty()) { 
                    JOptionPane.showMessageDialog(dlg, "Vui lòng thêm ít nhất 1 MaBanSao"); 
                    return; 
                }

                PhieuMuon pm = new PhieuMuon(); pm.setIdBD(idBD); pm.setEmailNguoiLap(email); pm.setNgayMuon(nm); pm.setHanTra(ht);
                List<ChiTietPhieuMuon> details = new ArrayList<>();
                for (int i=0;i<queueModel.size();i++) {
                    int m = queueModel.get(i);
                    ChiTietPhieuMuon ct = new ChiTietPhieuMuon(); ct.setMaBanSao(m); ct.setIdPM(0); ct.setNgayTraThucTe(null); ct.setTinhTrangKhiTra(null); ct.setEmailNguoiNhan(null);
                    details.add(ct);
                }
                boolean ok = pmController.createWithDetails(pm, details);
                if (ok) {
                    // try to get generated IdPM from pm (assume DAO sets it)
                    JOptionPane.showMessageDialog(dlg, "Đã tạo phiếu mượn " + (pm.getIdPM()>0? pm.getIdPM() : "(đã tạo)"));
                    dlg.dispose(); doSearch(1);
                } else JOptionPane.showMessageDialog(dlg, "Tạo thất bại");
            } catch (DateTimeParseException ex) { JOptionPane.showMessageDialog(dlg, "Sai định dạng ngày (dd-MM-yyyy)"); }
            catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage()); }
        });
        btnCancel.addActionListener(ae -> dlg.dispose());
        dlg.setVisible(true);
    }

    // live textfield autocomplete removed due to performance concerns

    private void openEditDialog() {
        int r = table.getSelectedRow(); 
        if (r<0 || isNoDataRow(r)) { 
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 phiếu để sửa"); 
            return; 
        }
        Object idObj = table.getValueAt(r,0); 
        if (idObj==null) return; 
        int idPM; 
        try { 
            idPM = Integer.parseInt(idObj.toString()); 
        } catch (Exception ex) { 
            JOptionPane.showMessageDialog(this, "Id không hợp lệ"); 
            return; 
        }
        PhieuMuon pm = pmController.findById(idPM); 
        if (pm==null) { 
            JOptionPane.showMessageDialog(this, "Không tìm thấy phiếu"); 
            return; 
        }

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Cập nhật Phiếu Mượn #"+idPM, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(750, 400); 
        dlg.setLocationRelativeTo(this);
        
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 15);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);
        
        JPanel p = new JPanel(new GridBagLayout()); 
        GridBagConstraints c = new GridBagConstraints(); 
        c.insets = new Insets(10,10,10,10); 
        c.anchor = GridBagConstraints.WEST;

        // Email Người lập
        JLabel lblNguoiLap = new JLabel("Email Người lập:");
        lblNguoiLap.setFont(labelFont);
        c.gridx=0; c.gridy=0; p.add(lblNguoiLap, c);
        JComboBox<String> cbEmailNguoiLap = new JComboBox<>();
        cbEmailNguoiLap.setEditable(true);
        cbEmailNguoiLap.setFont(fieldFont);
        cbEmailNguoiLap.setPreferredSize(new Dimension(350, 32));
        try { 
            TaiKhoanDAO tkDao = new TaiKhoanDAO(); 
            List<TaiKhoan> listTK = tkDao.getAllAccountsSimple(); 
            cbEmailNguoiLap.addItem(""); 
            for (TaiKhoan tk: listTK) cbEmailNguoiLap.addItem(tk.getEmail()); 
        } catch(Exception ex){ }
        cbEmailNguoiLap.setSelectedItem(pm.getEmailNguoiLap());
        c.gridx=1; c.fill = GridBagConstraints.HORIZONTAL; p.add(cbEmailNguoiLap, c);

        // Ngày mượn
        JLabel lblNgayMuon = new JLabel("Ngày mượn:");
        lblNgayMuon.setFont(labelFont);
        c.gridx=0; c.gridy=1; c.fill = GridBagConstraints.NONE; p.add(lblNgayMuon, c);
        JTextField tfN = new JTextField(pm.getNgayMuon()==null? "" : pm.getNgayMuon().format(dtf)); 
        tfN.setEditable(false);
        tfN.setBackground(Color.LIGHT_GRAY);
        tfN.setFont(fieldFont);
        tfN.setPreferredSize(new Dimension(200, 32));
        c.gridx=1; c.fill = GridBagConstraints.HORIZONTAL; p.add(tfN, c);

        // Hạn trả
        JLabel lblHanTra = new JLabel("Hạn trả (dd-MM-yyyy):");
        lblHanTra.setFont(labelFont);
        c.gridx=0; c.gridy=2; c.fill = GridBagConstraints.NONE; p.add(lblHanTra, c);
        JTextField tfH = new JTextField(pm.getHanTra()==null? "" : pm.getHanTra().format(dtf)); 
        tfH.setFont(fieldFont);
        tfH.setPreferredSize(new Dimension(200, 32));
        c.gridx=1; c.fill = GridBagConstraints.HORIZONTAL; p.add(tfH, c);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8)); 
        JButton btnUpdate = new JButton("Cập nhật");
        btnUpdate.setFont(buttonFont);
        btnUpdate.setPreferredSize(new Dimension(120, 36));
        JButton btnReturnBooks = new JButton("Thực hiện trả sách");
        btnReturnBooks.setFont(buttonFont);
        btnReturnBooks.setPreferredSize(new Dimension(180, 36));
        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(buttonFont);
        btnCancel.setPreferredSize(new Dimension(100, 36));
        btns.add(btnUpdate); 
        btns.add(btnReturnBooks);
        btns.add(btnCancel);
        c.gridx=0; c.gridy=3; c.gridwidth=2; p.add(btns, c);

        dlg.setContentPane(p);

        // Nút Cập nhật - chỉ cập nhật Email người lập và Hạn trả
        btnUpdate.addActionListener(ae -> {
            try {
                String email = ((String)cbEmailNguoiLap.getSelectedItem());
                if (email == null) email = "";
                email = email.trim();
                if (email.isBlank()) { 
                    JOptionPane.showMessageDialog(dlg, "Email người lập không được để trống"); 
                    return; 
                }
                
                // Kiểm tra email người lập có tồn tại không
                TaiKhoanDAO tkDao = new TaiKhoanDAO();
                TaiKhoan taiKhoan = tkDao.findByEmail(email);
                if (taiKhoan == null) {
                    JOptionPane.showMessageDialog(dlg, "Email người lập '" + email + "' không tồn tại trong hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Validate format hạn trả
                String dateRegex = "^\\d{2}-\\d{2}-\\d{4}$";
                String hanTraStr = tfH.getText().trim();
                if (!hanTraStr.matches(dateRegex)) {
                    JOptionPane.showMessageDialog(dlg, "Hạn trả không đúng định dạng dd-MM-yyyy\nVí dụ: 03-12-2025", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
                    tfH.requestFocus();
                    return;
                }
                
                LocalDate ht = LocalDate.parse(hanTraStr, dtf);
                
                if (ht.isBefore(pm.getNgayMuon())) { 
                    JOptionPane.showMessageDialog(dlg, "Hạn trả phải lớn hơn hoặc bằng Ngày Mượn (" + pm.getNgayMuon().format(dtf) + ")", "Lỗi", JOptionPane.WARNING_MESSAGE); 
                    return; 
                }
                
                pm.setEmailNguoiLap(email); 
                pm.setHanTra(ht);
                pmController.update(pm);
                
                JOptionPane.showMessageDialog(dlg, "Đã cập nhật phiếu mượn " + pm.getIdPM());
                dlg.dispose(); 
                doSearch(currentPage);
            } catch (DateTimeParseException ex) { 
                JOptionPane.showMessageDialog(dlg, "Sai định dạng ngày (dd-MM-yyyy)", "Lỗi", JOptionPane.ERROR_MESSAGE); 
            }
            catch (Exception ex) { 
                JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage()); 
            }
        });

        // Nút Thực hiện trả sách - mở dialog mới
        btnReturnBooks.addActionListener(ae -> {
            openReturnBooksDialog(pm);
            dlg.dispose();
        });

        btnCancel.addActionListener(ae -> dlg.dispose());
        dlg.setVisible(true);
    }

    /**
     * Dialog Thực hiện trả sách - cho phép cập nhật chi tiết phiếu mượn
     */
    private void openReturnBooksDialog(PhieuMuon pm) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Thực hiện trả sách - Phiếu Mượn #" + pm.getIdPM(), Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(1150, 700);
        dlg.setLocationRelativeTo(this);
        
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 15);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);

        JPanel content = new JPanel(new BorderLayout(10,10));
        content.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        // Top: Filters và Email người nhận
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.anchor = GridBagConstraints.WEST;

        // Bộ lọc
        JLabel lblReturned = new JLabel("Trạng thái trả:");
        lblReturned.setFont(labelFont);
        c.gridx=0; c.gridy=0; topPanel.add(lblReturned, c);
        JComboBox<String> cbReturned = new JComboBox<>(new String[]{"Tất cả","Đã trả","Chưa trả"});
        cbReturned.setSelectedItem("Chưa trả");
        cbReturned.setFont(fieldFont);
        cbReturned.setPreferredSize(new Dimension(150, 32));
        c.gridx=1; topPanel.add(cbReturned, c);

        JLabel lblOverdue = new JLabel("Trạng thái trễ hạn:");
        lblOverdue.setFont(labelFont);
        c.gridx=2; topPanel.add(lblOverdue, c);
        JComboBox<String> cbOverdue = new JComboBox<>(new String[]{"Tất cả","Trễ hạn","Chưa trễ hạn"});
        cbOverdue.setFont(fieldFont);
        cbOverdue.setPreferredSize(new Dimension(150, 32));
        c.gridx=3; topPanel.add(cbOverdue, c);

        // Email người nhận
        JLabel lblEmailNguoiNhan = new JLabel("Email Người nhận:");
        lblEmailNguoiNhan.setFont(labelFont);
        c.gridx=0; c.gridy=1; topPanel.add(lblEmailNguoiNhan, c);
        JComboBox<String> cbEmailNguoiNhan = new JComboBox<>();
        cbEmailNguoiNhan.setEditable(true);
        cbEmailNguoiNhan.setFont(fieldFont);
        cbEmailNguoiNhan.setPreferredSize(new Dimension(350, 32));
        try { 
            TaiKhoanDAO tkDao = new TaiKhoanDAO(); 
            List<TaiKhoan> listTK = tkDao.getAllAccountsSimple(); 
            cbEmailNguoiNhan.addItem(""); 
            for (TaiKhoan tk: listTK) cbEmailNguoiNhan.addItem(tk.getEmail()); 
        } catch(Exception ex){ }
        c.gridx=1; c.gridwidth=3; c.fill = GridBagConstraints.HORIZONTAL; topPanel.add(cbEmailNguoiNhan, c);

        content.add(topPanel, BorderLayout.NORTH);

        // Center: Bảng chi tiết với TinhTrangKhiTra có thể chỉnh sửa
        String[] cols = {"MaBanSao","Tình trạng hiện tại","Tình trạng khi trả","Email Người nhận","Ngày trả"};
        DefaultTableModel model = new DefaultTableModel(cols,0) {
            @Override 
            public boolean isCellEditable(int row, int col) { 
                return col == 2; // Chỉ cho phép chỉnh sửa cột "Tình trạng khi trả"
            }
        };
        JTable tbl = new JTable(model);
        
        // Tăng font size và row height
        Font tableFont = new Font("Segoe UI", Font.PLAIN, 14);
        tbl.setFont(tableFont);
        tbl.setRowHeight(32);
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        tbl.getTableHeader().setPreferredSize(new Dimension(0, 38));
        
        // Custom cell editor cho cột TinhTrangKhiTra dựa vào tình trạng hiện tại
        tbl.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox<>()) {
            private JComboBox<String> comboBox;
            
            @Override
            public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                String tinhTrangHienTai = (String) table.getValueAt(row, 1);
                
                // Tạo combobox mới với các options phù hợp
                comboBox = new JComboBox<>();
                comboBox.addItem("");
                
                // Logic: nếu Tốt → có thể chọn (Tốt, Cũ, Rất Cũ, Hỏng)
                //        nếu Cũ → có thể chọn (Cũ, Rất Cũ, Hỏng)
                //        nếu Rất Cũ → có thể chọn (Rất Cũ, Hỏng)
                //        nếu Hỏng → chỉ có Hỏng
                if ("Tốt".equals(tinhTrangHienTai)) {
                    comboBox.addItem("Tốt");
                    comboBox.addItem("Cũ");
                    comboBox.addItem("Rất Cũ");
                    comboBox.addItem("Hỏng");
                } else if ("Cũ".equals(tinhTrangHienTai)) {
                    comboBox.addItem("Cũ");
                    comboBox.addItem("Rất Cũ");
                    comboBox.addItem("Hỏng");
                } else if ("Rất Cũ".equals(tinhTrangHienTai)) {
                    comboBox.addItem("Rất Cũ");
                    comboBox.addItem("Hỏng");
                } else if ("Hỏng".equals(tinhTrangHienTai)) {
                    comboBox.addItem("Hỏng");
                } else {
                    // Default: cho phép tất cả
                    comboBox.addItem("Tốt");
                    comboBox.addItem("Cũ");
                    comboBox.addItem("Rất Cũ");
                    comboBox.addItem("Hỏng");
                }
                
                // Set giá trị hiện tại
                if (value != null && !value.toString().isEmpty()) {
                    comboBox.setSelectedItem(value);
                } else {
                    comboBox.setSelectedIndex(0); // Chọn rỗng
                }
                
                // Đảm bảo editor component được set đúng
                this.editorComponent = comboBox;
                this.delegate = new EditorDelegate() {
                    @Override
                    public void setValue(Object value) {
                        comboBox.setSelectedItem(value);
                    }
                    
                    @Override
                    public Object getCellEditorValue() {
                        return comboBox.getSelectedItem();
                    }
                };
                
                return comboBox;
            }
            
            @Override
            public Object getCellEditorValue() {
                if (comboBox != null) {
                    return comboBox.getSelectedItem();
                }
                return "";
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tbl);
        content.add(scrollPane, BorderLayout.CENTER);

        // Bottom: Pagination và buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        JPanel pag = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8)); 
        JButton bPrev = new JButton("Trước");
        bPrev.setFont(buttonFont);
        bPrev.setPreferredSize(new Dimension(100, 36));
        JButton bNext = new JButton("Tiếp");
        bNext.setFont(buttonFont);
        bNext.setPreferredSize(new Dimension(100, 36));
        JLabel lbl = new JLabel();
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        pag.add(bPrev); 
        pag.add(lbl); 
        pag.add(bNext);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        JButton btnSave = new JButton("Trả sách");
        btnSave.setFont(buttonFont);
        btnSave.setPreferredSize(new Dimension(120, 38));
        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(buttonFont);
        btnClose.setPreferredSize(new Dimension(100, 38));
        btnPanel.add(btnSave);
        btnPanel.add(btnClose);
        
        bottomPanel.add(pag, BorderLayout.NORTH);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);
        content.add(bottomPanel, BorderLayout.SOUTH);

        dlg.setContentPane(content);

        final int[] pageIdx = {1}; 
        final int[] totalPg = {1}; 
        final int pageSz = 10;

        // Load data action
        ActionListener reload = ae -> {
            SwingWorker<com.mycompany.quanlythuvien.model.PageResult<ChiTietPhieuMuon>,Void> w = new SwingWorker<>() {
                @Override 
                protected com.mycompany.quanlythuvien.model.PageResult<ChiTietPhieuMuon> doInBackground() throws Exception {
                    String returned = (String)cbReturned.getSelectedItem();
                    if ("Đã trả".equals(returned)) returned = "returned"; 
                    else if ("Chưa trả".equals(returned)) returned = "unreturned"; 
                    else returned = null;
                    
                    String overdue = (String)cbOverdue.getSelectedItem();
                    if ("Trễ hạn".equals(overdue)) overdue = "overdue"; 
                    else if ("Chưa trễ hạn".equals(overdue)) overdue = "notoverdue"; 
                    else overdue = null;
                    
                    return ctController.findByIdPMWithFiltersPaginated(pm.getIdPM(), returned, overdue, pageIdx[0], pageSz);
                }
                
                @Override 
                protected void done() {
                    try {
                        com.mycompany.quanlythuvien.model.PageResult<ChiTietPhieuMuon> res = get();
                        model.setRowCount(0);
                        for (ChiTietPhieuMuon c: res.getData()) {
                            // Lấy tình trạng hiện tại từ BANSAO table
                            String tinhTrangHienTai = "Tốt"; // Default
                            try {
                                BanSao bs = bsController.findById(c.getMaBanSao());
                                if (bs != null) {
                                    tinhTrangHienTai = bs.getTinhTrang();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            
                            String tinhTrangKhiTra = c.getTinhTrangKhiTra() == null ? "" : c.getTinhTrangKhiTra(); // Để trống nếu chưa trả, hoặc hiển thị giá trị đã lưu
                            String emailNguoiNhan = c.getEmailNguoiNhan() == null ? "" : c.getEmailNguoiNhan();
                            String ngayTra = c.getNgayTraThucTe() == null ? "" : c.getNgayTraThucTe().format(dtf);
                            model.addRow(new Object[]{c.getMaBanSao(), tinhTrangHienTai, tinhTrangKhiTra, emailNguoiNhan, ngayTra});
                        }
                        totalPg[0] = res.getTotalPages(); 
                        lbl.setText("Trang " + pageIdx[0] + " / " + Math.max(1, totalPg[0]) + " (Tổng: " + res.getTotalCount() + ")");
                        bPrev.setEnabled(pageIdx[0] > 1); 
                        bNext.setEnabled(pageIdx[0] < totalPg[0]);
                    } catch (Exception ex) { 
                        ex.printStackTrace(); 
                        JOptionPane.showMessageDialog(dlg, "Lỗi load chi tiết: " + ex.getMessage()); 
                    }
                }
            };
            w.execute();
        };

        bPrev.addActionListener(e -> { pageIdx[0] = Math.max(1, pageIdx[0]-1); reload.actionPerformed(null); });
        bNext.addActionListener(e -> { pageIdx[0] = Math.min(totalPg[0], pageIdx[0]+1); reload.actionPerformed(null); });
        cbReturned.addActionListener(reload); 
        cbOverdue.addActionListener(reload);

        // Nút Trả sách
        btnSave.addActionListener(ae -> {
            try {
                // Dừng cell editing để commit giá trị vào model
                if (tbl.isEditing()) {
                    tbl.getCellEditor().stopCellEditing();
                }
                
                String emailNguoiNhan = ((String)cbEmailNguoiNhan.getSelectedItem());
                System.out.println("Debug: EmailNguoiNhan before trim='" + emailNguoiNhan + "'");
                if (emailNguoiNhan != null) emailNguoiNhan = emailNguoiNhan.trim();
                
                // Nếu có nhập email người nhận thì kiểm tra
                if (emailNguoiNhan != null && !emailNguoiNhan.isBlank()) {
                    TaiKhoanDAO tkDao = new TaiKhoanDAO();
                    TaiKhoan taiKhoan = tkDao.findByEmail(emailNguoiNhan);
                    if (taiKhoan == null) {
                        JOptionPane.showMessageDialog(dlg, "Email người nhận '" + emailNguoiNhan + "' không tồn tại trong hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                // Thu thập các bản sao cần cập nhật
                List<ChiTietPhieuMuon> toUpdate = new ArrayList<>();
                int updateCount = 0;
                
                for (int i = 0; i < model.getRowCount(); i++) {
                    int maBanSao = (int) model.getValueAt(i, 0);
                    String tinhTrangKhiTra = (String) model.getValueAt(i, 2);
                    System.out.println("Debug: MaBanSao=" + maBanSao + ", TinhTrangKhiTra=" + tinhTrangKhiTra);
                    
                    // Chỉ cập nhật nếu TinhTrangKhiTra không trống (người dùng đã chọn)
                    if (tinhTrangKhiTra != null && !tinhTrangKhiTra.trim().isEmpty()) {
                        ChiTietPhieuMuon ct = new ChiTietPhieuMuon();
                        ct.setIdPM(pm.getIdPM());
                        ct.setMaBanSao(maBanSao);
                        ct.setNgayTraThucTe(LocalDate.now());
                        ct.setTinhTrangKhiTra(tinhTrangKhiTra);
                        ct.setEmailNguoiNhan(emailNguoiNhan);
                        toUpdate.add(ct);
                        updateCount++;
                    }
                }
                
                // Nếu có nhập email người nhận mà không có bản sao nào được cập nhật
                if (emailNguoiNhan != null && !emailNguoiNhan.isBlank() && updateCount == 0) {
                    JOptionPane.showMessageDialog(dlg, "Phải có ít nhất 1 bản sao được cập nhật", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (emailNguoiNhan == null || emailNguoiNhan.isBlank()) {
                    // Nếu không nhập email người nhận, đảm bảo không có bản sao nào được cập nhật
                    if (updateCount > 0) {
                        JOptionPane.showMessageDialog(dlg, "Phải nhập Email Người nhận khi trả sách", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                
                // Cập nhật vào database
                if (!toUpdate.isEmpty()) {
                    for (ChiTietPhieuMuon ct : toUpdate) {
                        boolean ok = ctController.markReturned(ct.getIdPM(), ct.getMaBanSao(), ct.getNgayTraThucTe(), ct.getTinhTrangKhiTra(), ct.getEmailNguoiNhan());
                        if (!ok) {
                            JOptionPane.showMessageDialog(dlg, "Lỗi khi cập nhật bản sao " + ct.getMaBanSao(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    JOptionPane.showMessageDialog(dlg, "Đã cập nhật " + updateCount + " bản sao");
                    reload.actionPerformed(null); // Refresh lại bảng
                } else {
                    JOptionPane.showMessageDialog(dlg, "Không có bản sao nào được cập nhật");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        btnClose.addActionListener(ae -> {
            dlg.dispose();
            doSearch(currentPage); // Refresh lại bảng chính
        });

        // Initial load
        reload.actionPerformed(null);
        dlg.setVisible(true);
    }

    private void performDelete() {
        int r = table.getSelectedRow(); if (r<0 || isNoDataRow(r)) { JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 phiếu để xóa"); return; }
        Object idObj = table.getValueAt(r,0); if (idObj == null) return; int idPM; try { idPM = Integer.parseInt(idObj.toString()); } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Id không hợp lệ"); return; }

        // custom confirm with red Yes
        int option = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa " + idPM + " này không ?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) return;
        boolean ok = pmController.delete(idPM);
        if (ok) { JOptionPane.showMessageDialog(this, "Xóa thành công"); doSearch(1); } else JOptionPane.showMessageDialog(this, "Xóa thất bại");
    }

    private void openSelectedDetail() {
        int r = table.getSelectedRow();
        if (r < 0 || isNoDataRow(r)) { JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 phiếu mượn"); return; }
        Object idObj = table.getValueAt(r, 0);
        if (idObj == null) return;
        int idPM;
        try { idPM = Integer.parseInt(idObj.toString()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "IdPM không hợp lệ"); return; }

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết Phiếu Mượn #"+idPM, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(1050, 550);
        dlg.setLocationRelativeTo(this);
        
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 15);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);

        // Build dialog content
        JPanel content = new JPanel(new BorderLayout(10,10));
        content.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        
        JPanel topFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        
        JLabel lblReturned = new JLabel("Trạng thái trả:");
        lblReturned.setFont(labelFont);
        topFilters.add(lblReturned);
        
        JComboBox<String> cbReturned = new JComboBox<>(new String[]{"Tất cả","Đã trả","Chưa trả"});
        cbReturned.setFont(fieldFont);
        cbReturned.setPreferredSize(new Dimension(140, 32));
        topFilters.add(cbReturned);
        
        JLabel lblOverdue = new JLabel("Trạng thái trễ hạn:");
        lblOverdue.setFont(labelFont);
        topFilters.add(lblOverdue);
        
        JComboBox<String> cbOverdue = new JComboBox<>(new String[]{"Tất cả","Trễ hạn","Chưa trễ hạn"});
        cbOverdue.setFont(fieldFont);
        cbOverdue.setPreferredSize(new Dimension(140, 32));
        topFilters.add(cbOverdue);
        
        content.add(topFilters, BorderLayout.NORTH);

        String[] cols = {"MaBanSao","EmailNguoiNhan","NgayTraThucTe","TinhTrangKhiTra"};
        DefaultTableModel model = new DefaultTableModel(cols,0) { 
            @Override public boolean isCellEditable(int r,int c){return false;} 
        };
        JTable tbl = new JTable(model);
        
        // Tăng font size và row height
        Font tableFont = new Font("Segoe UI", Font.PLAIN, 14);
        tbl.setFont(tableFont);
        tbl.setRowHeight(32);
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        tbl.getTableHeader().setPreferredSize(new Dimension(0, 38));
        
        content.add(new JScrollPane(tbl), BorderLayout.CENTER);

        JPanel pag = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        
        JButton bPrev = new JButton("Trước");
        bPrev.setFont(buttonFont);
        bPrev.setPreferredSize(new Dimension(100, 36));
        
        JButton bNext = new JButton("Tiếp");
        bNext.setFont(buttonFont);
        bNext.setPreferredSize(new Dimension(100, 36));
        
        JLabel lbl = new JLabel();
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        
        pag.add(bPrev); pag.add(lbl); pag.add(bNext);
        content.add(pag, BorderLayout.SOUTH);

        dlg.setContentPane(content);

        final int[] pageIdx = {1}; final int[] totalPg = {1}; final int pageSz = 10;

        ActionListener reload = ae -> {
            SwingWorker<com.mycompany.quanlythuvien.model.PageResult<ChiTietPhieuMuon>,Void> w = new SwingWorker<>() {
                @Override protected com.mycompany.quanlythuvien.model.PageResult<ChiTietPhieuMuon> doInBackground() throws Exception {
                    String returned = (String)cbReturned.getSelectedItem();
                    if ("Đã trả".equals(returned)) returned = "returned"; else if ("Chưa trả".equals(returned)) returned = "unreturned"; else returned = null;
                    String overdue = (String)cbOverdue.getSelectedItem();
                    if ("Trễ hạn".equals(overdue)) overdue = "overdue"; else if ("Chưa trễ hạn".equals(overdue)) overdue = "notoverdue"; else overdue = null;
                    return ctController.findByIdPMWithFiltersPaginated(idPM, returned, overdue, pageIdx[0], pageSz);
                }
                @Override protected void done() {
                    try {
                        com.mycompany.quanlythuvien.model.PageResult<ChiTietPhieuMuon> res = get();
                        model.setRowCount(0);
                        for (ChiTietPhieuMuon c: res.getData()) model.addRow(new Object[]{c.getMaBanSao(), c.getEmailNguoiNhan(), c.getNgayTraThucTe()==null?"":c.getNgayTraThucTe().format(dtf), c.getTinhTrangKhiTra()});
                        totalPg[0] = res.getTotalPages(); lbl.setText("Trang " + pageIdx[0] + " / " + Math.max(1, totalPg[0]) + " (Tổng: " + res.getTotalCount() + ")");
                        bPrev.setEnabled(pageIdx[0] > 1); bNext.setEnabled(pageIdx[0] < totalPg[0]);
                    } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(dlg, "Lỗi load chi tiết: " + ex.getMessage()); }
                }
            };
            w.execute();
        };

        bPrev.addActionListener(e -> { pageIdx[0] = Math.max(1, pageIdx[0]-1); reload.actionPerformed(null); });
        bNext.addActionListener(e -> { pageIdx[0] = Math.min(totalPg[0], pageIdx[0]+1); reload.actionPerformed(null); });
        cbReturned.addActionListener(reload); cbOverdue.addActionListener(reload);

        // initial load
        reload.actionPerformed(null);
        dlg.setVisible(true);
    }
}
