package uk.co.lucidsource.filtrate

import uk.co.lucidsource.filtrate.api.validation.FilterValid
import uk.co.lucidsource.filtrate.api.validation.FilterValidator
import jakarta.validation.Validation
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SimpleValidationTest {
    @FilterValid
    data class TestDto(
        val isValid: Boolean
    ) : FilterValidator {
        override fun validate(): String? {
            return if (isValid) null else "not valid"
        }
    }

    @Test
    fun testValidationIsNotValid() {
        val validator = Validation.byDefaultProvider().configure().messageInterpolator(ParameterMessageInterpolator())
            .buildValidatorFactory().validator

        val violations = validator.validate(TestDto(false))

        assertEquals(1, violations.size)
        assertEquals("not valid", violations.first().message)
    }

    @Test
    fun testValidationIsValid() {
        val validator = Validation.byDefaultProvider().configure().messageInterpolator(ParameterMessageInterpolator())
            .buildValidatorFactory().validator

        val violations = validator.validate(TestDto(true))

        assertEquals(0, violations.size)
    }
}