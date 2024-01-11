package co.uk.lucidsource.filtrate.constants

object CodeBlockFormat {
    const val Literal =
        "\$L" // emits a literal value with no escaping. Arguments for literals may be strings, primitives, type declarations, annotations and even other code blocks.
    const val Name =
        "\$N" // emits a name, using name collision avoidance where necessary. Arguments for names may be strings (actually any character sequence), parameters, fields, methods, and types.
    const val String =
        "\$S" // escapes the value as a string, wraps it with double quotes, and emits that. For example, 6" sandwich is emitted "6\" sandwich".
    const val Type =
        "\$T" // emits a type reference. Types will be imported if possible. Arguments for types may be classes, ,* type mirrors, and elements.
    const val Dollar = "$$" // emits a dollar sign.
    const val Whitespace =
        "\$W" // emits a space or a newline, depending on its position on the line. This prefers to wrap lines before 100 columns.
    const val ZeroSpace = "\$Z" // acts as a zero-width space. This prefers to wrap lines before 100 columns.
}