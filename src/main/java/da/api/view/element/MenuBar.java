package da.api.view.element;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import da.api.util.AppSettings;
import da.api.view.LogViewerDialog;
import da.api.view.MainFrameView;
import da.api.view.SettingsDialog;

public class MenuBar {

    private JMenuBar jMenuBar;
    private MainFrameView frameElement;
    private AppSettings appSettings;

    public MenuBar(MainFrameView frameElement, AppSettings appSettings) {
        this.frameElement = frameElement;
        this.appSettings = appSettings;
        
        this.jMenuBar = new JMenuBar();
        jMenuBar.add(this.fileMenu());
        jMenuBar.add(this.settingsMenu());
        jMenuBar.add(this.toolsMenu());
        jMenuBar.add(this.about());

        frameElement.setJMenuBar(jMenuBar);
    }
    
    private JMenu fileMenu() {
        JMenu fileMenu = new JMenu("檔案");
        
        JMenuItem logoutItem = new JMenuItem("登出");
        logoutItem.addActionListener(e -> logout());
        
        JMenuItem exitItem = new JMenuItem("結束");
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        return fileMenu;
    }
    
    private JMenu settingsMenu() {
        JMenu settingsMenu = new JMenu("設定");
        
        JMenuItem settingsItem = new JMenuItem("偏好設定");
        settingsItem.addActionListener(e -> openSettings());
        
        settingsMenu.add(settingsItem);
        
        return settingsMenu;
    }
    
    private JMenu toolsMenu() {
        JMenu toolsMenu = new JMenu("工具");
        
        JMenuItem logViewerItem = new JMenuItem("查看執行日誌");
        logViewerItem.addActionListener(e -> openLogViewer());
        
        toolsMenu.add(logViewerItem);
        
        return toolsMenu;
    }

    private JMenu about() {
        JMenu aboutMenu = new JMenu("關於");

        JMenuItem menuAbout = new JMenuItem("詳細資訊");
        menuAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String currentUser = frameElement.getUserService().getCurrentUser().getUsername();
                String role = frameElement.getUserService().getCurrentUser().getRole().getDisplayName();
                
                JOptionPane.showOptionDialog(null,
                        "服務名稱: API KEY服務台\n" +
                                "開發人員: 柳宏達\n\n" +
                                "目前使用者: " + currentUser + " (" + role + ")",
                        "關於 API KEY 服務台",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null, null, null);
            }
        });

        aboutMenu.add(menuAbout);

        return aboutMenu;
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(frameElement,
            "確定要登出嗎?", 
            "登出確認",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            frameElement.getUserService().logout();
            frameElement.dispose();
            System.exit(0);
        }
    }
    
    private void openSettings() {
        SettingsDialog dialog = new SettingsDialog(frameElement, appSettings);
        dialog.setVisible(true);
    }
    
    private void openLogViewer() {
        LogViewerDialog dialog = new LogViewerDialog(frameElement);
        dialog.setVisible(true);
    }
}
