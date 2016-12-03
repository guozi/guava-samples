import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

/**
 * @author chenyun
 * @date 2016-11-20 13:37
 * @since 1.0.0
 */
public class GuavaFilterTransformCollectionsTest {

    /****filter方法过滤符合条件的项****/

    @Test
    public void whenFilterWithIterables_thenFiltered() {
        List<String> names = Lists.newArrayList("John", "Jane", "Adam", "Tom");
        Iterable<String> result =
                Iterables.filter(names, Predicates.containsPattern("a"));

        assertThat(result, containsInAnyOrder("Jane", "Adam"));
    }

    @Test
    public void whenFilterWithCollections2_thenFiltered() {
        List<String> names = Lists.newArrayList("John", "Jane", "Adam", "Tom");
        //!!!the output of Collections.filter() is a live view of the original collection
        // changes to one will be reflected in the other
        Collection<String> result = Collections2.filter(names, Predicates.containsPattern("a"));
        assertEquals(2, result.size());
        assertThat(result, containsInAnyOrder("Jane", "Adam"));

        result.add("anna");
        assertEquals(5, names.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenFilteredCollection_whenAddingInvalidElement_thenException() {
        List<String> names = Lists.newArrayList("John", "Jane", "Adam", "Tom");
        //!!!the result is constrained by the predicate – if we add an element that doesn’t satisfy that Predicate,
        // an IllegalArgumentException will be thrown
        Collection<String> result = Collections2.filter(names, Predicates.containsPattern("a"));

        result.add("Jim");
    }

    @Test
    public void whenFilterCollectionWithCustomPredicate_thenFiltered() {
        //自己组装过滤条件
        Predicate<String> predicate = new Predicate<String>() {
            public boolean apply(String input) {
                return input.startsWith("A") || input.startsWith("J");
            }
        };
        List<String> names = Lists.newArrayList("John", "Jane", "Adam", "Tom");
        Collection<String> result = Collections2.filter(names, predicate);

        assertEquals(3, result.size());
        assertThat(result, containsInAnyOrder("John", "Jane", "Tom"));
    }

    @Test
    public void whenFilterUsingMultiplePredicates_thenFiltered() {
        List<String> names = Lists.newArrayList("John", "Jane", "Adam", "Tom");
        //过滤字符串包含"j"或者不包含"a"
        Collection<String> result = Collections2.filter(names,
                Predicates.or(Predicates.containsPattern("J"),
                        Predicates.not(Predicates.containsPattern("a"))));

        assertEquals(3, result.size());
        assertThat(result, containsInAnyOrder("John", "Jane", "Tom"));
    }

    @Test
    public void whenRemoveNullFromCollection_thenRemoved() {
        List<String> names =
                Lists.newArrayList("John", null, "Jane", null, "Adam", "Tom");
        //过滤空元素
        Collection<String> result = Collections2.filter(names, Predicates.<String>notNull());

        assertEquals(4, result.size());
        assertThat(result, containsInAnyOrder("John", "Jane", "Adam", "Tom"));
    }

    /**
     * 判断迭代器中的元素是否都满足某个条件 all 方法
     */
    @Test
    public void whenCheckingIfAllElementsMatchACondition_thenCorrect() {
        List<String> names = Lists.newArrayList("John", "Jane", "Adam", "Tom");

        boolean result = Iterables.all(names, Predicates.containsPattern("n|m"));
        assertTrue(result);

        result = Iterables.all(names, Predicates.containsPattern("a"));
        assertFalse(result);

        /*通过any判断迭代器中是否有一个满足条件的记录*/
        result = Iterables.any(names, Predicates.containsPattern("a"));
        assertTrue(result);
    }

    /**
     * get方法获得迭代器中的第x个元素
     */
    @Test
    public void getAnElementFromCollection() {
        List<String> names = Lists.newArrayList("John", "Jane", "Adam", "Tom");
        String result = Iterables.get(names, 2);

        assertTrue("Adam".equals(result));
    }

    /**
     * find方法返回符合条件的第一个元素
     */
    @Test
    public void findFirstElementFromCollection_thenFiltered() {
        List<String> names = Lists.newArrayList("John", "Jane", "Adam", "Tom");
        String result = Iterables.find(names, new Predicate<String>() {
            public boolean apply(String input) {
                return input.length() == 3;
            }
        });

        assertTrue("Tom".equals(result));
    }

    /********collection transform*********/
    @Test
    public void whenTransformWithIterables_thenTransformed() {
        Function<String, Integer> function = new Function<String, Integer>() {
            public Integer apply(String input) {
                return input.length();
            }
        };
        List<String> names = Lists.newArrayList("John", "Jane", "Adam", "Tom");
        Iterable<Integer> result = Iterables.transform(names, function);
        assertThat(result, contains(4, 4, 4, 3));
    }

    @Test
    public void whenTransformWithCollections2_thenTransformed() {
        Function<String, Integer> function = new Function<String, Integer>() {
            public Integer apply(String input) {
                return input.length();
            }
        };
        List<String> names = Lists.newArrayList("John", "Jane", "Adam", "Tom");
        //the output of Collections.transform() is a live view of the original Collection
        // changes to one affect the other
        Collection<Integer> result = Collections2.transform(names, function);
        assertEquals(4, result.size());
        assertThat(result, contains(4, 4, 4, 3));

        result.remove(3);
        assertEquals(3, names.size());
    }

    @Test
    public void whenCreatingAFunctionFromAPredicate_thenCorrect() {
        List<String> names = Lists.newArrayList("John", "Jane", "Adam", "Tom");
        Collection<Boolean> result =
                Collections2.transform(names, Functions.forPredicate(Predicates.containsPattern("m")));
        assertEquals(4, result.size());
        assertThat(result, contains(false, false, true, true));
    }

    @Test
    public void whenTransformingUsingComposedFunction_thenTransformed() {
        Function<String, Integer> f1 = new Function<String, Integer>() {
            public Integer apply(String input) {
                return input.length();
            }
        };

        Function<Integer, Boolean> f2 = new Function<Integer, Boolean>() {
            public Boolean apply(Integer input) {
                return input % 2 == 0;
            }
        };

        List<String> names = Lists.newArrayList("John", "Jane", "Adam", "Tom");
        Collection<Boolean> result = Collections2.transform(names, Functions.compose(f2, f1));
        assertEquals(4, result.size());
        assertThat(result, contains(true, true, true, false));
    }

    @Test
    public void whenFilteringAndTransformingCollection_thenCorrect() {
        Predicate<String> predicate = new Predicate<String>() {
            public boolean apply(String input) {
                return input.startsWith("A") || input.startsWith("T");
            }
        };
        Function<String, Integer> function = new Function<String, Integer>() {
            public Integer apply(String input) {
                return input.length();
            }
        };

        List<String> names = Lists.newArrayList("John", "Jane", "Adam", "Tom");
        Collection<Integer> result =
                FluentIterable.from(names)
                        .filter(predicate)
                        .transform(function)
                        .toList();
        assertEquals(2, result.size());
        assertThat(result, containsInAnyOrder(4, 3));
    }
}
