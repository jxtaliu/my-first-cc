package com.sme.pm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to require a specific project role for accessing an endpoint.
 * The user must have the specified role in the project to access the resource.
 *
 * Roles map to project_role.role field:
 * - PROJECT_OWNER: Product Owner - full access to project management
 * - PROJECT_MANAGER: Scrum Master - manages sprint and team
 * - DEVELOPER: Team Member - works on tasks
 * - DEV_LEAD: Technical lead - manages developers
 * - GUEST: Read-only access
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireProjectRole {
    /**
     * Required roles (any of these roles is sufficient)
     */
    String[] value() default {};

    /**
     * Operation being performed (for logging/auditing)
     */
    String operation() default "";

    /**
     * Allow if user is a project member (any role)
     */
    boolean memberOnly() default false;
}
