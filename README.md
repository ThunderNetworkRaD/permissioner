# Permissioner

A lightweight, hierarchical permission-checking library for Kotlin. Originally written in
TypeScript, `permissioner` lets you define fine-grained, dot-separated permissions
(`user.read`, `user.*`, `not.user.delete`, ...) and evaluate them against arbitrarily complex
boolean expressions (`AND` / `OR` / `NOT`).

> **Status:** Kotlin port complete. A Kotlin/JS build for npm distribution is planned — see
> [Roadmap](#roadmap).

---

## Table of contents

- [Features](#features)
- [Installation](#installation)
- [Core concepts](#core-concepts)
    - [Permission syntax](#permission-syntax)
    - [Wildcards and hierarchy](#wildcards-and-hierarchy)
    - [Negative permissions](#negative-permissions)
- [Usage](#usage)
    - [Checking a single permission](#checking-a-single-permission)
    - [Checking a list of permissions](#checking-a-list-of-permissions)
    - [Composing checks with AND / OR / NOT](#composing-checks-with-and--or--not)
    - [Replacements](#replacements)
- [API reference](#api-reference)
- [Comparison tables](#comparison-tables)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Features

- **Hierarchical permissions** — `user` implicitly grants `user.read`, `user.write`, etc.
- **Wildcards** — `user.*` grants any permission under `user`.
- **Negative permissions** — `not.user.delete` explicitly revokes a permission that would
  otherwise be granted by a broader positive permission.
- **Boolean composition** — combine permission checks with `$and`, `$or`, and `$not` to
  express arbitrarily complex authorization rules.
- **Placeholder replacement** — substitute dynamic values (e.g. a user ID) into permission
  strings before evaluation.
- **Zero runtime dependencies.**

---

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("org.thundernetwork:permissioner:1.0.0")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'org.thundernetwork:permissioner:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>org.thundernetwork</groupId>
    <artifactId>permissioner</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## Core concepts

### Permission syntax

A permission is a **dot-separated string**, read left to right from most general to most
specific:

```
user.read
user.profile.edit
admin.settings.billing.view
```

### Wildcards and hierarchy

| Granted permission | Matches required permission | Why |
|---|---|---|
| `user.read` | `user.read` | Exact match |
| `user` | `user.read` | An empty trailing segment acts as "all children" |
| `user.*` | `user.read` | Explicit wildcard segment |
| `user.*` | `user.read.detail` | Wildcard matches any depth below it |
| `user.read` | `user` | **No match** — a specific permission does not grant a broader one |
| `user.read` | `user.write` | **No match** — different leaf |

In short: **broader or wildcard permissions grant narrower ones, never the other way around.**

### Negative permissions

Prefixing a permission with `not.` explicitly revokes it, even if another granted permission
would otherwise satisfy the requirement:

```
permissions = ["user.*", "not.user.delete"]
required    = "user.delete"
→ false  // user.* would normally grant this, but not.user.delete revokes it
```

If a more specific, non-revoked permission also grants the requirement, it still succeeds:

```
permissions = ["user.*", "user.delete", "not.user.*"]
required    = "user.delete"
→ true  // user.* is revoked, but the explicit user.delete permission is not
```

A required permission can never start with `not` — attempting this throws
`IllegalStateException`.

---

## Usage

### Checking a single permission

```kotlin
import org.thundernetwork.permissioner.checkSingle

checkSingle("user.read", "user.read")   // true
checkSingle("user", "user.read")        // true  (hierarchy)
checkSingle("user.*", "user.read")      // true  (wildcard)
checkSingle("user.read", "user.write")  // false
```

### Checking a list of permissions

Use `checkList` when a user holds multiple permissions and you need to satisfy one or more
required permissions, taking negative permissions into account:

```kotlin
import org.thundernetwork.permissioner.checkList

val granted = listOf("user.*", "not.user.delete")
val required = listOf("user.read", "user.write")

checkList(granted, required) // true

checkList(granted, listOf("user.delete")) // false, revoked by not.user.delete
```

### Composing checks with AND / OR / NOT

For more complex authorization logic, build a `Calculation` tree and evaluate it with
`evaluate`:

```kotlin
import org.thundernetwork.permissioner.*

val granted = listOf("user.read", "admin.reports.view")

val rule = Or(listOf(
    PermissionCheck(listOf("admin.*")),
    And(listOf(
        PermissionCheck(listOf("user.read")),
        Not(PermissionCheck(listOf("user.delete")))
    ))
))

evaluate(granted, rule) // true
```

This reads as: *"grant access if the user is an admin, OR if they can read users but cannot
delete them."*

### Replacements

Permissions can contain placeholders that get substituted at check time — useful for
resource-scoped permissions like `user.{userId}.edit`:

```kotlin
import org.thundernetwork.permissioner.*

val options = CheckSingleOptions(
    replacements = listOf(
        Replacement(
            key = "{userId}",
            replacement = "123",
            replacementLocation = ReplacementLocation.REQUIRED_AND_GRANTED
        )
    )
)

checkSingle("user.{userId}.edit", "user.123.edit", options) // true
```

`ReplacementLocation` controls where the substitution is applied:

| Value | Effect |
|---|---|
| `GRANTED` | Replaces only in the granted permission |
| `REQUIRED` | Replaces only in the required permission |
| `REQUIRED_AND_GRANTED` | Replaces in both |

---

## API reference

### `checkSingle`

```kotlin
fun checkSingle(
    permission: Permission,
    requiredPermission: Permission,
    options: CheckSingleOptions? = null
): Boolean
```

Checks whether a single granted permission satisfies a single required permission.
See [Comparison tables](#comparison-tables) for the full matching matrix.

### `checkList`

```kotlin
fun checkList(
    permissions: PermissionList,
    requiredPermissions: PermissionList
): Boolean
```

Checks whether **all** required permissions are satisfied by a list of granted permissions,
honoring `not.`-prefixed negative permissions.

### `evaluate`

```kotlin
fun evaluate(
    permissions: PermissionList,
    calculation: Calculation,
    options: EvaluateOptions? = null
): Boolean
```

Recursively evaluates a `Calculation` tree (`And`, `Or`, `Not`, or `PermissionCheck`) against a
list of granted permissions.

### Types

```kotlin
typealias Permission = String
typealias PermissionList = List<Permission>

sealed interface Calculation
data class And(val and: List<Calculation>) : Calculation
data class Or(val or: List<Calculation>) : Calculation
data class Not(val not: Calculation) : Calculation
data class PermissionCheck(val permissions: PermissionList) : Calculation
```

---

## Comparison tables

### `checkSingle` — segment-by-segment matching

With `x`, `y` as two different concrete segments, empty string (`""`) representing an
omitted/terminated segment, and `*` the wildcard:

| Granted | Required | Result | Description |
|---|---|---|---|
| `*` | `*` | `TRUE` | Equal permissions |
| `x` | `x` | `TRUE` | Equal permissions |
| `x` | `y` | `FALSE` | Different permissions |
| *(empty)* | `x` | `TRUE` | Empty segment represents all permissions below it |
| `x` | *(empty)* | `FALSE` | A specific permission does not satisfy a broader requirement |
| *(empty)* | `*` | `TRUE` | Empty segment includes wildcard |
| `*` | *(empty)* | `FALSE` | Wildcard does not satisfy a broader requirement |
| `*` | `x` | `TRUE` | Wildcard includes everything |
| `x` | `*` | `FALSE` | A specific permission does not satisfy a wildcard requirement |
| `not` | `x` | `ERROR` | A required permission can never start with `not` |
| `x` | `not` | `FALSE` | A negated granted permission never matches |

---

## Roadmap

- [x] Port core logic (`checkSingle`, `checkList`, `evaluate`) from TypeScript to Kotlin
- [ ] Publish Kotlin artifact to Maven Central / GitHub Packages
- [ ] Kotlin/JS target for npm distribution (drop-in replacement for the original JS package)
- [ ] KDoc-generated API site

---

## Contributing

Issues and pull requests are welcome. Please include tests for any new matching behavior —
the permission-matching rules are subtle and easy to regress.

## License

MIT — see [LICENSE](LICENSE) for details.