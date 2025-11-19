package com.mycompany.quanlythuvien.view.dialog;

import com.mycompany.quanlythuvien.controller.YeuCauResetMKController;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

/**
 * Dialog tạo request reset mật khẩu
 * @author Tien
 */
public class ResetMatKhauDialog extends JDialog {
    
    private final YeuCauResetMKController resetController = new YeuCauResetMKController();
    
    // Màu sắc chủ đạo
    private final Color PRIMARY_COLOR = new Color(37, 99, 235); // Xanh dương hiện đại hơn
    private final Color HOVER_COLOR = new Color(29, 78, 216);
    private final Color TEXT_COLOR = new Color(31, 41, 55);
    private final Color SUBTEXT_COLOR = new Color(107, 114, 128);
    private final Color BORDER_COLOR = new Color(209, 213, 219);

    public ResetMatKhauDialog(Window parent) {
        super(parent, "Quên mật khẩu", ModalityType.APPLICATION_MODAL);
        initComponents();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setSize(500, 500);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel lblTitle = new JLabel("Quên mật khẩu?");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 26));
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblInfo = new JLabel("<html><div style='text-align: center; width: 350px;'>Nhập email đăng ký và lý do.<br>Hệ thống sẽ gửi mật khẩu mới cho bạn.</div></html>");
        lblInfo.setFont(new Font("Arial", Font.PLAIN, 14));
        lblInfo.setForeground(SUBTEXT_COLOR);
        lblInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JLabel lblEmail = createLabel("Email của bạn");
        JTextField txtEmail = createTextField();

        JLabel lblReason = createLabel("Lý do khôi phục");
        JTextArea txtReason = createTextArea();
        JScrollPane scrollReason = new JScrollPane(txtReason);
        scrollReason.setBorder(new RoundedBorder(BORDER_COLOR, 8));
        scrollReason.getViewport().setBackground(Color.WHITE);

        gbc.gridy = 0; formPanel.add(lblEmail, gbc);
        gbc.gridy = 1; formPanel.add(txtEmail, gbc);
        
        gbc.gridy = 2; 
        gbc.insets = new Insets(15, 0, 5, 0);
        formPanel.add(lblReason, gbc);
        
        gbc.gridy = 3; 
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.ipady = 40;
        formPanel.add(scrollReason, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setMaximumSize(new Dimension(500, 50));

        JButton btnCancel = createButton("Hủy bỏ", Color.WHITE, SUBTEXT_COLOR, BORDER_COLOR);
        JButton btnSubmit = createButton("Gửi yêu cầu", PRIMARY_COLOR, Color.WHITE, PRIMARY_COLOR);

        btnCancel.addActionListener(e -> dispose());
        
        btnSubmit.addActionListener(e -> {
            String email = txtEmail.getText().trim();
            String reason = txtReason.getText().trim();
            
            if (email.isEmpty()) {
                showError("Vui lòng nhập email!");
                txtEmail.requestFocus();
                return;
            }
            if (reason.isEmpty()) {
                showError("Vui lòng nhập lý do!");
                txtReason.requestFocus();
                return;
            }
            
            try {
                resetController.createYeuCau(email, reason);
                JOptionPane.showMessageDialog(this,
                    "Yêu cầu đã được gửi! Vui lòng kiểm tra email.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) {
                showError("Lỗi: " + ex.getMessage());
            }
        });

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSubmit);

        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(lblInfo);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(buttonPanel);

        add(mainPanel);
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setForeground(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER_COLOR, 8),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea(3, 20);
        area.setFont(new Font("Arial", Font.PLAIN, 14));
        area.setForeground(TEXT_COLOR);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        // Viền của TextArea sẽ do JScrollPane quản lý
        area.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); 
        return area;
    }

    private JButton createButton(String text, Color bg, Color fg, Color border) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    // Nếu là nút màu trắng thì hover xám nhẹ, nếu nút màu thì hover đậm hơn
                    g2.setColor(bg.equals(Color.WHITE) ? new Color(245, 245, 245) : HOVER_COLOR);
                } else {
                    g2.setColor(bg);
                }
                
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                
                // Border
                g2.setColor(border);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        return btn;
    }
    
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // Class vẽ viền bo tròn cho TextField
    private static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius + 1, radius + 1, radius + 2, radius);
        }
    }
}