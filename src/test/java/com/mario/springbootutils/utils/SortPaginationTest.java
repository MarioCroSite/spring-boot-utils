package com.mario.springbootutils.utils;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.function.Function;

import static com.mario.springbootutils.utils.SortPagination.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.data.domain.Pageable.ofSize;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

public class SortPaginationTest {

    @Test
    void verify_sort_order_comparator() {
        final Function<String, Integer> keyExtractor = String::length;
        final var ascendingOrder = new SortPagination<>(keyExtractor, true);
        final var descendingOrder = new SortPagination<>(keyExtractor, false);

        final var list = asList("bike", "train", "car");
        list.sort(ascendingOrder.getComparator());
        assertEquals(asList("car", "bike", "train"), list);

        list.sort(descendingOrder.getComparator());
        assertEquals(asList("train", "bike", "car"), list);
    }

    @Test
    void verify_extract_sort_params() {
        final var sortParam1 = new Sort.Order(ASC, "name");
        final var sortParam2 = new Sort.Order(DESC, "age");
        final var pageable = of(0, 10, Sort.by(sortParam1, sortParam2));

        final var sortParams = extractSortParams(pageable);
        assertEquals(asList(
                new SortParam("name", true),
                new SortParam("age", false)), sortParams);
    }

    @Test
    void verify_sort_and_page_data() {
        final var people = asList(
                new Person("Charlie", 30, new Address("New York", "USA")),
                new Person("Alice", 25, new Address("Paris", "France")),
                new Person("Bob", 20, new Address("Los Angeles", "USA")),
                new Person("David", 35, new Address("Los Angeles", "USA")),
                new Person("Eva", 28, new Address("Paris", "France"))
        );

        final var sortParams = asList(
                new SortParam("age", true),
                new SortParam("city", true)
        );

        final var columnMap = new HashMap<String, Function<Person, ? extends Comparable<?>>>();
        columnMap.put("name", Person::name);
        columnMap.put("age", Person::age);
        columnMap.put("city", person -> person.address().city());
        columnMap.put("country", person -> person.address().country());

        final var sortOrders = createSortPagination(sortParams, columnMap);
        final var pageable = ofSize(3).withPage(0);

        final var resultPage = sortAndPageData(people, sortOrders, pageable);

        assertThat(resultPage.getContent()).hasSize(3);
        assertThat(resultPage.getContent()).extracting(Person::name)
                .containsExactly("Bob", "Alice", "Eva"); // Sorted by age then by city
        assertThat(resultPage.getTotalElements()).isEqualTo(5);
    }

    public record Address(String city, String country) {}
    public record Person(String name, int age, Address address) {}
}
