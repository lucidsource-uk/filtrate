package uk.co.lucidsource.filtrate.api.expression

/**
 * Interface representing a group of filter expressions.
 *
 * @param T the type of the filter expressions
 */
interface FilterGroupExpression<T> {
    val all: List<T>?

    val any: List<T>?

    val not: T
}
