package co.uk.lucidsource.filtrate.api.ast

import co.uk.lucidsource.filtrate.api.FilterGroupOperator

class FilterGroupUnaryExpression(
    val operator: FilterGroupOperator,
    val expression: Expression
) : Expression {
    override fun <V> accept(visitor: ExpressionVisitor<V>): V {
        return visitor.visit(this)
    }
}
