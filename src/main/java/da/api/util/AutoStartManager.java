package da.api.util;

import java.io.File;
import java.io.FileWriter;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

/**
 * Windows 開機自動啟動管理
 * 注意: 需要在 pom.xml 中加入 JNA 依賴
 */
public class AutoStartManager {
    private static final String APP_NAME = "DataScout";
    private static final String REGISTRY_PATH = "Software\\Microsoft\\Windows\\CurrentVersion\\Run";

    /**
     * 設定開機自動啟動 (使用登錄檔方式)
     */
    public static boolean setAutoStart(boolean enable) {
        try {
            String jarPath = getJarPath();
            String command = "javaw -jar \"" + jarPath + "\"";

            if (enable) {
                // 新增到登錄檔
                Advapi32Util.registrySetStringValue(
                        WinReg.HKEY_CURRENT_USER,
                        REGISTRY_PATH,
                        APP_NAME,
                        command);
            } else {
                // 從登錄檔移除
                if (Advapi32Util.registryValueExists(
                        WinReg.HKEY_CURRENT_USER,
                        REGISTRY_PATH,
                        APP_NAME)) {
                    Advapi32Util.registryDeleteValue(
                            WinReg.HKEY_CURRENT_USER,
                            REGISTRY_PATH,
                            APP_NAME);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return setAutoStartFallback(enable);
        }
    }

    /**
     * 備用方案: 使用啟動資料夾
     */
    private static boolean setAutoStartFallback(boolean enable) {
        try {
            String startupFolder = System.getenv("APPDATA") +
                    "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
            File shortcutFile = new File(startupFolder, APP_NAME + ".bat");

            if (enable) {
                String jarPath = getJarPath();
                String batContent = "@echo off\r\n" +
                        "start javaw -jar \"" + jarPath + "\"";

                try (FileWriter writer = new FileWriter(shortcutFile)) {
                    writer.write(batContent);
                }
            } else {
                if (shortcutFile.exists()) {
                    shortcutFile.delete();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 檢查是否已設定開機自動啟動
     */
    public static boolean isAutoStartEnabled() {
        try {
            return Advapi32Util.registryValueExists(
                    WinReg.HKEY_CURRENT_USER,
                    REGISTRY_PATH,
                    APP_NAME);
        } catch (Exception e) {
            // 檢查備用方案
            String startupFolder = System.getenv("APPDATA") +
                    "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
            File shortcutFile = new File(startupFolder, APP_NAME + ".bat");
            return shortcutFile.exists();
        }
    }

    /**
     * 取得當前 JAR 檔案路徑
     */
    private static String getJarPath() {
        try {
            return new File(AutoStartManager.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath()).getAbsolutePath();
        } catch (Exception e) {
            return System.getProperty("user.dir") + "\\DataScout.jar";
        }
    }
}
