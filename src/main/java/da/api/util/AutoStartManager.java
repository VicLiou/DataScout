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
     * 設定開機自動啟動 (支援指定檔案)
     * 
     * @param enable   是否啟用
     * @param filePath 指定要開啟的檔案路徑 (可為 null，表示僅開啟應用程式)
     */
    public static boolean setAutoStart(boolean enable, String filePath) {
        try {
            File target = getTargetExecutable();
            String command;

            // 建立基本指令
            if (target.getName().toLowerCase().endsWith(".exe")) {
                command = "\"" + target.getAbsolutePath() + "\"";
            } else {
                command = "javaw -jar \"" + target.getAbsolutePath() + "\"";
            }

            // 如果有指定檔案，要在指令後加上路徑參數
            if (filePath != null && !filePath.trim().isEmpty()) {
                command += " \"" + filePath + "\"";
            }

            // 加上 autostart 參數，讓程式能識別是自動啟動
            command += " --autostart";

            // 取得對應的 Registry Key 名稱
            String keyName = getRegistryKeyName(filePath);

            if (enable) {
                // 新增到登錄檔
                Advapi32Util.registrySetStringValue(
                        WinReg.HKEY_CURRENT_USER,
                        REGISTRY_PATH,
                        keyName,
                        command);
            } else {
                // 從登錄檔移除
                if (Advapi32Util.registryValueExists(
                        WinReg.HKEY_CURRENT_USER,
                        REGISTRY_PATH,
                        keyName)) {
                    Advapi32Util.registryDeleteValue(
                            WinReg.HKEY_CURRENT_USER,
                            REGISTRY_PATH,
                            keyName);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return setAutoStartFallback(enable, filePath);
        }
    }

    /**
     * 保持向下相容的方法 (僅開啟應用程式)
     */
    public static boolean setAutoStart(boolean enable) {
        return setAutoStart(enable, null);
    }

    /**
     * 檢查是否已設定開機自動啟動 (支援指定檔案)
     */
    public static boolean isAutoStartEnabled(String filePath) {
        try {
            String keyName = getRegistryKeyName(filePath);
            return Advapi32Util.registryValueExists(
                    WinReg.HKEY_CURRENT_USER,
                    REGISTRY_PATH,
                    keyName);
        } catch (Exception e) {
            // 檢查備用方案
            return isAutoStartFallbackEnabled(filePath);
        }
    }

    /**
     * 保持向下相容的方法
     */
    public static boolean isAutoStartEnabled() {
        return isAutoStartEnabled(null);
    }

    /**
     * 產生 Registry Key 名稱
     * 如果 filePath 為 null，回傳 "DataScout"
     * 如果 filePath 有值，回傳 "DataScout_{HashCode}" 避免檔名衝突或非法字元
     */
    private static String getRegistryKeyName(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return APP_NAME;
        }
        // 使用路徑的 HashCode 產生唯一標識，確保鍵值合法且唯一
        return APP_NAME + "_" + String.format("%08X", filePath.hashCode());
    }

    /**
     * 備用方案: 使用啟動資料夾
     */
    private static boolean setAutoStartFallback(boolean enable, String filePath) {
        try {
            String startupFolder = System.getenv("APPDATA") +
                    "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";

            // 檔名也需要區分
            String shortcutName = getShortcutName(filePath);
            File shortcutFile = new File(startupFolder, shortcutName);

            if (enable) {
                File target = getTargetExecutable();
                String batContent;
                String args = "";

                if (filePath != null && !filePath.trim().isEmpty()) {
                    args = " \"" + filePath + "\"";
                }

                // 加上 autostart 參數
                args += " --autostart";

                if (target.getName().toLowerCase().endsWith(".exe")) {
                    batContent = "@echo off\r\n" +
                            "start \"\" \"" + target.getAbsolutePath() + "\"" + args;
                } else {
                    batContent = "@echo off\r\n" +
                            "start javaw -jar \"" + target.getAbsolutePath() + "\"" + args;
                }

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

    private static boolean isAutoStartFallbackEnabled(String filePath) {
        String startupFolder = System.getenv("APPDATA") +
                "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
        String shortcutName = getShortcutName(filePath);
        File shortcutFile = new File(startupFolder, shortcutName);
        return shortcutFile.exists();
    }

    private static String getShortcutName(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return APP_NAME + ".bat";
        }
        return APP_NAME + "_" + String.format("%08X", filePath.hashCode()) + ".bat";
    }

    /**
     * 取得目標執行檔 (優先尋找 .exe，否則回傳 .jar)
     */
    private static File getTargetExecutable() {
        try {
            File source = new File(AutoStartManager.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath());

            // 處理在 IDE 中運行的情況 (source 為目錄)
            if (source.isDirectory()) {
                File exe = new File(source.getParentFile(), APP_NAME + ".exe");
                if (exe.exists()) {
                    return exe;
                }
                // 如果找不到 exe，就回傳目錄本身 (雖然可能不適用於啟動)
                return source;
            }

            // 檢查是否有同名的 exe 檔案
            File parentDir = source.getParentFile();
            File exeFile = new File(parentDir, APP_NAME + ".exe");
            if (exeFile.exists()) {
                return exeFile;
            }

            return source;
        } catch (Exception e) {
            return new File(System.getProperty("user.dir"), APP_NAME + ".jar");
        }
    }
}
