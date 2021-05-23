package com.mattworzala.canary.junit.discovery;

import com.mattworzala.canary.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.junit.descriptor.RunnerTestDescriptor;
import com.mattworzala.canary.junit.descriptor.TestDescription;
import com.mattworzala.canary.junit.descriptor.TestSourceProvider;
import com.mattworzala.canary.junit.support.UniqueIdReader;
import com.mattworzala.canary.junit.support.UniqueIdStringifier;
import org.junit.platform.engine.UniqueId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;

public class TestDescriptorPostProcessor {
    private final UniqueIdReader uniqueIdReader = new UniqueIdReader();
    private final UniqueIdStringifier uniqueIdStringifier = new UniqueIdStringifier();
    private final TestSourceProvider sourceProvider = new TestSourceProvider();

    public void applyFiltersAndCreateDescendants(RunnerTestDescriptor runnerDescriptor) {
        addChildrenRecursive(runnerDescriptor);
        runnerDescriptor.applyFilters(this::addChildrenRecursive);
    }

    private void addChildrenRecursive(CanaryTestDescriptor parent) {
        if (parent.getDescription().isTest())
            return;

        List<TestDescription> children = parent.getDescription().getChildren();
        // Use LinkedHashMap to preserve order, ArrayList for fast access by index
        Map<String, List<TestDescription>> childrenByUniqueId = children.stream().collect(
                groupingBy(uniqueIdReader.andThen(uniqueIdStringifier), LinkedHashMap::new, toCollection(ArrayList::new)));
        for (Map.Entry<String, List<TestDescription>> entry : childrenByUniqueId.entrySet()) {
            String uniqueId = entry.getKey();
            List<TestDescription> childrenWithSameUniqueId = entry.getValue();
            IntFunction<String> uniqueIdGenerator = determineUniqueIdGenerator(uniqueId, childrenWithSameUniqueId);
            for (int index = 0; index < childrenWithSameUniqueId.size(); index++) {
                String reallyUniqueId = uniqueIdGenerator.apply(index);
                TestDescription description = childrenWithSameUniqueId.get(index);
                UniqueId id = parent.getUniqueId().append(VintageTestDescriptor.SEGMENT_TYPE_TEST, reallyUniqueId);
                CanaryTestDescriptor child = new CanaryTestDescriptor(id, description,
                        sourceProvider.findTestSource(description));
                parent.addChild(child);
                addChildrenRecursive(child);
            }
        }
    }

    //todo pretty sure UniqueId has a hierarchy which could be used here instead?
    private IntFunction<String> determineUniqueIdGenerator(String uniqueId, List<TestDescription> childrenWithSameUniqueId) {
        if (childrenWithSameUniqueId.size() == 1)
            return index -> uniqueId;
        return index -> uniqueId + "[" + index + "]";
    }
}
