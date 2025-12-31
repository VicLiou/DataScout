package da.api.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import da.api.model.ApiKeyData;

/**
 * Excel 資料服務類別
 */
public class ExcelService {
    private String filePath;

    public ExcelService(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 讀取所有資料
     */
    public List<ApiKeyData> readAllData() {
        List<ApiKeyData> dataList = new ArrayList<>();

        try (FileInputStream file = new FileInputStream(filePath);
                Workbook wb = new XSSFWorkbook(file)) {

            Sheet ws = wb.getSheetAt(0);

            // 跳過標題列,從第二列開始讀取
            for (int i = 1; i <= ws.getLastRowNum(); i++) {
                Row row = ws.getRow(i);
                if (row == null)
                    continue;

                ApiKeyData data = new ApiKeyData();
                data.setApid(getCellValue(row.getCell(0))); // APID
                data.setCloud(getCellValue(row.getCell(1))); // 雲
                data.setEnvironment(getCellValue(row.getCell(2))); // 環境

                // 讀取到期日 (第4欄) - 支援日期和字串格式
                Cell dateCell = row.getCell(3);
                if (dateCell != null) {
                    try {
                        if (dateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dateCell)) {
                            // 日期格式
                            Date date = dateCell.getDateCellValue();
                            data.setExpiryDate(date.toInstant()
                                    .atZone(ZoneId.systemDefault()).toLocalDate());
                        } else if (dateCell.getCellType() == CellType.STRING) {
                            // 字串格式 (例如: 2025-11-15 或 2025-1-5)
                            String dateStr = dateCell.getStringCellValue().trim();
                            if (!dateStr.isEmpty() && !dateStr.equals("無")) {
                                // 處理不同格式的日期字串
                                data.setExpiryDate(parseDateString(dateStr));
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("無法解析日期: " + getCellValue(dateCell) + ", 列: " + (i + 1));
                    }
                }

                data.setApiKey(getCellValue(row.getCell(4))); // API KEY
                data.setRequestNumber(getCellValue(row.getCell(5))); // 申請單號

                dataList.add(data);
            }
        } catch (IOException e) {
            System.err.println("讀取 Excel 檔案失敗: " + filePath);
            e.printStackTrace();
            // 如果檔案不存在,建立一個空的檔案
            createEmptyExcelFile();
        }

        return dataList;
    }

    /**
     * 建立空的 Excel 檔案
     */
    private void createEmptyExcelFile() {
        try {
            // 確保目錄存在
            java.io.File file = new java.io.File(filePath);
            file.getParentFile().mkdirs();

            saveAllData(new ArrayList<>());
            System.out.println("已建立空的 Excel 檔案: " + filePath);
        } catch (Exception e) {
            System.err.println("建立 Excel 檔案失敗");
            e.printStackTrace();
        }
    }

    /**
     * 儲存所有資料
     */
    public boolean saveAllData(List<ApiKeyData> dataList) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet ws = wb.createSheet("API Keys");

            // 建立標題列
            Row headerRow = ws.createRow(0);
            String[] headers = { "APID", "雲", "環境", "到期日", "API KEY", "申請單號" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 寫入資料
            for (int i = 0; i < dataList.size(); i++) {
                Row row = ws.createRow(i + 1);
                ApiKeyData data = dataList.get(i);

                row.createCell(0).setCellValue(data.getApid()); // APID
                row.createCell(1).setCellValue(data.getCloud()); // 雲
                row.createCell(2).setCellValue(data.getEnvironment()); // 環境

                // 到期日 (第4欄)
                if (data.getExpiryDate() != null) {
                    Cell dateCell = row.createCell(3);
                    Date date = Date.from(data.getExpiryDate()
                            .atStartOfDay(ZoneId.systemDefault()).toInstant());
                    dateCell.setCellValue(date);

                    // 設定日期格式
                    CellStyle dateCellStyle = wb.createCellStyle();
                    CreationHelper createHelper = wb.getCreationHelper();
                    dateCellStyle.setDataFormat(
                            createHelper.createDataFormat().getFormat("yyyy-mm-dd"));
                    dateCell.setCellStyle(dateCellStyle);
                }

                row.createCell(4).setCellValue(data.getApiKey()); // API KEY
                row.createCell(5).setCellValue(data.getRequestNumber()); // 申請單號
            }

            // 自動調整欄寬
            for (int i = 0; i < headers.length; i++) {
                ws.autoSizeColumn(i);
            }

            // 寫入檔案
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null)
            return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    /**
     * 解析日期字串,支援多種格式
     */
    private java.time.LocalDate parseDateString(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        dateStr = dateStr.trim();

        try {
            // 嘗試標準格式 yyyy-MM-dd
            return java.time.LocalDate.parse(dateStr);
        } catch (Exception e1) {
            try {
                // 處理 yyyy-M-d 格式 (例如: 2026-2-5)
                String[] parts = dateStr.split("-");
                if (parts.length == 3) {
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    return java.time.LocalDate.of(year, month, day);
                }
            } catch (Exception e2) {
                System.err.println("無法解析日期字串: " + dateStr);
            }
        }

        return null;
    }
}
