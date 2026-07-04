package org.thundernetwork.permissioner

import org.thundernetwork.permissioner.options.EvaluateOptions

/**
 * Recursively evaluates a permission [Calculation] against a list of granted permissions.
 * Supports logical composition of permission checks via [And], [Or], and [Not], with
 * [PermissionCheck] as the base case, resolved via [checkList].
 *
 * Evaluation rules:
 * - [And]: true only if every sub-calculation evaluates to true.
 * - [Or]: true if at least one sub-calculation evaluates to true.
 * - [Not]: inverts the result of the contained calculation.
 * - [PermissionCheck]: delegates to [checkList] with the given [options].
 *
 * @param permissions The list of granted permissions to evaluate against
 * @param calculation The permission calculation to evaluate (and/or/not/direct check)
 * @param options Optional configuration passed down to [checkList] for the base case
 * @returns a Boolean, true if the calculation is satisfied by the given permissions, false otherwise
 *
 * @example
 * Direct check
 * evaluate(permissions, PermissionCheck(listOf("user.read"))) // true if permissions grant user.read
 *
 * AND
 * evaluate(permissions, And(listOf(PermissionCheck(listOf("user.read")), PermissionCheck(listOf("user.write")))))
 *
 * OR
 * evaluate(permissions, Or(listOf(PermissionCheck(listOf("user.read")), PermissionCheck(listOf("admin.*")))))
 *
 * NOT
 * evaluate(permissions, Not(PermissionCheck(listOf("user.delete"))))
 */
fun evaluate(permissions: PermissionList, calculation: Calculation, options: EvaluateOptions? = null): Boolean {
    return when (calculation) {
        is And -> calculation.and.all { evaluate(permissions, it) }
        is Or -> calculation.or.any { evaluate(permissions, it) }
        is Not -> !evaluate(permissions, calculation.not)
        is PermissionCheck -> checkList(permissions, calculation.permissions, options)
    }
}