package co.uk.lucidsource.filtrate

import co.uk.lucidsource.filtrate.api.FilterOperator
import co.uk.lucidsource.filtrate.constants.CodeBlockFormat
import co.uk.lucidsource.filtrate.model.ClassContext
import co.uk.lucidsource.filtrate.model.ClassField
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import javax.lang.model.element.Modifier

object FilterBuilder {
    private fun FilterOperator.getPropertyTypeName(
        expressionParameterizedType: TypeName
    ): TypeName {
        return if (this.isCompound) ParameterizedTypeName.get(
            ClassName.get(MutableList::class.java),
            expressionParameterizedType
        ) else expressionParameterizedType
    }

    fun buildFilterOperatorASTCodeBlock(
        classField: ClassField,
        filterOperator: FilterOperator
    ): CodeBlock {
        return CodeBlock.of(
            """
            if (this.${filterOperator.fieldName} != null) {
                return new ${CodeBlockFormat.Type}(
                    "${classField.fieldName}",
                    ${CodeBlockFormat.Type}.${CodeBlockFormat.Literal},
                    this.${filterOperator.fieldName}
                );
            }
            """.trimIndent(),
            filterOperator.expressionType, filterOperator.javaClass, filterOperator
        )
    }

    fun buildFieldSetter(classContext: ClassContext, classField: ClassField): MethodSpec {
        val methodName = classField.fieldName
        val classType = ClassName.get("", classContext.className)
        val classFieldType = ClassName.get("", classField.classFieldExpressionClassName)

        return MethodSpec.methodBuilder(methodName)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(classType)
            .addParameter(classFieldType, "newValue")
            .addStatement(
                "${CodeBlockFormat.Type} classWithFieldSet = new ${CodeBlockFormat.Type}()",
                classType,
                classType
            )
            .addStatement("classWithFieldSet.${methodName} = newValue")
            .addStatement("return classWithFieldSet")
            .build()
    }

    fun buildField(classContext: ClassContext, classField: ClassField): FieldSpec {
        return FieldSpec.builder(
            ClassName.get(
                FilterAnnotationProcessorUtils.joinTypes(classContext.packageName, classContext.className),
                classField.classFieldExpressionClassName
            ),
            classField.fieldName,
            Modifier.PUBLIC
        )
            .addAnnotation(ClassName.get(Valid::class.java))
            .build()
    }

    fun buildFieldFilterOperatorField(
        classField: ClassField,
        filterOperator: FilterOperator
    ): FieldSpec {
        val fieldSpecBuilder: FieldSpec.Builder = FieldSpec.builder(
            filterOperator.getPropertyTypeName(classField.type),
            filterOperator.fieldName,
            Modifier.PUBLIC
        )

        if (filterOperator.isCompound) {
            val min = 1
            val max = 50
            fieldSpecBuilder.addAnnotation(
                AnnotationSpec.builder(ClassName.get(Size::class.java))
                    .addMember("min", "\$L", min)
                    .addMember("max", "\$L", max)
                    .build()
            )
        }

        return fieldSpecBuilder.build()
    }

    fun buildFieldFilterOperatorFieldSetter(
        classContext: ClassContext,
        classField: ClassField,
        filterOperator: FilterOperator
    ): MethodSpec {
        val methodName = filterOperator.fieldName
        val expressionClassType = ClassName.get(
            "${classContext.packageName}.${classContext.className}",
            classField.classFieldExpressionClassName
        )

        return MethodSpec.methodBuilder(
            methodName,
        )
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(expressionClassType)
            .addParameter(filterOperator.getPropertyTypeName(classField.type), "newValue")
            .addStatement(
                "${CodeBlockFormat.Type} classWithNewValue = new ${CodeBlockFormat.Type}()",
                expressionClassType,
                expressionClassType
            )
            .addStatement("classWithNewValue.${methodName} = newValue")
            .addStatement("return classWithNewValue")
            .build()
    }
}