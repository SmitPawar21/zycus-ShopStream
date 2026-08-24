# AI Integration Documentation

## Overview
This document explains how to configure and use the AI-powered pricing and reorder strategies in the AiInventoryPricing application.

## Architecture
The AI integration follows a clean architecture pattern:

```
AiPricingStrategy / AiReorderStrategy
           ↓
    CommerceContext
           ↓
    PromptBuilder
           ↓
    LLMGateway
           ↓
        LLM
           ↓
     LLMResponseParser
           ↓
    RecommendationValidator
```

## Configuration

### Environment Variables
To enable AI functionality, set the following environment variables:

```bash
LLM_API_KEY=your_api_key_here
LLM_API_URL=https://litellm-qc.zycus.net/v1/chat/completions  # Optional, defaults to this URL
LLM_MODEL_NAME=qwen-cursor  # Optional, defaults to this model
```

If `LLM_API_KEY` is not set, the AI strategies will automatically fall back to rule-based strategies.

## How It Works

### 1. Commerce Context Creation
The AI strategies first create a structured commerce context from the product data, including:
- Product ID, SKU, name, category
- Current price, stock level, reorder threshold
- Demand velocity, lifecycle status, cost price

### 2. Prompt Generation
Based on the commerce context and trigger reason (INVENTORY_LOW, DEMAND_SPIKE, etc.), the system generates appropriate prompts for the LLM.

### 3. LLM Interaction
The system makes HTTP calls to the LLM API with proper authentication and error handling:
- Timeout protection
- Network error handling
- HTTP error handling
- Empty response handling

### 4. Response Parsing
LLM responses are parsed from JSON format into structured recommendation objects:
- Pricing recommendations include price, direction, confidence, and reasoning
- Reorder recommendations include quantity, lead time, confidence, and reasoning

### 5. Validation
Recommendations are validated against business rules:
- Confidence thresholds (minimum 0.50)
- Price ranges (minimum $0.01)
- Quantity ranges (1-10,000 units)
- Lead time ranges (1-365 days)

### 6. Fallback Mechanism
If any step fails or produces invalid results, the system automatically falls back to rule-based strategies.

## Available Strategies

### Pricing Strategies
- **RULE_BASED**: Traditional rule-based pricing strategy
- **AI**: AI-powered pricing strategy (falls back to RULE_BASED if LLM unavailable)

### Reorder Strategies
- **RULE_BASED**: Traditional rule-based reorder strategy
- **AI**: AI-powered reorder strategy (falls back to RULE_BASED if LLM unavailable)

## Runtime Strategy Switching

You can switch between strategies at runtime using the API endpoints:

```
POST /api/strategy/pricing/switch?strategyName=AI
POST /api/strategy/reorder/switch?strategyName=RULE_BASED
```

## Usage Examples

### Generate AI Pricing Recommendation
```
POST /api/suggestions/pricing/generate/{sku}?strategy=AI
```

### Generate AI Reorder Recommendation
```
POST /api/suggestions/reorder/generate/{sku}?strategy=AI
```

## Testing

Run unit tests:
```bash
mvn test
```

Run integration tests:
```bash
mvn test -Dtest=AiStrategyIntegrationTest
```

## Extensibility

To add support for different LLM providers:
1. Implement a new `LLMGateway` implementation
2. Update the configuration to use your new gateway
3. Ensure proper error handling and fallback mechanisms