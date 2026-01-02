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

import da.api.model.ApiKeyData;
import da.api.service.ExcelService;
import da.api.view.DataEditDialog;
import da.api.view.MainFrameView;

public class Panel {

    private MainFrameView frameElement;
    private JTable dataTable;
    private ExcelService excelService;
    private List<ApiKeyData> currentData;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private javax.swing.JComboBox<String> cloudComboBox;
    private javax.swing.JComboBox<String> apidComboBox;
    private javax.swing.JComboBox<String> envComboBox;
    private List<ApiKeyData> allData;

    private da.api.model.ColumnConfig columnConfig;
    private JPanel filtersPanel;
    private java.util.Map<String, javax.swing.JComboBox<String>> dynamicComboBoxes = new java.util.HashMap<>();

    public Panel(MainFrameView frameElement, ExcelService excelService) {
        this(frameElement, excelService, null);
    }

    public Panel(MainFrameView frameElement, ExcelService excelService, da.api.model.ColumnConfig columnConfig) {
        this.frameElement = frameElement;
        this.excelService = excelService;
        this.columnConfig = columnConfig;

        JPanel jPanelMain = new JPanel();
        jPanelMain.setLayout(new BoxLayout(jPanelMain, BoxLayout.Y_AXIS));
        jPanelMain.setBackground(new java.awt.Color(245, 247, 250)); // Light gray background for contrast
        // Add global padding for a cleaner look
        jPanelMain.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Removed panelSearchAreaTitle as requested
        jPanelMain.add(this.panelSearchArea());
        jPanelMain.add(javax.swing.Box.createVerticalStrut(15)); // Add spacing
        jPanelMain.add(this.panelDataManagement());
        jPanelMain.add(javax.swing.Box.createVerticalStrut(10)); // Add spacing
        jPanelMain.add(this.panelTable());

        frameElement.add(jPanelMain);

        // 載入資料
        refreshData();
    }

    private JPanel panelSearchArea() {
        JPanel mainPanel = new JPanel(new java.awt.BorderLayout());
        mainPanel.setBackground(new java.awt.Color(255, 255, 255));
        // Outer margin removed since main panel handles it, but keep small bottom
        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 5, 0));

        // Toggle Button (Header)
        JButton toggleButton = new JButton("\u25BC 搜尋過濾器");
        // Use Dialog font which usually has better Unicode symbol support than CJK
        // fonts
        toggleButton.setFont(new java.awt.Font(java.awt.Font.DIALOG, java.awt.Font.BOLD, 16));
        toggleButton.setForeground(new java.awt.Color(66, 133, 244)); // Google Blue
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setFocusPainted(false);
        toggleButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        toggleButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Content Panel (Holds Filters + Action)
        JPanel contentPanel = new JPanel(new java.awt.BorderLayout(20, 0));
        contentPanel.setBackground(java.awt.Color.WHITE);
        // Inner styling (Line border instead of Titled)
        contentPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240), 1, true),
                javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15) // inner padding
        ));

        // --- Filters Panel (Left/Center) ---
        filtersPanel = new JPanel(new java.awt.GridBagLayout());
        filtersPanel.setBackground(new java.awt.Color(255, 255, 255));

        rebuildFiltersPanel();

        // --- Action Button Panel (Right) ---
        JPanel actionPanel = new JPanel(new java.awt.GridBagLayout());
        actionPanel.setBackground(new java.awt.Color(255, 255, 255));
        // Add a separation line on the left
        actionPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 1, 0, 0, new java.awt.Color(230, 230, 240)),
                javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 0)));

        JButton searchButton = createSearchButton();
        // Make the button larger and distinct
        searchButton.setPreferredSize(new java.awt.Dimension(100, 45));
        searchButton.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 16));

        actionPanel.add(searchButton);

        // Add panels to content panel
        contentPanel.add(filtersPanel, java.awt.BorderLayout.CENTER);
        contentPanel.add(actionPanel, java.awt.BorderLayout.EAST);

        // Toggle Logic
        toggleButton.addActionListener(e -> {
            boolean visible = contentPanel.isVisible();
            contentPanel.setVisible(!visible);
            toggleButton.setText(visible ? "\u25B6 搜尋過濾器" : "\u25BC 搜尋過濾器");
        });

        // Add to main panel
        mainPanel.add(toggleButton, java.awt.BorderLayout.NORTH);
        mainPanel.add(contentPanel, java.awt.BorderLayout.CENTER);

        return mainPanel;
    }

    private void rebuildFiltersPanel() {
        filtersPanel.removeAll();
        dynamicComboBoxes.clear(); // Clear old references

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 10, 10, 15); // Formatting
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

        java.awt.Font labelFont = new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 14);
        java.awt.Font comboFont = new java.awt.Font("微軟正黑體", java.awt.Font.PLAIN, 14);
        java.awt.Color labelColor = new java.awt.Color(60, 60, 60);

        if (columnConfig != null) {
            // Dynamic Generation
            List<String> searchColumns = columnConfig.getSearchFilterColumns();
            int gridX = 0;
            int gridY = 0;

            for (String colName : searchColumns) {
                // Label
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
                comboBox.setMinimumSize(new java.awt.Dimension(100, 35)); // Allow shrinking
                comboBox.setPreferredSize(new java.awt.Dimension(150, 35)); // Default size

                // Note: WidePopupMenuListener is added in updateSearchOptions for dynamic
                // combos

                dynamicComboBoxes.put(colName, comboBox);

                gbc.gridx = gridX + 1;
                gbc.weightx = 0.5; // Allow expansion
                filtersPanel.add(comboBox, gbc);

                gridX += 2;
                // Wrap after 3 pairs or if we have many
                if (gridX >= 6) {
                    gridX = 0;
                    gridY++;
                }
            }
        } else {
            // Legacy Logic
            Label jLabelElement = new Label();
            ComboBox comboBoxElement = new ComboBox();

            cloudComboBox = comboBoxElement.comboBoxCloudType();
            apidComboBox = comboBoxElement.comboBoxApidtype();
            envComboBox = comboBoxElement.comboBoxEnvtype();

            cloudComboBox.setFont(comboFont);
            cloudComboBox.setPreferredSize(new java.awt.Dimension(150, 35));
            // cloudComboBox.addPopupMenuListener(new WidePopupMenuListener());

            apidComboBox.setFont(comboFont);
            apidComboBox.setPreferredSize(new java.awt.Dimension(150, 35));
            // apidComboBox.addPopupMenuListener(new WidePopupMenuListener());

            envComboBox.setFont(comboFont);
            envComboBox.setPreferredSize(new java.awt.Dimension(150, 35));
            // envComboBox.addPopupMenuListener(new WidePopupMenuListener());

            javax.swing.JLabel cloudLabel = jLabelElement.labelCloudeType();
            javax.swing.JLabel apidLabel = jLabelElement.labelApidType();
            javax.swing.JLabel envLabel = jLabelElement.labelEnvType();

            javax.swing.JLabel[] labels = { cloudLabel, apidLabel, envLabel };
            for (javax.swing.JLabel lbl : labels) {
                lbl.setFont(labelFont);
                lbl.setForeground(labelColor);
            }

            // Layout Legacy
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.0;
            filtersPanel.add(cloudLabel, gbc);
            gbc.gridx = 1;
            gbc.weightx = 0.5;
            filtersPanel.add(cloudComboBox, gbc);

            gbc.gridx = 2;
            gbc.weightx = 0.0;
            filtersPanel.add(apidLabel, gbc);
            gbc.gridx = 3;
            gbc.weightx = 0.5;
            filtersPanel.add(apidComboBox, gbc);

            gbc.gridx = 4;
            gbc.weightx = 0.0;
            filtersPanel.add(envLabel, gbc);
            gbc.gridx = 5;
            gbc.weightx = 0.5;
            filtersPanel.add(envComboBox, gbc);
        }

        filtersPanel.revalidate();
        filtersPanel.repaint();
    }

    private JButton createSearchButton() {
        JButton searchButton = new JButton("查詢");
        searchButton.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 14));
        searchButton.setBackground(new java.awt.Color(66, 133, 244)); // Google Blue
        searchButton.setForeground(java.awt.Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 15, 5, 15));
        searchButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        searchButton.addActionListener(e -> performSearch());
        return searchButton;
    }

    public void openSettings() {
        List<String> headers = excelService.readHeaders();
        // Use full constructor to pre-fill
        da.api.view.ColumnConfigDialog dialog = new da.api.view.ColumnConfigDialog(headers, columnConfig);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            da.api.model.ColumnConfig newConfig = dialog.getConfig();
            this.columnConfig = newConfig;
            this.excelService.setColumnConfig(newConfig);

            // Save to settings
            da.api.util.AppSettings settings = new da.api.util.AppSettings();
            settings.saveColumnConfig(excelService.getFilePath(), newConfig);

            rebuildFiltersPanel();
            refreshData();
            JOptionPane.showMessageDialog(frameElement, "設定已更新!");
        }
    }

    private JPanel panelDataManagement() {
        JPanel managementPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        managementPanel.setBackground(new java.awt.Color(245, 247, 250)); // Match main background

        java.awt.Font btnFont = new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 14);

        // 新增按鈕
        addButton = new JButton("新增");
        addButton.setFont(btnFont);
        addButton.setBackground(new java.awt.Color(52, 168, 83)); // Google Green
        addButton.setForeground(java.awt.Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> addData());

        // 編輯按鈕
        editButton = new JButton("編輯");
        editButton.setFont(btnFont);
        editButton.setBackground(new java.awt.Color(241, 196, 15)); // Warm Yellow/Orange
        editButton.setForeground(java.awt.Color.WHITE);
        editButton.setFocusPainted(false);
        editButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        editButton.addActionListener(e -> editData());

        // 刪除按鈕
        deleteButton = new JButton("刪除");
        deleteButton.setFont(btnFont);
        deleteButton.setBackground(new java.awt.Color(234, 67, 53)); // Google Red
        deleteButton.setForeground(java.awt.Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        deleteButton.addActionListener(e -> deleteData());

        managementPanel.add(addButton);
        managementPanel.add(editButton);
        managementPanel.add(deleteButton);

        // 根據權限設定按鈕可見性
        boolean isAdmin = frameElement.getUserService().isAdmin();
        addButton.setVisible(isAdmin);
        editButton.setVisible(isAdmin);
        deleteButton.setVisible(isAdmin);

        return managementPanel;
    }

    private JPanel panelTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        // Add style to match search area
        tablePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(226, 232, 240), 1, true));
        tablePanel.setBackground(java.awt.Color.WHITE);

        // 建立表格
        String[] columnNames = { "序號", "APID", "雲", "環境", "到期日", "API KEY", "申請單號" };
        dataTable = new JTable(new Object[0][7], columnNames);
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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

        tablePanel.add(scrollPane, BorderLayout.CENTER);

        return tablePanel;
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

            // Extract unique values
            java.util.Set<String> values = new java.util.TreeSet<>();
            for (ApiKeyData data : allData) {
                String val = data.getAttribute(colName);
                if (val != null && !val.trim().isEmpty()) {
                    values.add(val.trim());
                }
            }

            List<String> fullList = new ArrayList<>();
            fullList.add("ALL");
            fullList.addAll(values);

            // Reset model with full list initially
            comboBox.setModel(new javax.swing.DefaultComboBoxModel<>(fullList.toArray(new String[0])));

            // Fix size stability
            comboBox.setPrototypeDisplayValue("XXXXXXXXXXXXXXXXXXXX"); // Stable width reference

            // Make searchable
            setupSearchableComboBox(comboBox, fullList);

            // Re-enable WidePopupMenuListener to fix truncation for long items
            // Remove existing listeners first to avoid duplicates if any (though unlikely
            // here as we rebuild)
            for (javax.swing.event.PopupMenuListener l : comboBox.getPopupMenuListeners()) {
                if (l instanceof WidePopupMenuListener)
                    comboBox.removePopupMenuListener(l);
            }
            comboBox.addPopupMenuListener(new WidePopupMenuListener());

            // Restore selection if possible
            if (selected != null) {
                // Check if selected item exists (fuzzy check not needed as we just reloaded
                // from data)
                if ("ALL".equals(selected) || values.contains(selected)) {
                    comboBox.setSelectedItem(selected);
                }
            }
        }
    }

    private void setupSearchableComboBox(javax.swing.JComboBox<String> comboBox, List<String> items) {
        comboBox.setEditable(true);

        // Get Editor Component
        javax.swing.JTextField editor = (javax.swing.JTextField) comboBox.getEditor().getEditorComponent();

        // Check if we already have a listener
        SearchableKeyAdapter listener = (SearchableKeyAdapter) comboBox.getClientProperty("SearchableKeyAdapter");
        if (listener == null) {
            listener = new SearchableKeyAdapter(comboBox, items);
            editor.addKeyListener(listener);
            comboBox.putClientProperty("SearchableKeyAdapter", listener);
        } else {
            // Update items
            listener.setItems(items);
        }
    }

    private class SearchableKeyAdapter extends java.awt.event.KeyAdapter {
        private final javax.swing.JComboBox<String> comboBox;
        private java.util.List<String> items;
        private boolean isFiltering = false;

        public SearchableKeyAdapter(javax.swing.JComboBox<String> comboBox, java.util.List<String> items) {
            this.comboBox = comboBox;
            this.items = new ArrayList<>(items);
        }

        public void setItems(java.util.List<String> items) {
            this.items = new ArrayList<>(items);
        }

        @Override
        public void keyReleased(java.awt.event.KeyEvent e) {
            // Ignore navigation keys
            if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN ||
                    e.getKeyCode() == java.awt.event.KeyEvent.VK_UP ||
                    e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                return;
            }

            javax.swing.SwingUtilities.invokeLater(() -> {
                isFiltering = true;
                javax.swing.JTextField textField = (javax.swing.JTextField) comboBox.getEditor().getEditorComponent();
                String text = textField.getText();
                int caretPosition = textField.getCaretPosition();

                // Filter items
                List<String> filtered = new ArrayList<>();
                for (String item : items) {
                    if (item.toLowerCase().contains(text.toLowerCase())) {
                        filtered.add(item);
                    }
                }

                // Update Model
                javax.swing.DefaultComboBoxModel<String> model = new javax.swing.DefaultComboBoxModel<>(
                        filtered.toArray(new String[0]));
                comboBox.setModel(model);

                // Restore text and caret
                textField.setText(text);
                try {
                    textField.setCaretPosition(caretPosition);
                } catch (Exception ex) {
                    // Ignore caret errors
                }

                // Handle Popup
                if (!filtered.isEmpty()) {
                    comboBox.showPopup();
                    // Prevent auto-selection of first item blocking typing
                    if (comboBox.getItemCount() > 0) {
                        // comboBox.setSelectedIndex(0); // Optional: select first match? No, interrupts
                        // typing.
                    }
                } else {
                    comboBox.hidePopup();
                }
                isFiltering = false;
            });
        }
    }

    private void performSearch() {
        if (columnConfig != null) {
            List<String> searchColumns = columnConfig.getSearchFilterColumns();

            // Log search conditions
            StringBuilder sb = new StringBuilder("查詢條件 - ");
            for (String col : searchColumns) {
                // Get selected item from editor for editable combo boxes to ensure text match
                javax.swing.JComboBox<String> cb = dynamicComboBoxes.get(col);
                Object selectedObj = cb.getSelectedItem();

                // For editable combo box, sometimes getSelectedItem is null or not updated if
                // text is free-form
                // But for search filter, we usually want it.
                // If text field has content, take it?
                String selected = (selectedObj != null) ? selectedObj.toString() : "ALL";

                sb.append(col).append(": ").append(selected).append(", ");
            }
            System.out.println(sb.toString());

            currentData = new ArrayList<>();
            for (ApiKeyData data : allData) {
                boolean match = true;
                for (String col : searchColumns) {
                    javax.swing.JComboBox<String> cb = dynamicComboBoxes.get(col);
                    Object selectedObj = cb.getSelectedItem();
                    // Fallback to text editor content if selection is null but text exists?
                    // Usually getSelectedItem() reflects the text in editable CB.

                    String selected = (selectedObj != null) ? selectedObj.toString() : "ALL";

                    if (selected != null && !"ALL".equals(selected) && !selected.trim().isEmpty()) {
                        String val = data.getAttribute(col);
                        // Exact match or contains? Let's stick to equals for filter values usually.
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
            // Legacy Logic
            String cloudType = (String) cloudComboBox.getSelectedItem();
            String apiType = (String) apidComboBox.getSelectedItem();
            String envType = (String) envComboBox.getSelectedItem();

            System.out.println("查詢條件 - 雲: " + cloudType + ", APID: " + apiType + ", 環境: " + envType);

            // 篩選資料
            currentData = new ArrayList<>();
            for (ApiKeyData data : allData) {
                boolean match = true;

                // 如果選擇了特定條件才進行篩選 (排除 "ALL" 選項)
                if (cloudType != null && !"ALL".equals(cloudType)) {
                    if (data.getCloud() == null || !data.getCloud().equals(cloudType)) {
                        match = false;
                    }
                }
                if (apiType != null && !"ALL".equals(apiType)) {
                    if (data.getApid() == null || !data.getApid().equals(apiType)) {
                        match = false;
                    }
                }
                if (envType != null && !"ALL".equals(envType)) {
                    if (data.getEnvironment() == null || !data.getEnvironment().equals(envType)) {
                        match = false;
                    }
                }

                if (match) {
                    currentData.add(data);
                }
            }
        }

        updateTable();
        JOptionPane.showMessageDialog(frameElement,
                "找到 " + currentData.size() + " 筆資料",
                "查詢結果",
                JOptionPane.INFORMATION_MESSAGE);
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
            // Legacy
            columnNames = new String[] { "序號", "APID", "雲", "環境", "到期日", "API KEY", "申請單號" };
            data = new Object[currentData.size()][7];
            for (int i = 0; i < currentData.size(); i++) {
                data[i] = currentData.get(i).toArray(i + 1);
            }
        }

        dataTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可直接編輯
            }
        });

        // Basic column width adjustment
        if (dataTable.getColumnCount() > 0) {
            dataTable.getColumnModel().getColumn(0).setPreferredWidth(50); // 序號
        }

        if (columnConfig == null) {
            // Legacy widths
            if (dataTable.getColumnCount() >= 7) {
                dataTable.getColumnModel().getColumn(1).setPreferredWidth(120); // APID
                dataTable.getColumnModel().getColumn(2).setPreferredWidth(80); // 雲
                dataTable.getColumnModel().getColumn(3).setPreferredWidth(60); // 環境
                dataTable.getColumnModel().getColumn(4).setPreferredWidth(100); // 到期日
                dataTable.getColumnModel().getColumn(5).setPreferredWidth(250); // API KEY
                dataTable.getColumnModel().getColumn(6).setPreferredWidth(90); // 申請單號
            }
        }
    }

    private void addData() {
        DataEditDialog dialog = new DataEditDialog(frameElement, null, true, columnConfig);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            currentData.add(dialog.getResult());
            if (excelService.saveAllData(currentData)) {
                refreshData();
                JOptionPane.showMessageDialog(frameElement, "新增成功!");
            } else {
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

        ApiKeyData selectedData = currentData.get(selectedRow);
        DataEditDialog dialog = new DataEditDialog(frameElement, selectedData, false, columnConfig);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            currentData.set(selectedRow, dialog.getResult());
            if (excelService.saveAllData(currentData)) {
                refreshData();
                JOptionPane.showMessageDialog(frameElement, "編輯成功!");
            } else {
                JOptionPane.showMessageDialog(frameElement,
                        "編輯失敗!", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteData() {
        int selectedRow = dataTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frameElement,
                    "請先選擇要刪除的資料!", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(frameElement,
                "確定要刪除這筆資料嗎?", "確認刪除",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            currentData.remove(selectedRow);
            if (excelService.saveAllData(currentData)) {
                refreshData();
                JOptionPane.showMessageDialog(frameElement, "刪除成功!");
            } else {
                JOptionPane.showMessageDialog(frameElement,
                        "刪除失敗!", "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Listener to force popup menu to expand horizontally to fit content
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
                    // Start with 0 to avoid cumulative growth
                    int contentWidth = 0;

                    // Iterate items to find max width
                    javax.swing.ListCellRenderer renderer = box.getRenderer();
                    // Use raw type to avoid generic issues in this context
                    for (int i = 0; i < box.getItemCount(); i++) {
                        Object value = box.getItemAt(i);
                        java.awt.Component c = renderer.getListCellRendererComponent(new javax.swing.JList(), value, i,
                                false, false);
                        contentWidth = Math.max(contentWidth, c.getPreferredSize().width);
                    }

                    // Add padding for scrollbar
                    contentWidth += 20;

                    // Final width is max of content or box itself
                    int finalWidth = Math.max(box.getWidth(), contentWidth);

                    // Only adjust if strictly necessary or different, but resetting preferred size
                    // is safer to prevent stickiness
                    popup.setPreferredSize(new java.awt.Dimension(finalWidth, size.height));

                    // Ensure layout is handled if not already
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