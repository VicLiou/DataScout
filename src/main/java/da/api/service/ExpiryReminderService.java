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
        da.api.model.ColumnConfig config = excelService.getColumnConfig();

        for (ApiKeyData item : expiringItems) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(today, item.getExpiryDate());
            String displayInfo = null;

            if (config != null && config.getAllHeaders() != null) {
                // Try to build a label from Search Filter Columns or just the first few columns
                List<String> labelCols = config.getSearchFilterColumns();
                if (labelCols == null || labelCols.isEmpty()) {
                    // Fallback to first column that is NOT expiry date
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
                    // Use configured search filters as label
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
                // Legacy fallback
                if (item.getCloud() != null || item.getApid() != null) {
                    displayInfo = (item.getCloud() == null ? "" : item.getCloud()) +
                            (item.getApid() == null ? "" : " - " + item.getApid());
                }
            }

            if (displayInfo == null || displayInfo.isEmpty()) {
                displayInfo = "未命名項目";
            }

            // Truncate if too long
            if (displayInfo.length() > 50) {
                displayInfo = displayInfo.substring(0, 47) + "...";
            }

            message.append(String.format("• %s\n  到期日: %s (%d 天後)\n",
                    displayInfo,
                    item.getExpiryDate().toString(),
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
