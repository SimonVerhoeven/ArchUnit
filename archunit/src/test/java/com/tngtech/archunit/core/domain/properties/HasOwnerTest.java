package com.tngtech.archunit.core.domain.properties;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.properties.HasOwner.Functions.Get;
import com.tngtech.archunit.core.domain.properties.HasOwner.Predicates.With;
import org.junit.Test;

import static com.tngtech.archunit.testutil.Assertions.assertThat;

public class HasOwnerTest {
    @Test
    public void predicate_with_owner() {
        HasOwner<String> hasOwner = hasOwner("owner");

        assertThat(With.owner(startsWith("o"))).accepts(hasOwner);
        assertThat(With.owner(startsWith("w"))).rejects(hasOwner);
        assertThat(With.owner(startsWith("foo"))).hasDescription("owner starts with foo");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void function_get_owner() {
        HasOwner<String> hasOwner = hasOwner("owner");

        assertThat(Get.<String>owner().apply(hasOwner)).isEqualTo("owner");
    }

    private DescribedPredicate<String> startsWith(String prefix) {
        return new DescribedPredicate<String>("starts with " + prefix) {
            @Override
            public boolean test(String input) {
                return input.startsWith(prefix);
            }
        };
    }

    private <T> HasOwner<T> hasOwner(T owner) {
        return () -> owner;
    }
}
