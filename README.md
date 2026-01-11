# DataScout

## 專案簡介
DataScout 是一個基於 Java Swing 開發的高效能桌面應用程式，專門設計用於管理、監控與探索 Excel 數據。它不僅提供便捷的 Excel 檔案讀取與編輯功能，核心特色在於其智慧型的**到期提醒服務**與**檔案即時監控**，讓使用者能掌握關鍵數據的時效性。

## 主要功能

### 📊 數據管理
- **Excel 深度整合**：完整支援 `.xlsx` 格式檔案的讀取、寫入與建立。
- **自定義視圖**：提供彈性的欄位設定 (Column Config)，使用者可依需求調整顯示欄位與標題名稱。
- **最近存取清單**：自動記錄並快速存取最近開啟的檔案。

### ⚡ 智慧監控
- **即時熱更新 (Real-time Updates)**：自動偵測來源 Excel 檔案的外部變更，並即時刷新應用程式介面，確保數據永遠同步。
- **到期提醒服務 (Expiry Reminder)**：針對具備時效性的資料（如合約到期、憑證更新等）進行背景監控，並主動發出提醒。

### 🛠 使用者體驗
- **現代化介面**：採用簡潔清晰的 Swing GUI 設計。
- **系統托盤整合**：支援「縮小至托盤」功能，讓應用程式能在背景持續運作而不干擾工作列。
- **操作日誌**：內建日誌查看器，記錄關鍵操作與系統狀態。

## 技術架構
本專案採用穩健的 Java 生態系技術：

- **核心語言**：Java 17
- **建置工具**：Maven 3.x
- **GUI 框架**：Java Swing (客製化 UI 元件)
- **資料處理**：Apache POI (Excel 處理), Lombok (程式碼簡化)
- **系統整合**：JNA (原生系統呼叫)
- **部署打包**：Launch4j (Windows .exe 封裝), Maven Shade Plugin (Uber-JAR)

## 開發環境建置 (Prerequisites)

在開始之前，請確保您的環境已安裝：
1. **Java Development Kit (JDK) 17** 或更高版本。
2. **Maven** 建置工具。

## 建置與執行 (Build & Run)

### 1. 編譯專案
在專案根目錄執行以下指令以下載依賴並編譯程式碼：
```bash
mvn clean compile
```

### 2. 打包應用程式
執行以下指令產生可執行的 JAR 檔與 EXE 檔：
```bash
mvn clean package
```
*註：若要跳過測試可加上 `-DskipTests` 參數。*

### 3. 啟動程式
打包完成後，您可以在 `target/` 目錄下找到輸出檔案：

*   **Windows 可執行檔 (推薦)**：
    ```
    target/DataScout.exe
    ```
*   **Java JAR 檔**：
    ```bash
    java -jar target/data-scout-1.0.0-RELEASE.jar
    ```

## 專案結構
```
src/main/java/da/api/
├── App.java                 # 應用程式進入點 (Main Class)
├── model/                   # 資料模型 (如 ExcelData, ColumnConfig)
├── service/                 # 核心邏輯 (ExcelService, ExpiryReminderService)
├── util/                    # 工具類別 (AppSettings, LogManager)
└── view/                    # 使用者介面 (MainFrameView, Dialogs)
    └── element/             # UI 元件 (Panel, MenuBar)
```

## 作者
*   **DDAD**

---
© 2024-2026 DataScout Project. All Rights Reserved.
