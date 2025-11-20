/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.quanlythuvien.view.panel;

import com.mycompany.quanlythuvien.model.BanDoc;
import com.mycompany.quanlythuvien.dao.ChiTietPhieuMuonDAO;
import com.mycompany.quanlythuvien.dao.SachDAO;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author DMX MSI
 */
public class ChiTietPhieuBanDocPanel extends javax.swing.JPanel {
    
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


    
    void setThongKePhieuMuonBanDoc(ArrayList<Object> arr) {
        if (arr == null || arr.isEmpty()) {
            lblShowSoLanMuon.setText("0");
            lblShowSoSachDaMuon.setText("0");
            lblShowSoSachDangMuon.setText("0");
            return;
        }
        int fieldsPerRecord = FIELDS_PER_RECORD;
        int cntSoSachDangMuon = 0;
        int cntSoSachDaMuon = arr.size() / fieldsPerRecord;
        ArrayList<Integer> cntSoLanMuon = new ArrayList<>();
        for (int i = 0; i + fieldsPerRecord - 1 < arr.size(); i += fieldsPerRecord) {
            Object idPM = arr.get(i);
            try {
                if (idPM != null) cntSoLanMuon.add(Integer.parseInt(idPM.toString()));
            } catch (NumberFormatException ex) {
                // bỏ qua nếu không parse được
            }
            Object ngayTraThucTe = arr.get(i + 5);
            if (ngayTraThucTe == null) {
                ++cntSoSachDangMuon;
            }
        }
        HashSet<Integer> set = new HashSet<>(cntSoLanMuon);

        lblShowSoLanMuon.setText(Integer.toString(set.size()));
        lblShowSoSachDaMuon.setText(Integer.toString(cntSoSachDaMuon));
        lblShowSoSachDangMuon.setText(Integer.toString(cntSoSachDangMuon));
    }

    void setThongKePhieuPhatBanDoc(ArrayList<Object> arr) {
        if (arr == null || arr.isEmpty()) {
            lblShowSoPhieuPhat.setText("0");
            lblShowTongSoTienPhatChuaDong.setText("0.0");
            lblShowTongSoTienPhatDaDong.setText("0.0");
            return;
        }
        int fieldsPerRecord = FIELDS_PER_RECORD;
        int cntSoPhieuPhat = arr.size() / fieldsPerRecord;
        double totTongTienPhatChuaDong = 0.0;
        double totTongTienPhatDaDong = 0.0;
        for (int i = 0; i + fieldsPerRecord - 1 < arr.size(); i += fieldsPerRecord) {
            Object soTien = arr.get(i + 5);
            Object trangThai = arr.get(i + 7);

            String sTien = safeToStr(soTien);
            String sTrangThai = safeToStr(trangThai);

            double val = 0.0;
            try {
                if (!sTien.isEmpty()) val = Double.parseDouble(sTien);
            } catch (NumberFormatException ex) {
                // bỏ qua / hoặc log
            }

            if ("Da dong".equalsIgnoreCase(sTrangThai.trim())) {
                totTongTienPhatDaDong += val;
            } else {
                totTongTienPhatChuaDong += val;
            }
        }
        lblShowSoPhieuPhat.setText(Integer.toString(cntSoPhieuPhat));
        lblShowTongSoTienPhatChuaDong.setText(String.format("%.2f", totTongTienPhatChuaDong));
        lblShowTongSoTienPhatDaDong.setText(String.format("%.2f", totTongTienPhatDaDong));
    }

    private String safeToStr(Object o) {
        return (o == null) ? "" : o.toString();
    }

    void loadPhieuMuon() {
        lastSelectedModelRow = -1;
        hideDetail();

        new javax.swing.SwingWorker<ArrayList<Object>, Void>() {
            @Override
            protected ArrayList<Object> doInBackground() throws Exception {
                ChiTietPhieuMuonDAO qr = new ChiTietPhieuMuonDAO();
                return qr.getAllPhieuMuonBanDoc(cur);
            }

            @Override
            protected void done() {
                try {
                    ArrayList<Object> arr = get(); // từ background
                    cachePhieuMuon = (arr == null) ? new ArrayList<>() : arr;
                    if (cachePhieuMuon.size() % FIELDS_PER_RECORD != 0) {
                        System.err.println("Warning: cachePhieuMuon size not multiple of FIELDS_PER_RECORD");
                    }
                    totalRowsMuon = cachePhieuMuon.size() / FIELDS_PER_RECORD;
                    totalPagesMuon = Math.max(1, (int) Math.ceil((double) totalRowsMuon / pageSizeMuon));
                    currentPageMuon = Math.min(Math.max(1, currentPageMuon), totalPagesMuon);
                    setThongKePhieuMuonBanDoc(cachePhieuMuon);
                    renderPagePhieuMuon();
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

        // nếu không có dữ liệu
        if (cur == null || cachePhieuMuon == null || cachePhieuMuon.isEmpty()) {
            lblPageInfo.setText("Trang 0/0");
            btnPrv.setEnabled(false);
            btnNxt.setEnabled(false);
            // reset selection/detail
            lastSelectedModelRow = -1;
            hideDetail();
            return;
        }

        int startRecord = (currentPageMuon - 1) * pageSizeMuon; // record index (0-based)
        int fields = FIELDS_PER_RECORD;
        int endRecordExclusive = Math.min(totalRowsMuon, startRecord + pageSizeMuon);

        for (int rec = startRecord; rec < endRecordExclusive; ++rec) {
            int base = rec * fields;
            if (base + fields - 1 >= cachePhieuMuon.size()) break;
            Object idPM = safeGetFromCache(cachePhieuMuon, base);
            Object emailNguoiLap = safeGetFromCache(cachePhieuMuon, base + 1);
            Object ngayMuon = safeGetFromCache(cachePhieuMuon, base + 2);
            Object hanTra = safeGetFromCache(cachePhieuMuon, base + 3);
            Object maBanSao = safeGetFromCache(cachePhieuMuon, base + 4);
            Object ngayTraThucTe = safeGetFromCache(cachePhieuMuon, base + 5);
            Object tinhTrang = safeGetFromCache(cachePhieuMuon, base + 6);
            Object emailNguoiNhan = safeGetFromCache(cachePhieuMuon, base + 7);

            model.addRow(new Object[] {
                safeToStr(idPM),
                safeToStr(emailNguoiLap),
                safeToStr(ngayMuon),
                safeToStr(hanTra),
                safeToStr(maBanSao),
                safeToStr(ngayTraThucTe),
                safeToStr(tinhTrang),
                safeToStr(emailNguoiNhan)
            });
        }

        // reset selection + hide detail when switching page
        lastSelectedModelRow = -1;
        hideDetail();

        lblPageInfo.setText(String.format("Trang %d/%d (Hiển thị %d-%d / %d)",
                currentPageMuon, totalPagesMuon,
                startRecord + 1, Math.min(totalRowsMuon, startRecord + pageSizeMuon),
                totalRowsMuon));

        btnPrv.setEnabled(currentPageMuon > 1);
        btnNxt.setEnabled(currentPageMuon < totalPagesMuon);
    }


    void loadPhieuPhat() throws Exception {
        lastSelectedModelRow1 = -1;
        hideDetail1();

        new javax.swing.SwingWorker<ArrayList<Object>, Void>() {
            @Override
            protected ArrayList<Object> doInBackground() throws Exception {
                ChiTietPhieuMuonDAO qr = new ChiTietPhieuMuonDAO();
                return qr.getAllPhieuPhatBanDoc(cur);
            }

            @Override
            protected void done() {
                try {
                    ArrayList<Object> arr = get(); // từ background
                    cachePhieuPhat = (arr == null) ? new ArrayList<>() : arr;
                    if (cachePhieuPhat.size() % FIELDS_PER_RECORD != 0) {
                        // *** SỬA: in đúng biến cachePhieuPhat thay vì cachePhieuMuon
                        System.err.println("Warning: cachePhieuPhat size not multiple of FIELDS_PER_RECORD");
                    }
                    totalRowsPhat = cachePhieuPhat.size() / FIELDS_PER_RECORD;
                    totalPagesPhat = Math.max(1, (int) Math.ceil((double) totalRowsPhat / pageSizePhat));
                    currentPagePhat = Math.min(Math.max(1, currentPagePhat), totalPagesPhat);
                    setThongKePhieuPhatBanDoc(cachePhieuPhat);
                    renderPagePhieuPhat();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    private void renderPagePhieuPhat() {
        DefaultTableModel model = (DefaultTableModel) tblPhieuPhat.getModel();
        model.setRowCount(0);
        if (cur == null || cachePhieuPhat == null || cachePhieuPhat.isEmpty()) {
            lblPageInfo1.setText("Trang 0/0");
            btnPrv1.setEnabled(false);
            btnNxt1.setEnabled(false);
            // reset selection/detail
            lastSelectedModelRow1 = -1;
            hideDetail1();
            return;
        }

        int startRecord = (currentPagePhat - 1) * pageSizePhat;
        int fields = FIELDS_PER_RECORD;
        int endRecordExclusive = Math.min(totalRowsPhat, startRecord + pageSizePhat);
        for (int rec = startRecord; rec < endRecordExclusive; ++rec) {
            int base = rec * fields;
            if (base + fields - 1 >= cachePhieuPhat.size()) break;
            Object idPhat = safeGetFromCache(cachePhieuPhat, base);
            Object idPM = safeGetFromCache(cachePhieuPhat, base + 1);
            Object emailNguoiLap = safeGetFromCache(cachePhieuPhat, base + 2);
            Object ngayMuon = safeGetFromCache(cachePhieuPhat, base + 3);
            Object loaiPhat = safeGetFromCache(cachePhieuPhat, base + 4);
            Object soTien = safeGetFromCache(cachePhieuPhat, base + 5);
            Object ngayGhiNhan = safeGetFromCache(cachePhieuPhat, base + 6);
            Object trangThai = safeGetFromCache(cachePhieuPhat, base + 7);

            model.addRow(new Object[] {
                safeToStr(idPhat),
                safeToStr(idPM),
                safeToStr(emailNguoiLap),
                safeToStr(ngayMuon),
                safeToStr(loaiPhat),
                safeToStr(soTien),
                safeToStr(ngayGhiNhan),
                safeToStr(trangThai)
            });

        }
        lastSelectedModelRow1 = -1;
        hideDetail1();
        lblPageInfo1.setText(String.format("Trang %d/%d (Hiển thị %d-%d / %d)",
                currentPagePhat, totalPagesPhat,
                startRecord + 1, Math.min(totalRowsPhat, startRecord + pageSizePhat),
                totalRowsPhat));

        btnPrv1.setEnabled(currentPagePhat > 1);
        btnNxt1.setEnabled(currentPagePhat < totalPagesPhat);
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
            SachDAO tmp = new SachDAO();
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
                        SachDAO tmp = new SachDAO();
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
                            SachDAO tmp = new SachDAO();
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
        // --- đăng ký phân trang cho PHIEU_MUON
        btnPrv.addActionListener(evt -> {
            if (currentPageMuon > 1) {
                currentPageMuon--;
                try {
                    // render từ cache (không gọi DB)
                    renderPagePhieuMuon();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        btnNxt.addActionListener(evt -> {
            if (currentPageMuon < totalPagesMuon) {
                currentPageMuon++;
                try {
                    renderPagePhieuMuon();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // --- đăng ký phân trang cho PHIEU_PHAT
        btnPrv1.addActionListener(evt -> {
            if (currentPagePhat > 1) {
                currentPagePhat--;
                try {
                    renderPagePhieuPhat();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        btnNxt1.addActionListener(evt -> {
            if (currentPagePhat < totalPagesPhat) {
                currentPagePhat++;
                try {
                    renderPagePhieuPhat();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // alternating rows + selection color
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
        cardPhieuMuon = new javax.swing.JPanel();
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
        lblSoSachDangMuon = new javax.swing.JLabel();
        lblSoSachDaMuon = new javax.swing.JLabel();
        lblSoLanMuon = new javax.swing.JLabel();
        lblShowSoLanMuon = new javax.swing.JLabel();
        lblShowSoSachDaMuon = new javax.swing.JLabel();
        lblShowSoSachDangMuon = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnPrv = new javax.swing.JButton();
        lblPageInfo = new javax.swing.JLabel();
        btnNxt = new javax.swing.JButton();

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
                .addContainerGap(609, Short.MAX_VALUE))
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
        jPanel1.add(btnNxt1);

        javax.swing.GroupLayout cardPhieuPhatLayout = new javax.swing.GroupLayout(cardPhieuPhat);
        cardPhieuPhat.setLayout(cardPhieuPhatLayout);
        cardPhieuPhatLayout.setHorizontalGroup(
            cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPhieuPhatLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1222, Short.MAX_VALUE)
                    .addGroup(cardPhieuPhatLayout.createSequentialGroup()
                        .addGroup(cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(cardPhieuPhatLayout.createSequentialGroup()
                                .addComponent(lblTongSoTienPhatDaDong)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblShowTongSoTienPhatDaDong))
                            .addGroup(cardPhieuPhatLayout.createSequentialGroup()
                                .addComponent(lblTongSoTienPhatChuaDong)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblShowTongSoTienPhatChuaDong))
                            .addGroup(cardPhieuPhatLayout.createSequentialGroup()
                                .addComponent(lblSoPhieuPhat)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblShowSoPhieuPhat, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(cardPhieuPhatLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(splitMain1)
                    .addContainerGap()))
        );
        cardPhieuPhatLayout.setVerticalGroup(
            cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPhieuPhatLayout.createSequentialGroup()
                .addGap(40, 40, 40)
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
                    .addComponent(lblShowTongSoTienPhatDaDong))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 459, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardPhieuPhatLayout.createSequentialGroup()
                    .addContainerGap(131, Short.MAX_VALUE)
                    .addComponent(splitMain1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(132, Short.MAX_VALUE)))
        );

        pnlCards.add(cardPhieuPhat, "PHIEU_PHAT");

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

        lblSoSachDangMuon.setText("Số Sách Đang Mượn:");

        lblSoSachDaMuon.setText("Số Sách Đã Mượn:");

        lblSoLanMuon.setText("Số lần mượn:");

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
        jPanel2.add(btnNxt);

        javax.swing.GroupLayout cardPhieuMuonLayout = new javax.swing.GroupLayout(cardPhieuMuon);
        cardPhieuMuon.setLayout(cardPhieuMuonLayout);
        cardPhieuMuonLayout.setHorizontalGroup(
            cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPhieuMuonLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(cardPhieuMuonLayout.createSequentialGroup()
                        .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(cardPhieuMuonLayout.createSequentialGroup()
                                .addComponent(lblSoLanMuon)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblShowSoLanMuon, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(cardPhieuMuonLayout.createSequentialGroup()
                                .addComponent(lblSoSachDaMuon)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblShowSoSachDaMuon, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(cardPhieuMuonLayout.createSequentialGroup()
                                .addComponent(lblSoSachDangMuon)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblShowSoSachDangMuon, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 1018, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 1222, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(cardPhieuMuonLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(splitMain, javax.swing.GroupLayout.DEFAULT_SIZE, 1222, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        cardPhieuMuonLayout.setVerticalGroup(
            cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPhieuMuonLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSoLanMuon)
                    .addComponent(lblShowSoLanMuon))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSoSachDaMuon)
                    .addComponent(lblShowSoSachDaMuon))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSoSachDangMuon)
                    .addComponent(lblShowSoSachDangMuon))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 446, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cardPhieuMuonLayout.createSequentialGroup()
                    .addContainerGap(119, Short.MAX_VALUE)
                    .addComponent(splitMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(119, Short.MAX_VALUE)))
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
        // TODO add your handling code here:
    }//GEN-LAST:event_btnPrv1ActionPerformed

    private void btnPrvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrvActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnPrvActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
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
    private javax.swing.JPanel pnlCards;
    private javax.swing.JPanel pnlDetailBook;
    private javax.swing.JPanel pnlDetailBook1;
    private javax.swing.JSplitPane splitMain;
    private javax.swing.JSplitPane splitMain1;
    private javax.swing.JTable tblPhieuMuon;
    private javax.swing.JTable tblPhieuPhat;
    // End of variables declaration//GEN-END:variables
}
