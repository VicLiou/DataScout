package da.api.model;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTable;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class JTableModel {

    private static final String FILEPATH = "D:\\gitSpace\\api-infodesk\\src\\resource\\APIKEY.xlsx";

    public JTable returnExcelData() {

        List<String> title = new ArrayList<>();
        List<List<String>> data = new ArrayList<>();

        try {
            FileInputStream file = new FileInputStream(FILEPATH);
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet ws = wb.getSheetAt(0);
            Iterator<Row> rowIterator = ws.iterator();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                List<String> rowDataList = new ArrayList<>();

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();

                    if (row.getRowNum() == 0) {
                        title.add(row.getCell(cell.getColumnIndex()).toString());
                    } else {
                        rowDataList.add(row.getCell(cell.getColumnIndex()).toString());
                    }
                }
                if (row.getRowNum() != 0) {
                    data.add(rowDataList);
                }

            }

            System.out.println(title);
            System.out.println(data);

            wb.close();
            file.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new JTable(array22Object2(data), array2Strings(title));
    }

    private Object[][] array22Object2(List<List<String>> data) {
        int rows = data.size();
        int cols = data.get(0).size();
        Object[][] dataArray = new String[rows][cols];
        for (int i = 0; i < rows; i++) {
            dataArray[i] = data.get(i).toArray(new String[0]);
        }
        return dataArray;
    }

    private String[] array2Strings(List<String> title) {
        String[] titleArray = new String[title.size()];
        for (int i = 0; i < title.size(); i++) {
            titleArray[i] = title.get(i);
        }
        return titleArray;
    }
}
