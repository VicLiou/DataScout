package da.api.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private String apid; // APID (Legacy)
    private String cloud; // 雲 (Legacy)
    private String environment; // 環境 (Legacy)
    private LocalDate expiryDate; // 到期日
    private String apiKey; // API KEY (Legacy)
    private String requestNumber; // 申請單號 (Legacy)

    private java.util.Map<String, String> attributes = new java.util.HashMap<>();

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public Object[] toArray(List<String> headers, int index) {
        List<Object> row = new ArrayList<>();
        row.add(index);
        for (String header : headers) {
            row.add(attributes.getOrDefault(header, ""));
        }
        return row.toArray();
    }

    // Keep legacy method for compatibility if needed, but preferable to use the new
    // one
    public Object[] toArray(int index) {
        return new Object[] { index, apid, cloud, environment, expiryDate, apiKey, requestNumber };
    }
}
