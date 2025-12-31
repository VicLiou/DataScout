package da.api.util;

import java.util.List;

import da.api.model.ApiKeyData;
import da.api.service.ExcelService;

/**
 * 測試資料讀取工具
 */
public class DataTestReader {

    private static void printRepeated(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        System.out.println(sb.toString());
    }

    public static void main(String[] args) {
        ExcelService excelService = new ExcelService("src/resource/APIKEY.xlsx");

        System.out.println("開始讀取 Excel 資料...");
        printRepeated("=", 80);

        List<ApiKeyData> dataList = excelService.readAllData();

        System.out.println("總共讀取 " + dataList.size() + " 筆資料\n");

        if (dataList.isEmpty()) {
            System.out.println("⚠️ 沒有讀取到任何資料!");
        } else {
            int index = 1;
            for (ApiKeyData data : dataList) {
                System.out.println("【第 " + index + " 筆資料】");
                System.out.println("  APID:      " + data.getApid());
                System.out.println("  雲:        " + data.getCloud());
                System.out.println("  環境:      " + data.getEnvironment());
                System.out.println("  到期日:    " + data.getExpiryDate());
                System.out.println("  API KEY:   " + data.getApiKey());
                System.out.println("  申請單號:  " + data.getRequestNumber());
                printRepeated("-", 80);
                index++;
            }
        }

        // 驗證到期日是否正確讀取
        long withExpiryDate = dataList.stream()
                .filter(d -> d.getExpiryDate() != null)
                .count();

        System.out.println("\n統計資訊:");
        System.out.println("  有到期日的資料: " + withExpiryDate + " 筆");
        System.out.println("  沒有到期日的資料: " + (dataList.size() - withExpiryDate) + " 筆");

        if (withExpiryDate == 0 && dataList.size() > 0) {
            System.out.println("\n⚠️ 警告: 所有資料都沒有到期日!");
        }
    }
}
