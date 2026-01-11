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
public class ExcelData {
    private LocalDate expiryDate; // 到期日

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
}
