# dashboard

## ADDED Requirements

### Requirement: Dashboard displays project progress
The system SHALL show an overview of all visible projects with progress indicators.

#### Scenario: View project progress
- **WHEN** user opens dashboard
- **THEN** system displays project cards with completion percentage

#### Scenario: Empty project list
- **WHEN** user has no assigned projects
- **THEN** system displays empty state with guidance

### Requirement: Dashboard displays weekly work hours
The system SHALL show logged hours for the current week with comparison to capacity.

#### Scenario: View weekly hours summary
- **WHEN** user views dashboard
- **THEN** system displays hours logged vs. capacity for current week

#### Scenario: View overtime indicator
- **WHEN** user logs hours exceeding capacity
- **THEN** system shows overtime warning indicator

### Requirement: Dashboard displays pending tasks
The system SHALL show tasks assigned to the user that are not yet completed.

#### Scenario: View pending tasks
- **WHEN** user opens dashboard
- **THEN** system displays list of incomplete assigned tasks

#### Scenario: View overdue tasks
- **WHEN** user has tasks past due date
- **THEN** system highlights overdue tasks with red indicator

### Requirement: Dashboard displays recent activity
The system SHALL show recent project activities relevant to the user.

#### Scenario: View recent updates
- **WHEN** user views activity feed
- **THEN** system displays recent task updates, comments, and status changes

### Requirement: Role-specific dashboard views
The system SHALL display different information based on user role.

#### Scenario: Super Admin dashboard
- **WHEN** Super Admin opens dashboard
- **THEN** system displays system-wide metrics and all projects

#### Scenario: Department Admin dashboard
- **WHEN** Department Admin opens dashboard
- **THEN** system displays department-specific projects and metrics

#### Scenario: Project Admin dashboard
- **WHEN** Project Admin opens dashboard
- **THEN** system displays managed projects and team performance

#### Scenario: Member dashboard
- **WHEN** Member opens dashboard
- **THEN** system displays assigned tasks and personal timesheet summary
