package da.api.service;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import da.api.model.User;
import da.api.model.User.UserRole;

/**
 * 使用者服務類別 - 根據 IP 位址自動判斷權限
 */
public class UserService {
    private static final String ADMIN_IP = "172.19.96.1";
    private User currentUser;
    private boolean isAdminByIP;

    public UserService() {
        // 取得本地 IP 並判斷權限
        String localIP = getLocalIPAddress();
        System.out.println("偵測到本地 IP: " + localIP);

        if (ADMIN_IP.equals(localIP)) {
            isAdminByIP = true;
            currentUser = new User("admin", "", UserRole.ADMIN);
            System.out.println("IP 符合管理員權限: " + ADMIN_IP);
        } else {
            isAdminByIP = false;
            currentUser = new User("user", "", UserRole.USER);
            System.out.println("IP 不符合管理員權限，僅提供瀏覽功能");
        }
    }

    /**
     * 取得本地 IP 位址
     */
    private String getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                // 跳過迴路介面和未啟用的介面
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    // 只取 IPv4 位址，排除本地迴路
                    if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(':') == -1) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("無法取得本地 IP: " + e.getMessage());
            e.printStackTrace();
        }
        return "unknown";
    }

    /**
     * 取得目前使用者
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * 檢查是否為管理者
     */
    public boolean isAdmin() {
        return isAdminByIP;
    }

    /**
     * 取得本地 IP（供外部查詢）
     */
    public String getCurrentIP() {
        return getLocalIPAddress();
    }

    /**
     * 登入方法（基於 IP 權限，此處保留兼容性）
     */
    public boolean login(String username, String password) {
        // 因為已經基於 IP 自動判斷權限，這裡直接返回 true
        // 可以在這裡添加額外的驗證邏輯
        return true;
    }

    /**
     * 登出方法
     */
    public void logout() {
        // 登出處理，可以在這裡清除資料或記錄日誌
        System.out.println("使用者 " + currentUser.getUsername() + " 已登出");
    }
}
