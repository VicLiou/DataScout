package da.api.view.element;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import da.api.model.ExcelData;
import da.api.service.ExcelService;
import da.api.view.DataEditDialog;
import da.api.view.MainFrameView;

public class Panel {

    private MainFrameView frameElement;
    private JTable dataTable;
    private ExcelService excelService;
    private List<ExcelData> currentData;
    // 本地按鈕元件

    private List<ExcelData> allData;

    private da.api.model.ColumnConfig columnConfig;
    private JPanel filtersPanel;
    private da.api.util.AppSettings appSettings;
    private java.util.Map<String, javax.swing.JComboBox<String>> dynamicComboBoxes = new java.util.HashMap<>();

    // 檔案監控
    private java.util.concurrent.ScheduledExecutorService fileMonitorExecutor;
    private long lastModifiedTime = 0;
    private boolean isInternalChange = false;

    // 到期提醒天數顯示標籤
    private javax.swing.JLabel expiryReminderLabel;

    // 主面板
    private JPanel jPanelMain;

    public Panel(MainFrameView frameElement, ExcelService excelService, da.api.util.AppSettings appSettings) {
        this(frameElement, excelService, null, appSettings);
    }

    public Panel(MainFrameView frameElement, ExcelService excelService, da.api.model.ColumnConfig columnConfig,
            da.api.util.AppSettings appSettings) {
        this.frameElement = frameElement;
        this.excelService = excelService;
        this.columnConfig = columnConfig;
        this.appSettings = appSettings;

        jPanelMain = new JPanel();
        jPanelMain.setLayout(new BoxLayout(jPanelMain, BoxLayout.Y_AXIS));
        jPanelMain.setBackground(new java.awt.Color(245, 247, 250)); // 淺灰色背景增加對比度
        // 增加全域內距使介面更整潔
        jPanelMain.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // 根據需求移除了 panelSearchAreaTitle
        jPanelMain.add(this.panelSearchArea());
        jPanelMain.add(javax.swing.Box.createVerticalStrut(15)); // 增加間距
        jPanelMain.add(this.panelDataManagement());
        jPanelMain.add(javax.swing.Box.createVerticalStrut(10)); // 增加間距
        jPanelMain.add(this.panelTable());

        frameElement.add(jPanelMain);

        // 載入資料
        refreshData();

        // 若檔案存在則開始監控
        if (excelService != null && excelService.getFilePath() != null) {
            startFileMonitoring(excelService.getFilePath());
        }
    }

    private void startFileMonitoring(String filePath) {
        stopFileMonitoring();

        java.io.File file = new java.io.File(filePath);
        if (!file.exists())
            return;

        lastModifiedTime = file.lastModified();

        fileMonitorExecutor = java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "FileMonitor");
            t.setDaemon(true);
            return t;
        });

        fileMonitorExecutor.scheduleWithFixedDelay(() -> {
            java.io.File monitoredFile = new java.io.File(filePath);
            if (monitoredFile.exists()) {
                long currentModified = monitoredFile.lastModified();
                if (currentModified > lastModifiedTime) {
                    lastModifiedTime = currentModified;

                    // 如果是內部變更，跳過重新整理 (僅更新時間戳記)
                    // 透過 isInternalChange 標記來判斷是否為內部變更

                    if (isInternalChange) {
                        isInternalChange = false;
                    } else {
                        // 外部變更
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            refreshData();
                            da.api.util.LogManager.getInstance().info("偵測到外部檔案變更，已重新載入");
                        });
                    }
                }
            }
        }, 2, 2, java.util.concurrent.TimeUnit.SECONDS);
    }

    private void stopFileMonitoring() {
        if (fileMonitorExecutor != null && !fileMonitorExecutor.isShutdown()) {
            fileMonitorExecutor.shutdownNow();
        }
        fileMonitorExecutor = null;
    }

    // ... rest of the class ...

    private JPanel panelSearchArea() {
        JPanel mainPanel = new JPanel(new java.awt.BorderLayout()) {
            @Override
            public java.awt.Dimension getMaximumSize() {
                // 限制最大高度為偏好高度，寬度則無限延伸
                return new java.awt.Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        mainPanel.setBackground(new java.awt.Color(255, 255, 255));
        // 主面板已處理外邊距，此處僅保留底部微小邊距
        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 5, 0));

        // 切換按鈕 (標題)
        JButton toggleButton = new JButton("\u25BC 搜尋過濾器");
        // 使用 Dialog 字型以獲得更好的 Unicode 符號支援
        toggleButton.setFont(new java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.BOLD, 16));
        toggleButton.setForeground(new java.awt.Color(66, 133, 244)); // Google 藍
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setFocusPainted(false);
        toggleButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        toggleButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // 內容面板 (包含過濾器與動作)
        JPanel contentPanel = new JPanel(new java.awt.BorderLayout(20, 0));
        contentPanel.setBackground(java.awt.Color.WHITE);
        // 內部樣式 (線條邊框取代標題邊框)
        contentPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240), 1, true),
                javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15) // 內部間距
        ));

        // --- 過濾器面板 (左/中) ---
        filtersPanel = new JPanel(new java.awt.GridBagLayout());
        filtersPanel.setBackground(new java.awt.Color(255, 255, 255));

        rebuildFiltersPanel();

        // --- 動作按鈕面板 (右) ---
        JPanel actionPanel = new JPanel(new java.awt.GridBagLayout());
        actionPanel.setBackground(new java.awt.Color(255, 255, 255));
        // 在左側增加分隔線
        actionPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 0, new java.awt.Color(230, 230, 240)),
                javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 0)));

        JButton searchButton = createSearchButton();
        // 放大按鈕以更顯著
        searchButton.setPreferredSize(new java.awt.Dimension(100, 45));
        searchButton.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 16));

        actionPanel.add(searchButton);

        // 將面板加入內容面板
        contentPanel.add(filtersPanel, java.awt.BorderLayout.CENTER);
        contentPanel.add(actionPanel, java.awt.BorderLayout.EAST);

        // 切換邏輯
        // 切換邏輯
        toggleButton.addActionListener(e -> {
            boolean isContentVisible = contentPanel.isVisible();
            contentPanel.setVisible(!isContentVisible);
            toggleButton.setText(isContentVisible ? "\u25B6 搜尋過濾器" : "\u25BC 搜尋過濾器");

            // 動態調整邊框：收起時移除底部邊距，展開時保留
            int bottomMargin = !isContentVisible ? 5 : 0;
            mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, bottomMargin, 0));

            // 重新驗證布局
            if (jPanelMain != null) {
                jPanelMain.revalidate();
                jPanelMain.repaint();
            }
            mainPanel.revalidate();
            mainPanel.repaint();
            if (frameElement != null) {
                frameElement.revalidate();
                frameElement.repaint();
            }
        });

        // 加入主面板
        mainPanel.add(toggleButton, java.awt.BorderLayout.NORTH);
        mainPanel.add(contentPanel, java.awt.BorderLayout.CENTER);

        return mainPanel;
    }

    private void rebuildFiltersPanel() {
        filtersPanel.removeAll();
        dynamicComboBoxes.clear(); // 清除舊參考

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 10, 10, 15); // 格式設定
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

        java.awt.Font labelFont = new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 14);
        java.awt.Font comboFont = new java.awt.Font("微軟正黑體", java.awt.Font.PLAIN, 14);
        java.awt.Color labelColor = new java.awt.Color(60, 60, 60);

        if (columnConfig != null) {
            // 動態生成
            List<String> searchColumns = columnConfig.getSearchFilterColumns();
            int gridX = 0;
            int gridY = 0;

            for (String colName : searchColumns) {
                // 標籤
                javax.swing.JLabel label = new javax.swing.JLabel(colName + "：");
                label.setFont(labelFont);
                label.setForeground(labelColor);
                gbc.gridx = gridX;
                gbc.gridy = gridY;
                gbc.weightx = 0.0;
                filtersPanel.add(label, gbc);

                // ComboBox
                javax.swing.JComboBox<String> comboBox = new javax.swing.JComboBox<>();
                comboBox.addItem("ALL");
                comboBox.setFont(comboFont);
                comboBox.setBackground(java.awt.Color.WHITE);
                comboBox.setMinimumSize(new java.awt.Dimension(100, 35)); // 允許縮小
                comboBox.setPreferredSize(new java.awt.Dimension(150, 35)); // 預設大小

                // 注意: WidePopupMenuListener 將在 updateSearchOptions 中加入以支援動態下拉選單

                dynamicComboBoxes.put(colName, comboBox);

                gbc.gridx = gridX + 1;
                gbc.weightx = 0.5; // 允許擴展
                filtersPanel.add(comboBox, gbc);

                gridX += 2;
                // 每三個一組換行，若數量過多則自動調整
                if (gridX >= 6) {
                    gridX = 0;
                    gridY++;
                }
            }
        }

        filtersPanel.revalidate();
        filtersPanel.repaint();
    }

    private JButton createSearchButton() {
        JButton searchButton = new JButton("查詢");
        searchButton.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 14));
        searchButton.setBackground(new java.awt.Color(66, 133, 244)); // Google 藍
        searchButton.setForeground(java.awt.Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 15, 5, 15));
        searchButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        searchButton.addActionListener(e -> performSearch());
        return searchButton;
    }

    /**
     * 開啟欄位設定對話框
     */
    public void openSettings() {
        List<String> headers = excelService.readHeaders();
        // 使用完整建構子以預先填入資料
        da.api.view.ColumnConfigDialog dialog = new da.api.view.ColumnConfigDialog(headers, columnConfig);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            da.api.model.ColumnConfig newConfig = dialog.getConfig();
            this.columnConfig = newConfig;
            this.excelService.setColumnConfig(newConfig);

            // 儲存至設定
            appSettings.saveColumnConfig(excelService.getFilePath(), newConfig);

            rebuildFiltersPanel();
            refreshData();
            String filters = (newConfig.getSearchFilterColumns() != null)
                    ? String.join(", ", newConfig.getSearchFilterColumns())
                    : "無";
            String expiryCol = (newConfig.getExpiryDateColumn() != null) ? newConfig.getExpiryDateColumn() : "無";

            da.api.util.LogManager.getInstance().info(
                    String.format("使用者更新欄位設定。到期日欄位: [%s], 過濾欄位: [%s]", expiryCol, filters));
            JOptionPane.showMessageDialog(frameElement, "設定已更新!");
        } else {
            da.api.util.LogManager.getInstance().info("使用者取消欄位設定更新");
        }
    }

    private JPanel panelDataManagement() {
        // 主容器
        JPanel container = new JPanel(new java.awt.BorderLayout());
        container.setBackground(new java.awt.Color(248, 249, 250));
        container.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(229, 231, 235), 1, true),
                javax.swing.BorderFactory.createEmptyBorder(12, 15, 12, 15)));

        // 按鈕面板
        JPanel managementPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 15));
        managementPanel.setBackground(new java.awt.Color(248, 249, 250));

        java.awt.Font btnFont = new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 13);
        java.awt.Dimension btnSize = new java.awt.Dimension(95, 38);

        // 資料操作標籤
        javax.swing.JLabel dataLabel = new javax.swing.JLabel("資料操作：");
        dataLabel.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 13));
        dataLabel.setForeground(new java.awt.Color(55, 65, 81));
        managementPanel.add(dataLabel);
        managementPanel.add(javax.swing.Box.createHorizontalStrut(5));

        // 新增按鈕
        JButton addButton = createStyledButton("新增", new java.awt.Color(16, 185, 129), btnFont, btnSize);
        addButton.addActionListener(e -> addData());
        managementPanel.add(addButton);

        // 編輯按鈕
        JButton editButton = createStyledButton("編輯", new java.awt.Color(249, 115, 22), btnFont, btnSize);
        editButton.addActionListener(e -> editData());
        managementPanel.add(editButton);

        // 刪除按鈕
        JButton deleteButton = createStyledButton("刪除", new java.awt.Color(239, 68, 68), btnFont, btnSize);
        deleteButton.addActionListener(e -> deleteData());
        managementPanel.add(deleteButton);

        // 分隔線
        managementPanel.add(javax.swing.Box.createHorizontalStrut(15));
        javax.swing.JSeparator separator1 = new javax.swing.JSeparator(javax.swing.SwingConstants.VERTICAL);
        separator1.setPreferredSize(new java.awt.Dimension(1, 30));
        separator1.setForeground(new java.awt.Color(209, 213, 219));
        managementPanel.add(separator1);
        managementPanel.add(javax.swing.Box.createHorizontalStrut(15));

        // 欄位操作標籤
        javax.swing.JLabel columnLabel = new javax.swing.JLabel("欄位管理：");
        columnLabel.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 13));
        columnLabel.setForeground(new java.awt.Color(55, 65, 81));
        managementPanel.add(columnLabel);
        managementPanel.add(javax.swing.Box.createHorizontalStrut(5));

        // 新增欄位按鈕
        JButton addColumnButton = createStyledButton("+ 欄位", new java.awt.Color(59, 130, 246), btnFont, btnSize);
        addColumnButton.addActionListener(e -> addColumn());
        managementPanel.add(addColumnButton);

        // 刪除欄位按鈕
        JButton deleteColumnButton = createStyledButton("- 欄位", new java.awt.Color(168, 85, 247), btnFont, btnSize);
        deleteColumnButton.addActionListener(e -> deleteColumn());
        managementPanel.add(deleteColumnButton);

        // 添加分隔符
        managementPanel.add(javax.swing.Box.createHorizontalStrut(25));

        // 添加到期提醒天數顯示標籤
        expiryReminderLabel = new javax.swing.JLabel();
        expiryReminderLabel.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.PLAIN, 12));
        expiryReminderLabel.setForeground(new java.awt.Color(107, 114, 128));
        updateExpiryReminderLabel();
        managementPanel.add(expiryReminderLabel);

        container.add(managementPanel, java.awt.BorderLayout.CENTER);

        // 設定最大高度，防止在垂直方向被拉伸
        container.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 80));

        return container;
    }

    /**
     * 創建美化按鈕
     */
    private JButton createStyledButton(String text, java.awt.Color bgColor, java.awt.Font font,
            java.awt.Dimension size) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setForeground(java.awt.Color.WHITE);
        button.setBackground(bgColor);
        button.setPreferredSize(size);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Hover 效果
        java.awt.Color originalBg = bgColor;
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(darkenColor(originalBg, 0.85));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalBg);
            }
        });

        return button;
    }

    /**
     * 加深顏色
     */
    private java.awt.Color darkenColor(java.awt.Color color, double factor) {
        return new java.awt.Color(
                Math.max((int) (color.getRed() * factor), 0),
                Math.max((int) (color.getGreen() * factor), 0),
                Math.max((int) (color.getBlue() * factor), 0));
    }

    /**
     * 更新到期提醒天數標籤顯示
     */
    private void updateExpiryReminderLabel() {
        if (expiryReminderLabel != null && excelService != null) {
            int days = appSettings.getExpiryReminderDays(excelService.getFilePath());
            expiryReminderLabel.setText("【到期提醒：" + days + " 天前】");
        }
    }

    private JPanel panelTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        // 美化樣式
        tablePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240), 1, true));
        tablePanel.setBackground(java.awt.Color.WHITE);

        // 建立表格
        String[] columnNames = { "序號", "APID", "雲", "環境", "到期日", "API KEY", "申請單號" };
        dataTable = new JTable(new Object[0][7], columnNames);
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 美化表格樣式
        styleTable();

        // 設定欄位寬度
        dataTable.getColumnModel().getColumn(0).setPreferredWidth(50); // 序號
        dataTable.getColumnModel().getColumn(1).setPreferredWidth(120); // APID
        dataTable.getColumnModel().getColumn(2).setPreferredWidth(80); // 雲
        dataTable.getColumnModel().getColumn(3).setPreferredWidth(60); // 環境
        dataTable.getColumnModel().getColumn(4).setPreferredWidth(100); // 到期日
        dataTable.getColumnModel().getColumn(5).setPreferredWidth(250); // API KEY
        dataTable.getColumnModel().getColumn(6).setPreferredWidth(90); // 申請單號

        // 加入滑鼠右鍵選單
        addTableContextMenu();

        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setPreferredSize(new Dimension(750, 300));
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(java.awt.Color.WHITE);

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
    }

    /**
     * 美化表格樣式
     */
    private void styleTable() {
        // 字體
        dataTable.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.PLAIN, 13));
        dataTable.setRowHeight(32);

        // 表格顏色
        dataTable.setBackground(java.awt.Color.WHITE);
        dataTable.setForeground(new java.awt.Color(31, 41, 55));
        dataTable.setGridColor(new java.awt.Color(229, 231, 235));
        dataTable.setSelectionBackground(new java.awt.Color(219, 234, 254));
        dataTable.setSelectionForeground(new java.awt.Color(30, 58, 138));
        dataTable.setShowGrid(true);
        dataTable.setIntercellSpacing(new java.awt.Dimension(1, 1));

        // 表頭樣式
        javax.swing.table.JTableHeader header = dataTable.getTableHeader();
        header.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 13));
        header.setBackground(new java.awt.Color(248, 249, 250));
        header.setForeground(new java.awt.Color(55, 65, 81));
        header.setPreferredSize(new java.awt.Dimension(header.getPreferredSize().width, 40));
        header.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0,
                new java.awt.Color(229, 231, 235)));

        // 交替行顏色
        dataTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);

                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(java.awt.Color.WHITE);
                    } else {
                        c.setBackground(new java.awt.Color(249, 250, 251));
                    }
                    c.setForeground(new java.awt.Color(31, 41, 55));
                } else {
                    c.setBackground(new java.awt.Color(219, 234, 254));
                    c.setForeground(new java.awt.Color(30, 58, 138));
                }

                // 設定內距
                setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));

                return c;
            }
        });
    }

    /**
     * 加入表格右鍵選單
     */
    private void addTableContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        // 複製儲存格內容
        JMenuItem copyItem = new JMenuItem("複製");
        copyItem.addActionListener(e -> {
            int row = dataTable.getSelectedRow();
            int col = dataTable.getSelectedColumn();

            if (row >= 0 && col >= 0) {
                Object value = dataTable.getValueAt(row, col);
                String text = value != null ? value.toString() : "";

                // 複製到剪貼簿
                StringSelection selection = new StringSelection(text);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

                JOptionPane.showMessageDialog(frameElement,
                        "已複製: " + text,
                        "複製成功",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // 複製整列
        JMenuItem copyRowItem = new JMenuItem("複製整列");
        copyRowItem.addActionListener(e -> {
            int row = dataTable.getSelectedRow();

            if (row >= 0) {
                StringBuilder sb = new StringBuilder();
                int colCount = dataTable.getColumnCount();

                for (int i = 0; i < colCount; i++) {
                    Object value = dataTable.getValueAt(row, i);
                    sb.append(value != null ? value.toString() : "");
                    if (i < colCount - 1) {
                        sb.append("\t"); // 使用 Tab 分隔
                    }
                }

                // 複製到剪貼簿
                StringSelection selection = new StringSelection(sb.toString());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

                JOptionPane.showMessageDialog(frameElement,
                        "已複製整列資料",
                        "複製成功",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        popupMenu.add(copyItem);
        popupMenu.add(copyRowItem);

        // 加入滑鼠事件監聽
        dataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                int row = dataTable.rowAtPoint(e.getPoint());
                int col = dataTable.columnAtPoint(e.getPoint());

                if (row >= 0 && col >= 0) {
                    dataTable.setRowSelectionInterval(row, row);
                    dataTable.setColumnSelectionInterval(col, col);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private void refreshData() {
        allData = excelService.readAllData();
        currentData = new ArrayList<>(allData);
        if (columnConfig != null) {
            updateSearchOptions();
        }
        updateTable();
    }

    private void updateSearchOptions() {
        for (String colName : dynamicComboBoxes.keySet()) {
            javax.swing.JComboBox<String> comboBox = dynamicComboBoxes.get(colName);
            Object selectedObj = comboBox.getSelectedItem();
            String selected = (selectedObj != null) ? selectedObj.toString() : null;

            // 提取唯一值
            java.util.Set<String> values = new java.util.TreeSet<>();
            for (ExcelData data : allData) {
                String val = data.getAttribute(colName);
                if (val != null && !val.trim().isEmpty()) {
                    values.add(val.trim());
                }
            }

            List<String> fullList = new ArrayList<>();
            fullList.add("ALL");
            fullList.addAll(values);

            // 初始時使用完整列表重設模型
            comboBox.setModel(new javax.swing.DefaultComboBoxModel<>(fullList.toArray(new String[0])));

            // 修正尺寸穩定性
            comboBox.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXX"); // 穩定的寬度參考

            // 啟用搜尋功能
            setupSearchableComboBox(comboBox, fullList);

            // 重新啟用 WidePopupMenuListener 以修正長項目被截斷的問題
            // 先移除現有的監聽器以避免重複 (雖然在此處重建時不太可能發生)
            for (javax.swing.event.PopupMenuListener l : comboBox.getPopupMenuListeners()) {
                if (l instanceof WidePopupMenuListener)
                    comboBox.removePopupMenuListener(l);
            }
            comboBox.addPopupMenuListener(new WidePopupMenuListener());

            // 盡可能恢復選取項目
            if (selected != null) {
                // 檢查選取項目是否存在 (因剛從資料載入，無需模糊檢查)
                if ("ALL".equals(selected) || values.contains(selected)) {
                    comboBox.setSelectedItem(selected);
                }
            }
        }
    }

    private void setupSearchableComboBox(javax.swing.JComboBox<String> comboBox, List<String> items) {
        comboBox.setEditable(true);

        // 取得編輯器元件
        javax.swing.JTextField editor = (javax.swing.JTextField) comboBox.getEditor().getEditorComponent();

        // 檢查是否已有監聽器
        SearchableKeyAdapter listener = (SearchableKeyAdapter) comboBox.getClientProperty("SearchableKeyAdapter");
        if (listener == null) {
            listener = new SearchableKeyAdapter(comboBox, items);
            editor.addKeyListener(listener);
            comboBox.putClientProperty("SearchableKeyAdapter", listener);
        } else {
            // 更新項目
            listener.setItems(items);
        }
    }

    private class SearchableKeyAdapter extends java.awt.event.KeyAdapter {
        private final javax.swing.JComboBox<String> comboBox;
        private java.util.List<String> items;

        public SearchableKeyAdapter(javax.swing.JComboBox<String> comboBox, java.util.List<String> items) {
            this.comboBox = comboBox;
            this.items = new ArrayList<>(items);
        }

        public void setItems(java.util.List<String> items) {
            this.items = new ArrayList<>(items);
        }

        @Override
        public void keyReleased(java.awt.event.KeyEvent e) {
            // 忽略導航鍵
            if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN ||
                    e.getKeyCode() == java.awt.event.KeyEvent.VK_UP ||
                    e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                return;
            }

            javax.swing.SwingUtilities.invokeLater(() -> {
                javax.swing.JTextField textField = (javax.swing.JTextField) comboBox.getEditor().getEditorComponent();
                String text = textField.getText();
                int caretPosition = textField.getCaretPosition();

                // 過濾項目
                List<String> filtered = new ArrayList<>();
                for (String item : items) {
                    if (item.toLowerCase().contains(text.toLowerCase())) {
                        filtered.add(item);
                    }
                }

                // 更新模型
                javax.swing.DefaultComboBoxModel<String> model = new javax.swing.DefaultComboBoxModel<>(
                        filtered.toArray(new String[0]));
                comboBox.setModel(model);

                // 恢復文字和游標位置
                textField.setText(text);
                try {
                    textField.setCaretPosition(caretPosition);
                } catch (Exception ex) {
                    // 忽略游標錯誤
                }

                // 處理彈出視窗
                if (!filtered.isEmpty()) {
                    comboBox.showPopup();
                    // 防止自動選擇第一個項目阻礙打字
                    if (comboBox.getItemCount() > 0) {
                        // comboBox.setSelectedIndex(0); // 選項: 選擇第一個相符項目? 否，會中斷打字。
                        // typing.
                    }
                } else {
                    comboBox.hidePopup();
                }
            });
        }
    }

    private void performSearch() {
        StringBuilder sb = new StringBuilder();

        if (columnConfig != null) {
            List<String> searchColumns = columnConfig.getSearchFilterColumns();

            // 記錄搜尋條件
            sb.append("查詢條件 - ");
            for (String col : searchColumns) {
                // 從可編輯下拉選單的編輯器取得選取項目以確保文字相符
                javax.swing.JComboBox<String> cb = dynamicComboBoxes.get(col);
                Object selectedObj = cb.getSelectedItem();

                // 對於可編輯下拉選單，若文字為自由格式，getSelectedItem 有時為 null 或未更新
                // 但對於搜尋過濾，我們通常需要它。
                // 若文字欄位有內容，是否採用?
                String selected = (selectedObj != null) ? selectedObj.toString() : "ALL";

                sb.append(col).append(": ").append(selected).append(", ");
            }

            currentData = new ArrayList<>();
            for (ExcelData data : allData) {
                boolean match = true;
                for (String col : searchColumns) {
                    javax.swing.JComboBox<String> cb = dynamicComboBoxes.get(col);
                    Object selectedObj = cb.getSelectedItem();
                    // 若選擇為 null 但文字存在，是否退回使用文字編輯器內容?
                    // 通常 getSelectedItem() 會反映可編輯 CB 中的文字。

                    String selected = (selectedObj != null) ? selectedObj.toString() : "ALL";

                    if (selected != null && !"ALL".equals(selected) && !selected.trim().isEmpty()) {
                        String val = data.getAttribute(col);
                        // 完全符合或包含? 過濾值通常使用完全符合。
                        if (val == null || !val.trim().equals(selected.trim())) {
                            match = false;
                            break;
                        }
                    }
                }
                if (match)
                    currentData.add(data);
            }
        } else {
            // 若無設定，不做任何篩選或顯示全部
            currentData = new ArrayList<>(allData);
        }

        updateTable();
        // 記錄搜尋
        String filterConditions = (columnConfig != null && sb.length() > 0) ? sb.toString() : "無過濾條件";
        da.api.util.LogManager.getInstance()
                .info("使用者執行搜尋查詢 [" + filterConditions + "] 找到 " + currentData.size() + " 筆資料");

        // 使用美化的對話框顯示查詢結果
        da.api.util.StyledDialogs.showMessageDialog(frameElement,
                "找到 " + currentData.size() + " 筆資料",
                "查詢結果",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 檢查資料是否符合當前的篩選條件
     */
    private boolean matchesCurrentFilter(ExcelData data) {
        if (columnConfig == null) {
            return true; // 無篩選條件，全部符合
        }

        List<String> searchColumns = columnConfig.getSearchFilterColumns();
        if (searchColumns == null || searchColumns.isEmpty()) {
            return true; // 無篩選條件，全部符合
        }

        // 檢查每個篩選條件
        for (String col : searchColumns) {
            javax.swing.JComboBox<String> cb = dynamicComboBoxes.get(col);
            if (cb != null) {
                Object selectedObj = cb.getSelectedItem();
                String selected = (selectedObj != null) ? selectedObj.toString() : "ALL";

                if (selected != null && !"ALL".equals(selected) && !selected.trim().isEmpty()) {
                    String val = data.getAttribute(col);
                    if (val == null || !val.trim().equals(selected.trim())) {
                        return false; // 不符合此篩選條件
                    }
                }
            }
        }

        return true; // 全部篩選條件都符合
    }

    private void updateTable() {
        String[] columnNames;
        Object[][] data;

        if (columnConfig != null) {
            List<String> headers = columnConfig.getAllHeaders();
            List<String> tableHeaders = new ArrayList<>();
            tableHeaders.add("序號");
            tableHeaders.addAll(headers);
            columnNames = tableHeaders.toArray(new String[0]);

            data = new Object[currentData.size()][columnNames.length];
            for (int i = 0; i < currentData.size(); i++) {
                data[i] = currentData.get(i).toArray(headers, i + 1);
            }
        } else {
            // 預設 / 檔案空白時的狀態
            columnNames = new String[] { "序號" };
            data = new Object[0][1];
        }

        dataTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可直接編輯
            }
        });

        // 到期列高亮顯示
        RowExpiryRenderer renderer = new RowExpiryRenderer();
        for (int i = 0; i < dataTable.getColumnCount(); i++) {
            dataTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // 基本欄寬調整
        if (dataTable.getColumnCount() > 0) {
            dataTable.getColumnModel().getColumn(0).setPreferredWidth(50); // 序號
        }
    }

    /**
     * 載入新檔案
     */
    // 到期提醒服務參照
    private da.api.service.ExpiryReminderService reminderService;

    public void setExpiryReminderService(da.api.service.ExpiryReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /**
     * 載入新檔案
     */
    public void loadNewFile() {
        da.api.view.FileSelectionDialog dialog = new da.api.view.FileSelectionDialog(appSettings.getRecentFiles(),
                path -> appSettings.removeRecentFile(path),
                null); // 此處不可建立新檔，僅限切換檔案
        dialog.setVisible(true);
        String selectedPath = dialog.getSelectedFilePath();

        if (selectedPath != null && !selectedPath.isEmpty()) {
            // 驗證文件是否存在
            java.io.File file = new java.io.File(selectedPath);
            if (!file.exists()) {
                javax.swing.JOptionPane.showMessageDialog(frameElement,
                        "檔案不存在: " + selectedPath + "\n將從最近使用清單中移除。",
                        "錯誤",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                appSettings.removeRecentFile(selectedPath);
                // 重新打開對話框，讓使用者重新選擇
                loadNewFile();
                return;
            }

            // 停止舊的監控服務
            stopFileMonitoring();
            if (reminderService != null) {
                reminderService.stopPeriodicCheck();
            }

            da.api.util.LogManager.getInstance().info("使用者載入新檔案：" + selectedPath);

            // 關閉當前視窗
            if (frameElement != null) {
                frameElement.dispose();
            }

            // 啟動新實例
            da.api.App.launch(selectedPath);
        }
    }

    /**
     * 建立新檔案
     */
    public void createNewFile() {
        // 先顯示最近檔案對話框 (使用者需求: "類似開啟檔案")
        // 傳遞回呼給 "建立空白檔案" 以觸發舊的建立邏輯
        da.api.view.FileSelectionDialog dialog = new da.api.view.FileSelectionDialog(appSettings.getRecentFiles(),
                path -> appSettings.removeRecentFile(path),
                () -> {
                    // 建立新檔案回呼 (使用者點擊 "建立空白檔案")
                    // 若 isCreateNewRequested 為真，將在對話框關閉後處理
                });

        dialog.setVisible(true);

        // 檢查使用者是否要求建立新檔案
        if (dialog.isCreateNewRequested()) {
            da.api.util.LogManager.getInstance().info("使用者請求建立新檔案");
            performCreateNewFile();
            return;
        }

        // 檢查使用者是否選擇了現有檔案 (在新視窗開啟)
        String selectedPath = dialog.getSelectedFilePath();
        if (selectedPath != null && !selectedPath.isEmpty()) {
            da.api.util.LogManager.getInstance().info("使用者選擇在新視窗開啟檔案：" + selectedPath);
            if (new java.io.File(selectedPath).exists()) {
                da.api.App.launch(selectedPath);
            } else {
                JOptionPane.showMessageDialog(frameElement,
                        "檔案不存在: " + selectedPath,
                        "錯誤",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void performCreateNewFile() {
        javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
        fileChooser.setDialogTitle("另存新檔");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        int userSelection = fileChooser.showSaveDialog(frameElement);
        if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            String path = fileToSave.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".xlsx")) {
                path += ".xlsx";
            }

            // 檢查使用者是否選擇了與目前開啟相同的檔案
            if (excelService != null && path.equalsIgnoreCase(excelService.getFilePath())) {
                JOptionPane.showMessageDialog(frameElement,
                        "無法覆蓋目前正在使用的檔案!",
                        "錯誤",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 若檔案已存在則直接開啟
            if (new java.io.File(path).exists()) {
                da.api.App.launch(path);
                return;
            }

            da.api.service.ExcelService newService = new da.api.service.ExcelService(path);
            newService.createEmptyExcelFile(); // 建立空檔案

            // 確保無殘留設定
            appSettings.removeColumnConfig(path);

            if (new java.io.File(path).exists()) {
                da.api.util.LogManager.getInstance().info("建立新檔案成功，啟動新實例：" + path);
                da.api.App.launch(path);
            } else {
                da.api.util.LogManager.getInstance().error("建立新檔案失敗：" + path);
                JOptionPane.showMessageDialog(frameElement, "建立檔案失敗", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class RowExpiryRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                // 預設顏色
                c.setBackground(java.awt.Color.WHITE);
                c.setForeground(java.awt.Color.BLACK);

                if (currentData != null && row >= 0 && row < currentData.size()) {
                    try {
                        da.api.model.ExcelData data = currentData.get(row);
                        if (data.getExpiryDate() != null) {
                            long days = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(),
                                    data.getExpiryDate());

                            if (days < 0) {
                                // 已經過期：紅底警告
                                c.setBackground(new java.awt.Color(255, 205, 210)); // 淺紅色
                                c.setForeground(new java.awt.Color(198, 40, 40)); // 深紅色文字
                            } else if (days >= 0
                                    && days <= appSettings.getExpiryReminderDays(excelService.getFilePath())) {
                                // 即將到期：黃底警告
                                c.setBackground(new java.awt.Color(255, 243, 205)); // 淺黃色
                                c.setForeground(new java.awt.Color(133, 100, 4)); // 深黃/褐色文字
                            }
                        }
                    } catch (Exception e) {
                        // 忽略模型錯誤
                    }
                }
            } else {
                // 保持選取顏色
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            }
            return c;
        }
    }

    private void addData() {
        // 檢查設定/標題是否為空
        if (columnConfig == null || columnConfig.getAllHeaders() == null || columnConfig.getAllHeaders().isEmpty()) {
            // 進一步檢查服務，以防配置遺失但檔案有標題?
            // 通常有效的配置意味著我們讀取了標題。如果配置為空，可能檔案是舊版或空的。
            // 若 excelService.readHeaders() 為空，表示絕對是空檔案
            java.util.List<String> rawHeaders = excelService.readHeaders();

            if (rawHeaders == null || rawHeaders.isEmpty()) {
                // 提示輸入標題
                da.api.view.HeaderSetupDialog headerDialog = new da.api.view.HeaderSetupDialog(frameElement);
                headerDialog.setVisible(true);

                if (headerDialog.isConfirmed()) {
                    java.util.List<String> newHeaders = headerDialog.getHeaders();

                    // 提示設定欄位配置 (過濾/到期日)
                    da.api.view.ColumnConfigDialog configDialog = new da.api.view.ColumnConfigDialog(newHeaders);
                    configDialog.setVisible(true);

                    da.api.model.ColumnConfig newConfig;
                    if (configDialog.isConfirmed()) {
                        newConfig = configDialog.getConfig();
                        // 確保所有標題已設定 (雖然對話框會處理)
                        newConfig.setAllHeaders(newHeaders);
                    } else {
                        return; // 使用者取消配置
                    }

                    this.columnConfig = newConfig;
                    this.excelService.setColumnConfig(newConfig);

                    // 初始儲存以寫入標題 (空資料)
                    this.excelService.saveAllData(new ArrayList<>());

                    // 儲存偏好設定
                    appSettings.saveColumnConfig(excelService.getFilePath(), newConfig);

                    // 重新整理介面
                    rebuildFiltersPanel();
                    updateTable();
                } else {
                    return; // 使用者取消標題設定
                }
            }
        }

        DataEditDialog dialog = new DataEditDialog(frameElement, null, true, columnConfig);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            ExcelData newData = dialog.getResult();
            // 加入到完整資料集
            allData.add(newData);
            // 如果符合當前篩選條件，也加入到篩選結果
            if (matchesCurrentFilter(newData)) {
                currentData.add(newData);
            }
            // 保存完整資料集
            if (excelService.saveAllData(allData)) {
                // 更新時間戳記以避免重複讀取
                java.io.File file = new java.io.File(excelService.getFilePath());
                if (file.exists()) {
                    isInternalChange = true;
                    lastModifiedTime = file.lastModified();
                }
                updateTable(); // 只更新表格顯示，不重新載入檔案
                updateSearchOptions(); // 更新過濾器選項

                // 構建資料內容字串
                StringBuilder sb = new StringBuilder();
                if (columnConfig != null && columnConfig.getAllHeaders() != null) {
                    for (String header : columnConfig.getAllHeaders()) {
                        sb.append(header).append(": ").append(newData.getAttribute(header)).append(", ");
                    }
                }
                da.api.util.LogManager.getInstance().info("使用者新增資料成功。內容: {" + sb.toString() + "}");
                JOptionPane.showMessageDialog(frameElement, "新增成功!");
            } else {
                // 保存失敗，回滾
                allData.remove(newData);
                currentData.remove(newData);
                da.api.util.LogManager.getInstance().error("使用者新增資料失敗");
                JOptionPane.showMessageDialog(frameElement,
                        "新增失敗!", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frameElement,
                    "請先選擇要編輯的資料!", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ExcelData selectedData = currentData.get(selectedRow);
        DataEditDialog dialog = new DataEditDialog(frameElement, selectedData, false, columnConfig);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            ExcelData editedData = dialog.getResult();
            // 在完整資料集中找到並更新
            int allDataIndex = allData.indexOf(selectedData);
            if (allDataIndex != -1) {
                allData.set(allDataIndex, editedData);
            }
            // 更新篩選結果
            currentData.set(selectedRow, editedData);

            // 保存完整資料集
            if (excelService.saveAllData(allData)) {
                // Update timestamp to avoid re-reading
                java.io.File file = new java.io.File(excelService.getFilePath());
                if (file.exists()) {
                    isInternalChange = true;
                    lastModifiedTime = file.lastModified();
                }
                updateTable(); // 只更新表格顯示，不重新載入檔案
                updateSearchOptions(); // 更新過濾器選項

                // 構建差異字串
                StringBuilder diff = new StringBuilder();
                if (columnConfig != null && columnConfig.getAllHeaders() != null) {
                    for (String header : columnConfig.getAllHeaders()) {
                        String oldVal = selectedData.getAttribute(header);
                        String newVal = editedData.getAttribute(header);
                        oldVal = (oldVal == null) ? "" : oldVal.trim();
                        newVal = (newVal == null) ? "" : newVal.trim();
                        if (!oldVal.equals(newVal)) {
                            diff.append(header).append(": \"").append(oldVal).append("\" -> \"").append(newVal)
                                    .append("\", ");
                        }
                    }
                }
                da.api.util.LogManager.getInstance()
                        .info("使用者編輯資料成功 (Row: " + selectedRow + ")。變更: [" + diff.toString() + "]");
                JOptionPane.showMessageDialog(frameElement, "編輯成功!");
            } else {
                // 保存失敗，回滾
                if (allDataIndex != -1) {
                    allData.set(allDataIndex, selectedData);
                }
                currentData.set(selectedRow, selectedData);
                da.api.util.LogManager.getInstance().error("使用者編輯資料失敗 (Row: " + selectedRow + ")");
                JOptionPane.showMessageDialog(frameElement,
                        "編輯失敗!", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            da.api.util.StyledDialogs.showMessageDialog(frameElement,
                    "請先選擇要刪除的資料!", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean confirm = da.api.util.StyledDialogs.showConfirmDialog(frameElement,
                "確定要刪除這筆資料嗎?", "確認刪除", JOptionPane.WARNING_MESSAGE);

        if (confirm) {
            ExcelData dataToDelete = currentData.get(selectedRow);
            // 從完整資料集中刪除
            allData.remove(dataToDelete);
            // 從篩選結果中刪除
            currentData.remove(selectedRow);

            // 保存完整資料集
            if (excelService.saveAllData(allData)) {
                // 更新時間戳記以避免重複讀取
                java.io.File file = new java.io.File(excelService.getFilePath());
                if (file.exists()) {
                    isInternalChange = true;
                    lastModifiedTime = file.lastModified();
                }
                updateTable(); // 只更新表格顯示，不重新載入檔案
                updateSearchOptions(); // 更新過濾器選項

                // 構建刪除資料內容字串
                StringBuilder sb = new StringBuilder();
                if (columnConfig != null && columnConfig.getAllHeaders() != null) {
                    for (String header : columnConfig.getAllHeaders()) {
                        sb.append(header).append(": ").append(dataToDelete.getAttribute(header)).append(", ");
                    }
                }
                da.api.util.LogManager.getInstance()
                        .info("使用者刪除資料成功 (Row: " + selectedRow + ")。已刪除內容: {" + sb.toString() + "}");
                da.api.util.StyledDialogs.showMessageDialog(frameElement, "刪除成功!", "成功",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                // 保存失敗，回滾
                allData.add(dataToDelete);
                currentData.add(selectedRow, dataToDelete);
                da.api.util.LogManager.getInstance().error("使用者刪除資料失敗 (Row: " + selectedRow + ")");
                da.api.util.StyledDialogs.showMessageDialog(frameElement,
                        "刪除失敗!", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 新增欄位
     */
    private void addColumn() {
        // 檢查設定是否存在
        if (columnConfig == null) {
            da.api.util.StyledDialogs.showMessageDialog(frameElement,
                    "請先載入檔案並設定欄位配置！", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 輸入新欄位名稱
        String columnName = da.api.util.StyledDialogs.showInputDialog(frameElement,
                "請輸入新欄位名稱：",
                "新增欄位",
                "");

        // 檢查輸入
        if (columnName == null || columnName.trim().isEmpty()) {
            return; // 用戶取消或輸入空值
        }

        columnName = columnName.trim();

        // 檢查欄位是否已存在
        if (columnConfig.getAllHeaders().contains(columnName)) {
            da.api.util.StyledDialogs.showMessageDialog(frameElement,
                    "欄位「" + columnName + "」已存在！", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 確認添加
        boolean confirm = da.api.util.StyledDialogs.showConfirmDialog(frameElement,
                "確定要新增欄位「" + columnName + "」嗎？\n\n" +
                        "注意：新欄位將會添加到所有資料列，\n" +
                        "現有資料的該欄位值將為空白。",
                "確認新增欄位",
                JOptionPane.QUESTION_MESSAGE);

        if (!confirm) {
            return;
        }

        try {
            // 更新 ColumnConfig
            List<String> headers = new ArrayList<>(columnConfig.getAllHeaders());
            headers.add(columnName);
            columnConfig.setAllHeaders(headers);

            // 保存新的欄位配置
            appSettings.saveColumnConfig(excelService.getFilePath(), columnConfig);

            // 更新 Excel 檔案（添加新欄位到標題列）
            if (excelService.addColumnToExcel(columnName)) {
                // 重新載入資料
                refreshData();
                da.api.util.LogManager.getInstance().info("使用者新增欄位：" + columnName);
                da.api.util.StyledDialogs.showMessageDialog(frameElement,
                        "欄位「" + columnName + "」新增成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // 回滾配置
                headers.remove(columnName);
                columnConfig.setAllHeaders(headers);
                appSettings.saveColumnConfig(excelService.getFilePath(), columnConfig);

                da.api.util.LogManager.getInstance().error("使用者新增欄位失敗：" + columnName);
                da.api.util.StyledDialogs.showMessageDialog(frameElement,
                        "新增欄位失敗！", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            da.api.util.LogManager.getInstance().error("新增欄位時發生錯誤：" + e.getMessage());
            da.api.util.StyledDialogs.showMessageDialog(frameElement,
                    "新增欄位時發生錯誤：" + e.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 刪除欄位
     */
    private void deleteColumn() {
        // 檢查設定是否存在
        if (columnConfig == null || columnConfig.getAllHeaders() == null || columnConfig.getAllHeaders().isEmpty()) {
            da.api.util.StyledDialogs.showMessageDialog(frameElement,
                    "目前沒有可刪除的欄位！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<String> headers = columnConfig.getAllHeaders();
        String expiryColumn = columnConfig.getExpiryDateColumn();

        // 建立可刪除的欄位列表（排除到期日欄位）
        List<String> deletableHeaders = new ArrayList<>();
        for (String header : headers) {
            if (!header.equals(expiryColumn)) {
                deletableHeaders.add(header);
            }
        }

        if (deletableHeaders.isEmpty()) {
            da.api.util.StyledDialogs.showMessageDialog(frameElement,
                    "沒有可刪除的欄位！\n（到期日欄位不能刪除）", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 讓用戶選擇要刪除的欄位
        String[] options = deletableHeaders.toArray(new String[0]);
        String selectedColumn = da.api.util.StyledDialogs.showSelectionDialog(
                frameElement,
                "請選擇要刪除的欄位：",
                "刪除欄位",
                options,
                options[0]);

        // 用戶取消選擇
        if (selectedColumn == null) {
            return;
        }

        // 確認刪除
        boolean confirm = da.api.util.StyledDialogs.showConfirmDialog(frameElement,
                "確定要刪除欄位「" + selectedColumn + "」嗎？\n\n" +
                        "警告：此操作無法還原！\n" +
                        "所有資料的該欄位資訊將永久遺失。",
                "確認刪除欄位",
                JOptionPane.WARNING_MESSAGE);

        if (!confirm) {
            return;
        }

        try {
            // 找到欄位索引
            int columnIndex = headers.indexOf(selectedColumn);
            if (columnIndex < 0) {
                da.api.util.StyledDialogs.showMessageDialog(frameElement,
                        "找不到欄位！", "錯誤", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 備份原始配置
            List<String> originalHeaders = new ArrayList<>(headers);

            // 更新 ColumnConfig
            List<String> newHeaders = new ArrayList<>(headers);
            newHeaders.remove(selectedColumn);
            columnConfig.setAllHeaders(newHeaders);

            // 從搜尋過濾欄位中移除（如果存在）
            List<String> searchFilterColumns = columnConfig.getSearchFilterColumns();
            if (searchFilterColumns != null && searchFilterColumns.contains(selectedColumn)) {
                List<String> newSearchFilters = new ArrayList<>(searchFilterColumns);
                newSearchFilters.remove(selectedColumn);
                columnConfig.setSearchFilterColumns(newSearchFilters);
            }

            // 保存新的欄位配置
            appSettings.saveColumnConfig(excelService.getFilePath(), columnConfig);

            // 從 Excel 檔案中刪除欄位
            if (excelService.removeColumnFromExcel(columnIndex)) {
                // 重新載入資料
                refreshData();
                da.api.util.LogManager.getInstance().info("使用者刪除欄位：" + selectedColumn);
                da.api.util.StyledDialogs.showMessageDialog(frameElement,
                        "欄位「" + selectedColumn + "」刪除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // 回滾配置
                columnConfig.setAllHeaders(originalHeaders);
                if (searchFilterColumns != null && !searchFilterColumns.contains(selectedColumn)) {
                    columnConfig.setSearchFilterColumns(searchFilterColumns);
                }
                appSettings.saveColumnConfig(excelService.getFilePath(), columnConfig);

                da.api.util.LogManager.getInstance().error("使用者刪除欄位失敗：" + selectedColumn);
                da.api.util.StyledDialogs.showMessageDialog(frameElement,
                        "刪除欄位失敗！", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            da.api.util.LogManager.getInstance().error("刪除欄位時發生錯誤：" + e.getMessage());
            da.api.util.StyledDialogs.showMessageDialog(frameElement,
                    "刪除欄位時發生錯誤：" + e.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 開啟到期提醒設定
     */
    public void openExpirySettings() {
        int currentDays = appSettings.getExpiryReminderDays(excelService.getFilePath());

        // 創建美化的自訂對話框
        javax.swing.JDialog dialog = new javax.swing.JDialog(frameElement, "到期日提醒設定", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(frameElement);
        dialog.setResizable(false);

        // 設定圖示
        try {
            java.net.URL iconUrl = getClass().getResource("/app_icon.png");
            if (iconUrl != null) {
                dialog.setIconImage(new javax.swing.ImageIcon(iconUrl).getImage());
            }
        } catch (Exception e) {
            // 忽略
        }

        // 主容器
        JPanel mainContainer = new JPanel(new java.awt.BorderLayout(0, 20));
        mainContainer.setBackground(new java.awt.Color(248, 249, 250));
        mainContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // ============ 標題區域 ============
        JPanel headerPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 10));
        headerPanel.setBackground(new java.awt.Color(254, 249, 195));
        headerPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(253, 224, 71), 1, true),
                javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        javax.swing.JLabel titleLabel = new javax.swing.JLabel("到期日提醒設定");
        titleLabel.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 16));
        titleLabel.setForeground(new java.awt.Color(133, 77, 14));
        headerPanel.add(titleLabel);

        // ============ 內容區域 ============
        JPanel contentPanel = new JPanel(new java.awt.BorderLayout(0, 15));
        contentPanel.setBackground(java.awt.Color.WHITE);
        contentPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(229, 231, 235), 1, true),
                javax.swing.BorderFactory.createEmptyBorder(25, 25, 25, 25)));

        // 說明文字
        javax.swing.JLabel descLabel = new javax.swing.JLabel(
                "<html><div style='text-align: center;'>" +
                        "設定提前幾天提醒即將到期的項目<br>" +
                        "範圍：1 至 365 天" +
                        "</div></html>");
        descLabel.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.PLAIN, 13));
        descLabel.setForeground(new java.awt.Color(107, 114, 128));
        descLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        // Spinner 面板
        JPanel spinnerPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 15));
        spinnerPanel.setBackground(java.awt.Color.WHITE);

        javax.swing.JLabel label = new javax.swing.JLabel("提醒天數：");
        label.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 14));
        label.setForeground(new java.awt.Color(31, 41, 55));

        javax.swing.SpinnerNumberModel spinnerModel = new javax.swing.SpinnerNumberModel(
                currentDays, 1, 365, 1);
        javax.swing.JSpinner spinner = new javax.swing.JSpinner(spinnerModel);
        spinner.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 16));
        spinner.setPreferredSize(new java.awt.Dimension(100, 40));
        ((javax.swing.JSpinner.DefaultEditor) spinner.getEditor()).getTextField()
                .setHorizontalAlignment(javax.swing.JTextField.CENTER);

        javax.swing.JLabel unitLabel = new javax.swing.JLabel("天");
        unitLabel.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 14));
        unitLabel.setForeground(new java.awt.Color(31, 41, 55));

        spinnerPanel.add(label);
        spinnerPanel.add(spinner);
        spinnerPanel.add(unitLabel);

        contentPanel.add(descLabel, java.awt.BorderLayout.NORTH);
        contentPanel.add(spinnerPanel, java.awt.BorderLayout.CENTER);

        // ============ 按鈕區域 ============
        JPanel buttonPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(new java.awt.Color(248, 249, 250));

        // 確定按鈕
        javax.swing.JButton okButton = new javax.swing.JButton("確定");
        okButton.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 14));
        okButton.setForeground(java.awt.Color.WHITE);
        okButton.setBackground(new java.awt.Color(37, 99, 235));
        okButton.setPreferredSize(new java.awt.Dimension(120, 42));
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        okButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        okButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                okButton.setBackground(new java.awt.Color(29, 78, 216));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                okButton.setBackground(new java.awt.Color(37, 99, 235));
            }
        });

        okButton.addActionListener(e -> {
            int days = (Integer) spinner.getValue();
            appSettings.setExpiryReminderDays(excelService.getFilePath(), days);
            updateExpiryReminderLabel();
            da.api.util.LogManager.getInstance().info("使用者設定到期提醒天數：" + days + " 天");
            dialog.dispose();
            JOptionPane.showMessageDialog(frameElement, "設定已更新！到期提醒天數設為 " + days + " 天");
            dataTable.repaint();
        });

        // 取消按鈕
        javax.swing.JButton cancelButton = new javax.swing.JButton("取消");
        cancelButton.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 14));
        cancelButton.setForeground(new java.awt.Color(55, 65, 81));
        cancelButton.setBackground(new java.awt.Color(229, 231, 235));
        cancelButton.setPreferredSize(new java.awt.Dimension(120, 42));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                cancelButton.setBackground(new java.awt.Color(209, 213, 219));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                cancelButton.setBackground(new java.awt.Color(229, 231, 235));
            }
        });

        cancelButton.addActionListener(e -> {
            da.api.util.LogManager.getInstance().info("使用者取消設定到期提醒天數");
            dialog.dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        // 組合面板
        mainContainer.add(headerPanel, java.awt.BorderLayout.NORTH);
        mainContainer.add(contentPanel, java.awt.BorderLayout.CENTER);
        mainContainer.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        dialog.add(mainContainer);
        dialog.setVisible(true);
    }

    /**
     * 監聽器：強制彈出選單水平擴展以適應內容寬度
     */
    private class WidePopupMenuListener implements javax.swing.event.PopupMenuListener {
        @Override
        public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
            javax.swing.JComboBox<?> box = (javax.swing.JComboBox<?>) e.getSource();
            Object comp = box.getUI().getAccessibleChild(box, 0);
            if (comp instanceof javax.swing.JPopupMenu) {
                javax.swing.JPopupMenu popup = (javax.swing.JPopupMenu) comp;
                javax.swing.JScrollPane scrollPane = null;
                for (java.awt.Component c : popup.getComponents()) {
                    if (c instanceof javax.swing.JScrollPane) {
                        scrollPane = (javax.swing.JScrollPane) c;
                        break;
                    }
                }
                if (scrollPane != null) {
                    java.awt.Dimension size = popup.getPreferredSize();
                    // 從 0 開始避免累加增長
                    int contentWidth = 0;

                    // 遍歷項目找出最大寬度
                    javax.swing.ListCellRenderer renderer = box.getRenderer();
                    // 使用原始型別避免在此處的泛型問題
                    for (int i = 0; i < box.getItemCount(); i++) {
                        Object value = box.getItemAt(i);
                        java.awt.Component c = renderer.getListCellRendererComponent(new javax.swing.JList(), value, i,
                                false, false);
                        contentWidth = Math.max(contentWidth, c.getPreferredSize().width);
                    }

                    // 增加捲動條的內距
                    contentWidth += 20;

                    // 最終寬度取內容與下拉框本體的最大值
                    int finalWidth = Math.max(box.getWidth(), contentWidth);

                    // 僅在必要時調整，但重設首選大小較安全以防卡住
                    popup.setPreferredSize(new java.awt.Dimension(finalWidth, size.height));

                    // 確保版面配置已處理
                    if (!(popup.getLayout() instanceof java.awt.BorderLayout)) {
                        popup.setLayout(new java.awt.BorderLayout());
                        popup.add(scrollPane, java.awt.BorderLayout.CENTER);
                    }
                }
            }
        }

        @Override
        public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
        }

        @Override
        public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
        }
    }
}
