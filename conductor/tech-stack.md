# Technology Stack

## Core Technologies
*   **Java 21:** The primary programming language, leveraging modern features and long-term support.
*   **Spring Boot 3.4.5:** The application framework providing dependency injection, configuration management, and production-ready features.
*   **Maven:** Dependency management and build automation.

## Communication & Data
*   **gRPC:** Used for high-performance communication with the Salesforce Pub/Sub API.
*   **Protocol Buffers (Protobuf):** Data serialization format for gRPC.
*   **Apache Avro:** Used for serializing and deserializing Salesforce event payloads within the Pub/Sub API.

## Integration & Security
*   **Salesforce Pub/Sub API:** The target integration point for real-time eventing.
*   **JWT Bearer Token Flow:** Secure, server-to-server authentication for Salesforce API access.
*   **Lombok:** Library to reduce boilerplate code (getters, setters, builders).
*   **Jackson:** JSON processing and data binding for any RESTful auxiliary calls.

## Infrastructure
*   **Standard JVM Deployment:** Suitable for cloud environments (AWS, Azure, GCP) or on-premises servers.
