package da.api.service;

import java.awt.TrayIcon;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import da.api.model.ApiKeyData;
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
     * 檢查即將到期的項目
     */
    public void checkExpiringItems() {
        List<ApiKeyData> allData = excelService.readAllData();
        int reminderDays = appSettings.getExpiryReminderDays();
        LocalDate today = LocalDate.now();

        List<ApiKeyData> expiringItems = allData.stream()
                .filter(data -> data.getExpiryDate() != null)
                .filter(data -> {
                    long daysUntilExpiry = ChronoUnit.DAYS.between(today, data.getExpiryDate());
                    return daysUntilExpiry >= 0 && daysUntilExpiry <= reminderDays;
                })
                .collect(Collectors.toList());

        if (!expiringItems.isEmpty()) {
            showExpiryReminder(expiringItems);
        }
    }

    /**
     * 顯示到期提醒
     */
    private void showExpiryReminder(List<ApiKeyData> expiringItems) {
        StringBuilder message = new StringBuilder();
        message.append("以下 API KEY 即將到期:\n\n");

        LocalDate today = LocalDate.now();
        for (ApiKeyData item : expiringItems) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(today, item.getExpiryDate());

            String displayInfo;
            if (item.getCloud() != null || item.getApid() != null) {
                displayInfo = (item.getCloud() == null ? "" : item.getCloud()) +
                        (item.getApid() == null ? "" : " - " + item.getApid());
            } else {
                // Use first attribute found or something generic
                displayInfo = item.getAttribute("APID"); // Try to find APID by name
                if (displayInfo == null) {
                    // Just grab the first few values from attributes map
                    displayInfo = item.getExpiryDate().toString();
                    // This is weak. Let's try to get ANY value.
                    // But ApiKeyData doesn't expose keys easily without headers.
                    // But we have attributes map.
                }
            }

            // Allow printing attributes directly if legacy fields are empty
            if (item.getCloud() == null && item.getApid() == null) {
                // Try to construct a meaningful label.
                // Since we don't have the config here easily, we rely on what's in attributes.
                // Let's just use "Item expiring on [Date]"
                displayInfo = "項目";
            }

            message.append(String.format("• %s (%d 天後到期)\n",
                    displayInfo,
                    daysUntilExpiry));
        }

        // 顯示對話框
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null,
                    message.toString(),
                    "到期提醒",
                    JOptionPane.WARNING_MESSAGE);
        });

        // 如果有系統托盤圖示,也顯示托盤通知
        if (trayIcon != null) {
            trayIcon.displayMessage(
                    "API KEY 到期提醒",
                    String.format("有 %d 個 API KEY 即將到期", expiringItems.size()),
                    TrayIcon.MessageType.WARNING);
        }
    }

    /**
     * 啟動定期檢查 (每小時檢查一次)
     */
    public void startPeriodicCheck() {
        Timer timer = new Timer(3600000, e -> checkExpiringItems()); // 每小時
        timer.start();

        // 程式啟動時立即檢查一次
        checkExpiringItems();
    }
}
