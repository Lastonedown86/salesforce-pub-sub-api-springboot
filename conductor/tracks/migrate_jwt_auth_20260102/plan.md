# Plan: Migrate Authentication to JWT Bearer Flow

## Phase 1: Setup and Security
- [ ] Task: Configure environment variables/properties for JWT credentials (Client ID, Username, Login URL).
- [ ] Task: Securely store the private key file and configure its path in the application.
- [ ] Task: Add necessary dependencies for JWT signing (e.g., JJWT or similar) if not already present.

## Phase 2: Implementation
- [ ] Task: Create a JWT generator utility to sign requests with the private key.
- [ ] Task: Implement the token exchange service to request an access token from Salesforce using the signed JWT.
- [ ] Task: Update `SalesforceSessionTokenService` to use the new JWT flow instead of previous methods.

## Phase 3: Verification
- [ ] Task: Write unit tests for JWT generation and token exchange logic.
- [ ] Task: Perform an end-to-end test to verify gRPC connection stability with the new JWT-based tokens.
- [ ] Task: Conductor - User Manual Verification 'Verification' (Protocol in workflow.md)
