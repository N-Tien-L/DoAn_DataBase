/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.quanlythuvien.view.panel;

import com.mycompany.quanlythuvien.view.dialog.BanDocFormDialog;
import com.mycompany.quanlythuvien.controller.BanDocController;
import com.mycompany.quanlythuvien.dao.BanDocDAO;
import com.mycompany.quanlythuvien.model.BanDoc;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.Timer; 
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author DMX MSI
 */
public class QuanLyBanDocPanel extends javax.swing.JPanel {

    /**
     * Creates new form QuanLyBanDocPanel
     */
    BanDocController cur;
    String txtSearchPrv = "";
    private int currentPage = 1;
    private int pageSize = 32;    
    private int totalRecords = 0;
    private int totalPages = 1;
    private List<BanDoc> displayedList = new ArrayList<>();
    private Timer searchTimer;
    private JComboBox<Integer> pageSizeCombo;
    private JSpinner gotoPageSpinner;
    private JPopupMenu tablePopup;
    private DocumentListener searchDocListener;

//    private JButton btnClearSearch;
    private boolean tableSelectionEnabled = true; // để tắt selection khi rỗng
    private void updateDetailLabelsForId(int idBD) {
        // disable labels tạm để UI thấy đang load (option)
        lblShowSoLanMuon.setText("...");
        lblShowSoPhieuPhat.setText("...");
        lblShowSoSachDaMuon.setText("...");
        lblShowSoSachDangMuon.setText("...");
        lblShowTongSoTienPhatChuaDong.setText("...");
        lblShowTongSoTienPhatDaDong.setText("...");

        new SwingWorker<Void, Void>() {
            int soLanMuon = 0;
            int soPhieuPhat = 0;
            int soSachDaMuon = 0;
            int soSachDangMuon = 0;
            int soTienPhatChuaDong = 0;
            int soTienPhatDaDong = 0;

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    BanDocDAO tmp = new BanDocDAO();
                    soLanMuon = tmp.getSoLanMuonCuaBanDoc(idBD);
                    soSachDangMuon = tmp.getSoSachDangMuonCuaBanDoc(idBD);
                    soSachDaMuon = tmp.getSoSachDaMuonCuaBanDoc(idBD);
                    soPhieuPhat = tmp.getSoPhieuPhatBanDoc(idBD);
                    soTienPhatChuaDong = tmp.getSoTienPhatChuaDongBanDoc(idBD);
                    soTienPhatDaDong = tmp.getSoTienPhatDaDongBanDoc(idBD);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    // nếu lỗi, để các biến = 0 (đã default)
                }
                return null;
            }

            @Override
            protected void done() {
                // an toàn cập nhật UI trên EDT
                NumberFormat nf = NumberFormat.getInstance(new Locale("vi","VN"));
                try {
                    lblShowSoLanMuon.setText(String.valueOf(soLanMuon));
                    lblShowSoPhieuPhat.setText(String.valueOf(soPhieuPhat));
                    lblShowSoSachDaMuon.setText(String.valueOf(soSachDaMuon));
                    lblShowSoSachDangMuon.setText(String.valueOf(soSachDangMuon));
                    // format tiền: 1.000.000
                    lblShowTongSoTienPhatChuaDong.setText(nf.format(soTienPhatChuaDong));
                    lblShowTongSoTienPhatDaDong.setText(nf.format(soTienPhatDaDong));
                } catch (Exception ex) {
                    // fallback: đặt text an toàn
                    lblShowSoLanMuon.setText("-");
                    lblShowSoPhieuPhat.setText("-");
                    lblShowSoSachDaMuon.setText("-");
                    lblShowSoSachDangMuon.setText("-");
                    lblShowTongSoTienPhatChuaDong.setText("-");
                    lblShowTongSoTienPhatDaDong.setText("-");
                }
            }
        }.execute();
    }
    // ------------------ THÊM VÀO: phương thức khởi tạo UI bổ sung ------------------
    private void initUIEnhancements() {
        // --- tooltips (giữ nguyên) ---
        if (btnAdd != null) btnAdd.setToolTipText("Thêm (Ctrl+N)");
        if (btnEdit != null) btnEdit.setToolTipText("Sửa (Ctrl+E)");
        if (btnDelete != null) btnDelete.setToolTipText("Xóa (Delete)");
        if (btnView != null) btnView.setToolTipText("Xem chi tiết (Double click)");
        if (txtSearch != null) txtSearch.setToolTipText("Nhập để tìm kiếm. Nhấn Enter để lọc ngay hoặc gõ để lọc tự động.");

        // --- đảm bảo btnClearSearch: tạo nếu chưa có, reuse nếu đã có ---
        if (btnClearSearch == null) {
            btnClearSearch = new JButton("X");
            btnClearSearch.setMargin(new java.awt.Insets(2,6,2,6));
            btnClearSearch.setFocusable(false);
            btnClearSearch.setToolTipText("Xóa tìm kiếm");
            // cố gắng thêm vào panelSearch nếu tồn tại, nếu không thì toolbar
            try {
                if (panelSearch != null) {
                    panelSearch.add(btnClearSearch);
                } else {
                    int idx = jToolBar1.getComponentIndex(txtSearch);
                    if (idx >= 0) jToolBar1.add(btnClearSearch, idx + 1);
                    else jToolBar1.add(btnClearSearch);
                }
            } catch (Exception ex) {
                // fallback: add to toolbar
                try { jToolBar1.add(btnClearSearch); } catch (Exception ignored) {}
            }
        } else {
            btnClearSearch.setToolTipText("Xóa tìm kiếm");
            btnClearSearch.setFocusable(false);
        }
        // đảm bảo không thêm listener nhiều lần: remove tất cả action listeners trước khi add mới
        for (ActionListener al : btnClearSearch.getActionListeners()) {
            btnClearSearch.removeActionListener(al);
        }
        btnClearSearch.addActionListener(e -> {
            if (txtSearch != null) {
                txtSearch.setText("");
                txtSearchActionPerformed(null);
                txtSearch.requestFocusInWindow();
            }
        });

        // --- debounce tìm kiếm bằng Swing Timer (300ms) ---
        if (searchTimer == null) {
            searchTimer = new Timer(300, e -> {
                txtSearchActionPerformed(null);
                searchTimer.stop();
            });
            searchTimer.setRepeats(false);
        }

        // --- DocumentListener: tạo 1 listener duy nhất và add nếu chưa add ---
        if (txtSearch != null) {
            if (searchDocListener == null) {
                searchDocListener = new DocumentListener() {
                    @Override public void insertUpdate(DocumentEvent e) { docChanged(); }
                    @Override public void removeUpdate(DocumentEvent e) { docChanged(); }
                    @Override public void changedUpdate(DocumentEvent e) { docChanged(); }

                    private void docChanged() {
                        try {
                            String txt = txtSearch.getText().trim();
                            if (txt.isEmpty()) {
                                if (searchTimer != null) searchTimer.stop();
                                showList(new ArrayList<>(safeGetDsBanDoc()));
                                txtSearchPrv = "";
                            } else {
                                if (searchTimer != null) searchTimer.restart();
                            }
                        } catch (Exception ex) {
                            // defensive: nếu có lỗi truy xuất văn bản thì ignore
                        }
                    }
                };
                txtSearch.getDocument().addDocumentListener(searchDocListener);
            }
        }

        // --- page size selector & goto page spinner (ở panelPagination) ---
        if (pageSizeCombo == null) {
            Integer[] sizes = new Integer[]{10, 20, 32, 50, 100};
            pageSizeCombo = new JComboBox<>(sizes);
            pageSizeCombo.setSelectedItem(pageSize);
            pageSizeCombo.setToolTipText("Số bản ghi mỗi trang");
            pageSizeCombo.addActionListener(e -> {
                Integer s = (Integer) pageSizeCombo.getSelectedItem();
                if (s != null && s > 0) {
                    pageSize = s;
                    currentPage = 1;
                    initPagination();
                }
            });
            // thêm vào panelPagination nếu chưa có
            boolean already = false;
            for (java.awt.Component c : panelPagination.getComponents()) {
                if (c == pageSizeCombo) { already = true; break; }
            }
            if (!already) {
                panelPagination.add(new JLabel(" / trang: "));
                panelPagination.add(pageSizeCombo);
            }
        }

        // spinner để nhảy trang nhanh
        if (gotoPageSpinner == null) {
            gotoPageSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Math.max(1, totalPages), 1));
            gotoPageSpinner.setPreferredSize(new Dimension(60, gotoPageSpinner.getPreferredSize().height));
            gotoPageSpinner.setToolTipText("Chuyển tới trang");
            gotoPageSpinner.addChangeListener(e -> {
                int p = (Integer) gotoPageSpinner.getValue();
                if (p >= 1 && p <= totalPages) {
                    currentPage = p;
                    loadPage(currentPage);
                }
            });
            boolean got = false;
            for (java.awt.Component c : panelPagination.getComponents()) {
                if (c == gotoPageSpinner) { got = true; break; }
            }
            if (!got) {
                panelPagination.add(new JLabel(" | Chuyển tới: "));
                panelPagination.add(gotoPageSpinner);
            }
        }

        // --- context menu cho table (right click) ---
        if (tablePopup == null) {
            tablePopup = new JPopupMenu();
            JMenuItem miView = new JMenuItem("Xem chi tiết");
            JMenuItem miEdit = new JMenuItem("Sửa");
            JMenuItem miDelete = new JMenuItem("Xóa");

            miView.addActionListener(e -> btnViewActionPerformed(null));
            miEdit.addActionListener(e -> btnEditActionPerformed(null));
            miDelete.addActionListener(e -> btnDeleteActionPerformed(null));

            tablePopup.add(miView);
            tablePopup.add(miEdit);
            tablePopup.add(miDelete);
        }

        // attach popup listener (an toàn: có thể add nhiều nhưng hành vi giống nhau)
        tblUsers.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { maybeShowPopup(e); }
            @Override
            public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = tblUsers.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        tblUsers.setRowSelectionInterval(row, row);
                    }
                    if (tablePopup != null) tablePopup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    tblUsers.getSelectionModel().addListSelectionListener(evt -> {
        if (!evt.getValueIsAdjusting()) {
            int viewRow = tblUsers.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = tblUsers.convertRowIndexToModel(viewRow);
                int idBD = Integer.parseInt(tblUsers.getModel().getValueAt(modelRow, 0).toString());
                updateDetailLabelsForId(idBD);
            }
        }
    });


        // --- key bindings (phím tắt) ---
        InputMap im = this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = this.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "add");
        am.put("add", new AbstractAction(){ @Override public void actionPerformed(ActionEvent e) { btnAddActionPerformed(null); } });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK), "edit");
        am.put("edit", new AbstractAction(){ @Override public void actionPerformed(ActionEvent e) { btnEditActionPerformed(null); } });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        am.put("delete", new AbstractAction(){ @Override public void actionPerformed(ActionEvent e) { btnDeleteActionPerformed(null); } });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "focusSearch");
        am.put("focusSearch", new AbstractAction(){ @Override public void actionPerformed(ActionEvent e) { if (txtSearch!=null){ txtSearch.requestFocusInWindow(); txtSearch.selectAll(); } } });

        // --- uniform toolbar buttons style ---
        java.util.List<JButton> tbBtns = Arrays.asList(btnAdd, btnEdit, btnDelete, btnView);
        for (JButton b : tbBtns) {
            if (b == null) continue;
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
            b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            b.setContentAreaFilled(false);
            b.setOpaque(false);
            b.setHorizontalAlignment(SwingConstants.CENTER);
            b.setVerticalAlignment(SwingConstants.CENTER);
            if ("Xem chi tiết".equals(b.getText())) {
                b.setText("Xem");
            }
        }

        // btnClearSearch cosmetic
        if (btnClearSearch != null) {
            btnClearSearch.setFocusPainted(false);
            btnClearSearch.setBorder(BorderFactory.createEmptyBorder(2,6,2,6));
            btnClearSearch.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            btnClearSearch.setContentAreaFilled(false);
            btnClearSearch.setOpaque(false);
        }

        // --- nicer table behavior: column widths, selection mode already set ---
        tblUsers.setFillsViewportHeight(true);
        try {
            tblUsers.getColumnModel().getColumn(0).setPreferredWidth(60); // ID
            tblUsers.getColumnModel().getColumn(1).setPreferredWidth(180); // name
            tblUsers.getColumnModel().getColumn(2).setPreferredWidth(180); // email
        } catch (Exception ex) {
            // ignore if columns not ready yet
        }
        tblUsers.setRowHeight(28);
        tblUsers.setIntercellSpacing(new Dimension(8, 6));
        tblUsers.setShowGrid(false);
        tblUsers.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        tblUsers.getTableHeader().setReorderingAllowed(false);
        tblUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblUsers.setSelectionBackground(new java.awt.Color(51,153,255));
        tblUsers.setSelectionForeground(java.awt.Color.white);

        // --- accessibility names ---
        if (btnAdd != null) btnAdd.getAccessibleContext().setAccessibleName("Thêm bạn đọc");
        if (btnEdit != null) btnEdit.getAccessibleContext().setAccessibleName("Sửa bạn đọc");
        if (btnDelete != null) btnDelete.getAccessibleContext().setAccessibleName("Xóa bạn đọc");
        if (txtSearch != null) txtSearch.getAccessibleContext().setAccessibleDescription("Nhập từ khóa tìm kiếm");
        if (searchByCombo != null) searchByCombo.getAccessibleContext().setAccessibleName("Tìm theo");

        // --- ensure goto spinner sync now if exists ---
        if (gotoPageSpinner != null) {
            gotoPageSpinner.setModel(new SpinnerNumberModel(currentPage, 1, Math.max(1, totalPages), 1));
        }
        
        
    // --- Fix truncated toolbar text / choose text-mode or icon-mode ---
    // Paste this at the end of initUIEnhancements()

    // Set this flag: false = keep text (wider buttons), true = use icons (icon-only)
        boolean usingIcons = false;

        tbBtns = new ArrayList<>();
        if (btnAdd instanceof JButton) tbBtns.add((JButton) btnAdd);
        if (btnEdit instanceof JButton) tbBtns.add((JButton) btnEdit);
        if (btnDelete instanceof JButton) tbBtns.add((JButton) btnDelete);
        if (btnView instanceof JButton) tbBtns.add((JButton) btnView);

        if (!usingIcons) {
            // Text mode: make buttons wide enough to show short labels
            Dimension textBtnSize = new Dimension(88, 36); // enough for short Viet labels
            for (JButton b : tbBtns) {
                if (b == null) continue;
                b.setPreferredSize(textBtnSize);
                b.setMinimumSize(textBtnSize);
                b.setMaximumSize(textBtnSize);
                b.setHorizontalTextPosition(SwingConstants.CENTER);
                b.setVerticalTextPosition(SwingConstants.BOTTOM);
                b.setMargin(new Insets(2,6,2,6));
                // shorten overly long phrases to simple words
                String txt = b.getText();
                if (txt != null) {
                    if (txt.contains("Thêm")) b.setText("Thêm");
                    else if (txt.contains("Sửa")) b.setText("Sửa");
                    else if (txt.contains("Xóa")) b.setText("Xóa");
                    else if (txt.contains("Xem")) b.setText("Xem");
                }
            }
        } else {
            // Icon mode: try to load icons from resources and hide text.
            // Put your icons in resources (e.g. /icons/add.png). If load fails, fallback to short text.
            try {
                if (btnAdd != null)  btnAdd.setIcon(new ImageIcon(getClass().getResource("/icons/add.png")));
                if (btnEdit != null) btnEdit.setIcon(new ImageIcon(getClass().getResource("/icons/edit.png")));
                if (btnDelete != null) btnDelete.setIcon(new ImageIcon(getClass().getResource("/icons/delete.png")));
                if (btnView != null)   btnView.setIcon(new ImageIcon(getClass().getResource("/icons/view.png")));
                // hide text and set compact size
                Dimension icoSize = new Dimension(40, 40);
                for (JButton b : tbBtns) {
                    if (b == null) continue;
                    b.setText("");
                    b.setPreferredSize(icoSize);
                    b.setMinimumSize(icoSize);
                    b.setMaximumSize(icoSize);
                    b.setHorizontalTextPosition(SwingConstants.CENTER);
                    b.setVerticalTextPosition(SwingConstants.BOTTOM);
                    b.setContentAreaFilled(false);
                }
            } catch (Exception ex) {
                // fallback to text-mode if icons not found
                Dimension textBtnSize = new Dimension(88, 36);
                for (JButton b : tbBtns) {
                    if (b == null) continue;
                    b.setPreferredSize(textBtnSize);
                    b.setText((b.getText() == null || b.getText().isEmpty()) ? "?" : b.getText());
                }
            }
        }

        // small cosmetic tune for clear button (so nó aligns with text-mode)
        if (btnClearSearch != null) {
            btnClearSearch.setPreferredSize(new Dimension(30, 26));
            btnClearSearch.setMinimumSize(new Dimension(30, 26));
            btnClearSearch.setMaximumSize(new Dimension(30, 26));
        }

        // force UI refresh
        jToolBar1.revalidate();
        jToolBar1.repaint();

    }

    private void recalcTotalPages() {
        totalPages = (totalRecords + pageSize - 1) / pageSize;
        if (totalPages == 0) totalPages = 1;
    }
    private void initPagination() {


        totalRecords = displayedList == null ? 0 : displayedList.size();
        recalcTotalPages();
        loadPage(currentPage);

    }
    private void loadPage(int page) {
        if (displayedList == null) displayedList = new ArrayList<>();
        // clamp page
        if (page < 1) page = 1;
        int pages = Math.max(1, (totalRecords + pageSize - 1) / pageSize);
        if (page > pages) page = pages;
        currentPage = page; // đồng bộ currentPage

        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, displayedList.size());

        DefaultTableModel model = (DefaultTableModel) tblUsers.getModel();
        model.setRowCount(0);

        for (int i = start; i < end; i++) {
            BanDoc u = displayedList.get(i);
            model.addRow(new Object[]{
                u.getIdBD(),
                u.getHoTen() == null ? "" : u.getHoTen(),
                u.getEmail() == null ? "" : u.getEmail(),
                u.getSdt() == null ? "" : u.getSdt(),
                u.getDiaChi() == null ? "" : u.getDiaChi()
            });
        }

        int startDisplay = totalRecords == 0 ? 0 : (start + 1);
        int endDisplay = totalRecords == 0 ? 0 : end;
        lblPageInfo.setText("Trang " + currentPage + "/" + totalPages+ " (Hiển thị " + startDisplay + "-" + endDisplay + " / " + totalRecords + ")");
        btnPrv.setEnabled(currentPage > 1);
        btnNxt.setEnabled(currentPage < totalPages);

        // scroll to top of table
        scrollPaneUsers.getViewport().setViewPosition(new java.awt.Point(0,0));
        if (gotoPageSpinner != null) {
            gotoPageSpinner.setModel(new SpinnerNumberModel(currentPage, 1, Math.max(1, totalPages), 1));
            gotoPageSpinner.setValue(currentPage);
        }
        // tắt selection khi không có bản ghi
        boolean enabled = totalRecords > 0;
        tblUsers.setRowSelectionAllowed(enabled);
        tblUsers.setEnabled(enabled);
    }




    
    public QuanLyBanDocPanel() throws Exception {
        initComponents();
        initUIEnhancements();
        cur = new BanDocController();
        tblUsers.setModel(createTableModel());
        tblUsers.setAutoCreateRowSorter(true);
        tblUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        try {
            cur.init();
            showList(new ArrayList<>(safeGetDsBanDoc()));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi load dữ liệu:\n" + ex.getMessage());
        }
        initPagination();
        tblUsers.setRowHeight(26);
        tblUsers.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    setBackground(new java.awt.Color(51,153,255));
                    setForeground(java.awt.Color.white);
                } else {
                    setBackground(row % 2 == 0 ? java.awt.Color.white : new java.awt.Color(245,247,250));
                    setForeground(java.awt.Color.darkGray);
                }
                setBorder(noFocusBorder);
                return this;
            }
        });
        tblUsers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    btnViewActionPerformed(null);
                }
            }
        });

    }
    private ArrayList<BanDoc> filterList(String KEYFIELD, String KEYTXT) {
        ArrayList<BanDoc> newDsBanDoc = new ArrayList<BanDoc>(); 
        
        switch(KEYFIELD) {
            case "Họ Tên":
                newDsBanDoc = (ArrayList<BanDoc>) cur.getDsBanDoc().stream()
                            .filter(x -> x.getHoTen() != null && x.getHoTen().toLowerCase().contains(KEYTXT))
                            .collect(Collectors.toCollection(ArrayList::new));
                break;

            case "Email":
                newDsBanDoc = (ArrayList<BanDoc>) cur.getDsBanDoc().stream()
                            .filter(x -> x.getEmail() != null && x.getEmail().toLowerCase().contains(KEYTXT))
                            .collect(Collectors.toCollection(ArrayList::new));
                break;

            case "SĐT":
                newDsBanDoc = (ArrayList<BanDoc>) cur.getDsBanDoc().stream()
                            .filter(x -> x.getSdt() != null && x.getSdt().toLowerCase().contains(KEYTXT))
                            .collect(Collectors.toCollection(ArrayList::new));
                break;

            case "Địa Chỉ":
                newDsBanDoc = (ArrayList<BanDoc>) cur.getDsBanDoc().stream()
                            .filter(x -> x.getDiaChi() != null && x.getDiaChi().toLowerCase().contains(KEYTXT))
                            .collect(Collectors.toCollection(ArrayList::new));
                break;
            case "ID":
                newDsBanDoc = (ArrayList<BanDoc>) cur.getDsBanDoc().stream()
                            .filter(x -> Integer.toString(x.getIdBD()).contains(KEYTXT))
                            .collect(Collectors.toCollection(ArrayList::new));
                break;
        }
        return newDsBanDoc;
    }
    
    private DefaultTableModel createTableModel() {
        String[] cols = {"ID", "Họ tên", "Email", "SDT", "Địa chỉ"};
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                return String.class;
            }
        };
    }
    private void showList(ArrayList<BanDoc> list) {
        // cập nhật danh sách hiển thị
        if (list == null) displayedList = new ArrayList<>();
        else displayedList = new ArrayList<>(list);

        // cập nhật totalRecords và paging
        totalRecords = displayedList.size();
        recalcTotalPages();
        // reset về trang 1 khi thay dữ liệu
        currentPage = 1;
        loadPage(currentPage);

    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        btnAdd = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnView = new javax.swing.JButton();
        panelSearch = new javax.swing.JPanel();
        Search = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnClearSearch = new javax.swing.JButton();
        searchByCombo = new javax.swing.JComboBox<>();
        scrollPaneUsers = new javax.swing.JScrollPane();
        tblUsers = new javax.swing.JTable();
        panelPagination = new javax.swing.JPanel();
        btnPrv = new javax.swing.JButton();
        lblPageInfo = new javax.swing.JLabel();
        btnNxt = new javax.swing.JButton();
        lblSoSachDangMuon = new javax.swing.JLabel();
        lblSoSachDaMuon = new javax.swing.JLabel();
        lblSoLanMuon = new javax.swing.JLabel();
        lblShowSoLanMuon = new javax.swing.JLabel();
        lblShowSoSachDaMuon = new javax.swing.JLabel();
        lblShowSoSachDangMuon = new javax.swing.JLabel();
        lblTongSoTienPhatChuaDong = new javax.swing.JLabel();
        lblTongSoTienPhatDaDong = new javax.swing.JLabel();
        lblSoPhieuPhat = new javax.swing.JLabel();
        lblShowSoPhieuPhat = new javax.swing.JLabel();
        lblShowTongSoTienPhatChuaDong = new javax.swing.JLabel();
        lblShowTongSoTienPhatDaDong = new javax.swing.JLabel();

        jToolBar1.setRollover(true);
        jToolBar1.setBorderPainted(false);

        btnAdd.setText("Thêm");
        btnAdd.setBorderPainted(false);
        btnAdd.setContentAreaFilled(false);
        btnAdd.setFocusable(false);
        btnAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAdd.setPreferredSize(new java.awt.Dimension(40, 40));
        btnAdd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });
        jToolBar1.add(btnAdd);

        btnEdit.setText("Sửa");
        btnEdit.setBorderPainted(false);
        btnEdit.setContentAreaFilled(false);
        btnEdit.setFocusable(false);
        btnEdit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnEdit.setPreferredSize(new java.awt.Dimension(40, 40));
        btnEdit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        jToolBar1.add(btnEdit);

        btnDelete.setText("Xóa");
        btnDelete.setBorderPainted(false);
        btnDelete.setContentAreaFilled(false);
        btnDelete.setFocusable(false);
        btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDelete.setPreferredSize(new java.awt.Dimension(40, 40));
        btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        jToolBar1.add(btnDelete);

        btnView.setText("Xem chi tiết");
        btnView.setBorderPainted(false);
        btnView.setContentAreaFilled(false);
        btnView.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnView.setPreferredSize(new java.awt.Dimension(40, 40));
        btnView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewActionPerformed(evt);
            }
        });
        jToolBar1.add(btnView);

        panelSearch.setToolTipText("");
        panelSearch.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        panelSearch.setOpaque(false);
        panelSearch.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 10));

        Search.setText("Tìm kiếm:");
        panelSearch.add(Search);

        txtSearch.setToolTipText("");
        txtSearch.setActionCommand("<Not Set>");
        txtSearch.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtSearch.setPreferredSize(new java.awt.Dimension(200, 26));
        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });
        panelSearch.add(txtSearch);

        btnClearSearch.setText("X");
        btnClearSearch.setBorderPainted(false);
        btnClearSearch.setContentAreaFilled(false);
        btnClearSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnClearSearch.setPreferredSize(new java.awt.Dimension(26, 26));
        btnClearSearch.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        panelSearch.add(btnClearSearch);

        searchByCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ID", "Họ Tên", "Email", "SĐT", "Địa Chỉ" }));
        searchByCombo.setMinimumSize(new java.awt.Dimension(120, 26));
        searchByCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchByComboActionPerformed(evt);
            }
        });
        panelSearch.add(searchByCombo);

        jToolBar1.add(panelSearch);

        tblUsers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Họ tên", "Email", "SDT", "Địa chỉ"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        scrollPaneUsers.setViewportView(tblUsers);

        panelPagination.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        btnPrv.setText("Trang trước");
        btnPrv.setBorder(null);
        btnPrv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrvActionPerformed(evt);
            }
        });
        panelPagination.add(btnPrv);

        lblPageInfo.setText("Trang 1/1");
        panelPagination.add(lblPageInfo);

        btnNxt.setText("Trang sau");
        btnNxt.setBorder(null);
        btnNxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNxtActionPerformed(evt);
            }
        });
        panelPagination.add(btnNxt);

        lblSoSachDangMuon.setText("Số Sách Đang Mượn:");

        lblSoSachDaMuon.setText("Số Sách Đã Mượn:");

        lblSoLanMuon.setText("Số lần mượn:");

        lblTongSoTienPhatChuaDong.setText("Tổng Số Tiền Phạt Chưa Đóng:");

        lblTongSoTienPhatDaDong.setText("Tổng Số Tiền Phạt Đã Đóng:");

        lblSoPhieuPhat.setText("Số Phiếu Phạt:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(scrollPaneUsers, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
            .addComponent(panelPagination, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblSoLanMuon)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblShowSoLanMuon, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblSoSachDaMuon)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblShowSoSachDaMuon, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblSoSachDangMuon)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblShowSoSachDangMuon, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTongSoTienPhatDaDong)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblShowTongSoTienPhatDaDong))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTongSoTienPhatChuaDong)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblShowTongSoTienPhatChuaDong))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblSoPhieuPhat)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblShowSoPhieuPhat, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(scrollPaneUsers, javax.swing.GroupLayout.PREFERRED_SIZE, 455, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelPagination, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSoPhieuPhat)
                            .addComponent(lblShowSoPhieuPhat))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTongSoTienPhatChuaDong)
                            .addComponent(lblShowTongSoTienPhatChuaDong))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTongSoTienPhatDaDong)
                            .addComponent(lblShowTongSoTienPhatDaDong)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSoLanMuon)
                            .addComponent(lblShowSoLanMuon))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblSoSachDaMuon)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblSoSachDangMuon)
                                    .addComponent(lblShowSoSachDangMuon)))
                            .addComponent(lblShowSoSachDaMuon))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        BanDocFormDialog dlg = new BanDocFormDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            BanDoc newBd = dlg.getBanDoc();
            System.out.println(newBd.getHoTen());
            try {
                cur.add(newBd);
                // thành công -> refresh
                showList(new ArrayList<>(safeGetDsBanDoc()));
                JOptionPane.showMessageDialog(this, "Thêm bạn đọc thành công.");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm bạn đọc: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
}

        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        int[] selected = tblUsers.getSelectedRows();
        if (selected.length == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 bạn đọc để sửa.", "Chú ý", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (selected.length > 1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chỉ chọn 1 bạn đọc để sửa.", "Chú ý", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int viewRow = selected[0];
        int modelRow = tblUsers.convertRowIndexToModel(viewRow);

        Object idObj = tblUsers.getModel().getValueAt(modelRow, 0);
        Object nameObj = tblUsers.getModel().getValueAt(modelRow, 1);
        Object emailObj = tblUsers.getModel().getValueAt(modelRow, 2);
        Object sdtObj = tblUsers.getModel().getValueAt(modelRow, 3);
        Object diaChiObj = tblUsers.getModel().getValueAt(modelRow, 4);

        final String id = idObj == null ? "" : idObj.toString();
        final String name = nameObj == null ? "" : nameObj.toString();
        final String email = emailObj == null ? "" : emailObj.toString();
        final String sdt = sdtObj == null ? "" : sdtObj.toString();
        final String diaChi = diaChiObj == null ? "" : diaChiObj.toString();

        BanDoc tmp = new BanDoc(Integer.parseInt(id), name, email, diaChi, sdt);
        BanDocFormDialog dlg = new BanDocFormDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dlg.setBanDoc(tmp);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            BanDoc ntmp = dlg.getBanDoc();
            ntmp.setIdBD(Integer.parseInt(id));
            if (tmp.equals(ntmp)) return;
            try {
                cur.update(ntmp);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
            // refresh danh sách (giữ paging)
            showList(new ArrayList<>(safeGetDsBanDoc()));;
            JOptionPane.showMessageDialog(this, "Sửa bạn đọc thành công.");
        }
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int[] viewRows = tblUsers.getSelectedRows();
        if (viewRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 bạn đọc để xóa.", "Chú ý", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // build summary cho confirm (đếm, liệt kê vài id/name)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < viewRows.length; ++i) {
            int modelRow = tblUsers.convertRowIndexToModel(viewRows[i]);
            Object idObj = tblUsers.getModel().getValueAt(modelRow, 0);
            Object nameObj = tblUsers.getModel().getValueAt(modelRow, 1);
            sb.append(String.format("ID: %s  -  %s\n",
                idObj == null ? "" : idObj.toString(),
                nameObj == null ? "" : nameObj.toString()));
            // show tối đa 10 dòng
            if (i >= 9) { sb.append("...\n"); break; }
        }
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa " + viewRows.length + " bạn đọc sau:\n" + sb.toString(),
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (choice != JOptionPane.YES_OPTION) return;

        // xóa từng bản ghi (nên xóa theo model ID từ DAO)
        boolean anyFailed = false;
        for (int viewRow : viewRows) {
            int modelRow = tblUsers.convertRowIndexToModel(viewRow);
            Object idObj = tblUsers.getModel().getValueAt(modelRow, 0);
            if (idObj == null) continue;
            int id;
            try {
                id = Integer.parseInt(idObj.toString());
            } catch (NumberFormatException nfe) {
                anyFailed = true;
                continue;
            }
            try {
                // gọi controller/DAO xóa theo id
                BanDoc tmp = new BanDoc();
                tmp.setIdBD(id);
                boolean deleted = cur.delete(tmp); // recommended: implement deleteById in controller
                if (!deleted) anyFailed = true;
            } catch (Exception ex) {
                anyFailed = true;
            }
        }

        // refresh list
        try {
            showList(new ArrayList<>(safeGetDsBanDoc()));;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (anyFailed) {
            JOptionPane.showMessageDialog(this, "Hoàn tất, nhưng có vài bản ghi xóa thất bại.", "Kết quả", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Xóa thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        String fieldSearchCur = searchByCombo.getSelectedItem().toString();
        String txtSearchCur = txtSearch.getText().toLowerCase().trim();

        if (txtSearchCur.isEmpty()) {
            // không lọc -> hiển thị toàn bộ
            showList(new ArrayList<>(safeGetDsBanDoc()));;
            txtSearchPrv = "";
            return;
        }

        // chỉ lọc khi khác giá trị trước (optional)
        if (!txtSearchCur.equals(txtSearchPrv)) {
            ArrayList<BanDoc> filtered = filterList(fieldSearchCur, txtSearchCur);
            showList(filtered);
            txtSearchPrv = txtSearchCur;
        }
    }//GEN-LAST:event_txtSearchActionPerformed

    private void searchByComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchByComboActionPerformed
        String txtSearchCur = txtSearch.getText().toLowerCase().trim();
        if (txtSearchCur.isEmpty()) return; // đang không lọc
        String fieldSearchCur = searchByCombo.getSelectedItem().toString();
        ArrayList<BanDoc> filtered = filterList(fieldSearchCur, txtSearchCur);
        showList(filtered);
        
    }//GEN-LAST:event_searchByComboActionPerformed

    private void btnViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewActionPerformed
    int[] selected = tblUsers.getSelectedRows();
    if (selected.length == 0) {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 bạn đọc để xem chi tiết.", "Chú ý", JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    if (selected.length > 1) {
        JOptionPane.showMessageDialog(this, "Vui lòng chỉ chọn 1 bạn đọc để xem chi tiết.", "Chú ý", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    int viewRow = selected[0];
    int modelRow = tblUsers.convertRowIndexToModel(viewRow);

    Object idObj = tblUsers.getModel().getValueAt(modelRow, 0);
    Object nameObj = tblUsers.getModel().getValueAt(modelRow, 1);
    Object emailObj = tblUsers.getModel().getValueAt(modelRow, 2);
    Object sdtObj = tblUsers.getModel().getValueAt(modelRow, 3);
    Object diaChiObj = tblUsers.getModel().getValueAt(modelRow, 4);

    final String idStr = idObj == null ? "" : idObj.toString();
    final String name = nameObj == null ? "" : nameObj.toString();
    final String email = emailObj == null ? "" : emailObj.toString();
    final String sdt = sdtObj == null ? "" : sdtObj.toString();
    final String diaChi = diaChiObj == null ? "" : diaChiObj.toString();

    // parse id an toàn
    int id;
    try {
        id = Integer.parseInt(idStr);
    } catch (NumberFormatException nfe) {
        JOptionPane.showMessageDialog(this, "ID không hợp lệ: " + idStr, "Lỗi", JOptionPane.ERROR_MESSAGE);
        return;
    }
        BanDoc bd = new BanDoc(id, name, email, diaChi, sdt);

        try {
            ChiTietPhieuBanDocPanel chiTietPanel = new ChiTietPhieuBanDocPanel(bd);

//            chiTietPanel.setPreferredSize(new Dimension(1338, 715));

            JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Chi tiết phiếu - " + name,
                Dialog.ModalityType.APPLICATION_MODAL
            );

            dialog.getContentPane().add(chiTietPanel);

            dialog.pack();

            dialog.setLocationRelativeTo(this);

            dialog.setVisible(true);



        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi mở chi tiết: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnViewActionPerformed
    private List<BanDoc> safeGetDsBanDoc() {
        List<BanDoc> ds = cur.getDsBanDoc();
        return ds == null ? new ArrayList<>() : ds;
    }
    private void btnNxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNxtActionPerformed
        if (currentPage < totalPages) {
            currentPage++;
            loadPage(currentPage);
        }
    }//GEN-LAST:event_btnNxtActionPerformed

    private void btnPrvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrvActionPerformed
        if (currentPage > 1) {
            currentPage--;
            loadPage(currentPage);
        }
    }//GEN-LAST:event_btnPrvActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Search;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClearSearch;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnNxt;
    private javax.swing.JButton btnPrv;
    private javax.swing.JButton btnView;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblPageInfo;
    private javax.swing.JLabel lblShowSoLanMuon;
    private javax.swing.JLabel lblShowSoPhieuPhat;
    private javax.swing.JLabel lblShowSoSachDaMuon;
    private javax.swing.JLabel lblShowSoSachDangMuon;
    private javax.swing.JLabel lblShowTongSoTienPhatChuaDong;
    private javax.swing.JLabel lblShowTongSoTienPhatDaDong;
    private javax.swing.JLabel lblSoLanMuon;
    private javax.swing.JLabel lblSoPhieuPhat;
    private javax.swing.JLabel lblSoSachDaMuon;
    private javax.swing.JLabel lblSoSachDangMuon;
    private javax.swing.JLabel lblTongSoTienPhatChuaDong;
    private javax.swing.JLabel lblTongSoTienPhatDaDong;
    private javax.swing.JPanel panelPagination;
    private javax.swing.JPanel panelSearch;
    private javax.swing.JScrollPane scrollPaneUsers;
    private javax.swing.JComboBox<String> searchByCombo;
    private javax.swing.JTable tblUsers;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
