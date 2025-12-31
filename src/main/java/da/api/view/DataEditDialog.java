package da.api.view;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

import da.api.model.ApiKeyData;

/**
 * 資料編輯對話框
 */
public class DataEditDialog extends JDialog {
    private JTextField apidField;
    private JTextField cloudField;
    private JTextField environmentField;
    private JSpinner expiryDateSpinner;
    private JTextField apiKeyField;
    private JTextField requestNumberField;
    private JButton saveButton;
    private JButton cancelButton;
    
    private ApiKeyData result;
    private boolean confirmed = false;
    
    public DataEditDialog(JFrame parent, ApiKeyData data, boolean isNew) {
        super(parent, isNew ? "新增資料" : "編輯資料", true);
        
        initComponents();
        
        if (data != null) {
            loadData(data);
        }
        
        setSize(400, 350);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // APID
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("APID:"), gbc);
        
        gbc.gridx = 1;
        apidField = new JTextField(20);
        mainPanel.add(apidField, gbc);
        
        // 雲
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("雲:"), gbc);
        
        gbc.gridx = 1;
        cloudField = new JTextField(20);
        mainPanel.add(cloudField, gbc);
        
        // 環境
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("環境:"), gbc);
        
        gbc.gridx = 1;
        environmentField = new JTextField(20);
        mainPanel.add(environmentField, gbc);
        
        // 到期日
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("到期日:"), gbc);
        
        gbc.gridx = 1;
        SpinnerDateModel dateModel = new SpinnerDateModel();
        expiryDateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(expiryDateSpinner, "yyyy-MM-dd");
        expiryDateSpinner.setEditor(dateEditor);
        mainPanel.add(expiryDateSpinner, gbc);
        
        // API KEY
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("API KEY:"), gbc);
        
        gbc.gridx = 1;
        apiKeyField = new JTextField(20);
        mainPanel.add(apiKeyField, gbc);
        
        // 申請單號
        gbc.gridx = 0;
        gbc.gridy = 5;
        mainPanel.add(new JLabel("申請單號:"), gbc);
        
        gbc.gridx = 1;
        requestNumberField = new JTextField(20);
        mainPanel.add(requestNumberField, gbc);
        
        // 按鈕面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        saveButton = new JButton("儲存");
        saveButton.addActionListener(e -> saveData());
        
        cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel);
    }
    
    private void loadData(ApiKeyData data) {
        apidField.setText(data.getApid());
        cloudField.setText(data.getCloud());
        environmentField.setText(data.getEnvironment());
        apiKeyField.setText(data.getApiKey());
        requestNumberField.setText(data.getRequestNumber());
        
        if (data.getExpiryDate() != null) {
            Date date = Date.from(data.getExpiryDate()
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
            expiryDateSpinner.setValue(date);
        }
    }
    
    private void saveData() {
        // 驗證必填欄位
        if (apidField.getText().trim().isEmpty() ||
            apiKeyField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "APID 和 API KEY 為必填欄位",
                "驗證錯誤",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        result = new ApiKeyData();
        result.setApid(apidField.getText().trim());
        result.setCloud(cloudField.getText().trim());
        result.setEnvironment(environmentField.getText().trim());
        
        Date date = (Date) expiryDateSpinner.getValue();
        result.setExpiryDate(date.toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDate());
        
        result.setApiKey(apiKeyField.getText().trim());
        result.setRequestNumber(requestNumberField.getText().trim());
        
        confirmed = true;
        dispose();
    }
    
    public ApiKeyData getResult() {
        return result;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}
