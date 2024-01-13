package uk.co.lucidsource.filtrate.api.ast

import uk.co.lucidsource.filtrate.api.FilterGroupOperator

class FilterGroupCompoundExpression(
    val operator: FilterGroupOperator,
    val expressions: List<Expression>
) : Expression {
    override fun <V> accept(visitor: ExpressionVisitor<V>): V {
        return visitor.visit(this)
    }
}
