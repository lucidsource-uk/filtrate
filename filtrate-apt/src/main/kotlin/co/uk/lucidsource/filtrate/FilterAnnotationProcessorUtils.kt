package co.uk.lucidsource.filtrate

import co.uk.lucidsource.filtrate.FilterBuilder.buildField
import co.uk.lucidsource.filtrate.FilterBuilder.buildFieldFilterOperatorField
import co.uk.lucidsource.filtrate.FilterBuilder.buildFieldFilterOperatorFieldSetter
import co.uk.lucidsource.filtrate.FilterBuilder.buildFieldSetter
import co.uk.lucidsource.filtrate.FilterBuilder.buildFilterOperatorASTCodeBlock
import co.uk.lucidsource.filtrate.FilterGroupBuilder.buildFilterGroupOperatorASTCodeBlock
import co.uk.lucidsource.filtrate.FilterGroupBuilder.buildFilterGroupOperatorField
import co.uk.lucidsource.filtrate.FilterGroupBuilder.buildFilterGroupOperatorFieldGetter
import co.uk.lucidsource.filtrate.FilterGroupBuilder.buildFilterGroupOperatorFieldSetter
import co.uk.lucidsource.filtrate.api.FilterGroupOperator
import co.uk.lucidsource.filtrate.api.ast.Expression
import co.uk.lucidsource.filtrate.api.ast.FilterGroupCompoundExpression
import co.uk.lucidsource.filtrate.api.expression.FilterExpression
import co.uk.lucidsource.filtrate.api.expression.FilterGroupExpression
import co.uk.lucidsource.filtrate.api.validation.FilterValid
import co.uk.lucidsource.filtrate.api.validation.FilterValidator
import co.uk.lucidsource.filtrate.constants.CodeBlockFormat
import co.uk.lucidsource.filtrate.constants.CodeBlockFormat.Literal
import co.uk.lucidsource.filtrate.constants.CodeBlockFormat.Type
import co.uk.lucidsource.filtrate.model.ClassContext
import co.uk.lucidsource.filtrate.model.ClassField
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.lang.model.element.Modifier

object FilterAnnotationProcessorUtils {

    const val NEWLINE = "\r\n"

    fun joinTypes(vararg args: String?): String {
        return java.lang.String.join(".", *args)
    }

    fun applyField(classBuilder: TypeSpec.Builder, classContext: ClassContext, classField: ClassField) {
        classBuilder.addField(buildField(classContext, classField))

        val fieldExpressionClassBuilder = TypeSpec.classBuilder(classField.classFieldExpressionClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addAnnotation(FilterValid::class.java)
            .addSuperinterface(FilterValidator::class.java)

        fieldExpressionClassBuilder.addFields(
            classField.operators.map { buildFieldFilterOperatorField(classField, it) }
        )

        fieldExpressionClassBuilder.addMethods(
            classField.operators.map { buildFieldFilterOperatorFieldSetter(classContext, classField, it) }
        )

        val fieldNames = classField.operators.joinToString(",") { it.fieldName }

        fieldExpressionClassBuilder.addMethod(
            MethodSpec.methodBuilder("validate")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(String::class.java))
                .addStatement(
                    "long fieldsSet = ${CodeBlockFormat.Type}.of(${fieldNames}).filter(java.util.Objects::nonNull).count()",
                    Stream::class.java
                )
                .addStatement(
                    "return (fieldsSet == 1L) ? null : ${CodeBlockFormat.String}",
                    "one filter criteria must be specified."
                )
                .build()
        )

        classBuilder.addMethod(
            buildFieldSetter(classContext, classField)
        )

        classBuilder.addType(
            fieldExpressionClassBuilder
                .addSuperinterface(
                    ClassName.get(FilterExpression::class.java)
                )
                .addMethod(
                    MethodSpec.methodBuilder("ast")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get(Expression::class.java))
                        .addCode(CodeBlock.join(classField.operators.map {
                            buildFilterOperatorASTCodeBlock(
                                classField,
                                it
                            )
                        }, NEWLINE))
                        .addStatement("return null")
                        .build()
                )
                .build()
        )
    }

    fun applyExpressionGroup(classBuilder: TypeSpec.Builder, classContext: ClassContext) {
        val expressionParameterizedType = TypeVariableName.get(classContext.className)

        classBuilder
            .addSuperinterface(
                ParameterizedTypeName.get(
                    ClassName.get(FilterGroupExpression::class.java),
                    expressionParameterizedType
                )
            )
            .addField(buildFilterGroupOperatorField(expressionParameterizedType, FilterGroupOperator.ANY))
            .addField(buildFilterGroupOperatorField(expressionParameterizedType, FilterGroupOperator.NOT))
            .addField(buildFilterGroupOperatorField(expressionParameterizedType, FilterGroupOperator.ALL))
            .addMethod(buildFilterGroupOperatorFieldGetter(expressionParameterizedType, FilterGroupOperator.ANY))
            .addMethod(buildFilterGroupOperatorFieldGetter(expressionParameterizedType, FilterGroupOperator.NOT))
            .addMethod(buildFilterGroupOperatorFieldGetter(expressionParameterizedType, FilterGroupOperator.ALL))
            .addMethod(buildFilterGroupOperatorFieldSetter(expressionParameterizedType, FilterGroupOperator.ANY))
            .addMethod(buildFilterGroupOperatorFieldSetter(expressionParameterizedType, FilterGroupOperator.NOT))
            .addMethod(buildFilterGroupOperatorFieldSetter(expressionParameterizedType, FilterGroupOperator.ALL))
    }

    fun applyExpressionGroupValidator(
        classBuilder: TypeSpec.Builder,
        classContext: ClassContext,
        fields: List<ClassField>
    ) {
        classBuilder.addAnnotation(FilterValid::class.java)
        classBuilder.addSuperinterface(FilterValidator::class.java)

        val filterGroupFieldNames = FilterGroupOperator.entries.joinToString(",") { it.fieldName }
        val fieldNames = fields.joinToString(",") { it.fieldName }

        classBuilder.addMethod(
            MethodSpec.methodBuilder("validate")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(String::class.java))
                .addStatement("long filterGroups = java.util.stream.Stream.of(${filterGroupFieldNames}).filter(java.util.Objects::nonNull).count()")
                .addStatement("boolean hasFilters = java.util.stream.Stream.of(${fieldNames}).filter(java.util.Objects::nonNull).count() == 1L")
                .addStatement("if(filterGroups > 0L && hasFilters){ return ${CodeBlockFormat.String}; }", "one filter group or many filter criteria must be specified.")
                .addStatement("return null")
                .build()
        )
    }

    fun applyAstGenerationMethod(
        classBuilder: TypeSpec.Builder,
        fields: List<ClassField>
    ) {
        val fieldNamesCommaSeperated = fields.stream()
            .map { it.fieldName }
            .collect(Collectors.joining(","))

        classBuilder
            .addSuperinterface(
                ClassName.get(FilterExpression::class.java)
            )
            .addMethod(
                MethodSpec.methodBuilder("ast")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.get(Expression::class.java))
                    .addCode(
                        CodeBlock.join(
                            FilterGroupOperator.entries.toTypedArray()
                                .map {
                                    buildFilterGroupOperatorASTCodeBlock(it)
                                }.toList(), NEWLINE
                        )
                    ).addCode(
                        CodeBlock.of(
                            """
                        $Type propertyExpressions = java.util.stream.Stream.of($Literal)
                            .filter(java.util.Objects::nonNull)
                            .map(i -> i.ast())
                            .toList();

                        if(!propertyExpressions.isEmpty()) {
                            return new $Type(
                                $Type.$Literal,
                                propertyExpressions
                            );
                        }

                        return null;
                        """.trimIndent(),
                            ParameterizedTypeName.get(
                                ClassName.get(List::class.java),
                                TypeVariableName.get(Expression::class.java)
                            ),
                            fieldNamesCommaSeperated,
                            FilterGroupCompoundExpression::class.java,
                            FilterGroupOperator::class.java,
                            FilterGroupOperator.ALL
                        )
                    )
                    .build()
            )
    }
}
