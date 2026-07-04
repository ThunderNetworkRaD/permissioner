package org.thundernetwork.permissioner

/**
 * Represents a list of permission strings.
 * Each string is a dot-separated permission path (e.g., ["user.read", "admin.write"]).
 */
typealias PermissionList = MutableList<Permission>