# user-auth

## ADDED Requirements

### Requirement: User can register a local account
The system SHALL allow users to register a local account with username, password, and email.

#### Scenario: Successful registration
- **WHEN** user submits valid registration form (username, email, password)
- **THEN** system creates account and returns success message

#### Scenario: Registration with duplicate username
- **WHEN** user submits registration with existing username
- **THEN** system returns error "Username already exists"

#### Scenario: Registration with invalid email
- **WHEN** user submits registration with malformed email
- **THEN** system returns error "Invalid email format"

### Requirement: User can login
The system SHALL allow users to login with username and password, returning a JWT token.

#### Scenario: Successful login
- **WHEN** user submits valid credentials
- **THEN** system returns JWT token and user info

#### Scenario: Login with wrong password
- **WHEN** user submits valid username but wrong password
- **THEN** system returns error "Invalid credentials"

#### Scenario: Login with non-existent user
- **WHEN** user submits non-existent username
- **THEN** system returns error "Invalid credentials"

### Requirement: User can logout
The system SHALL allow authenticated users to logout, invalidating their session.

#### Scenario: Successful logout
- **WHEN** authenticated user calls logout endpoint
- **THEN** system invalidates token and returns success

### Requirement: Authenticated requests are validated
The system SHALL validate JWT token on protected endpoints and reject invalid/expired tokens.

#### Scenario: Request with valid token
- **WHEN** user sends request with valid Authorization header
- **THEN** system processes request and returns data

#### Scenario: Request with expired token
- **WHEN** user sends request with expired JWT token
- **THEN** system returns 401 Unauthorized

#### Scenario: Request without token
- **WHEN** user sends request without Authorization header
- **THEN** system returns 401 Unauthorized
