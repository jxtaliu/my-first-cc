# project-management

## ADDED Requirements

### Requirement: Project CRUD operations
The system SHALL allow users with appropriate permissions to create, read, update, and delete projects.

#### Scenario: Create project
- **WHEN** Project Admin or Super Admin creates a new project
- **THEN** system creates project and returns project details

#### Scenario: List projects
- **WHEN** user requests project list
- **THEN** system returns projects visible to user's role

#### Scenario: Update project
- **WHEN** authorized user updates project details
- **THEN** system persists changes and returns updated project

#### Scenario: Delete project
- **WHEN** Super Admin deletes a project
- **THEN** system soft-deletes project and returns success

### Requirement: Sprint management with multiple modes
The system SHALL support three Sprint modes: Fixed Sprint, Agile Iteration, and Kanban.

#### Scenario: Create Fixed Sprint
- **WHEN** user creates Sprint with fixed duration (1-4 weeks)
- **THEN** system creates Sprint with defined start/end dates

#### Scenario: Create Agile Iteration
- **WHEN** user creates Sprint with variable length
- **THEN** system creates Sprint without enforcing duration constraints

#### Scenario: Switch to Kanban mode
- **WHEN** user switches project to Kanban mode
- **THEN** system enables continuous flow without fixed Sprints

### Requirement: Flexible task hierarchy with max 4 levels
The system SHALL support configurable task hierarchy with a maximum of 4 levels.

#### Scenario: Create custom task levels
- **WHEN** admin configures task levels (e.g., Epic → Feature → Story → Task)
- **THEN** system stores hierarchy configuration with max 4 levels

#### Scenario: Create task in hierarchy
- **WHEN** user creates task with parent reference
- **THEN** system links task to parent and displays in hierarchy view

#### Scenario: Default hierarchy
- **WHEN** team uses default four-level hierarchy
- **THEN** system supports Epic → Feature → Story → Sub-task

#### Scenario: Exceed maximum depth
- **WHEN** user attempts to create task at depth 5
- **THEN** system returns error "Maximum task hierarchy depth is 4"

### Requirement: Task assignment and status tracking
The system SHALL allow tasks to be assigned to team members and track status progression.

#### Scenario: Assign task
- **WHEN** Project Admin assigns task to team member
- **THEN** system updates task assignee and sends notification

#### Scenario: Update task status
- **WHEN** user updates task status (e.g., To Do → In Progress → Done)
- **THEN** system records status change with timestamp

### Requirement: Project member management
The system SHALL allow Project Admin to add/remove project members.

#### Scenario: Add project member
- **WHEN** Project Admin adds user to project
- **THEN** system creates membership and returns success

#### Scenario: Remove project member
- **WHEN** Project Admin removes user from project
- **THEN** system removes membership and returns success

### Requirement: Project archive
The system SHALL allow archiving completed or inactive projects.

#### Scenario: Archive project
- **WHEN** Super Admin or Project Admin archives a project
- **THEN** system marks project as archived and hides from active list

#### Scenario: View archived projects
- **WHEN** authorized user requests archived projects
- **THEN** system returns list of archived projects

#### Scenario: Restore archived project
- **WHEN** Super Admin restores archived project
- **THEN** system marks project as active and returns to active list
