package uk.co.lucidsource.filtrate

import uk.co.lucidsource.filtrate.api.Filter
import uk.co.lucidsource.filtrate.api.FilterOperator
import uk.co.lucidsource.filtrate.api.FilterProperty

@Filter
data class SimpleDto(
    @FilterProperty(FilterOperator.EQ, FilterOperator.IN)
    val aField: String,

    @FilterProperty(FilterOperator.EQ, FilterOperator.IN)
    val anotherField: Int
)
