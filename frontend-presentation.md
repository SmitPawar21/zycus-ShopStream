# ShopStream Frontend: Merchandising Console

This document provides a step-by-step walkthrough of the frontend architecture and features for the ShopStream platform. It is designed to help you present the React codebase to an interviewer.

## 1. Overview and Architecture

**File:** `frontend/src/main.jsx` & `frontend/src/App.jsx`
ShopStream's frontend is a single-page application built with React 19, Vite, and Tailwind CSS. It focuses on functional, enterprise-grade density over flashy animations. 

**Key Architectural Choices:**
- **State Management:** Uses React's built-in `useState` and `useEffect`. Data is fetched at the top level (`HomePage`) and passed down to child components to keep the data flow unidirectional and predictable.
- **Polling:** To support asynchronous AI suggestion generation without implementing WebSockets, the app uses a polling mechanism (`setInterval`) that refreshes the data every 5 seconds.
- **Tailwind CSS:** Used for utility-first styling, enabling rapid prototyping of the dense, data-heavy table layouts required for a merchandising console.

## 2. API Integration Layer

**File:** `frontend/src/api.js`
All backend communication is centralized in a single file. 

**Talking Points:**
- **Centralization:** This prevents scattering `fetch` calls across components. It makes it easy to add global error handling, authentication headers, or swap out the `fetch` implementation later.
- **RESTful Endpoints:** The frontend interacts with standard REST endpoints to manage products (`/products`), handle suggestions (`/pricing-suggestions`, `/reorder-suggestions`), and toggle active strategies (`/strategies/active`).

## 3. The Core Dashboard (HomePage)

**File:** `frontend/src/pages/HomePage.jsx`
The `HomePage` component is the heart of the application, managing state and coordinating actions.

**Step-by-Step Breakdown:**

1. **State Initialization:**
   ```javascript
   const [products, setProducts] = useState([]);
   const [pricingSuggestions, setPricingSuggestions] = useState({});
   const [activeStrategies, setActiveStrategies] = useState({ pricingStrategy: '', reorderStrategy: '' });
   ```
   *Explain:* We hold the core data in state. Suggestions are stored as maps (`{ [productId]: suggestion }`) for $O(1)$ lookup when rendering individual product rows.

2. **Data Loading & Polling:**
   ```javascript
   const loadData = useCallback(async () => {
     const [productsData, pricingData, ...] = await Promise.all([
       fetchProducts(), fetchPendingPricingSuggestions(), ...
     ]);
     // ... state updates
   });
   ```
   *Explain:* `Promise.all` ensures we fetch all necessary dashboard data concurrently, minimizing load times.

3. **Global Controls & Filters:**
   *Explain:* The UI provides category filtering and dropdowns to hot-swap the active AI strategies (e.g., switching between `RULE_BASED` and `AI`). When a strategy is changed, an API call is made, and the dashboard immediately reflects the new configuration.

4. **Custom Modals & Notifications:**
   *Explain:* Instead of relying on jarring native browser `alert()` and `prompt()` dialogs, the app uses custom React components (`AddProductModal`, `UpdateStockModal`, `Notification`). This provides a seamless, professional user experience. 

## 4. The Data View (ProductRow)

**File:** `frontend/src/components/ProductRow.jsx`
The `ProductRow` component encapsulates the display and interaction logic for a single product. It is highly data-dense.

**Key UI Ceiling Features to Highlight:**

- **Stock Heatmap (`<StockHeatmap />`):** 
  Instead of just showing a number, a visual progress bar compares the current stock level against the reorder threshold. It dynamically changes color (Green → Yellow → Red) to instantly alert the merchandiser to critical inventory levels.

- **Margin Calculation:**
  The frontend computes the profit margin dynamically based on the current price and cost price. It uses color coding (green for profit, red for loss) to help users evaluate pricing suggestions.

- **Status Badges (`<StatusBadge />`):**
  Clear, color-coded badges indicate if a product is `ACTIVE`, `OUT_OF_STOCK`, or has a `PRICE_REVIEW_PENDING`.

- **Inline Suggestion Blocks (`<SuggestionBlock />`):**
  When the backend's AI generates a suggestion, it appears directly inside the product's row. 
  *Explain:* This block shows the trigger reason, the recommended action (e.g., "$50.00 → $45.00"), the AI's confidence score, and the reasoning behind it. The user can Accept or Reject right there.

- **Price History Toggle:**
  Users can click "▸ Price History" to reveal a mini-table showing recent price changes for that specific product, helping them make informed decisions on new pricing suggestions.

## 5. Interaction Flow Example: Simulating a Sale

When presenting, walk the interviewer through this lifecycle:

1. **User Action:** Click "Simulate Sale" on a product.
2. **Frontend:** Calls `POST /api/products/{id}/orders`.
3. **Backend:** Decrements stock, increments velocity, and checks if stock dropped below the threshold.
4. **Event Trigger:** If stock is low, the backend asynchronously generates an AI reorder suggestion.
5. **Frontend Polling:** The frontend's 5-second polling loop picks up the new pending suggestion.
6. **UI Update:** The `ProductRow` dynamically renders the new `<SuggestionBlock />`.
7. **Resolution:** The user reads the AI's reasoning, clicks "Accept", the stock is updated, and the suggestion is cleared.
