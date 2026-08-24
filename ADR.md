# Architectural Decision Record (ADR)

## 1. Commerce Logic Placement

### Context

Commerce decision-making logic (pricing and inventory replenishment recommendations) needs to be placed in a maintainable, testable location that supports both manual on-demand requests and automated event-driven triggers.

### Options

- Embed logic directly in controllers
- Place in service layer with clear interfaces
- Create dedicated strategy/advisor components
- Externalize to separate services/microservices

### Decision

Place commerce logic in dedicated advisor components (`CommerceAdvisor`) accessed through service layer interfaces. Controllers delegate to services which coordinate advisor interactions.

### Tradeoffs

__Pro:__ Clean separation of concerns, highly testable, enables strategy pattern\
__Con:__ Additional abstraction layer adds complexity for simple cases

## 2. Unified vs Split Commerce Contracts

### Context

Commerce recommendations involve both pricing and reorder decisions that are inherently related in retail operations. Need to decide whether to unify these in one contract or keep separate.

### Options

- Unified `CommerceAdvisor` returning both pricing and reorder recommendations
- Separate `PricingStrategy` and `ReorderStrategy` contracts
- Hybrid with both approaches available

### Decision

Use unified `CommerceAdvisor` interface returning both recommendation types in a single call.

### Tradeoffs

__Pro:__ Holistic merchandising decisions, single AI call efficiency, natural extension for Sprint 2 features\
__Con:__ Sacrifices granular fallback capability (can't have AI pricing with rule-based reorder), harder to test in isolation

## 3. Runtime Strategy Switching

### Context

Different commerce strategies (rule-based, AI-powered, competitor-aware) need to be switchable without code changes or system restarts for experimentation and fallback purposes.

### Options

- Compile-time strategy selection via dependency injection
- Configuration-driven strategy switching with restart required
- Runtime-configurable strategy selection without restart
- Feature flags for strategy activation

### Decision

Runtime-configurable strategy selection using Spring configuration properties with no restart required.

### Tradeoffs

__Pro:__ Enables A/B testing, immediate fallback on failures, easy experimentation\
__Con:__ Requires careful design of common interfaces, slight performance overhead

## 4. LLM Failure Handling

### Context

AI-powered commerce advisors may encounter timeouts, quota limits, unparsable responses, or degraded service quality requiring graceful degradation.

### Options

- Fail-closed (return errors to users)
- Fail-over to hardcoded defaults
- Fallback to alternative strategy implementation
- Retry mechanisms with exponential backoff

### Decision

Fallback to rule-based strategy implementation on any AI failure with silent handling (fail-open).

### Tradeoffs

__Pro:__ System remains functional under AI failures, transparent operation to users, business continuity assured\
__Con:__ May miss AI-powered optimizations during outages, harder to detect/focus on fixing AI issues

## 5. Agentic Loop Decoupling

### Context

Inventory signals (low stock, demand spikes) must trigger recommendation generation asynchronously to avoid blocking user-facing operations.

### Options

- Synchronous processing during stock/order updates
- Scheduled polling for condition detection
- Event-driven asynchronous processing
- Message queue-based decoupling

### Decision

Event-driven asynchronous processing using Spring `@EventListener` with `@Async`.

### Tradeoffs

__Pro:__ Immediate user response times, clean decoupling, scalable processing\
__Con:__ Increased system complexity, eventual consistency model, harder debugging/tracing

## 6. Extensibility/Exclusions

### Context

Future Sprint 2 requirements include competitor prices, margin floors, and supplier catalogs. Current design must accommodate extension while avoiding over-engineering.

### Options

- Comprehensive upfront modeling of all possible future attributes
- Minimal base with extension points via common context objects
- Plugin architecture with dynamic loading
- YAGNI (You Aren't Gonna Need It) - minimal viable scope

### Decision

Minimal base with extension points in `ProductContext` object, deferring complex plugin mechanisms.

### Tradeoffs

__Pro:__ Placeholder costs nothing, clear extension path for known future work, avoids over-engineering\
__Con:__ May require refactoring when actual extensions implemented, less comprehensive than full design

## 7. Agentic vs Ordinary Automation

### Context

Recommendation generation requires distinguishing between agentic workflow (inventory events → suggestions → human approval → action) and ordinary automation (direct API execution → immediate action).

### Options

- Single execution path with conditional human approval
- Separate agentic and direct execution paths
- Workflow engine managing handoffs
- Flag-driven behavior differentiation

### Decision

Separate execution paths - event-triggered suggestions go through agentic loop with human approval; API calls execute directly within request context.

### Tradeoffs

__Pro:__ Clear distinction of intent, appropriate SLAs for each path, controlled human oversight\
__Con:__ Potential code duplication, more paths to maintain, inconsistent user experience

## 8. Low-stock vs Demand-spike Handling

### Context

Low inventory and high demand represent significantly different merchandising scenarios requiring distinct analytical approaches and recommendation strategies.

### Options

- Single generic anomaly detection with generic responses
- Dedicated handling paths with distinct prompts/processing
- Same algorithm with contextual parameters
- Machine learning classification of scenarios

### Decision

Dedicated handling with distinct trigger reasons passed to advisors, enabling tailored prompting and business logic.

### Tradeoffs

__Pro:__ Context-appropriate recommendations, better AI performance with focused prompts, explicit handling of different business cases\
__Con:__ More code paths, increased complexity, harder to maintain consistency

## 9. Trigger Handler/Idempotency Design

### Context

Multiple inventory events may trigger repeated suggestion creation attempts. System must prevent duplicate pending suggestions while ensuring valid new ones are processed.

### Options

- Allow all suggestions, filter duplicates in UI
- Check existence before creation with database constraints
- Deduplication cache with TTL
- Composite key uniqueness in database

### Decision

Database-level uniqueness constraint on (product_id, trigger_reason, suggestion_type, status=PENDING) preventing duplicate pending suggestions.

### Tradeoffs

__Pro:__ Reliable deduplication, consistent data model, minimal application complexity\
__Con:__ Database-centric solution, harder to customize policies later, blocking by constraint

## 10. Accept-side Effects

### Context

Accepting pricing or reorder suggestions must atomically update source product records with appropriate transaction boundaries and error handling.

### Options

- Direct in-place updates within acceptance transaction
- Staged updates with validation steps
- Event sourcing for all changes
- Outbox pattern for distributed effects

### Decision

Direct atomic updates within same transaction as suggestion status change, with rollback on validation failures.

### Tradeoffs

__Pro:__ Strong consistency, simplicity, immediate effect visibility\
__Con:__ Larger transaction boundary, potential contention, harder to undo business effects
