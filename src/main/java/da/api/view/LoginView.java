package da.api.view;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import da.api.service.UserService;

/**
 * 登入視窗
 */
public class LoginView extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private UserService userService;
    private boolean loginSuccess = false;
    
    public LoginView(JFrame parent, UserService userService) {
        super(parent, "登入 - API KEY 服務台", true);
        this.userService = userService;
        
        initComponents();
        setSize(350, 200);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 使用者名稱
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("使用者名稱:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        usernameField = new JTextField(15);
        mainPanel.add(usernameField, gbc);
        
        // 密碼
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("密碼:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        passwordField = new JPasswordField(15);
        mainPanel.add(passwordField, gbc);
        
        // 提示資訊
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        JLabel hintLabel = new JLabel("<html><small>預設管理員: admin/admin123<br>預設使用者: user/user123</small></html>");
        hintLabel.setForeground(Color.GRAY);
        mainPanel.add(hintLabel, gbc);
        
        // 按鈕面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        loginButton = new JButton("登入");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        cancelButton = new JButton("取消");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                System.exit(0);
            }
        });
        
        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridy = 3;
        mainPanel.add(buttonPanel, gbc);
        
        // Enter 鍵登入
        passwordField.addActionListener(e -> performLogin());
        
        add(mainPanel);
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "請輸入使用者名稱和密碼", 
                "登入失敗", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (userService.login(username, password)) {
            loginSuccess = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "使用者名稱或密碼錯誤", 
                "登入失敗", 
                JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
    
    public boolean isLoginSuccess() {
        return loginSuccess;
    }
}
