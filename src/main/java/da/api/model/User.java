package da.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 使用者實體類別
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String username;
    private String password;
    private UserRole role;
    
    public enum UserRole {
        ADMIN("管理者"),
        USER("一般使用者");
        
        private String displayName;
        
        UserRole(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}
