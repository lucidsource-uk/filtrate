package uk.co.lucidsource.filtrate.api.ast

interface ExpressionVisitor<T> {
    fun visit(expression: Expression): T
}
