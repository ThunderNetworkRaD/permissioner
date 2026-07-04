package org.thundernetwork.permissioner

import kotlin.js.JsExport

/**
 * Represents a permission calculation that can be:
 * - An AND operation
 * - An OR operation
 * - A NOT operation
 * - A direct permission check (list of permission strings)
 */
@JsExport
sealed interface Calculation

/**
 * Represents a logical AND operation between multiple permission calculations.
 * All calculations in the list must evaluate to true for the AND to be true.
 */
@JsExport
data class And(val and: List<Calculation>) : Calculation

/**
 * Represents a logical OR operation between multiple permission calculations.
 * At least one calculation in the list must evaluate to true for the OR to be true.
 */
@JsExport
data class Or(val or: List<Calculation>) : Calculation

/**
 * Represents a logical NOT operation on a permission calculation.
 * Inverts the result of the contained calculation.
 */
@JsExport
data class Not(val not: Calculation) : Calculation

/**
 * Represents a direct permission check: a list of permission strings that must
 * satisfy the required permissions via [checkList].
 * Each string is a dot-separated permission path (e.g., ["user.read", "admin.write"]).
 */
@JsExport
data class PermissionCheck(val permissions: PermissionList) : Calculation