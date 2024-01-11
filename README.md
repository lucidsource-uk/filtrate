# filtrate
Filtrate is an annotation processor which generates filter criteria DTOs for use in API dialects such as REST, or GraphQL


```java
@Filter
public class MyJavaObject {
    // @FilterProperty enables a property on the DTO as filterable
    // FilterOperator is an enum of available filer operators.
    @FilterProperty(FilterOperator.EQ, FilterOperator.IN)
    public String propertyToFilter;
}
```

The above annotations will generate a `MyJavaObjectFilter` class, which can be used in the following way:

```java
MyJavaObjectFilter filter = MyJavaObjectFilter.any(
    MyJavaObjectFilter.PropertyToFilterExpression.eq(
        "value to filter"
    )
);
```

The generated filter works well with smallrye graphql, for example:

```java
@GraphQLApi
public class MyResource {
    @Query
    public List<MyJavaObject> getObjects(MyJavaObjectFilter filter) {
        Expression expression = filter.ast();
        // convert ast to DB query
        // return filtered results
    }
}
```

When used in graphql - the experience looks like this:

```graphql
{
    getObjects(filter: {
        propertyToFilter: {
            eq: "test.*"
        }
  }) {
    id,
    firstname
  }
}
```