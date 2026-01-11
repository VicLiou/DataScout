package da.api.util;

import javax.swing.*;
import java.awt.*;

/**
 * 美化的對話框工具類
 */
public class StyledDialogs {

    private static final String FONT_NAME = "微軟正黑體";
    private static final Color BG_COLOR = new Color(248, 249, 250);

    /**
     * 顯示美化的確認對話框
     */
    public static boolean showConfirmDialog(Component parent, String message, String title, int type) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), title, true);
        dialog.setSize(500, 330);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);

        // 設定圖示
        setDialogIcon(dialog);

        // 主容器
        JPanel mainContainer = new JPanel(new BorderLayout(0, 20));
        mainContainer.setBackground(BG_COLOR);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // 標題區域
        JPanel headerPanel = createHeaderPanel(title, type);

        // 內容區域
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // 使用 JTextArea 以支援多行文字
        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        messageArea.setForeground(new Color(31, 41, 55));
        messageArea.setBackground(Color.WHITE);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(null);
        messageArea.setOpaque(true);
        contentPanel.add(messageArea, BorderLayout.CENTER);

        // 按鈕區域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(BG_COLOR);

        final boolean[] result = { false };

        // 是按鈕
        JButton yesButton = new JButton("是 (Y)");
        styleButton(yesButton, new Color(37, 99, 235), Color.WHITE, 120, 42);
        yesButton.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

        // 否按鈕
        JButton noButton = new JButton("否 (N)");
        styleButton(noButton, new Color(229, 231, 235), new Color(55, 65, 81), 120, 42);
        noButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        // 組合面板
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(contentPanel, BorderLayout.CENTER);
        mainContainer.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainContainer);
        dialog.setVisible(true);

        return result[0];
    }

    /**
     * 顯示美化的輸入對話框
     */
    public static String showInputDialog(Component parent, String message, String title, String initialValue) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), title, true);
        dialog.setSize(480, 400);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);

        setDialogIcon(dialog);

        // 主容器
        JPanel mainContainer = new JPanel(new BorderLayout(0, 20));
        mainContainer.setBackground(BG_COLOR);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // 標題區域
        JPanel headerPanel = createHeaderPanel(title, JOptionPane.QUESTION_MESSAGE);

        // 內容區域
        JPanel contentPanel = new JPanel(new BorderLayout(10, 15));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        messageLabel.setForeground(new Color(31, 41, 55));

        JTextField textField = new JTextField(initialValue);
        textField.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        textField.setPreferredSize(new Dimension(0, 38));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        contentPanel.add(messageLabel, BorderLayout.NORTH);
        contentPanel.add(textField, BorderLayout.CENTER);

        // 按鈕區域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(BG_COLOR);

        final String[] result = { null };

        // 確定按鈕
        JButton okButton = new JButton("確定");
        styleButton(okButton, new Color(37, 99, 235), Color.WHITE, 120, 42);
        okButton.addActionListener(e -> {
            result[0] = textField.getText();
            dialog.dispose();
        });

        // 取消按鈕
        JButton cancelButton = new JButton("取消");
        styleButton(cancelButton, new Color(229, 231, 235), new Color(55, 65, 81), 120, 42);
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        // 組合面板
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(contentPanel, BorderLayout.CENTER);
        mainContainer.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainContainer);
        textField.requestFocusInWindow();
        dialog.setVisible(true);

        return result[0];
    }

    /**
     * 顯示美化的訊息對話框
     */
    /**
     * 顯示美化的訊息對話框
     */
    public static void showMessageDialog(Component parent, String message, String title, int type) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), title, true);
        dialog.setSize(500, 330); // 增加尺寸
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);

        setDialogIcon(dialog);

        // 主容器
        JPanel mainContainer = new JPanel(new BorderLayout(0, 20));
        mainContainer.setBackground(BG_COLOR);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // 標題區域
        JPanel headerPanel = createHeaderPanel(title, type);

        // 內容區域
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // 使用 JTextArea 以支援多行文字
        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        messageArea.setForeground(new Color(31, 41, 55));
        messageArea.setBackground(Color.WHITE);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(null);
        messageArea.setOpaque(true);
        contentPanel.add(messageArea, BorderLayout.CENTER);

        // 按鈕區域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        buttonPanel.setBackground(BG_COLOR);

        JButton okButton = new JButton("確定");
        styleButton(okButton, new Color(37, 99, 235), Color.WHITE, 120, 42);
        okButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(okButton);

        // 組合面板
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(contentPanel, BorderLayout.CENTER);
        mainContainer.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainContainer);
        dialog.setVisible(true);
    }

    /**
     * 顯示美化的選擇對話框
     */
    public static String showSelectionDialog(Component parent, String message, String title,
            String[] options, String initialSelection) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), title, true);
        dialog.setSize(500, 380);
        dialog.setLocationRelativeTo(parent);
        dialog.setResizable(false);

        setDialogIcon(dialog);

        // 主容器
        JPanel mainContainer = new JPanel(new BorderLayout(0, 20));
        mainContainer.setBackground(BG_COLOR);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // 標題區域
        JPanel headerPanel = createHeaderPanel(title, JOptionPane.QUESTION_MESSAGE);

        // 內容區域
        JPanel contentPanel = new JPanel(new BorderLayout(10, 15));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        messageLabel.setForeground(new Color(31, 41, 55));

        JComboBox<String> comboBox = new JComboBox<>(options);
        comboBox.setSelectedItem(initialSelection);
        comboBox.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
        comboBox.setPreferredSize(new Dimension(0, 38));
        comboBox.setBackground(Color.WHITE);

        contentPanel.add(messageLabel, BorderLayout.NORTH);
        contentPanel.add(comboBox, BorderLayout.CENTER);

        // 按鈕區域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(BG_COLOR);

        final String[] result = { null };

        // 確定按鈕
        JButton okButton = new JButton("確定");
        styleButton(okButton, new Color(37, 99, 235), Color.WHITE, 120, 42);
        okButton.addActionListener(e -> {
            result[0] = (String) comboBox.getSelectedItem();
            dialog.dispose();
        });

        // 取消按鈕
        JButton cancelButton = new JButton("取消");
        styleButton(cancelButton, new Color(229, 231, 235), new Color(55, 65, 81), 120, 42);
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        // 組合面板
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(contentPanel, BorderLayout.CENTER);
        mainContainer.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainContainer);
        dialog.setVisible(true);

        return result[0];
    }

    /**
     * 創建標題面板
     */
    private static JPanel createHeaderPanel(String title, int type) {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        Color bgColor, borderColor, textColor;

        switch (type) {
            case JOptionPane.WARNING_MESSAGE:
                bgColor = new Color(254, 243, 199);
                borderColor = new Color(251, 191, 36);
                textColor = new Color(146, 64, 14);
                break;
            case JOptionPane.ERROR_MESSAGE:
                bgColor = new Color(254, 226, 226);
                borderColor = new Color(239, 68, 68);
                textColor = new Color(153, 27, 27);
                break;
            case JOptionPane.INFORMATION_MESSAGE:
                bgColor = new Color(224, 242, 254);
                borderColor = new Color(59, 130, 246);
                textColor = new Color(30, 64, 175);
                break;
            default: // QUESTION_MESSAGE
                bgColor = new Color(224, 242, 254);
                borderColor = new Color(59, 130, 246);
                textColor = new Color(30, 64, 175);
        }

        headerPanel.setBackground(bgColor);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font(FONT_NAME, Font.BOLD, 16));
        titleLabel.setForeground(textColor);
        headerPanel.add(titleLabel);

        return headerPanel;
    }

    /**
     * 美化按鈕
     */
    private static void styleButton(JButton button, Color bgColor, Color fgColor, int width, int height) {
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
    private static Color darkenColor(Color color, double factor) {
        return new Color(
                Math.max((int) (color.getRed() * factor), 0),
                Math.max((int) (color.getGreen() * factor), 0),
                Math.max((int) (color.getBlue() * factor), 0));
    }

    /**
     * 設定對話框圖示
     */
    private static void setDialogIcon(JDialog dialog) {
        try {
            java.net.URL iconUrl = StyledDialogs.class.getResource("/app_icon.png");
            if (iconUrl != null) {
                dialog.setIconImage(new ImageIcon(iconUrl).getImage());
            }
        } catch (Exception e) {
            // 忽略
        }
    }
}
