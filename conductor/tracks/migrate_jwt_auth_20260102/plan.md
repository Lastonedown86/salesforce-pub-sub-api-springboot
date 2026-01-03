# Plan: Migrate Authentication to JWT Bearer Flow

## Phase 1: Setup and Security [checkpoint: 87d7b8d]
- [x] Task: Configure environment variables/properties for JWT credentials (Client ID, Username, Login URL). be1d4d9
- [x] Task: Securely store the private key file and configure its path in the application. cd5f49d
- [x] Task: Add necessary dependencies for JWT signing (e.g., JJWT or similar) if not already present. 397eb59

## Phase 2: Implementation [checkpoint: d7f23ab]
- [x] Task: Create a JWT generator utility to sign requests with the private key. 2303869
- [x] Task: Implement the token exchange service to request an access token from Salesforce using the signed JWT. 41c1e32
- [x] Task: Update `SalesforceSessionTokenService` to use the new JWT flow instead of previous methods. 478fa8e

## Phase 3: Verification
- [x] Task: Write unit tests for JWT generation and token exchange logic. 68ee651
- [x] Task: Perform an end-to-end test to verify gRPC connection stability with the new JWT-based tokens. 696769c
- [~] Task: Conductor - User Manual Verification 'Verification' (Protocol in workflow.md)
