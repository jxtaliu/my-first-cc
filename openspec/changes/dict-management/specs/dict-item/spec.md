## ADDED Requirements

### Requirement: Admin can view all dict items
The system SHALL allow administrators to view all dictionary items, optionally filtered by type.

#### Scenario: View all dict items
- **WHEN** admin accesses the dict items endpoint without type filter
- **THEN** system returns all dictionary items

#### Scenario: View items by type
- **WHEN** admin accesses the dict items endpoint with typeId filter
- **THEN** system returns only items belonging to that type

### Requirement: Admin can create dict item
The system SHALL allow administrators to create a new dictionary item.

#### Scenario: Successful creation
- **WHEN** admin submits valid dict item data (dictTypeId, code, name, nameEn, nameZh, sortOrder)
- **THEN** system creates the dict item and refreshes cache

#### Scenario: Duplicate code within type
- **WHEN** admin submits a dict item with an existing code under the same type
- **THEN** system returns error "Dictionary item code already exists under this type"

#### Scenario: Missing required fields
- **WHEN** admin submits dict item with missing required fields
- **THEN** system returns validation error

### Requirement: Admin can update dict item
The system SHALL allow administrators to update an existing dictionary item.

#### Scenario: Successful update
- **WHEN** admin submits updated dict item data
- **THEN** system updates the dict item, refreshes cache, and returns success

#### Scenario: Update non-existent item
- **WHEN** admin updates a dict item that does not exist
- **THEN** system returns error "Dictionary item not found"

### Requirement: Admin can delete dict item
The system SHALL allow administrators to delete a dictionary item.

#### Scenario: Successful deletion
- **WHEN** admin deletes a dict item
- **THEN** system deletes the dict item, refreshes cache, and returns success

#### Scenario: Delete non-existent item
- **WHEN** admin deletes a dict item that does not exist
- **THEN** system returns error "Dictionary item not found"
