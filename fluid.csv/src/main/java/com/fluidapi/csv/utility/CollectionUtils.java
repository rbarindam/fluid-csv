package com.fluidapi.csv.utility;

import static com.fluidapi.csv.utility.StreamUtils.fastStream;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntPredicate;

/**
 * All sorts of {@link Collection} or {@code Array} related methods that are not
 * found in existing API's like {@code Apache Commons Lang3}
 * 
 * @author Arindam Biswas
 * @since 0.1
 */
public interface CollectionUtils {
	
	/**
	 * Checks if any item in the {@code int[]} contains an {@code int} that matches
	 * with the condition given in {@link IntPredicate}
	 * 
	 * @param ints      the array of {@code int} numbers
	 * @param condition the int check
	 * @return if any {@code int} in the array matches the given condition
	 */
	static boolean contains(int[] ints, IntPredicate condition) {
		requireNonNull(ints, "ints");
		requireNonNull(condition, "condition");
		return fastStream(ints).anyMatch(condition);
	}
	
	/**
	 * checks the presence of null elements
	 * 
	 * @param <T> any object type
	 * @param elements objects in dynamic parameter or as a single entry of array
	 * @return whether the given array is empty or has any null element
	 */
	@SafeVarargs
	static <T> boolean hasNull(T...elements) {
		return isEmpty(elements) || stream(elements).anyMatch(Objects::isNull);
	}
	
	@SafeVarargs
	static <E> Set<E> asSet(E...elements) {
		return stream(elements).collect(toSet());
	}
	
	static Set<Character> asSet(char...characters) {
		Set<Character> set = new HashSet<>(characters.length, 1f);
		for (char character : characters) {
			set.add(character);
		}
		
		return set;
	}
	
}
