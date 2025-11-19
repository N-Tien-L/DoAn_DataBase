package com.mycompany.quanlythuvien.view.panel;

import com.mycompany.quanlythuvien.controller.LichLamController;
import com.mycompany.quanlythuvien.controller.TaiKhoanController;
import com.mycompany.quanlythuvien.model.LichLam;
import com.mycompany.quanlythuvien.model.TaiKhoan;
import com.mycompany.quanlythuvien.view.dialog.LichLamDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Panel admin quản lý lịch làm
 * @author Tien
 */
public class LichLamViecPanel extends JPanel {

    private final TaiKhoan currentUser;
    private final LichLamController lichLamController;
    private final TaiKhoanController taiKhoanController;

    private LocalDate currentWeekMonday;
    private JLabel lblDateRange;
    private JPanel scheduleGridPanel;

    public LichLamViecPanel(TaiKhoan currentUser) {
        this.currentUser = currentUser;
        this.lichLamController = new LichLamController();
        this.taiKhoanController = new TaiKhoanController();
        this.currentWeekMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "LỊCH LÀM VIỆC",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 16)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setBackground(Color.WHITE);

        // 1. Header & Navigation
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(Color.WHITE);

        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftNav.setBackground(Color.WHITE);
        JButton btnAddSch = new JButton("+ Thêm ca làm");
        btnAddSch.setFont(new Font("Arial", Font.PLAIN, 14));
        btnAddSch.addActionListener(e -> handleAddSchedule());
        leftNav.add(btnAddSch);

        JPanel centerNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        centerNav.setBackground(Color.WHITE);

        Font navButtonFont = new Font("Arial", Font.PLAIN, 14);
        JButton btnPrev = new JButton("◄");
        btnPrev.setFont(navButtonFont);
        JButton btnNext = new JButton("►");
        btnNext.setFont(navButtonFont);
        JButton btnToday = new JButton("Tuần này");
        btnToday.setFont(navButtonFont);

        lblDateRange = new JLabel("Loading...");
        lblDateRange.setFont(new Font("Arial", Font.BOLD, 16));
        lblDateRange.setPreferredSize(new Dimension(220, 30));
        lblDateRange.setHorizontalAlignment(SwingConstants.CENTER);

        btnPrev.addActionListener(e -> {
            currentWeekMonday = currentWeekMonday.minusWeeks(1);
            loadWeekSchedule();
        });

        btnNext.addActionListener(e -> {
            currentWeekMonday = currentWeekMonday.plusWeeks(1);
            loadWeekSchedule();
        });

        btnToday.addActionListener(e -> {
            currentWeekMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            loadWeekSchedule();
        });

        centerNav.add(btnPrev);
        centerNav.add(lblDateRange);
        centerNav.add(btnNext);
        centerNav.add(btnToday);

        navPanel.add(leftNav, BorderLayout.WEST);
        navPanel.add(centerNav, BorderLayout.CENTER);

        // 2. Main Grid
        scheduleGridPanel = new JPanel(new GridLayout(1, 7, 5, 0));
        scheduleGridPanel.setBackground(new Color(245, 245, 245));

        JScrollPane scrollGrid = new JScrollPane(scheduleGridPanel);
        scrollGrid.setBorder(null);
        scrollGrid.getVerticalScrollBar().setUnitIncrement(16);
        scrollGrid.setPreferredSize(new Dimension(0, 400));

        add(navPanel, BorderLayout.NORTH);
        add(scrollGrid, BorderLayout.CENTER);

        loadWeekSchedule();
    }

    public void loadWeekSchedule() {
        scheduleGridPanel.removeAll();

        LocalDate sunday = currentWeekMonday.plusDays(6);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblDateRange.setText(currentWeekMonday.format(fmt) + " - " + sunday.format(fmt));

        try {
            List<LichLam> weeklyShifts = lichLamController.getSchedulesByDateRange(
                currentUser,
                currentWeekMonday,
                sunday
            );

            if (weeklyShifts == null) weeklyShifts = new ArrayList<>();

            DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd/MM");
            String[] dayNames = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "CN"};

            for (int i = 0; i < 7; i++) {
                LocalDate dateAtCol = currentWeekMonday.plusDays(i);
                String headerText = dayNames[i] + " (" + dateAtCol.format(dayFmt) + ")";
                boolean isToday = dateAtCol.equals(LocalDate.now());

                List<LichLam> dailyShifts = weeklyShifts.stream()
                    .filter(shift -> shift.getNgay().toLocalDate().equals(dateAtCol))
                    .collect(Collectors.toList());

                scheduleGridPanel.add(createDayColumn(headerText, dailyShifts, isToday));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JLabel err = new JLabel("Lỗi tải dữ liệu: " + e.getMessage());
            err.setForeground(Color.RED);
            scheduleGridPanel.add(err);
        }

        scheduleGridPanel.revalidate();
        scheduleGridPanel.repaint();
    }

    private JPanel createDayColumn(String headerText, List<LichLam> shifts, boolean isToday) {
        JPanel column = new JPanel(new BorderLayout());
        column.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        column.setBackground(isToday ? new Color(255, 250, 230) : Color.WHITE);

        JLabel lblHeader = new JLabel(headerText, SwingConstants.CENTER);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 15));
        lblHeader.setOpaque(true);
        lblHeader.setBackground(isToday ? new Color(255, 220, 150) : new Color(230, 230, 230));
        lblHeader.setPreferredSize(new Dimension(0, 35));
        lblHeader.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(5, 3, 5, 3));

        List<LichLam> sangShifts = shifts.stream()
            .filter(s -> s.getGioBatDau().toLocalTime().getHour() < 12)
            .collect(Collectors.toList());
        List<LichLam> chieuShifts = shifts.stream()
            .filter(s -> s.getGioBatDau().toLocalTime().getHour() >= 12)
            .collect(Collectors.toList());

        addShiftsToBody(bodyPanel, "SÁNG", sangShifts);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        addShiftsToBody(bodyPanel, "CHIỀU/TỐI", chieuShifts);

        column.add(lblHeader, BorderLayout.NORTH);
        column.add(bodyPanel, BorderLayout.CENTER);

        return column;
    }
    
    private void addShiftsToBody(JPanel body, String title, List<LichLam> shifts) {
        JLabel lblTitle = new JLabel("— " + title + " —");
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTitle.setForeground(Color.GRAY);
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);
        body.add(lblTitle);
        body.add(Box.createRigidArea(new Dimension(0, 5)));

        for (LichLam s : shifts) {
            body.add(createCompactCard(s));
            body.add(Box.createRigidArea(new Dimension(0, 5)));
        }
    }

    private JPanel createCompactCard(LichLam schedule) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);

        Color statusColor;
        if ("Cancelled".equalsIgnoreCase(schedule.getTrangThai())) {
            statusColor = new Color(220, 53, 69);
        } else if ("Done".equalsIgnoreCase(schedule.getTrangThai())) {
            statusColor = new Color(40, 167, 69);
        } else {
            statusColor = new Color(0, 123, 255);
        }

        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, statusColor),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 5, 5, 2)
            )
        ));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel timeLbl = new JLabel(
            schedule.getGioBatDau().toString().substring(0, 5) + " - " +
            schedule.getGioKetThuc().toString().substring(0, 5)
        );
        timeLbl.setFont(new Font("Arial", Font.BOLD, 13));

        String name = schedule.getEmailThuThu();
        if (name.contains("@")) name = name.substring(0, name.indexOf("@"));
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Arial", Font.PLAIN, 12));

        infoPanel.add(timeLbl);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        infoPanel.add(nameLbl);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        JButton btnDel = new JButton("×");
        btnDel.setFont(new Font("Arial", Font.BOLD, 16));
        btnDel.setForeground(Color.RED);
        btnDel.setBorderPainted(false);
        btnDel.setContentAreaFilled(false);
        btnDel.setFocusPainted(false);
        btnDel.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnDel.setPreferredSize(new Dimension(20, 20));
        btnDel.addActionListener(e -> handleDeleteSchedule(schedule));

        btnPanel.add(btnDel);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.EAST);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    handleEditSchedule(schedule);
                }
            }
        });

        return card;
    }

    private void handleAddSchedule() {
        try {
            List<TaiKhoan> thuThus = taiKhoanController.getAllAccountsSimple(currentUser);
            if (thuThus != null) {
                thuThus = thuThus.stream()
                    .filter(tk -> "ThuThu".equalsIgnoreCase(tk.getRole()) || "Admin".equalsIgnoreCase(tk.getRole()))
                    .collect(Collectors.toList());
            }

            LichLamDialog dialog = new LichLamDialog(
                SwingUtilities.getWindowAncestor(this),
                currentUser,
                thuThus,
                null
            );
            dialog.setVisible(true);

            if (dialog.isSuccess()) {
                loadWeekSchedule();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleEditSchedule(LichLam schedule) {
        try {
            List<TaiKhoan> thuThus = taiKhoanController.getAllAccountsSimple(currentUser);
            if (thuThus != null) {
                thuThus = thuThus.stream()
                    .filter(tk -> "ThuThu".equalsIgnoreCase(tk.getRole()) || "Admin".equalsIgnoreCase(tk.getRole()))
                    .collect(Collectors.toList());
            }

            LichLamDialog dialog = new LichLamDialog(
                SwingUtilities.getWindowAncestor(this),
                currentUser,
                thuThus,
                schedule
            );
            dialog.setVisible(true);

            if (dialog.isSuccess()) {
                loadWeekSchedule();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteSchedule(LichLam schedule) {
        String action = "xóa";
        boolean isPast = false;
        
        // Check if schedule is in the past
        java.time.LocalDateTime endDateTime = java.time.LocalDateTime.of(
            schedule.getNgay().toLocalDate(),
            schedule.getGioKetThuc().toLocalTime()
        );
        
        if (endDateTime.isBefore(java.time.LocalDateTime.now())) {
            action = "hủy (do đã qua)";
            isPast = true;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn " + action + " lịch làm này?\n" +
            "NV: " + schedule.getEmailThuThu() + "\n" +
            "Ngày: " + schedule.getNgay() + " " + schedule.getGioBatDau() + "-" + schedule.getGioKetThuc(),
            "Xác nhận " + action,
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (isPast) {
                    lichLamController.cancelShift(currentUser, schedule.getIdLich());
                } else {
                    lichLamController.deleteSchedule(currentUser, schedule.getIdLich());
                }
                loadWeekSchedule();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Thao tác thất bại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
