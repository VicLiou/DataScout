package da.api.view;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import da.api.util.AppSettings;
import da.api.util.AutoStartManager;

/**
 * 設定對話框
 */
public class SettingsDialog extends JDialog {
    private JCheckBox autoStartCheckBox;
    private JCheckBox minimizeToTrayCheckBox;
    private JSpinner reminderDaysSpinner;
    private AppSettings appSettings;
    
    public SettingsDialog(JFrame parent, AppSettings appSettings) {
        super(parent, "設定", true);
        this.appSettings = appSettings;
        
        initComponents();
        loadSettings();
        
        setSize(400, 250);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // 開機自動啟動
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        autoStartCheckBox = new JCheckBox("開機自動啟動");
        mainPanel.add(autoStartCheckBox, gbc);
        
        // 最小化到系統托盤
        gbc.gridy = 1;
        minimizeToTrayCheckBox = new JCheckBox("關閉視窗時最小化到系統托盤");
        mainPanel.add(minimizeToTrayCheckBox, gbc);
        
        // 到期提醒天數
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("到期提醒天數:"), gbc);
        
        gbc.gridx = 1;
        reminderDaysSpinner = new JSpinner(new SpinnerNumberModel(7, 1, 365, 1));
        mainPanel.add(reminderDaysSpinner, gbc);
        
        // 說明文字
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JLabel noteLabel = new JLabel("<html><small>註: 系統會在 API KEY 到期前指定天數發出提醒</small></html>");
        noteLabel.setForeground(Color.GRAY);
        mainPanel.add(noteLabel, gbc);
        
        // 按鈕面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton saveButton = new JButton("儲存");
        saveButton.addActionListener(e -> saveSettings());
        
        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridy = 4;
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel);
    }
    
    private void loadSettings() {
        autoStartCheckBox.setSelected(appSettings.isAutoStart());
        minimizeToTrayCheckBox.setSelected(appSettings.isMinimizeToTray());
        reminderDaysSpinner.setValue(appSettings.getExpiryReminderDays());
    }
    
    private void saveSettings() {
        // 儲存設定
        boolean autoStart = autoStartCheckBox.isSelected();
        appSettings.setAutoStart(autoStart);
        appSettings.setMinimizeToTray(minimizeToTrayCheckBox.isSelected());
        appSettings.setExpiryReminderDays((Integer) reminderDaysSpinner.getValue());
        
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
