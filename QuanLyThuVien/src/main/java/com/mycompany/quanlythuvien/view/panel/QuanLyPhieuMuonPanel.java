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

        // formatters
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        DateFormatter dateFormatter = new DateFormatter(sdf);
        DefaultFormatterFactory dff = new DefaultFormatterFactory(dateFormatter);

        tfFromDate = new JFormattedTextField(dff); tfFromDate.setColumns(10);
        tfToDate = new JFormattedTextField(dff); tfToDate.setColumns(10);

        // Top: search controls
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.anchor = GridBagConstraints.WEST;

        // Email bạn đọc
        c.gridx = 0; c.gridy = 0; top.add(new JLabel("Email Bạn đọc:"), c);
        cbEmailBanDoc.setEditable(true);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 0.4; top.add(cbEmailBanDoc, c);

        // Email người lập
        c.gridx = 2; c.gridy = 0; c.weightx = 0; c.fill = GridBagConstraints.NONE; top.add(new JLabel("Email Người lập:"), c);
        cbEmailNguoiLap.setEditable(true);
        c.gridx = 3; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 0.4; top.add(cbEmailNguoiLap, c);

        // Date from (format dd-MM-yyyy)
        c.gridx = 0; c.gridy = 1; c.weightx = 0; c.fill = GridBagConstraints.NONE; top.add(new JLabel("Từ ngày (dd-MM-yyyy):"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; top.add(tfFromDate, c);

        // Date to (format dd-MM-yyyy)
        c.gridx = 2; c.fill = GridBagConstraints.NONE; top.add(new JLabel("Đến ngày (dd-MM-yyyy):"), c);
        c.gridx = 3; c.fill = GridBagConstraints.HORIZONTAL; top.add(tfToDate, c);

        // Row buttons (search/reset)
        c.gridx = 0; c.gridy = 2; c.gridwidth = 4; c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.WEST;
        JPanel rowBtns = new JPanel(new FlowLayout(FlowLayout.LEFT,4,0)); rowBtns.add(btnSearch); rowBtns.add(btnReset); rowBtns.add(spinner);
        spinner.setVisible(false); spinner.setIndeterminate(true);
        top.add(rowBtns, c);

        // Row: New / Edit / Delete
        c.gridx = 0; c.gridy = 3; c.gridwidth = 4; c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.CENTER;
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
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
        table.getSelectionModel().addListSelectionListener((ListSelectionListener) e -> {
            boolean sel = table.getSelectedRow() >= 0 && !isNoDataRow(table.getSelectedRow());
            btnEdit.setEnabled(sel);
            btnDelete.setEnabled(sel);
            btnViewDetail.setEnabled(sel);
        });
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(new Dimension(800, 220));
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
        JPanel leftPaging = new JPanel(new FlowLayout(FlowLayout.CENTER));
        leftPaging.add(btnPrev);
        leftPaging.add(lblPageInfo);
        leftPaging.add(btnNext);

        // give the bottom panel a fixed preferred height so it stays visible and nicely spaced
        bottom.setPreferredSize(new Dimension(0, 40));
        bottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        bottom.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
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
        try {
            String sf = tfFromDate.getText().trim();
            if (!sf.isEmpty()) from = LocalDate.parse(sf, dtf);
            String st = tfToDate.getText().trim();
            if (!st.isEmpty()) to = LocalDate.parse(st, dtf);
            if (from != null && to != null && from.isAfter(to)) {
                JOptionPane.showMessageDialog(this, "Từ ngày phải nhỏ hơn hoặc bằng Đến ngày");
                return;
            }
        } catch (DateTimeParseException ex) { JOptionPane.showMessageDialog(this, "Sai định dạng ngày (dd-MM-yyyy)"); return; }

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
        dlg.setSize(540, 420);
        dlg.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout()); GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(6,6,6,6); c.anchor = GridBagConstraints.WEST;

        // Bạn đọc
        c.gridx=0; c.gridy=0; p.add(new JLabel("Bạn đọc (Email):"), c);
        JComboBox<String> cbBD = new JComboBox<>();
        try { BanDocDAO bdao = new BanDocDAO(); java.util.ArrayList<BanDoc> list = new java.util.ArrayList<>(); bdao.readDAO(list); cbBD.addItem(""); for (BanDoc b: list) cbBD.addItem(b.getEmail()); } catch(Exception ex){ }
        c.gridx=1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx=1; p.add(cbBD, c);

        // Email Người lập (gợi ý)
        c.gridx=0; c.gridy=1; c.fill = GridBagConstraints.NONE; c.weightx=0; p.add(new JLabel("Email Người lập:"), c);
        JTextField tfEmail = new JTextField(25);
        c.gridx=1; c.fill = GridBagConstraints.HORIZONTAL; p.add(tfEmail, c);
        // autocompletion disabled for tfEmail due to performance issues

        // Ngày mượn
        c.gridx=0; c.gridy=2; c.fill = GridBagConstraints.NONE; p.add(new JLabel("Ngày mượn (dd-MM-yyyy):"), c);
        JFormattedTextField tfN = new JFormattedTextField(new DefaultFormatterFactory(new DateFormatter(new SimpleDateFormat("dd-MM-yyyy")))); tfN.setColumns(10); tfN.setText(dtf.format(LocalDate.now()));
        c.gridx=1; c.fill = GridBagConstraints.HORIZONTAL; p.add(tfN, c);

        // Hạn trả
        c.gridx=0; c.gridy=3; c.fill = GridBagConstraints.NONE; p.add(new JLabel("Hạn trả (dd-MM-yyyy):"), c);
        JFormattedTextField tfH = new JFormattedTextField(new DefaultFormatterFactory(new DateFormatter(new SimpleDateFormat("dd-MM-yyyy")))); tfH.setColumns(10); tfH.setText(dtf.format(LocalDate.now().plusDays(14)));
        c.gridx=1; c.fill = GridBagConstraints.HORIZONTAL; p.add(tfH, c);

        // Separator
        c.gridx=0; c.gridy=4; c.gridwidth=2; p.add(new JSeparator(), c);

        // Bottom: MaBanSao input + queue
        c.gridx=0; c.gridy=5; c.gridwidth=1; c.fill = GridBagConstraints.NONE; p.add(new JLabel("Mã Bản Sao:"), c);
        JTextField tfMaBanSao = new JTextField(10); c.gridx=1; c.fill = GridBagConstraints.HORIZONTAL; p.add(tfMaBanSao, c);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT)); JButton btnThem = new JButton("Thêm"); JButton btnKiemTra = new JButton("Kiểm tra"); btns.add(btnThem); btns.add(btnKiemTra);
        c.gridx=1; c.gridy=6; p.add(btns, c);

        // queue list
        DefaultListModel<Integer> queueModel = new DefaultListModel<>(); JList<Integer> queueList = new JList<>(queueModel); queueList.setVisibleRowCount(6);
        // JScrollPane qsp = new JScrollPane(queueList); qsp.setPreferredSize(new Dimension(200,120)); c.gridx=0; c.gridy=7; c.gridwidth=2; p.add(qsp, c);

        JPanel bottomBtns = new JPanel(new FlowLayout(FlowLayout.CENTER)); JButton btnSave = new JButton("Tạo mới"); JButton btnCancel = new JButton("Hủy"); bottomBtns.add(btnSave); bottomBtns.add(btnCancel);
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
                String sel = (String)cbBD.getSelectedItem(); if (sel==null || sel.isBlank()) { JOptionPane.showMessageDialog(dlg, "Vui lòng chọn bạn đọc"); return; }
                int idBD = Integer.parseInt(sel.split(" ")[0]);
                String email = tfEmail.getText().trim(); if (email.isBlank()) { JOptionPane.showMessageDialog(dlg, "Email người lập không được để trống"); return; }
                LocalDate nm = LocalDate.parse(tfN.getText().trim(), dtf);
                LocalDate ht = LocalDate.parse(tfH.getText().trim(), dtf);
                if (ht.isBefore(nm)) { JOptionPane.showMessageDialog(dlg, "Hạn trả phải lớn hơn hoặc bằng Ngày Mượn"); return; }
                if (queueModel.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Vui lòng thêm ít nhất 1 MaBanSao"); return; }

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
        int r = table.getSelectedRow(); if (r<0 || isNoDataRow(r)) { JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 phiếu để sửa"); return; }
        Object idObj = table.getValueAt(r,0); if (idObj==null) return; int idPM; try { idPM = Integer.parseInt(idObj.toString()); } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Id không hợp lệ"); return; }
        PhieuMuon pm = pmController.findById(idPM); if (pm==null) { JOptionPane.showMessageDialog(this, "Không tìm thấy phiếu"); return; }

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Cập nhật Phiếu Mượn #"+idPM, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(560,420); dlg.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout()); GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(6,6,6,6); c.anchor = GridBagConstraints.WEST;

        // Top: allow editing EmailNguoiLap and HanTra
        c.gridx=0; c.gridy=0; p.add(new JLabel("Email Người lập:"), c);
        JTextField tfEmail = new JTextField(pm.getEmailNguoiLap(),25); c.gridx=1; c.fill = GridBagConstraints.HORIZONTAL; p.add(tfEmail, c);
        // autocompletion disabled for tfEmail in edit dialog (was causing lag)

        c.gridx=0; c.gridy=1; c.fill = GridBagConstraints.NONE; p.add(new JLabel("Ngày mượn (dd-MM-yyyy):"), c);
        JFormattedTextField tfN = new JFormattedTextField(new DefaultFormatterFactory(new DateFormatter(new SimpleDateFormat("dd-MM-yyyy")))); tfN.setColumns(10); tfN.setText(pm.getNgayMuon()==null? dtf.format(LocalDate.now()): pm.getNgayMuon().format(dtf)); c.gridx=1; c.fill = GridBagConstraints.HORIZONTAL; p.add(tfN, c);

        c.gridx=0; c.gridy=2; p.add(new JLabel("Hạn trả (dd-MM-yyyy):"), c);
        JFormattedTextField tfH = new JFormattedTextField(new DefaultFormatterFactory(new DateFormatter(new SimpleDateFormat("dd-MM-yyyy")))); tfH.setColumns(10); tfH.setText(pm.getHanTra()==null? dtf.format(LocalDate.now().plusDays(14)): pm.getHanTra().format(dtf)); c.gridx=1; c.fill = GridBagConstraints.HORIZONTAL; p.add(tfH, c);

        // separator
        c.gridx=0; c.gridy=3; c.gridwidth=2; p.add(new JSeparator(), c);

        // Bottom: trả bản sao (MaBanSao nhập thủ công)
        c.gridwidth=1; c.gridx=0; c.gridy=4; p.add(new JLabel("MaBanSao:"), c);
        JTextField tfMaBanSao = new JTextField(10); c.gridx=1; c.fill = GridBagConstraints.HORIZONTAL; p.add(tfMaBanSao, c);
        c.gridx=0; c.gridy=5; p.add(new JLabel("TinhTrangKhiTra:"), c);
        JComboBox<String> cbTinhTrang = new JComboBox<>(new String[]{"Tốt","Cũ hơn","Hỏng"}); c.gridx=1; p.add(cbTinhTrang, c);
        c.gridx=0; c.gridy=6; p.add(new JLabel("Email Người nhận:"), c);
        JTextField tfEmailNhan = new JTextField(25); c.gridx=1; p.add(tfEmailNhan, c);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT)); JButton btnReturn = new JButton("Trả"); JButton btnCancel = new JButton("Hủy"); btns.add(btnReturn); btns.add(btnCancel);
        c.gridx=0; c.gridy=7; c.gridwidth=2; p.add(btns, c);

        dlg.setContentPane(p);

        btnReturn.addActionListener(ae -> {
            try {
                LocalDate nm = LocalDate.parse(tfN.getText().trim(), dtf);
                LocalDate ht = LocalDate.parse(tfH.getText().trim(), dtf);
                if (ht.isBefore(nm)) { JOptionPane.showMessageDialog(dlg, "Hạn trả phải lớn hơn Ngày Mượn"); return; }
                pm.setEmailNguoiLap(tfEmail.getText().trim()); pm.setNgayMuon(nm); pm.setHanTra(ht);

                // handle return of a MaBanSao if provided
                String sMa = tfMaBanSao.getText().trim(); if (!sMa.isEmpty()) {
                    int m = Integer.parseInt(sMa);
                    // checks like in create
                    if (!ctController.isMaBanSaoBorrowed(m)) { JOptionPane.showMessageDialog(dlg, "Bản sao hiện tại không đang được mượn"); return; }
                    String tt = (String) cbTinhTrang.getSelectedItem();
                    boolean ok = ctController.markReturned(pm.getIdPM(), m, LocalDate.now(), tt);
                    if (!ok) { JOptionPane.showMessageDialog(dlg, "Trả bản sao thất bại"); return; }
                    // set extra fields
                    // dao should set tinh trang and email nguoi nhan; here we suppose markReturned can be extended; otherwise separate dao call required
                }

                // update PM always
                pmController.update(pm);
                JOptionPane.showMessageDialog(dlg, "Đã cập nhật phiếu mượn " + pm.getIdPM());
                dlg.dispose(); doSearch(currentPage);
            } catch (DateTimeParseException ex) { JOptionPane.showMessageDialog(dlg, "Sai định dạng ngày (dd-MM-yyyy)"); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(dlg, "Mã phải là số"); }
            catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Lỗi: " + ex.getMessage()); }
        });

        btnCancel.addActionListener(ae -> dlg.dispose());
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
        dlg.setSize(800, 400);
        dlg.setLocationRelativeTo(this);

        // Build dialog content
        JPanel content = new JPanel(new BorderLayout(6,6));
        JPanel topFilters = new JPanel();
        JComboBox<String> cbReturned = new JComboBox<>(new String[]{"Tất cả","Đã trả","Chưa trả"});
        JComboBox<String> cbOverdue = new JComboBox<>(new String[]{"Tất cả","Trễ hạn","Chưa trễ hạn"});
        topFilters.add(new JLabel("Trạng thái trả:")); topFilters.add(cbReturned);
        topFilters.add(new JLabel("Trạng thái trễ hạn:")); topFilters.add(cbOverdue);
        content.add(topFilters, BorderLayout.NORTH);

        String[] cols = {"MaBanSao","EmailNguoiNhan","NgayTraThucTe","TinhTrangKhiTra"};
        DefaultTableModel model = new DefaultTableModel(cols,0) { @Override public boolean isCellEditable(int r,int c){return false;} };
        JTable tbl = new JTable(model);
        content.add(new JScrollPane(tbl), BorderLayout.CENTER);

        JPanel pag = new JPanel(new FlowLayout(FlowLayout.CENTER)); JButton bPrev = new JButton("Trước"), bNext = new JButton("Tiếp"); JLabel lbl = new JLabel(); pag.add(bPrev); pag.add(lbl); pag.add(bNext);
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
