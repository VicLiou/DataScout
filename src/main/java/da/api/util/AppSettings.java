package da.api.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 應用程式設定管理
 */
public class AppSettings {
    private static final String SETTINGS_FILENAME = "app.settings";
    private Properties properties;

    /**
     * 取得應用程式資料儲存目錄 (Windows 為 AppData/Roaming/DataScout)
     */
    public static String getStoragePath(String filename) {
        String appData = System.getenv("APPDATA");
        String folderPath;
        if (appData != null) {
            folderPath = appData + File.separator + "DataScout";
        } else {
            // 非 Windows 系統使用家目錄
            folderPath = System.getProperty("user.home") + File.separator + ".datascout";
        }

        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folderPath + File.separator + filename;
    }

    private String getSettingsPath() {
        return getStoragePath(SETTINGS_FILENAME);
    }

    public AppSettings() {
        properties = new Properties();
        loadSettings();
    }

    /**
     * 載入設定
     */
    private void loadSettings() {
        File file = new File(getSettingsPath());
        if (!file.exists()) {
            // 設定預設值（全域）
            properties.setProperty("global.autoStart", "false");
            properties.setProperty("global.minimizeToTray", "true");
            properties.setProperty("global.expiryReminderDays", "30");
            saveSettings();
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
            // 遷移舊格式設定到新格式
            migrateOldSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 遷移舊格式設定到新格式
     */
    private void migrateOldSettings() {
        boolean needsSave = false;

        // 遷移 autoStart
        if (properties.containsKey("autoStart") && !properties.containsKey("global.autoStart")) {
            properties.setProperty("global.autoStart", properties.getProperty("autoStart"));
            properties.remove("autoStart");
            needsSave = true;
        }

        // 遷移 minimizeToTray
        if (properties.containsKey("minimizeToTray") && !properties.containsKey("global.minimizeToTray")) {
            properties.setProperty("global.minimizeToTray", properties.getProperty("minimizeToTray"));
            properties.remove("minimizeToTray");
            needsSave = true;
        }

        // 遷移 expiryReminderDays
        if (properties.containsKey("expiryReminderDays") && !properties.containsKey("global.expiryReminderDays")) {
            properties.setProperty("global.expiryReminderDays", properties.getProperty("expiryReminderDays"));
            properties.remove("expiryReminderDays");
            needsSave = true;
        }

        if (needsSave) {
            saveSettings();
        }
    }

    /**
     * 儲存設定
     */
    public void saveSettings() {
        try (FileOutputStream fos = new FileOutputStream(getSettingsPath())) {
            properties.store(fos, "Application Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 取得全域自動啟動設定
     */
    public boolean isGlobalAutoStart() {
        return Boolean.parseBoolean(properties.getProperty("global.autoStart", "false"));
    }

    /**
     * 設定全域自動啟動
     */
    public void setGlobalAutoStart(boolean autoStart) {
        properties.setProperty("global.autoStart", String.valueOf(autoStart));
        saveSettings();
    }

    /**
     * 取得指定檔案的自動啟動設定（若未設定則使用全域設定）
     */
    public boolean isAutoStart(String filePath) {
        String keyHash = String.valueOf(filePath.hashCode());
        String value = properties.getProperty("config." + keyHash + ".autoStart");
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return isGlobalAutoStart();
    }

    /**
     * 設定指定檔案的自動啟動
     */
    public void setAutoStart(String filePath, boolean autoStart) {
        String keyHash = String.valueOf(filePath.hashCode());
        properties.setProperty("config." + keyHash + ".autoStart", String.valueOf(autoStart));
        saveSettings();
    }

    /**
     * 取得全域最小化到系統匣設定
     */
    public boolean isGlobalMinimizeToTray() {
        return Boolean.parseBoolean(properties.getProperty("global.minimizeToTray", "true"));
    }

    /**
     * 設定全域最小化到系統匣
     */
    public void setGlobalMinimizeToTray(boolean minimize) {
        properties.setProperty("global.minimizeToTray", String.valueOf(minimize));
        saveSettings();
    }

    /**
     * 取得指定檔案的最小化到系統匣設定（若未設定則使用全域設定）
     */
    public boolean isMinimizeToTray(String filePath) {
        String keyHash = String.valueOf(filePath.hashCode());
        String value = properties.getProperty("config." + keyHash + ".minimizeToTray");
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return isGlobalMinimizeToTray();
    }

    /**
     * 設定指定檔案的最小化到系統匣
     */
    public void setMinimizeToTray(String filePath, boolean minimize) {
        String keyHash = String.valueOf(filePath.hashCode());
        properties.setProperty("config." + keyHash + ".minimizeToTray", String.valueOf(minimize));
        saveSettings();
    }

    /**
     * 取得全域到期提醒天數設定
     */
    public int getGlobalExpiryReminderDays() {
        return Integer.parseInt(properties.getProperty("global.expiryReminderDays", "30"));
    }

    /**
     * 設定全域到期提醒天數
     */
    public void setGlobalExpiryReminderDays(int days) {
        properties.setProperty("global.expiryReminderDays", String.valueOf(days));
        saveSettings();
    }

    /**
     * 取得指定檔案的到期提醒天數（若未設定則使用全域設定）
     */
    public int getExpiryReminderDays(String filePath) {
        String keyHash = String.valueOf(filePath.hashCode());
        String value = properties.getProperty("config." + keyHash + ".expiryReminderDays");
        if (value != null) {
            return Integer.parseInt(value);
        }
        return getGlobalExpiryReminderDays();
    }

    /**
     * 設定指定檔案的到期提醒天數
     */
    public void setExpiryReminderDays(String filePath, int days) {
        String keyHash = String.valueOf(filePath.hashCode());
        properties.setProperty("config." + keyHash + ".expiryReminderDays", String.valueOf(days));
        saveSettings();
    }

    public java.util.List<String> getRecentFiles() {
        String recent = properties.getProperty("recentFiles", "");
        if (recent.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return new java.util.ArrayList<>(java.util.Arrays.asList(recent.split(";")));
    }

    public void addRecentFile(String path) {
        java.util.List<String> files = getRecentFiles();
        // 如果存在則移除，以便移至頂部
        files.remove(path);
        // 加入至頂部
        files.add(0, path);
        // 僅保留前 5 筆
        if (files.size() > 5) {
            files = files.subList(0, 5);
        }

        properties.setProperty("recentFiles", String.join(";", files));
        saveSettings();
    }

    public void removeRecentFile(String path) {
        java.util.List<String> files = getRecentFiles();
        if (files.remove(path)) {
            properties.setProperty("recentFiles", String.join(";", files));
            saveSettings();
        }
    }

    public da.api.model.ColumnConfig getColumnConfig(String filePath) {
        String keyHash = String.valueOf(filePath.hashCode());
        String expiryCol = properties.getProperty("config." + keyHash + ".expiry");
        String filterCols = properties.getProperty("config." + keyHash + ".filters");

        if (expiryCol != null && filterCols != null) {
            java.util.List<String> filters = new java.util.ArrayList<>();
            if (!filterCols.isEmpty()) {
                filters.addAll(java.util.Arrays.asList(filterCols.split(";")));
            }
            // 註: allHeaders 是暫時的/結構性的，不會被儲存。它將由 ExcelService 重新填入或在此傳遞空值。
            // 由於 ColumnConfig 通常需要 allHeaders 來驗證或顯示，但嚴格來說對於提示，我們可能只需要載入偏好設定。
            // 實際上，我們將此設定傳遞給 ExcelService/Panel。
            // 我們應該只填入我們擁有的。ExcelService 無論如何都會從檔案讀取標題。
            return new da.api.model.ColumnConfig(expiryCol, filters, new java.util.ArrayList<>());
        }
        return null;
    }

    public void saveColumnConfig(String filePath, da.api.model.ColumnConfig config) {
        String keyHash = String.valueOf(filePath.hashCode());

        String expiry = config.getExpiryDateColumn();
        properties.setProperty("config." + keyHash + ".expiry", expiry != null ? expiry : "");

        java.util.List<String> filters = config.getSearchFilterColumns();
        String filterStr = (filters != null) ? String.join(";", filters) : "";
        properties.setProperty("config." + keyHash + ".filters", filterStr);

        saveSettings();
    }

    /**
     * 移除指定檔案的所有設定
     */
    public void removeColumnConfig(String filePath) {
        String keyHash = String.valueOf(filePath.hashCode());
        properties.remove("config." + keyHash + ".expiry");
        properties.remove("config." + keyHash + ".filters");
        properties.remove("config." + keyHash + ".autoStart");
        properties.remove("config." + keyHash + ".minimizeToTray");
        properties.remove("config." + keyHash + ".expiryReminderDays");
        saveSettings();
    }

    /**
     * 取得指定檔案的提醒暫停日期（格式：yyyy-MM-dd）
     */
    public String getReminderSnoozeDate(String filePath) {
        String keyHash = String.valueOf(filePath.hashCode());
        return properties.getProperty("config." + keyHash + ".reminderSnoozeDate");
    }

    /**
     * 設定指定檔案的提醒暫停日期（格式：yyyy-MM-dd）
     */
    public void setReminderSnoozeDate(String filePath, String date) {
        String keyHash = String.valueOf(filePath.hashCode());
        properties.setProperty("config." + keyHash + ".reminderSnoozeDate", date);
        saveSettings();
    }
}
