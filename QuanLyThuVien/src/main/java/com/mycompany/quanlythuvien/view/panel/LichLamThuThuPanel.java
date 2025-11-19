package com.mycompany.quanlythuvien.view.panel;

import com.mycompany.quanlythuvien.controller.LichLamController;
import com.mycompany.quanlythuvien.model.LichLam;
import com.mycompany.quanlythuvien.model.TaiKhoan;
import com.mycompany.quanlythuvien.view.dialog.ChiTietLichLamDialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Date;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * Panel lịch làm cá nhân của thủ thư
 * @author Tien
 */
public class LichLamThuThuPanel extends JPanel {

    private final TaiKhoan currentUser;
    private final LichLamController lichLamController;

    private LocalDate currentWeekMonday;
    private JLabel lblDateRange;
    private JPanel scheduleGridPanel;

    public LichLamThuThuPanel(TaiKhoan currentUser) {
        this.currentUser = currentUser;
        this.lichLamController = new LichLamController();
        this.currentWeekMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "LỊCH LÀM VIỆC CÁ NHÂN",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Arial", Font.BOLD, 20) 
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setBackground(Color.WHITE);

        // Header & Navigation
        JPanel navPanel = createNavigationPanel();

        // Main Grid
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

    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        navPanel.setBackground(Color.WHITE);

        Font navButtonFont = new Font("Arial", Font.PLAIN, 14);
        JButton btnPrev = new JButton("◄ Tuần trước");
        btnPrev.setFont(navButtonFont);
        JButton btnNext = new JButton("Tuần sau ►");
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

        navPanel.add(btnPrev);
        navPanel.add(lblDateRange);
        navPanel.add(btnNext);
        navPanel.add(btnToday);

        return navPanel;
    }

    public void loadWeekSchedule() {
        scheduleGridPanel.removeAll();

        LocalDate sunday = currentWeekMonday.plusDays(6);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblDateRange.setText(currentWeekMonday.format(fmt) + " - " + sunday.format(fmt));

        try {
            Date sqlFromDate = Date.valueOf(currentWeekMonday);
            Date sqlToDate = Date.valueOf(sunday);
            List<LichLam> weeklyShifts = lichLamController.getShiftsByEmailBetween(currentUser, currentUser.getEmail(), sqlFromDate, sqlToDate);
            
            if (weeklyShifts == null) {
                weeklyShifts = new ArrayList<>();
            }

            DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("dd/MM");
            String[] dayNames = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "CN"};

            for (int i = 0; i < 7; i++) {
                LocalDate dateAtCol = currentWeekMonday.plusDays(i);
                String headerText = dayNames[i] + " (" + dateAtCol.format(dayFmt) + ")";
                boolean isToday = dateAtCol.equals(LocalDate.now());

                List<LichLam> dailyShifts = new ArrayList<>();
                for (LichLam shift : weeklyShifts) {
                    if (shift.getNgay().toLocalDate().equals(dateAtCol)) {
                        dailyShifts.add(shift);
                    }
                }
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

        addShiftsToPanel(bodyPanel, "— SÁNG —", sangShifts);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        addShiftsToPanel(bodyPanel, "— CHIỀU/TỐI —", chieuShifts);

        column.add(lblHeader, BorderLayout.NORTH);
        column.add(bodyPanel, BorderLayout.CENTER);

        return column;
    }
    
    private void addShiftsToPanel(JPanel panel, String title, List<LichLam> shifts) {
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTitle.setForeground(Color.GRAY);
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(lblTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        if (shifts.isEmpty()) {
            JLabel noShiftLabel = new JLabel("Không có ca");
            noShiftLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            noShiftLabel.setForeground(Color.LIGHT_GRAY);
            noShiftLabel.setAlignmentX(CENTER_ALIGNMENT);
            panel.add(noShiftLabel);
        } else {
            for (LichLam s : shifts) {
                panel.add(createCompactCard(s));
                panel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
    }

    private JPanel createCompactCard(LichLam schedule) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        Color statusColor;
        if ("Cancelled".equalsIgnoreCase(schedule.getTrangThai())) {
            statusColor = new Color(220, 53, 69); // Red
        } else if ("Done".equalsIgnoreCase(schedule.getTrangThai())) {
            statusColor = new Color(40, 167, 69); // Green
        } else {
            statusColor = new Color(0, 123, 255); // Blue
        }

        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, statusColor),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
            )
        ));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel timeLbl = new JLabel(
            schedule.getGioBatDau().toString().substring(0, 5) + " - " +
            schedule.getGioKetThuc().toString().substring(0, 5)
        );
        timeLbl.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel statusLbl = new JLabel("Trạng thái: " + schedule.getTrangThai());
        statusLbl.setFont(new Font("Arial", Font.PLAIN, 12));

        infoPanel.add(timeLbl);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        infoPanel.add(statusLbl);

        card.add(infoPanel, BorderLayout.CENTER);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 1) {
                    handleViewScheduleDetail(schedule);
                }
            }
        });

        return card;
    }

    private void handleViewScheduleDetail(LichLam schedule) {
        ChiTietLichLamDialog dialog = new ChiTietLichLamDialog(
            javax.swing.SwingUtilities.getWindowAncestor(this),
            schedule
        );
        dialog.setVisible(true);
    }
}
