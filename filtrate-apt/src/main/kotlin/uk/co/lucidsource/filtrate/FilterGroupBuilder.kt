package uk.co.lucidsource.filtrate

import uk.co.lucidsource.filtrate.api.FilterGroupOperator
import uk.co.lucidsource.filtrate.api.ast.Expression
import uk.co.lucidsource.filtrate.constants.CodeBlockFormat
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeVariableName
import jakarta.validation.Valid
import java.util.Optional
import javax.lang.model.element.Modifier

object FilterGroupBuilder {
    private fun FilterGroupOperator.getPropertyTypeName(
        expressionParameterizedType: TypeName
    ): TypeName {
        return if (this.isCompound) ParameterizedTypeName.get(
            ClassName.get(MutableList::class.java),
            expressionParameterizedType
        ) else expressionParameterizedType
    }

    fun buildFilterGroupOperatorField(
        expressionParameterizedType: TypeVariableName,
        filterGroupOperator: FilterGroupOperator
    ): FieldSpec {
        return FieldSpec
            .builder(
                filterGroupOperator.getPropertyTypeName(expressionParameterizedType),
                filterGroupOperator.fieldName,
                Modifier.PUBLIC
            )
            .addAnnotation(ClassName.get(Valid::class.java))
            .build()
    }

    fun buildFilterGroupOperatorFieldGetter(
        expressionParameterizedType: TypeVariableName,
        filterGroupOperator: FilterGroupOperator
    ): MethodSpec {
        val methodName = "get" + filterGroupOperator.fieldName.replaceFirstChar(Char::titlecase)

        return MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return this.${filterGroupOperator.fieldName}")
            .returns(
                filterGroupOperator.getPropertyTypeName(expressionParameterizedType)
            )
            .build()
    }

    fun buildFilterGroupOperatorFieldSetter(
        expressionParameterizedType: TypeVariableName,
        filterGroupOperator: FilterGroupOperator
    ): MethodSpec {
        val methodName = filterGroupOperator.fieldName
        val valueAssignment = if(filterGroupOperator.isCompound) "java.util.Arrays.asList(newValue)" else "newValue"

        return MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .varargs(filterGroupOperator.isCompound)
            .addParameter(
                ParameterSpec.builder(
                    if (filterGroupOperator.isCompound) ArrayTypeName.of(expressionParameterizedType) else expressionParameterizedType,
                    "newValue"
                ).build()
            )
            .addStatement(
                "${CodeBlockFormat.Type} newClassWithValue = new ${CodeBlockFormat.Type}()",
                expressionParameterizedType,
                expressionParameterizedType
            )
            .addStatement("newClassWithValue.${filterGroupOperator.fieldName} = ${valueAssignment}")
            .addStatement("return newClassWithValue")
            .returns(
                expressionParameterizedType
            )
            .build()
    }

    fun buildFilterGroupOperatorASTCodeBlock(
        filterGroupOperator: FilterGroupOperator
    ): CodeBlock {
        val mapToAst =
            if (filterGroupOperator.isCompound) "stream().map(i -> i.ast()).filter(java.util.Objects::nonNull).toList()" else "ast()"
        val expressionType = filterGroupOperator.getPropertyTypeName(ClassName.get(Expression::class.java))
        val expressionClass = filterGroupOperator.expressionType
        val temporaryVariableName = filterGroupOperator.fieldName + "Check"
        val checkIfValid = if (filterGroupOperator.isCompound) "!field.isEmpty()" else "field != null"

        return CodeBlock.of(
            """
                ${CodeBlockFormat.Type} $temporaryVariableName = java.util.Optional.ofNullable(this.${filterGroupOperator.fieldName})
                    .map(field -> field.$mapToAst)
                    .filter(field -> $checkIfValid);
                    
                if($temporaryVariableName.isPresent()) {
                    return new ${CodeBlockFormat.Type}(
                        ${CodeBlockFormat.Type}.${CodeBlockFormat.Literal},
                        $temporaryVariableName.get()
                    );
                }
                        """.trimIndent(),
            ParameterizedTypeName.get(ClassName.get(Optional::class.java), expressionType),
            expressionClass,
            FilterGroupOperator::class.java,
            filterGroupOperator
        )
    }
}