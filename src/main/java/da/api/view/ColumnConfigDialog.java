package da.api.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
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
        this.config = existingConfig; // Store initial config reference if needed, or just use for init
        setModal(true);
        setTitle("欄位設定");
        setSize(400, 500);
        setLocationRelativeTo(null);
        initComponents(existingConfig);
    }

    // Default constructor for compatibility
    public ColumnConfigDialog(List<String> headers) {
        this(headers, null);
    }

    private void initComponents(ColumnConfig existingConfig) {
        setLayout(new BorderLayout());

        // Top Panel: Expiry Date Selection
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("設定到期日欄位"));

        JPanel expiryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        expiryPanel.add(new JLabel("選擇欄位: "));
        expiryDateComboBox = new JComboBox<>(headers.toArray(new String[0]));
        if (existingConfig != null && existingConfig.getExpiryDateColumn() != null) {
            expiryDateComboBox.setSelectedItem(existingConfig.getExpiryDateColumn());
        }
        expiryPanel.add(expiryDateComboBox);
        topPanel.add(expiryPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Center Panel: Search Filter Selection
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("設定搜尋過濾欄位 (多選)"));

        JPanel checkBoxPanel = new JPanel(new GridLayout(0, 1));
        searchFilterCheckBoxes = new ArrayList<>();

        for (String header : headers) {
            JCheckBox checkBox = new JCheckBox(header);
            if (existingConfig != null && existingConfig.getSearchFilterColumns() != null) {
                if (existingConfig.getSearchFilterColumns().contains(header)) {
                    checkBox.setSelected(true);
                }
            }
            searchFilterCheckBoxes.add(checkBox);
            checkBoxPanel.add(checkBox);
        }

        JScrollPane scrollPane = new JScrollPane(checkBoxPanel);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom Panel: Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton("確定");
        okButton.addActionListener(e -> onOK());

        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> onCancel());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
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
