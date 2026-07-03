package org.thundernetwork.permissioner

/**
 * Defines a replacement rule for permission strings.
 * Useful for handling dynamic or parameterized permissions.
 *
 * @example
 * // Replace {userId} with '123' in both permission strings
 * { key: '{userId}', value: '123', where: WhereToReplace.Both }
 */
interface Replacement {
    /** The string pattern to search for in permission strings */
    val key: Regex
    /** The string to replace the key with */
    val replacement: String
    /**
     * Specifies which permission string(s) to apply the replacement to.
     */
    val replacementLocation: ReplacementLocation
}