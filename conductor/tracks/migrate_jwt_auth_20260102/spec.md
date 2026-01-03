# Specification: Migrate Authentication to JWT Bearer Flow

## Goal
Replace the current authentication mechanism with the Salesforce OAuth 2.0 JWT Bearer Token flow. This provides a more secure, non-interactive, server-to-server authentication method suitable for automated integrations.

## Requirements
- Implement JWT generation using a private key (.jks or .pem).
- Exchange the JWT for an access token via the Salesforce OAuth token endpoint.
- Integrate the new token retrieval process into the existing `SalesforceSessionTokenService`.
- Ensure secure handling of the private key and other sensitive credentials (Client ID, Username, Login URL).
- Maintain compatibility with the existing gRPC interceptors and REST clients.

## Success Criteria
- The application can successfully authenticate with Salesforce using only the private key, client ID, and username.
- The Pub/Sub API connection remains stable using the token obtained via the JWT flow.
- No hardcoded secrets are present in the codebase.
