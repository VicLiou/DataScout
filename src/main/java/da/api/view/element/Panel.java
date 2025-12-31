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

    public Panel(MainFrameView frameElement, ExcelService excelService) {
        this.frameElement = frameElement;
        this.excelService = excelService;

        JPanel jPanelMain = new JPanel();
        jPanelMain.setLayout(new BoxLayout(jPanelMain, BoxLayout.Y_AXIS));

        jPanelMain.add(this.panelSearchAreaTitle());
        jPanelMain.add(this.panelSearchArea());
        jPanelMain.add(this.panelDataManagement());
        jPanelMain.add(this.panelTable());

        frameElement.add(jPanelMain);

        // 載入資料
        refreshData();
    }

    private JPanel panelSearchAreaTitle() {
        Label jLabelElement = new Label();
        javax.swing.JLabel titleLabel = jLabelElement.labelSearchTitle();
        titleLabel.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 24));
        titleLabel.setForeground(new java.awt.Color(50, 50, 50));

        JPanel searchAreaTitlePanel = new JPanel();
        searchAreaTitlePanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 0, 15, 0));
        searchAreaTitlePanel.add(titleLabel);

        return searchAreaTitlePanel;
    }

    private JPanel panelSearchArea() {
        JPanel searchAreaJPanel = new JPanel();
        searchAreaJPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createTitledBorder(
                        javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200)),
                        "搜尋過濾器",
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        new java.awt.Font("微軟正黑體", java.awt.Font.PLAIN, 14),
                        new java.awt.Color(100, 100, 100)),
                javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        // 初始化相關元件
        Label jLabelElement = new Label();
        ComboBox comboBoxElement = new ComboBox();

        // 儲存 ComboBox 實例供查詢使用
        cloudComboBox = comboBoxElement.comboBoxCloudType();
        apidComboBox = comboBoxElement.comboBoxApidtype();
        envComboBox = comboBoxElement.comboBoxEnvtype();

        // 設定字型
        java.awt.Font labelFont = new java.awt.Font("微軟正黑體", java.awt.Font.PLAIN, 14);
        javax.swing.JLabel cloudLabel = jLabelElement.labelCloudeType();
        javax.swing.JLabel apidLabel = jLabelElement.labelApidType();
        javax.swing.JLabel envLabel = jLabelElement.labelEnvType();

        cloudLabel.setFont(labelFont);
        apidLabel.setFont(labelFont);
        envLabel.setFont(labelFont);

        // 美化下拉選單
        java.awt.Font comboFont = new java.awt.Font("微軟正黑體", java.awt.Font.PLAIN, 14);
        cloudComboBox.setFont(comboFont);
        apidComboBox.setFont(comboFont);
        envComboBox.setFont(comboFont);

        // 建立查詢按鈕
        JButton searchButton = new JButton("查詢");
        searchButton.setFont(new java.awt.Font("微軟正黑體", java.awt.Font.BOLD, 14));
        searchButton.setBackground(new java.awt.Color(33, 150, 243)); // Android Blue
        searchButton.setForeground(java.awt.Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 15, 5, 15));
        searchButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        searchButton.addActionListener(e -> performSearch());

        // 使用 GridBagLayout 進行排版
        searchAreaJPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 15); // 元件間距
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.fill = java.awt.GridBagConstraints.NONE;

        // 第一個項目: 雲類型
        gbc.gridx = 0;
        gbc.gridy = 0;
        searchAreaJPanel.add(cloudLabel, gbc);
        gbc.gridx = 1;
        searchAreaJPanel.add(cloudComboBox, gbc);

        // 第二個項目: APID
        gbc.gridx = 2;
        searchAreaJPanel.add(apidLabel, gbc);
        gbc.gridx = 3;
        searchAreaJPanel.add(apidComboBox, gbc);

        // 第三個項目: 環境
        gbc.gridx = 4;
        searchAreaJPanel.add(envLabel, gbc);
        gbc.gridx = 5;
        searchAreaJPanel.add(envComboBox, gbc);

        // 查詢按鈕 (放在最後，稍微推開一點)
        gbc.gridx = 6;
        gbc.weightx = 1.0; // 佔用剩餘空間
        gbc.insets = new java.awt.Insets(5, 20, 5, 5);
        searchAreaJPanel.add(searchButton, gbc);

        return searchAreaJPanel;
    }

    private JPanel panelDataManagement() {
        JPanel managementPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // 新增按鈕
        addButton = new JButton("新增");
        addButton.addActionListener(e -> addData());

        // 編輯按鈕
        editButton = new JButton("編輯");
        editButton.addActionListener(e -> editData());

        // 刪除按鈕
        deleteButton = new JButton("刪除");
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
        updateTable();
    }

    private void performSearch() {
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

        updateTable();
        JOptionPane.showMessageDialog(frameElement,
                "找到 " + currentData.size() + " 筆資料",
                "查詢結果",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateTable() {
        String[] columnNames = { "序號", "APID", "雲", "環境", "到期日", "API KEY", "申請單號" };
        Object[][] data = new Object[currentData.size()][7];

        for (int i = 0; i < currentData.size(); i++) {
            data[i] = currentData.get(i).toArray(i + 1);
        }

        dataTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可直接編輯
            }
        });

        // 更新後重新設定欄位寬度
        dataTable.getColumnModel().getColumn(0).setPreferredWidth(50); // 序號
        dataTable.getColumnModel().getColumn(1).setPreferredWidth(120); // APID
        dataTable.getColumnModel().getColumn(2).setPreferredWidth(80); // 雲
        dataTable.getColumnModel().getColumn(3).setPreferredWidth(60); // 環境
        dataTable.getColumnModel().getColumn(4).setPreferredWidth(100); // 到期日
        dataTable.getColumnModel().getColumn(5).setPreferredWidth(250); // API KEY
        dataTable.getColumnModel().getColumn(6).setPreferredWidth(90); // 申請單號
    }

    private void addData() {
        DataEditDialog dialog = new DataEditDialog(frameElement, null, true);
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
        DataEditDialog dialog = new DataEditDialog(frameElement, selectedData, false);
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
}