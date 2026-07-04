package org.thundernetwork.permissioner.replacements

/**
 * Enum that specifies where to apply a replacement in permission strings.
 * Used in Replace interface to determine which permission string(s) to modify.
 */
enum class ReplacementLocation {
    /** Apply replacement only to the required permission string */
    REQUIRED,
    /** Apply replacement only to the user's permission string */
    GRANTED,
    /** Apply replacement to both permission strings */
    REQUIRED_AND_GRANTED
}