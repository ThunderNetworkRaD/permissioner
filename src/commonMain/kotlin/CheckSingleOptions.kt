package org.thundernetwork.permissioner

/**
 * Options for customizing the behavior of checkSingle function.
 */
interface CheckSingleOptions {
    /**
     * Array of replacement rules to apply to permission strings before comparison.
     * Useful for handling dynamic values in permissions like user IDs or resource IDs.
     *
     * @example
     * // Replace {userId} with '123' in permission strings
     * { replaces: [{ key: '{userId}', value: '123', where: WhereToReplace.Both }] }
     */
    val replacements: List<Replacement>?
}