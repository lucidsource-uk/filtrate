package uk.co.lucidsource.filtrate.model

import uk.co.lucidsource.filtrate.api.FilterOperator
import com.squareup.javapoet.TypeName

data class ClassField(
    val fieldName: String,
    val operators: Array<out FilterOperator>,
    val type: TypeName
) {
    val classFieldExpressionClassName: String
        get() = String.format(
            "%sExpression",
            fieldName.replaceFirstChar(Char::titlecase)
        )
}
