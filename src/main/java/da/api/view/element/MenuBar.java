package da.api.view.element;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import da.api.util.AppSettings;
import da.api.view.LogViewerDialog;
import da.api.view.MainFrameView;
import da.api.view.SettingsDialog;

public class MenuBar {

    private JMenuBar jMenuBar;
    private MainFrameView frameElement;
    private AppSettings appSettings;
    private da.api.service.ExcelService excelService;

    public MenuBar(MainFrameView frameElement, AppSettings appSettings, da.api.service.ExcelService excelService) {
        this.frameElement = frameElement;
        this.appSettings = appSettings;
        this.excelService = excelService;

        this.jMenuBar = new JMenuBar();

        // 美化 MenuBar
        styleMenuBar();

        jMenuBar.add(this.fileMenu());
        jMenuBar.add(this.settingsMenu());
        jMenuBar.add(this.toolsMenu());
        jMenuBar.add(this.about());

        frameElement.setJMenuBar(jMenuBar);
    }

    /**
     * 美化 MenuBar 樣式
     */
    private void styleMenuBar() {
        jMenuBar.setBackground(new java.awt.Color(255, 255, 255));
        jMenuBar.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0,
                new java.awt.Color(229, 231, 235)));
        jMenuBar.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.PLAIN, 13));
    }

    /**
     * 設定選單樣式
     */
    private void styleMenu(JMenu menu) {
        menu.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.PLAIN, 13));
        menu.setForeground(new java.awt.Color(31, 41, 55));
        menu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    /**
     * 設定選單項目樣式
     */
    private void styleMenuItem(JMenuItem item) {
        item.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.PLAIN, 13));
        item.setBackground(java.awt.Color.WHITE);
        item.setForeground(new java.awt.Color(31, 41, 55));
        item.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 15, 8, 15));
        item.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    private JMenu fileMenu() {
        JMenu fileMenu = new JMenu("檔案");
        styleMenu(fileMenu);

        JMenuItem newFileItem = new JMenuItem("另開新檔");
        styleMenuItem(newFileItem);
        newFileItem.addActionListener(e -> {
            if (createNewFileAction != null) {
                createNewFileAction.run();
            }
        });

        JMenuItem openFileItem = new JMenuItem("重新選擇檔案");
        styleMenuItem(openFileItem);
        openFileItem.addActionListener(e -> {
            if (openFileAction != null) {
                openFileAction.run();
            }
        });

        JMenuItem programExitItem = new JMenuItem("結束程式");
        styleMenuItem(programExitItem);
        programExitItem.addActionListener(e -> frameElement.dispose());

        fileMenu.add(newFileItem);
        fileMenu.add(openFileItem);
        fileMenu.addSeparator();
        fileMenu.add(programExitItem);

        return fileMenu;
    }

    // logout logic removed as per user request

    private Runnable openColumnConfigAction;
    private Runnable openExpirySettingsAction;
    private Runnable openFileAction;
    private Runnable createNewFileAction;

    public void setCreateNewFileAction(Runnable action) {
        this.createNewFileAction = action;
    }

    public void setOpenColumnConfigAction(Runnable action) {
        this.openColumnConfigAction = action;
    }

    public void setOpenExpirySettingsAction(Runnable action) {
        this.openExpirySettingsAction = action;
    }

    public void setOpenFileAction(Runnable action) {
        this.openFileAction = action;
    }

    private JMenu settingsMenu() {
        JMenu settingsMenu = new JMenu("設定");
        styleMenu(settingsMenu);

        JMenuItem settingsItem = new JMenuItem("偏好設定");
        styleMenuItem(settingsItem);
        settingsItem.addActionListener(e -> openSettings());

        JMenuItem expiryItem = new JMenuItem("到期日提醒設定");
        styleMenuItem(expiryItem);
        expiryItem.addActionListener(e -> {
            if (openExpirySettingsAction != null) {
                openExpirySettingsAction.run();
            } else {
                da.api.util.StyledDialogs.showMessageDialog(frameElement, "功能未就緒", "提示",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JMenuItem columnConfigItem = new JMenuItem("過濾器欄位設定");
        styleMenuItem(columnConfigItem);
        columnConfigItem.addActionListener(e -> {
            if (openColumnConfigAction != null) {
                openColumnConfigAction.run();
            } else {
                da.api.util.StyledDialogs.showMessageDialog(frameElement, "功能未就緒", "提示",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        settingsMenu.add(settingsItem);
        settingsMenu.add(expiryItem);
        settingsMenu.add(columnConfigItem);

        return settingsMenu;
    }

    private JMenu toolsMenu() {
        JMenu toolsMenu = new JMenu("工具");
        styleMenu(toolsMenu);

        JMenuItem logViewerItem = new JMenuItem("查看執行日誌");
        styleMenuItem(logViewerItem);
        logViewerItem.addActionListener(e -> openLogViewer());

        toolsMenu.add(logViewerItem);

        return toolsMenu;
    }

    private JMenu about() {
        JMenu aboutMenu = new JMenu("關於");
        styleMenu(aboutMenu);

        JMenuItem menuAbout = new JMenuItem("詳細資訊");
        styleMenuItem(menuAbout);
        menuAbout.addActionListener(e -> {
            showAboutDialog();
        });

        aboutMenu.add(menuAbout);

        return aboutMenu;
    }

    /**
     * 顯示美化的關於對話框
     */
    private void showAboutDialog() {
        String version = getVersion();
        String buildDate = getBuildDate();

        // 創建自定義對話框
        javax.swing.JDialog dialog = new javax.swing.JDialog(frameElement, "關於 DataScout", true);
        dialog.setLayout(new java.awt.BorderLayout(20, 20));
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(frameElement);
        dialog.setResizable(false);
        // 設定圖示
        try {
            java.net.URL iconUrl = getClass().getResource("/app_icon.png");
            if (iconUrl != null) {
                dialog.setIconImage(new javax.swing.ImageIcon(iconUrl).getImage());
            }
        } catch (Exception e) {
            // 忽略圖示載入錯誤
        }

        // 標題面板
        javax.swing.JPanel titlePanel = new javax.swing.JPanel();
        titlePanel.setBackground(new java.awt.Color(66, 133, 244)); // Google 藍
        titlePanel.setPreferredSize(new java.awt.Dimension(450, 80));
        titlePanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 25));

        javax.swing.JLabel titleLabel = new javax.swing.JLabel("DataScout");
        titleLabel.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 32));
        titleLabel.setForeground(java.awt.Color.WHITE);
        titlePanel.add(titleLabel);

        // 內容面板
        javax.swing.JPanel contentPanel = new javax.swing.JPanel();
        contentPanel.setBackground(java.awt.Color.WHITE);
        contentPanel.setLayout(new java.awt.GridBagLayout());
        contentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 30, 20, 30));

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(8, 0, 8, 0);

        // 版本資訊
        addInfoRow(contentPanel, gbc, "版本：", version);
        gbc.gridy++;

        // 更新日期
        addInfoRow(contentPanel, gbc, "更新日期：", buildDate);
        gbc.gridy++;

        // 開發人員
        addInfoRow(contentPanel, gbc, "開發人員：", "DDAD");
        gbc.gridy++;

        // 分隔線
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new java.awt.Insets(15, 0, 15, 0);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        javax.swing.JSeparator separator = new javax.swing.JSeparator();
        contentPanel.add(separator, gbc);

        // 版本更新說明標題
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.insets = new java.awt.Insets(10, 0, 5, 0);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        javax.swing.JLabel updatesTitle = new javax.swing.JLabel("版本更新說明：");
        updatesTitle.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 14));
        updatesTitle.setForeground(new java.awt.Color(60, 60, 60));
        contentPanel.add(updatesTitle, gbc);

        // 版本更新說明區域
        gbc.gridy++;
        gbc.weighty = 0.5;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.insets = new java.awt.Insets(0, 0, 15, 0);

        javax.swing.JTextArea updateNotesArea = new javax.swing.JTextArea();
        updateNotesArea.setText(getUpdateNotes());
        updateNotesArea.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.PLAIN, 12));
        updateNotesArea.setForeground(new java.awt.Color(80, 80, 80));
        updateNotesArea.setEditable(false);
        updateNotesArea.setLineWrap(true);
        updateNotesArea.setWrapStyleWord(true);
        updateNotesArea.setBackground(new java.awt.Color(250, 250, 250));

        javax.swing.JScrollPane updateScrollPane = new javax.swing.JScrollPane(updateNotesArea);
        updateScrollPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(230, 230, 230)));
        contentPanel.add(updateScrollPane, gbc);

        // 描述
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.insets = new java.awt.Insets(0, 0, 8, 0);
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        javax.swing.JLabel descLabel = new javax.swing.JLabel("智能數據探索與管理工具");
        descLabel.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.PLAIN, 13));
        descLabel.setForeground(new java.awt.Color(100, 100, 100));
        contentPanel.add(descLabel, gbc);

        // 按鈕面板
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        buttonPanel.setBackground(java.awt.Color.WHITE);
        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 15));

        javax.swing.JButton closeButton = new javax.swing.JButton("關閉");
        closeButton.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 13));
        closeButton.setBackground(new java.awt.Color(66, 133, 244));
        closeButton.setForeground(java.awt.Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        closeButton.setPreferredSize(new java.awt.Dimension(100, 35));
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);

        // 組裝對話框
        // 將內容面板包裝在滝動面板中
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        dialog.add(titlePanel, java.awt.BorderLayout.NORTH);
        dialog.add(scrollPane, java.awt.BorderLayout.CENTER);
        dialog.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * 添加資訊行到面板
     */
    private void addInfoRow(javax.swing.JPanel panel, java.awt.GridBagConstraints gbc, String label, String value) {
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        javax.swing.JLabel labelComponent = new javax.swing.JLabel(label);
        labelComponent.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 14));
        labelComponent.setForeground(new java.awt.Color(60, 60, 60));
        panel.add(labelComponent, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        javax.swing.JLabel valueComponent = new javax.swing.JLabel(value);
        valueComponent.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.PLAIN, 14));
        valueComponent.setForeground(new java.awt.Color(80, 80, 80));
        panel.add(valueComponent, gbc);
    }

    /**
     * 從 pom.xml 讀取版本號
     */
    private String getVersion() {
        try {
            // 嘗試從 pom.properties 讀取（Maven 打包後會生成）
            java.util.Properties properties = new java.util.Properties();
            java.io.InputStream is = getClass().getResourceAsStream(
                    "/META-INF/maven/da.api/data-scout/pom.properties");

            if (is != null) {
                properties.load(is);
                is.close();
                String version = properties.getProperty("version");
                if (version != null && !version.isEmpty()) {
                    return version;
                }
            }
        } catch (Exception e) {
            // 忽略錯誤，使用備用方案
        }

        // 備用方案：嘗試從 MANIFEST.MF 讀取
        try {
            Package pkg = getClass().getPackage();
            String version = pkg.getImplementationVersion();
            if (version != null && !version.isEmpty()) {
                return version;
            }
        } catch (Exception e) {
            // 忽略錯誤
        }

        // 如果都失敗，返回預設值
        return "v1.0.0-RELEASE";
    }

    /**
     * 獲取構建日期
     */
    private String getBuildDate() {
        try {
            // 嘗試從 pom.properties 讀取構建時間
            java.util.Properties properties = new java.util.Properties();
            java.io.InputStream is = getClass().getResourceAsStream(
                    "/META-INF/maven/da.api/data-scout/pom.properties");

            if (is != null) {
                properties.load(is);
                is.close();
                // Maven 不會自動生成 build.time，我們可以使用當前 JAR 的修改時間
            }
        } catch (Exception e) {
            // 忽略錯誤
        }

        // 嘗試從 JAR 檔案的修改時間獲取
        try {
            java.net.URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
            if (location != null) {
                java.io.File jarFile = new java.io.File(location.toURI());
                if (jarFile.exists()) {
                    long lastModified = jarFile.lastModified();
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(new java.util.Date(lastModified));
                }
            }
        } catch (Exception e) {
            // 忽略錯誤
        }

        // 如果都失敗，返回當前日期
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new java.util.Date());
    }

    /**
     * 獲取版本更新說明
     */
    private String getUpdateNotes() {
        return "【1.0.0-RELEASE 版本內容】\n" +
                "1. 實作資料欄位過濾查詢功能 \n" +
                "2. 實作欄位資料新增/編輯/刪除功能 \n" +
                "3. 實作欄位新增/刪除功能 \n" +
                "4. 實作偏好設定功能 \n" +
                "5. 實作日誌查看功能 \n" +
                "6. 實作到期日提醒/過濾器欄位設定功能";
    }

    private void openSettings() {
        String filePath = (excelService != null) ? excelService.getFilePath() : null;
        SettingsDialog dialog = new SettingsDialog(frameElement, appSettings, filePath);
        dialog.setVisible(true);
    }

    private void openLogViewer() {
        LogViewerDialog dialog = new LogViewerDialog(frameElement);
        dialog.setVisible(true);
    }
}
