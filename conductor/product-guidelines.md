# Product Guidelines

## 1. Documentation & Communication Style

*   **Tone:** Technical, Precise, and Professional.
    *   *Rationale:* As a developer-focused tool, accuracy and clarity are paramount. The documentation should respect the user's expertise while providing comprehensive details on configuration and architecture.
*   **Format:**
    *   **Code-First:** Prioritize code snippets and configuration examples over long prose.
    *   **Standardized:** Use JavaDoc for all public methods and classes. Maintain a consistent structure in README files (Overview, Prerequisites, Setup, Usage, troubleshooting).
*   **Terminology:** Use standard Salesforce and Spring Boot terminology (e.g., "Org," "Platform Event," "Bean," "Dependency Injection") to minimize cognitive load.

## 2. Code Quality & Architecture

*   **Principles:**
    *   **Clean Code:** Adhere to standard Java naming conventions (CamelCase, PascalCase). Keep methods focused and small.
    *   **Modularity:** Use Spring's dependency injection to ensure loosely coupled components. Separate concerns (e.g., Auth vs. Event Processing).
    *   **Robustness:** Defensive programming is key. Explicitly handle exceptions, especially for network-bound operations (gRPC calls) and authentication failures.
*   **Testing:**
    *   Unit tests should cover core logic (parsing, authentication flows).
    *   Integration tests should mock external Salesforce endpoints to ensure system stability without requiring a live connection for every build.

## 3. Configuration & Security

*   **Security First:** Never hardcode credentials. Use environment variables or encrypted configuration properties for sensitive data (Connected App secrets, Private Keys).
*   **Configurability:** Expose key parameters (Pub/Sub endpoint, topic names, retry counts) via `application.properties` or `application.yaml` to allow deployment in different environments without code changes.

## 4. Developer Experience (DX)

*   **Quick Start:** The project should be runnable with minimal setup. Provide a `docker-compose` or clear local setup guide.
*   **Logging:** distinct and structured logging (e.g., SLF4J) to aid in debugging distributed systems issues.
