package com.mattworzala.canary.junit.descriptor;

import com.mattworzala.canary.junit.runner.Filter;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class OrFilter extends Filter {
    private final Collection<? extends Filter> filters;

    public OrFilter(Collection<? extends Filter> filters) {
        assert !filters.isEmpty();
        this.filters = filters;
    }

    @Override
    public boolean shouldRun(TestDescription description) {
        return filters.stream().anyMatch(filter -> filter.shouldRun(description));
    }

    @Override
    public String describe() {
        return filters.stream().map(Filter::describe).collect(Collectors.joining(" OR "));
    }
}
