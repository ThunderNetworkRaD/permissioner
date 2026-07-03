package org.thundernetwork.permissioner

/**
 * Fill the PermissionList with empty strings until it reaches the specified length.
 * If is already longer, it is returned unchanged.
 *
 * @param length - The list desired length
 * @return nothing as this function edit the original PermissionList
 */
fun PermissionList.fill(length: Int) {
    while (this.size < length) add("")
}