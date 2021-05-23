package com.mattworzala.canary.junit.support;

import com.mattworzala.canary.junit.descriptor.TestDescription;
import org.junit.platform.commons.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.function.Function;

//todo yeet this
public class UniqueIdReader implements Function<TestDescription, Serializable> {

    private static final Logger logger = LoggerFactory.getLogger(UniqueIdReader.class);

    private final String fieldName;

    public UniqueIdReader() {
        this("uniqueId");
    }

    // For tests only
    UniqueIdReader(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public Serializable apply(TestDescription description) {
        ReflectionUtils.tryToReadFieldValue(TestDescription.class, fieldName, description)
                .andThenTry(Serializable.class::cast)
                .ifFailure(cause -> logger.warn(cause, () ->
                        String.format("Could not read unique ID for Description; using display name instead: %s", description)))
                .toOptional()
                .orElseGet(description::getName);
    }

}
