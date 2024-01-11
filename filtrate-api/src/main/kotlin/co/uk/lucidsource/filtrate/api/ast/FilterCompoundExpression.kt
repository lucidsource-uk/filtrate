package co.uk.lucidsource.filtrate.api.ast

import co.uk.lucidsource.filtrate.api.FilterOperator

class FilterCompoundExpression<T>(
    val field: String,
    val operator: FilterOperator,
    val values: List<T>
) : Expression {
    override fun <V> accept(visitor: ExpressionVisitor<V>): V {
        return visitor.visit(this)
    }
}