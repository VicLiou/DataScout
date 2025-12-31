package da.api.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API KEY 資料實體
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyData {
    private String apid;           // APID
    private String cloud;          // 雲
    private String environment;    // 環境
    private LocalDate expiryDate;  // 到期日
    private String apiKey;         // API KEY
    private String requestNumber;  // 申請單號
    
    public Object[] toArray(int index) {
        return new Object[]{index, apid, cloud, environment, expiryDate, apiKey, requestNumber};
    }
}
