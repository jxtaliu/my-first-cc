# time-tracking

## ADDED Requirements

### Requirement: User can log work hours
The system SHALL allow users to log hours worked on specific tasks/projects with date and description.

#### Scenario: Log hours for task
- **WHEN** user submits timesheet entry with task, date, and hours
- **THEN** system creates entry and returns success

#### Scenario: Log hours without task
- **WHEN** user submits timesheet entry without task reference
- **THEN** system creates entry with project-level hours

#### Scenario: Edit existing timesheet
- **WHEN** user edits their own timesheet entry
- **THEN** system updates entry and returns success

### Requirement: User can view weekly timesheet
The system SHALL display a weekly view of logged hours grouped by project and task.

#### Scenario: View weekly summary
- **WHEN** user requests weekly timesheet view
- **THEN** system returns entries grouped by day with totals

#### Scenario: View week with no entries
- **WHEN** user requests week with no logged hours
- **THEN** system returns empty week view with zero totals

### Requirement: User can view monthly timesheet
The system SHALL display a monthly view of logged hours with project breakdown.

#### Scenario: View monthly summary
- **WHEN** user requests monthly timesheet view
- **THEN** system returns entries grouped by week/day with project totals

#### Scenario: Monthly hours by project
- **WHEN** user requests monthly project breakdown
- **THEN** system returns hours aggregated by project

### Requirement: Manager can view team timesheets
The system SHALL allow managers to view timesheets of users they supervise.

#### Scenario: Department Admin views department timesheet
- **WHEN** Department Admin requests team weekly view
- **THEN** system returns all department members' entries

#### Scenario: Project Admin views project timesheet
- **WHEN** Project Admin requests project timesheet
- **THEN** system returns all project members' entries

### Requirement: Timesheet reports
The system SHALL generate work hour reports with configurable date ranges and filters.

#### Scenario: Generate workload report
- **WHEN** authorized user requests workload report
- **THEN** system returns hours per user with date range filter

#### Scenario: Generate project cost report
- **WHEN** authorized user requests project cost report
- **THEN** system returns total hours and cost based on hourly rates
