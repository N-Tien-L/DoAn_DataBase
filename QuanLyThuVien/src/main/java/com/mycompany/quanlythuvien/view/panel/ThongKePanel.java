package com.mycompany.quanlythuvien.view.panel;

import com.mycompany.quanlythuvien.controller.ThongKeController;
import com.mycompany.quanlythuvien.util.IconLoader;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

/**
 * Dashboard Thống Kê Quản Lý Thư Viện
 *
 * @author Tien
 */
public class ThongKePanel extends JPanel {

    private final ThongKeController controller = new ThongKeController();

    // ============= COLORS =============
    private final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private final Color WARNING_COLOR = new Color(241, 196, 15);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color INFO_COLOR = new Color(149, 165, 166);
    private final Color BG_COLOR = new Color(236, 240, 241);
    private final Color CARD_COLOR = Color.WHITE;

    // ============= FONTS =============
    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    private final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 12);
    private final Font FONT_VALUE = new Font("Segoe UI", Font.BOLD, 28);
    private final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 10);

    public ThongKePanel() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_LABEL);
        tabbedPane.setBackground(CARD_COLOR);
        tabbedPane.setForeground(Color.BLACK);

        tabbedPane.addTab("Tổng Quan", IconLoader.loadIconScaled(IconLoader.ICON_OVERVIEW, 16, 16),
                createOverviewTab());
        tabbedPane.addTab("Bạn Đọc", IconLoader.loadIconScaled(IconLoader.ICON_READERS, 16, 16), createBanDocTab());
        tabbedPane.addTab("Tài Khoản", IconLoader.loadIconScaled(IconLoader.ICON_ACCOUNT, 16, 16), createTaiKhoanTab());
        tabbedPane.addTab("Sách", IconLoader.loadIconScaled(IconLoader.ICON_BOOK, 16, 16), createSachTab());
        tabbedPane.addTab("Mượn - Trả", IconLoader.loadIconScaled(IconLoader.ICON_BORROW, 16, 16), createMuonTraTab());
        tabbedPane.addTab("Vi Phạm", IconLoader.loadIconScaled(IconLoader.ICON_VIOLATION, 16, 16), createViPhamTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ============= TAB TỔNG QUAN =============
    private JPanel createOverviewTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(createOverviewContent());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOverviewContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_COLOR);

        try {
            int totalBanDoc = controller.getTotalBanDoc();
            int banDocCoMuon = controller.getTotalBanDocCouMuon();
            int totalTaiKhoan = controller.getTotalTaiKhoanAdmin() + controller.getTotalTaiKhoanThuThu();
            int totalBanSao = controller.getTotalBanSao();
            int banSaoDangMuon = controller.getTotalBanSaoDangMuon();
            BigDecimal tienPhatChuaDong = controller.getTotalTienPhatChuaDong();
            int phieuTreHan = controller.getTotalPhieuTreHan();

            // KPI Row 1
            JPanel kpiRow1 = createKPIRow(
                    createKPICard("Tổng Bạn Đọc", String.valueOf(totalBanDoc), "Số bạn đọc đã đăng ký",
                            PRIMARY_COLOR, IconLoader.ICON_READERS),
                    createKPICard("Bạn Đọc Có Mượn", String.valueOf(banDocCoMuon), "Bạn đọc đã mượn sách",
                            SUCCESS_COLOR, IconLoader.ICON_ACTIVE_USER),
                    createKPICard("Tài Khoản", String.valueOf(totalTaiKhoan), "Admin + Thủ thư", INFO_COLOR,
                            IconLoader.ICON_ACCOUNT));
            panel.add(kpiRow1);
            panel.add(Box.createVerticalStrut(15));

            // KPI Row 2
            JPanel kpiRow2 = createKPIRow(
                    createKPICard("Tổng Bản Sao", String.valueOf(totalBanSao), "Số lượng sách trong kho",
                            PRIMARY_COLOR, IconLoader.ICON_COPY),
                    createKPICard("Đang Mượn", String.valueOf(banSaoDangMuon), "Bản sao hiện đang mượn",
                            WARNING_COLOR, IconLoader.ICON_BORROW),
                    createKPICard("Trễ Hẹn", String.valueOf(phieuTreHan), "Phiếu mượn đang trễ hạn", DANGER_COLOR,
                            IconLoader.ICON_OVERDUE));
            panel.add(kpiRow2);
            panel.add(Box.createVerticalStrut(15));

            // KPI Row 3
            JPanel kpiRow3 = createKPIRow(
                    createKPICard("Phạt Chưa Đóng", formatCurrency(tienPhatChuaDong),
                            "Tổng tiền phạt chưa thanh toán", DANGER_COLOR, IconLoader.ICON_UNPAID),
                    createKPICard("Phạt Đã Đóng", formatCurrency(controller.getTotalTienPhatDaDong()),
                            "Tổng tiền phạt đã thanh toán", SUCCESS_COLOR, IconLoader.ICON_PAID),
                    createKPICard("Tổng Phạt", formatCurrency(controller.getTotalTienPhat()),
                            "Tổng tiền phạt toàn bộ", INFO_COLOR, IconLoader.ICON_MONEY));
            panel.add(kpiRow3);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return panel;
    }

    // ============= TAB BẠN ĐỌC =============
    private JPanel createBanDocTab() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 15));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        try {
            int total = controller.getTotalBanDoc();
            int daMuon = controller.getTotalBanDocCouMuon();
            int chuaMuon = total - daMuon;

            // KPI Panel
            JPanel kpiPanel = new JPanel();
            kpiPanel.setLayout(new BoxLayout(kpiPanel, BoxLayout.Y_AXIS));
            kpiPanel.setBackground(BG_COLOR);

            JPanel card1 = createKPICard("Tổng Bạn Đọc", String.valueOf(total), "Số lượng bạn đọc đã đăng ký",
                    PRIMARY_COLOR, IconLoader.ICON_READERS);
            JPanel card2 = createKPICard("Có Mượn Sách", String.valueOf(daMuon),
                    String.format("%.1f%% bạn đọc", (daMuon * 100.0 / total)), SUCCESS_COLOR, IconLoader.ICON_BORROW);
            JPanel card3 = createKPICard("Chưa Mượn", String.valueOf(chuaMuon),
                    String.format("%.1f%% bạn đọc", (chuaMuon * 100.0 / total)), WARNING_COLOR, IconLoader.ICON_READER);

            kpiPanel.add(card1);
            kpiPanel.add(Box.createVerticalStrut(10));
            kpiPanel.add(card2);
            kpiPanel.add(Box.createVerticalStrut(10));
            kpiPanel.add(card3);
            kpiPanel.add(Box.createVerticalGlue());

            panel.add(createCardWithChart("Bạn Đọc - Trạng Thái Mượn", createBanDocPieChart(daMuon, chuaMuon)));
            panel.add(kpiPanel);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return panel;
    }

    // ============= TAB TÀI KHOẢN =============
    private JPanel createTaiKhoanTab() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 15, 15));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        try {
            int admin = controller.getTotalTaiKhoanAdmin();
            int thuThu = controller.getTotalTaiKhoanThuThu();

            panel.add(createCardWithChart("Tài Khoản - Phân Loại", createTaiKhoanPieChart(admin, thuThu)));
            panel.add(createCardWithChart("Tài Khoản - Chi Tiết", createTaiKhoanBarChart(admin, thuThu)));

            JPanel kpiPanel1 = createKPICard("Admin", String.valueOf(admin), "Số tài khoản quản trị viên",
                    PRIMARY_COLOR, IconLoader.ICON_ADMIN);
            JPanel kpiPanel2 = createKPICard("Thủ Thư", String.valueOf(thuThu), "Số tài khoản nhân viên thư viện",
                    SUCCESS_COLOR, IconLoader.ICON_LIBRARIAN);

            panel.add(kpiPanel1);
            panel.add(kpiPanel2);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return panel;
    }

    // ============= TAB SÁCH =============
    private JPanel createSachTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTabbedPane subTabs = new JTabbedPane();
        subTabs.setFont(FONT_LABEL);
        subTabs.setBackground(CARD_COLOR);

        subTabs.addTab("Phổ Biến", createSachPhoBienTab());
        subTabs.addTab("Kho", createSachKhoTab());
        subTabs.addTab("Danh Mục", createSachDanhMucTab());

        panel.add(subTabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSachPhoBienTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        try {
            panel.add(createCardWithChart("Top 10 Sách Được Mượn Nhiều Nhất", createTop10SachChart()),
                    BorderLayout.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return panel;
    }

    private JPanel createSachKhoTab() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 15));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        try {
            int total = controller.getTotalBanSao();
            int dangMuon = controller.getTotalBanSaoDangMuon();
            int coSan = controller.getTotalBanSaoCoSan();

            // KPI Panel
            JPanel kpiPanel = new JPanel();
            kpiPanel.setLayout(new BoxLayout(kpiPanel, BoxLayout.Y_AXIS));
            kpiPanel.setBackground(BG_COLOR);

            JPanel card1 = createKPICard("Tổng Bản Sao", String.valueOf(total), "Tổng số sách trong kho",
                    PRIMARY_COLOR, IconLoader.ICON_COPY);
            JPanel card2 = createKPICard("Đang Mượn", String.valueOf(dangMuon),
                    String.format("%.1f%%", (dangMuon * 100.0 / total)), WARNING_COLOR, IconLoader.ICON_BORROW);
            JPanel card3 = createKPICard("Có Sẵn", String.valueOf(coSan),
                    String.format("%.1f%%", (coSan * 100.0 / total)), SUCCESS_COLOR, IconLoader.ICON_BOOK);

            kpiPanel.add(card1);
            kpiPanel.add(Box.createVerticalStrut(10));
            kpiPanel.add(card2);
            kpiPanel.add(Box.createVerticalStrut(10));
            kpiPanel.add(card3);
            kpiPanel.add(Box.createVerticalGlue());

            panel.add(createCardWithChart("Bản Sao - Tình Trạng", createSachTrangThaiChart(total, dangMuon, coSan)));
            panel.add(kpiPanel);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return panel;
    }

    private JPanel createSachDanhMucTab() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 15));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        try {
            panel.add(createCardWithChart("Mượn Theo Thể Loại", createSachTheLoaiChart()));
            panel.add(createCardWithChart("Mượn Theo Tác Giả", createSachTacGiaChart()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return panel;
    }

    // ============= TAB MƯỢN - TRẢ =============
    private JPanel createMuonTraTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTabbedPane subTabs = new JTabbedPane();
        subTabs.setFont(FONT_LABEL);
        subTabs.setBackground(CARD_COLOR);

        subTabs.addTab("Xu Hướng", createMuonTrendTab());
        subTabs.addTab("Vi Phạm", createMuonViolationTab());

        panel.add(subTabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMuonTrendTab() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 15, 15));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        try {
            panel.add(createCardWithChart("Mượn Theo Ngày (30 Ngày Gần Đây)", createMuonNgayChart()));
            panel.add(createCardWithChart("Mượn Theo Tháng (12 Tháng Gần Đây)", createMuonThangChart()));
            panel.add(createCardWithChart("Mượn Theo Năm", createMuonNamChart()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return panel;
    }

    private JPanel createMuonViolationTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        try {
            int phieuTreHan = controller.getTotalPhieuTreHan();
            int sachTreHan = controller.getTotalSachTreHan();

            JPanel kpiPanel = new JPanel(new GridLayout(1, 2, 15, 0));
            kpiPanel.setBackground(BG_COLOR);
            kpiPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

            JPanel card1 = createKPICard("Phiếu Trễ Hẹn", String.valueOf(phieuTreHan), "Phiếu mượn đang trễ hạn",
                    DANGER_COLOR, IconLoader.ICON_OVERDUE);
            JPanel card2 = createKPICard("Sách Trễ Hẹn", String.valueOf(sachTreHan), "Bản sao đang trễ hạn",
                    DANGER_COLOR, IconLoader.ICON_VIOLATION);

            kpiPanel.add(card1);
            kpiPanel.add(card2);

            panel.add(kpiPanel, BorderLayout.NORTH);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return panel;
    }

    // ============= TAB VI PHẠM =============
    private JPanel createViPhamTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        try {
            panel.add(createCardWithChart("Loại Vi Phạm Phổ Biến", createLoaiViPhamChart()),
                    BorderLayout.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return panel;
    }

    // ============= CHART CREATION METHODS =============

    private JFreeChart createBanDocPieChart(int daMuon, int chuaMuon) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Có Mượn", daMuon);
        dataset.setValue("Chưa Mượn", chuaMuon);

        JFreeChart chart = ChartFactory.createPieChart("", dataset, true, true, false);
        customizePieChart(chart);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Có Mượn", SUCCESS_COLOR);
        plot.setSectionPaint("Chưa Mượn", WARNING_COLOR);

        // Custom tooltip
        plot.setToolTipGenerator(
                (dataset1, key) -> String.format("<html><b>%s</b><br>Số lượng: %d<br>Tỷ lệ: %.1f%%</html>",
                        key,
                        dataset1.getValue(key).intValue(),
                        (dataset1.getValue(key).doubleValue() / (daMuon + chuaMuon)) * 100));

        return chart;
    }

    private JFreeChart createTaiKhoanPieChart(int admin, int thuThu) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Admin", admin);
        dataset.setValue("Thủ Thư", thuThu);

        JFreeChart chart = ChartFactory.createPieChart("", dataset, true, true, false);
        customizePieChart(chart);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Admin", PRIMARY_COLOR);
        plot.setSectionPaint("Thủ Thư", SUCCESS_COLOR);

        // Custom tooltip
        plot.setToolTipGenerator(
                (dataset1, key) -> String.format("<html><b>%s</b><br>Số lượng: %d<br>Tỷ lệ: %.1f%%</html>",
                        key,
                        dataset1.getValue(key).intValue(),
                        (dataset1.getValue(key).doubleValue() / (admin + thuThu)) * 100));

        return chart;
    }

    private JFreeChart createTaiKhoanBarChart(int admin, int thuThu) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(admin, "Admin", "Tài Khoản");
        dataset.addValue(thuThu, "Thủ Thư", "Tài Khoản");

        JFreeChart chart = ChartFactory.createBarChart("", "Loại Tài Khoản", "Số Lượng", dataset,
                PlotOrientation.VERTICAL, true, true, false);
        customizeBarChart(chart);

        BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
        renderer.setSeriesPaint(0, PRIMARY_COLOR);
        renderer.setSeriesPaint(1, SUCCESS_COLOR);
        renderer.setDefaultToolTipGenerator(new CustomCategoryToolTipGenerator("Loại Tài Khoản", "Số Lượng"));

        return chart;
    }

    private JFreeChart createSachTrangThaiChart(int total, int dangMuon, int coSan) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Đang Mượn", dangMuon);
        dataset.setValue("Có Sẵn", coSan);

        JFreeChart chart = ChartFactory.createPieChart("", dataset, true, true, false);
        customizePieChart(chart);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Đang Mượn", WARNING_COLOR);
        plot.setSectionPaint("Có Sẵn", SUCCESS_COLOR);

        return chart;
    }

    private JFreeChart createTop10SachChart() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<Object[]> data = controller.getTop10SachPhoBien();

        for (int i = 0; i < Math.min(10, data.size()); i++) {
            Object[] row = data.get(i);
            String tenSach = (String) row[0];
            int soLan = (int) row[1];
            dataset.addValue(soLan, "Lần Mượn", truncateText(tenSach, 30));
        }

        JFreeChart chart = ChartFactory.createBarChart("", "Tên Sách", "Lần Mượn", dataset,
                PlotOrientation.HORIZONTAL, false, true, false);
        customizeBarChart(chart);

        BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
        renderer.setSeriesPaint(0, PRIMARY_COLOR);
        renderer.setDefaultToolTipGenerator(new CustomCategoryToolTipGenerator("Tên Sách", "Lần Mượn"));

        return chart;
    }

    private JFreeChart createSachTheLoaiChart() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<Object[]> data = controller.getSoLuotMuonTheoTheLoai();

        for (Object[] row : data) {
            String theLoai = (String) row[0];
            int soLuot = (int) row[1];
            dataset.addValue(soLuot, "Lần Mượn", truncateText(theLoai, 20));
        }

        JFreeChart chart = ChartFactory.createBarChart("", "Thể Loại", "Lần Mượn", dataset,
                PlotOrientation.VERTICAL, false, true, false);
        customizeBarChart(chart);

        BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
        renderer.setSeriesPaint(0, SUCCESS_COLOR);
        renderer.setDefaultToolTipGenerator(new CustomCategoryToolTipGenerator("Thể Loại", "Lần Mượn"));

        return chart;
    }

    private JFreeChart createSachTacGiaChart() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<Object[]> data = controller.getSoLuotMuonTheoTacGia();

        for (int i = 0; i < Math.min(10, data.size()); i++) {
            Object[] row = data.get(i);
            String tacGia = (String) row[0];
            int soLuot = (int) row[1];
            dataset.addValue(soLuot, "Lần Mượn", truncateText(tacGia, 20));
        }

        JFreeChart chart = ChartFactory.createBarChart("", "Tác Giả", "Lần Mượn", dataset,
                PlotOrientation.VERTICAL, false, true, false);
        customizeBarChart(chart);

        BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
        renderer.setSeriesPaint(0, INFO_COLOR);
        renderer.setDefaultToolTipGenerator(new CustomCategoryToolTipGenerator("Tác Giả", "Lần Mượn"));

        return chart;
    }

    private JFreeChart createMuonNgayChart() throws Exception {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("Lần Mượn");

        List<Object[]> data = controller.getSoLuotMuonTheoNgay();
        for (Object[] row : data) {
            Date ngay = (Date) row[0];
            int soLuot = (int) row[1];
            series.add(ngay.getTime(), soLuot);
        }

        dataset.addSeries(series);
        JFreeChart chart = ChartFactory.createXYLineChart("", "Ngày", "Lần Mượn", dataset,
                PlotOrientation.VERTICAL, false, true, false);
        customizeLineChart(chart);

        return chart;
    }

    private JFreeChart createMuonThangChart() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<Object[]> data = controller.getSoLuotMuonTheoThang();

        for (Object[] row : data) {
            int nam = (int) row[0];
            int thang = (int) row[1];
            int soLuot = (int) row[2];
            String label = String.format("T%d/%d", thang, nam);
            dataset.addValue(soLuot, "Lần Mượn", label);
        }

        JFreeChart chart = ChartFactory.createLineChart("", "Tháng/Năm", "Lần Mượn", dataset,
                PlotOrientation.VERTICAL, false, true, false);
        customizeBarChart(chart);

        return chart;
    }

    private JFreeChart createMuonNamChart() throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<Object[]> data = controller.getSoLuotMuonTheoNam();

        for (Object[] row : data) {
            int nam = (int) row[0];
            int soLuot = (int) row[1];
            dataset.addValue(soLuot, "Lần Mượn", String.valueOf(nam));
        }

        JFreeChart chart = ChartFactory.createLineChart("", "Năm", "Lần Mượn", dataset,
                PlotOrientation.VERTICAL, false, true, false);
        customizeBarChart(chart);

        return chart;
    }

    private JFreeChart createLoaiViPhamChart() throws Exception {
        DefaultPieDataset dataset = new DefaultPieDataset();
        List<Object[]> data = controller.getSoLuongViPhamTheoLoai();

        for (Object[] row : data) {
            String loai = (String) row[0];
            int soLuong = (int) row[1];
            dataset.setValue(loai, soLuong);
        }

        JFreeChart chart = ChartFactory.createPieChart("", dataset, true, true, false);
        customizePieChart(chart);

        PiePlot plot = (PiePlot) chart.getPlot();
        Color[] colors = { DANGER_COLOR, WARNING_COLOR, PRIMARY_COLOR };
        for (int i = 0; i < data.size(); i++) {
            plot.setSectionPaint((String) data.get(i)[0], colors[i % colors.length]);
        }

        return chart;
    }

    // ============= HELPER METHODS =============

    private JPanel createKPIRow(JPanel... cards) {
        JPanel panel = new JPanel(new GridLayout(1, cards.length, 15, 0));
        panel.setBackground(BG_COLOR);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        for (JPanel card : cards) {
            panel.add(card);
        }

        return panel;
    }

    private JPanel createKPICard(String title, String value, String subtitle, Color color, String iconName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 3),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.setToolTipText("<html><b>" + title + "</b><br>" + subtitle + "<br><i>Giá trị: " + value + "</i></html>");

        // Mouse listener để highlight khi hover
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                panel.setBackground(new Color(245, 245, 245));
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(color, 4),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                panel.setBackground(CARD_COLOR);
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(color, 3),
                        BorderFactory.createEmptyBorder(15, 15, 15, 15)));
            }
        });

        // Icon + Title row
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        ImageIcon icon = IconLoader.loadIconScaled(iconName, 20, 20);
        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            headerPanel.add(iconLabel);
            headerPanel.add(Box.createHorizontalStrut(8));
        }

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_LABEL);
        titleLabel.setForeground(color);
        headerPanel.add(titleLabel);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FONT_VALUE);
        valueLabel.setForeground(Color.BLACK);
        valueLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(FONT_SMALL);
        subtitleLabel.setForeground(new Color(127, 140, 141));
        subtitleLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        panel.add(headerPanel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(valueLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitleLabel);

        return panel;
    }

    private JPanel createCardWithChart(String title, JFreeChart chart) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(CARD_COLOR);
        chartPanel.setDisplayToolTips(true);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setToolTipText("Hover để xem chi tiết");

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    private void customizePieChart(JFreeChart chart) {
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(CARD_COLOR);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})"));
    }

    private void customizeBarChart(JFreeChart chart) {
        chart.getPlot().setBackgroundPaint(CARD_COLOR);
        if (chart.getCategoryPlot() != null) {
            chart.getCategoryPlot().setDomainGridlinePaint(new Color(200, 200, 200));
            chart.getCategoryPlot().setRangeGridlinePaint(new Color(200, 200, 200));
        }
    }

    private void customizeLineChart(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(CARD_COLOR);
        plot.setDomainGridlinePaint(new Color(200, 200, 200));
        plot.setRangeGridlinePaint(new Color(200, 200, 200));

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, PRIMARY_COLOR);
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        renderer.setSeriesShape(0, new Ellipse2D.Double(-5, -5, 10, 10));
        plot.setRenderer(renderer);
    }

    private String truncateText(String text, int maxLength) {
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    private String formatCurrency(BigDecimal amount) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(amount) + " đ";
    }

    /**
     * Custom tooltip generator cho bar chart
     */
    private class CustomCategoryToolTipGenerator implements CategoryToolTipGenerator {
        private final String categoryLabel;
        private final String valueLabel;

        public CustomCategoryToolTipGenerator(String categoryLabel, String valueLabel) {
            this.categoryLabel = categoryLabel;
            this.valueLabel = valueLabel;
        }

        @Override
        public String generateToolTip(org.jfree.data.category.CategoryDataset dataset, int series, int category) {
            String categoryName = dataset.getColumnKey(category).toString();
            Number value = dataset.getValue(series, category);
            return "<html><b>" + categoryLabel + ":</b> " + categoryName + "<br/>" +
                    "<b>" + valueLabel + ":</b> " + value + "</html>";
        }
    }
}
