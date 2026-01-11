package da.api.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

import da.api.model.ExcelData;
import da.api.model.ColumnConfig;

/**
 * 資料編輯對話框
 */
public class DataEditDialog extends JDialog {
    private static final String FONT_NAME = "微軟正黑體";
    private static final Color BG_COLOR = new Color(248, 249, 250);

    // 動態欄位
    private Map<String, JComponent> dynamicFields;
    private ColumnConfig columnConfig;

    private ExcelData result;
    private boolean confirmed = false;

    public DataEditDialog(JFrame parent, ExcelData data, boolean isNew, ColumnConfig config) {
        super(parent, isNew ? "新增資料" : "編輯資料", true);
        this.columnConfig = config;

        initComponentsDynamic(isNew);

        if (data != null) {
            loadDataDynamic(data);
        }

        setSize(550, 600);
        setMinimumSize(new Dimension(500, 400));

        // 設定圖示
        try {
            java.net.URL iconUrl = getClass().getResource("/app_icon.png");
            if (iconUrl != null) {
                setIconImage(new javax.swing.ImageIcon(iconUrl).getImage());
            }
        } catch (Exception e) {
            // 忽略
        }

        setLocationRelativeTo(parent);
    }

    private void initComponentsDynamic(boolean isNew) {
        dynamicFields = new HashMap<>();

        // 主容器
        JPanel mainContainer = new JPanel(new BorderLayout(0, 20));
        mainContainer.setBackground(BG_COLOR);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // 標題區域
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        Color headerBg = isNew ? new Color(224, 242, 254) : new Color(254, 243, 199);
        Color headerBorder = isNew ? new Color(59, 130, 246) : new Color(251, 191, 36);
        Color headerText = isNew ? new Color(30, 64, 175) : new Color(146, 64, 14);

        headerPanel.setBackground(headerBg);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(headerBorder, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel titleLabel = new JLabel(isNew ? "新增資料" : "編輯資料");
        titleLabel.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        titleLabel.setForeground(headerText);
        headerPanel.add(titleLabel);

        // 表單區域
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        String expiryCol = columnConfig.getExpiryDateColumn();

        for (String header : columnConfig.getAllHeaders()) {
            // 標籤
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.0;
            gbc.anchor = GridBagConstraints.WEST;

            JLabel label = new JLabel(header + "：");
            label.setFont(new Font(FONT_NAME, Font.BOLD, 13));
            label.setForeground(new Color(55, 65, 81));
            formPanel.add(label, gbc);

            // 輸入欄位
            gbc.gridx = 1;
            gbc.weightx = 1.0;

            JComponent field;
            if (header.equals(expiryCol)) {
                SpinnerDateModel dateModel = new SpinnerDateModel();
                JSpinner spinner = new JSpinner(dateModel);
                JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
                spinner.setEditor(dateEditor);
                spinner.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
                spinner.setPreferredSize(new Dimension(0, 35));
                field = spinner;
            } else {
                JTextField textField = new JTextField(20);
                textField.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
                textField.setPreferredSize(new Dimension(0, 35));
                textField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)));
                field = textField;
            }

            dynamicFields.put(header, field);
            formPanel.add(field, gbc);
            row++;
        }

        // 包裝在捲動面板中
        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.setBackground(Color.WHITE);
        scrollWrapper.add(formPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(scrollWrapper);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 按鈕區域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(BG_COLOR);

        // 儲存按鈕
        JButton saveButton = new JButton("儲存");
        styleButton(saveButton, new Color(37, 99, 235), Color.WHITE, 120, 42);
        saveButton.addActionListener(e -> saveDataDynamic());

        // 取消按鈕
        JButton cancelButton = new JButton("取消");
        styleButton(cancelButton, new Color(229, 231, 235), new Color(55, 65, 81), 120, 42);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // 組合面板
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(scrollPane, BorderLayout.CENTER);
        mainContainer.add(buttonPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(mainContainer, BorderLayout.CENTER);
    }

    private void loadDataDynamic(ExcelData data) {
        String expiryCol = columnConfig.getExpiryDateColumn();

        for (String header : columnConfig.getAllHeaders()) {
            JComponent field = dynamicFields.get(header);
            if (field == null)
                continue;

            if (header.equals(expiryCol)) {
                // 對於到期日，優先使用已填入的欄位
                if (data.getExpiryDate() != null) {
                    Date date = Date.from(data.getExpiryDate()
                            .atStartOfDay(ZoneId.systemDefault()).toInstant());
                    ((JSpinner) field).setValue(date);
                }
            } else {
                String value = data.getAttribute(header);
                if (value == null)
                    value = "";
                ((JTextField) field).setText(value);
            }
        }
    }

    private void saveDataDynamic() {
        result = new ExcelData();
        String expiryCol = columnConfig.getExpiryDateColumn();

        for (String header : columnConfig.getAllHeaders()) {
            JComponent field = dynamicFields.get(header);
            if (field instanceof JSpinner spinner) {
                Date date = (Date) spinner.getValue();
                if (header.equals(expiryCol)) {
                    // 將日期轉換為 LocalDate 並設置到 expiryDate 欄位
                    java.time.LocalDate localDate = date.toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                    result.setExpiryDate(localDate);

                    // 同時將格式化的日期字串存入 attributes，以便顯示
                    result.setAttribute(header, localDate.toString());
                }
            } else if (field instanceof JTextField textField) {
                String value = textField.getText().trim();
                result.setAttribute(header, value);
            }
        }

        confirmed = true;
        dispose();
    }

    public ExcelData getResult() {
        return result;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * 美化按鈕
     */
    private void styleButton(JButton button, Color bgColor, Color fgColor, int width, int height) {
        button.setFont(new Font(FONT_NAME, Font.BOLD, 14));
        button.setForeground(fgColor);
        button.setBackground(bgColor);
        button.setPreferredSize(new Dimension(width, height));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover 效果
        Color originalBg = bgColor;
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
    }

    /**
     * 加深顏色
     */
    private Color darkenColor(Color color, double factor) {
        return new Color(
                Math.max((int) (color.getRed() * factor), 0),
                Math.max((int) (color.getGreen() * factor), 0),
                Math.max((int) (color.getBlue() * factor), 0));
    }
}
