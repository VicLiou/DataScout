package da.api.view;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import da.api.service.UserService;

public class MainFrameView extends JFrame {

    private UserService userService;

    public MainFrameView(UserService userService) {
        super("API KEY 服務台");
        this.userService = userService;
        setSize(800, 500);
        // 關閉後程式將退出，不會在後台運行
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 設定視窗初始化顯示於螢幕正中間
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - super.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - super.getHeight()) / 2);
        super.setLocation(x, y);

    }

    public UserService getUserService() {
        return userService;
    }

    /*
     * 顯示視窗
     */
    public void showGUI() {
        setUIFont(new FontUIResource("微軟正黑體", Font.PLAIN, 12));
        // 鎖定視窗長寬
        setResizable(false);
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
