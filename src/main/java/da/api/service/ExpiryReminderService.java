package da.api.service;

import java.awt.TrayIcon;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import da.api.model.ExcelData;
import da.api.util.AppSettings;

/**
 * 到期提醒服務
 */
public class ExpiryReminderService {
    private ExcelService excelService;
    private AppSettings appSettings;
    private TrayIcon trayIcon;

    public ExpiryReminderService(ExcelService excelService, AppSettings appSettings) {
        this.excelService = excelService;
        this.appSettings = appSettings;
    }

    public void setTrayIcon(TrayIcon trayIcon) {
        this.trayIcon = trayIcon;
    }

    /**
     * 檢查即將到期和已過期的項目
     */
    public void checkExpiringItems() {
        LocalDate today = LocalDate.now();

        // 檢查是否已設定今日不再提示
        String snoozeDate = appSettings.getReminderSnoozeDate(excelService.getFilePath());
        if (snoozeDate != null && snoozeDate.equals(today.toString())) {
            da.api.util.LogManager.getInstance().info("到期提醒已設定今日暫停，略過檢查");
            return;
        }

        List<ExcelData> allData = excelService.readAllData();
        int reminderDays = appSettings.getExpiryReminderDays(excelService.getFilePath());

        List<ExcelData> expiringItems = allData.stream()
                .filter(data -> data.getExpiryDate() != null)
                .filter(data -> {
                    long daysUntilExpiry = ChronoUnit.DAYS.between(today, data.getExpiryDate());
                    // 包含已過期的和即將到期的
                    return daysUntilExpiry <= reminderDays;
                })
                .collect(Collectors.toList());

        if (!expiringItems.isEmpty()) {
            showExpiryReminder(expiringItems);
        }
    }

    /**
     * 顯示到期提醒
     */
    private void showExpiryReminder(List<ExcelData> expiringItems) {
        LocalDate today = LocalDate.now();
        da.api.model.ColumnConfig config = excelService.getColumnConfig();

        // 分類資料：已過期、今天到期、即將到期
        List<ExcelData> expired = new ArrayList<>();
        List<ExcelData> expiringToday = new ArrayList<>();
        List<ExcelData> upcoming = new ArrayList<>();

        for (ExcelData item : expiringItems) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(today, item.getExpiryDate());
            if (daysUntilExpiry < 0) {
                expired.add(item);
            } else if (daysUntilExpiry == 0) {
                expiringToday.add(item);
            } else {
                upcoming.add(item);
            }
        }

        // 對已過期的資料按過期天數排序（最久的排最前面）
        expired.sort((a, b) -> a.getExpiryDate().compareTo(b.getExpiryDate()));
        // 對即將到期的資料按剩餘天數排序（最快到期的排最前面）
        upcoming.sort((a, b) -> a.getExpiryDate().compareTo(b.getExpiryDate()));

        StringBuilder message = new StringBuilder();

        int totalCount = expired.size() + expiringToday.size() + upcoming.size();
        message.append(String.format("共有 %d 筆資料需要關注\n\n", totalCount));

        // 顯示已過期的資料
        if (!expired.isEmpty()) {
            message.append("【已過期 - 緊急處理】\n");
            message.append("───────────────────────────────────\n");
            for (ExcelData item : expired) {
                appendItemInfo(message, item, config, today);
            }
            message.append("\n");
        }

        // 顯示今天到期的資料
        if (!expiringToday.isEmpty()) {
            message.append("【今天到期 - 立即處理】\n");
            message.append("───────────────────────────────────\n");
            for (ExcelData item : expiringToday) {
                appendItemInfo(message, item, config, today);
            }
            message.append("\n");
        }

        // 顯示即將到期的資料
        if (!upcoming.isEmpty()) {
            message.append("【即將到期 - 請注意】\n");
            message.append("───────────────────────────────────\n");
            for (ExcelData item : upcoming) {
                appendItemInfo(message, item, config, today);
            }
        }

        // 記錄提醒事件
        da.api.util.LogManager.getInstance().info(
                String.format("系統彈出到期提醒：共 %d 筆資料（已過期 %d 筆，今日到期 %d 筆，即將到期 %d 筆）",
                        totalCount, expired.size(), expiringToday.size(), upcoming.size()));

        // 顯示對話框
        SwingUtilities.invokeLater(() -> {
            // 創建美化的自訂對話框
            javax.swing.JDialog dialog = new javax.swing.JDialog((java.awt.Frame) null, "到期提醒", true);
            dialog.setSize(650, 550);
            dialog.setLocationRelativeTo(null);
            dialog.setResizable(true);

            // 設定視窗圖示
            try {
                java.net.URL iconUrl = getClass().getResource("/app_icon.png");
                if (iconUrl != null) {
                    dialog.setIconImage(new javax.swing.ImageIcon(iconUrl).getImage());
                }
            } catch (Exception e) {
                // 忽略
            }

            // 主容器
            javax.swing.JPanel mainContainer = new javax.swing.JPanel(new java.awt.BorderLayout(0, 20));
            mainContainer.setBackground(new java.awt.Color(248, 249, 250));
            mainContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25));

            // 標題區域
            javax.swing.JPanel headerPanel = new javax.swing.JPanel(
                    new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 10));
            headerPanel.setBackground(new java.awt.Color(254, 243, 199));
            headerPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new java.awt.Color(251, 191, 36), 1, true),
                    javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)));

            javax.swing.JLabel titleLabel = new javax.swing.JLabel("到期資料提醒");
            titleLabel.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 16));
            titleLabel.setForeground(new java.awt.Color(146, 64, 14));
            headerPanel.add(titleLabel);

            // 內容區域
            javax.swing.JTextArea textArea = new javax.swing.JTextArea(message.toString());
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.PLAIN, 13));
            textArea.setBackground(java.awt.Color.WHITE);
            textArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

            javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(textArea);
            scrollPane
                    .setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(229, 231, 235), 1, true));
            scrollPane.setBackground(java.awt.Color.WHITE);

            // 按鈕區域
            javax.swing.JPanel buttonPanel = new javax.swing.JPanel(
                    new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 15));
            buttonPanel.setBackground(new java.awt.Color(248, 249, 250));

            final boolean[] snoozeToday = { false };

            // 確定按鈕
            javax.swing.JButton okButton = new javax.swing.JButton("確定");
            okButton.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 14));
            okButton.setForeground(java.awt.Color.WHITE);
            okButton.setBackground(new java.awt.Color(37, 99, 235));
            okButton.setPreferredSize(new java.awt.Dimension(140, 42));
            okButton.setFocusPainted(false);
            okButton.setBorderPainted(false);
            okButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            okButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    okButton.setBackground(new java.awt.Color(29, 78, 216));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    okButton.setBackground(new java.awt.Color(37, 99, 235));
                }
            });

            okButton.addActionListener(e -> dialog.dispose());

            // 今日不再提示按鈕
            javax.swing.JButton snoozeButton = new javax.swing.JButton("今日不再提示");
            snoozeButton.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 14));
            snoozeButton.setForeground(new java.awt.Color(55, 65, 81));
            snoozeButton.setBackground(new java.awt.Color(229, 231, 235));
            snoozeButton.setPreferredSize(new java.awt.Dimension(140, 42));
            snoozeButton.setFocusPainted(false);
            snoozeButton.setBorderPainted(false);
            snoozeButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

            snoozeButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    snoozeButton.setBackground(new java.awt.Color(209, 213, 219));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    snoozeButton.setBackground(new java.awt.Color(229, 231, 235));
                }
            });

            snoozeButton.addActionListener(e -> {
                snoozeToday[0] = true;
                dialog.dispose();
            });

            buttonPanel.add(okButton);
            buttonPanel.add(snoozeButton);

            // 組合面板
            mainContainer.add(headerPanel, java.awt.BorderLayout.NORTH);
            mainContainer.add(scrollPane, java.awt.BorderLayout.CENTER);
            mainContainer.add(buttonPanel, java.awt.BorderLayout.SOUTH);

            dialog.add(mainContainer);
            dialog.setVisible(true);

            // 檢查使用者選擇
            if (snoozeToday[0]) {
                appSettings.setReminderSnoozeDate(excelService.getFilePath(), today.toString());
                da.api.util.LogManager.getInstance().info("使用者設定今日不再提示到期提醒");
            }
        });

        // 如果有系統托盤圖示,也顯示托盤通知
        if (trayIcon != null) {
            String notificationMsg;
            if (!expired.isEmpty()) {
                notificationMsg = String.format("有 %d 筆資料已過期，%d 筆即將到期",
                        expired.size(), expiringToday.size() + upcoming.size());
            } else {
                notificationMsg = String.format("有 %d 筆資料即將到期", expiringItems.size());
            }

            trayIcon.displayMessage(
                    "到期提醒",
                    notificationMsg,
                    TrayIcon.MessageType.WARNING);
        }
    }

    /**
     * 將單筆資料的資訊附加到訊息中
     */
    private void appendItemInfo(StringBuilder message, ExcelData item,
            da.api.model.ColumnConfig config, LocalDate today) {
        long daysUntilExpiry = ChronoUnit.DAYS.between(today, item.getExpiryDate());
        String displayInfo = getDisplayInfo(item, config);

        // 根據到期狀態顯示不同訊息
        if (daysUntilExpiry < 0) {
            message.append(String.format("  ■ %s\n", displayInfo));
            message.append(String.format("     到期日：%s  (已過期 %d 天)\n\n",
                    item.getExpiryDate().toString(), Math.abs(daysUntilExpiry)));
        } else if (daysUntilExpiry == 0) {
            message.append(String.format("  ■ %s\n", displayInfo));
            message.append(String.format("     到期日：%s  (今天到期！)\n\n",
                    item.getExpiryDate().toString()));
        } else {
            message.append(String.format("  • %s\n", displayInfo));
            message.append(String.format("     到期日：%s  (還有 %d 天)\n\n",
                    item.getExpiryDate().toString(), daysUntilExpiry));
        }
    }

    /**
     * 取得資料的顯示資訊
     */
    private String getDisplayInfo(ExcelData item, da.api.model.ColumnConfig config) {
        String displayInfo = null;

        if (config != null && config.getAllHeaders() != null) {
            // 嘗試從搜尋過濾欄位或僅前幾個欄位建立標籤
            List<String> labelCols = config.getSearchFilterColumns();
            if (labelCols == null || labelCols.isEmpty()) {
                // 回退到第一個非到期日的欄位
                String expiryCol = config.getExpiryDateColumn();
                for (String header : config.getAllHeaders()) {
                    if (!header.equals(expiryCol)) {
                        String val = item.getAttribute(header);
                        if (val != null && !val.isEmpty()) {
                            displayInfo = header + ": " + val;
                            break;
                        }
                    }
                }
            } else {
                // 使用設定的搜尋過濾器作為標籤
                StringBuilder sb = new StringBuilder();
                for (String col : labelCols) {
                    String val = item.getAttribute(col);
                    if (val != null && !val.isEmpty()) {
                        if (sb.length() > 0)
                            sb.append(", ");
                        sb.append(val);
                    }
                }
                if (sb.length() > 0)
                    displayInfo = sb.toString();
            }
        }

        if (displayInfo == null) {
            // 泛型回退方案：嘗試使用任何可用的屬性值
            for (String val : item.getAttributes().values()) {
                if (val != null && !val.trim().isEmpty()) {
                    displayInfo = val;
                    break;
                }
            }
        }

        if (displayInfo == null || displayInfo.isEmpty()) {
            displayInfo = "未命名項目";
        }

        // 若太長則截斷
        if (displayInfo.length() > 60) {
            displayInfo = displayInfo.substring(0, 57) + "...";
        }

        return displayInfo;
    }

    /**
     * 啟動定期檢查 (每小時檢查一次)
     */
    private Timer timer;

    /**
     * 啟動定期檢查 (每小時檢查一次)
     */
    public void startPeriodicCheck() {
        if (timer != null && timer.isRunning()) {
            return;
        }
        timer = new Timer(3600000, e -> checkExpiringItems()); // 每小時
        timer.start();

        // 程式啟動時立即檢查一次
        checkExpiringItems();
    }

    /**
     * 停止定期檢查
     */
    public void stopPeriodicCheck() {
        if (timer != null) {
            timer.stop();
            timer = null;
            da.api.util.LogManager.getInstance().info("到期提醒服務已停止");
        }
    }
}
