package com.mattworzala.canary.junit.descriptor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.engine.UniqueId;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.platform.commons.util.StringUtils.isNotBlank;

public class TestDescription {
    private static final Pattern METHOD_AND_CLASS_NAME_PATTERN = Pattern
            .compile("([\\s\\S]*)\\((.*)\\)");

    private final Collection<TestDescription> children = new ConcurrentLinkedQueue<>();
    private final Serializable id; //todo should be junit uniqueid
    private final String name;
    private final Annotation[] annotations;
    private volatile /* write once */ Class<?> testClass;

    private TestDescription(@NotNull String name, @Nullable Class<?> clazz, Annotation... annotations) {
        this(name, name, clazz, annotations);
    }

    private TestDescription(@NotNull Serializable id, @NotNull String name, @Nullable Class<?> testClass, Annotation... annotations) {
        this.id = id;
        this.name = name;
        this.testClass = testClass;
        this.annotations = annotations;
    }

    @NotNull
    public Serializable getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Collection<TestDescription> getChildren() {
        return children;
    }

    public void addChild(@NotNull TestDescription description) {
        children.add(description);
    }

    @Nullable
    public Class<?> getTestClass() {
        if (testClass != null)
            return testClass;
        String name = getClassName();
        if (name == null)
            return null;
        try {
            testClass = Class.forName(name, false, getClass().getClassLoader());
            return testClass;
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    @NotNull
    public String getClassName() {
        return testClass != null ? testClass.getName() : methodAndClassNamePatternGroupOrDefault(2, toString());
    }

    @NotNull
    public String getMethodName() {
        return methodAndClassNamePatternGroupOrDefault(1, null);
    }

    @NotNull
    public Collection<Annotation> getAnnotations() {
        return Arrays.asList(annotations);
    }

    @Nullable
    public <T extends Annotation> T getAnnotation(Class<T> type) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(type)) {
                return type.cast(annotation);
            }
        }
        return null;
    }

    public boolean isSuite() {
        return !children.isEmpty();
    }

    public boolean isTest() {
        return children.isEmpty();
    }

    public int testCount() {
        if (isTest()) return 1;
        int total = 0;
        for (TestDescription child : children)
            total += child.testCount();
        return total;
    }

    public TestDescription copyNoChildren() {
        return new TestDescription(name, testClass, annotations);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestDescription)) return false;
        TestDescription that = (TestDescription) o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    private String methodAndClassNamePatternGroupOrDefault(int group, String defaultString) {
        Matcher matcher = METHOD_AND_CLASS_NAME_PATTERN.matcher(toString());
        return matcher.matches() ? matcher.group(group) : defaultString;
    }

    public static String getMethodName(TestDescription description) {
        String displayName = description.getName();
        int i = displayName.indexOf('(');
        if (i >= 0) {
            int j = displayName.lastIndexOf('(');
            if (i == j) {
                char lastChar = displayName.charAt(displayName.length() - 1);
                if (lastChar == ')') {
                    return displayName.substring(0, i);
                }
            }
        }
        return description.getMethodName();
    }

    public static String generateName(TestDescription description) {
        String methodName = getMethodName(description);
        return isNotBlank(methodName) ? methodName : description.getName();
    }
}
