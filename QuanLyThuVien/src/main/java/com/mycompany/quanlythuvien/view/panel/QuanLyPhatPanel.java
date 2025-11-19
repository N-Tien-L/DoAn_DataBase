package com.mycompany.quanlythuvien.view.panel;

import com.mycompany.quanlythuvien.controller.PhatController;
import com.mycompany.quanlythuvien.model.Phat;
import com.mycompany.quanlythuvien.model.ChiTietPhieuMuonInfo;
import com.mycompany.quanlythuvien.model.BanDocPhat;
import com.mycompany.quanlythuvien.model.PaginationResult;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Panel quản lý phạt với danh sách, tìm kiếm và chi tiết
 * 
 * @author Tien
 */
public class QuanLyPhatPanel extends JPanel {
    private final PhatController phatController = new PhatController();
    private JTable tablePhat;
    private DefaultTableModel tableModel;
    private JButton btnCreatePhat;
    private JTextField txtSearchIdPM;
    private JList<Phat> suggestList;
    private DefaultListModel<Phat> suggestModel;
    private JPanel detailContentPanel;
    private JPanel readerContentPanel;

    // Pagination fields
    private int currentCursor = 0;
    private int nextCursor = -1;
    private int pageSize = 10;
    private int totalCount = 0;
    private int currentRecordStart = 1; // Vị trí record đầu tiên của trang hiện tại (1-based)
    private String currentSearchText = "";
    private boolean isSearching = false;
    private JButton btnPrevious;
    private JButton btnNext;
    private JLabel lblPageInfo;
    private java.util.Stack<Integer> cursorStack = new java.util.Stack<>();
    private java.util.Stack<Integer> recordStartStack = new java.util.Stack<>(); // Lưu vị trí đầu của từng trang

    public QuanLyPhatPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(245, 245, 245));

        add(createToolbar(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);

        loadTableData();
    }

    /**
     * Helper: Load và scale icon với chất lượng cao
     */
    private ImageIcon loadScaledIcon(String path, int width, int height) {
        try {
            java.net.URL resource = getClass().getResource(path);
            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                Image img = icon.getImage();
                // Scale với Image.SCALE_SMOOTH để render mượt hơn
                Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }
        } catch (Exception e) {
            // Icon load failed
        }
        return null;
    }

    /**
     * Tạo toolbar với nút tạo phạt và tìm kiếm
     */
    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        // Nút tạo phạt
        btnCreatePhat = new JButton("+ Tạo Vé Phạt");
        btnCreatePhat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCreatePhat.setForeground(Color.WHITE);
        btnCreatePhat.setBackground(new Color(0, 102, 153));
        btnCreatePhat.setFocusPainted(false);
        btnCreatePhat.setBorderPainted(false);
        btnCreatePhat.setMargin(new Insets(8, 16, 8, 16));
        btnCreatePhat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCreatePhat.addActionListener(e -> openCreatePhatDialog());
        btnCreatePhat.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnCreatePhat.setBackground(new Color(0, 82, 123));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnCreatePhat.setBackground(new Color(0, 102, 153));
            }
        });

        // Panel tìm kiếm
        JPanel searchPanel = createSearchPanel();

        toolbar.add(btnCreatePhat, BorderLayout.WEST);
        toolbar.add(searchPanel, BorderLayout.EAST);

        return toolbar;
    }

    /**
     * Tạo panel tìm kiếm với gợi ý typing
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(500, 200));

        JLabel lblSearch = new JLabel("Tìm Vé Phạt:");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        txtSearchIdPM = new JTextField(20);
        txtSearchIdPM.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtSearchIdPM.setToolTipText("Tìm theo Tên, Email, SĐT hoặc IdPM");
        txtSearchIdPM.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Setup suggest list - hiển thị danh sách vé phạt
        suggestModel = new DefaultListModel<>();
        suggestList = new JList<>(suggestModel);
        suggestList.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        suggestList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                if (value instanceof Phat) {
                    Phat phat = (Phat) value;
                    label.setText(String.format("IdPhat: %d | IdPM: %d | Loại: %s | Tiền: %,d | Trạng Thái: %s",
                            phat.getIdPhat(), phat.getIdPM(), phat.getLoaiPhat(),
                            phat.getSoTien().longValue(), phat.getTrangThai()));
                }
                return label;
            }
        });

        JScrollPane scrollSuggest = new JScrollPane(suggestList);
        scrollSuggest.setPreferredSize(new Dimension(500, 150));
        scrollSuggest.setVisible(false);

        // Document listener cho search
        txtSearchIdPM.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchSuggestion();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchSuggestion();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchSuggestion();
            }

            private void searchSuggestion() {
                String text = txtSearchIdPM.getText().trim();
                suggestModel.clear();

                if (text.isEmpty()) {
                    // Nếu trống, reset phân trang và reload toàn bộ dữ liệu
                    isSearching = false;
                    currentSearchText = "";
                    currentCursor = 0;
                    currentRecordStart = 1;
                    cursorStack.clear();
                    recordStartStack.clear();
                    loadTableData();
                    scrollSuggest.setVisible(false);
                    return;
                }

                // Lưu trạng thái tìm kiếm
                isSearching = true;
                currentSearchText = text;
                currentCursor = 0;
                currentRecordStart = 1;
                cursorStack.clear();
                recordStartStack.clear();

                // Tải dữ liệu trang đầu tiên của tìm kiếm
                loadTableData();
                scrollSuggest.setVisible(false);
            }
        });

        // Khi nhấn Enter hoặc click vào suggest item, xóa search để reload toàn bộ dữ
        // liệu
        txtSearchIdPM.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    txtSearchIdPM.setText("");
                    loadTableData();
                }
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setOpaque(false);
        inputPanel.add(lblSearch, BorderLayout.WEST);
        inputPanel.add(txtSearchIdPM, BorderLayout.CENTER);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollSuggest, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Tạo main panel với bảng, chi tiết phiếu mượn, và info bạn đọc
     */
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);

        // Panel bảng phạt
        JPanel tablePanel = createTablePanel();
        tablePanel.setPreferredSize(new Dimension(800, 300)); // Đảm bảo table có kích thước tối thiểu

        // Panel phân trang
        JPanel paginationPanel = createPaginationPanel();

        // Split panel: chi tiết phiếu mươn (left) + info bạn đọc (right)
        Component detailComponent = createDetailPanel();
        Component readerComponent = createReaderPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, detailComponent, readerComponent);
        splitPane.setDividerLocation(0.65); // Chi tiết phiếu mượn chiều ngang rộng hơn (65%)
        splitPane.setDividerSize(5);
        splitPane.setOpaque(false);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(splitPane, BorderLayout.CENTER);
        bottomPanel.setPreferredSize(new Dimension(800, 200)); // Kích thước tối thiểu cho panel dưới

        // Panel giữa chứa bảng + phân trang
        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(tablePanel, BorderLayout.CENTER);
        centerPanel.add(paginationPanel, BorderLayout.SOUTH);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centerPanel, bottomPanel);
        mainSplit.setDividerLocation(0.65); // Tăng từ 0.6 lên 0.65 để table chiếm nhiều không gian hơn
        mainSplit.setDividerSize(5);
        mainSplit.setOpaque(false);

        panel.add(mainSplit, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Tạo panel điều khiển phân trang
     */
    private JPanel createPaginationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panel.setOpaque(false);

        // Nút Previous
        btnPrevious = new JButton("Trang Trước");
        try {
            ImageIcon leftIcon = loadScaledIcon("/icons/32x32/left.png", 20, 20);
            if (leftIcon != null) {
                btnPrevious.setIcon(leftIcon);
            }
        } catch (Exception e) {
            btnPrevious.setText("◀ Trang Trước");
        }
        btnPrevious.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnPrevious.setForeground(Color.WHITE);
        btnPrevious.setBackground(new Color(100, 100, 100));
        btnPrevious.setFocusPainted(false);
        btnPrevious.setBorderPainted(false);
        btnPrevious.setEnabled(false);
        btnPrevious.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPrevious.addActionListener(e -> previousPage());
        btnPrevious.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btnPrevious.isEnabled()) {
                    btnPrevious.setBackground(new Color(80, 80, 80));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnPrevious.setBackground(new Color(100, 100, 100));
            }
        });
        panel.add(btnPrevious);

        // Thông tin trang
        lblPageInfo = new JLabel("Hiển thị 1 - 10 / 0");
        lblPageInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPageInfo.setForeground(new Color(80, 80, 80));
        panel.add(lblPageInfo);

        // Nút Next
        btnNext = new JButton("Trang Sau");
        try {
            ImageIcon rightIcon = loadScaledIcon("/icons/32x32/right.png", 20, 20);
            if (rightIcon != null) {
                btnNext.setIcon(rightIcon);
            }
        } catch (Exception e) {
            btnNext.setText("Trang Sau ▶");
        }
        btnNext.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnNext.setForeground(Color.WHITE);
        btnNext.setBackground(new Color(100, 100, 100));
        btnNext.setFocusPainted(false);
        btnNext.setBorderPainted(false);
        btnNext.setEnabled(false);
        btnNext.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNext.addActionListener(e -> nextPage());
        btnNext.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btnNext.isEnabled()) {
                    btnNext.setBackground(new Color(80, 80, 80));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnNext.setBackground(new Color(100, 100, 100));
            }
        });
        panel.add(btnNext);

        return panel;
    }

    /**
     * Chuyển đến trang trước
     */
    private void previousPage() {
        if (!cursorStack.isEmpty()) {
            currentCursor = cursorStack.pop();
            currentRecordStart = recordStartStack.pop();
            loadTableData();
        }
    }

    /**
     * Chuyển đến trang tiếp theo
     */
    private void nextPage() {
        if (nextCursor >= 0) {
            cursorStack.push(currentCursor);
            recordStartStack.push(currentRecordStart);
            currentCursor = nextCursor;
            currentRecordStart += pageSize;
            loadTableData();
        }
    }

    /**
     * Tạo panel hiển thị bảng danh sách phạt
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        // Table model
        String[] columns = { "IdPhat", "IdPM", "MaBanSao", "Loại Phạt", "Số Tiền", "Ngày Ghi Nhận", "Trạng Thái" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablePhat = new JTable(tableModel);
        tablePhat.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tablePhat.setRowHeight(25);
        tablePhat.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tablePhat.getTableHeader().setBackground(new Color(0, 102, 153));
        tablePhat.getTableHeader().setForeground(Color.WHITE);
        tablePhat.setSelectionBackground(new Color(200, 220, 240));
        tablePhat.setGridColor(new Color(220, 220, 220));

        // Thêm 10 ô rỗng ban đầu
        for (int i = 0; i < 10; i++) {
            tableModel.addRow(new Object[] { "", "", "", "", "", "", "" });
        }

        // Thêm listener để cập nhật detail panels
        tablePhat.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablePhat.getSelectedRow() != -1) {
                int row = tablePhat.getSelectedRow();
                Object idPMObj = tableModel.getValueAt(row, 1);
                Object maBanSaoObj = tableModel.getValueAt(row, 2);

                if (idPMObj != null && !idPMObj.toString().isEmpty() &&
                        maBanSaoObj != null && !maBanSaoObj.toString().isEmpty()) {
                    try {
                        int idPM = Integer.parseInt(idPMObj.toString());
                        int maBanSao = Integer.parseInt(maBanSaoObj.toString());
                        displayDetailAndReader(idPM, maBanSao);
                    } catch (NumberFormatException ex) {
                        // Bỏ qua nếu không phải số
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablePhat);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // Panel nút chỉnh sửa, xóa, thanh toán
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        // Helper method để tạo nút đẹp
        java.util.function.BiConsumer<JButton, String> styleButton = (btn, colorType) -> {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setMargin(new Insets(10, 12, 10, 12));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setOpaque(true);
        };

        // Nút Edit - Màu cam
        JButton btnEdit = new JButton("✏ Chỉnh Sửa");
        try {
            ImageIcon editIcon = loadScaledIcon("/icons/32x32/edit.png", 20, 20);
            if (editIcon != null) {
                btnEdit.setIcon(editIcon);
                btnEdit.setText("  Chỉnh Sửa");
            }
        } catch (Exception e) {
            // Fallback: dùng text
        }
        styleButton.accept(btnEdit, "edit");
        btnEdit.setBackground(new Color(255, 152, 0));
        btnEdit.setToolTipText("Nhấp để chỉnh sửa vé phạt");
        btnEdit.addActionListener(e -> {
            if (tablePhat.getSelectedRow() != -1) {
                int row = tablePhat.getSelectedRow();
                Object idPhatObj = tableModel.getValueAt(row, 0);

                // Kiểm tra ô rỗng
                if (idPhatObj == null || idPhatObj.toString().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn một vé phạt để chỉnh sửa!", "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                try {
                    int idPhat = Integer.parseInt(idPhatObj.toString());
                    // Lấy tất cả dữ liệu từ hàng
                    Object loaiPhat = tableModel.getValueAt(row, 3);
                    Object soTien = tableModel.getValueAt(row, 4);
                    Object trangThai = tableModel.getValueAt(row, 6);

                    openEditPhatDialog(idPhat, loaiPhat.toString(), soTien.toString(), trangThai.toString());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn một vé phạt hợp lệ!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một vé phạt để chỉnh sửa!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        btnEdit.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnEdit.setBackground(new Color(255, 111, 0));
                btnEdit.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnEdit.setBackground(new Color(255, 152, 0));
                btnEdit.setForeground(Color.WHITE);
            }
        });

        // Nút Delete - Màu đỏ
        JButton btnDelete = new JButton("🗑 Xóa");
        try {
            ImageIcon deleteIcon = loadScaledIcon("/icons/32x32/delete.png", 20, 20);
            if (deleteIcon != null) {
                btnDelete.setIcon(deleteIcon);
                btnDelete.setText("  Xóa");
            }
        } catch (Exception e) {
            // Fallback: dùng text
        }
        styleButton.accept(btnDelete, "delete");
        btnDelete.setBackground(new Color(244, 67, 54));
        btnDelete.setToolTipText("Nhấp để xóa vé phạt");
        btnDelete.addActionListener(e -> {
            if (tablePhat.getSelectedRow() != -1) {
                int row = tablePhat.getSelectedRow();
                Object idPhatObj = tableModel.getValueAt(row, 0);

                // Kiểm tra ô rỗng
                if (idPhatObj == null || idPhatObj.toString().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn một vé phạt để xóa!", "Thông báo",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                try {
                    int idPhat = Integer.parseInt(idPhatObj.toString());

                    // Hỏi xác nhận trước khi xóa
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Bạn có chắc muốn xóa vé phạt này không?",
                            "Xác nhận xóa",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (confirm == JOptionPane.YES_OPTION) {
                        boolean success = phatController.deletePhat(idPhat);
                        if (success) {
                            JOptionPane.showMessageDialog(this, "Xóa vé phạt thành công!", "Thành công",
                                    JOptionPane.INFORMATION_MESSAGE);
                            // Reset table data
                            currentCursor = 0;
                            isSearching = false;
                            currentSearchText = "";
                            txtSearchIdPM.setText("");
                            loadTableData();
                        } else {
                            JOptionPane.showMessageDialog(this, "Xóa thất bại!", "Lỗi",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn một vé phạt hợp lệ!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một vé phạt để xóa!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        btnDelete.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnDelete.setBackground(new Color(211, 47, 47));
                btnDelete.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnDelete.setBackground(new Color(244, 67, 54));
                btnDelete.setForeground(Color.WHITE);
            }
        });

        // Nút Thanh toán - Màu xanh
        JButton btnPayment = new JButton("💰 Thanh Toán");
        try {
            ImageIcon paymentIcon = loadScaledIcon("/icons/32x32/money.png", 20, 20);
            if (paymentIcon != null) {
                btnPayment.setIcon(paymentIcon);
                btnPayment.setText("  Thanh Toán");
            }
        } catch (Exception e) {
            // Fallback: dùng text
        }
        styleButton.accept(btnPayment, "payment");
        btnPayment.setBackground(new Color(76, 175, 80));
        btnPayment.setToolTipText("Nhấp để xử lý thanh toán phạt");
        btnPayment.addActionListener(e -> openPaymentDialog());
        btnPayment.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnPayment.setBackground(new Color(56, 142, 60));
                btnPayment.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnPayment.setBackground(new Color(76, 175, 80));
                btnPayment.setForeground(Color.WHITE);
            }
        });

        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnPayment);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Tạo panel hiển thị chi tiết phiếu mượn
     */
    private JComponent createDetailPanel() {
        detailContentPanel = new JPanel();
        detailContentPanel.setLayout(new BoxLayout(detailContentPanel, BoxLayout.Y_AXIS));
        detailContentPanel.setBackground(new Color(250, 250, 250));
        detailContentPanel.setOpaque(true);
        detailContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Các trường thông tin
        detailContentPanel.add(createInfoField("IdPM:", "lblIdPM"));
        detailContentPanel.add(Box.createVerticalStrut(10));
        detailContentPanel.add(createInfoField("MaBanSao:", "lblMaBanSao"));
        detailContentPanel.add(Box.createVerticalStrut(10));
        detailContentPanel.add(createInfoField("IdBD:", "lblIdBD"));
        detailContentPanel.add(Box.createVerticalStrut(10));
        detailContentPanel.add(createDateField("Ngày Mượn:", "lblNgayMuon"));
        detailContentPanel.add(Box.createVerticalStrut(10));
        detailContentPanel.add(createDateField("Hạn Trả:", "lblHanTra"));
        detailContentPanel.add(Box.createVerticalGlue());

        // Wrap với scroll
        JScrollPane scrollPane = new JScrollPane(detailContentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(0, 102, 153), 2),
                        "Chi Tiết Phiếu Mượn",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 12),
                        new Color(0, 102, 153)),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        return scrollPane;
    }

    /**
     * Tạo panel hiển thị thông tin bạn đọc
     */
    private JComponent createReaderPanel() {
        readerContentPanel = new JPanel();
        readerContentPanel.setLayout(new BoxLayout(readerContentPanel, BoxLayout.Y_AXIS));
        readerContentPanel.setBackground(new Color(250, 250, 250));
        readerContentPanel.setOpaque(true);
        readerContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Các trường thông tin bạn đọc
        readerContentPanel.add(createInfoField("Họ Tên:", "lblHoTen"));
        readerContentPanel.add(Box.createVerticalStrut(10));
        readerContentPanel.add(createInfoField("Email:", "lblEmail"));
        readerContentPanel.add(Box.createVerticalStrut(10));
        readerContentPanel.add(createInfoField("Địa Chỉ:", "lblDiaChi"));
        readerContentPanel.add(Box.createVerticalStrut(10));
        readerContentPanel.add(createInfoField("SĐT:", "lblSDT"));
        readerContentPanel.add(Box.createVerticalStrut(12));

        // Separator
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        readerContentPanel.add(separator);
        readerContentPanel.add(Box.createVerticalStrut(12));

        // Tiêu đề thống kê
        JLabel lblThongKe = new JLabel("Thống Kê Phạt");
        lblThongKe.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblThongKe.setForeground(new Color(0, 102, 153));
        readerContentPanel.add(lblThongKe);
        readerContentPanel.add(Box.createVerticalStrut(8));

        // Thống kê phạt
        readerContentPanel.add(createStatField("Trễ hạn:", "lblSoLuongTreHan"));
        readerContentPanel.add(Box.createVerticalStrut(8));
        readerContentPanel.add(createStatField("Hỏng sách:", "lblSoLuongHongSach"));
        readerContentPanel.add(Box.createVerticalStrut(8));
        readerContentPanel.add(createStatField("Mất sách:", "lblSoLuongMatSach"));
        readerContentPanel.add(Box.createVerticalStrut(12));

        // Separator
        JSeparator separator2 = new JSeparator();
        separator2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        readerContentPanel.add(separator2);
        readerContentPanel.add(Box.createVerticalStrut(12));

        // Tiêu đề tài chính
        JLabel lblTaiChinh = new JLabel("Tài Chính");
        lblTaiChinh.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTaiChinh.setForeground(new Color(0, 102, 153));
        readerContentPanel.add(lblTaiChinh);
        readerContentPanel.add(Box.createVerticalStrut(8));

        readerContentPanel.add(createStatField("Tổng tiền phạt:", "lblTongTienPhat"));
        readerContentPanel.add(Box.createVerticalStrut(8));
        readerContentPanel.add(createStatField("Chưa đóng:", "lblTongTienChuaDong"));
        readerContentPanel.add(Box.createVerticalGlue());

        // Wrap với scroll
        JScrollPane scrollPane = new JScrollPane(readerContentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(0, 153, 76), 2),
                        "Thông Tin Bạn Đọc",
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 12),
                        new Color(0, 153, 76)),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        return scrollPane;
    }

    /**
     * Helper: Tạo field hiển thị thông tin
     */
    private JPanel createInfoField(String label, String key) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblLabel.setForeground(new Color(60, 60, 60));
        lblLabel.setPreferredSize(new Dimension(100, 30));

        JLabel lblValue = new JLabel("--");
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblValue.setForeground(new Color(80, 80, 80));
        lblValue.setName(key);

        row.add(lblLabel, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.CENTER);

        return row;
    }

    /**
     * Helper: Tạo field hiển thị ngày tháng (mặc định YYYY-MM-DD)
     */
    private JPanel createDateField(String label, String key) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblLabel.setForeground(new Color(60, 60, 60));
        lblLabel.setPreferredSize(new Dimension(100, 30));

        JLabel lblValue = new JLabel("YYYY-MM-DD");
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblValue.setForeground(new Color(80, 80, 80));
        lblValue.setName(key);

        row.add(lblLabel, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.CENTER);

        return row;
    }

    /**
     * Helper: Tạo field thống kê
     */
    private JPanel createStatField(String label, String key) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblLabel.setForeground(new Color(100, 100, 100));
        lblLabel.setPreferredSize(new Dimension(120, 25));

        JLabel lblValue = new JLabel("0");
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblValue.setForeground(new Color(0, 102, 153));
        lblValue.setName(key);

        row.add(lblLabel, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.CENTER);

        return row;
    }

    /**
     * Hiển thị chi tiết phiếu mượn và thông tin bạn đọc
     */
    private void displayDetailAndReader(int idPM, int maBanSao) {
        try {
            // Lấy chi tiết phiếu mượn
            ChiTietPhieuMuonInfo detail = phatController.getChiTietPhieuMuonByIdPMAndMaBanSao(idPM, maBanSao);
            if (detail != null && detailContentPanel != null) {
                setLabelValue(detailContentPanel, "lblIdPM", String.valueOf(detail.getIdPM()));
                setLabelValue(detailContentPanel, "lblMaBanSao", String.valueOf(detail.getMaBanSao()));
                setLabelValue(detailContentPanel, "lblIdBD", String.valueOf(detail.getIdBD()));
                setLabelValue(detailContentPanel, "lblNgayMuon", detail.getNgayMuon().toString());
                setLabelValue(detailContentPanel, "lblHanTra", detail.getNgayHenTra().toString());

                // Lấy thông tin bạn đọc
                BanDocPhat reader = phatController.getBanDocPhatByIdBD(detail.getIdBD());
                if (reader != null && readerContentPanel != null) {
                    setLabelValue(readerContentPanel, "lblHoTen", reader.getHoTen());
                    setLabelValue(readerContentPanel, "lblEmail", reader.getEmail());
                    setLabelValue(readerContentPanel, "lblDiaChi", reader.getDiaChi());
                    setLabelValue(readerContentPanel, "lblSDT", reader.getSdt());

                    setLabelValue(readerContentPanel, "lblSoLuongTreHan", String.valueOf(reader.getSoLuongTreHan()));
                    setLabelValue(readerContentPanel, "lblSoLuongHongSach",
                            String.valueOf(reader.getSoLuongHongSach()));
                    setLabelValue(readerContentPanel, "lblSoLuongMatSach", String.valueOf(reader.getSoLuongMatSach()));
                    setLabelValue(readerContentPanel, "lblTongTienPhat", formatCurrency(reader.getTongTienPhat()));
                    setLabelValue(readerContentPanel, "lblTongTienChuaDong",
                            formatCurrency(reader.getTongTienChuaDong()));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Helper: Set giá trị label theo name
     */
    private void setLabelValue(JPanel panel, String labelName, String value) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel row = (JPanel) comp;
                for (Component child : row.getComponents()) {
                    if (child instanceof JLabel && labelName.equals(child.getName())) {
                        ((JLabel) child).setText(value != null ? value : "--");
                        return;
                    }
                }
            }
        }
    }

    /**
     * Helper: Format tiền
     */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 ₫";
        }
        return String.format("%,d ₫", amount.longValue());
    }

    /**
     * Tải dữ liệu vào bảng
     */
    private void loadTableData() {
        // Nếu đang tìm kiếm, dùng searchPhatByTextPaginated, ngược lại dùng
        // getAllPhatPaginated
        PaginationResult<Phat> result;

        if (isSearching && !currentSearchText.isEmpty()) {
            result = phatController.searchPhatByTextPaginated(currentSearchText, currentCursor, pageSize);
        } else {
            result = phatController.getAllPhatPaginated(currentCursor, pageSize);
        }

        // Cập nhật dữ liệu
        tableModel.setRowCount(0);
        List<Phat> phats = result.getData();

        for (Phat p : phats) {
            tableModel.addRow(new Object[] {
                    p.getIdPhat(),
                    p.getIdPM(),
                    p.getMaBanSao(),
                    p.getLoaiPhat(),
                    p.getSoTien(),
                    p.getNgayGhiNhan(),
                    p.getTrangThai()
            });
        }

        // Thêm ô rỗng cho đến khi đủ 10 hàng
        while (tableModel.getRowCount() < pageSize) {
            tableModel.addRow(new Object[] { "", "", "", "", "", "", "" });
        }

        // Cập nhật thông tin phân trang
        totalCount = result.getTotalCount();
        updatePaginationButtons(result);
    }

    /**
     * Cập nhật trạng thái nút phân trang
     */
    private void updatePaginationButtons(PaginationResult<Phat> result) {
        // Lưu trữ nextCursor từ result
        this.nextCursor = result.getNextCursor();

        if (btnPrevious != null) {
            // Nút trang trước được bật nếu stack không rỗng (tức là có trang trước)
            btnPrevious.setEnabled(!cursorStack.isEmpty());
        }
        if (btnNext != null) {
            // Nút trang sau được bật nếu có nextCursor
            btnNext.setEnabled(result.isHasNext());
        }
        if (lblPageInfo != null) {
            int dataSize = result.getData().size();
            if (dataSize > 0) {
                int displayFrom = currentRecordStart;
                int displayTo = currentRecordStart + dataSize - 1;
                lblPageInfo.setText(String.format("Hiển thị %d - %d / %d", displayFrom, displayTo, totalCount));
            } else {
                lblPageInfo.setText("Hiển thị 0 - 0 / " + totalCount);
            }
        }
    }

    /**
     * Mở dialog tạo vé phạt
     */
    private void openCreatePhatDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Tạo Vé Phạt", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(550, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        dialogPanel.setBackground(new Color(245, 245, 245));

        // Lưu các component
        Map<String, JComponent> components = new HashMap<>();

        // Panel tìm kiếm chi tiết phiếu mượn
        JPanel searchDetailPanel = createSearchDetailPanel(components);
        dialogPanel.add(searchDetailPanel);
        dialogPanel.add(Box.createVerticalStrut(10));

        // IdPM
        createAndAddField(dialogPanel, components, "IdPM:", "txtIdPM", "");

        // MaBanSao
        createAndAddField(dialogPanel, components, "MaBanSao:", "txtMaBanSao", "");

        // LoaiPhat
        JComboBox<String> cmbLoaiPhat = createAndAddCombo(dialogPanel, "Loại Phạt:", "cmbLoaiPhat");
        cmbLoaiPhat.addItem("Tre han");
        cmbLoaiPhat.addItem("Hong sach");
        cmbLoaiPhat.addItem("Mat sach");
        components.put("cmbLoaiPhat", cmbLoaiPhat);

        // SoTien
        createAndAddField(dialogPanel, components, "Số Tiền:", "txtSoTien", "");

        // NgayGhiNhan (mặc định hôm nay)
        createAndAddField(dialogPanel, components, "Ngày Ghi Nhận:", "txtNgayGhiNhan",
                LocalDate.now().toString());
        ((JTextField) components.get("txtNgayGhiNhan")).setEditable(false);

        // TrangThai (mặc định 'Chua dong')
        JComboBox<String> cmbTrangThai = createAndAddCombo(dialogPanel, "Trạng Thái:", "cmbTrangThai");
        cmbTrangThai.addItem("Chua dong");
        cmbTrangThai.addItem("Da dong");
        cmbTrangThai.setSelectedItem("Chua dong");
        components.put("cmbTrangThai", cmbTrangThai);

        // Nút lưu và hủy
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton btnSave = new JButton("Lưu");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBackground(new Color(0, 153, 76));
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setMargin(new Insets(8, 16, 8, 16));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> {
            try {
                JTextField txtIdPM = (JTextField) components.get("txtIdPM");
                JTextField txtMaBanSao = (JTextField) components.get("txtMaBanSao");
                JComboBox<String> cmbLoaiPhat_ = (JComboBox<String>) components.get("cmbLoaiPhat");
                JTextField txtSoTien = (JTextField) components.get("txtSoTien");
                JTextField txtNgayGhiNhan = (JTextField) components.get("txtNgayGhiNhan");
                JComboBox<String> cmbTrangThai_ = (JComboBox<String>) components.get("cmbTrangThai");

                int idPM = Integer.parseInt(txtIdPM.getText().trim());
                int maBanSao = Integer.parseInt(txtMaBanSao.getText().trim());
                String loaiPhat = (String) cmbLoaiPhat_.getSelectedItem();
                BigDecimal soTien = new BigDecimal(txtSoTien.getText().trim());
                LocalDate ngayGhiNhan = LocalDate.parse(txtNgayGhiNhan.getText().trim());
                String trangThai = (String) cmbTrangThai_.getSelectedItem();

                Phat p = new Phat(0, idPM, maBanSao, loaiPhat, soTien, ngayGhiNhan, trangThai);

                if (phatController.createPhat(p)) {
                    JOptionPane.showMessageDialog(dialog, "Tạo vé phạt thành công!", "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadTableData();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Lỗi tạo vé phạt!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Dữ liệu không hợp lệ: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCancel.setBackground(new Color(200, 200, 200));
        btnCancel.setFocusPainted(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setMargin(new Insets(8, 16, 8, 16));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        dialogPanel.add(Box.createVerticalStrut(10));
        dialogPanel.add(buttonPanel);

        JScrollPane scrollDialog = new JScrollPane(dialogPanel);
        scrollDialog.setBorder(null);
        dialog.add(scrollDialog);
        dialog.setVisible(true);
    }

    /**
     * Mở dialog chỉnh sửa vé phạt
     */
    private void openEditPhatDialog(int idPhat, String loaiPhatInit, String soTienInit, String trangThaiInit) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Chỉnh Sửa Vé Phạt", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(550, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        dialogPanel.setBackground(new Color(245, 245, 245));

        Map<String, JComponent> components = new HashMap<>();

        // LoaiPhat
        JComboBox<String> cmbLoaiPhat = createAndAddCombo(dialogPanel, "Loại Phạt:", "cmbLoaiPhat");
        cmbLoaiPhat.addItem("Tre han");
        cmbLoaiPhat.addItem("Hong sach");
        cmbLoaiPhat.addItem("Mat sach");
        cmbLoaiPhat.setSelectedItem(loaiPhatInit);
        components.put("cmbLoaiPhat", cmbLoaiPhat);

        // SoTien
        createAndAddField(dialogPanel, components, "Số Tiền:", "txtSoTien", soTienInit);

        // TrangThai
        JComboBox<String> cmbTrangThai = createAndAddCombo(dialogPanel, "Trạng Thái:", "cmbTrangThai");
        cmbTrangThai.addItem("Chua dong");
        cmbTrangThai.addItem("Da dong");
        cmbTrangThai.setSelectedItem(trangThaiInit);
        components.put("cmbTrangThai", cmbTrangThai);

        // Nút lưu và hủy
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton btnSave = new JButton("Lưu");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBackground(new Color(0, 153, 76));
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setMargin(new Insets(8, 16, 8, 16));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> {
            try {
                JComboBox<String> cmbLoaiPhat_ = (JComboBox<String>) components.get("cmbLoaiPhat");
                JTextField txtSoTien_ = (JTextField) components.get("txtSoTien");
                JComboBox<String> cmbTrangThai_ = (JComboBox<String>) components.get("cmbTrangThai");

                String loaiPhat = (String) cmbLoaiPhat_.getSelectedItem();
                long soTien = Long.parseLong(txtSoTien_.getText().trim());
                String trangThai = (String) cmbTrangThai_.getSelectedItem();

                // Tạo object Phat với dữ liệu mới
                Phat phatUpdate = new Phat(idPhat, 0, 0, loaiPhat, BigDecimal.valueOf(soTien),
                        java.time.LocalDate.now(), trangThai);

                // Gọi controller để update vé phạt
                boolean success = phatController.updatePhat(phatUpdate);

                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Cập nhật vé phạt thành công!", "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                    // Reset table data
                    currentCursor = 0;
                    isSearching = false;
                    currentSearchText = "";
                    txtSearchIdPM.setText("");
                    loadTableData();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Cập nhật thất bại!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCancel.setBackground(new Color(200, 200, 200));
        btnCancel.setFocusPainted(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setMargin(new Insets(8, 16, 8, 16));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dialog.dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        dialogPanel.add(Box.createVerticalStrut(10));
        dialogPanel.add(buttonPanel);

        JScrollPane scrollDialog = new JScrollPane(dialogPanel);
        scrollDialog.setBorder(null);
        dialog.add(scrollDialog);
        dialog.setVisible(true);
    }

    /**
     * Helper: Tạo và thêm TextField vào panel
     */
    private void createAndAddField(JPanel panel, Map<String, JComponent> components, String label,
            String key, String initialValue) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setPreferredSize(new Dimension(120, 30));

        JTextField txt = new JTextField(initialValue);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        row.add(lbl, BorderLayout.WEST);
        row.add(txt, BorderLayout.CENTER);

        panel.add(row);
        components.put(key, txt);
    }

    /**
     * Helper: Tạo và thêm ComboBox vào panel
     */
    private JComboBox<String> createAndAddCombo(JPanel panel, String label, String key) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setPreferredSize(new Dimension(120, 30));

        JComboBox<String> combo = new JComboBox<>();
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        combo.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        row.add(lbl, BorderLayout.WEST);
        row.add(combo, BorderLayout.CENTER);

        panel.add(row);

        return combo;
    }

    /**
     * Helper: Tạo panel tìm kiếm chi tiết phiếu mượn trong dialog
     */
    private JPanel createSearchDetailPanel(Map<String, JComponent> components) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Tìm Phiếu Mượn",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 11),
                new Color(0, 102, 153)));

        // TextField tìm kiếm
        JTextField txtSearchDetail = new JTextField(20);
        txtSearchDetail.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtSearchDetail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        txtSearchDetail.setToolTipText("Nhập IdPM, MaBanSao, Tên, hoặc SDT");

        // Nút tìm kiếm
        JButton btnSearch = new JButton("Tìm");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setBackground(new Color(0, 102, 153));
        btnSearch.setFocusPainted(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // List kết quả
        DefaultListModel<ChiTietPhieuMuonInfo> listModel = new DefaultListModel<>();
        JList<ChiTietPhieuMuonInfo> resultList = new JList<>(listModel);
        resultList.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        resultList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                if (value instanceof ChiTietPhieuMuonInfo) {
                    ChiTietPhieuMuonInfo info = (ChiTietPhieuMuonInfo) value;
                    label.setText(String.format("IdPM: %d | MaBanSao: %d | %s (%s)",
                            info.getIdPM(), info.getMaBanSao(), info.getHoTen(), info.getSdt()));
                }
                return label;
            }
        });

        JScrollPane scrollResult = new JScrollPane(resultList);
        scrollResult.setPreferredSize(new Dimension(Integer.MAX_VALUE, 120));

        // Action tìm kiếm
        btnSearch.addActionListener(e -> {
            String text = txtSearchDetail.getText().trim();
            listModel.clear();

            if (!text.isEmpty()) {
                List<ChiTietPhieuMuonInfo> results = phatController.searchChiTietPhieuMuon(text);
                results.forEach(listModel::addElement);
            }
        });

        // Action click vào kết quả
        resultList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && resultList.getSelectedValue() != null) {
                ChiTietPhieuMuonInfo selected = resultList.getSelectedValue();
                ((JTextField) components.get("txtIdPM")).setText(String.valueOf(selected.getIdPM()));
                ((JTextField) components.get("txtMaBanSao")).setText(String.valueOf(selected.getMaBanSao()));
                txtSearchDetail.setText("");
                listModel.clear();
            }
        });

        // Panel input
        JPanel inputRow = new JPanel(new BorderLayout(5, 0));
        inputRow.setOpaque(false);
        inputRow.add(txtSearchDetail, BorderLayout.CENTER);
        inputRow.add(btnSearch, BorderLayout.EAST);

        panel.add(inputRow, BorderLayout.NORTH);
        panel.add(scrollResult, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Dialog nhập thông tin bạn đọc để thanh toán phạt
     */
    private void openPaymentDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Thanh Toán Phạt", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(600, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 245, 245));

        // Tiêu đề
        JLabel lblTitle = new JLabel("Nhập thông tin bạn đọc");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(33, 33, 33));
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(15));

        // Input ID Bạn Đọc
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setOpaque(false);
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        JLabel lblIdBD = new JLabel("ID Bạn Đọc:");
        lblIdBD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblIdBD.setPreferredSize(new Dimension(100, 35));
        JTextField txtIdBD = new JTextField();
        txtIdBD.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Auto-fill IdBD nếu đang select một vé phạt
        int selectedRow = tablePhat.getSelectedRow();
        if (selectedRow >= 0) {
            try {
                Object idPMObj = tableModel.getValueAt(selectedRow, 1); // Column 1 là IdPM
                if (idPMObj != null && !idPMObj.toString().isEmpty()) {
                    int idPM = Integer.parseInt(idPMObj.toString());
                    // Lấy chi tiết phiếu mượn để tìm IdBD
                    ChiTietPhieuMuonInfo detail = phatController.getChiTietPhieuMuonByIdPMAndMaBanSao(idPM, 0);
                    if (detail != null && detail.getIdBD() > 0) {
                        txtIdBD.setText(String.valueOf(detail.getIdBD()));
                    }
                }
            } catch (Exception e) {
                // Nếu lỗi, text field vẫn trống để user nhập thủ công
            }
        }

        inputPanel.add(lblIdBD, BorderLayout.WEST);
        inputPanel.add(txtIdBD, BorderLayout.CENTER);
        mainPanel.add(inputPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // Nút tìm kiếm
        JButton btnSearch = new JButton("Tìm Kiếm");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setBackground(new Color(33, 150, 243));
        btnSearch.setFocusPainted(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setMargin(new Insets(8, 16, 8, 16));
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnSearch.addActionListener(e -> {
            try {
                String idBDStr = txtIdBD.getText().trim();
                if (idBDStr.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Vui lòng nhập ID bạn đọc!",
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                int idBD = Integer.parseInt(idBDStr);
                BanDocPhat banDocPhat = phatController.getBanDocPhatByIdBD(idBD);

                if (banDocPhat == null) {
                    JOptionPane.showMessageDialog(dialog, "Không tìm thấy bạn đọc!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Hiển thị dialog xác nhận thanh toán
                openConfirmPaymentDialog(banDocPhat, dialog);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "ID bạn đọc phải là số!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel searchBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchBtnPanel.setOpaque(false);
        searchBtnPanel.add(btnSearch);
        mainPanel.add(searchBtnPanel);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    /**
     * Dialog xác nhận thanh toán phạt
     */
    private void openConfirmPaymentDialog(BanDocPhat banDocPhat, JDialog parentDialog) {
        JDialog confirmDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Xác Nhận Thanh Toán", true);
        confirmDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        confirmDialog.setSize(550, 400);
        confirmDialog.setLocationRelativeTo(this);
        confirmDialog.setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 245, 245));

        // Tiêu đề
        JLabel lblTitle = new JLabel("Thông Tin Thanh Toán");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(33, 33, 33));
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(15));

        // Thông tin bạn đọc
        mainPanel.add(createInfoRow("ID Bạn Đọc:", String.valueOf(banDocPhat.getIdBD())));
        mainPanel.add(createInfoRow("Tên:", banDocPhat.getHoTen()));
        mainPanel.add(createInfoRow("Email:", banDocPhat.getEmail()));
        mainPanel.add(createInfoRow("Địa Chỉ:", banDocPhat.getDiaChi()));
        mainPanel.add(createInfoRow("SĐT:", banDocPhat.getSdt()));
        mainPanel.add(Box.createVerticalStrut(15));

        // Thông tin phạt
        JLabel lblPhatInfo = new JLabel("Thông Tin Phạt");
        lblPhatInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPhatInfo.setForeground(new Color(244, 67, 54));
        mainPanel.add(lblPhatInfo);
        mainPanel.add(Box.createVerticalStrut(8));

        mainPanel.add(createInfoRow("Trễ Hạn:", String.valueOf(banDocPhat.getSoLuongTreHan())));
        mainPanel.add(createInfoRow("Hỏng Sách:", String.valueOf(banDocPhat.getSoLuongHongSach())));
        mainPanel.add(createInfoRow("Mất Sách:", String.valueOf(banDocPhat.getSoLuongMatSach())));
        mainPanel.add(Box.createVerticalStrut(10));

        // Tổng tiền (hiển thị nổi bật)
        JPanel totalPanel = new JPanel(new BorderLayout(10, 0));
        totalPanel.setOpaque(false);
        totalPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        JLabel lblTotalLabel = new JLabel("Tổng Tiền Phạt Chưa Đóng:");
        lblTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotalLabel.setPreferredSize(new Dimension(200, 35));
        JLabel lblTotalAmount = new JLabel(String.format("₫ %,d",
                banDocPhat.getTongTienChuaDong().longValue()));
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalAmount.setForeground(new Color(244, 67, 54));
        totalPanel.add(lblTotalLabel, BorderLayout.WEST);
        totalPanel.add(lblTotalAmount, BorderLayout.CENTER);
        mainPanel.add(totalPanel);
        mainPanel.add(Box.createVerticalGlue());

        // Nút xác nhận và hủy
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton btnConfirm = new JButton("Xác Nhận Thanh Toán");
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setBackground(new Color(76, 175, 80));
        btnConfirm.setFocusPainted(false);
        btnConfirm.setBorderPainted(false);
        btnConfirm.setMargin(new Insets(8, 16, 8, 16));
        btnConfirm.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirm.addActionListener(e -> {
            boolean success = phatController.updateAllPhatToDaDongByIdBD(banDocPhat.getIdBD());
            if (success) {
                JOptionPane.showMessageDialog(confirmDialog, "Thanh toán thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                // Reset table
                currentCursor = 0;
                isSearching = false;
                currentSearchText = "";
                txtSearchIdPM.setText("");
                loadTableData();
                confirmDialog.dispose();
                parentDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(confirmDialog, "Thanh toán thất bại!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnCancel.setBackground(new Color(200, 200, 200));
        btnCancel.setFocusPainted(false);
        btnCancel.setBorderPainted(false);
        btnCancel.setMargin(new Insets(8, 16, 8, 16));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> confirmDialog.dispose());

        buttonPanel.add(btnConfirm);
        buttonPanel.add(btnCancel);

        mainPanel.add(buttonPanel);
        confirmDialog.add(mainPanel);
        confirmDialog.setVisible(true);
    }

    /**
     * Helper method để tạo info row
     */
    private JPanel createInfoRow(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblLabel.setPreferredSize(new Dimension(150, 25));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblValue.setForeground(new Color(66, 66, 66));

        panel.add(lblLabel, BorderLayout.WEST);
        panel.add(lblValue, BorderLayout.CENTER);
        return panel;
    }
}
