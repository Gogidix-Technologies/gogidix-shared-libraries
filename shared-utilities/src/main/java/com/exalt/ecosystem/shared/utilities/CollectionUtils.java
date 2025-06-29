package com.exalt.ecosystem.shared.utilities;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * Comprehensive collection utility class for the Exalt Social E-commerce Ecosystem.
 * Provides advanced collection manipulation, filtering, transformation, and validation methods.
 * 
 * <p>This utility class handles common collection operations across the ecosystem including:
 * null-safe operations, filtering, grouping, transformation, and validation operations.</p>
 * 
 * @author Exalt Development Team
 * @since 1.0.0
 */
public final class CollectionUtils {
    
    private CollectionUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Checks if a collection is null or empty.
     * 
     * @param collection collection to check
     * @return true if collection is null or empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    /**
     * Checks if a collection is not null and not empty.
     * 
     * @param collection collection to check
     * @return true if collection is not null and not empty
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }
    
    /**
     * Gets the size of a collection, handling null values.
     * 
     * @param collection collection to measure
     * @return size of collection or 0 if null
     */
    public static int size(Collection<?> collection) {
        return collection != null ? collection.size() : 0;
    }
    
    /**
     * Returns the collection if not null, otherwise returns empty list.
     * 
     * @param collection collection to check
     * @param <T> element type
     * @return collection or empty list if null
     */
    public static <T> List<T> defaultList(List<T> collection) {
        return collection != null ? collection : new ArrayList<>();
    }
    
    /**
     * Returns the collection if not null, otherwise returns empty set.
     * 
     * @param collection collection to check
     * @param <T> element type
     * @return collection or empty set if null
     */
    public static <T> Set<T> defaultSet(Set<T> collection) {
        return collection != null ? collection : new HashSet<>();
    }
    
    /**
     * Returns the map if not null, otherwise returns empty map.
     * 
     * @param map map to check
     * @param <K> key type
     * @param <V> value type
     * @return map or empty map if null
     */
    public static <K, V> Map<K, V> defaultMap(Map<K, V> map) {
        return map != null ? map : new HashMap<>();
    }
    
    /**
     * Safely gets the first element from a collection.
     * 
     * @param collection collection to get from
     * @param <T> element type
     * @return first element or null if collection is empty
     */
    public static <T> T getFirst(Collection<T> collection) {
        if (isEmpty(collection)) return null;
        return collection.iterator().next();
    }
    
    /**
     * Safely gets the first element from a list.
     * 
     * @param list list to get from
     * @param <T> element type
     * @return first element or null if list is empty
     */
    public static <T> T getFirst(List<T> list) {
        if (isEmpty(list)) return null;
        return list.get(0);
    }
    
    /**
     * Safely gets the last element from a list.
     * 
     * @param list list to get from
     * @param <T> element type
     * @return last element or null if list is empty
     */
    public static <T> T getLast(List<T> list) {
        if (isEmpty(list)) return null;
        return list.get(list.size() - 1);
    }
    
    /**
     * Safely gets an element at the specified index.
     * 
     * @param list list to get from
     * @param index index to get
     * @param <T> element type
     * @return element at index or null if index is out of bounds
     */
    public static <T> T get(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) return null;
        return list.get(index);
    }
    
    /**
     * Safely gets an element at the specified index with default value.
     * 
     * @param list list to get from
     * @param index index to get
     * @param defaultValue default value if index is out of bounds
     * @param <T> element type
     * @return element at index or default value
     */
    public static <T> T get(List<T> list, int index, T defaultValue) {
        T result = get(list, index);
        return result != null ? result : defaultValue;
    }
    
    /**
     * Creates a new list with null elements removed.
     * 
     * @param collection source collection
     * @param <T> element type
     * @return new list without null elements
     */
    public static <T> List<T> removeNulls(Collection<T> collection) {
        if (isEmpty(collection)) return new ArrayList<>();
        
        return collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * Creates a new list with duplicate elements removed.
     * 
     * @param collection source collection
     * @param <T> element type
     * @return new list without duplicates
     */
    public static <T> List<T> removeDuplicates(Collection<T> collection) {
        if (isEmpty(collection)) return new ArrayList<>();
        
        return collection.stream()
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Creates a new list with null elements and duplicates removed.
     * 
     * @param collection source collection
     * @param <T> element type
     * @return new clean list
     */
    public static <T> List<T> clean(Collection<T> collection) {
        if (isEmpty(collection)) return new ArrayList<>();
        
        return collection.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Filters a collection using a predicate.
     * 
     * @param collection source collection
     * @param predicate filter predicate
     * @param <T> element type
     * @return filtered list
     */
    public static <T> List<T> filter(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) return new ArrayList<>();
        
        return collection.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }
    
    /**
     * Maps a collection to a new type using a function.
     * 
     * @param collection source collection
     * @param mapper mapping function
     * @param <T> source type
     * @param <R> result type
     * @return mapped list
     */
    public static <T, R> List<R> map(Collection<T> collection, Function<T, R> mapper) {
        if (isEmpty(collection) || mapper == null) return new ArrayList<>();
        
        return collection.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }
    
    /**
     * Groups a collection by a classifier function.
     * 
     * @param collection source collection
     * @param classifier grouping function
     * @param <T> element type
     * @param <K> key type
     * @return grouped map
     */
    public static <T, K> Map<K, List<T>> groupBy(Collection<T> collection, Function<T, K> classifier) {
        if (isEmpty(collection) || classifier == null) return new HashMap<>();
        
        return collection.stream()
                .collect(Collectors.groupingBy(classifier));
    }
    
    /**
     * Partitions a collection into two lists based on a predicate.
     * 
     * @param collection source collection
     * @param predicate partitioning predicate
     * @param <T> element type
     * @return map with true/false keys containing matching/non-matching elements
     */
    public static <T> Map<Boolean, List<T>> partition(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) {
            Map<Boolean, List<T>> result = new HashMap<>();
            result.put(true, new ArrayList<>());
            result.put(false, new ArrayList<>());
            return result;
        }
        
        return collection.stream()
                .collect(Collectors.partitioningBy(predicate));
    }
    
    /**
     * Finds the first element matching a predicate.
     * 
     * @param collection source collection
     * @param predicate search predicate
     * @param <T> element type
     * @return first matching element or null
     */
    public static <T> T find(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) return null;
        
        return collection.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Checks if any element matches a predicate.
     * 
     * @param collection source collection
     * @param predicate test predicate
     * @param <T> element type
     * @return true if any element matches
     */
    public static <T> boolean anyMatch(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) return false;
        
        return collection.stream().anyMatch(predicate);
    }
    
    /**
     * Checks if all elements match a predicate.
     * 
     * @param collection source collection
     * @param predicate test predicate
     * @param <T> element type
     * @return true if all elements match
     */
    public static <T> boolean allMatch(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) return true;
        
        return collection.stream().allMatch(predicate);
    }
    
    /**
     * Checks if no elements match a predicate.
     * 
     * @param collection source collection
     * @param predicate test predicate
     * @param <T> element type
     * @return true if no elements match
     */
    public static <T> boolean noneMatch(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) return true;
        
        return collection.stream().noneMatch(predicate);
    }
    
    /**
     * Counts elements matching a predicate.
     * 
     * @param collection source collection
     * @param predicate counting predicate
     * @param <T> element type
     * @return count of matching elements
     */
    public static <T> long count(Collection<T> collection, Predicate<T> predicate) {
        if (isEmpty(collection) || predicate == null) return 0;
        
        return collection.stream().filter(predicate).count();
    }
    
    /**
     * Joins a collection into a delimited string.
     * 
     * @param collection source collection
     * @param delimiter delimiter to use
     * @param <T> element type
     * @return joined string
     */
    public static <T> String join(Collection<T> collection, String delimiter) {
        if (isEmpty(collection)) return "";
        
        return collection.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(delimiter != null ? delimiter : ""));
    }
    
    /**
     * Joins a collection into a delimited string with prefix and suffix.
     * 
     * @param collection source collection
     * @param delimiter delimiter to use
     * @param prefix prefix for the result
     * @param suffix suffix for the result
     * @param <T> element type
     * @return joined string with prefix and suffix
     */
    public static <T> String join(Collection<T> collection, String delimiter, String prefix, String suffix) {
        if (isEmpty(collection)) return (prefix != null ? prefix : "") + (suffix != null ? suffix : "");
        
        return collection.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(
                    delimiter != null ? delimiter : "",
                    prefix != null ? prefix : "",
                    suffix != null ? suffix : ""
                ));
    }
    
    /**
     * Converts a collection to an array.
     * 
     * @param collection source collection
     * @param arrayType array type
     * @param <T> element type
     * @return array containing collection elements
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Collection<T> collection, Class<T> arrayType) {
        if (isEmpty(collection)) return (T[]) java.lang.reflect.Array.newInstance(arrayType, 0);
        
        return collection.toArray((T[]) java.lang.reflect.Array.newInstance(arrayType, collection.size()));
    }
    
    /**
     * Creates a list from varargs.
     * 
     * @param elements elements to include
     * @param <T> element type
     * @return new list containing elements
     */
    @SafeVarargs
    public static <T> List<T> listOf(T... elements) {
        if (elements == null) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(elements));
    }
    
    /**
     * Creates a set from varargs.
     * 
     * @param elements elements to include
     * @param <T> element type
     * @return new set containing elements
     */
    @SafeVarargs
    public static <T> Set<T> setOf(T... elements) {
        if (elements == null) return new HashSet<>();
        return new HashSet<>(Arrays.asList(elements));
    }
    
    /**
     * Creates a map from key-value pairs.
     * 
     * @param pairs alternating keys and values
     * @param <K> key type
     * @param <V> value type
     * @return new map containing pairs
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> mapOf(Object... pairs) {
        if (pairs == null || pairs.length == 0) return new HashMap<>();
        if (pairs.length % 2 != 0) throw new IllegalArgumentException("Pairs must have even number of elements");
        
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((K) pairs[i], (V) pairs[i + 1]);
        }
        return map;
    }
    
    /**
     * Reverses a list in place.
     * 
     * @param list list to reverse
     * @param <T> element type
     * @return the same list (reversed)
     */
    public static <T> List<T> reverse(List<T> list) {
        if (list != null) {
            Collections.reverse(list);
        }
        return list;
    }
    
    /**
     * Creates a reversed copy of a list.
     * 
     * @param list list to reverse
     * @param <T> element type
     * @return new reversed list
     */
    public static <T> List<T> reversed(List<T> list) {
        if (isEmpty(list)) return new ArrayList<>();
        
        List<T> result = new ArrayList<>(list);
        Collections.reverse(result);
        return result;
    }
    
    /**
     * Shuffles a list in place.
     * 
     * @param list list to shuffle
     * @param <T> element type
     * @return the same list (shuffled)
     */
    public static <T> List<T> shuffle(List<T> list) {
        if (list != null) {
            Collections.shuffle(list);
        }
        return list;
    }
    
    /**
     * Creates a shuffled copy of a list.
     * 
     * @param list list to shuffle
     * @param <T> element type
     * @return new shuffled list
     */
    public static <T> List<T> shuffled(List<T> list) {
        if (isEmpty(list)) return new ArrayList<>();
        
        List<T> result = new ArrayList<>(list);
        Collections.shuffle(result);
        return result;
    }
    
    /**
     * Sorts a list in place using natural ordering.
     * 
     * @param list list to sort
     * @param <T> element type (must be Comparable)
     * @return the same list (sorted)
     */
    public static <T extends Comparable<T>> List<T> sort(List<T> list) {
        if (list != null) {
            Collections.sort(list);
        }
        return list;
    }
    
    /**
     * Sorts a list in place using a comparator.
     * 
     * @param list list to sort
     * @param comparator comparator to use
     * @param <T> element type
     * @return the same list (sorted)
     */
    public static <T> List<T> sort(List<T> list, Comparator<T> comparator) {
        if (list != null && comparator != null) {
            list.sort(comparator);
        }
        return list;
    }
    
    /**
     * Creates a sorted copy of a list using natural ordering.
     * 
     * @param list list to sort
     * @param <T> element type (must be Comparable)
     * @return new sorted list
     */
    public static <T extends Comparable<T>> List<T> sorted(List<T> list) {
        if (isEmpty(list)) return new ArrayList<>();
        
        return list.stream()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Creates a sorted copy of a list using a comparator.
     * 
     * @param list list to sort
     * @param comparator comparator to use
     * @param <T> element type
     * @return new sorted list
     */
    public static <T> List<T> sorted(List<T> list, Comparator<T> comparator) {
        if (isEmpty(list) || comparator == null) return new ArrayList<>(defaultList(list));
        
        return list.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the intersection of two collections.
     * 
     * @param collection1 first collection
     * @param collection2 second collection
     * @param <T> element type
     * @return new list containing intersection
     */
    public static <T> List<T> intersection(Collection<T> collection1, Collection<T> collection2) {
        if (isEmpty(collection1) || isEmpty(collection2)) return new ArrayList<>();
        
        return collection1.stream()
                .filter(collection2::contains)
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the union of two collections.
     * 
     * @param collection1 first collection
     * @param collection2 second collection
     * @param <T> element type
     * @return new list containing union
     */
    public static <T> List<T> union(Collection<T> collection1, Collection<T> collection2) {
        Set<T> result = new LinkedHashSet<>();
        
        if (isNotEmpty(collection1)) {
            result.addAll(collection1);
        }
        if (isNotEmpty(collection2)) {
            result.addAll(collection2);
        }
        
        return new ArrayList<>(result);
    }
    
    /**
     * Gets the difference of two collections (elements in first but not in second).
     * 
     * @param collection1 first collection
     * @param collection2 second collection
     * @param <T> element type
     * @return new list containing difference
     */
    public static <T> List<T> difference(Collection<T> collection1, Collection<T> collection2) {
        if (isEmpty(collection1)) return new ArrayList<>();
        if (isEmpty(collection2)) return new ArrayList<>(collection1);
        
        return collection1.stream()
                .filter(item -> !collection2.contains(item))
                .collect(Collectors.toList());
    }
    
    /**
     * Checks if two collections contain the same elements (order doesn't matter).
     * 
     * @param collection1 first collection
     * @param collection2 second collection
     * @param <T> element type
     * @return true if collections contain same elements
     */
    public static <T> boolean isEqualContent(Collection<T> collection1, Collection<T> collection2) {
        if (collection1 == collection2) return true;
        if (collection1 == null || collection2 == null) return false;
        if (collection1.size() != collection2.size()) return false;
        
        return collection1.containsAll(collection2) && collection2.containsAll(collection1);
    }
    
    /**
     * Chunks a collection into sublists of specified size.
     * 
     * @param collection source collection
     * @param chunkSize size of each chunk
     * @param <T> element type
     * @return list of chunks
     */
    public static <T> List<List<T>> chunk(Collection<T> collection, int chunkSize) {
        if (isEmpty(collection) || chunkSize <= 0) return new ArrayList<>();
        
        List<T> list = new ArrayList<>(collection);
        List<List<T>> chunks = new ArrayList<>();
        
        for (int i = 0; i < list.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, list.size());
            chunks.add(new ArrayList<>(list.subList(i, end)));
        }
        
        return chunks;
    }
    
    /**
     * Takes the first n elements from a collection.
     * 
     * @param collection source collection
     * @param count number of elements to take
     * @param <T> element type
     * @return new list with first n elements
     */
    public static <T> List<T> take(Collection<T> collection, int count) {
        if (isEmpty(collection) || count <= 0) return new ArrayList<>();
        
        return collection.stream()
                .limit(count)
                .collect(Collectors.toList());
    }
    
    /**
     * Skips the first n elements and returns the rest.
     * 
     * @param collection source collection
     * @param count number of elements to skip
     * @param <T> element type
     * @return new list with remaining elements
     */
    public static <T> List<T> skip(Collection<T> collection, int count) {
        if (isEmpty(collection) || count <= 0) return new ArrayList<>(collection);
        
        return collection.stream()
                .skip(count)
                .collect(Collectors.toList());
    }
}