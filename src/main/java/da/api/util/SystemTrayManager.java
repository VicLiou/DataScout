package da.api.util;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import da.api.view.MainFrameView;

/**
 * 系統托盤管理類別
 */
public class SystemTrayManager {
    private TrayIcon trayIcon;
    private MainFrameView mainFrame;
    private boolean minimizeToTray = true;
    
    public SystemTrayManager(MainFrameView mainFrame) {
        this.mainFrame = mainFrame;
        setupSystemTray();
        setupWindowListener();
    }
    
    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("系統不支援托盤功能");
            return;
        }
        
        SystemTray tray = SystemTray.getSystemTray();
        
        // 建立托盤圖示 (使用簡單的圖示)
        Image image = createTrayIcon();
        
        // 建立彈出選單
        PopupMenu popup = new PopupMenu();
        
        MenuItem showItem = new MenuItem("顯示視窗");
        showItem.addActionListener(e -> showMainWindow());
        
        MenuItem hideItem = new MenuItem("隱藏視窗");
        hideItem.addActionListener(e -> hideMainWindow());
        
        MenuItem exitItem = new MenuItem("結束程式");
        exitItem.addActionListener(e -> exitApplication());
        
        popup.add(showItem);
        popup.add(hideItem);
        popup.addSeparator();
        popup.add(exitItem);
        
        // 建立托盤圖示
        trayIcon = new TrayIcon(image, "API KEY 服務台", popup);
        trayIcon.setImageAutoSize(true);
        
        // 雙擊托盤圖示顯示視窗
        trayIcon.addActionListener(e -> showMainWindow());
        
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("無法新增托盤圖示: " + e.getMessage());
        }
    }
    
    private void setupWindowListener() {
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (minimizeToTray) {
                    hideMainWindow();
                } else {
                    exitApplication();
                }
            }
            
            @Override
            public void windowIconified(WindowEvent e) {
                if (minimizeToTray) {
                    hideMainWindow();
                }
            }
        });
        
        // 修改預設關閉行為
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }
    
    private Image createTrayIcon() {
        // 建立一個簡單的 16x16 圖示
        int size = 16;
        java.awt.image.BufferedImage image = 
            new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
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
        mainFrame.setExtendedState(JFrame.NORMAL);
        mainFrame.toFront();
    }
    
    public void hideMainWindow() {
        mainFrame.setVisible(false);
        if (trayIcon != null) {
            trayIcon.displayMessage("API KEY 服務台", 
                "程式已最小化到系統托盤", TrayIcon.MessageType.INFO);
        }
    }
    
    public void exitApplication() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
        }
        System.exit(0);
    }
    
    public void setMinimizeToTray(boolean minimize) {
        this.minimizeToTray = minimize;
    }
    
    public boolean isMinimizeToTray() {
        return minimizeToTray;
    }
}
