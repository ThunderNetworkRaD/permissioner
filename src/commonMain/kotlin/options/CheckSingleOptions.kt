package org.thundernetwork.permissioner.options

import org.thundernetwork.permissioner.replacements.Replacement
import kotlin.js.JsExport

/**
 * Options for customizing the behavior of checkSingle function.
 */
@JsExport
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