package da.api.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import da.api.util.LogManager;
import da.api.util.LogManager.LogEntry;
import da.api.util.LogManager.LogListener;

/**
 * 日誌查看器對話框
 */
public class LogViewerDialog extends JDialog implements LogListener {
    private JTextArea logTextArea;
    private JButton clearButton;
    private JButton refreshButton;
    private JButton closeButton;
    private LogManager logManager;

    public LogViewerDialog(JFrame parent) {
        super(parent, "執行日誌查看器", false); // 非模態對話框
        this.logManager = LogManager.getInstance();

        initComponents();
        loadLogs();

        // 註冊為日誌監聽器
        logManager.addListener(this);

        setSize(800, 600);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // 日誌顯示區域
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logTextArea.setBackground(new Color(30, 30, 30));
        logTextArea.setForeground(new Color(200, 200, 200));
        logTextArea.setCaretColor(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(logTextArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("日誌內容"));

        // 按鈕面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        refreshButton = new JButton("重新整理");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadLogs();
            }
        });

        clearButton = new JButton("清除日誌");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logManager.clearLogs();
                logTextArea.setText("");
            }
        });

        closeButton = new JButton("關閉");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(closeButton);

        // 組裝面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 載入所有日誌
     */
    private void loadLogs() {
        StringBuilder sb = new StringBuilder();
        for (LogEntry entry : logManager.getAllLogs()) {
            sb.append(entry.toString()).append("\n");
        }
        logTextArea.setText(sb.toString());

        // 自動捲動到最下方
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
    }

    @Override
    public void onLogAdded(LogEntry entry) {
        // 在 EDT 線程中更新 UI
        SwingUtilities.invokeLater(() -> {
            if (entry != null) {
                logTextArea.append(entry.toString() + "\n");
                // 自動捲動到最下方
                logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
            } else {
                // entry 為 null 表示清除日誌
                logTextArea.setText("");
            }
        });
    }

    @Override
    public void dispose() {
        // 移除監聽器
        logManager.removeListener(this);
        super.dispose();
    }
}
