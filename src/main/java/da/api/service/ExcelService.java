package da.api.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import da.api.model.ColumnConfig;
import da.api.model.ExcelData;
import da.api.util.LogManager;

public class ExcelService {

    private String filePath;
    private ColumnConfig columnConfig;

    public ExcelService(String filePath) {
        this.filePath = filePath;
    }

    public ExcelService(String filePath, ColumnConfig columnConfig) {
        this.filePath = filePath;
        this.columnConfig = columnConfig;
    }

    public void setColumnConfig(ColumnConfig columnConfig) {
        this.columnConfig = columnConfig;
    }

    public String getFilePath() {
        return filePath;
    }

    public ColumnConfig getColumnConfig() {
        return columnConfig;
    }

    public void createEmptyExcelFile() {
        try (Workbook wb = new XSSFWorkbook()) {
            wb.createSheet("API Keys");
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogManager.getInstance().error("無法建立空白檔案: " + filePath);
        }
    }

    public List<String> readHeaders() {
        List<String> headers = new ArrayList<>();
        // 如果檔案不存在，返回空列表
        if (!new java.io.File(filePath).exists()) {
            return headers;
        }

        try (FileInputStream file = new FileInputStream(filePath); Workbook wb = new XSSFWorkbook(file)) {
            // 檢查是否有 Sheet
            if (wb.getNumberOfSheets() == 0)
                return headers;

            Sheet ws = wb.getSheetAt(0);
            Row headerRow = ws.getRow(0);
            if (headerRow != null) {
                for (Cell cell : headerRow) {
                    headers.add(getCellValue(cell));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogManager.getInstance().error("讀取標題失敗: " + filePath);
        }
        return headers;
    }

    public List<ExcelData> readAllData() {
        if (columnConfig != null) {
            return readDataWithConfig();
        }
        // 如果沒有配置但有檔案，也許可讀取原始數據，但既然 ExcelData 依賴 header，這裡可能返回空或拋錯
        // 基於之前的邏輯，直接返回空
        return new ArrayList<>();
    }

    private List<ExcelData> readDataWithConfig() {
        List<ExcelData> dataList = new ArrayList<>();
        List<String> headers = columnConfig.getAllHeaders();
        String expiryCol = columnConfig.getExpiryDateColumn();

        try (FileInputStream file = new FileInputStream(filePath); Workbook wb = new XSSFWorkbook(file)) {
            if (wb.getNumberOfSheets() == 0)
                return dataList;

            Sheet ws = wb.getSheetAt(0);

            // 讀取標題列映射 (名稱 -> 索引)
            Map<String, Integer> headerMap = new HashMap<>();
            Row headerRow = ws.getRow(0);
            if (headerRow != null) {
                for (Cell cell : headerRow) {
                    headerMap.put(getCellValue(cell), cell.getColumnIndex());
                }
            }

            for (int i = 1; i <= ws.getLastRowNum(); i++) {
                Row row = ws.getRow(i);
                if (row == null)
                    continue;

                ExcelData data = new ExcelData();

                // 填入動態屬性
                for (String header : headers) {
                    Integer colIndex = headerMap.get(header);
                    if (colIndex != null) {
                        String value = getCellValue(row.getCell(colIndex));
                        data.setAttribute(header, value);
                    }
                }

                // 填入到期日
                if (expiryCol != null) {
                    Integer colIndex = headerMap.get(expiryCol);
                    if (colIndex != null) {
                        Cell dateCell = row.getCell(colIndex);
                        if (dateCell != null) {
                            try {
                                if (dateCell.getCellType() == CellType.NUMERIC
                                        && DateUtil.isCellDateFormatted(dateCell)) {
                                    Date date = dateCell.getDateCellValue();
                                    LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                    data.setExpiryDate(localDate);
                                    // 同時將格式化的日期字串存入 attributes，以便顯示
                                    data.setAttribute(expiryCol, localDate.toString());
                                } else if (dateCell.getCellType() == CellType.STRING) {
                                    String dateStr = dateCell.getStringCellValue().trim();
                                    if (!dateStr.isEmpty() && !dateStr.equals("無")) {
                                        LocalDate localDate = parseDateString(dateStr);
                                        data.setExpiryDate(localDate);
                                        // 同時將格式化的日期字串存入 attributes，以便顯示
                                        if (localDate != null) {
                                            data.setAttribute(expiryCol, localDate.toString());
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                LogManager.getInstance().error("無法解析日期: " + getCellValue(dateCell) + ", 列: " + (i + 1));
                            }
                        }
                    }
                }

                dataList.add(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
            LogManager.getInstance().error("讀取 Excel 資料時發生 IO 錯誤: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            LogManager.getInstance().error("讀取 Excel 資料時發生未預期錯誤: " + e.getMessage());
        }
        return dataList;
    }

    public boolean saveAllData(List<ExcelData> dataList) {
        if (columnConfig != null && columnConfig.getAllHeaders() != null && !columnConfig.getAllHeaders().isEmpty()) {
            return saveAllDataWithConfig(dataList);
        } else {
            // 無配置時，嘗試使用讀取時的標題 (若有) 或不進行儲存並記錄錯誤
            LogManager.getInstance().error("無法儲存資料: 缺少欄位設定");
            return false;
        }
    }

    private boolean saveAllDataWithConfig(List<ExcelData> dataList) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet ws = wb.createSheet("API Keys");

            // 寫入標題列
            Row headerRow = ws.createRow(0);
            List<String> headers = columnConfig.getAllHeaders();
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
            }

            String expiryCol = columnConfig.getExpiryDateColumn();

            // 寫入資料
            for (int i = 0; i < dataList.size(); i++) {
                Row row = ws.createRow(i + 1);
                ExcelData data = dataList.get(i);

                for (int j = 0; j < headers.size(); j++) {
                    String header = headers.get(j);
                    Cell cell = row.createCell(j);

                    if (header.equals(expiryCol)) {
                        // 處理到期日格式
                        if (data.getExpiryDate() != null) {
                            Date date = Date
                                    .from(data.getExpiryDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
                            cell.setCellValue(date);

                            CellStyle dateCellStyle = wb.createCellStyle();
                            CreationHelper createHelper = wb.getCreationHelper();
                            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));
                            cell.setCellStyle(dateCellStyle);
                        }
                    } else {
                        // 處理動態屬性
                        String value = data.getAttribute(header);
                        cell.setCellValue(value != null ? value : "");
                    }
                }
            }

            // 自動調整欄寬
            for (int i = 0; i < headers.size(); i++) {
                ws.autoSizeColumn(i);
            }

            // 寫入檔案
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            LogManager.getInstance().error("儲存 Excel 資料時發生 IO 錯誤: " + e.getMessage());
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            LogManager.getInstance().error("儲存 Excel 資料時發生未預期錯誤: " + e.getMessage());
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
                if (DateUtil.isCellDateFormatted(cell)) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(cell.getDateCellValue());
                }
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
                LogManager.getInstance().error("無法解析日期字串: " + dateStr);
            }
        }

        return null;
    }

    /**
     * 在 Excel 檔案中新增欄位
     */
    public boolean addColumnToExcel(String columnName) {
        if (filePath == null || columnName == null || columnName.trim().isEmpty()) {
            return false;
        }

        try (FileInputStream fis = new FileInputStream(new File(filePath));
                Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return false;
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                headerRow = sheet.createRow(0);
            }

            // 找到下一個空的欄位位置
            int newColumnIndex = headerRow.getLastCellNum();
            if (newColumnIndex < 0) {
                newColumnIndex = 0;
            }

            // 創建新的標題欄位
            Cell newHeaderCell = headerRow.createCell(newColumnIndex);
            newHeaderCell.setCellValue(columnName);

            // 寫回檔案
            try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
                workbook.write(fos);
                LogManager.getInstance().info("成功添加欄位到 Excel：" + columnName);
                return true;
            }

        } catch (Exception e) {
            LogManager.getInstance().error("添加欄位到 Excel 失敗：" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 從 Excel 檔案中刪除欄位
     */
    public boolean removeColumnFromExcel(int columnIndex) {
        if (filePath == null || columnIndex < 0) {
            return false;
        }

        try (FileInputStream fis = new FileInputStream(new File(filePath));
                Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return false;
            }

            // 遍歷所有列，刪除指定索引的儲存格
            for (Row row : sheet) {
                if (row != null) {
                    Cell cellToDelete = row.getCell(columnIndex);
                    if (cellToDelete != null) {
                        row.removeCell(cellToDelete);
                    }

                    // 將後面的儲存格向前移動
                    int lastCellNum = row.getLastCellNum();
                    for (int i = columnIndex + 1; i < lastCellNum; i++) {
                        Cell cell = row.getCell(i);
                        if (cell != null) {
                            Cell newCell = row.createCell(i - 1, cell.getCellType());
                            copyCellValue(cell, newCell);
                            row.removeCell(cell);
                        }
                    }
                }
            }

            // 寫回檔案
            try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
                workbook.write(fos);
                LogManager.getInstance().info("成功從 Excel 刪除欄位，索引：" + columnIndex);
                return true;
            }

        } catch (Exception e) {
            LogManager.getInstance().error("從 Excel 刪除欄位失敗：" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 複製儲存格的值
     */
    private void copyCellValue(Cell source, Cell target) {
        switch (source.getCellType()) {
            case STRING:
                target.setCellValue(source.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(source)) {
                    target.setCellValue(source.getDateCellValue());
                } else {
                    target.setCellValue(source.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                target.setCellValue(source.getBooleanCellValue());
                break;
            case FORMULA:
                target.setCellFormula(source.getCellFormula());
                break;
            default:
                break;
        }
    }
}
