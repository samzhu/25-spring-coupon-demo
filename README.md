# 軟體架構文件：購物車功能

## 1. 簡介與目標

### 1.1 文件目的
本文件旨在描述「電商優惠券錯誤疊加範例」應用程式的軟體架構。它將闡述系統的各個組成部分、它們之間的關係、運行時行為以及關鍵的架構決策。本文件是理解此演示應用程式設計和實現的基礎。

### 1.2 應用程式目標
此應用程式的核心目標是：
* **演示錯誤邏輯**：清晰地展示當允許多張固定金額優惠券同時套用並直接疊加其折扣金額時的計算結果。這是主要的教育目的。
* **功能完整性**：提供一個基本可操作的購物車界面，允許用戶查看固定的商品、透過勾選選擇多張優惠券，並即時看到基於（錯誤的）業務邏輯計算出的價格。
* **教育意義**：作為一個教學範例，幫助開發者和學習者理解在設計優惠券系統時，若未仔細考慮規則（如排他性、疊加限制），可能導致的邏輯缺陷和非預期後果。
* **技術展示**：展示一個使用特定技術棧（Java、Spring Boot、Mustache、jQuery、AJAX）實現的 Web 應用程式。

### 1.3 品質目標
* **響應性 (Responsiveness)**：前端操作（如勾選優惠券）後，價格更新應能快速響應，提供流暢的用戶體驗。
* **計算準確性 (依據錯誤邏輯)**：雖然業務邏輯本身是「錯誤的」（允許無限制疊加），但基於此定義的錯誤邏輯，其數學計算必須是準確的。
* **可理解性 (Understandability)**：架構和程式碼應易於理解，以符合其教育和演示的目的。
* **可部署性 (Deployability)**：作為一個演示專案，應易於在本機環境中運行和部署。

---

## 2. 上下文與範圍

### 2.1 業務上下文
此應用程式模擬一個極簡化的電商購物車結帳環節，其核心關注點在於優惠券的套用和計算。它並非一個完整的電商解決方案，而是特意設計用來凸顯一種常見的優惠券邏輯設計缺陷。

### 2.2 技術上下文
* **後端**: Java 21, Spring Boot 3.5.0, Spring MVC
* **前端模板引擎**: Mustache
* **前端腳本**: jQuery (用於 AJAX 和 DOM 操作)
* **數據儲存**: 應用程式內存 (In-memory ConcurrentHashMaps) 用於儲存產品和優惠券數據，無外部資料庫。
* **構建工具**: Gradle
* **開發工具**: Spring Boot DevTools (支援熱重載)

### 2.3 系統範圍與邊界

#### 2.3.1 系統邊界圖

```mermaid
graph LR
    A["使用者 (Browser)"]

    subgraph AppScope ["優惠券Demo應用"]
        direction TB
        B1["Controller"]
        B2["Service"]
        B3["Repositories"]
        B4["DTOs/Models"]
        B1 --> B2
        B2 --> B3
        B1 -.-> B4
        B2 -.-> B4
        B3 -.-> B4
    end

    A -- AJAX --> AppScope
    AppScope -- HTML/JS --> A
```

* **使用者 (User)**：透過網頁瀏覽器與系統互動。
* **網頁瀏覽器 (Web Browser)**：執行 HTML、CSS 和 JavaScript (jQuery)，負責呈現使用者介面和發起 AJAX 請求。
* **電商優惠券錯誤疊加範例應用 (Application Server)**：
    * 接收 HTTP 請求。
    * 提供初始 HTML 頁面。
    * 提供 API 端點 (`/cart/calculate`) 處理 AJAX 計算請求。
    * 執行業務邏輯（包括錯誤的優惠券計算）。
    * 從內部數據存儲中讀取產品和優惠券信息。

#### 2.3.2 功能範圍
**範圍內 (In Scope):**
* 展示固定的購物車商品列表（包含 4 個預設產品）。
* 展示所有可用的固定金額優惠券列表（包含 4 個預設優惠券）。
* 允許使用者透過核取方塊選擇多張優惠券。
* 前端透過 AJAX 將選中的優惠券代碼發送到後端，包含完整的錯誤處理和用戶反饋。
* 後端根據選中的優惠券（錯誤地）疊加計算折扣，並提供詳細的計算日誌。
* 後端返回計算結果 (原始總價、折扣後總價、總折扣、套用的優惠券列表)。
* 前端動態更新價格和已套用優惠券信息，並在發生錯誤時顯示適當的錯誤訊息。
* 允許使用者清除所有已選的優惠券。

**範圍外 (Out of Scope):**
* 使用者認證與授權。
* 完整的訂單生命週期管理（如下單、支付、配送）。
* 庫存管理。
* 複雜的優惠券規則（如有效期限、最低消費、特定商品適用、排他性規則的「正確」實現）。
* 使用者動態添加、移除或修改購物車中商品的功能（商品列表是固定的）。
* 購物車狀態的持久化儲存（例如，資料庫）。在此範例中，購物車的商品是固定的，優惠券狀態由前端管理並在每次計算時傳遞。
* 國際化與本地化。

---

## 3. 建構區塊視圖 (Building Block View - Level 1)
此視圖展示了系統的高層模組及其職責和關係。
```mermaid
graph TD
    UI["<b>使用者介面 (UI)</b><br/>(checkout.mustache + jQuery)<br/>- 顯示購物車內容、可用優惠券<br/>- 處理使用者勾選優惠券操作<br/>- 發起 AJAX 請求到後端進行計算<br/>- 動態更新價格顯示、已套用優惠券列表"]

    WebLayer["<b>Web 層 (Web Layer)</b><br/>(CartController.java)<br/>- 接收 HTTP 請求<br/>- 提供初始頁面數據<br/>- 提供 /cart/calculate API 端點<br/>- 調用服務層進行計算<br/>- 返回 CalculationResultDto (JSON)"]

    ServiceLayer["<b>服務層 (Service Layer)</b><br/>(CartService.java)<br/>- 接收 ShoppingCartInput DTO<br/>- 包含核心業務邏輯：<br/>  - 計算原始總價<br/>  - 查找優惠券 (透過 Repository)<br/>  - <b>執行錯誤的優惠券疊加計算邏輯</b><br/>- 返回 CalculationResultDto"]

    DataAccessLayer["<b>數據存取層 (Data Access Layer)</b><br/>(ProductRepository.java, CouponRepository.java)<br/>- 提供數據存取接口<br/>- 實作 (In-Memory 儲存)"]

    DataInitializer["<b>數據初始化器 (Data Initializer)</b><br/>(DataInitializer.java)<br/>- 應用程式啟動時填充範例數據"]
    
    DTOs["<b>DTOs</b><br/>ShoppingCartInput<br/>CalculationResultDto<br/>CartItemInput"]
    Models["<b>Models</b><br/>Product, Coupon<br/>CartItem, ShoppingCart"]

    UI -- "HTTP (GET /cart, POST /cart/calculate)" --> WebLayer;
    WebLayer -- "方法調用 (傳遞 DTO)" --> ServiceLayer;
    ServiceLayer -- "方法調用" --> DataAccessLayer;
    DataInitializer -- "(初始化時使用)" --> DataAccessLayer;

    WebLayer -. "使用" .-> DTOs;
    ServiceLayer -. "使用" .-> DTOs;
    WebLayer -. "使用 (組織視圖數據)" .-> Models;
    ServiceLayer -. "使用 (查找)" .-> Models;
    DataAccessLayer -. "管理" .-> Models;
    
    style UI fill:#lightgrey,stroke:#333,stroke-width:2px
    style WebLayer fill:#lightblue,stroke:#333,stroke-width:2px
    style ServiceLayer fill:#lightgreen,stroke:#333,stroke-width:2px
    style DataAccessLayer fill:#orange,stroke:#333,stroke-width:2px
    style DataInitializer fill:#yellow,stroke:#333,stroke-width:2px
    style DTOs fill:#whitesmoke,stroke:#333,stroke-width:1px,stroke-dasharray: 5 5
    style Models fill:#whitesmoke,stroke:#333,stroke-width:1px,stroke-dasharray: 5 5
```
### 3.1 組件職責
* **使用者介面 (UI)**：負責呈現資訊給使用者，並捕獲使用者輸入（勾選優惠券）。它使用 jQuery 透過 AJAX 與後端通訊，實現頁面的動態更新，避免整頁刷新。
* **Web 層 (`CartController`)**: 作為應用程式的入口點，處理來自前端的 HTTP 請求。它將請求路由到適當的服務，並將服務的結果轉換為適合前端的格式（HTML 頁面或 JSON 數據）。
* **服務層 (`CartService`)**: 封裝核心的業務邏輯。在此應用中，主要是根據輸入的商品和優惠券信息，執行（錯誤的）價格計算。此服務設計為無狀態的。
* **數據存取層 (`ProductRepository`, `CouponRepository`)**: 抽象化數據的儲存和檢索。目前使用記憶體內存儲，但此層的設計允許未來更換為其他儲存機制（如資料庫）。
* **數據初始化器 (`DataInitializer`)**: 確保應用程式啟動時有可用的範例數據，方便演示。
* **DTOs**: 用於在 Web 層和服務層之間傳遞數據，實現層間解耦。
* **Models**: `Product` 和 `Coupon` 是核心領域模型。`CartItem` 和 `ShoppingCart` 主要由 `CartController` 用於組織數據以傳遞給 Mustache 模板進行初始頁面渲染。

---

## 4. 執行時期視圖 (Runtime View)
此部分描述系統在執行期間關鍵場景下的行為。

### 4.1 場景 1：使用者載入購物車頁面
1.  **使用者**: 在瀏覽器中輸入 URL (例如 `http://localhost:8080/cart`)。
2.  **瀏覽器**: 發送 `GET /cart` HTTP 請求到應用程式伺服器。
3.  **`CartController`**: `viewCartPage` 方法被調用。
    * 調用 `ProductRepository.findAll()` 獲取所有產品資訊。
    * 調用 `CouponRepository.findAll()` 獲取所有可用優惠券資訊。
    * 準備用於初始顯示的固定商品列表 (`initialFixedCartItemsForDisplay`)。
    * 將這些數據放入 `Model` 中。
4.  **Spring MVC**: 使用 `Model` 中的數據渲染 `checkout.mustache` 模板。
5.  **應用程式伺服器**: 將渲染後的 HTML 頁面返回給瀏覽器。
6.  **瀏覽器**: 顯示購物車頁面。
    * 頁面上的 jQuery 腳本在 `$(document).ready()` (或 `DOMContentLoaded`) 時執行。
    * 前端 JavaScript (`updateCartCalculation` 函數) 會立即發起一次 AJAX `POST /cart/calculate` 請求，傳遞固定的商品項目和空的優惠券列表，以獲取並顯示初始的（未打折的）總價。

### 4.2 場景 2：使用者勾選/取消勾選優惠券
1.  **使用者**: 在瀏覽器中勾選或取消勾選一個或多個優惠券核取方塊。
2.  **瀏覽器 (jQuery)**:
    * `change` 事件被觸發。
    * JavaScript 更新其內部的 `currentAppliedCouponCodes` 列表，包含所有當前被勾選的優惠券代碼。
    * 調用 `updateCartCalculation()` 函數。
3.  **瀏覽器 (jQuery - `updateCartCalculation` 函數)**:
    * 建構 `ShoppingCartInput` DTO，包含固定的商品項目列表 (`fixedCartItems`) 和更新後的 `currentAppliedCouponCodes`。
    * 發起 AJAX `POST /cart/calculate` 請求到應用程式伺服器，請求體為 JSON 格式的 `ShoppingCartInput` DTO。
    * 包含完整的錯誤處理邏輯，在發生錯誤時顯示適當的錯誤訊息。
4.  **`CartController`**: `calculateCart` 方法被調用。
    * `@RequestBody` 將 JSON 請求體反序列化為 `ShoppingCartInput` DTO。
    * 調用 `cartService.calculateTotalsFromDto(shoppingCartInput)`。
5.  **`CartService`**: `calculateTotalsFromDto` 方法執行：
    * 根據 `shoppingCartInput.items` 計算原始總價，包含產品存在性檢查。
    * 遍歷 `shoppingCartInput.couponCodes`：
        * 對於每個有效的優惠券代碼，從 `CouponRepository` 獲取 `Coupon` 物件。
        * 記錄詳細的計算過程日誌。
        * **錯誤邏輯**：將此優惠券的 `discountAmount` 累加到總折扣中。
        * 將有效的 `Coupon` 物件加入到結果列表。
    * 計算折扣後總價，確保不會出現負數。
    * 返回一個包含計算結果的 `CalculationResultDto`。
6.  **`CartController`**: 將 `CalculationResultDto` 序列化為 JSON 並作為 HTTP 響應返回給前端。
7.  **瀏覽器 (jQuery - AJAX `success` 回調)**:
    * 接收到包含 `CalculationResultDto` 的 JSON 響應。
    * 調用 `renderCalculationResult(result)` 函數。
8.  **瀏覽器 (jQuery - `renderCalculationResult` 函數)**:
    * 使用返回的數據更新頁面上的相應元素（原始總價、折扣後總價、已套用優惠券列表、總節省金額等）。

---

## 5. 架構決策

* **AD1: 技術棧選擇 (Java/Spring Boot, jQuery, Mustache)**
    * **決策**: 選用 Java + Spring Boot 作為後端，jQuery 處理前端 AJAX 和 DOM 操作，Mustache 作為模板引擎。
    * **理由**:
        * Spring Boot：快速開發，內嵌伺服器，簡化配置，適合演示專案。
        * Java：廣泛使用，生態成熟。
        * jQuery：簡化前端 DOM 操作和 AJAX，對於此規模的演示項目足夠且易於理解。
        * Mustache：輕量級模板引擎，語法簡單，與 Spring Boot 整合良好。
    * **替代方案**:
        * 後端：Node.js/Express, Python/Flask (更輕量，但 Java/Spring Boot 更能體現企業級場景的簡化版)。
        * 前端：原生 JavaScript, Vue.js, React (對於此規模可能過於複雜，jQuery 更直接)。
* **AD2: 無狀態服務層 (`CartService`)**
    * **決策**: `CartService` 設計為無狀態，其計算方法 (`calculateTotalsFromDto`) 的輸出僅依賴於其輸入參數 (`ShoppingCartInput`)。
    * **理由**:
        * 簡化服務邏輯，易於理解和測試。
        * 符合現代 API 設計趨勢，易於擴展（如果需要）。
        * 計算邏輯與狀態管理分離。
    * **替代方案**: 有狀態的服務層，服務內部持有購物車狀態（類似早期版本）。對於此演示，無狀態更清晰。
* **AD3: 前端管理購物車狀態 (優惠券選擇)**
    * **決策**: 購物車中的商品項目是固定的。已選擇的優惠券列表狀態由前端 JavaScript 維護，並在每次計算請求時透過 AJAX 發送給後端。
    * **理由**:
        * 實現類似 SPA 的即時反饋體驗，避免整頁刷新。
        * 簡化後端，後端不需要管理使用者會話中的購物車狀態（對於優惠券部分）。
    * **替代方案**: 完全由後端 Session 管理購物車狀態。對於演示 AJAX 和前後端分離的計算，前端管理更合適。
* **AD4: 固定商品列表**
    * **決策**: 購物車中的商品項目在應用程式啟動時固定，使用者不能修改。
    * **理由**: 簡化演示範圍，使核心焦點集中在優惠券的（錯誤）計算邏輯上，避免實現完整的商品管理和購物車操作功能。
    * **替代方案**: 允許使用者動態添加/修改購物車商品。會增加專案複雜度，偏離核心演示目標。
* **AD5: 記憶體內數據存儲 (`In-Memory Repositories`)**
    * **決策**: 產品和優惠券數據使用 `ConcurrentHashMap` 存儲在應用程式記憶體中。
    * **理由**:
        * 對於演示專案足夠，啟動快速，無需外部資料庫配置。
        * 易於透過 `DataInitializer` 填充初始數據。
    * **替代方案**: 使用 H2 內存資料庫或外部資料庫。會增加配置複雜性。
* **AD6: 刻意設計的錯誤優惠券邏輯 (允許多張固定金額券疊加)**
    * **決策**: 優惠券計算邏輯故意設計為允許使用者選擇多張固定金額的優惠券，並且這些優惠券的折扣金額會直接疊加。
    * **理由**: 此為專案的核心教育目標，用以展示不當設計可能導致的財務風險或非預期結果。
    * **替代方案**: 實現正確的優惠券排他性、疊加限制、最優組合等邏輯。這將是一個不同的專案目標。
* **AD7: DTO 用於服務層接口**
    * **決策**: `CartService` 的公共計算接口接收 `ShoppingCartInput` DTO 並返回 `CalculationResultDto`。
    * **理由**:
        * 明確服務層的數據契約。
        * 使服務層不直接依賴於 Web 層的請求細節或領域模型的內部表示（儘管在此例中 DTO 與模型結構相似）。
        * 方便 API 的測試和演進。
    * **替代方案**: 服務層直接接收 HttpServletRequest 或多個原始參數。DTO 更結構化。

---

## 6. 詞彙表

- **AJAX (Asynchronous JavaScript and XML)**：一種在無需重新載入整個網頁的情況下，能夠更新部分網頁的技術。此專案中用於前端與後端計算 API 的非同步通訊。
- **API 端點 (API Endpoint)**：後端提供服務的特定 URL，前端可以透過此 URL 發起請求以獲取數據或執行操作 (例如 `/cart/calculate`)。
- **Controller (控制器)**：在 MVC (Model-View-Controller) 架構中，負責接收使用者輸入、調用模型和服務進行處理，並選擇適當的視圖來呈現結果的組件。在此專案中為 `CartController`。
- **DTO (Data Transfer Object - 數據傳輸物件)**：一個簡單的物件，用於在應用程式的不同層之間（例如 Controller 和 Service 之間）傳輸數據。例如 `ShoppingCartInput`, `CalculationResultDto`。
- **DOM (Document Object Model - 文件物件模型)**：一個程式設計接口，用於 HTML 和 XML 文件。它將文件表示為一個由節點和物件組成的樹狀結構，允許程式和腳本動態地存取和更新文件的內容、結構和樣式。
- **Gradle**: 一個基於 Apache Ant 和 Apache Maven 概念的開源構建自動化系統。
- **In-Memory Storage (記憶體內存儲)**：將數據直接存儲在應用程式的運行記憶體中，而不是外部持久化儲存（如資料庫）。應用程式關閉後數據會丟失。
- **jQuery**: 一個快速、小巧且功能豐富的 JavaScript 函式庫。它使 HTML 文件遍歷和操作、事件處理、動畫和 Ajax 等操作簡單得多。
- **JSON (JavaScript Object Notation)**：一種輕量級的數據交換格式，易於人閱讀和編寫，也易於機器解析和生成。
- **Model (模型)**：在 MVC 架構中，代表應用程式的數據和業務邏輯。在此專案中，如 `Product`, `Coupon`, `ShoppingCart` 等類別。
- **Mustache**: 一種無邏輯 (logic-less) 的模板引擎，可用於 HTML、配置文件、原始碼等。
- **Repository (倉儲)**：一種設計模式，用於抽象化數據的存取。提供一個類似集合的接口來存取領域物件。例如 `ProductRepository`, `CouponRepository`。
- **Service Layer (服務層)**：封裝應用程式核心業務邏輯的層次。例如 `CartService`。
- **Stateless (無狀態)**：指服務或組件不保存先前與客戶端互動的任何信息（狀態）。每個請求都被視為獨立的事務。
- **UI (User Interface - 使用者介面)**：使用者與應用程式互動的視覺部分。在此專案中為 `checkout.mustache` 渲染的 HTML 頁面。
- **錯誤處理 (Error Handling)**：系統在各層級（前端 JavaScript、後端 Controller、Service）都實現了完整的錯誤處理機制，包括：
  * 前端 AJAX 請求的錯誤處理和用戶反饋。
  * 後端服務層的產品和優惠券存在性檢查。
  * 詳細的計算過程日誌記錄。