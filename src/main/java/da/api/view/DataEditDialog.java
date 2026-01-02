package da.api.view;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Dimension;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

import da.api.model.ApiKeyData;
import da.api.model.ColumnConfig;

/**
 * 資料編輯對話框
 */
public class DataEditDialog extends JDialog {
    // Legacy fields
    private JTextField apidField;
    private JTextField cloudField;
    private JTextField environmentField;
    private JSpinner expiryDateSpinner;
    private JTextField apiKeyField;
    private JTextField requestNumberField;

    // Dynamic fields
    private Map<String, JComponent> dynamicFields;
    private ColumnConfig columnConfig;

    private JButton saveButton;
    private JButton cancelButton;

    private ApiKeyData result;
    private boolean confirmed = false;

    public DataEditDialog(JFrame parent, ApiKeyData data, boolean isNew, ColumnConfig config) {
        super(parent, isNew ? "新增資料" : "編輯資料", true);
        this.columnConfig = config;

        if (columnConfig != null) {
            initComponentsDynamic();
        } else {
            initComponentsLegacy();
        }

        if (data != null) {
            if (columnConfig != null) {
                loadDataDynamic(data);
            } else {
                loadDataLegacy(data);
            }
        }

        setSize(400, 500); // Increased height for scrolling if needed
        setLocationRelativeTo(parent);
    }

    // Default constructor for compatibility if needed, though we should update
    // callers
    public DataEditDialog(JFrame parent, ApiKeyData data, boolean isNew) {
        this(parent, data, isNew, null);
    }

    private void initComponentsLegacy() {
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

        addBottomButtons(mainPanel, 6);
        add(mainPanel);
    }

    private void initComponentsDynamic() {
        dynamicFields = new HashMap<>();
        JPanel mainPanel = new JPanel(new GridBagLayout());

        // Add padding around the main panel
        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Check padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        String expiryCol = columnConfig.getExpiryDateColumn();

        for (String header : columnConfig.getAllHeaders()) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0.0; // Label should not grow
            mainPanel.add(new JLabel(header + ":"), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0; // Field should grow
            JComponent field;
            if (header.equals(expiryCol)) {
                SpinnerDateModel dateModel = new SpinnerDateModel();
                JSpinner spinner = new JSpinner(dateModel);
                JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
                spinner.setEditor(dateEditor);
                field = spinner;
            } else {
                field = new JTextField(20);
            }

            dynamicFields.put(header, field);
            mainPanel.add(field, gbc);
            row++;
        }

        // Wrap in scroll pane if many fields
        JPanel scrollWrapper = new JPanel(new java.awt.BorderLayout());
        scrollWrapper.add(mainPanel, java.awt.BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(scrollWrapper);
        scrollPane.setBorder(null);

        // Button panel need to be separately added or appended to mainPanel?
        // Let's use border layout for the dialog to handle scrolling content and fixed
        // buttons
        setLayout(new java.awt.BorderLayout());
        add(scrollPane, java.awt.BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("儲存");
        saveButton.addActionListener(e -> saveDataDynamic());

        cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, java.awt.BorderLayout.SOUTH);

        // Auto-sizing
        setMinimumSize(new Dimension(400, 300));
        pack();
        setLocationRelativeTo(getParent());
    }

    private void addBottomButtons(JPanel panel, int gridy) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        saveButton = new JButton("儲存");
        saveButton.addActionListener(e -> saveDataLegacy());

        cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttonPanel, gbc);
    }

    private void loadDataLegacy(ApiKeyData data) {
        // Safe check for nulls
        apidField.setText(data.getApid() != null ? data.getApid() : "");
        cloudField.setText(data.getCloud() != null ? data.getCloud() : "");
        environmentField.setText(data.getEnvironment() != null ? data.getEnvironment() : "");
        apiKeyField.setText(data.getApiKey() != null ? data.getApiKey() : "");
        requestNumberField.setText(data.getRequestNumber() != null ? data.getRequestNumber() : "");

        if (data.getExpiryDate() != null) {
            Date date = Date.from(data.getExpiryDate()
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());
            expiryDateSpinner.setValue(date);
        }
    }

    private void loadDataDynamic(ApiKeyData data) {
        String expiryCol = columnConfig.getExpiryDateColumn();

        for (String header : columnConfig.getAllHeaders()) {
            JComponent field = dynamicFields.get(header);
            if (field == null)
                continue;

            if (header.equals(expiryCol)) {
                // For expiry date, prioritize the typed field
                if (data.getExpiryDate() != null) {
                    Date date = Date.from(data.getExpiryDate()
                            .atStartOfDay(ZoneId.systemDefault()).toInstant());
                    ((JSpinner) field).setValue(date);
                } else {
                    // Try to parse from attribute if exists
                    // But here we usually expect ExpiryReminderService/ExcelService to have
                    // populated expiryDate
                }
            } else {
                String value = data.getAttribute(header);
                if (value == null)
                    value = "";
                ((JTextField) field).setText(value);
            }
        }
    }

    private void saveDataLegacy() {
        // Very basic validation
        if (apidField.getText().trim().isEmpty()) {
            // In legacy, maybe APID is critical? Let's keep it simple
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

    private void saveDataDynamic() {
        result = new ApiKeyData();
        String expiryCol = columnConfig.getExpiryDateColumn();

        for (String header : columnConfig.getAllHeaders()) {
            JComponent field = dynamicFields.get(header);
            if (field instanceof JSpinner) {
                Date date = (Date) ((JSpinner) field).getValue();
                if (header.equals(expiryCol)) {
                    result.setExpiryDate(date.toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDate());
                }
                // Also save as attribute for string representation if needed?
                // result.setAttribute(header, ...);
                // Currently ExcelService largely reads from cell. When writing, we need to know
                // how to write.
                // But ApiKeyData is the model.
                // We should probably save the formatted date string to attributes for
                // consistency if other parts read it?
                // But for now, just setting the typed field is what ApiKeyData primarily uses
                // for logic.
            } else if (field instanceof JTextField) {
                String value = ((JTextField) field).getText().trim();
                result.setAttribute(header, value);
            }
        }

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
