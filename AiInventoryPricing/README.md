# AI Inventory Pricing System

An intelligent inventory management system that uses AI to generate dynamic pricing and reorder suggestions based on real-time inventory data and market conditions.

## Table of Contents

1. [Overview](#overview)
2. [Features](#features)
3. [Architecture](#architecture)
4. [Technology Stack](#technology-stack)
5. [Getting Started](#getting-started)
   - [Prerequisites](#prerequisites)
   - [Installation](#installation)
   - [Running the Application](#running-the-application)
6. [API Documentation](#api-documentation)
7. [AI Integration](#ai-integration)
8. [Testing](#testing)
9. [Project Structure](#project-structure)
10. [Contributing](#contributing)
11. [License](#license)

## Overview

The AI Inventory Pricing System is a Spring Boot application designed to optimize inventory management through intelligent automation. The system monitors inventory levels and demand patterns, automatically generating AI-powered pricing and reorder suggestions when predefined conditions are met.

Key capabilities include:
- Real-time inventory monitoring
- Automated trigger detection for low stock and demand spikes
- Asynchronous AI-powered suggestion generation
- Dual strategy approach (AI with rule-based fallback)

## Features

### Core Functionality
- **Inventory Management**: Track stock levels, demand velocity, and product lifecycle status
- **Automated Triggers**:
  - *INVENTORY_LOW*: Triggered when stock falls below reorder threshold
  - *DEMAND_SPIKE*: Triggered when demand velocity exceeds 150% of category average
- **AI-Powered Suggestions**:
  - Dynamic pricing recommendations
  - Intelligent reorder quantity calculations
- **Non-Blocking Operations**: All AI processing occurs asynchronously

### Advanced Capabilities
- **Duplicate Prevention**: Ensures only one pending suggestion exists per product-trigger combination
- **Graceful Failure Handling**: Falls back to rule-based strategies when AI is unavailable
- **Real-time Analytics**: Demand velocity calculations with category benchmarking
- **Extensible Design**: Easy addition of new trigger conditions and suggestion types

## Architecture

The system follows a layered architecture with event-driven processing:

```
┌─────────────┐    ┌──────────────┐    ┌─────────────┐
│   REST      │    │   Product    │    │   Event     │
│ Controllers │───▶│   Service    │───▶│ Publisher   │
└─────────────┘    └──────────────┘    └─────────────┘
                                          │
                                          ▼
                                ┌──────────────────┐
                                │   Async Event    │
                                │   Listener       │
                                └──────────────────┘
                                          │
                        ┌─────────────────┴─────────────────┐
                        ▼                                   ▼
              ┌──────────────────┐              ┌──────────────────┐
              │   Pricing AI     │              │   Reorder AI     │
              │   Strategies     │              │   Strategies     │
              └──────────────────┘              └──────────────────┘
                        │                                   │
                        ▼                                   ▼
              ┌──────────────────┐              ┌──────────────────┐
## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git

### Installation

1. Clone the repository:
```bash
git clone https://github.com/your-username/AiInventoryPricing.git
cd AiInventoryPricing
```

2. Set up environment variables for AI services:
```bash
export OPENAI_API_KEY="your-openai-api-key"
```

### Running the Application

1. Build the project:
```bash
mvn clean install
```

2. Run the application:
```bash
mvn spring-boot:run
```

Or alternatively:
```bash
java -jar target/AiInventoryPricing-0.0.1-SNAPSHOT.jar
```

3. Access the application:
- Main API: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:testdb)

## API Documentation

### Product Endpoints

#### Update Stock Level
```
PATCH /api/products/{id}/stock
Content-Type: application/json

{
  "quantity": 5
}
```

#### Increment Demand Velocity
```
POST /api/products/{id}/demand
Content-Type: application/json
## AI Integration

The system integrates with OpenAI's GPT models for advanced analytics:

### Environment Configuration
Set the following environment variable:
```
OPENAI_API_KEY=your_actual_openai_api_key_here
```

### AI Strategies
1. **Pricing Strategy**:
   - Analyzes market conditions, competitor pricing, and inventory levels
   - Generates optimal price points to maximize revenue
   
2. **Reorder Strategy**:
   - Considers supplier lead times, seasonal trends, and demand forecasts
   - Calculates optimal reorder quantities to minimize holding costs

### Fallback Mechanisms
When AI is unavailable or produces invalid results:
- Pricing falls back to markup-based calculation
- Reordering falls back to Economic Order Quantity (EOQ) model

For detailed information about the AI integration, see [AI Integration Documentation](docs/ai-integration.md).

## Testing

The project includes both unit and integration tests:

### Running Unit Tests
```bash
mvn test
```

### Running Specific Test Suites
```bash
# Run AI strategy tests
mvn test -Dtest=*AiStrategy*

## Project Structure

```
src/
├── main/
│   ├── java/com/example/AiInventoryPricing/
│   │   ├── controller/     # REST controllers
│   │   ├── entity/         # JPA entities
│   │   ├── enums/          # Enumerations
│   │   ├── event/          # Event classes
│   │   ├── listener/       # Event listeners
│   │   ├── repository/     # JPA repositories
│   │   ├── service/        # Business services and implementations
│   │   ├── strategy/       # AI and rule-based strategies
│   │   └── AiInventoryPricingApplication.java  # Main application class
│   └── resources/
│       ├── application.properties  # Configuration
│       └── data.sql               # Initial data (if any)
├── test/
│   ├── java/com/example/AiInventoryPricing/
│   │   ├── integration/   # Integration tests
│   │   ├── listener/      # Event listener tests
│   │   ├── repository/    # Repository tests
│   │   ├── service/       # Service layer tests
│   │   └── strategy/      # Strategy tests
│   └── resources/         # Test resources
└── docs/                  # Documentation files
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

Please ensure your code follows the existing style and includes appropriate tests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

*Note: This is a demonstration project built with educational purposes in mind. For production use, additional security measures, persistent storage, and scalability considerations would be necessary.*
# Run integration tests
mvn test -Dtest=*Integration*
```

### Test Coverage
- Entity validation
- Business logic testing
- AI strategy integration
- Repository operations
- REST endpoint validation

{
  "increment": 2
}
```

#### Get Product
```
GET /api/products/{id}
```

#### Get Product List
```
GET /api/products
```

#### Create Product
```
POST /api/products
Content-Type: application/json

{
  "sku": "ELEC-001",
  "name": "Smartphone X1",
  "category": "ELECTRONICS",
  "currentPrice": 699.99,
  "stockLevel": 20,
  "reorderThreshold": 5,
  "demandVelocity": 3,
  "costPrice": 500.00,
  "lifecycleStatus": "ACTIVE"
}
```
              │   Suggestion     │              │   Suggestion     │
              │   Repository     │              │   Repository     │
              └──────────────────┘              └──────────────────┘
```

### Key Components:
1. **Controllers**: Handle HTTP requests for product management
2. **Services**: Business logic including trigger detection
3. **Event System**: Publishes events when trigger conditions are met
4. **Async Listeners**: Process events and coordinate AI strategies
5. **Strategies**: AI and rule-based algorithms for generating suggestions
6. **Repositories**: Data access layer with JPA/Hibernate

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.1.1
- **Database**: H2 In-Memory Database (for development)
- **Persistence**: Spring Data JPA, Hibernate
- **Build Tool**: Apache Maven
- **Testing**: JUnit 5, Mockito
- **Documentation**: Swagger/OpenAPI (planned)
- **External Services**: OpenAI GPT API (configured via environment variables)