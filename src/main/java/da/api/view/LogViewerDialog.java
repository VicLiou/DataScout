package da.api.view;

import java.awt.BorderLayout;
import java.awt.Color;

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

        setSize(900, 650);
        setMinimumSize(new java.awt.Dimension(700, 500));

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

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(248, 249, 250));

        // 標題區域
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        headerPanel.setBackground(new Color(255, 255, 255));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));

        javax.swing.JLabel titleLabel = new javax.swing.JLabel("系統執行日誌");
        titleLabel.setFont(new Font("微軟正黑體", Font.BOLD, 18));
        titleLabel.setForeground(new Color(31, 41, 55));
        headerPanel.add(titleLabel);

        // 日誌顯示區域
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("微軟正黑體", Font.PLAIN, 13)); // 使用支援中文的微軟正黑體
        logTextArea.setBackground(new Color(30, 41, 59)); // 深藍灰色背景
        logTextArea.setForeground(new Color(209, 213, 219)); // 淺灰色文字
        logTextArea.setCaretColor(Color.WHITE);
        logTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(logTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 主顯示區塊 (包含邊距)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 249, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 按鈕面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        refreshButton = new JButton("重新整理");
        styleButton(refreshButton, new Color(59, 130, 246), Color.WHITE);
        refreshButton.addActionListener(e -> loadLogs());

        clearButton = new JButton("清除日誌");
        styleButton(clearButton, new Color(239, 68, 68), Color.WHITE);
        clearButton.addActionListener(e -> {
            boolean confirm = da.api.util.StyledDialogs.showConfirmDialog(
                    this, "確定要清除所有日誌嗎？", "清除日誌", javax.swing.JOptionPane.WARNING_MESSAGE);
            if (confirm) {
                logManager.clearLogs();
                logTextArea.setText("");
            }
        });

        closeButton = new JButton("關閉");
        styleButton(closeButton, new Color(229, 231, 235), new Color(55, 65, 81));
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(closeButton);

        add(headerPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 美化按鈕
     */
    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setFont(new Font("微軟正黑體", Font.BOLD, 14));
        button.setForeground(fgColor);
        button.setBackground(bgColor);
        button.setPreferredSize(new java.awt.Dimension(110, 38));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(darkenColor(bgColor, 0.9));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }

    private Color darkenColor(Color color, double factor) {
        return new Color(
                Math.max((int) (color.getRed() * factor), 0),
                Math.max((int) (color.getGreen() * factor), 0),
                Math.max((int) (color.getBlue() * factor), 0));
    }

    /**
     * 載入所有日誌
     */
    private void loadLogs() {
        StringBuilder sb = new StringBuilder();
        // 添加日誌頭部資訊
        sb.append("=================================================================\n");
        sb.append(" DataScout 系統執行日誌\n");
        sb.append(" 產生時間: ").append(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("=================================================================\n\n");

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
                loadLogs(); // 重新載入（實際上會清空並顯示標頭）
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
