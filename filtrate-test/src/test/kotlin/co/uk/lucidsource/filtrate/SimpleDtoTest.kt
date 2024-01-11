package co.uk.lucidsource.filtrate

import co.uk.lucidsource.filtrate.generated.filter.SimpleDtoFilter
import co.uk.lucidsource.filtrate.generated.filter.SimpleDtoFilter.AFieldExpression
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Validation
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

open class SimpleDtoTest {
    @Test
    fun testSimpleDtoSerialization() {
        val mapper = ObjectMapper()
        val aFieldValue = AFieldExpression.eq("This is a field.")
        val testInstance = SimpleDtoFilter.all(
            SimpleDtoFilter.aField(aFieldValue)
        )

        val deserialized = mapper.readValue(
            """{
            "all": [
                {
                    "aField": {
                        "eq": "This is a field."
                    }
                }
            ]
        }""", SimpleDtoFilter::class.java
        )

        assertEquals(aFieldValue.eq, testInstance.all[0].aField.eq)
        assertEquals(aFieldValue.eq, deserialized.all[0].aField.eq)
    }

    @Test
    fun testSimpleDtoValidation() {
        val validator = Validation.byDefaultProvider().configure().messageInterpolator(ParameterMessageInterpolator())
            .buildValidatorFactory().validator

        val aFieldValue = AFieldExpression.eq("This is a field.")
        val testInstance = SimpleDtoFilter.all(
            SimpleDtoFilter.aField(aFieldValue)
        )

        val constraintViolations = validator.validate(testInstance)

        assertEquals(0, constraintViolations.size)
    }

    @Test
    fun testSimpleDtoValidationValidEmpty() {
        val validator = Validation.byDefaultProvider().configure().messageInterpolator(ParameterMessageInterpolator())
            .buildValidatorFactory().validator

        val testInstance = SimpleDtoFilter()

        val constraintViolations = validator.validate(testInstance)

        assertEquals(0, constraintViolations.size)
    }

    @Test
    fun testSimpleDtoValidationInvalidExpression() {
        val validator = Validation.byDefaultProvider().configure().messageInterpolator(ParameterMessageInterpolator())
            .buildValidatorFactory().validator

        val testInstance = SimpleDtoFilter.all(
            SimpleDtoFilter.aField(AFieldExpression())
        )

        val constraintViolations = validator.validate(testInstance)

        assertEquals(1, constraintViolations.size)
        assertEquals("one filter criteria must be specified.", constraintViolations.first().message)
    }

    @Test
    fun testSimpleDtoValidationInvalidGroup() {
        val validator = Validation.byDefaultProvider().configure().messageInterpolator(ParameterMessageInterpolator())
            .buildValidatorFactory().validator

        val testInstance = SimpleDtoFilter()

        testInstance.aField = AFieldExpression.eq("test")
        testInstance.all = listOf(SimpleDtoFilter.aField(AFieldExpression.eq("test")))

        val constraintViolations = validator.validate(testInstance)

        assertEquals(1, constraintViolations.size)
        assertEquals(
            "one filter group or many filter criteria must be specified.",
            constraintViolations.first().message
        )
    }
}