package org.thundernetwork.permissioner

import org.thundernetwork.permissioner.options.CheckSingleOptions
import org.thundernetwork.permissioner.replacements.ReplacementLocation
import kotlin.math.max

/**
 * Checks if a single permission matches a required permission.
 * Supports wildcard (*) and hierarchical permissions (e.g., 'user.read' matches 'user.read.write').
 *
 * Permission comparison table:
 *
 * With 2 values, `x` and `y`, the empty string, and `*`
 *
 * | Permission | Required  | Result   | Description                                                  |
 * | ---------- | --------- | -------- | ------------------------------------------------------------ |
 * | `*`        | `*`       | `TRUE`   | Two equal permissions (e.g. `a.* & a.*`)                     |
 * | `x`        | `x`       | `TRUE`   | Two equal permissions (e.g. `a.b & a.b`)                     |
 * | `x`        | `y`       | `FALSE`  | Two different permissions (e.g. `a.b & a.c`)                 |
 * |            | `x`       | `TRUE`   | The empty string represents all permissions (e.g. `a & a.b`) |
 * | `x`        |           | `FALSE`  | The empty string represents all permissions (e.g. `a.b & a`) |
 * |            | `*`       | `TRUE`   | The empty string includes * (e.g. `a & a.*`)                |
 * | `*`        |           | `FALSE`  | The empty string includes * (e.g. `a.* & a`)                |
 * | `*`        | `x`       | `TRUE`   | * includes all (e.g. `a.* & a.b`)                           |
 * | `x`        | `*`       | `FALSE`  | * includes all (e.g. `a.b & a.*`)                           |
 * |            |           | `TRUE`   | Not recommended to use empty strings in both permissions.    |
 * | `not`      | `x`       | `ERROR`  | Do not use "not" as permission.                              |
 * | `x`        | `not`     | `FALSE`  | Always false.                                                |
 * @param permission The permission to check (e.g., 'user.read' or '*')
 * @param requiredPermission The required permission to check against (e.g., 'user.read')
 * @param options Optional configuration for the permission check
 * @returns a Boolean, True if the permission is granted, false otherwise
 *
 * @example
 * Basic usage
 * checkSingle('user.read', 'user.read'); // true
 *
 * Hierarchy check
 * checkSingle('user', 'user.read'); // true
 *
 * Wildcard check
 * checkSingle('user.*', 'user.read'); // true
 *
 * With replacements
 * checkSingle('user.{userId}', 'user.123', {
 *   replaces: [{ key: '{userId}', value: '123', where: WhereToReplace.Both }]
 * }); // true
 */
fun checkSingle(permission: Permission, requiredPermission: Permission, options: CheckSingleOptions? = null): Boolean {
    val replacements = options?.replacements
    replacements?.forEach { replacement ->
        when (replacement.replacementLocation) {
            ReplacementLocation.REQUIRED_AND_GRANTED -> {
                permission.replace(replacement.key, replacement.replacement)
                requiredPermission.replace(replacement.key, replacement.replacement)
            }

            ReplacementLocation.REQUIRED -> requiredPermission.replace(replacement.key, replacement.replacement)
            ReplacementLocation.GRANTED -> permission.replace(replacement.key, replacement.replacement)
        }
    }

    val actual = permission.split('.') as PermissionList
    val required = requiredPermission.split('.') as PermissionList

    val max = max(actual.size, required.size)
    actual.fill(max)
    required.fill(max)

    for (i in 0 until max) {
        val nowActual = actual[i]
        val nowRequired = required[i]

        return if (nowRequired == "not" && i == 0) throw IllegalStateException("Required permission cannot start with not.")
        else if (nowActual == nowRequired) continue
        else if (nowActual == "not" && i == 0) false
        else if (nowActual == "" || nowActual == "*") true
        else false
    }

    return true
}