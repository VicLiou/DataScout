package da.api.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

/**
 * 用於設定新檔案的欄位標題
 */
public class HeaderSetupDialog extends JDialog {
    private DefaultListModel<String> listModel;
    private JList<String> headerList;
    private JTextField inputField;
    private boolean confirmed = false;

    public HeaderSetupDialog(JFrame parent) {
        super(parent, "設定欄位", true);
        setSize(400, 500);

        // 隱藏圖示 (使用透明圖片)
        try {
            java.net.URL iconUrl = getClass().getResource("/app_icon.png");
            if (iconUrl != null) {
                setIconImage(new javax.swing.ImageIcon(iconUrl).getImage());
            }
        } catch (Exception e) {
            // 忽略
        }

        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initComponents();
    }

    private void initComponents() {
        // 上方面板: 說明文字
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("此檔案為空白檔案，請先定義您需要的欄位："));
        add(topPanel, BorderLayout.NORTH);

        // 中間面板: 列表與新增控制項
        JPanel centerPanel = new JPanel(new BorderLayout());

        // List
        listModel = new DefaultListModel<>();
        headerList = new JList<>(listModel);
        headerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(headerList);

        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // 新增/移除控制項
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        inputField = new JTextField();
        controlPanel.add(inputField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JButton addButton = new JButton("新增");
        addButton.addActionListener(e -> addHeader());
        controlPanel.add(addButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JButton removeButton = new JButton("移除選取欄位");
        removeButton.addActionListener(e -> removeHeader());
        controlPanel.add(removeButton, gbc);

        // 允許按 Enter 鍵新增
        inputField.addActionListener(e -> addHeader());

        centerPanel.add(controlPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        // 下方面板: 確認/取消
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton confirmButton = new JButton("完成設定");
        confirmButton.addActionListener(e -> onConfirm());

        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose()); // confirmed 保持 false

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addHeader() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        if (listModel.contains(text)) {
            JOptionPane.showMessageDialog(this, "欄位名稱重複", "錯誤", JOptionPane.WARNING_MESSAGE);
            return;
        }

        listModel.addElement(text);
        inputField.setText("");
        inputField.requestFocus();
    }

    private void removeHeader() {
        int index = headerList.getSelectedIndex();
        if (index != -1) {
            listModel.remove(index);
        }
    }

    private void onConfirm() {
        if (listModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "請至少新增一個欄位", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public List<String> getHeaders() {
        List<String> headers = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            headers.add(listModel.get(i));
        }
        return headers;
    }
}
