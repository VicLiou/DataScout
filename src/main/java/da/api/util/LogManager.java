package da.api.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 日誌管理器 - 用於記錄和管理應用程式的執行日誌
 */
public class LogManager {
    private static LogManager instance;
    private List<LogEntry> logs;
    private java.util.concurrent.CopyOnWriteArrayList<LogListener> listeners;
    private static final int MAX_LOGS = 1000; // 最多保存 1000 條日誌
    private java.util.logging.Logger fileLogger;

    private LogManager() {
        logs = new ArrayList<>();
        listeners = new java.util.concurrent.CopyOnWriteArrayList<>();
        setupFileLogger();
    }

    private void setupFileLogger() {
        try {
            fileLogger = java.util.logging.Logger.getLogger("DataScoutLogger");
            fileLogger.setUseParentHandlers(false); // Disable default console handler to avoid duplicates

            // Create FileHandler
            // Get path from AppSettings helper
            String logPath = AppSettings.getStoragePath("application.log");
            java.util.logging.FileHandler fileHandler = new java.util.logging.FileHandler(logPath, true);
            fileHandler.setFormatter(new java.util.logging.SimpleFormatter() {
                private static final String format = "[%1$tF %1$tT] [%2$s] %3$s%n";

                @Override
                public synchronized String format(java.util.logging.LogRecord lr) {
                    return String.format(format,
                            new java.util.Date(lr.getMillis()),
                            lr.getLevel().getLocalizedName(),
                            lr.getMessage());
                }
            });
            fileLogger.addHandler(fileHandler);

        } catch (Exception e) {
            System.err.println("Failed to setup file logger: " + e.getMessage());
        }
    }

    public static synchronized LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }

    /**
     * 記錄一般訊息
     */
    public void info(String message) {
        addLog(LogLevel.INFO, message);
        if (fileLogger != null)
            fileLogger.info(message);
    }

    /**
     * 記錄警告訊息
     */
    public void warn(String message) {
        addLog(LogLevel.WARN, message);
        if (fileLogger != null)
            fileLogger.warning(message);
    }

    /**
     * 記錄錯誤訊息
     */
    public void error(String message) {
        addLog(LogLevel.ERROR, message);
        if (fileLogger != null)
            fileLogger.severe(message);
    }

    /**
     * 記錄錯誤訊息（含例外）
     */
    public void error(String message, Exception e) {
        String fullMessage = message + ": " + e.getMessage();
        addLog(LogLevel.ERROR, fullMessage);
        if (fileLogger != null)
            fileLogger.log(java.util.logging.Level.SEVERE, fullMessage, e);
    }

    /**
     * 新增日誌
     */
    private void addLog(LogLevel level, String message) {
        LogEntry entry = new LogEntry(level, message);

        synchronized (logs) {
            logs.add(entry);
            // 限制日誌數量
            if (logs.size() > MAX_LOGS) {
                logs.remove(0);
            }
        }

        // 同時輸出到控制台
        System.out.println(entry.toString());

        // 通知所有監聽器
        notifyListeners(entry);
    }

    /**
     * 取得所有日誌
     */
    public List<LogEntry> getAllLogs() {
        synchronized (logs) {
            return new ArrayList<>(logs);
        }
    }

    /**
     * 清除所有日誌
     */
    public void clearLogs() {
        synchronized (logs) {
            logs.clear();
        }
        notifyListeners(null);
    }

    /**
     * 新增日誌監聽器
     */
    public void addListener(LogListener listener) {
        listeners.add(listener);
    }

    /**
     * 移除日誌監聽器
     */
    public void removeListener(LogListener listener) {
        listeners.remove(listener);
    }

    /**
     * 通知所有監聽器
     */
    private void notifyListeners(LogEntry entry) {
        for (LogListener listener : listeners) {
            listener.onLogAdded(entry);
        }
    }

    /**
     * 日誌級別
     */
    public enum LogLevel {
        INFO("資訊"),
        WARN("警告"),
        ERROR("錯誤");

        private String displayName;

        LogLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * 日誌條目
     */
    public static class LogEntry {
        private LocalDateTime timestamp;
        private LogLevel level;
        private String message;

        public LogEntry(LogLevel level, String message) {
            this.timestamp = LocalDateTime.now();
            this.level = level;
            this.message = message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public LogLevel getLevel() {
            return level;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return String.format("[%s] [%s] %s",
                    timestamp.format(formatter),
                    level.getDisplayName(),
                    message);
        }
    }

    /**
     * 日誌監聽器接口
     */
    public interface LogListener {
        void onLogAdded(LogEntry entry);
    }
}
