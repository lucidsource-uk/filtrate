package co.uk.lucidsource.filtrate.api.ast

interface Expression {
    fun <T> accept(visitor: ExpressionVisitor<T>): T
}
