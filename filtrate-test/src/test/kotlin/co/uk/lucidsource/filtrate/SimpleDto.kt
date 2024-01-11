package co.uk.lucidsource.filtrate

import co.uk.lucidsource.filtrate.api.Filter
import co.uk.lucidsource.filtrate.api.FilterOperator
import co.uk.lucidsource.filtrate.api.FilterProperty

@Filter
data class SimpleDto(
    @FilterProperty(FilterOperator.EQ, FilterOperator.IN)
    val aField: String,

    @FilterProperty(FilterOperator.EQ, FilterOperator.IN)
    val anotherField: Int
)
