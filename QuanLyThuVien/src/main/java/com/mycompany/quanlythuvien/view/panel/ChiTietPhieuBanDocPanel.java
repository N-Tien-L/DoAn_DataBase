/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.quanlythuvien.view.panel;

import com.mycompany.quanlythuvien.controller.BanDocController;
import com.mycompany.quanlythuvien.dao.BanDocDAO;
import com.mycompany.quanlythuvien.model.BanDoc;
import com.mycompany.quanlythuvien.dao.ChiTietPhieuMuonDAO;
import com.mycompany.quanlythuvien.dao.SachDAO;
import java.awt.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ChiTietPhieuBanDocPanel extends javax.swing.JPanel {
    
    private javax.swing.table.TableRowSorter<DefaultTableModel> sorterPhieuMuon;
    private javax.swing.table.TableRowSorter<DefaultTableModel> sorterPhieuPhat;
    private CardLayout cardLayout;
    private BanDoc cur;
    Map<Integer, ArrayList<Object>> bookInfoCache = new HashMap<>();
    Map<Integer, Integer> bookInfoCacheIdPhat = new HashMap<>();
    private boolean detailVisible = false;
    private int lastSelectedModelRow = -1;
    private int savedDividerLocation = -1;
    private final int DETAIL_WIDTH = 340; // chỉnh theo mong muốn
    private final int MA_BAN_SAO_COL = 4; // cột MaBanSao ở tblPhieuMuon (0-based)

    // --- MỚI: cho PHIEU_PHAT (tách biệt trạng thái)
    private boolean detailVisible1 = false;
    private int lastSelectedModelRow1 = -1;
    private int savedDividerLocation1 = -1;
    private final int DETAIL_WIDTH1 = 340; // width bên phải cho phieu phat (có thể khác)
    private final int ID_PHAT_COL = 0; // cột chứa IdPhat trong tblPhieuPhat (0-based)

    // PHIEU_MUON keyset state
    private int pageIndexMuon = 0; // 0-based
    private java.util.List<Integer> pageLastIdsMuon = new ArrayList<>();
    private boolean hasMoreAfterMuon = false;
    private ArrayList<Object> pageCacheMuon = new ArrayList<>(); // lưu flattened cho page hiện tại

    // PHAT keyset state
    private int pageIndexPhat = 0;
    private java.util.List<Integer> pageLastIdsPhat = new ArrayList<>();
    private boolean hasMoreAfterPhat = false;
    private ArrayList<Object> pageCachePhat = new ArrayList<>();

    private int currentPagePhat = 1;
    private int pageSizePhat = 20; // rows per page, chỉnh theo ý
    private int totalRowsPhat = 0;
    private int totalPagesPhat = 1;
    // pagination for PHIEU_MUON (nếu muốn phân trang phiếu mượn)
    private int currentPageMuon = 1;
    private int pageSizeMuon = 20;
    private int totalRowsMuon = 0;
    private int totalPagesMuon = 1;
    private final int FIELDS_PER_RECORD = 8;
    // --- lưu cache kết quả để phân trang
    private ArrayList<Object> cachePhieuPhat = null;
    private ArrayList<Object> cachePhieuMuon = null;
    private int getColumnIndexForMuon(String by) {
        String k = normalizeKey(by);
        switch (k) {
            case "ID PHIEU MUON":
            case "ID PHIẾU MƯỢN": return 0;
            case "EMAIL THU THU LAP":
            case "EMAIL THỦ THƯ LẬP": return 1;
            case "NGAY MUON":
            case "NGÀY MƯỢN": return 2;
            case "HAN TRA":
            case "HẠN TRẢ": return 3;
            case "MA BAN SAO":
            case "MÃ BẢN SAO": return MA_BAN_SAO_COL;
            case "NGAY TRA":
            case "NGÀY TRẢ": return 5;
            case "TINH TRANG KHI TRA":
            case "TÌNH TRẠNG KHI TRẢ": return 6;
            case "EMAIL THU THU NHAN":
            case "EMAIL THỦ THƯ NHẬN": return 7;
            default: return 0;
        }
    }
    private int getColumnIndexForPhat(String by) {
        String k = normalizeKey(by);
        switch (k) {
            case "ID PHIEU PHAT":
            case "ID PHIẾU PHẠT": return ID_PHAT_COL;
            case "ID PHIEU MUON":
            case "ID PHIẾU MƯỢN": return 1;
            case "EMAIL THU THU LAP":
            case "EMAIL THỦ THƯ LẬP": return 2;
            case "NGAY MUON":
            case "NGÀY MƯỢN": return 3;
            case "LOAI PHAT":
            case "LOẠI PHẠT": return 4;
            case "SO TIEN":
            case "SỐ TIỀN": return 5;
            case "NGAY GHI NHAN":
            case "NGÀY GHI NHẬN": return 6;
            case "TRANG THAI DONG":
            case "TRẠNG THÁI ĐÓNG": return 7;
            default: return 0;
        }
    }
    private String escapeRegex(String s) {
        // tránh ký tự regex gây lỗi, giữ nguyên tìm kiếm "contains"
        return s.replaceAll("([\\\\\\[\\]{}()*+?^$.|])", "\\\\$1");
    }

    private void applyFilterMuon() {
        if (sorterPhieuMuon == null) return;
        String text = txtSearch1.getText();
        String by = (String)(searchByCombo1.getSelectedItem());
        if (text == null || text.trim().isEmpty()) {
            sorterPhieuMuon.setRowFilter(null);
            return;
        }
        text = text.trim();
        int col = getColumnIndexForMuon(by);
        // dùng RowFilter.regexFilter (case-insensitive) trên cột tương ứng
        try {
            String regex = "(?i).*" + escapeRegex(text) + ".*";
            sorterPhieuMuon.setRowFilter(RowFilter.regexFilter(regex, col));
        } catch (java.util.regex.PatternSyntaxException ex) {
            // fallback: chứa chuỗi (ignore regex errors)
            sorterPhieuMuon.setRowFilter(RowFilter.regexFilter("(?i).*" + Pattern.quote(text) + ".*", col));
        }
    }

    private void applyFilterPhat() {
        if (sorterPhieuPhat == null) return;
        String text = txtSearch.getText();
        String by = (String)(searchByCombo.getSelectedItem());
        if (text == null || text.trim().isEmpty()) {
            sorterPhieuPhat.setRowFilter(null);
            return;
        }
        text = text.trim();
        int col = getColumnIndexForPhat(by);
        try {
            String regex = "(?i).*" + escapeRegex(text) + ".*";
            sorterPhieuPhat.setRowFilter(RowFilter.regexFilter(regex, col));
        } catch (java.util.regex.PatternSyntaxException ex) {
            sorterPhieuPhat.setRowFilter(RowFilter.regexFilter("(?i).*" + Pattern.quote(text) + ".*", col));
        }
    }

    private void initTableFilters() {
        // tạo sorter dựa trên model hiện tại của table
        DefaultTableModel modelMuon = (DefaultTableModel) tblPhieuMuon.getModel();
        sorterPhieuMuon = new TableRowSorter<>(modelMuon);
        tblPhieuMuon.setRowSorter(sorterPhieuMuon);

        DefaultTableModel modelPhat = (DefaultTableModel) tblPhieuPhat.getModel();
        sorterPhieuPhat = new TableRowSorter<>(modelPhat);
        tblPhieuPhat.setRowSorter(sorterPhieuPhat);

        // DocumentListener cho text search (PhieuMuon)
        txtSearch1.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.   DocumentEvent e) { applyFilterMuon(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilterMuon(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilterMuon(); }
        });

        // DocumentListener cho text search (PhieuPhat)
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilterPhat(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilterPhat(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilterPhat(); }
        });

        // ComboBox change -> apply filter
        searchByCombo1.addActionListener(e -> applyFilterMuon());
        searchByCombo.addActionListener(e -> applyFilterPhat());

        // Clear buttons
        btnClearSearch1.addActionListener(e -> {
            txtSearch1.setText("");
            if (searchByCombo1.getItemCount() > 0) searchByCombo1.setSelectedIndex(0);
            sorterPhieuMuon.setRowFilter(null);
        });
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            if (searchByCombo.getItemCount() > 0) searchByCombo.setSelectedIndex(0);
            sorterPhieuPhat.setRowFilter(null);
        });
        btnClearSearch.setPreferredSize(new Dimension(24, 24));
        btnClearSearch.setMargin(new Insets(0, 0, 0, 0));
        btnClearSearch.setFont(new Font("Dialog", Font.BOLD, 12));
        btnClearSearch.setToolTipText("Clear search");
        btnClearSearch1.setPreferredSize(new Dimension(24, 24));
        btnClearSearch1.setMargin(new Insets(0, 0, 0, 0));
        btnClearSearch1.setFont(new Font("Dialog", Font.BOLD, 12));
        btnClearSearch1.setToolTipText("Clear search");
    }
    // --- helper: kiểm tra null/blank
    private boolean isNullOrBlankObj(Object o) {
        if (o == null) return true;
        String s = o.toString();
        return s == null || s.trim().isEmpty();
    }

    // --- helper: parse double an toàn từ Object (Number, BigDecimal, String)
    private double parseDoubleSafe(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        String s = o.toString().trim();
        if (s.isEmpty()) return 0.0;
        // loại bỏ ký tự không phải số/dấu chấm/dấu trừ (ví dụ currency, thousands separators)
        // chuyển dấu phẩy thành dấu chấm nếu có (ví dụ "1.234,56" -> "1234.56" => remove '.' then replace ',' -> '.')
        // Đơn giản: giữ các ký tự 0-9, dot, minus
        // Nhưng trước tiên thay dấu phẩy bằng dấu chấm nếu có, rồi xóa ký tự không số/dot/minus.
        s = s.replace('\u00A0', ' '); // non-breaking space
        // Nếu chuỗi có cả '.' và ',' khả năng '.' là thousands sep và ',' là decimal -> chuẩn hoá:
        if (s.indexOf('.') >= 0 && s.indexOf(',') >= 0) {
            s = s.replace(".", ""); // remove dots
            s = s.replace(',', '.'); // comma -> dot
        } else {
            // else thay mọi dấu phẩy thành dấu chấm (nếu người dùng lỡ dùng comma)
            s = s.replace(',', '.');
        }
        // xóa ký tự lạ
        s = s.replaceAll("[^0-9.\\-]", "");
        if (s.isEmpty() || s.equals(".") || s.equals("-") || s.equals("-.") ) return 0.0;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }
    // Cập nhật thống kê mượn (dùng BanDocDAO)
    void setThongKePhieuMuonBanDoc(final int idBD) {
        // hiển thị trạng thái loading
        lblShowSoLanMuon.setText("...");
        lblShowSoSachDaMuon.setText("...");
        lblShowSoSachDangMuon.setText("...");

        new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() {
                int soLanMuon = 0;
                int soSachDaMuon = 0;
                int soSachDangMuon = 0;
                try {
                    BanDocDAO dao = new BanDocDAO();
                    soLanMuon = dao.getSoLanMuonCuaBanDoc(idBD);
                    soSachDangMuon = dao.getSoSachDangMuonCuaBanDoc(idBD);
                    soSachDaMuon = dao.getSoSachDaMuonCuaBanDoc(idBD);
                } catch (Exception ex) {
                    ex.printStackTrace(); // hoặc log
                    // giữ 0 khi lỗi
                }
                return new int[]{soLanMuon, soSachDaMuon, soSachDangMuon};
            }

            @Override
            protected void done() {
                try {
                    int[] res = get();
                    lblShowSoLanMuon.setText(String.valueOf(res[0]));
                    lblShowSoSachDaMuon.setText(String.valueOf(res[1]));
                    lblShowSoSachDangMuon.setText(String.valueOf(res[2]));
                } catch (Exception ex) {
                    // fallback an toàn
                    lblShowSoLanMuon.setText("0");
                    lblShowSoSachDaMuon.setText("0");
                    lblShowSoSachDangMuon.setText("0");
                }
            }
        }.execute();
    }

    // Cập nhật thống kê phạt (dùng BanDocDAO)
    void setThongKePhieuPhatBanDoc(final int idBD) {
        // hiển thị trạng thái loading
        lblShowSoPhieuPhat.setText("...");
        lblShowTongSoTienPhatChuaDong.setText("...");
        lblShowTongSoTienPhatDaDong.setText("...");

        new SwingWorker<Object, Void>() {
            @Override
            protected Object doInBackground() {
                int soPhieuPhat = 0;
                double soTienPhatChuaDong = 0.0;
                double soTienPhatDaDong = 0.0;
                try {
                    BanDocDAO dao = new BanDocDAO();
                    soPhieuPhat = dao.getSoPhieuPhatBanDoc(idBD);
                    soTienPhatChuaDong = dao.getSoTienPhatChuaDongBanDoc(idBD);
                    soTienPhatDaDong = dao.getSoTienPhatDaDongBanDoc(idBD);
                } catch (Exception ex) {
                    ex.printStackTrace(); // hoặc log
                }
                return new Object[]{soPhieuPhat, soTienPhatChuaDong, soTienPhatDaDong};
            }

            @Override
            protected void done() {
                try {
                    Object[] res = (Object[]) get();
                    int soPhieuPhat = (res[0] instanceof Number) ? ((Number) res[0]).intValue() : 0;
                    double chuaDong = (res[1] instanceof Number) ? ((Number) res[1]).doubleValue() : 0.0;
                    double daDong = (res[2] instanceof Number) ? ((Number) res[2]).doubleValue() : 0.0;

                    java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
                    // hiển thị không có phần thập phân (nếu bạn muốn 2 chữ số thập phân -> setMaximumFractionDigits(2))
                    nf.setMaximumFractionDigits(0);
                    nf.setMinimumFractionDigits(0);

                    lblShowSoPhieuPhat.setText(String.valueOf(soPhieuPhat));
                    lblShowTongSoTienPhatChuaDong.setText(nf.format(chuaDong));
                    lblShowTongSoTienPhatDaDong.setText(nf.format(daDong));
                } catch (Exception ex) {
                    lblShowSoPhieuPhat.setText("0");
                    lblShowTongSoTienPhatChuaDong.setText("0");
                    lblShowTongSoTienPhatDaDong.setText("0");
                }
            }
        }.execute();
    }



    private String safeToStr(Object o) {
        return (o == null) ? "" : o.toString();
    }

    void loadPhieuMuon() {
        lastSelectedModelRow = -1;
        hideDetail();

        // prepare cursor for current page
        Integer lastId = null;
        if (pageIndexMuon > 0 && pageLastIdsMuon.size() >= pageIndexMuon) {
            lastId = pageLastIdsMuon.get(pageIndexMuon - 1);
        }

        final Integer cursor = lastId;
        final String searchTextArg = txtSearch1.getText().trim().isEmpty() ? null : txtSearch1.getText().trim();

        new javax.swing.SwingWorker<ArrayList<Object>, Void>() {
            @Override
            protected ArrayList<Object> doInBackground() throws Exception {
                // request pageSizeMuon + 1 records (flattened => +1 * FIELDS_PER_RECORD)
                BanDocController tmp = new BanDocController();
                return tmp.getPhieuMuonPageByBanDoc(cur.getIdBD(), pageSizeMuon + 1, cursor, searchTextArg);
            }

            @Override
            protected void done() {
                try {
                    ArrayList<Object> rows = get();
                    if (rows == null) rows = new ArrayList<>();

                    // rows is flattened => number of records = rows.size() / FIELDS_PER_RECORD
                    int totalFields = rows.size();
                    int recordCount = (FIELDS_PER_RECORD == 0) ? 0 : totalFields / FIELDS_PER_RECORD;

                    hasMoreAfterMuon = recordCount > pageSizeMuon;

                    // number of records to actually show on this page
                    int showRecords = Math.min(recordCount, pageSizeMuon);
                    int showFields = showRecords * FIELDS_PER_RECORD;

                    ArrayList<Object> toShow = new ArrayList<>();
                    if (showFields > 0) {
                        toShow.addAll(rows.subList(0, Math.min(showFields, rows.size())));
                    }

                    pageCacheMuon = toShow;

                    // cập nhật cursor stack: last Id trên page hiện tại (IdPM nằm ở base index mỗi record)
                    if (showRecords > 0) {
                        int lastRecordBase = (showRecords - 1) * FIELDS_PER_RECORD;
                        Object lastIdObj = toShow.get(lastRecordBase); // IdPM
                        Integer last = null;
                        try { last = Integer.parseInt(String.valueOf(lastIdObj)); } catch (Exception ignored) {}
                        while (pageLastIdsMuon.size() > pageIndexMuon) pageLastIdsMuon.remove(pageLastIdsMuon.size() - 1);
                        if (last != null) pageLastIdsMuon.add(last);
                    } else {
                        while (pageLastIdsMuon.size() > pageIndexMuon) pageLastIdsMuon.remove(pageLastIdsMuon.size() - 1);
                    }

                    // render the page
                    renderPagePhieuMuon();

                    // update controls
                    btnPrv.setEnabled(pageIndexMuon > 0);
                    btnNxt.setEnabled(hasMoreAfterMuon);
                    lblPageInfo.setText(String.format("Trang %d%s", pageIndexMuon + 1, hasMoreAfterMuon ? " (còn trang sau)" : ""));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }


    private Object safeGetFromCache(ArrayList<Object> cache, int idx) {
        if (cache == null) return null;
        if (idx < 0 || idx >= cache.size()) return null;
        return cache.get(idx);
    }

    private void renderPagePhieuMuon() {
        DefaultTableModel model = (DefaultTableModel) tblPhieuMuon.getModel();
        model.setRowCount(0);

        if (cur == null || pageCacheMuon == null || pageCacheMuon.isEmpty()) {
            lblPageInfo.setText("Trang 0/0");
            btnPrv.setEnabled(false);
            btnNxt.setEnabled(false);
            lastSelectedModelRow = -1;
            hideDetail();
            return;
        }

        int fields = FIELDS_PER_RECORD;
        for (int rec = 0; rec < pageCacheMuon.size() / fields; ++rec) {
            int base = rec * fields;
            model.addRow(new Object[] {
                safeToStr(pageCacheMuon.get(base)),
                safeToStr(pageCacheMuon.get(base + 1)),
                safeToStr(pageCacheMuon.get(base + 2)),
                safeToStr(pageCacheMuon.get(base + 3)),
                safeToStr(pageCacheMuon.get(base + 4)),
                safeToStr(pageCacheMuon.get(base + 5)),
                safeToStr(pageCacheMuon.get(base + 6)),
                safeToStr(pageCacheMuon.get(base + 7))
            });
        }

        lastSelectedModelRow = -1;
        hideDetail();

        int startRecord = pageIndexMuon * pageSizeMuon + 1;
        int endRecord = pageIndexMuon * pageSizeMuon + (pageCacheMuon.size() / fields);
        lblPageInfo.setText(String.format("Trang %d (Hiển thị %d-%d / trang)", pageIndexMuon + 1, startRecord, endRecord));
        btnPrv.setEnabled(pageIndexMuon > 0);
        btnNxt.setEnabled(hasMoreAfterMuon);

    }


    void loadPhieuPhat() throws Exception {
        lastSelectedModelRow1 = -1;
        hideDetail1();

        Integer lastId = null;
        if (pageIndexPhat > 0 && pageLastIdsPhat.size() >= pageIndexPhat) {
            lastId = pageLastIdsPhat.get(pageIndexPhat - 1);
        }
        final Integer cursor = lastId;
        final String searchTextArg = txtSearch.getText().trim().isEmpty() ? null : txtSearch.getText().trim();

        new javax.swing.SwingWorker<ArrayList<Object>, Void>() {
            @Override
            protected ArrayList<Object> doInBackground() throws Exception {
                BanDocController tmp = new BanDocController();
                return tmp.getPhieuPhatPageByBanDoc(cur.getIdBD(), pageSizePhat + 1, cursor, searchTextArg);
            }

            @Override
            protected void done() {
                try {
                    ArrayList<Object> rows = get();
                    if (rows == null) rows = new ArrayList<>();

                    int totalFields = rows.size();
                    int recordCount = (FIELDS_PER_RECORD == 0) ? 0 : totalFields / FIELDS_PER_RECORD;

                    hasMoreAfterPhat = recordCount > pageSizePhat;

                    int showRecords = Math.min(recordCount, pageSizePhat);
                    int showFields = showRecords * FIELDS_PER_RECORD;

                    ArrayList<Object> toShow = new ArrayList<>();
                    if (showFields > 0) {
                        toShow.addAll(rows.subList(0, Math.min(showFields, rows.size())));
                    }

                    pageCachePhat = toShow;

                    if (showRecords > 0) {
                        int lastRecordBase = (showRecords - 1) * FIELDS_PER_RECORD;
                        Object lastIdObj = toShow.get(lastRecordBase);
                        Integer last = null;
                        try { last = Integer.parseInt(String.valueOf(lastIdObj)); } catch (Exception ignored) {}
                        while (pageLastIdsPhat.size() > pageIndexPhat) pageLastIdsPhat.remove(pageLastIdsPhat.size() - 1);
                        if (last != null) pageLastIdsPhat.add(last);
                    } else {
                        while (pageLastIdsPhat.size() > pageIndexPhat) pageLastIdsPhat.remove(pageLastIdsPhat.size() - 1);
                    }

                    renderPagePhieuPhat();

                    btnPrv1.setEnabled(pageIndexPhat > 0);
                    btnNxt1.setEnabled(hasMoreAfterPhat);
                    lblPageInfo1.setText(String.format("Trang %d%s", pageIndexPhat + 1, hasMoreAfterPhat ? " (còn trang sau)" : ""));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }


    private void renderPagePhieuPhat() {
        DefaultTableModel model = (DefaultTableModel) tblPhieuPhat.getModel();
        model.setRowCount(0);
        if (cur == null || pageCachePhat == null || pageCachePhat.isEmpty()) {
            lblPageInfo1.setText("Trang 0/0");
            btnPrv1.setEnabled(false);
            btnNxt1.setEnabled(false);
            lastSelectedModelRow1 = -1;
            hideDetail1();
            return;
        }

        int fields = FIELDS_PER_RECORD;
        for (int rec = 0; rec < pageCachePhat.size() / fields; ++rec) {
            int base = rec * fields;
            model.addRow(new Object[] {
                safeToStr(pageCachePhat.get(base)),
                safeToStr(pageCachePhat.get(base + 1)),
                safeToStr(pageCachePhat.get(base + 2)),
                safeToStr(pageCachePhat.get(base + 3)),
                safeToStr(pageCachePhat.get(base + 4)),
                safeToStr(pageCachePhat.get(base + 5)),
                safeToStr(pageCachePhat.get(base + 6)),
                safeToStr(pageCachePhat.get(base + 7))
            });
        }

        lastSelectedModelRow1 = -1;
        hideDetail1();

        int startRecord = pageIndexPhat * pageSizePhat + 1;
        int endRecord = pageIndexPhat * pageSizePhat + (pageCachePhat.size() / fields);
        lblPageInfo1.setText(String.format("Trang %d (Hiển thị %d-%d / trang)", pageIndexPhat + 1, startRecord, endRecord));
        btnPrv1.setEnabled(pageIndexPhat > 0);
        btnNxt1.setEnabled(hasMoreAfterPhat);
    }

    /**
     * Creates new form ChiTietPhieuBanDocPanel
     */
    public ChiTietPhieuBanDocPanel() {
        initComponents();
    }
    // helper để lấy giá trị từ TableModel an toàn
    private Object safeGet(javax.swing.table.TableModel model, int row, int col) {
        if (model == null) return null;
        if (row < 0 || row >= model.getRowCount()) return null;
        if (col < 0 || col >= model.getColumnCount()) return null;
        return model.getValueAt(row, col);
    }
    // Lấy info sách an toàn từ maBanSao (kiểm tra null / -1)
    private ArrayList<Object> getBookInfoByMaBanSaoSafe(Integer maBanSao) {
        if (maBanSao == null || maBanSao <= 0) return null;
        if (bookInfoCache.containsKey(maBanSao)) return bookInfoCache.get(maBanSao);
        try {
            BanDocDAO tmp = new BanDocDAO();
            ArrayList<Object> info = tmp.getSomeInfoSachByMaBanSao(maBanSao);
            if (info != null) bookInfoCache.put(maBanSao, info);
            return info;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    private void showDetail() {
        if (splitMain == null) return;
        // nếu form mới vừa mở và width = 0, postpone để tránh set sai vị trí
        if (splitMain.getWidth() <= 0) {
            SwingUtilities.invokeLater(() -> {
                savedDividerLocation = (savedDividerLocation == -1) ? splitMain.getDividerLocation() : savedDividerLocation;
                int total = splitMain.getWidth();
                int newDivider = Math.max(0, total - DETAIL_WIDTH);
                splitMain.setDividerLocation(newDivider);
                pnlDetailBook.setVisible(true);
                detailVisible = true;
                splitMain.revalidate();
                splitMain.repaint();
            });
            return;
        }
        if (!detailVisible) {
            if (savedDividerLocation == -1) savedDividerLocation = splitMain.getDividerLocation();
            int total = splitMain.getWidth();
            int newDivider = Math.max(0, total - DETAIL_WIDTH);
            splitMain.setDividerLocation(newDivider);
            pnlDetailBook.setVisible(true);
            detailVisible = true;
        } else {
            pnlDetailBook.setVisible(true);
        }
        splitMain.revalidate();
        splitMain.repaint();
    }
    private void hideDetail() {
        if (splitMain == null) return;
        if (savedDividerLocation != -1) {
            splitMain.setDividerLocation(savedDividerLocation);
        } else {
            // fallback: cho left chiếm 70%
            splitMain.setDividerLocation((int)(splitMain.getWidth() * 0.7));
        }
        pnlDetailBook.setVisible(false);
        detailVisible = false;
        splitMain.revalidate();
        splitMain.repaint();
    }
    private void populateDetailPanel(ArrayList<Object> info) {
        // info: [ISBN, TenSach, TenTacGia, TenTheLoai, TenNXB, NamXuatBan]
        if (info == null || info.isEmpty()) { clearBookDetail(); return; }
        lblDetailISBN.setText(info.size() > 0 && info.get(0) != null ? info.get(0).toString() : "");
        lblDetailTenSach.setText(info.size() > 1 && info.get(1) != null ? info.get(1).toString() : "");
        lblDetailTacGia.setText(info.size() > 2 && info.get(2) != null ? info.get(2).toString() : "");
        lblDetailTheLoai.setText(info.size() > 3 && info.get(3) != null ? info.get(3).toString() : "");
        lblDetailNXB.setText(info.size() > 4 && info.get(4) != null ? info.get(4).toString() : "");
        lblDetailNamXB.setText(info.size() > 5 && info.get(5) != null ? String.valueOf(info.get(5)) : "");
    }
    private void clearBookDetail() {
        lblDetailISBN.setText("");
        lblDetailTenSach.setText("");
        lblDetailTacGia.setText("");
        lblDetailTheLoai.setText("");
        lblDetailNXB.setText("");
        lblDetailNamXB.setText("");
    }
    private void showDetail1() {
        if (splitMain1 == null) return;
        if (splitMain1.getWidth() <= 0) {
            SwingUtilities.invokeLater(() -> {
                savedDividerLocation1 = (savedDividerLocation1 == -1) ? splitMain1.getDividerLocation() : savedDividerLocation1;
                int total = splitMain1.getWidth();
                int newDivider = Math.max(0, total - DETAIL_WIDTH1);
                splitMain1.setDividerLocation(newDivider);
                pnlDetailBook1.setVisible(true);
                detailVisible1 = true;
                splitMain1.revalidate();
                splitMain1.repaint();
            });
            return;
        }
        if (!detailVisible1) {
            if (savedDividerLocation1 == -1) savedDividerLocation1 = splitMain1.getDividerLocation();
            int total = splitMain1.getWidth();
            int newDivider = Math.max(0, total - DETAIL_WIDTH1);
            splitMain1.setDividerLocation(newDivider);
            pnlDetailBook1.setVisible(true);
            detailVisible1 = true;
        } else {
            pnlDetailBook1.setVisible(true);
        }
        splitMain1.revalidate();
        splitMain1.repaint();
    }
    private void hideDetail1() {
        if (splitMain1 == null) return;
        if (savedDividerLocation1 != -1) {
            splitMain1.setDividerLocation(savedDividerLocation1);
        } else {
            splitMain1.setDividerLocation((int)(splitMain1.getWidth() * 0.7));
        }
        pnlDetailBook1.setVisible(false);
        detailVisible1 = false;
        splitMain1.revalidate();
        splitMain1.repaint();
    }
    private void populateDetailPanel1(ArrayList<Object> info) {
        if (info == null || info.isEmpty()) { clearBookDetail1(); return; }

        lblDetailISBN1.setText(info.size() > 0 && info.get(0) != null ? info.get(0).toString() : "");
        lblDetailTenSach1.setText(info.size() > 1 && info.get(1) != null ? info.get(1).toString() : "");
        lblDetailTacGia1.setText(info.size() > 2 && info.get(2) != null ? info.get(2).toString() : "");
        lblDetailTheLoai1.setText(info.size() > 3 && info.get(3) != null ? info.get(3).toString() : "");
        lblDetailNXB1.setText(info.size() > 4 && info.get(4) != null ? info.get(4).toString() : "");
        lblDetailNamXB1.setText(info.size() > 5 && info.get(5) != null ? String.valueOf(info.get(5)) : "");
    }
    private void clearBookDetail1() {
        lblDetailISBN1.setText("");
        lblDetailTenSach1.setText("");
        lblDetailTacGia1.setText("");
        lblDetailTheLoai1.setText("");
        lblDetailNXB1.setText("");
        lblDetailNamXB1.setText("");
    }
    public ChiTietPhieuBanDocPanel(BanDoc x) throws Exception {
        initComponents(); 
        initTableFilters();
        cur = x;
        cardLayout = (CardLayout) pnlCards.getLayout();
        cardLayout.show(pnlCards, "PHIEU_MUON");
        btnPhieuMuon.setSelected(true);
        loadPhieuMuon();
        pnlDetailBook.setVisible(false);
        pnlDetailBook1.setVisible(false);
        // ---------------------------
        // PHIEU_MUON listeners
        // ---------------------------
        tblPhieuMuon.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = tblPhieuMuon.getSelectedRow();
            if (viewRow == -1) return;
            int modelRow = tblPhieuMuon.convertRowIndexToModel(viewRow);
            // toggle: click lại cùng row sẽ ẩn (chỉ khi người dùng dùng keyboard để thay đổi selection)
            if (modelRow == lastSelectedModelRow && detailVisible) {
                hideDetail();
                lastSelectedModelRow = -1;
                tblPhieuMuon.clearSelection();
                return;
            }
            // nếu chọn hàng mới
            lastSelectedModelRow = modelRow;
            // lấy MaBanSao từ table model (đổi MA_BAN_SAO_COL nếu khác)
            Object maBanSaoObj = safeGet(tblPhieuMuon.getModel(), modelRow, MA_BAN_SAO_COL);
            if (maBanSaoObj != null) {
                try {
                    int maBanSao = Integer.parseInt(String.valueOf(maBanSaoObj));
                    ArrayList<Object> info = getBookInfoByMaBanSaoSafe(maBanSao);
                    populateDetailPanel(info);
                } catch (NumberFormatException ex) {
                    clearBookDetail();
                }
            } else {
                clearBookDetail();
            }
            showDetail();
        });
        tblPhieuMuon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int viewRow = tblPhieuMuon.rowAtPoint(e.getPoint());
                if (viewRow == -1) return;
                int modelRow = tblPhieuMuon.convertRowIndexToModel(viewRow);

                // nếu click vào hàng đã chọn trước đó -> toggle off
                if (modelRow == lastSelectedModelRow && detailVisible) {
                    hideDetail();
                    lastSelectedModelRow = -1;
                    tblPhieuMuon.clearSelection();
                    return;
                }

                // khác hàng hiện tại -> chọn và show
                lastSelectedModelRow = modelRow;
                // set selection using view index
                tblPhieuMuon.setRowSelectionInterval(viewRow, viewRow);
                Object maBanSaoObj = safeGet(tblPhieuMuon.getModel(), modelRow, MA_BAN_SAO_COL);
                if (maBanSaoObj != null) {
                    try {
                        int maBanSao = Integer.parseInt(String.valueOf(maBanSaoObj));
                        ArrayList<Object> info = getBookInfoByMaBanSaoSafe(maBanSao);
                        populateDetailPanel(info);
                    } catch (NumberFormatException ex) {
                        clearBookDetail();
                    }
                } else {
                    clearBookDetail();
                }
                showDetail();
            }
        });
        // ---------------------------
        // PHIEU_PHAT listeners
        // ---------------------------
        tblPhieuPhat.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = tblPhieuPhat.getSelectedRow();
            if (viewRow == -1) return;
            int modelRow = tblPhieuPhat.convertRowIndexToModel(viewRow);
            if (modelRow == lastSelectedModelRow1 && detailVisible1) {
                // nếu selection không thay đổi và hiện rồi thì không tắt ở đây
                return;
            }
            lastSelectedModelRow1 = modelRow;
            Object idPhatObj = safeGet(tblPhieuPhat.getModel(), modelRow, ID_PHAT_COL);
            if (idPhatObj != null) {
                try {
                    int idPhat = Integer.parseInt(String.valueOf(idPhatObj));
                    ArrayList<Object> info = null;
                    if(!bookInfoCacheIdPhat.containsKey(idPhat)) {
                        BanDocDAO tmp = new BanDocDAO();
                        int maBanSao = tmp.getMaBanSaoByIdPhat(idPhat);
                        if (maBanSao > 0) {
                            info = getBookInfoByMaBanSaoSafe(maBanSao);
                            if (info != null) {
                                bookInfoCacheIdPhat.put(idPhat, maBanSao);
                            }
                        }
                    } else {
                        Integer maBanSao = bookInfoCacheIdPhat.get(idPhat);
                        info = (maBanSao != null) ? bookInfoCache.get(maBanSao) : null;
                    }
                    populateDetailPanel1(info);
                } catch (NumberFormatException ex) {
                    clearBookDetail1();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    clearBookDetail1();
                }
            } else {
                clearBookDetail1();
            }
            showDetail1();
        });
        tblPhieuPhat.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int viewRow = tblPhieuPhat.rowAtPoint(e.getPoint());
                if (viewRow == -1) return;
                int modelRow = tblPhieuPhat.convertRowIndexToModel(viewRow);

                if (modelRow == lastSelectedModelRow1 && detailVisible1) {
                    // toggle off
                    hideDetail1();
                    lastSelectedModelRow1 = -1;
                    tblPhieuPhat.clearSelection();
                    return;
                }
                // khác -> chọn và show
                lastSelectedModelRow1 = modelRow;
                tblPhieuPhat.setRowSelectionInterval(viewRow, viewRow);
                Object idPhatObj = safeGet(tblPhieuPhat.getModel(), modelRow, ID_PHAT_COL);
                if (idPhatObj != null) {
                    try {
                        int idPhat = Integer.parseInt(String.valueOf(idPhatObj));

                        ArrayList<Object> info = null;
                        if(!bookInfoCacheIdPhat.containsKey(idPhat)) {
                            BanDocDAO tmp = new BanDocDAO();
                            int maBanSao = tmp.getMaBanSaoByIdPhat(idPhat);
                            if (maBanSao > 0) {
                                info = getBookInfoByMaBanSaoSafe(maBanSao);
                                if (info != null) bookInfoCacheIdPhat.put(idPhat, maBanSao);
                            }
                        } else {
                            Integer maBanSao = bookInfoCacheIdPhat.get(idPhat);
                            info = (maBanSao != null) ? bookInfoCache.get(maBanSao) : null;
                        }

                        populateDetailPanel1(info);
                    } catch (NumberFormatException ex) {
                        clearBookDetail1();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        clearBookDetail1();
                    }
                } else {
                    clearBookDetail1();
                }
                showDetail1();
            }
        });
  
        tblPhieuMuon.setRowHeight(26);
        tblPhieuMuon.setIntercellSpacing(new Dimension(6,6));
        tblPhieuMuon.setGridColor(new Color(230,230,230));
        tblPhieuMuon.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) { setBackground(new Color(51, 153, 255)); setForeground(Color.white); }
                else { setBackground(row % 2 == 0 ? Color.white : new Color(248, 249, 250)); setForeground(Color.darkGray); }
                return this;
            }
        });
        // header style
        tblPhieuMuon.getTableHeader().setFont(tblPhieuMuon.getTableHeader().getFont().deriveFont(Font.BOLD));
        ((DefaultTableCellRenderer) tblPhieuMuon.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        // center ID column
        if (tblPhieuMuon.getColumnModel().getColumnCount() > 0) {
            DefaultTableCellRenderer center = new DefaultTableCellRenderer();
            center.setHorizontalAlignment(SwingConstants.CENTER);
            tblPhieuMuon.getColumnModel().getColumn(0).setCellRenderer(center);
        }
        // alternating rows + selection color
        tblPhieuPhat.setRowHeight(26);
        tblPhieuPhat.setIntercellSpacing(new Dimension(6,6));
        tblPhieuPhat.setGridColor(new Color(230,230,230));
        tblPhieuPhat.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) { setBackground(new Color(51, 153, 255)); setForeground(Color.white); }
                else { setBackground(row % 2 == 0 ? Color.white : new Color(248, 249, 250)); setForeground(Color.darkGray); }
                return this;
            }
        });
        // header style
        tblPhieuPhat.getTableHeader().setFont(tblPhieuPhat.getTableHeader().getFont().deriveFont(Font.BOLD));
        ((DefaultTableCellRenderer) tblPhieuPhat.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        // center ID column
        if (tblPhieuPhat.getColumnModel().getColumnCount() > 0) {
            DefaultTableCellRenderer center = new DefaultTableCellRenderer();
            center.setHorizontalAlignment(SwingConstants.CENTER);
            tblPhieuPhat.getColumnModel().getColumn(0).setCellRenderer(center);
        }
        setThongKePhieuMuonBanDoc(cur.getIdBD());
        setThongKePhieuPhatBanDoc(cur.getIdBD());
    }
    private String normalizeKey(String s) {
        if (s == null) return "";
        // bỏ dấu, xóa khoảng trắng thừa, chuyển uppercase
        String n = Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
                             .replaceAll("\\p{M}", ""); // remove diacritics
        n = n.replaceAll("\\s+", " ").toUpperCase();
        return n;
    }
    

    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        btnPhieuMuon = new javax.swing.JToggleButton();
        btnPhieuPhat = new javax.swing.JToggleButton();
        pnlCards = new javax.swing.JPanel();
        cardPhieuPhat = new javax.swing.JPanel();
        lblTongSoTienPhatDaDong = new javax.swing.JLabel();
        lblTongSoTienPhatChuaDong = new javax.swing.JLabel();
        lblSoPhieuPhat = new javax.swing.JLabel();
        lblShowSoPhieuPhat = new javax.swing.JLabel();
        lblShowTongSoTienPhatChuaDong = new javax.swing.JLabel();
        lblShowTongSoTienPhatDaDong = new javax.swing.JLabel();
        splitMain1 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblPhieuPhat = new javax.swing.JTable();
        pnlDetailBook1 = new javax.swing.JPanel();
        lblDetailISBN1 = new javax.swing.JLabel();
        lblDetailTenSach1 = new javax.swing.JLabel();
        lblDetailTacGia1 = new javax.swing.JLabel();
        lblDetailTheLoai1 = new javax.swing.JLabel();
        lblDetailNXB1 = new javax.swing.JLabel();
        lblDetailNamXB1 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        btnPrv1 = new javax.swing.JButton();
        lblPageInfo1 = new javax.swing.JLabel();
        btnNxt1 = new javax.swing.JButton();
        panelSearch = new javax.swing.JPanel();
        Search = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        btnClearSearch = new javax.swing.JButton();
        searchByCombo = new javax.swing.JComboBox<>();
        cardPhieuMuon = new javax.swing.JPanel();
        lblSoSachDangMuon = new javax.swing.JLabel();
        lblSoSachDaMuon = new javax.swing.JLabel();
        lblSoLanMuon = new javax.swing.JLabel();
        lblShowSoLanMuon = new javax.swing.JLabel();
        lblShowSoSachDaMuon = new javax.swing.JLabel();
        lblShowSoSachDangMuon = new javax.swing.JLabel();
        splitMain = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPhieuMuon = new javax.swing.JTable();
        pnlDetailBook = new javax.swing.JPanel();
        lblDetailISBN = new javax.swing.JLabel();
        lblDetailTenSach = new javax.swing.JLabel();
        lblDetailTacGia = new javax.swing.JLabel();
        lblDetailTheLoai = new javax.swing.JLabel();
        lblDetailNXB = new javax.swing.JLabel();
        lblDetailNamXB = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnPrv = new javax.swing.JButton();
        lblPageInfo = new javax.swing.JLabel();
        btnNxt = new javax.swing.JButton();
        panelSearch1 = new javax.swing.JPanel();
        Search1 = new javax.swing.JLabel();
        txtSearch1 = new javax.swing.JTextField();
        btnClearSearch1 = new javax.swing.JButton();
        searchByCombo1 = new javax.swing.JComboBox<>();

        buttonGroup1.add(btnPhieuMuon);
        btnPhieuMuon.setText("Phiếu Mượn");
        btnPhieuMuon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPhieuMuonActionPerformed(evt);
            }
        });

        buttonGroup1.add(btnPhieuPhat);
        btnPhieuPhat.setText("Phiếu Phạt");
        btnPhieuPhat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPhieuPhatActionPerformed(evt);
            }
        });

        pnlCards.setLayout(new java.awt.CardLayout());

        lblTongSoTienPhatDaDong.setText("Tổng Số Tiền Phạt Đã Đóng:");

        lblTongSoTienPhatChuaDong.setText("Tổng Số Tiền Phạt Chưa Đóng:");

        lblSoPhieuPhat.setText("Số Phiếu Phạt:");

        tblPhieuPhat.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID Phiếu Phạt", "ID Phiếu Mượn", "Email Thủ Thư Lập", "Ngày Mượn", "Loại Phạt", "Số Tiền", "Ngày Ghi Nhận", "Trạng Thái Đóng"
            }
        ));
        jScrollPane2.setViewportView(tblPhieuPhat);

        splitMain1.setLeftComponent(jScrollPane2);

        lblDetailISBN1.setText("jLabel1");

        lblDetailTenSach1.setText("jLabel1");

        lblDetailTacGia1.setText("jLabel1");

        lblDetailTheLoai1.setText("jLabel1");

        lblDetailNXB1.setText("jLabel1");

        lblDetailNamXB1.setText("jLabel1");

        jLabel8.setText("Chi Tiết Sách Bạn Đọc Mượn");

        jLabel9.setText("Mã số sách:");

        jLabel10.setText("Tên sách:");

        jLabel11.setText("Tác giả:");

        jLabel12.setText("Thể loại:");

        jLabel13.setText("Nhà xuất bản:");

        jLabel14.setText("Năm xuất bản:");

        javax.swing.GroupLayout pnlDetailBook1Layout = new javax.swing.GroupLayout(pnlDetailBook1);
        pnlDetailBook1.setLayout(pnlDetailBook1Layout);
        pnlDetailBook1Layout.setHorizontalGroup(
            pnlDetailBook1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetailBook1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDetailBook1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addGroup(pnlDetailBook1Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDetailISBN1))
                    .addGroup(pnlDetailBook1Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDetailTenSach1))
                    .addGroup(pnlDetailBook1Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDetailTacGia1))
                    .addGroup(pnlDetailBook1Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDetailTheLoai1))
                    .addGroup(pnlDetailBook1Layout.createSequentialGroup()
                        .addGroup(pnlDetailBook1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlDetailBook1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDetailNamXB1)
                            .addComponent(lblDetailNXB1))))
                .addContainerGap(709, Short.MAX_VALUE))
        );
        pnlDetailBook1Layout.setVerticalGroup(
            pnlDetailBook1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetailBook1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addGap(18, 18, 18)
                .addGroup(pnlDetailBook1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(lblDetailISBN1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetailBook1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(lblDetailTenSach1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetailBook1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(lblDetailTacGia1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetailBook1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(lblDetailTheLoai1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetailBook1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(lblDetailNXB1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetailBook1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(lblDetailNamXB1))
                .addContainerGap(261, Short.MAX_VALUE))
        );

        splitMain1.setRightComponent(pnlDetailBook1);

        btnPrv1.setText("Trang trước");
        btnPrv1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrv1ActionPerformed(evt);
            }
        });
        jPanel1.add(btnPrv1);

        lblPageInfo1.setText("Trang 1/1");
        jPanel1.add(lblPageInfo1);

        btnNxt1.setText("Trang sau");
        btnNxt1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNxt1ActionPerformed(evt);
            }
        });
        jPanel1.add(btnNxt1);

        panelSearch.setToolTipText("");
        panelSearch.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        panelSearch.setOpaque(false);
        panelSearch.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 10));

        Search.setText("Tìm kiếm:");
        panelSearch.add(Search);

        txtSearch.setToolTipText("");
        txtSearch.setActionCommand("<Not Set>");
        txtSearch.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtSearch.setPreferredSize(new java.awt.Dimension(200, 26));
        panelSearch.add(txtSearch);

        btnClearSearch.setText("X");
        btnClearSearch.setBorderPainted(false);
        btnClearSearch.setContentAreaFilled(false);
        btnClearSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnClearSearch.setPreferredSize(new java.awt.Dimension(26, 26));
        btnClearSearch.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnClearSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearSearchActionPerformed(evt);
            }
        });
        panelSearch.add(btnClearSearch);

        searchByCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ID Phiếu Phạt", "ID Phiếu Mượn", "Email Thủ Thư Lập", "Ngày Mượn", "Loại Phạt", "Số Tiền", "Ngày Ghi Nhận", "Trạng Thái Đóng" }));
        searchByCombo.setMinimumSize(new java.awt.Dimension(120, 26));
        panelSearch.add(searchByCombo);

        javax.swing.GroupLayout cardPhieuPhatLayout = new javax.swing.GroupLayout(cardPhieuPhat);
        cardPhieuPhat.setLayout(cardPhieuPhatLayout);
        cardPhieuPhatLayout.setHorizontalGroup(
            cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPhieuPhatLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cardPhieuPhatLayout.createSequentialGroup()
                        .addComponent(lblSoPhieuPhat)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblShowSoPhieuPhat, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(cardPhieuPhatLayout.createSequentialGroup()
                        .addComponent(lblTongSoTienPhatDaDong)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblShowTongSoTienPhatDaDong))
                    .addGroup(cardPhieuPhatLayout.createSequentialGroup()
                        .addComponent(lblTongSoTienPhatChuaDong)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblShowTongSoTienPhatChuaDong)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1081, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(cardPhieuPhatLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(splitMain1)
                    .addContainerGap()))
            .addGroup(cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardPhieuPhatLayout.createSequentialGroup()
                    .addContainerGap(425, Short.MAX_VALUE)
                    .addComponent(panelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(425, Short.MAX_VALUE)))
        );
        cardPhieuPhatLayout.setVerticalGroup(
            cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardPhieuPhatLayout.createSequentialGroup()
                .addContainerGap(559, Short.MAX_VALUE)
                .addGroup(cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cardPhieuPhatLayout.createSequentialGroup()
                        .addGroup(cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSoPhieuPhat)
                            .addComponent(lblShowSoPhieuPhat))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTongSoTienPhatChuaDong)
                            .addComponent(lblShowTongSoTienPhatChuaDong))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblTongSoTienPhatDaDong)
                            .addComponent(lblShowTongSoTienPhatDaDong)))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(71, 71, 71))
            .addGroup(cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardPhieuPhatLayout.createSequentialGroup()
                    .addContainerGap(131, Short.MAX_VALUE)
                    .addComponent(splitMain1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(132, Short.MAX_VALUE)))
            .addGroup(cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardPhieuPhatLayout.createSequentialGroup()
                    .addContainerGap(84, Short.MAX_VALUE)
                    .addComponent(panelSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(560, Short.MAX_VALUE)))
        );

        pnlCards.add(cardPhieuPhat, "PHIEU_PHAT");

        lblSoSachDangMuon.setText("Số Sách Đang Mượn:");

        lblSoSachDaMuon.setText("Số Sách Đã Mượn:");

        lblSoLanMuon.setText("Số lần mượn:");

        splitMain.setDividerSize(6);
        splitMain.setResizeWeight(1.0);
        splitMain.setToolTipText("");
        splitMain.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        tblPhieuMuon.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID Phiếu Mượn", "Email Thủ Thư Lập", "Ngày Mượn", "Hạn Trả", "Mã Bản Sao", "Ngày Trả", "Tình Trạng Khi Trả", "Email Thủ Thư Nhận"
            }
        ));
        jScrollPane1.setViewportView(tblPhieuMuon);

        splitMain.setLeftComponent(jScrollPane1);

        lblDetailISBN.setText("jLabel1");

        lblDetailTenSach.setText("jLabel1");

        lblDetailTacGia.setText("jLabel1");

        lblDetailTheLoai.setText("jLabel1");

        lblDetailNXB.setText("jLabel1");

        lblDetailNamXB.setText("jLabel1");

        jLabel1.setText("Chi Tiết Sách Bạn Đọc Mượn");

        jLabel2.setText("Mã số sách:");

        jLabel3.setText("Tên sách:");

        jLabel4.setText("Tác giả:");

        jLabel5.setText("Thể loại:");

        jLabel6.setText("Nhà xuất bản:");

        jLabel7.setText("Năm xuất bản:");

        javax.swing.GroupLayout pnlDetailBookLayout = new javax.swing.GroupLayout(pnlDetailBook);
        pnlDetailBook.setLayout(pnlDetailBookLayout);
        pnlDetailBookLayout.setHorizontalGroup(
            pnlDetailBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetailBookLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDetailBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(pnlDetailBookLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDetailISBN))
                    .addGroup(pnlDetailBookLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDetailTenSach))
                    .addGroup(pnlDetailBookLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDetailTacGia))
                    .addGroup(pnlDetailBookLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDetailTheLoai))
                    .addGroup(pnlDetailBookLayout.createSequentialGroup()
                        .addGroup(pnlDetailBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlDetailBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDetailNamXB)
                            .addComponent(lblDetailNXB))))
                .addContainerGap(251, Short.MAX_VALUE))
        );
        pnlDetailBookLayout.setVerticalGroup(
            pnlDetailBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDetailBookLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(pnlDetailBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lblDetailISBN))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetailBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblDetailTenSach))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetailBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(lblDetailTacGia))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetailBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(lblDetailTheLoai))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetailBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(lblDetailNXB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlDetailBookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(lblDetailNamXB))
                .addContainerGap(261, Short.MAX_VALUE))
        );

        splitMain.setRightComponent(pnlDetailBook);

        btnPrv.setText("Trang trước");
        btnPrv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrvActionPerformed(evt);
            }
        });
        jPanel2.add(btnPrv);

        lblPageInfo.setText("Trang 1/1");
        jPanel2.add(lblPageInfo);

        btnNxt.setText("Trang sau");
        btnNxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNxtActionPerformed(evt);
            }
        });
        jPanel2.add(btnNxt);

        panelSearch1.setToolTipText("");
        panelSearch1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        panelSearch1.setOpaque(false);
        panelSearch1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 10));

        Search1.setText("Tìm kiếm:");
        panelSearch1.add(Search1);

        txtSearch1.setToolTipText("");
        txtSearch1.setActionCommand("<Not Set>");
        txtSearch1.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtSearch1.setPreferredSize(new java.awt.Dimension(200, 26));
        panelSearch1.add(txtSearch1);

        btnClearSearch1.setText("X");
        btnClearSearch1.setBorderPainted(false);
        btnClearSearch1.setContentAreaFilled(false);
        btnClearSearch1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnClearSearch1.setPreferredSize(new java.awt.Dimension(26, 26));
        btnClearSearch1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        panelSearch1.add(btnClearSearch1);

        searchByCombo1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ID Phiếu Mượn", "Email Thủ Thư Lập", "Ngày Mượn", "Hạn Trả", "Mã Bản Sao", "Ngày Trả", "Tình Trạng Khi Trả", "Email Thủ Thư Nhận" }));
        searchByCombo1.setMinimumSize(new java.awt.Dimension(120, 26));
        panelSearch1.add(searchByCombo1);

        javax.swing.GroupLayout cardPhieuMuonLayout = new javax.swing.GroupLayout(cardPhieuMuon);
        cardPhieuMuon.setLayout(cardPhieuMuonLayout);
        cardPhieuMuonLayout.setHorizontalGroup(
            cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPhieuMuonLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cardPhieuMuonLayout.createSequentialGroup()
                        .addComponent(lblSoSachDangMuon)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblShowSoSachDangMuon, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(cardPhieuMuonLayout.createSequentialGroup()
                        .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(cardPhieuMuonLayout.createSequentialGroup()
                                .addComponent(lblSoSachDaMuon)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblShowSoSachDaMuon, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(cardPhieuMuonLayout.createSequentialGroup()
                                .addComponent(lblSoLanMuon)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblShowSoLanMuon, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 1125, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(cardPhieuMuonLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(splitMain, javax.swing.GroupLayout.DEFAULT_SIZE, 1322, Short.MAX_VALUE)
                    .addContainerGap()))
            .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardPhieuMuonLayout.createSequentialGroup()
                    .addContainerGap(421, Short.MAX_VALUE)
                    .addComponent(panelSearch1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(421, Short.MAX_VALUE)))
        );
        cardPhieuMuonLayout.setVerticalGroup(
            cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardPhieuMuonLayout.createSequentialGroup()
                .addContainerGap(559, Short.MAX_VALUE)
                .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cardPhieuMuonLayout.createSequentialGroup()
                        .addComponent(lblSoLanMuon)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSoSachDaMuon)
                            .addComponent(lblShowSoSachDaMuon))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblSoSachDangMuon)
                            .addComponent(lblShowSoSachDangMuon)))
                    .addComponent(lblShowSoLanMuon)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(71, 71, 71))
            .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardPhieuMuonLayout.createSequentialGroup()
                    .addContainerGap(119, Short.MAX_VALUE)
                    .addComponent(splitMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(119, Short.MAX_VALUE)))
            .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardPhieuMuonLayout.createSequentialGroup()
                    .addContainerGap(84, Short.MAX_VALUE)
                    .addComponent(panelSearch1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(560, Short.MAX_VALUE)))
        );

        pnlCards.add(cardPhieuMuon, "PHIEU_MUON");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(528, 528, 528)
                .addComponent(btnPhieuMuon)
                .addGap(5, 5, 5)
                .addComponent(btnPhieuPhat))
            .addComponent(pnlCards, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnPhieuMuon)
                    .addComponent(btnPhieuPhat))
                .addGap(5, 5, 5)
                .addComponent(pnlCards, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnPhieuMuonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPhieuMuonActionPerformed
        hideDetail1();
        clearBookDetail1();
        btnPhieuMuon.setSelected(true);
        btnPhieuPhat.setSelected(false);
        cardLayout.show(pnlCards, "PHIEU_MUON");
        try {
            loadPhieuMuon();
        } catch (Exception ex) {
            System.getLogger(ChiTietPhieuBanDocPanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }//GEN-LAST:event_btnPhieuMuonActionPerformed

    private void btnPhieuPhatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPhieuPhatActionPerformed
        hideDetail();
        clearBookDetail();
        btnPhieuMuon.setSelected(false);
        btnPhieuPhat.setSelected(true);
        cardLayout.show(pnlCards, "PHIEU_PHAT");
        try {
            loadPhieuPhat();
        } catch (Exception ex) {
            System.getLogger(ChiTietPhieuBanDocPanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }//GEN-LAST:event_btnPhieuPhatActionPerformed

    private void btnPrv1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrv1ActionPerformed
        if (pageIndexPhat == 0) return;
        pageIndexPhat--;
        try {
            loadPhieuPhat();
        } catch (Exception ex) {
            System.getLogger(ChiTietPhieuBanDocPanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }//GEN-LAST:event_btnPrv1ActionPerformed

    private void btnPrvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrvActionPerformed
        if (pageIndexMuon == 0) return;
        pageIndexMuon--;
        loadPhieuMuon();
    }//GEN-LAST:event_btnPrvActionPerformed

    private void btnClearSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnClearSearchActionPerformed

    private void btnNxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNxtActionPerformed
        if (!hasMoreAfterMuon) return;
        pageIndexMuon++;
        loadPhieuMuon();
    }//GEN-LAST:event_btnNxtActionPerformed

    private void btnNxt1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNxt1ActionPerformed
        if (!hasMoreAfterPhat) return;
        pageIndexPhat++;
        try {
            loadPhieuPhat();
        } catch (Exception ex) {
            System.getLogger(ChiTietPhieuBanDocPanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }//GEN-LAST:event_btnNxt1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Search;
    private javax.swing.JLabel Search1;
    private javax.swing.JButton btnClearSearch;
    private javax.swing.JButton btnClearSearch1;
    private javax.swing.JButton btnNxt;
    private javax.swing.JButton btnNxt1;
    private javax.swing.JToggleButton btnPhieuMuon;
    private javax.swing.JToggleButton btnPhieuPhat;
    private javax.swing.JButton btnPrv;
    private javax.swing.JButton btnPrv1;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel cardPhieuMuon;
    private javax.swing.JPanel cardPhieuPhat;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblDetailISBN;
    private javax.swing.JLabel lblDetailISBN1;
    private javax.swing.JLabel lblDetailNXB;
    private javax.swing.JLabel lblDetailNXB1;
    private javax.swing.JLabel lblDetailNamXB;
    private javax.swing.JLabel lblDetailNamXB1;
    private javax.swing.JLabel lblDetailTacGia;
    private javax.swing.JLabel lblDetailTacGia1;
    private javax.swing.JLabel lblDetailTenSach;
    private javax.swing.JLabel lblDetailTenSach1;
    private javax.swing.JLabel lblDetailTheLoai;
    private javax.swing.JLabel lblDetailTheLoai1;
    private javax.swing.JLabel lblPageInfo;
    private javax.swing.JLabel lblPageInfo1;
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
    private javax.swing.JPanel panelSearch;
    private javax.swing.JPanel panelSearch1;
    private javax.swing.JPanel pnlCards;
    private javax.swing.JPanel pnlDetailBook;
    private javax.swing.JPanel pnlDetailBook1;
    private javax.swing.JComboBox<String> searchByCombo;
    private javax.swing.JComboBox<String> searchByCombo1;
    private javax.swing.JSplitPane splitMain;
    private javax.swing.JSplitPane splitMain1;
    private javax.swing.JTable tblPhieuMuon;
    private javax.swing.JTable tblPhieuPhat;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtSearch1;
    // End of variables declaration//GEN-END:variables
}
