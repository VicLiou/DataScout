package da.api.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import da.api.util.AppSettings;
import da.api.util.AutoStartManager;

/**
 * 設定對話框
 */
public class SettingsDialog extends JDialog {
    private JCheckBox autoStartCheckBox;
    private JCheckBox minimizeToTrayCheckBox;
    private AppSettings appSettings;
    private String filePath;

    public SettingsDialog(JFrame parent, AppSettings appSettings, String filePath) {
        super(parent, "偏好設定", true);
        this.appSettings = appSettings;
        this.filePath = filePath;

        initComponents();
        loadSettings();

        setSize(520, 420);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        // 主容器
        JPanel mainContainer = new JPanel(new BorderLayout(0, 20));
        mainContainer.setBackground(new Color(248, 249, 250));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // ============ 標題區域 ============
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        headerPanel.setBackground(new Color(239, 246, 255));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel titleLabel = new JLabel("系統偏好設定");
        titleLabel.setFont(new Font("微軟正黑體", Font.BOLD, 16));
        titleLabel.setForeground(new Color(30, 64, 175));
        headerPanel.add(titleLabel);

        // ============ 設定區域 ============
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBackground(Color.WHITE);
        settingsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // 開機自動啟動
        JPanel autoStartPanel = createSettingItem();
        autoStartCheckBox = new JCheckBox("開機自動啟動");
        styleCheckBox(autoStartCheckBox);
        autoStartPanel.add(autoStartCheckBox);

        JLabel autoStartDesc = new JLabel("程式將在 Windows 啟動時自動執行");
        autoStartDesc.setFont(new Font("微軟正黑體", Font.PLAIN, 12));
        autoStartDesc.setForeground(new Color(107, 114, 128));
        autoStartDesc.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
        autoStartPanel.add(autoStartDesc);

        // 最小化到系統托盤
        JPanel trayPanel = createSettingItem();
        minimizeToTrayCheckBox = new JCheckBox("關閉視窗時最小化到系統托盤");
        styleCheckBox(minimizeToTrayCheckBox);
        trayPanel.add(minimizeToTrayCheckBox);

        JLabel trayDesc = new JLabel("關閉視窗時程式將繼續在背景執行");
        trayDesc.setFont(new Font("微軟正黑體", Font.PLAIN, 12));
        trayDesc.setForeground(new Color(107, 114, 128));
        trayDesc.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
        trayPanel.add(trayDesc);

        settingsPanel.add(autoStartPanel);
        settingsPanel.add(javax.swing.Box.createVerticalStrut(15));
        settingsPanel.add(trayPanel);

        // ============ 按鈕區域 ============
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));

        JButton saveButton = createStyledButton("儲存", new Color(37, 99, 235), Color.WHITE);
        saveButton.addActionListener(e -> saveSettings());

        JButton cancelButton = createStyledButton("取消", new Color(229, 231, 235), new Color(55, 65, 81));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // 組合面板
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(settingsPanel, BorderLayout.CENTER);
        mainContainer.add(buttonPanel, BorderLayout.SOUTH);

        add(mainContainer);
    }

    /**
     * 創建設定項目面板
     */
    private JPanel createSettingItem() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        return panel;
    }

    /**
     * 美化 CheckBox
     */
    private void styleCheckBox(JCheckBox checkBox) {
        checkBox.setFont(new Font("微軟正黑體", Font.BOLD, 14));
        checkBox.setBackground(Color.WHITE);
        checkBox.setForeground(new Color(31, 41, 55));
        checkBox.setFocusPainted(false);
        checkBox.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        checkBox.setAlignmentX(LEFT_ALIGNMENT);

        // Hover 效果
        checkBox.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (checkBox.isEnabled()) {
                    checkBox.setBackground(new Color(249, 250, 251));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                checkBox.setBackground(Color.WHITE);
            }
        });
    }

    /**
     * 創建美化按鈕
     */
    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(new Font("微軟正黑體", Font.BOLD, 14));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setPreferredSize(new Dimension(120, 42));
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

    private void loadSettings() {
        if (filePath != null) {
            // 使用檔案特定設定
            autoStartCheckBox.setSelected(appSettings.isAutoStart(filePath));
            minimizeToTrayCheckBox.setSelected(appSettings.isMinimizeToTray(filePath));
        } else {
            // 使用全域設定作為回退
            autoStartCheckBox.setSelected(appSettings.isGlobalAutoStart());
            minimizeToTrayCheckBox.setSelected(appSettings.isGlobalMinimizeToTray());
        }
    }

    private void saveSettings() {
        // 儲存設定
        boolean autoStart = autoStartCheckBox.isSelected();
        boolean minimizeToTray = minimizeToTrayCheckBox.isSelected();

        if (filePath != null) {
            // 保存為檔案特定設定
            appSettings.setAutoStart(filePath, autoStart);
            appSettings.setMinimizeToTray(filePath, minimizeToTray);
        } else {
            // 保存為全域設定
            appSettings.setGlobalAutoStart(autoStart);
            appSettings.setGlobalMinimizeToTray(minimizeToTray);
        }

        // 記錄設定變更
        da.api.util.LogManager.getInstance().info(
                String.format("使用者儲存偏好設定: [開機自動啟動: %b, 最小化到托盤: %b]", autoStart, minimizeToTray));

        // 設定開機自動啟動
        if (AutoStartManager.setAutoStart(autoStart)) {
            JOptionPane.showMessageDialog(this,
                    "設定已儲存!",
                    "成功",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "開機自動啟動設定失敗\n其他設定已儲存",
                    "警告",
                    JOptionPane.WARNING_MESSAGE);
        }

        dispose();
    }
}
