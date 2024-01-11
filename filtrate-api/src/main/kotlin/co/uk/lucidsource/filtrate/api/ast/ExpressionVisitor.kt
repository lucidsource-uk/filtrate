package co.uk.lucidsource.filtrate.api.ast

interface ExpressionVisitor<T> {
    fun visit(expression: Expression): T
}
