package uk.co.lucidsource.filtrate.api.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class FilterValidConstraint : ConstraintValidator<FilterValid, FilterValidator> {
    override fun isValid(value: FilterValidator, context: ConstraintValidatorContext): Boolean {
        val validationError = value.validate()
        if (validationError != null) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate(validationError).addConstraintViolation()
            return false
        }
        return true
    }
}