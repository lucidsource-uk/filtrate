package uk.co.lucidsource.filtrate.api.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Constraint(validatedBy = [FilterValidConstraint::class])
@Target(AnnotationTarget.CLASS)
@Retention(
    AnnotationRetention.RUNTIME
)
annotation class FilterValid(
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
