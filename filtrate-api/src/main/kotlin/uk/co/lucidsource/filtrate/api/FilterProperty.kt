package uk.co.lucidsource.filtrate.api

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class FilterProperty(vararg val value: FilterOperator, val name: String = "")
