package da.api;

import javax.swing.SwingUtilities;

import da.api.service.ExcelService;
import da.api.service.ExpiryReminderService;
import da.api.service.UserService;
import da.api.util.AppSettings;
import da.api.util.LogManager;
import da.api.util.SystemTrayManager;
import da.api.view.MainFrameView;
import da.api.view.element.MenuBar;
import da.api.view.element.Panel;

/**
 * 服務名稱:API KEY 服務台應用程式
 * 
 * @author 柳宏達
 */
public class App {
    public static void main(String[] args) {
        // Force UTF-8 encoding for console output
        try {
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LogManager logger = LogManager.getInstance();
            logger.info("======================================");
            logger.info("DataScout 工具啟動中...");

            // 初始化設定
            AppSettings appSettings = new AppSettings();

            // 選擇資料來源
            da.api.view.FileSelectionDialog dialog = new da.api.view.FileSelectionDialog(appSettings.getRecentFiles());
            dialog.setVisible(true);

            String selectedPath = dialog.getSelectedFilePath();
            if (selectedPath == null || selectedPath.isEmpty()) {
                logger.info("使用者取消選擇檔案，應用程式結束");
                System.exit(0);
            }

            // 更新最近使用的檔案
            appSettings.addRecentFile(selectedPath);

            // 初始化服務
            logger.info("初始化服務...");
            UserService userService = new UserService();
            ExcelService excelService = new ExcelService(selectedPath);
            logger.info("服務初始化完成");

            // 直接建立主視窗（不需要登入）
            logger.info("建立主視窗...");
            MainFrameView jframe = new MainFrameView(userService);

            // 建立主視窗元件
            @SuppressWarnings("unused")
            MenuBar menuBar = new MenuBar(jframe, appSettings);
            @SuppressWarnings("unused")
            Panel panel = new Panel(jframe, excelService);
            logger.info("主視窗元件建立完成");

            // 設定系統托盤
            logger.info("設定系統托盤...");
            SystemTrayManager trayManager = new SystemTrayManager(jframe);
            trayManager.setMinimizeToTray(appSettings.isMinimizeToTray());
            logger.info("系統托盤設定完成");

            // 設定到期提醒服務
            logger.info("啟動到期提醒服務...");
            ExpiryReminderService reminderService = new ExpiryReminderService(excelService, appSettings);
            reminderService.startPeriodicCheck();
            logger.info("到期提醒服務已啟動");

            // 顯示主視窗
            logger.info("顯示主視窗");
            jframe.showGUI();
            logger.info("應用程式啟動完成!");
            logger.info("======================================");
        });
    }

}