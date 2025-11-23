/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.quanlythuvien.view.panel;

import com.mycompany.quanlythuvien.view.dialog.BanDocFormDialog;
import com.mycompany.quanlythuvien.controller.BanDocController;
import com.mycompany.quanlythuvien.model.BanDoc;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private JButton btnClearSearch;
    private boolean tableSelectionEnabled = true; // để tắt selection khi rỗng

    // ------------------ THÊM VÀO: phương thức khởi tạo UI bổ sung ------------------
    private void initUIEnhancements() {
        // --- tooltips + icons (nếu bạn có icon resource, load ở đây) ---
        btnAdd.setToolTipText("Thêm (Ctrl+N)");
        btnEdit.setToolTipText("Sửa (Ctrl+E)");
        btnDelete.setToolTipText("Xóa (Delete)");
        btnView.setToolTipText("Xem chi tiết (Double click)");
        txtSearch.setToolTipText("Nhập để tìm kiếm. Nhấn Enter để lọc ngay hoặc gõ để lọc tự động.");

        // --- clear search button (nhỏ, nằm cạnh txtSearch) ---
        btnClearSearch = new JButton("X");
        btnClearSearch.setMargin(new java.awt.Insets(2,6,2,6));
        btnClearSearch.setFocusable(false);
        btnClearSearch.setToolTipText("Xóa tìm kiếm");
        // add vào toolbar bên cạnh txtSearch (jToolBar1 là accessible)
        jToolBar1.add(btnClearSearch, jToolBar1.getComponentIndex(txtSearch) + 1);
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            txtSearchActionPerformed(null);
            txtSearch.requestFocusInWindow();
        });

        // --- debounce tìm kiếm bằng Swing Timer (300ms) ---
        searchTimer = new Timer(300, e -> {
            // thực sự chạy tìm
            txtSearchActionPerformed(null);
            searchTimer.stop();
        });
        searchTimer.setRepeats(false);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { docChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { docChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { docChanged(); }
            private void docChanged() {
                // nếu rỗng thì cho phép clear và hiển thị tất cả
                String txt = txtSearch.getText().trim();
                if (txt.isEmpty()) {
                    // show all immediately
                    searchTimer.stop();
                    showList(new ArrayList<>(safeGetDsBanDoc()));
                    txtSearchPrv = "";
                } else {
                    // restart debounce timer
                    searchTimer.restart();
                }
            }
        });

        // --- page size selector & goto page spinner (ở panelPagination) ---
        Integer[] sizes = new Integer[]{10, 20, 32, 50, 100};
        pageSizeCombo = new JComboBox<>(sizes);
        pageSizeCombo.setSelectedItem(pageSize);
        pageSizeCombo.setToolTipText("Số bản ghi mỗi trang");
        pageSizeCombo.addActionListener(e -> {
            Integer s = (Integer) pageSizeCombo.getSelectedItem();
            if (s != null && s > 0) {
                pageSize = s;
                // reset page về 1 để tránh trang vượt
                currentPage = 1;
                initPagination();
            }
        });
        // thêm vào panelPagination (thêm trước lblPageInfo)
        panelPagination.add(new JLabel(" / trang: "));
        panelPagination.add(pageSizeCombo);

        // spinner để nhảy trang nhanh
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
        panelPagination.add(new JLabel(" | Chuyển tới: "));
        panelPagination.add(gotoPageSpinner);

        // --- context menu cho table (right click) ---
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
                    tablePopup.show(e.getComponent(), e.getX(), e.getY());
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
        am.put("focusSearch", new AbstractAction(){ @Override public void actionPerformed(ActionEvent e) { txtSearch.requestFocusInWindow(); txtSearch.selectAll(); } });

        // --- nicer table behavior: column widths, selection mode already set ---
        tblUsers.setFillsViewportHeight(true);
        tblUsers.getColumnModel().getColumn(0).setPreferredWidth(60); // ID
        tblUsers.getColumnModel().getColumn(1).setPreferredWidth(180); // name
        tblUsers.getColumnModel().getColumn(2).setPreferredWidth(180); // email
        // ensure row height is comfortable
        tblUsers.setRowHeight(28);

        // double-click already wired to btnViewActionPerformed; keep it

        // --- khi cập nhật paging thì đồng bộ goto spinner ---
        // override loadPage to cập nhật gotoPageSpinner and enable/disable các nút
        // (mình sẽ cập nhật loadPage phía dưới; nếu không muốn sửa loadPage, gọi updatePagingControls() từ cuối loadPage)
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
        Search = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        searchByCombo = new javax.swing.JComboBox<>();
        scrollPaneUsers = new javax.swing.JScrollPane();
        tblUsers = new javax.swing.JTable();
        panelPagination = new javax.swing.JPanel();
        btnPrv = new javax.swing.JButton();
        lblPageInfo = new javax.swing.JLabel();
        btnNxt = new javax.swing.JButton();

        jToolBar1.setRollover(true);

        btnAdd.setText("Thêm");
        btnAdd.setFocusable(false);
        btnAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAdd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });
        jToolBar1.add(btnAdd);

        btnEdit.setText("Sửa");
        btnEdit.setFocusable(false);
        btnEdit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnEdit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        jToolBar1.add(btnEdit);

        btnDelete.setText("Xóa");
        btnDelete.setFocusable(false);
        btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        jToolBar1.add(btnDelete);

        btnView.setText("Xem chi tiết");
        btnView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewActionPerformed(evt);
            }
        });
        jToolBar1.add(btnView);

        Search.setText("Tìm kiếm");
        jToolBar1.add(Search);

        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });
        jToolBar1.add(txtSearch);

        searchByCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ID", "Họ Tên", "Email", "SĐT", "Địa Chỉ" }));
        searchByCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchByComboActionPerformed(evt);
            }
        });
        jToolBar1.add(searchByCombo);

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(scrollPaneUsers, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
            .addComponent(panelPagination, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(scrollPaneUsers, javax.swing.GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE)
                .addGap(12, 12, 12)
                .addComponent(panelPagination, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
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
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnNxt;
    private javax.swing.JButton btnPrv;
    private javax.swing.JButton btnView;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblPageInfo;
    private javax.swing.JPanel panelPagination;
    private javax.swing.JScrollPane scrollPaneUsers;
    private javax.swing.JComboBox<String> searchByCombo;
    private javax.swing.JTable tblUsers;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
