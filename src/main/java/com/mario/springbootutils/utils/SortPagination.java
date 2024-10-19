package com.mario.springbootutils.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;

/**
 * The {@code SortPagination} class provides functionality for dynamically sorting and paginating
 * data collections based on a set of user-defined sorting parameters and a pageable object.
 * This class is generic and works with any data type {@code T}, as long as the fields by which
 * the sorting is performed implement the {@link Comparable} interface.
 * <p>
 * The core purpose of this class is to facilitate sorting and pagination of data in a flexible
 * manner, allowing the sorting columns to be dynamically mapped and applied during runtime.
 * The sorting order (ascending or descending) is determined by the user-provided parameters.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Supports multiple sorting orders for different fields, combining them into a single comparator.</li>
 *   <li>Allows for dynamic mapping of field names to sorting functions via the {@code columnMap}.</li>
 *   <li>Integrates seamlessly with Spring Data's {@link Pageable} interface for efficient data paging.</li>
 * </ul>
 *
 * <h3>How It Works:</h3>
 * <p>
 * The {@code SortPagination} class operates in several stages:
 * <ul>
 *   <li><b>Extracting sorting parameters:</b> The static method {@link #extractSortParams(Pageable)} reads sorting
 *   instructions (fields and order) from a Spring Data {@code Pageable} object. This method is particularly useful
 *   in Spring applications where sorting metadata comes from API requests.</li>
 *
 *   <li><b>Creating sort configurations:</b> The method {@link #createSortPagination(List, Map)} creates a list
 *   of {@code SortPagination} instances. Each instance is responsible for extracting the sort key for a
 *   specific field (using a function from the provided column map) and applying the correct sorting order
 *   (ascending or descending).</li>
 *
 *   <li><b>Sorting and paginating data:</b> The static method {@link #sortAndPageData(List, List, Pageable)}
 *   combines the sorting logic from multiple {@code SortPagination} instances and applies it to a given
 *   dataset. After sorting, the method uses the {@code Pageable} object to return only the requested page of data.</li>
 * </ul>
 * </p>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // Assume we have a list of objects of type T and a pageable request
 * List<T> dataList = ...;
 * Pageable pageable = ...;
 *
 * // Map column names to corresponding field extraction functions
 * Map<String, Function<T, ? extends Comparable<?>>> columnMap = new HashMap<>();
 * columnMap.put("name", T::getName);
 * columnMap.put("age", T::getAge);
 *
 * // Extract sorting parameters from pageable and create SortPagination instances
 * List<SortPagination<T>> sortOrders = SortPagination.createSortPagination(
 *     SortPagination.extractSortParams(pageable), columnMap);
 *
 * // Sort and paginate the data
 * Page<T> resultPage = SortPagination.sortAndPageData(dataList, sortOrders, pageable);
 * }</pre>
 *
 * @param <T> The type of elements in the dataset that will be sorted and paginated.
 */
public class SortPagination<T> {
    private final Function<T, ? extends Comparable<?>> keyExtractor;
    private final boolean ascending;

    public SortPagination(Function<T, ? extends Comparable<?>> keyExtractor, boolean ascending) {
        this.keyExtractor = keyExtractor;
        this.ascending = ascending;
    }

    public Comparator<T> getComparator() {
        @SuppressWarnings("unchecked")
        Comparator<T> comparator = comparing((Function<T, Comparable<Object>>) keyExtractor);
        return ascending ? comparator : comparator.reversed();
    }

    public static List<SortParam> extractSortParams(Pageable pageable) {
        return StreamSupport.stream(pageable.getSort().stream().spliterator(), false)
                .map(order -> new SortParam(order.getProperty(), order.isAscending()))
                .toList();
    }

    public static <T> List<SortPagination<T>> createSortPagination(
            List<SortParam> sortRequests,
            Map<String, Function<T, ? extends Comparable<?>>> columnMap) {

        return sortRequests.stream()
                .map(sortRequest -> {
                    final var column = sortRequest.field();
                    final var ascending = sortRequest.ascending();
                    return ofNullable(columnMap.get(column))
                            .map(keyExtractor -> new SortPagination<>(keyExtractor, ascending))
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Invalid sorting column name: " + column + ". Available columns: " + columnMap.keySet()));
                })
                .toList();
    }

    public static <T> Page<T> sortAndPageData(List<T> dataList, List<SortPagination<T>> sortOrders, Pageable pageable) {
        final var comparator = sortOrders.stream()
                .map(SortPagination::getComparator)
                .reduce(Comparator::thenComparing)
                .orElse((t1, t2) -> 0);

        final var sortedAndPagedData = dataList.stream()
                .sorted(comparator)
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .toList();

        return new PageImpl<>(sortedAndPagedData, pageable, dataList.size());
    }

    public record SortParam(String field, boolean ascending) {

    }
}
