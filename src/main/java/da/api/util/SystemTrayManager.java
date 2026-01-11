package da.api.util;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

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
            System.out.println("系統不支援托盤功能");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();

        // 建立托盤圖示
        Image image = createTrayIcon();

        // 注意: 為了支援自訂字型以解決亂碼問題，我們不使用建構子傳入 PopupMenu，
        // 而是改用 Swing JPopupMenu 搭配 MouseListener
        trayIcon = new TrayIcon(image, "DataScout");
        trayIcon.setImageAutoSize(true);

        // 初始化 Swing 右鍵選單
        initSwingPopupMenu();

        // 滑鼠事件監聽
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // 檢查是否為右鍵點擊 (觸發選單)
                if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                    showSwingPopupMenu(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // 左鍵雙擊開啟視窗
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
        // 設定微軟正黑體
        Font font = new Font("Microsoft JhengHei", Font.PLAIN, 12);

        JMenuItem showItem = new JMenuItem("顯示視窗");
        showItem.setFont(font);
        showItem.addActionListener(e -> showMainWindow());

        JMenuItem hideItem = new JMenuItem("隱藏視窗");
        hideItem.setFont(font);
        hideItem.addActionListener(e -> hideMainWindow());

        JMenuItem exitItem = new JMenuItem("關閉視窗");
        exitItem.setFont(font);
        exitItem.addActionListener(e -> closeWindow());

        popupMenu.add(showItem);
        popupMenu.add(hideItem);
        popupMenu.addSeparator();
        popupMenu.add(exitItem);

        // 建立一個隱藏的 Dialog 作為 PopupMenu 的依附對象
        // 這樣可以確保 PopupMenu 正確顯示在最上層
        hiddenDialog = new JDialog();
        hiddenDialog.setUndecorated(true);
        hiddenDialog.setSize(0, 0);
        hiddenDialog.setAlwaysOnTop(true);

        // 當選單關閉時，隱藏 dummy dialog
        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                hiddenDialog.setVisible(false);
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                hiddenDialog.setVisible(false);
            }
        });
    }

    private void showSwingPopupMenu(int eventX, int eventY) {
        if (popupMenu != null) {
            // Use MouseInfo to get the most accurate current cursor position
            java.awt.Point mousePoint = java.awt.MouseInfo.getPointerInfo().getLocation();
            int x = mousePoint.x;
            int y = mousePoint.y;

            // Force calculations to ensure valid size
            popupMenu.revalidate();
            popupMenu.pack();
            Dimension size = popupMenu.getPreferredSize();

            // Fallback size
            if (size.width == 0 || size.height == 0) {
                // Approximate standard size
                size = new Dimension(120, 100);
            }

            // Get standard screen configuration and insets (taskbar area)
            java.awt.GraphicsConfiguration config = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration();
            java.awt.Insets insets = java.awt.Toolkit.getDefaultToolkit().getScreenInsets(config);
            java.awt.Rectangle screenBounds = config.getBounds();

            // Calculate usable screen bottom and right (excluding taskbar)
            int usableBottom = screenBounds.y + screenBounds.height - insets.bottom;
            int usableRight = screenBounds.x + screenBounds.width - insets.right;

            int menuX = x;
            int menuY = y;

            // Vertical Positioning Logic:
            // Check if menu would overflow the usable bottom area
            // OR if cursor is in the bottom 25% of the full screen (fallback heuristic)
            boolean overflowBottom = (y + size.height > usableBottom);
            boolean isBottomRegion = (y > (screenBounds.height * 0.75));

            if (overflowBottom || isBottomRegion) {
                // Position ABOVE the cursor
                menuY = y - size.height - 5;
            } else {
                // Position BELOW the cursor
                menuY = y + 5;
            }

            // Horizontal Positioning Logic:
            // If menu overlaps right usable edge, shift left
            if (x + size.width > usableRight) {
                menuX = x - size.width;
            }

            // Final safety clamp to screen bounds (ensure strictly on-screen)
            if (menuY < screenBounds.y)
                menuY = screenBounds.y;
            if (menuX < screenBounds.x)
                menuX = screenBounds.x;

            // Apply position
            hiddenDialog.setLocation(menuX, menuY);
            hiddenDialog.setVisible(true);
            hiddenDialog.toFront();

            // Show popup relative to the positioned hidden dialog
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

        // 修改預設關閉行為
        mainFrame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    private Image createTrayIcon() {
        try {
            java.net.URL iconUrl = getClass().getResource("/app_icon.png");
            if (iconUrl != null) {
                return new javax.swing.ImageIcon(iconUrl).getImage();
            }
        } catch (Exception e) {
            // 載入失敗則使用預設繪製圖示
        }

        // 備用方案：建立一個簡單的 16x16 圖示
        int size = 16;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(size, size,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // 繪製一個藍色圓形
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(0, 120, 215));
        g2d.fillOval(0, 0, size - 1, size - 1);

        // 繪製 "A" 字母
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("A", 4, 13);

        g2d.dispose();
        return image;
    }

    public void showMainWindow() {
        mainFrame.setVisible(true);
        mainFrame.setExtendedState(java.awt.Frame.NORMAL);
        mainFrame.toFront();
    }

    public void hideMainWindow() {
        mainFrame.setVisible(false);
        if (trayIcon != null) {
            trayIcon.displayMessage("DataScout",
                    "程式已最小化到系統托盤", TrayIcon.MessageType.INFO);
        }
    }

    public void closeWindow() {
        // 直接釋放視窗，這會觸發 windowClosed 事件進行清理
        mainFrame.dispose();
    }

    public void setTooltip(String tooltip) {
        if (trayIcon != null) {
            trayIcon.setToolTip(tooltip);
        }
    }

}
