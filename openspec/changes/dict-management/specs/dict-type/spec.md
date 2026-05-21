## ADDED Requirements

### Requirement: Admin can view all dict types
The system SHALL allow administrators to view a list of all dictionary types.

#### Scenario: View all dict types
- **WHEN** admin accesses the dict type list endpoint
- **THEN** system returns all dictionary types with id, code, name, description

### Requirement: Admin can create dict type
The system SHALL allow administrators to create a new dictionary type.

#### Scenario: Successful creation
- **WHEN** admin submits valid dict type data (code, name, description)
- **THEN** system creates the dict type and returns success

#### Scenario: Duplicate code
- **WHEN** admin submits a dict type with an existing code
- **THEN** system returns error "Dictionary type code already exists"

#### Scenario: Missing required fields
- **WHEN** admin submits dict type with missing code or name
- **THEN** system returns validation error

### Requirement: Admin can update dict type
The system SHALL allow administrators to update an existing dictionary type.

#### Scenario: Successful update
- **WHEN** admin submits updated dict type data
- **THEN** system updates the dict type and returns success

#### Scenario: Update non-existent type
- **WHEN** admin updates a dict type that does not exist
- **THEN** system returns error "Dictionary type not found"

### Requirement: Admin can delete dict type
The system SHALL allow administrators to delete a dictionary type.

#### Scenario: Successful deletion
- **WHEN** admin deletes a dict type with no associated items
- **THEN** system deletes the dict type and returns success

#### Scenario: Delete type with associated items
- **WHEN** admin deletes a dict type that has associated dict items
- **THEN** system returns error "Cannot delete type with associated items"
