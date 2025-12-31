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
    private static final String SETTINGS_FILE = "app.settings";
    private Properties properties;

    public AppSettings() {
        properties = new Properties();
        loadSettings();
    }

    /**
     * 載入設定
     */
    private void loadSettings() {
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            // 設定預設值
            properties.setProperty("autoStart", "false");
            properties.setProperty("minimizeToTray", "true");
            properties.setProperty("expiryReminderDays", "7");
            saveSettings();
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 儲存設定
     */
    public void saveSettings() {
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(fos, "Application Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAutoStart() {
        return Boolean.parseBoolean(properties.getProperty("autoStart", "false"));
    }

    public void setAutoStart(boolean autoStart) {
        properties.setProperty("autoStart", String.valueOf(autoStart));
        saveSettings();
    }

    public boolean isMinimizeToTray() {
        return Boolean.parseBoolean(properties.getProperty("minimizeToTray", "true"));
    }

    public void setMinimizeToTray(boolean minimize) {
        properties.setProperty("minimizeToTray", String.valueOf(minimize));
        saveSettings();
    }

    public int getExpiryReminderDays() {
        return Integer.parseInt(properties.getProperty("expiryReminderDays", "7"));
    }

    public void setExpiryReminderDays(int days) {
        properties.setProperty("expiryReminderDays", String.valueOf(days));
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
        // Remove if exists to move to top
        files.remove(path);
        // Add to top
        files.add(0, path);
        // Keep only top 5
        if (files.size() > 5) {
            files = files.subList(0, 5);
        }

        properties.setProperty("recentFiles", String.join(";", files));
        saveSettings();
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
            // Note: allHeaders is transient/structural, not saved. It will be re-populated
            // by ExcelService or passed empty here.
            // Since ColumnConfig usually needs allHeaders to validate or display, but
            // strictly for prompt we might just load preferences.
            // Actually, we pass this config to ExcelService/Panel.
            // We should just fill what we have. ExcelService will read headers from file
            // anyway.
            return new da.api.model.ColumnConfig(expiryCol, filters, new java.util.ArrayList<>());
        }
        return null;
    }

    public void saveColumnConfig(String filePath, da.api.model.ColumnConfig config) {
        String keyHash = String.valueOf(filePath.hashCode());
        properties.setProperty("config." + keyHash + ".expiry", config.getExpiryDateColumn());
        properties.setProperty("config." + keyHash + ".filters", String.join(";", config.getSearchFilterColumns()));
        saveSettings();
    }
}
