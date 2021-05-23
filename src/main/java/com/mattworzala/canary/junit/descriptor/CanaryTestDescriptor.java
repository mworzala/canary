package com.mattworzala.canary.junit.descriptor;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

import java.util.*;
import java.util.function.Predicate;

import static org.junit.platform.commons.util.StringUtils.isNotBlank;

public class CanaryTestDescriptor extends AbstractTestDescriptor {
    public static final String ENGINE_ID = "canary";
    public static final String SEGMENT_TYPE_RUNNER = "runner";
    public static final String SEGMENT_TYPE_TEST = "test";
    public static final String SEGMENT_TYPE_DYNAMIC = "dynamic";

    protected TestDescription description;

    public CanaryTestDescriptor(UniqueId uniqueId, TestDescription description, TestSource source) {
        this(uniqueId, description, TestDescription.generateName(description), source);
    }

    public CanaryTestDescriptor(UniqueId uniqueId, TestDescription description, String name, TestSource source) {
        super(uniqueId, name, source);
        this.description = description;
    }

    public TestDescription getDescription() {
        return description;
    }

    @Override
    public String getLegacyReportingName() {
        String methodName = TestDescription.getMethodName(description);
        if (methodName == null) {
            String className = description.getClassName();
            if (isNotBlank(className)) {
                return className;
            }
        }
        return super.getLegacyReportingName();
    }

    @Override
    public Type getType() {
        return description.isTest() ? Type.TEST : Type.CONTAINER;
    }

    @Override
    public Set<TestTag> getTags() {
        Set<TestTag> tags = new LinkedHashSet<>();
        addTagsFromParent(tags);
        return tags;
    }

    @Override
    public void removeFromHierarchy() {
        if (canBeRemovedFromHierarchy()) {
            super.removeFromHierarchy();
        }
    }

    protected boolean canBeRemovedFromHierarchy() {
        return tryToExcludeFromRunner(this.description);
    }

    protected boolean tryToExcludeFromRunner(TestDescription description) {
        return getParent().map(CanaryTestDescriptor.class::cast)
                .map(parent -> parent.tryToExcludeFromRunner(description))
                .orElse(false);
    }

    void pruneDescriptorsForObsoleteDescriptions(List<TestDescription> newSiblingDescriptions) {
        Optional<TestDescription> newDescription = newSiblingDescriptions.stream().filter(Predicate.isEqual(description)).findAny();
        if (newDescription.isPresent()) {
            List<TestDescription> newChildren = new ArrayList<>(newDescription.get().getChildren());
            new ArrayList<>(children).stream().map(CanaryTestDescriptor.class::cast).forEach(
                    childDescriptor -> childDescriptor.pruneDescriptorsForObsoleteDescriptions(newChildren));
        }
        else {
            super.removeFromHierarchy();
        }
    }

    private void addTagsFromParent(Set<TestTag> tags) {
        getParent().map(TestDescriptor::getTags).ifPresent(tags::addAll);
    }
}
