package org.thundernetwork.permissioner

import org.thundernetwork.permissioner.options.CheckListOptions

/**
 * Checks if a list of granted permissions satisfies all required permissions.
 * Supports wildcard (*) and hierarchical permissions via [checkSingle], as well as
 * explicit negative permissions (e.g. 'not.user.delete') that can revoke an otherwise
 * matching positive permission.
 *
 * Resolution logic per required permission:
 * - Permissions starting with "not" are treated as negative permissions and are never
 *   used directly as a positive match; the "not" prefix is stripped and the remainder
 *   is used to check if it overrides a positive match.
 * - For each required permission, the granted (positive) permissions are checked in order.
 *   The first positive permission that matches AND is not overridden by any negative
 *   permission grants access for that required permission. If a match is overridden,
 *   the search continues with the remaining positive permissions.
 * - If no positive, non-overridden match is found for a required permission, the whole
 *   check fails.
 *
 * Result table (high level; see [checkSingle] for the underlying single-permission rules):
 *
 * | requiredPermissions   | permissions         | Result  | Description                                                |
 * | --------------------- | ------------------- | ------- | ---------------------------------------------------------- |
 * | empty                 | any                 | `TRUE`  | No permissions are required.                               |
 * | any (non-empty)       | empty               | `FALSE` | No permissions granted, but some are required.             |
 * | `a.b`                 | `a.b`               | `TRUE`  | Direct match.                                              |
 * | `a.b`                 | `a.*`               | `TRUE`  | Wildcard match.                                            |
 * | `a.b`                 | `a.b, not.a.b`      | `FALSE` | Negative permission overrides the matching positive one.   |
 * | `a.b`                 | `a.*, not.a.b`      | `FALSE` | Negative overrides even a broader positive match.          |
 * | `a.b`                 | `a.*, a.b, not.a.*` | `TRUE`  | `a.*` is overridden, but `a.b` is not, so it still matches.|
 * | `a.b, a.c`            | `a.b`               | `FALSE` | Not all required permissions are satisfied.                |
 *
 * @param permissions The list of granted permissions (e.g. ['user.read', 'not.user.delete'])
 * @param requiredPermissions The list of required permissions to check against (e.g. ['user.read'])
 * @returns a Boolean, true if every required permission is granted (and not overridden by a negative permission), false otherwise
 *
 * @example
 * Basic usage
 * checkList(['user.read'], ['user.read']); // true
 *
 * Hierarchy / wildcard
 * checkList(['user.*'], ['user.read', 'user.write']); // true
 *
 * Negative permission overrides a match
 * checkList(['user.*', 'not.user.delete'], ['user.delete']); // false
 *
 * Negative permission does not affect unrelated matches
 * checkList(['user.*', 'not.user.delete'], ['user.read']); // true
 */
fun checkList(permissions: PermissionList, requiredPermissions: PermissionList, options: CheckListOptions? = null): Boolean {
    if (requiredPermissions.isEmpty()) return true
    else if (permissions.isEmpty()) return false

    val negativePermissions = permissions
        .filter { it.split('.')[0] == "not" }
        .map { it
            .split('.')
            .drop(1)
            .joinToString (".")
        }
    val positivePermissions = permissions
        .filter { it.split('.')[0] != "not" }

    for (requiredPermission in requiredPermissions) {
        var hasPermission = false;
        for (permission in positivePermissions) {
            if (checkSingle(permission, requiredPermission, options)) {
                var tempPermission = true
                if (!negativePermissions.isEmpty()) {
                    for (negativePermission in negativePermissions) {
                        if (checkSingle(negativePermission, requiredPermission, options)) {
                            tempPermission = false
                            break
                        }
                    }
                }
                if (tempPermission) {
                    hasPermission = true
                    break
                }
            }
        }
        if (!hasPermission) return false
    }
    return true
}