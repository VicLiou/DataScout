package da.api.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class MainFrameView extends JFrame {

    public MainFrameView() {
        super("DataScout");
        setSize(1000, 650);
        setMinimumSize(new Dimension(900, 600));
        // 關閉後程式將退出，不會在後台運行
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 設定視窗初始化顯示於螢幕正中間
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - super.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - super.getHeight()) / 2);
        super.setLocation(x, y);

        // 設定視窗圖示
        try {
            java.net.URL iconUrl = getClass().getResource("/app_icon.png");
            if (iconUrl != null) {
                setIconImage(new javax.swing.ImageIcon(iconUrl).getImage());
            }
        } catch (Exception e) {
            // 忽略圖示載入錯誤
        }

        // 設定現代化的外觀
        setupModernLookAndFeel();
    }

    /**
     * 設定現代化的外觀
     */
    private void setupModernLookAndFeel() {
        try {
            // 設定背景顏色
            getContentPane().setBackground(new Color(248, 249, 250));

            // 自訂 UI 元件顏色
            UIManager.put("Panel.background", new Color(248, 249, 250));
            UIManager.put("OptionPane.background", new Color(248, 249, 250));
            UIManager.put("Button.background", new Color(255, 255, 255));
            UIManager.put("Button.foreground", new Color(31, 41, 55));
            UIManager.put("Table.background", Color.WHITE);
            UIManager.put("Table.alternateRowColor", new Color(249, 250, 251));
            UIManager.put("Table.gridColor", new Color(229, 231, 235));
            UIManager.put("Table.selectionBackground", new Color(219, 234, 254));
            UIManager.put("Table.selectionForeground", new Color(30, 58, 138));
            UIManager.put("ScrollPane.background", Color.WHITE);
            UIManager.put("Viewport.background", Color.WHITE);

        } catch (Exception e) {
            // 如果設定失敗，使用預設外觀
        }
    }

    /*
     * 顯示視窗
     */
    public void showGUI() {
        setUIFont(new FontUIResource("微軟正黑體", Font.PLAIN, 13));
        // 允許調整視窗大小
        setResizable(true);
        setVisible(true);
    }

    /**
     * 設定 UI 介面字型
     */
    public static void setUIFont(FontUIResource font) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }
}
