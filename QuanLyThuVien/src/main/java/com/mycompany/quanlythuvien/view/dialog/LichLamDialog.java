package com.mycompany.quanlythuvien.view.dialog;

import com.mycompany.quanlythuvien.controller.LichLamController;
import com.mycompany.quanlythuvien.model.LichLam;
import com.mycompany.quanlythuvien.model.TaiKhoan;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Dialog quản lý lịch làm
 * @author Tien
 */
public class LichLamDialog extends JDialog {
    
    private final LichLamController controller;
    private final TaiKhoan currentUser;
    private final List<TaiKhoan> thuThus;
    private final LichLam existingSchedule;
    
    private JComboBox<String> cboThuThu;
    private JSpinner spinnerDate;
    private JSpinner spinnerStartTime;
    private JSpinner spinnerEndTime;
    private JComboBox<String> cboStatus;
    private JTextArea txtNote;
    
    private boolean success = false;
    private final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Color PRIMARY_COLOR = new Color(0, 123, 255);
    
    public LichLamDialog(Window owner, TaiKhoan currentUser, List<TaiKhoan> thuThus, LichLam existingSchedule) {
        super(owner, existingSchedule == null ? "Thêm Lịch Làm Mới" : "Cập Nhật Lịch Làm", ModalityType.APPLICATION_MODAL);
        this.controller = new LichLamController();
        this.currentUser = currentUser;
        this.thuThus = thuThus;
        this.existingSchedule = existingSchedule;
        
        initComponents();
        if (existingSchedule != null) {
            loadData();
        }
        
        setSize(600, 700); // Tăng kích thước dialog
        setLocationRelativeTo(owner);
        setResizable(false);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);
        
        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        JLabel lblHeader = new JLabel(existingSchedule == null ? "TẠO LỊCH LÀM VIỆC" : "CHỈNH SỬA LỊCH LÀM");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setForeground(PRIMARY_COLOR);
        headerPanel.add(lblHeader);
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 15, 0); // Spacing bottom
        gbc.gridx = 0; 
        
        // 1. Chọn Thủ thư
        addFormLabel(formPanel, "Nhân viên thủ thư", gbc, 0);
        String[] emails = thuThus.stream()
            .map(tk -> tk.getEmail() + " (" + tk.getHoTen() + ")")
            .toArray(String[]::new);
        cboThuThu = new JComboBox<>(emails);
        styleComponent(cboThuThu);
        addFormComponent(formPanel, cboThuThu, gbc, 1);
        
        // 2. Chọn Ngày (Custom Spinner)
        addFormLabel(formPanel, "Ngày làm việc", gbc, 2);
        SpinnerDateModel dateModel = new SpinnerDateModel();
        spinnerDate = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinnerDate, "dd/MM/yyyy");
        spinnerDate.setEditor(dateEditor);
        styleSpinner(spinnerDate);
        addFormComponent(formPanel, spinnerDate, gbc, 3);
        
        // 3. Chọn Giờ (Đặt 2 spinner trên 1 dòng)
        addFormLabel(formPanel, "Thời gian làm việc", gbc, 4);
        
        JPanel timePanel = new JPanel(new GridLayout(1, 2, 15, 0)); // 15px gap
        timePanel.setBackground(Color.WHITE);
        
        // Start Time
        spinnerStartTime = createTimeSpinner();
        JPanel pnlStart = new JPanel(new BorderLayout());
        pnlStart.setBackground(Color.WHITE);
        pnlStart.add(new JLabel("Từ:"), BorderLayout.NORTH);
        pnlStart.add(spinnerStartTime, BorderLayout.CENTER);
        
        // End Time
        spinnerEndTime = createTimeSpinner();
        JPanel pnlEnd = new JPanel(new BorderLayout());
        pnlEnd.setBackground(Color.WHITE);
        pnlEnd.add(new JLabel("Đến:"), BorderLayout.NORTH);
        pnlEnd.add(spinnerEndTime, BorderLayout.CENTER);
        
        timePanel.add(pnlStart);
        timePanel.add(pnlEnd);
        
        addFormComponent(formPanel, timePanel, gbc, 5);
        
        int currentRow = 6;
        // 4. Trạng thái (Chỉ hiện khi edit)
        if (existingSchedule != null) {
            addFormLabel(formPanel, "Trạng thái", gbc, currentRow++);
            cboStatus = new JComboBox<>(new String[]{"Scheduled", "Done", "Cancelled"});
            styleComponent(cboStatus);
            addFormComponent(formPanel, cboStatus, gbc, currentRow++);
        }
        
        // 5. Ghi chú
        addFormLabel(formPanel, "Ghi chú", gbc, currentRow++);
        txtNote = new JTextArea(5, 20); // Tăng số dòng hiển thị mặc định
        txtNote.setFont(INPUT_FONT);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        JScrollPane scrollNote = new JScrollPane(txtNote);
        scrollNote.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        gbc.gridy = currentRow++;
        gbc.weighty = 1.0; // Fill remaining space
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(scrollNote, gbc);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));
        
        JButton btnCancel = new JButton("Hủy bỏ");
        styleButton(btnCancel, new Color(108, 117, 125));
        
        JButton btnSave = new JButton("Lưu lại");
        styleButton(btnSave, PRIMARY_COLOR);
        
        btnSave.addActionListener(e -> handleSave());
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        
        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    // --- Helper Methods để Style ---
    
    private void addFormLabel(JPanel panel, String text, GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(80, 80, 80));
        lbl.setBorder(new EmptyBorder(0, 0, 5, 0)); // Space below label
        panel.add(lbl, gbc);
    }
    
    private void addFormComponent(JPanel panel, JComponent comp, GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        panel.add(comp, gbc);
    }
    
    private JSpinner createTimeSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "HH:mm");
        spinner.setEditor(editor);
        styleSpinner(spinner);
        return spinner;
    }
    
    private void styleComponent(JComponent comp) {
        comp.setFont(INPUT_FONT);
        comp.setBackground(Color.WHITE);
        // Hack để JComboBox trông đẹp hơn
        if (comp instanceof JComboBox) {
            ((JComboBox<?>)comp).setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
        }
    }
    
    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(INPUT_FONT);
        // Tạo viền padding bên trong
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor)editor).getTextField().setBorder(new EmptyBorder(5, 5, 5, 5));
        }
        spinner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
    }
    
    private void styleButton(JButton btn, Color color) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 25, 10, 25));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void loadData() {
        // Find matching email in combo
        for (int i = 0; i < cboThuThu.getItemCount(); i++) {
            if (cboThuThu.getItemAt(i).startsWith(existingSchedule.getEmailThuThu())) {
                cboThuThu.setSelectedIndex(i);
                break;
            }
        }
        
        // Set date
        java.util.Date utilDate = new java.util.Date(existingSchedule.getNgay().getTime());
        spinnerDate.setValue(utilDate);
        
        // Set times
        java.util.Calendar cal = java.util.Calendar.getInstance();
        
        cal.set(java.util.Calendar.HOUR_OF_DAY, existingSchedule.getGioBatDau().toLocalTime().getHour());
        cal.set(java.util.Calendar.MINUTE, existingSchedule.getGioBatDau().toLocalTime().getMinute());
        spinnerStartTime.setValue(cal.getTime());
        
        cal.set(java.util.Calendar.HOUR_OF_DAY, existingSchedule.getGioKetThuc().toLocalTime().getHour());
        cal.set(java.util.Calendar.MINUTE, existingSchedule.getGioKetThuc().toLocalTime().getMinute());
        spinnerEndTime.setValue(cal.getTime());
        
        // Set status
        if (cboStatus != null) {
            cboStatus.setSelectedItem(existingSchedule.getTrangThai());
        }
        
        // Set note
        if (existingSchedule.getGhiChu() != null) {
            txtNote.setText(existingSchedule.getGhiChu());
        }
    }
    
    private void handleSave() {
        try {
            // Get selected email
            String selected = (String) cboThuThu.getSelectedItem();
            String email = selected.substring(0, selected.indexOf(" ("));
            
            // Get date
            java.util.Date utilDate = (java.util.Date) spinnerDate.getValue();
            LocalDate date = new java.sql.Date(utilDate.getTime()).toLocalDate();
            
            // Get times
            java.util.Date startTimeUtil = (java.util.Date) spinnerStartTime.getValue();
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(startTimeUtil);
            LocalTime startTime = LocalTime.of(cal.get(java.util.Calendar.HOUR_OF_DAY), 
                                               cal.get(java.util.Calendar.MINUTE));
            
            java.util.Date endTimeUtil = (java.util.Date) spinnerEndTime.getValue();
            cal.setTime(endTimeUtil);
            LocalTime endTime = LocalTime.of(cal.get(java.util.Calendar.HOUR_OF_DAY), 
                                            cal.get(java.util.Calendar.MINUTE));
            
            String note = txtNote.getText().trim();
            
            if (existingSchedule == null) {
                // Create new
                controller.createShift(currentUser, email, date, startTime, endTime, note.isEmpty() ? null : note);
                JOptionPane.showMessageDialog(this, "Thêm lịch làm thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Update existing
                String status = cboStatus != null ? (String) cboStatus.getSelectedItem() : "Scheduled";
                controller.updateSchedule(currentUser, existingSchedule.getIdLich(), email, date, 
                                        startTime, endTime, status, note.isEmpty() ? null : note);
                JOptionPane.showMessageDialog(this, "Cập nhật lịch làm thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }
            
            success = true;
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isSuccess() {
        return success;
    }
}
