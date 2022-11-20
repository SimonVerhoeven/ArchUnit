package com.tngtech.archunit.testutil.syntax;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.tngtech.archunit.lang.ArchRule;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.stream.Collectors.toList;

public class MethodChoiceStrategy {
    private final Random random = new Random();

    private final Predicate<Method> ignorePredicate;

    private MethodChoiceStrategy() {
        this((__) -> false);
    }

    private MethodChoiceStrategy(Predicate<Method> ignorePredicate) {
        this.ignorePredicate = ignorePredicate;
    }

    public static MethodChoiceStrategy chooseAllArchUnitSyntaxMethods() {
        return new MethodChoiceStrategy();
    }

    public MethodChoiceStrategy exceptMethodsWithName(String string) {
        return new MethodChoiceStrategy(ignorePredicate.or(methodWithName(string)));
    }

    private Predicate<Method> methodWithName(String methodName) {
        return input -> input.getName().equals(methodName);
    }

    Optional<Method> choose(PropagatedType type, boolean tryToTerminate) {
        List<Method> methods = getPossibleMethodCandidates(type.getRawType());
        if (methods.isEmpty()) {
            return Optional.empty();
        }

        return tryToTerminate
                ? tryToChooseTerminationMethod(methods)
                : Optional.of(methods.get(random.nextInt(methods.size())));
    }

    private Optional<Method> tryToChooseTerminationMethod(List<Method> methods) {
        Optional<Method> terminationMethod = findMethodWithReturnType(methods, ArchRule.class);
        return terminationMethod.isPresent() ? terminationMethod : Optional.of(methods.iterator().next());
    }

    private Optional<Method> findMethodWithReturnType(List<Method> methods, Class<ArchRule> returnType) {
        for (Method method : methods) {
            if (returnType.isAssignableFrom(method.getReturnType())) {
                return Optional.of(method);
            }
        }
        return Optional.empty();
    }

    private List<Method> getPossibleMethodCandidates(Class<?> clazz) {
        return getInvocableMethods(clazz).stream().filter(this::isCandidate).collect(toList());
    }

    private Collection<Method> getInvocableMethods(Class<?> clazz) {
        Map<MethodKey, Method> result = new HashMap<>();
        for (Method method : clazz.getMethods()) {
            MethodKey key = MethodKey.of(method);
            Method invocableCandidate = result.containsKey(key)
                    ? resolveMoreSpecificMethod(method, result.get(key))
                    : method;
            result.put(key, invocableCandidate);
        }
        return result.values();
    }

    private Method resolveMoreSpecificMethod(Method first, Method second) {
        if (first.getDeclaringClass() != second.getDeclaringClass()) {
            return second.getDeclaringClass().isAssignableFrom(first.getDeclaringClass()) ? first : second;
        } else {
            return second.getReturnType().isAssignableFrom(first.getReturnType()) ? first : second;
        }
    }

    private boolean isCandidate(Method method) {
        return belongsToArchUnit(method) && isNoArchRuleMethod(method) && !ignorePredicate.test(method);
    }

    private boolean belongsToArchUnit(Method method) {
        return method.getDeclaringClass().getName().contains(".archunit.") && methodDoesNotBelongTo(Object.class, method);
    }

    private boolean isNoArchRuleMethod(Method method) {
        return methodDoesNotBelongTo(ArchRule.class, method);
    }

    private boolean methodDoesNotBelongTo(Class<?> type, Method method) {
        try {
            type.getMethod(method.getName(), method.getParameterTypes());
            return false;
        } catch (NoSuchMethodException e) {
            return true;
        }
    }

    private static class MethodKey {
        private final String name;
        private final List<Class<?>> parameterTypes;

        private MethodKey(Method method) {
            name = method.getName();
            parameterTypes = ImmutableList.copyOf(method.getParameterTypes());
        }

        static MethodKey of(Method method) {
            return new MethodKey(method);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, parameterTypes);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            MethodKey other = (MethodKey) obj;
            return Objects.equals(this.name, other.name)
                    && Objects.equals(this.parameterTypes, other.parameterTypes);
        }

        @Override
        public String toString() {
            return toStringHelper(this)
                    .add("name", name)
                    .add("parameterTypes", parameterTypes)
                    .toString();
        }
    }
}
