# Filtrate - compile time API filters
Filtrate is an annotation processor which generates filter criteria DTOs for use in API dialects such as REST, or GraphQL.

### Adding filtrate

The jitpack maven repository must be added to the build.gradle file.
```gradle
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

#### Java
Annotation processors are enabled in java using the `annotationProcessor` gradle directive.
```gradle
dependencies {
        annotationProcessor "uk.co.lucidsource.filtrate:filtrate-apt:1.0"
        implementation "uk.co.lucidsource.filtrate:filtrate-api:1.0"
}
```

#### Kotlin
Annotation processors are enabled in kotlin using the `kapt` directive.
```gradle
dependencies {
    kapt("uk.co.lucidsource.filtrate:filtrate-apt:1.0")
    implementation("uk.co.lucidsource.filtrate:filtrate-api:1.0")
}
```

### Java

Java class filters are built by annotating classes with the `@Filter` annotation. Properties are included as filterable by annotating them with `FilterProperty`. `FilterProperty` is only supported on fields. 

```java
@Filter
public class MyFilterableDto {
    @FilterProperty(FilterOperator.EQ, FilterOperator.IN)
    private String name;

    @FilterProperty(FilterOperator.GT, FilterOperator.LT)
    private Integer age;

    // getters and setters
}
```

### Kotlin
Kotlin class filters are built by annotating classes with the `@Filter` annotation. Properties are included as filterable by annotating them with `FilterProperty`. `FilterProperty` is only supported on fields.

```kotlin
@Filter
data class MyFilterableDto(
    @FilterProperty(FilterOperator.EQ, FilterOperator.IN)
    val name: String,

    @FilterProperty(FilterOperator.GT, FilterOperator.LT)
    val age: Int
)
```

### Generated filters interface
The above annotations will generate a `MyFilterableDtoFilter` class, which can then be used in codefirst API definitions, or graphQL as an input parameter. Filters have the following interface:

```java
MyFilterableDtoFilter filter = MyFilterableDtoFilter.all(
    MyFilterableDtoFilter.NameExpression.eq(
        "john"
    ),
    MyFilterableDtoFilter.AgeExpression.gt(
        25
    )
);
```

JSON notation format:

```json
{
  "all": [
    {
      "firstname": {
        "eq": "john"
      },
      "age": {
        "gt": 25
      }
    }
  ]
}
```

GraphQL notation format:
```graphql
{
    queryDtos(filter: {
        all: [
            {
                firstname: {
                    eq: "john"
                }
            },
            {
                age: {
                    gt: 25
                }
            }
        ]
  }) {
    id,
    firstname
  }
}
```
### Converting filter AST to database queries
Generated filter classes expose a method named `ast()` which returns an `Expression`. Expression exposes a visitor interface over the AST, enabling for simple parsing and conversion into a database expression.
An example of a simple converter into a mongodb morphia query is as follows:

```kotlin
class MongoDbCriteriaExpressionVisitor : ExpressionVisitor<Filter> {
    override fun visit(expression: Expression): Filter {
        when (expression) {
            is FilterGroupCompoundExpression -> {
                val filters = expression.expressions
                    .map { it.accept(this) }
                    .toTypedArray()

                return when (expression.operator) {
                    FilterGroupOperator.ANY -> Filters.and(*filters)
                    FilterGroupOperator.ALL -> Filters.or(*filters)
                    FilterGroupOperator.NOT -> {
                        throw IllegalArgumentException()
                    }

                    null -> {
                        throw IllegalStateException()
                    }
                }
            }

            is FilterGroupUnaryExpression -> {
                return when (expression.operator) {
                    FilterGroupOperator.NOT -> Filters.nor(expression.expression.accept(this))
                    FilterGroupOperator.ALL, FilterGroupOperator.ANY, null -> {
                        throw IllegalStateException()
                    }
                }
            }

            is FilterUnaryExpression<*> -> {
                return when (expression.operator) {
                    FilterOperator.LIKE -> Filters.regex(expression.field, expression.value.toString())
                    FilterOperator.EQ -> Filters.eq(expression.field, expression.value)
                    FilterOperator.GT -> Filters.gt(expression.field, expression.value)
                    FilterOperator.GT_EQ -> Filters.gte(expression.field, expression.value)
                    FilterOperator.LT -> Filters.lt(expression.field, expression.value)
                    FilterOperator.LT_EQ -> Filters.lte(expression.field, expression.value)
                    FilterOperator.IN, null -> {
                        throw IllegalArgumentException("Unknown unary operator ${expression.operator}")
                    }
                }
            }

            is FilterCompoundExpression<*> -> {
                return when (expression.operator) {
                    FilterOperator.IN -> Filters.`in`(expression.field, expression.values)
                    FilterOperator.LIKE, FilterOperator.EQ, FilterOperator.GT, FilterOperator.GT_EQ, FilterOperator.LT, FilterOperator.LT_EQ, null -> {
                        throw IllegalArgumentException("Unknown compound operator ${expression.operator}")
                    }
                }
            }

            else -> {
                throw IllegalArgumentException("Unknown visited class ${expression.javaClass}")
            }
        }
    }
}
```

Filter classes are then converter into queries with the following code:
```kotlin
// val mongoDbCriteriaExpressionVisitor MongoDbCriteriaExpressionVisitor
// val inputFilter MyFilterableDtoFilter
val dbQuery = mongoDbCriteriaExpressionVisitor.visit(inputFilter.ast())
datastore.find(MyFilterableDto::class.java).filter(dbQuery).toList()
```