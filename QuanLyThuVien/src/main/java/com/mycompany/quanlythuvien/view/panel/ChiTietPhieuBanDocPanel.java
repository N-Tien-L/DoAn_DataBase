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
import java.util.HashSet;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author DMX MSI
 */
public class ChiTietPhieuBanDocPanel extends javax.swing.JPanel {
    private CardLayout cardLayout;
    private BanDoc cur;
    
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
    
    
    void setThongKePhieuMuonBanDoc(ArrayList<Object> arr) {
        int fieldsPerRecord = 8;
        int cntSoSachDangMuon = 0, cntSoSachDaMuon = arr.size()/fieldsPerRecord; 
        ArrayList<Integer> cntSoLanMuon = new ArrayList<Integer>();
        for (int i = 0; i + fieldsPerRecord - 1 < arr.size(); i += fieldsPerRecord) {
            Object idPM = arr.get(i);
            cntSoLanMuon.add(Integer.parseInt(idPM.toString()));
            Object emailNguoiLap = arr.get(i + 1);
            Object ngayMuon = arr.get(i + 2);
            Object hanTra = arr.get(i + 3);
            Object maBanSao = arr.get(i + 4);
            Object ngayTraThucTe = arr.get(i + 5);
            Object tinhTrang = arr.get(i + 6);
            Object emailNguoiNhan = arr.get(i + 7);
            if(ngayTraThucTe.equals(null)) {
                ++cntSoSachDangMuon;
            }
        }
        HashSet<Integer> set = new HashSet<>(cntSoLanMuon);
        
        lblShowSoLanMuon.setText(Integer.toString(set.size()));
        lblShowSoSachDaMuon.setText(Integer.toString(cntSoSachDaMuon));
        lblShowSoSachDangMuon.setText(Integer.toString(cntSoSachDangMuon));
    }
    void setThongKePhieuPhatBanDoc(ArrayList<Object> arr) {
        int fieldsPerRecord = 8;
        int cntSoPhieuPhat = arr.size(); 
        double totTongTienPhatChuaDong = 0.0;
        double totTongTienPhatDaDong = 0.0;
        for (int i = 0; i + fieldsPerRecord - 1 < arr.size(); i += fieldsPerRecord) {
            Object idPhat = arr.get(i);
            Object idPM = arr.get(i + 1);
            Object emailNguoiLap = arr.get(i + 2);
            Object ngayMuon = arr.get(i + 3);
            Object loaiPhat = arr.get(i + 4);
            Object soTien = arr.get(i + 5);
            Object ngayGhiNhan = arr.get(i + 6);
            Object trangThai = arr.get(i + 7);
            if(trangThai.toString().equals("Da dong")) {
                totTongTienPhatDaDong += Double.parseDouble(soTien.toString());
            } else {
                totTongTienPhatChuaDong += Double.parseDouble(soTien.toString());
            }
        }
        lblShowSoPhieuPhat.setText(Integer.toString(cntSoPhieuPhat));
        lblShowTongSoTienPhatChuaDong.setText(Double.toString(totTongTienPhatChuaDong));
        lblShowTongSoTienPhatDaDong.setText(Double.toString(totTongTienPhatDaDong));
    }
    
    void loadPhieuMuon() throws Exception {
        DefaultTableModel model = (DefaultTableModel) tblPhieuMuon.getModel();
        model.setRowCount(0); // xóa dữ liệu cũ

        if (cur == null) {
            System.out.println("BanDoc null -> không load phiếu mượn");
            return;
        }

        ChiTietPhieuMuonDAO qr = new ChiTietPhieuMuonDAO();
        ArrayList<Object> arr = qr.getAllPhieuMuonBanDoc(cur);
        setThongKePhieuMuonBanDoc(arr);

        if (arr == null || arr.isEmpty()) {
            // không có dữ liệu
            return;
        }

        int fieldsPerRecord = 8;
        if (arr.size() % fieldsPerRecord != 0) {
            System.out.println("Warning: arr.size() không chia hết cho 8: " + arr.size());
        }
        for (int i = 0; i + fieldsPerRecord - 1 < arr.size(); i += fieldsPerRecord) {
            Object idPM = arr.get(i);
            Object emailNguoiLap = arr.get(i + 1);
            Object ngayMuon = arr.get(i + 2);
            Object hanTra = arr.get(i + 3);
            Object maBanSao = arr.get(i + 4);
            Object ngayTraThucTe = arr.get(i + 5);
            Object tinhTrang = arr.get(i + 6);
            Object emailNguoiNhan = arr.get(i + 7);

            // Nếu muốn format riêng các LocalDate -> String, có thể làm ở đây:
            // (nhưng DefaultTableModel hiển thị LocalDate.toString() cũng ok)
            model.addRow(new Object[] {
                idPM,
                emailNguoiLap,
                ngayMuon,
                hanTra,
                maBanSao,
                ngayTraThucTe,
                tinhTrang,
                emailNguoiNhan
            });
        }
    }
    
    void loadPhieuPhat() throws Exception {
        DefaultTableModel model = (DefaultTableModel) tblPhieuPhat.getModel();
        model.setRowCount(0); // xóa dữ liệu cũ

        if (cur == null) {
            System.out.println("BanDoc null -> không load phiếu phạt");
            return;
        }

        ChiTietPhieuMuonDAO qr = new ChiTietPhieuMuonDAO();
        ArrayList<Object> arr = qr.getAllPhieuPhatBanDoc(cur);
        setThongKePhieuPhatBanDoc(arr);
        if (arr == null || arr.isEmpty()) {
            // không có dữ liệu
            return;
        }

        int fieldsPerRecord = 8; // tương ứng với DAO trả về 8 giá trị/record
        if (arr.size() % fieldsPerRecord != 0) {
            System.out.println("Warning: arr.size() không chia hết cho 8: " + arr.size());
        }
        for (int i = 0; i + fieldsPerRecord - 1 < arr.size(); i += fieldsPerRecord) {
            Object idPhat = arr.get(i);
            Object idPM = arr.get(i + 1);
            Object emailNguoiLap = arr.get(i + 2);
            Object ngayMuon = arr.get(i + 3);
            Object loaiPhat = arr.get(i + 4);
            Object soTien = arr.get(i + 5);
            Object ngayGhiNhan = arr.get(i + 6);
            Object trangThai = arr.get(i + 7);

            // Nếu muốn format LocalDate -> String (ví dụ dd/MM/yyyy) bạn có thể chuyển ở đây.
            model.addRow(new Object[] {
                idPhat,
                idPM,
                emailNguoiLap,
                ngayMuon,
                loaiPhat,
                soTien,
                ngayGhiNhan,
                trangThai
            });
        }
    }



    /**
     * Creates new form ChiTietPhieuBanDocPanel
     */
    public ChiTietPhieuBanDocPanel() {
        initComponents();
    }
    private Object safeGet(javax.swing.table.TableModel model, int row, int col) {
        if (model == null) return null;
        if (row < 0 || row >= model.getRowCount()) return null;
        if (col < 0 || col >= model.getColumnCount()) return null;
        return model.getValueAt(row, col);
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
        btnPhieuMuon.setSelected(false);
        loadPhieuMuon();
        pnlDetailBook.setVisible(false);
        pnlDetailBook1.setVisible(false);

        
        
        tblPhieuMuon.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;

            int viewRow = tblPhieuMuon.getSelectedRow();
            if (viewRow == -1) return;

            int modelRow = tblPhieuMuon.convertRowIndexToModel(viewRow);

            // toggle: click lại cùng row sẽ ẩn
            if (modelRow == lastSelectedModelRow && detailVisible) {
                hideDetail();
                lastSelectedModelRow = -1;
                tblPhieuMuon.clearSelection();
            } else {
                lastSelectedModelRow = modelRow;
                // lấy MaBanSao từ table model (đổi MA_BAN_SAO_COL nếu khác)
                Object maBanSaoObj = safeGet(tblPhieuMuon.getModel(), modelRow, MA_BAN_SAO_COL);
                if (maBanSaoObj != null) {
                    int maBanSao;
                    try {
                        maBanSao = Integer.parseInt(String.valueOf(maBanSaoObj));
                        SachDAO tmp = new SachDAO();
                        ArrayList<Object> info = tmp.getSomeInfoSachByMaBanSao(maBanSao);
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

                // khác hàng hiện tại -> chọn và show (mouse click thường cũng thay selection, nhưng đảm bảo xử lý)
                lastSelectedModelRow = modelRow;
                tblPhieuMuon.setRowSelectionInterval(viewRow, viewRow); // ensure selected
                Object maBanSaoObj = safeGet(tblPhieuMuon.getModel(), modelRow, MA_BAN_SAO_COL);
                if (maBanSaoObj != null) {
                    try {
                        int maBanSao = Integer.parseInt(String.valueOf(maBanSaoObj));
                        SachDAO tmp = new SachDAO();
                        ArrayList<Object> info = tmp.getSomeInfoSachByMaBanSao(maBanSao);
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
        tblPhieuPhat.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;

            int viewRow = tblPhieuPhat.getSelectedRow();
            if (viewRow == -1) return;

            int modelRow = tblPhieuPhat.convertRowIndexToModel(viewRow);

            if (modelRow == lastSelectedModelRow1 && detailVisible1) {
                // nếu selection không thay đổi và hiện rồi thì không tắt ở đây
                // (tắt sẽ do mouseClicked bắt)
                return;
            }

            // chọn row mới -> lấy IdPhat (col 0), từ IdPhat lấy MaBanSao, rồi lấy info sách
            lastSelectedModelRow1 = modelRow;
            Object idPhatObj = safeGet(tblPhieuPhat.getModel(), modelRow, ID_PHAT_COL);
            if (idPhatObj != null) {
                try {
                    int idPhat = Integer.parseInt(String.valueOf(idPhatObj));

                    SachDAO tmp = new SachDAO();
                    ArrayList<Object> info = tmp.getSomeInfoSachByIdPhat(idPhat);
                    populateDetailPanel1(info);
                } catch (NumberFormatException ex) {
                    clearBookDetail1();
                }
            } else {
                clearBookDetail1();
            }
            showDetail1();
        });

        // MouseListener: bắt click lại cùng hàng để toggle off
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
                        
                        SachDAO tmp = new SachDAO();
                        ArrayList<Object> info = tmp.getSomeInfoSachByIdPhat(idPhat);
                        populateDetailPanel1(info);
                    } catch (NumberFormatException ex) {
                        clearBookDetail1();
                    }
                } else {
                    clearBookDetail1();
                }
                showDetail1();
            }
        });
        
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
                .addContainerGap(251, Short.MAX_VALUE))
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

        javax.swing.GroupLayout cardPhieuPhatLayout = new javax.swing.GroupLayout(cardPhieuPhat);
        cardPhieuPhat.setLayout(cardPhieuPhatLayout);
        cardPhieuPhatLayout.setHorizontalGroup(
            cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPhieuPhatLayout.createSequentialGroup()
                .addContainerGap()
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
                .addContainerGap(993, Short.MAX_VALUE))
            .addGroup(cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(cardPhieuPhatLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(splitMain1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
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
                .addContainerGap(590, Short.MAX_VALUE))
            .addGroup(cardPhieuPhatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(cardPhieuPhatLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(splitMain1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
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

        javax.swing.GroupLayout cardPhieuMuonLayout = new javax.swing.GroupLayout(cardPhieuMuon);
        cardPhieuMuon.setLayout(cardPhieuMuonLayout);
        cardPhieuMuonLayout.setHorizontalGroup(
            cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPhieuMuonLayout.createSequentialGroup()
                .addContainerGap()
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
                .addContainerGap(1024, Short.MAX_VALUE))
            .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(cardPhieuMuonLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(splitMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
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
                .addContainerGap(590, Short.MAX_VALUE))
            .addGroup(cardPhieuMuonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(cardPhieuMuonLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(splitMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
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
            // nếu muốn load dữ liệu khi chuyển:
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
        // nếu muốn load dữ liệu khi chuyển:
        try {
            loadPhieuPhat();
        } catch (Exception ex) {
            System.getLogger(ChiTietPhieuBanDocPanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }//GEN-LAST:event_btnPhieuPhatActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnPhieuMuon;
    private javax.swing.JToggleButton btnPhieuPhat;
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
