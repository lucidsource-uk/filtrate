package co.uk.lucidsource.filtrate.api

import co.uk.lucidsource.filtrate.api.ast.Expression
import co.uk.lucidsource.filtrate.api.ast.FilterGroupCompoundExpression
import co.uk.lucidsource.filtrate.api.ast.FilterGroupUnaryExpression

enum class FilterGroupOperator(val fieldName: String, val isCompound: Boolean) {
    ALL("all", true),
    NOT("not", false),
    ANY("any", true);

    val expressionType: Class<out Expression> =
        if (isCompound) FilterGroupCompoundExpression::class.java else FilterGroupUnaryExpression::class.java
}
