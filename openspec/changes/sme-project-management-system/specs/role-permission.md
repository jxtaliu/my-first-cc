# role-permission

## ADDED Requirements

### Requirement: System supports four predefined roles
The system SHALL support four role types: Super Admin, Department Admin, Project Admin, and Member.

#### Scenario: Super Admin capabilities
- **WHEN** Super Admin performs any operation
- **THEN** system allows all operations across all modules

#### Scenario: Department Admin capabilities
- **WHEN** Department Admin accesses data outside their department
- **THEN** system returns 403 Forbidden

#### Scenario: Project Admin capabilities
- **WHEN** Project Admin accesses project they manage
- **THEN** system allows full project management

#### Scenario: Member capabilities
- **WHEN** Member accesses assigned project tasks
- **THEN** system allows task-level operations only

### Requirement: Users can have multiple roles
The system SHALL allow a single user to have multiple roles simultaneously.

#### Scenario: User with dual roles
- **WHEN** user has both Department Admin and Project Admin roles
- **THEN** system grants combined permissions from both roles

### Requirement: Super Admin can create custom roles
The system SHALL allow Super Admin to create custom roles with specific permission sets.

#### Scenario: Create custom role
- **WHEN** Super Admin creates role with selected permissions
- **THEN** system creates role and returns role ID

#### Scenario: Assign permissions to custom role
- **WHEN** Super Admin assigns permissions to custom role
- **THEN** system persists permission assignments

### Requirement: User roles can be assigned and revoked
The system SHALL allow authorized users to assign/revoke roles to/from users.

#### Scenario: Assign role to user
- **WHEN** authorized admin assigns role to user
- **THEN** system updates user roles and returns success

#### Scenario: Revoke role from user
- **WHEN** authorized admin revokes role from user
- **THEN** system removes role from user and returns success

#### Scenario: Prevent removing last Super Admin
- **WHEN** attempting to revoke the last Super Admin role
- **THEN** system returns error "Cannot remove last Super Admin"
