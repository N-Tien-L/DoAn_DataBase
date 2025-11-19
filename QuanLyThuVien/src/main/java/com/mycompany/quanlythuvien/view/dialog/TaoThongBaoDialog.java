package com.mycompany.quanlythuvien.view.dialog;

import com.mycompany.quanlythuvien.controller.ThongBaoController;
import com.mycompany.quanlythuvien.dao.TaiKhoanDAO;
import com.mycompany.quanlythuvien.model.TaiKhoan;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Dialog tạo thông báo mới
 * @author Tien
 */
public class TaoThongBaoDialog extends JDialog {
    
    private final ThongBaoController controller;
    private final TaiKhoan currentUser;
    private boolean success = false;
    
    private JTextField txtTieuDe;
    private JTextArea txtNoiDung;
    private JList<String> listAllEmails;
    private JList<String> listSelectedEmails;
    private DefaultListModel<String> modelAll;
    private DefaultListModel<String> modelSelected;
    
    public TaoThongBaoDialog(Window parent, TaiKhoan currentUser) {
        super(parent, "Tạo thông báo mới", ModalityType.APPLICATION_MODAL);
        this.controller = new ThongBaoController();
        this.currentUser = currentUser;
        
        initComponents();
        loadAllEmails();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setSize(800, 600);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(Color.WHITE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel lblTitle = new JLabel("Tạo thông báo mới");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(37, 99, 235));
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Tiêu đề
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel("Tiêu đề:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtTieuDe = new JTextField();
        txtTieuDe.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(txtTieuDe, gbc);
        
        // Nội dung
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("Nội dung:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 0.3;
        gbc.fill = GridBagConstraints.BOTH;
        txtNoiDung = new JTextArea(5, 40);
        txtNoiDung.setFont(new Font("Arial", Font.PLAIN, 14));
        txtNoiDung.setLineWrap(true);
        txtNoiDung.setWrapStyleWord(true);
        JScrollPane scrollNoiDung = new JScrollPane(txtNoiDung);
        formPanel.add(scrollNoiDung, gbc);
        
        // Recipients Panel
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; 
        gbc.weightx = 1.0; gbc.weighty = 0.7;
        formPanel.add(createRecipientPanel(), gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Arial", Font.PLAIN, 14));
        btnCancel.addActionListener(e -> dispose());
        
        JButton btnSend = new JButton("Gửi thông báo");
        btnSend.setFont(new Font("Arial", Font.BOLD, 14));
        btnSend.setBackground(new Color(37, 99, 235));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        btnSend.addActionListener(e -> handleSend());
        
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSend);
        
        mainPanel.add(lblTitle, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createRecipientPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        
        JLabel lblRecipients = new JLabel("Người nhận:");
        lblRecipients.setFont(new Font("Arial", Font.BOLD, 13));
        
        JPanel listsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        listsPanel.setBackground(Color.WHITE);
        
        // All emails list
        JPanel allPanel = new JPanel(new BorderLayout(5, 5));
        allPanel.setBackground(Color.WHITE);
        allPanel.add(new JLabel("Tất cả tài khoản:"), BorderLayout.NORTH);
        
        modelAll = new DefaultListModel<>();
        listAllEmails = new JList<>(modelAll);
        listAllEmails.setFont(new Font("Arial", Font.PLAIN, 12));
        allPanel.add(new JScrollPane(listAllEmails), BorderLayout.CENTER);
        
        // Control buttons
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(Color.WHITE);
        
        JButton btnAdd = new JButton("  >>  ");
        JButton btnAddAll = new JButton("  >>>  ");
        JButton btnRemove = new JButton("  <<  ");
        JButton btnRemoveAll = new JButton("  <<<  ");
        
        btnAdd.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAddAll.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRemove.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRemoveAll.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnAdd.addActionListener(e -> moveSelected(listAllEmails, modelAll, modelSelected));
        btnAddAll.addActionListener(e -> moveAll(modelAll, modelSelected));
        btnRemove.addActionListener(e -> moveSelected(listSelectedEmails, modelSelected, modelAll));
        btnRemoveAll.addActionListener(e -> moveAll(modelSelected, modelAll));
        
        controlPanel.add(Box.createVerticalGlue());
        controlPanel.add(btnAdd);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(btnAddAll);
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(btnRemove);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(btnRemoveAll);
        controlPanel.add(Box.createVerticalGlue());
        
        // Selected emails list
        JPanel selectedPanel = new JPanel(new BorderLayout(5, 5));
        selectedPanel.setBackground(Color.WHITE);
        selectedPanel.add(new JLabel("Đã chọn:"), BorderLayout.NORTH);
        
        modelSelected = new DefaultListModel<>();
        listSelectedEmails = new JList<>(modelSelected);
        listSelectedEmails.setFont(new Font("Arial", Font.PLAIN, 12));
        selectedPanel.add(new JScrollPane(listSelectedEmails), BorderLayout.CENTER);
        
        listsPanel.add(allPanel);
        listsPanel.add(controlPanel);
        listsPanel.add(selectedPanel);
        
        panel.add(lblRecipients, BorderLayout.NORTH);
        panel.add(listsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadAllEmails() {
        try {
            TaiKhoanDAO dao = new TaiKhoanDAO();
            List<TaiKhoan> accounts = dao.getAllAccountsSimple();
            
            modelAll.clear();
            for (TaiKhoan acc : accounts) {
                modelAll.addElement(acc.getEmail());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Không thể tải danh sách tài khoản: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void moveSelected(JList<String> fromList, DefaultListModel<String> fromModel, 
                             DefaultListModel<String> toModel) {
        List<String> selected = fromList.getSelectedValuesList();
        for (String email : selected) {
            if (!contains(toModel, email)) {
                toModel.addElement(email);
                fromModel.removeElement(email);
            }
        }
    }
    
    private void moveAll(DefaultListModel<String> fromModel, DefaultListModel<String> toModel) {
        while (!fromModel.isEmpty()) {
            String email = fromModel.getElementAt(0);
            if (!contains(toModel, email)) {
                toModel.addElement(email);
            }
            fromModel.removeElement(email);
        }
    }
    
    private boolean contains(DefaultListModel<String> model, String email) {
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).equals(email)) {
                return true;
            }
        }
        return false;
    }
    
    private void handleSend() {
        String tieuDe = txtTieuDe.getText().trim();
        String noiDung = txtNoiDung.getText().trim();
        
        if (tieuDe.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập tiêu đề!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            txtTieuDe.requestFocus();
            return;
        }
        
        if (noiDung.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập nội dung!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            txtNoiDung.requestFocus();
            return;
        }
        
        if (modelSelected.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn ít nhất một người nhận!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<String> recipients = new ArrayList<>();
        for (int i = 0; i < modelSelected.getSize(); i++) {
            recipients.add(modelSelected.getElementAt(i));
        }
        
        try {
            int id = controller.createAnnouncement(currentUser, tieuDe, noiDung, recipients);
            
            JOptionPane.showMessageDialog(this,
                "Đã gửi thông báo thành công!\nID: " + id + "\nSố người nhận: " + recipients.size(),
                "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
            
            success = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Lỗi gửi thông báo: " + e.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isSuccess() {
        return success;
    }
}
