package da.api;

import javax.swing.SwingUtilities;

import da.api.service.ExcelService;
import da.api.service.ExpiryReminderService;

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
        // 強制設定控制台輸出為 UTF-8
        try {
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> launch(null));
    }

    public static void launch(String initialPath) {
        LogManager logger = LogManager.getInstance();
        logger.info("======================================");
        if (initialPath != null) {
            logger.info("DataScout 新視窗啟動中... (" + initialPath + ")");
        } else {
            logger.info("DataScout 工具啟動中...");
        }

        // 初始化設定
        AppSettings appSettings = new AppSettings();

        String selectedPath = initialPath;
        if (selectedPath == null) {
            // 選擇資料來源
            da.api.view.FileSelectionDialog dialog = new da.api.view.FileSelectionDialog(appSettings.getRecentFiles(),
                    appSettings::removeRecentFile,
                    () -> {
                    }); // 傳遞空的 Runnable 以啟用按鈕，邏輯在下方處理
            dialog.setVisible(true);

            // 檢查是否請求建立新檔案
            if (dialog.isCreateNewRequested()) {
                performCreateNewFileAtStartup(logger);
                return; // 對話框已處理建立與啟動，或使用者取消存檔
            }

            selectedPath = dialog.getSelectedFilePath();
            if (selectedPath == null || selectedPath.isEmpty()) {
                logger.info("使用者取消選擇檔案");
                return;
            }
        }

        // 驗證檔案是否存在
        java.io.File file = new java.io.File(selectedPath);
        if (!file.exists()) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "檔案不存在: " + selectedPath + "\n將從最近使用清單中移除。",
                    "錯誤",
                    javax.swing.JOptionPane.ERROR_MESSAGE);

            appSettings.removeRecentFile(selectedPath);
            appSettings.removeColumnConfig(selectedPath);

            // 如果是初始啟動 (非新視窗)，則重新啟動選擇流程
            if (initialPath == null) {
                launch(null);
            }
            return;
        }

        // 更新最近使用的檔案
        appSettings.addRecentFile(selectedPath);

        // 初始化服務
        logger.info("初始化服務...");

        ExcelService excelService = new ExcelService(selectedPath);

        // 讀取 Excel 標題並讓使用者設定欄位
        java.util.List<String> headers = excelService.readHeaders();
        da.api.model.ColumnConfig config = null;

        if (headers != null && !headers.isEmpty()) {
            // 檢查是否有儲存的設定
            da.api.model.ColumnConfig savedConfig = appSettings.getColumnConfig(selectedPath);
            boolean useSaved = false;

            if (savedConfig != null) {
                // 創建美化的載入設定對話框
                javax.swing.JDialog dialog = new javax.swing.JDialog((java.awt.Frame) null, "載入設定", true);
                dialog.setSize(480, 400);
                dialog.setLocationRelativeTo(null);
                dialog.setResizable(false);

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

                javax.swing.JLabel titleLabel = new javax.swing.JLabel("偵測到已儲存的設定");
                titleLabel.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 16));
                titleLabel.setForeground(new java.awt.Color(146, 64, 14));
                headerPanel.add(titleLabel);

                // 內容區域
                javax.swing.JPanel contentPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
                contentPanel.setBackground(java.awt.Color.WHITE);
                contentPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createLineBorder(new java.awt.Color(229, 231, 235), 1, true),
                        javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)));

                javax.swing.JLabel messageLabel = new javax.swing.JLabel(
                        "<html><div style='text-align: center;'>" +
                                "偵測到此檔案已有儲存的欄位設定。<br><br>" +
                                "是否使用之前的設定？" +
                                "</div></html>");
                messageLabel.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.PLAIN, 14));
                messageLabel.setForeground(new java.awt.Color(31, 41, 55));
                messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                contentPanel.add(messageLabel, java.awt.BorderLayout.CENTER);

                // 按鈕區域
                javax.swing.JPanel buttonPanel = new javax.swing.JPanel(
                        new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 15));
                buttonPanel.setBackground(new java.awt.Color(248, 249, 250));

                final boolean[] choice = { false };

                // 是按鈕
                javax.swing.JButton yesButton = new javax.swing.JButton("是 (Y)");
                yesButton.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 14));
                yesButton.setForeground(java.awt.Color.WHITE);
                yesButton.setBackground(new java.awt.Color(37, 99, 235));
                yesButton.setPreferredSize(new java.awt.Dimension(120, 42));
                yesButton.setFocusPainted(false);
                yesButton.setBorderPainted(false);
                yesButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

                yesButton.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        yesButton.setBackground(new java.awt.Color(29, 78, 216));
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        yesButton.setBackground(new java.awt.Color(37, 99, 235));
                    }
                });

                yesButton.addActionListener(e -> {
                    choice[0] = true;
                    dialog.dispose();
                });

                // 否按鈕
                javax.swing.JButton noButton = new javax.swing.JButton("否 (N)");
                noButton.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 14));
                noButton.setForeground(new java.awt.Color(55, 65, 81));
                noButton.setBackground(new java.awt.Color(229, 231, 235));
                noButton.setPreferredSize(new java.awt.Dimension(120, 42));
                noButton.setFocusPainted(false);
                noButton.setBorderPainted(false);
                noButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

                noButton.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        noButton.setBackground(new java.awt.Color(209, 213, 219));
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        noButton.setBackground(new java.awt.Color(229, 231, 235));
                    }
                });

                noButton.addActionListener(e -> dialog.dispose());

                buttonPanel.add(yesButton);
                buttonPanel.add(noButton);

                // 組合面板
                mainContainer.add(headerPanel, java.awt.BorderLayout.NORTH);
                mainContainer.add(contentPanel, java.awt.BorderLayout.CENTER);
                mainContainer.add(buttonPanel, java.awt.BorderLayout.SOUTH);

                dialog.add(mainContainer);
                dialog.setVisible(true);

                if (choice[0]) {
                    config = savedConfig;
                    config.setAllHeaders(headers); // 設定標題 (未儲存於設定檔中)
                    useSaved = true;
                }
            }

            if (!useSaved) {
                da.api.view.ColumnConfigDialog configDialog = new da.api.view.ColumnConfigDialog(headers);
                configDialog.setVisible(true);

                if (configDialog.isConfirmed()) {
                    config = configDialog.getConfig();
                    // 儲存新設定
                    appSettings.saveColumnConfig(selectedPath, config);
                } else {
                    // 使用者取消設定
                    logger.info("使用者取消欄位設定");
                    return;
                }
            }
        }

        if (config != null) {
            excelService.setColumnConfig(config);
        }

        logger.info("服務初始化完成");

        // 直接建立主視窗（不需要登入）
        logger.info("建立主視窗...");
        MainFrameView jframe = new MainFrameView();

        // 建立主視窗元件
        @SuppressWarnings("unused")
        MenuBar menuBar = new MenuBar(jframe, appSettings, excelService);
        @SuppressWarnings("unused")
        Panel panel = new Panel(jframe, excelService, config, appSettings);
        menuBar.setOpenColumnConfigAction(panel::openSettings);
        menuBar.setOpenExpirySettingsAction(panel::openExpirySettings);
        menuBar.setOpenFileAction(panel::loadNewFile);
        menuBar.setCreateNewFileAction(panel::createNewFile);
        logger.info("主視窗元件建立完成");

        // 設定系統托盤
        logger.info("設定系統托盤...");
        // SystemTrayManager 內部使用 appSettings 即時檢查偏好設定
        SystemTrayManager trayManager = new SystemTrayManager(jframe, appSettings, selectedPath);
        logger.info("系統托盤設定完成");

        // 設定視窗標題與托盤提示
        java.io.File currentFile = new java.io.File(selectedPath);
        String title = "DataScout - " + currentFile.getName();
        jframe.setTitle(title);
        trayManager.setTooltip(title);

        // 設定到期提醒服務
        logger.info("啟動到期提醒服務...");
        ExpiryReminderService reminderService = new ExpiryReminderService(excelService, appSettings);
        reminderService.startPeriodicCheck();
        // 將服務參照設定到面板，以便切換檔案時能停止舊服務
        panel.setExpiryReminderService(reminderService);
        logger.info("到期提醒服務已啟動");

        // 顯示主視窗
        logger.info("顯示主視窗");
        jframe.showGUI();
        logger.info("應用程式啟動完成!");
        logger.info("======================================");
    }

    private static void performCreateNewFileAtStartup(LogManager logger) {
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("建立新檔案");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        int userSelection = fileChooser.showSaveDialog(null);
        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            String path = fileToSave.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".xlsx")) {
                path += ".xlsx";
            }

            if (new java.io.File(path).exists()) {
                launch(path);
                return;
            }

            da.api.service.ExcelService newService = new da.api.service.ExcelService(path);
            newService.createEmptyExcelFile();

            // 移除設定
            new da.api.util.AppSettings().removeColumnConfig(path);

            if (new java.io.File(path).exists()) {
                launch(path);
            } else {
                javax.swing.JOptionPane.showMessageDialog(null, "建立檔案失敗", "錯誤", javax.swing.JOptionPane.ERROR_MESSAGE);
                launch(null); // 重試
            }
        } else {
            logger.info("使用者取消建立新檔");
            launch(null); // 返回選擇介面
        }
    }

}