package uk.co.lucidsource.filtrate.api

import uk.co.lucidsource.filtrate.api.ast.Expression
import uk.co.lucidsource.filtrate.api.ast.FilterGroupCompoundExpression
import uk.co.lucidsource.filtrate.api.ast.FilterGroupUnaryExpression

enum class FilterGroupOperator(val fieldName: String, val isCompound: Boolean) {
    ALL("all", true),
    NOT("not", false),
    ANY("any", true);

    val expressionType: Class<out Expression> =
        if (isCompound) FilterGroupCompoundExpression::class.java else FilterGroupUnaryExpression::class.java
}
