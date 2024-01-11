package co.uk.lucidsource.filtrate.api.ast

import co.uk.lucidsource.filtrate.api.FilterGroupOperator

class FilterGroupCompoundExpression(
    val operator: FilterGroupOperator,
    val expressions: List<Expression>
) : Expression {
    override fun <V> accept(visitor: ExpressionVisitor<V>): V {
        return visitor.visit(this)
    }
}
