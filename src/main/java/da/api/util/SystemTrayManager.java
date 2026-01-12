package da.api.util;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import da.api.view.MainFrameView;

/**
 * 系統托盤管理類別
 */
public class SystemTrayManager {
    private TrayIcon trayIcon;
    private MainFrameView mainFrame;
    private AppSettings appSettings;
    private JPopupMenu popupMenu;
    private JDialog hiddenDialog;
    private String filePath;

    public SystemTrayManager(MainFrameView mainFrame, AppSettings appSettings, String filePath) {
        this.mainFrame = mainFrame;
        this.appSettings = appSettings;
        this.filePath = filePath;
        setupSystemTray();
        setupWindowListener();
    }

    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        Image image = createTrayIcon();
        trayIcon = new TrayIcon(image, "DataScout");
        trayIcon.setImageAutoSize(true);

        // 初始化 Swing 右鍵選單
        initSwingPopupMenu();

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                    showSwingPopupMenu();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    showMainWindow();
                }
            }
        });

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("無法新增托盤圖示: " + e.getMessage());
        }
    }

    private void initSwingPopupMenu() {
        popupMenu = new JPopupMenu();
        popupMenu.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        popupMenu.setBackground(Color.WHITE);

        Font font = new Font("微軟正黑體", Font.PLAIN, 13);
        Font headerFont = new Font("微軟正黑體", Font.BOLD, 14);

        // 1. 標題
        JLabel titleItem = new JLabel("  DataScout");
        titleItem.setFont(headerFont);
        titleItem.setForeground(new Color(37, 99, 235));
        titleItem.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        titleItem.setOpaque(true);
        titleItem.setBackground(new Color(248, 250, 252));
        popupMenu.add(titleItem);

        popupMenu.addSeparator();

        // 2. 選項
        JMenuItem showItem = createStyledMenuItem("顯示視窗", font, new MenuIcon(MenuIcon.Type.WINDOW));
        showItem.addActionListener(e -> showMainWindow());
        popupMenu.add(showItem);

        JMenuItem hideItem = createStyledMenuItem("隱藏視窗", font, new MenuIcon(MenuIcon.Type.MINIMIZE));
        hideItem.addActionListener(e -> hideMainWindow());
        popupMenu.add(hideItem);

        popupMenu.addSeparator();

        JMenuItem exitItem = createStyledMenuItem("關閉程式", font, new MenuIcon(MenuIcon.Type.CLOSE));
        exitItem.addActionListener(e -> closeWindow());
        popupMenu.add(exitItem);

        hiddenDialog = new JDialog();
        hiddenDialog.setUndecorated(true);
        hiddenDialog.setSize(0, 0);
        hiddenDialog.setAlwaysOnTop(true);
        hiddenDialog.setType(java.awt.Window.Type.UTILITY); // 設為工具視窗，避免在 Alt+Tab 中出現

        popupMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                hiddenDialog.setVisible(false);
            }

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
                hiddenDialog.setVisible(false);
            }
        });
    }

    private JMenuItem createStyledMenuItem(String text, Font font, Icon icon) {
        JMenuItem item = new JMenuItem(text, icon);
        item.setFont(font);
        item.setBackground(Color.WHITE);
        item.setForeground(new Color(55, 65, 81));
        item.setBorder(BorderFactory.createEmptyBorder(6, 5, 6, 5));
        item.setIconTextGap(12); // 增加圖示與文字間距
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return item;
    }

    private void showSwingPopupMenu() {
        if (popupMenu != null) {
            // 取得游標位置
            Point mousePoint = MouseInfo.getPointerInfo().getLocation();
            int x = mousePoint.x;
            int y = mousePoint.y;

            // 計算選單大小
            popupMenu.pack();
            Dimension size = popupMenu.getPreferredSize();

            // 取得螢幕邊界資訊
            GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
            Rectangle screenBounds = config.getBounds();

            int usableBottom = screenBounds.y + screenBounds.height - insets.bottom;
            int usableRight = screenBounds.x + screenBounds.width - insets.right;

            int menuX = x;
            int menuY = y;

            // Y軸邏輯：如果下方空間不足，則將選單顯示在游標上方
            if (y + size.height > usableBottom) {
                menuY = y - size.height;
            }

            // X軸邏輯：如果右方空間不足，則向左平移
            if (x + size.width > usableRight) {
                menuX = x - size.width;
            }

            // 確保不超出螢幕邊界
            if (menuY < screenBounds.y)
                menuY = screenBounds.y;
            if (menuX < screenBounds.x)
                menuX = screenBounds.x;

            // 設定隱藏 Dialog 位置並顯示
            // 讓 Dialog 有一個極小的尺寸，確保它能接收焦點，這對於選單自動關閉很重要
            hiddenDialog.setSize(10, 10);
            hiddenDialog.setLocation(menuX, menuY);

            // 將 Dialog 背景設為完全透明（如果系統支援），這樣就不會看到一個奇怪的小方塊
            try {
                hiddenDialog.setBackground(new Color(0, 0, 0, 0));
            } catch (Exception e) {
                // 不支援透明背景則忽略，10x10 的方塊會被選單遮住大部分
            }

            hiddenDialog.setVisible(true);
            hiddenDialog.toFront();
            hiddenDialog.requestFocus();

            // 顯示 Popup (相對於 Dialog 的 0,0)
            popupMenu.show(hiddenDialog, 0, 0);
        }
    }

    private void setupWindowListener() {
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (filePath != null && appSettings.isMinimizeToTray(filePath)) {
                    hideMainWindow();
                } else if (filePath == null && appSettings.isGlobalMinimizeToTray()) {
                    hideMainWindow();
                } else {
                    mainFrame.dispose();
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (trayIcon != null) {
                    SystemTray.getSystemTray().remove(trayIcon);
                }
                if (hiddenDialog != null) {
                    hiddenDialog.dispose();
                }
            }

            @Override
            public void windowIconified(WindowEvent e) {
                if (filePath != null && appSettings.isMinimizeToTray(filePath)) {
                    hideMainWindow();
                } else if (filePath == null && appSettings.isGlobalMinimizeToTray()) {
                    hideMainWindow();
                }
            }
        });
        mainFrame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    private Image createTrayIcon() {
        try {
            java.net.URL iconUrl = getClass().getResource("/app_icon.png");
            if (iconUrl != null) {
                return new javax.swing.ImageIcon(iconUrl).getImage();
            }
        } catch (Exception e) {
        }

        // Fallback icon
        int size = 16;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(size, size,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(0, 120, 215));
        g2d.fillOval(0, 0, size - 1, size - 1);
        g2d.dispose();
        return image;
    }

    public void showMainWindow() {
        mainFrame.setVisible(true);
        mainFrame.setExtendedState(java.awt.Frame.NORMAL);
        mainFrame.toFront();
        // 嘗試將焦點帶回主視窗
        SwingUtilities.invokeLater(() -> mainFrame.requestFocus());
    }

    public void hideMainWindow() {
        mainFrame.setVisible(false);
        if (trayIcon != null) {
            trayIcon.displayMessage("DataScout", "程式已最小化到系統托盤", TrayIcon.MessageType.INFO);
        }
    }

    public void closeWindow() {
        mainFrame.dispose();
    }

    public void setTooltip(String tooltip) {
        if (trayIcon != null) {
            trayIcon.setToolTip(tooltip);
        }
    }

    // ============ 自訂圖示類別 ============
    // 使用 Java 2D 繪圖，確保在任何系統上都能清晰顯示，不受字型影響
    private static class MenuIcon implements Icon {
        enum Type {
            WINDOW, MINIMIZE, CLOSE
        }

        private final Type type;
        private final int iconSize = 14;

        public MenuIcon(Type type) {
            this.type = type;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 根據類型設定顏色
            if (type == Type.CLOSE) {
                g2d.setColor(new Color(220, 38, 38)); // 紅色
            } else {
                g2d.setColor(new Color(107, 114, 128)); // 灰色
            }
            // 懸停變色邏輯由 JMenuItem 負責，這裡繪製基礎圖形

            g2d.setStroke(new BasicStroke(1.5f));

            // 置中計算
            int offset = 1;

            switch (type) {
                case WINDOW:
                    // 畫一個方框
                    g2d.drawRect(x + offset, y + offset + 1, 10, 8);
                    // 畫上方標題列線條
                    g2d.drawLine(x + offset, y + offset + 3, x + offset + 10, y + offset + 3);
                    break;
                case MINIMIZE:
                    // 畫一條橫線
                    g2d.drawLine(x + offset, y + iconSize / 2 + 2, x + offset + 10, y + iconSize / 2 + 2);
                    break;
                case CLOSE:
                    // 畫一個 X
                    g2d.drawLine(x + offset + 1, y + offset + 2, x + offset + 9, y + offset + 10);
                    g2d.drawLine(x + offset + 9, y + offset + 2, x + offset + 1, y + offset + 10);
                    break;
            }

            g2d.dispose();
        }

        @Override
        public int getIconWidth() {
            return iconSize;
        }

        @Override
        public int getIconHeight() {
            return iconSize;
        }
    }
}
