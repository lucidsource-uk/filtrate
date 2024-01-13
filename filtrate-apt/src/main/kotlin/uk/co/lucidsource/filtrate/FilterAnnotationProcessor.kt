package uk.co.lucidsource.filtrate

import uk.co.lucidsource.filtrate.FilterAnnotationProcessorUtils.applyAstGenerationMethod
import uk.co.lucidsource.filtrate.api.Filter
import uk.co.lucidsource.filtrate.api.FilterProperty
import uk.co.lucidsource.filtrate.model.ClassContext
import uk.co.lucidsource.filtrate.model.ClassField
import com.google.auto.service.AutoService
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.io.IOException
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedAnnotationTypes("uk.co.lucidsource.filtrate.api.Filter")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(
    Processor::class
)
class FilterAnnotationProcessor : AbstractProcessor() {
    override fun process(annotations: Set<TypeElement?>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.isEmpty()) {
            return false
        }

        roundEnv.getElementsAnnotatedWith(Filter::class.java).forEach { annotatedClass: Element ->
            processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "processing: ${annotatedClass.simpleName}")

            val packageName = processingEnv.elementUtils.getPackageOf(annotatedClass).toString() + ".generated.filter"
            val classContext = ClassContext("${annotatedClass.simpleName}Filter", packageName)

            val generatedClassBuilder = TypeSpec.classBuilder(classContext.className)
                .addModifiers(Modifier.PUBLIC)

            FilterAnnotationProcessorUtils.applyExpressionGroup(generatedClassBuilder, classContext)

            val annotatedElements: Set<Element?> = roundEnv.getElementsAnnotatedWith(
                FilterProperty::class.java
            )

            val classFields: List<ClassField> = annotatedElements.stream()
                .map { annotatedProperty: Element? ->
                    val annotation: FilterProperty = annotatedProperty!!.getAnnotation(
                        FilterProperty::class.java
                    )
                    val definedFieldName: String = annotation.name
                    val classFieldName =
                        definedFieldName.ifEmpty { annotatedProperty.simpleName.toString() }

                    ClassField(
                        classFieldName,
                        annotation.value,
                        TypeName.get(annotatedProperty.asType()).box()
                    )
                }
                .toList()

            FilterAnnotationProcessorUtils.applyExpressionGroupValidator(
                generatedClassBuilder,
                classContext,
                classFields
            )

            classFields.forEach { classField ->
                FilterAnnotationProcessorUtils.applyField(
                    generatedClassBuilder, classContext, classField
                )
            }

            applyAstGenerationMethod(generatedClassBuilder, classFields)

            val generatedClass = generatedClassBuilder.build()
            try {
                JavaFile.builder(packageName, generatedClass).build().writeTo(processingEnv.filer)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        return true
    }
}