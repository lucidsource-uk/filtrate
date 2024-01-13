package uk.co.lucidsource.filtrate.api.ast

import uk.co.lucidsource.filtrate.api.FilterGroupOperator

class FilterGroupUnaryExpression(
    val operator: FilterGroupOperator,
    val expression: Expression
) : Expression {
    override fun <V> accept(visitor: ExpressionVisitor<V>): V {
        return visitor.visit(this)
    }
}
