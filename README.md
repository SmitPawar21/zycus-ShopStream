# StockPulse — AI-Powered Merchandising Operations Platform

An enterprise-grade inventory and pricing management system that combines rule-based automation with LLM-powered intelligence to generate real-time pricing and reorder suggestions. Built as a full-stack application with a Spring Boot backend, React frontend, and pluggable AI strategy architecture.

---

## Table of Contents

1. [Overview](#overview)
2. [System Architecture](#system-architecture)
3. [Technology Stack](#technology-stack)
4. [Repository Structure](#repository-structure)
5. [Getting Started](#getting-started)
   - [Prerequisites](#prerequisites)
   - [Backend Setup](#backend-setup)
   - [Frontend Setup](#frontend-setup)
6. [Backend — AiInventoryPricing](#backend--aiinventorypricing)
   - [Domain Model](#domain-model)
   - [API Reference](#api-reference)
   - [Strategy System](#strategy-system)
   - [Event-Driven Triggers](#event-driven-triggers)
   - [AI / LLM Integration](#ai--llm-integration)
   - [Suggestion Lifecycle](#suggestion-lifecycle)
7. [Frontend — Merchandising Console](#frontend--merchandising-console)
   - [Features](#frontend-features)
   - [API Integration](#api-integration)
   - [Component Architecture](#component-architecture)
8. [Architectural Decisions](#architectural-decisions)
9. [Testing](#testing)
10. [Configuration Reference](#configuration-reference)
11. [Contributing](#contributing)

---

## Overview

ShopStream is a merchandising operations platform designed for internal commerce/operations teams. The system monitors product inventory levels and demand patterns, automatically detecting conditions that require pricing adjustments or inventory replenishment. When conditions are detected (low stock, demand spikes), the system asynchronously generates AI-powered suggestions that a human operator reviews, accepts, or rejects through the Merchandising Console.

### Key Capabilities

- **Real-time inventory monitoring** with automated trigger detection
- **Dual strategy architecture** — rule-based engine with LLM-powered AI and automatic fallback
- **Asynchronous suggestion generation** via Spring event system (`@Async` + `@EventListener`)
- **Human-in-the-loop workflow** — AI generates suggestions, operators approve/reject
- **Runtime strategy switching** — swap between AI and rule-based strategies without restart
- **Duplicate prevention** — only one pending suggestion per product-trigger combination
- **Enterprise-grade dashboard** — dense, table-based UI optimized for desktop operations

---

## System Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                     FRONTEND (React + Vite)                         │
│                                                                      │
│  ┌────────────┐  ┌──────────────┐  ┌────────────────────────────┐   │
│  │ HomePage   │  │ ProductRow   │  │ Strategy Config Panel      │   │
│  │ (Polling)  │  │ (Table Row)  │  │ (Pricing / Reorder Select) │   │
│  └────────────┘  └──────────────┘  └────────────────────────────┘   │
│         │                │                      │                    │
│         └────────────────┴──────────────────────┘                    │
│                          │ fetch / PATCH / POST                      │
└──────────────────────────┼───────────────────────────────────────────┘
                           │ HTTP (port 5173 → 8080)
┌──────────────────────────┼───────────────────────────────────────────┐
│                     BACKEND (Spring Boot)                            │
│                          │                                           │
│  ┌───────────────────────▼───────────────────────────────────────┐   │
│  │                    REST Controllers                           │   │
│  │  ProductController │ PricingSuggestionController              │   │
│  │  ReorderSuggestionController │ StrategyController             │   │
│  └───────────────────────┬───────────────────────────────────────┘   │
│                          │                                           │
│  ┌───────────────────────▼───────────────────────────────────────┐   │
│  │                    Service Layer                              │   │
│  │  ProductServiceImpl          SuggestionServiceImpl            │   │
│  │    ├─ Stock/Demand updates     ├─ Strategy-based generation   │   │
│  │    ├─ Trigger detection        ├─ Accept/Reject handling      │   │
│  │    └─ Event publishing         └─ Duplicate prevention        │   │
│  └───────────────────────┬───────────────────────────────────────┘   │
│                          │                                           │
│  ┌───────────────────────▼───────────────────────────────────────┐   │
│  │              Event System (@Async)                            │   │
│  │  ProductEvent → ProductEventListener                          │   │
│  │    Triggers: INVENTORY_LOW, DEMAND_SPIKE                      │   │
│  └───────────────────────┬───────────────────────────────────────┘   │
│                          │                                           │
│  ┌───────────────────────▼───────────────────────────────────────┐   │
│  │              Strategy Registry                                │   │
│  │  ┌─────────────────┐    ┌─────────────────┐                   │   │
│  │  │ PricingStrategy │    │ ReorderStrategy  │                  │   │
│  │  │  ├─ RULE_BASED  │    │  ├─ RULE_BASED   │                  │   │
│  │  │  └─ AI          │    │  └─ AI           │                  │   │
│  │  └─────────────────┘    └─────────────────┘                   │   │
│  └───────────────────────┬───────────────────────────────────────┘   │
│                          │                                           │
│  ┌───────────────────────▼───────────────────────────────────────┐   │
│  │          AI Pipeline (for AI strategy only)                   │   │
│  │  PromptBuilder → LLMGateway → LLMResponseParser → Validator  │   │
│  │                       │                                       │   │
│  │              LiteLLM / OpenAI-compatible API                  │   │
│  └───────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  ┌───────────────────────────────────────────────────────────────┐   │
│  │              Data Layer (H2 In-Memory)                        │   │
│  │  Products │ PricingSuggestions │ ReorderSuggestions            │   │
│  └───────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

| Layer     | Technology                                           |
|-----------|------------------------------------------------------|
| Backend   | Java 17, Spring Boot 3.1.1, Spring Data JPA          |
| Database  | H2 In-Memory (development), Hibernate ORM 6.2        |
| AI/LLM    | LiteLLM-compatible API (Qwen), OpenAI chat format    |
| Frontend  | React 19, Vite 8, Tailwind CSS 4, React Router 7     |
| Build     | Maven 3.6+ (backend), npm (frontend)                 |
| Testing   | JUnit 5, Mockito                                     |

---

## Repository Structure

```
zycus-ShopStream/
├── README.md                          ← This file
├── ADR.md                             ← Architectural Decision Records
│
├── AiInventoryPricing/                ← Spring Boot backend
│   ├── pom.xml
│   ├── README.md                      ← Backend-specific documentation
│   ├── docs/
│   │   └── ai-integration.md         ← Detailed AI integration docs
│   └── src/
│       ├── main/java/.../
│       │   ├── controller/            ← REST API endpoints
│       │   ├── service/impl/          ← Business logic
│       │   ├── entity/                ← JPA entities (Product, Suggestions)
│       │   ├── dto/                   ← Request/Response DTOs
│       │   ├── enums/                 ← Category, Status, TriggerReason
│       │   ├── strategy/             
│       │   │   ├── StrategyRegistry   ← Runtime strategy management
│       │   │   ├── pricing/impl/      ← RuleBasedPricingStrategy, AiPricingStrategy
│       │   │   └── reorder/impl/      ← RuleBasedReorderStrategy, AiReorderStrategy
│       │   ├── ai/
│       │   │   ├── gateway/           ← LLMGateway (HTTP calls to LLM API)
│       │   │   ├── prompt/            ← PromptBuilder (context → prompt)
│       │   │   ├── parser/            ← LLMResponseParser (JSON extraction)
│       │   │   ├── validator/         ← Recommendation validators
│       │   │   └── dto/               ← CommerceContext, Recommendations
│       │   ├── event/                 ← ProductEvent
│       │   ├── listener/              ← ProductEventListener (@Async)
│       │   ├── config/                ← StrategyConfig, RestClientConfig
│       │   ├── exception/             ← GlobalExceptionHandler
│       │   └── repository/            ← JPA repositories
│       ├── main/resources/
│       │   ├── application.properties ← DB, LLM, server config
│       │   └── import.sql             ← Seed data (8 products)
│       └── test/                      ← Unit and integration tests
│
└── frontend/                          ← React merchandising console
    ├── package.json
    ├── vite.config.js
    ├── index.html
    └── src/
        ├── main.jsx                   ← App entry point
        ├── App.jsx                    ← Router setup
        ├── api.js                     ← All backend API client functions
        ├── pages/
        │   └── HomePage.jsx           ← Main dashboard (polling, state, actions)
        └── components/
            └── ProductRow.jsx         ← Product table row with suggestion panels
```

---

## Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **Node.js 18+** and **npm**
- **Git**
- (Optional) LLM API key for AI strategy — system works without it using rule-based fallback

### Backend Setup

```bash
# Clone the repository
git clone https://github.com/SmitPawar21/zycus-ShopStream.git
cd zycus-ShopStream/AiInventoryPricing

# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

The backend starts on **http://localhost:8080**.  
H2 Console is available at **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:testdb`, user: `sa`, password: `password`).

### Frontend Setup

```bash
# In a separate terminal
cd zycus-ShopStream/frontend

# Install dependencies
npm install

# Start the dev server
npm run dev
```

The frontend starts on **http://localhost:5173** and connects to the backend at `localhost:8080`.

---

## Backend — AiInventoryPricing

### Domain Model

**Product** — Core entity representing a merchandised item.

| Field              | Type            | Description                                    |
|--------------------|-----------------|------------------------------------------------|
| `id`               | Long            | Auto-generated primary key                     |
| `sku`              | String (unique) | Stock Keeping Unit identifier                  |
| `name`             | String          | Product display name                           |
| `category`         | Enum            | `ELECTRONICS`, `APPAREL`, `HOME`               |
| `currentPrice`     | BigDecimal      | Current selling price                          |
| `costPrice`        | BigDecimal      | Cost basis (for margin calculations)           |
| `stockLevel`       | Integer         | Current inventory count                        |
| `reorderThreshold` | Integer         | Stock level that triggers reorder alerts       |
| `demandVelocity`   | Integer         | Units sold per day (rolling metric)            |
| `lifecycleStatus`  | Enum            | `ACTIVE`, `PRICE_REVIEW_PENDING`, `OUT_OF_STOCK` |

**PricingSuggestion** — AI/rule-generated pricing recommendation awaiting human review.

| Field              | Type            | Description                                    |
|--------------------|-----------------|------------------------------------------------|
| `currentPrice`     | BigDecimal      | Price at time of suggestion                    |
| `recommendedPrice` | BigDecimal      | Suggested new price                            |
| `changeDirection`  | Enum            | `INCREASE`, `DECREASE`, `HOLD`                 |
| `confidence`       | BigDecimal      | 0.00–1.00 confidence score                     |
| `reasoning`        | String          | Human-readable explanation                     |
| `triggerReason`    | Enum            | `INITIAL`, `INVENTORY_LOW`, `DEMAND_SPIKE`, `MANUAL` |
| `status`           | Enum            | `PENDING`, `ACCEPTED`, `REJECTED`              |

**ReorderSuggestion** — AI/rule-generated reorder recommendation.

| Field                 | Type     | Description                              |
|-----------------------|----------|------------------------------------------|
| `currentStock`        | Integer  | Stock at time of suggestion              |
| `recommendedQuantity` | Integer  | Suggested reorder quantity               |
| `suggestedLeadTimeDays` | Integer | Estimated supplier lead time           |
| `confidence`          | BigDecimal | 0.00–1.00 confidence score             |
| `reasoning`           | String   | Human-readable explanation               |
| `triggerReason`       | Enum     | Same as PricingSuggestion                |
| `status`              | Enum     | `PENDING`, `ACCEPTED`, `REJECTED`        |

### API Reference

#### Product Management

| Method | Endpoint                              | Description                                    |
|--------|---------------------------------------|------------------------------------------------|
| `POST` | `/api/products`                       | Create a new product                           |
| `GET`  | `/api/products`                       | List all products (optional `?status=` and `?category=` filters) |
| `GET`  | `/api/products/{id}`                  | Get product by ID                              |
| `PATCH`| `/api/products/{id}/stock`            | Update stock level `{"stockLevel": 25}`        |
| `POST` | `/api/products/{id}/orders`           | Simulate a sale (stock -1, velocity +1)        |

#### Suggestion Generation

| Method | Endpoint                                     | Description                                         |
|--------|----------------------------------------------|-----------------------------------------------------|
| `POST` | `/api/products/{id}/suggest-pricing`         | Manual pricing suggestion (requires JSON body)       |
| `POST` | `/api/products/{id}/suggest-reorder`         | Manual reorder suggestion (requires JSON body)       |
| `POST` | `/api/products/{id}/suggest-pricing/strategy`| Generate pricing suggestion using active AI strategy |
| `POST` | `/api/products/{id}/suggest-reorder/strategy`| Generate reorder suggestion using active AI strategy |

#### Suggestion Management

| Method  | Endpoint                        | Description                                          |
|---------|---------------------------------|------------------------------------------------------|
| `GET`   | `/api/pricing-suggestions`      | List pricing suggestions (optional `?status=PENDING`) |
| `GET`   | `/api/pricing-suggestions/{id}` | Get pricing suggestion by ID                          |
| `PATCH` | `/api/pricing-suggestions/{id}` | Accept or reject `{"status": "ACCEPTED"}` or `{"status": "REJECTED"}` |
| `GET`   | `/api/reorder-suggestions`      | List reorder suggestions (optional `?status=PENDING`) |
| `GET`   | `/api/reorder-suggestions/{id}` | Get reorder suggestion by ID                          |
| `PATCH` | `/api/reorder-suggestions/{id}` | Accept or reject                                      |

#### Strategy Management

| Method | Endpoint                                        | Description                        |
|--------|--------------------------------------------------|------------------------------------|
| `GET`  | `/api/strategies/available`                      | List all registered strategies     |
| `GET`  | `/api/strategies/active`                         | Get currently active strategies    |
| `POST` | `/api/strategies/activate/pricing/{strategyName}`| Set active pricing strategy        |
| `POST` | `/api/strategies/activate/reorder/{strategyName}`| Set active reorder strategy        |

### Strategy System

The application uses the **Strategy Pattern** with a runtime-configurable registry:

```
StrategyRegistry
 ├── Pricing Strategies
 │    ├── RULE_BASED  ← Default. Deterministic rules based on stock/demand ratios.
 │    └── AI          ← LLM-powered. Calls external API, falls back to RULE_BASED on failure.
 └── Reorder Strategies
      ├── RULE_BASED  ← Default. Calculates quantities from demand velocity and safety stock.
      └── AI          ← LLM-powered. Falls back to RULE_BASED on failure.
```

**Rule-Based Pricing Logic:**
- Stock ≤ 50% of threshold → +15% price (reduce demand)
- Stock ≥ 3× threshold AND velocity ≤ 3 → -15% price (clear excess)
- Velocity ≥ 10 → +5% price (capitalize on demand)
- Velocity ≤ 2 → -5% price (stimulate demand)

**Rule-Based Reorder Logic:**
- Stock ≤ threshold → order 2 weeks demand + safety stock
- Stock ≤ 1.5× threshold → order 1 week demand + smaller safety stock
- Stock > 1.5× threshold → minimal order (user can reject)

### Event-Driven Triggers

When stock or demand changes, the system automatically checks for trigger conditions:

| Trigger          | Condition                                            | Effect                               |
|------------------|------------------------------------------------------|--------------------------------------|
| `INVENTORY_LOW`  | `stockLevel ≤ reorderThreshold`                      | Generates pricing + reorder suggestions |
| `DEMAND_SPIKE`   | `demandVelocity > 150% of category average velocity` | Generates pricing + reorder suggestions |

Triggers are processed asynchronously via `@Async @EventListener` so the originating API call returns immediately.

### AI / LLM Integration

When the AI strategy is active, the system:

1. **Builds a prompt** (`PromptBuilder`) with full product context (price, stock, velocity, category, cost)
2. **Calls the LLM** (`LLMGateway`) via OpenAI-compatible chat completions API
3. **Parses the response** (`LLMResponseParser`) extracting structured JSON from the LLM output
4. **Validates the recommendation** (`PricingRecommendationValidator` / `ReorderRecommendationValidator`)
5. **Falls back to rule-based** if any step fails (empty response, parse error, validation failure)

The LLM endpoint is configured in `application.properties`:

```properties
llm.api.key=<your-api-key>
llm.api.url=https://litellm-qc.zycus.net/v1/chat/completions
llm.model.name=qwen-cursor
```

### Suggestion Lifecycle

```
                    ┌──────────┐
  Strategy generates│  PENDING │
  suggestion ──────►│          │
                    └────┬─────┘
                         │
              ┌──────────┴──────────┐
              │                     │
        ┌─────▼─────┐        ┌─────▼─────┐
        │  ACCEPTED  │        │  REJECTED  │
        │            │        │            │
        └─────┬──────┘        └────────────┘
              │
              ▼
   Side Effects Applied:
   • Pricing: Product.currentPrice ← recommendedPrice
   • Reorder: Product.stockLevel += recommendedQuantity
```

---

## Frontend — Merchandising Console

### Frontend Features

The React dashboard is a functional, enterprise-grade operations console:

- **Product Table** — All products displayed in a dense, scannable data table with SKU, category, price, stock level, demand velocity, and lifecycle status
- **Pending Suggestions** — Inline display of AI/rule-generated pricing and reorder suggestions with confidence scores, reasoning text, and trigger badges
- **Accept / Reject Controls** — One-click approval or rejection of suggestions, immediately updating backend state
- **Simulate Sale** — Decrements stock by 1 and increments demand velocity, triggering automatic suggestion generation when thresholds are crossed
- **Update Stock** — Manual stock level override via browser prompt
- **Generate AI Suggestions** — On-demand trigger of the active AI strategy for any product
- **Strategy Configuration** — Dropdown selectors to switch between RULE_BASED and AI strategies at runtime
- **Add Product** — Quick product creation via sequential prompts
- **Auto-Polling** — Dashboard refreshes data every 5 seconds to pick up async suggestion updates
- **Error Handling** — System error banner for API failures, alert dialogs for action failures

### API Integration

All API communication is centralized in `frontend/src/api.js`:

```javascript
fetchProducts()                          // GET /api/products
fetchPendingPricingSuggestions()          // GET /api/pricing-suggestions?status=PENDING
fetchPendingReorderSuggestions()          // GET /api/reorder-suggestions?status=PENDING
simulateSale(productId)                  // POST /api/products/{id}/orders
updatePricingSuggestion(id, status)      // PATCH /api/pricing-suggestions/{id}
updateReorderSuggestion(id, status)      // PATCH /api/reorder-suggestions/{id}
fetchAvailableStrategies()               // GET /api/strategies/available
fetchActiveStrategies()                  // GET /api/strategies/active
activatePricingStrategy(name)            // POST /api/strategies/activate/pricing/{name}
activateReorderStrategy(name)            // POST /api/strategies/activate/reorder/{name}
generatePricingSuggestion(productId)     // POST /api/products/{id}/suggest-pricing/strategy
generateReorderSuggestion(productId)     // POST /api/products/{id}/suggest-reorder/strategy
updateStock(productId, stockLevel)       // PATCH /api/products/{id}/stock
createProduct(productDto)                // POST /api/products
```

### Component Architecture

```
App.jsx
 └── HomePage.jsx (main container)
      ├── State: products[], pricingSuggestions{}, reorderSuggestions{},
      │          availableStrategies, activeStrategies, loading, error
      ├── Polling: setInterval(loadData, 5000)
      ├── Strategy Config Panel (dropdowns for pricing/reorder strategy)
      └── Product Table
           └── ProductRow.jsx (per product)
                ├── Product Details (name, sku, category)
                ├── Metrics (price, stock, velocity, status badge)
                ├── Suggestion Blocks (pricing + reorder, inline)
                │    ├── Trigger badge
                │    ├── Reasoning text
                │    ├── Confidence score
                │    └── Accept / Reject buttons
                └── Actions (Simulate Sale, Update Stock, Generate AI Pricing/Reorder)
```

---

## Architectural Decisions

Key design decisions are documented in [ADR.md](ADR.md). Summary:

| # | Decision | Choice |
|---|----------|--------|
| 1 | Commerce logic placement | Dedicated strategy components via service layer |
| 2 | Pricing vs reorder contracts | Separate `PricingStrategy` and `ReorderStrategy` interfaces |
| 3 | Runtime strategy switching | Hot-swappable via `StrategyRegistry`, no restart needed |
| 4 | LLM failure handling | Automatic fallback to rule-based strategy (fail-open) |
| 5 | Async decoupling | Spring `@EventListener` + `@Async` for non-blocking triggers |
| 6 | Extensibility | Minimal base with `CommerceContext` extension points |
| 7 | Agentic vs automation | Separate paths — events go through human approval loop |
| 8 | Trigger handling | Distinct `INVENTORY_LOW` and `DEMAND_SPIKE` with tailored prompts |
| 9 | Idempotency | Code-level duplicate prevention for pending suggestions |
| 10 | Accept side-effects | Atomic updates within same transaction |

---

## Testing

```bash
# Run all tests
cd AiInventoryPricing
mvn test

# Run specific test suites
mvn test -Dtest=RuleBasedPricingStrategyTest
mvn test -Dtest=RuleBasedReorderStrategyTest
mvn test -Dtest=StrategyRegistryTest
```

---

## Configuration Reference

All configuration is in `AiInventoryPricing/src/main/resources/application.properties`:

| Property                            | Default                                              | Description                        |
|-------------------------------------|------------------------------------------------------|------------------------------------|
| `server.port`                       | `8080`                                               | Backend HTTP port                  |
| `spring.datasource.url`            | `jdbc:h2:mem:testdb`                                 | H2 in-memory database URL         |
| `spring.jpa.hibernate.ddl-auto`    | `create`                                             | Schema recreated on each startup   |
| `spring.h2.console.enabled`        | `true`                                               | Enable H2 web console             |
| `llm.api.key`                      | *(empty)*                                            | LLM API bearer token              |
| `llm.api.url`                      | `https://litellm-qc.zycus.net/v1/chat/completions`  | LLM chat completions endpoint     |
| `llm.model.name`                   | `qwen-cursor`                                        | LLM model identifier              |

> **Note:** If `llm.api.key` is not set, the AI strategy will return empty responses and automatically fall back to the rule-based strategy. The system is fully functional without an LLM API key.

---

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/YourFeature`)
3. Commit your changes (`git commit -m 'Add YourFeature'`)
4. Push to the branch (`git push origin feature/YourFeature`)
5. Open a Pull Request

---

*Built by SmitPawar21 as part of the Zycus ShopStream project.*
