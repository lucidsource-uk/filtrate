package uk.co.lucidsource.filtrate.api

import uk.co.lucidsource.filtrate.api.ast.Expression
import uk.co.lucidsource.filtrate.api.ast.FilterCompoundExpression
import uk.co.lucidsource.filtrate.api.ast.FilterUnaryExpression

enum class FilterOperator(val fieldName: String, val isCompound: Boolean) {
    LIKE("like", false),
    EQ("eq", false),
    GT("gt", false),
    GT_EQ("gtEq", false),
    LT("lt", false),
    LT_EQ("ltEq", false),
    IN("in", true);

    val expressionType: Class<out Expression> =
        if (isCompound) FilterCompoundExpression::class.java else FilterUnaryExpression::class.java
}
