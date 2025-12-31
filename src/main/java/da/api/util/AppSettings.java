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
}
