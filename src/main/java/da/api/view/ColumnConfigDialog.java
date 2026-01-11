package da.api.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import da.api.model.ColumnConfig;

public class ColumnConfigDialog extends JDialog {
    private List<String> headers;
    private ColumnConfig config;
    private JComboBox<String> expiryDateComboBox;
    private List<JCheckBox> searchFilterCheckBoxes;
    private boolean confirmed = false;

    public ColumnConfigDialog(List<String> headers, ColumnConfig existingConfig) {
        this.headers = headers;
        this.config = existingConfig;
        setModal(true);
        setTitle("欄位設定");
        setSize(480, 600);
        setLocationRelativeTo(null);

        // 隱藏圖示
        try {
            java.net.URL iconUrl = getClass().getResource("/app_icon.png");
            if (iconUrl != null) {
                setIconImage(new javax.swing.ImageIcon(iconUrl).getImage());
            }
        } catch (Exception e) {
            // 忽略
        }

        initComponents(existingConfig);
    }

    // 為了相容性的預設建構子
    public ColumnConfigDialog(List<String> headers) {
        this(headers, null);
    }

    private void initComponents(ColumnConfig existingConfig) {
        // 主容器
        JPanel mainContainer = new JPanel(new BorderLayout(0, 15));
        mainContainer.setBackground(new Color(248, 249, 250));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ============ 到期日欄位區域 ============
        JPanel expirySection = createSection("設定到期日欄位", new Color(239, 246, 255));

        JPanel expiryContent = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        expiryContent.setBackground(Color.WHITE);
        expiryContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel expiryLabel = new JLabel("選擇欄位：");
        expiryLabel.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
        expiryLabel.setForeground(new Color(55, 65, 81));

        expiryDateComboBox = new JComboBox<>(headers.toArray(new String[0]));
        expiryDateComboBox.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
        expiryDateComboBox.setPreferredSize(new Dimension(250, 36));
        expiryDateComboBox.setBackground(Color.WHITE);
        expiryDateComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        if (existingConfig != null && existingConfig.getExpiryDateColumn() != null) {
            expiryDateComboBox.setSelectedItem(existingConfig.getExpiryDateColumn());
        }

        expiryContent.add(expiryLabel);
        expiryContent.add(expiryDateComboBox);
        expirySection.add(expiryContent, BorderLayout.CENTER);

        // ============ 搜尋過濾欄位區域 ============
        JPanel filterSection = createSection("設定搜尋過濾欄位（多選）", new Color(240, 253, 244));

        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        checkBoxPanel.setBackground(Color.WHITE);
        checkBoxPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        searchFilterCheckBoxes = new ArrayList<>();

        for (String header : headers) {
            JCheckBox checkBox = new JCheckBox(header);
            checkBox.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
            checkBox.setBackground(Color.WHITE);
            checkBox.setForeground(new Color(55, 65, 81));
            checkBox.setFocusPainted(false);

            // 設置更好的間距
            checkBox.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));

            // Hover 效果
            checkBox.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (checkBox.isEnabled()) {
                        checkBox.setBackground(new Color(243, 244, 246));
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    checkBox.setBackground(Color.WHITE);
                }
            });

            if (existingConfig != null && existingConfig.getSearchFilterColumns() != null) {
                if (existingConfig.getSearchFilterColumns().contains(header)) {
                    checkBox.setSelected(true);
                }
            }
            searchFilterCheckBoxes.add(checkBox);
            checkBoxPanel.add(checkBox);
        }

        JScrollPane scrollPane = new JScrollPane(checkBoxPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Color.WHITE);
        filterSection.add(scrollPane, BorderLayout.CENTER);

        // ============ 按鈕區域 ============
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));

        JButton okButton = createStyledButton("確定", new Color(37, 99, 235), Color.WHITE);
        okButton.addActionListener(e -> onOK());

        JButton cancelButton = createStyledButton("取消", new Color(229, 231, 235), new Color(55, 65, 81));
        cancelButton.addActionListener(e -> onCancel());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        // 組合面板
        mainContainer.add(expirySection, BorderLayout.NORTH);
        mainContainer.add(filterSection, BorderLayout.CENTER);
        mainContainer.add(buttonPanel, BorderLayout.SOUTH);

        add(mainContainer);
    }

    /**
     * 創建區域面板
     */
    private JPanel createSection(String title, Color headerBg) {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        // 標題區
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        header.setBackground(headerBg);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微軟正黑體", Font.BOLD, 15));
        titleLabel.setForeground(new Color(31, 41, 55));
        header.add(titleLabel);

        section.add(header, BorderLayout.NORTH);
        return section;
    }

    /**
     * 創建美化按鈕
     */
    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(new Font("微軟正黑體", Font.BOLD, 14));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setPreferredSize(new Dimension(120, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Hover 效果
        Color originalBg = bg;
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (originalBg.equals(new Color(37, 99, 235))) {
                    button.setBackground(new Color(29, 78, 216));
                } else {
                    button.setBackground(new Color(209, 213, 219));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalBg);
            }
        });

        return button;
    }

    private void onOK() {
        String selectedExpiry = (String) expiryDateComboBox.getSelectedItem();
        List<String> selectedFilters = new ArrayList<>();

        for (JCheckBox checkBox : searchFilterCheckBoxes) {
            if (checkBox.isSelected()) {
                selectedFilters.add(checkBox.getText());
            }
        }

        if (selectedExpiry == null) {
            JOptionPane.showMessageDialog(this, "請選擇一個到期日欄位", "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }

        config = new ColumnConfig(selectedExpiry, selectedFilters, headers);
        confirmed = true;
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public ColumnConfig getConfig() {
        return config;
    }
}
