# spring-boot-utils

## Overview

`spring-boot-utils` is a collection of utility classes designed to simplify common tasks and enhance productivity while 
working with Spring Boot applications. This project aims to provide reusable components that can help developers 
avoid redundancy and streamline their development process.

## Features

- **Utility Classes**: A variety of utility classes that can be easily integrated into your Spring Boot applications.
- **Simplified Code**: Reduce boilerplate code by using pre-built utility methods.
- **Improved Readability**: Enhance the readability and maintainability of your codebase.

## Utility Classes

| Class Name       | Description                                        |
|------------------|----------------------------------------------------|
| `SortPagination` | Facilitates sorting and pagination of collections. |

### SortPagination
This class facilitates sorting and pagination of data, making it easier to manage and display collections of objects 
in a paginated format.

#### Key Features of SortPagination:

- **Flexible Sorting**: Easily sort collections based on multiple fields, including nested object properties.
- **Pagination Support**: Retrieve specific pages of data with configurable page sizes.
- **Type Safety**: Generic implementation allows for usage with any data type.

#### Example Usage
Here is a quick example of how to use the `SortPagination` class:

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// Assuming you have a List<Person> people
List<Person> people = ...; // your data list
List<SortPagination.SortParam> sortParams = Arrays.asList(
        new SortPagination.SortParam("age", true), 
        new SortPagination.SortParam("city", true)
);
List<SortPagination<Person>> sortOrders = SortPagination.createSortPagination(sortParams, columnMap);
Pageable pageable = Pageable.ofSize(3).withPage(0); 

Page<Person> resultPage = SortPagination.sortAndPageData(people, sortOrders, pageable);
```

## Additional Examples

For more examples and detailed usage, please refer to the test cases located in the `src/test/java` 
directory of the project. The test cases provide various scenarios and demonstrate how to effectively use the 
utility classes in real-world applications. By examining the tests, you can gain insights into different 
use cases and best practices.