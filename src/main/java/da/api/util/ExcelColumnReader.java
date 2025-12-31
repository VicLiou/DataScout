package da.api.util;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Excel 欄位讀取工具
 */
public class ExcelColumnReader {
    
    public static void main(String[] args) {
        String filepath = "src/resource/APIKEY.xlsx";
        
        try (FileInputStream file = new FileInputStream(filepath);
             Workbook wb = new XSSFWorkbook(file)) {
            
            Sheet ws = wb.getSheetAt(0);
            Row headerRow = ws.getRow(0);
            
            if (headerRow != null) {
                System.out.println("Excel 檔案的欄位:");
                System.out.println("================");
                
                List<String> columns = new ArrayList<>();
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell cell = headerRow.getCell(i);
                    if (cell != null) {
                        String columnName = cell.getStringCellValue();
                        columns.add(columnName);
                        System.out.println((i + 1) + ". " + columnName);
                    }
                }
                
                System.out.println("\n總共有 " + columns.size() + " 個欄位");
                
                // 顯示第一筆資料作為範例
                Row dataRow = ws.getRow(1);
                if (dataRow != null) {
                    System.out.println("\n第一筆資料範例:");
                    System.out.println("================");
                    for (int i = 0; i < columns.size(); i++) {
                        Cell cell = dataRow.getCell(i);
                        String value = "";
                        if (cell != null) {
                            switch (cell.getCellType()) {
                                case STRING:
                                    value = cell.getStringCellValue();
                                    break;
                                case NUMERIC:
                                    if (DateUtil.isCellDateFormatted(cell)) {
                                        value = cell.getDateCellValue().toString();
                                    } else {
                                        value = String.valueOf(cell.getNumericCellValue());
                                    }
                                    break;
                                default:
                                    value = "";
                            }
                        }
                        System.out.println(columns.get(i) + ": " + value);
                    }
                }
            } else {
                System.out.println("Excel 檔案沒有標題列!");
            }
            
        } catch (Exception e) {
            System.err.println("讀取 Excel 檔案失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
