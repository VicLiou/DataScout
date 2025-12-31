package da.api.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 欄位設定配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnConfig {
    private String expiryDateColumn;
    private List<String> searchFilterColumns;
    private List<String> allHeaders;
}
