package com.mattworzala.canary.junit.execution;

import com.mattworzala.canary.junit.descriptor.CanaryTestDescriptor;
import com.mattworzala.canary.junit.descriptor.RunnerTestDescriptor;
import com.mattworzala.canary.junit.descriptor.TestDescription;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.opentest4j.MultipleFailuresError;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TestRun {

    private final RunnerTestDescriptor runnerTestDescriptor;
    private final Set<TestDescriptor> runnerDescendants;
    private final Map<TestDescription, CanaryDescriptors> descriptionToDescriptors;
    private final Map<TestDescriptor, List<TestExecutionResult>> executionResults = new LinkedHashMap<>();
    private final Set<TestDescriptor> skippedDescriptors = new LinkedHashSet<>();
    private final Set<TestDescriptor> startedDescriptors = new HashSet<>();
    private final Map<TestDescriptor, EventType> inProgressDescriptors = new LinkedHashMap<>();
    private final Set<TestDescriptor> finishedDescriptors = new LinkedHashSet<>();
    private final ThreadLocal<Deque<CanaryTestDescriptor>> inProgressDescriptorsByStartingThread = ThreadLocal.withInitial(
            ArrayDeque::new);

    TestRun(RunnerTestDescriptor runnerTestDescriptor) {
        this.runnerTestDescriptor = runnerTestDescriptor;
        runnerDescendants = new LinkedHashSet<>(runnerTestDescriptor.getDescendants());
        descriptionToDescriptors = Stream.concat(Stream.of(runnerTestDescriptor), runnerDescendants.stream())
                .map(CanaryTestDescriptor.class::cast)
                .collect(Collectors.toMap(CanaryTestDescriptor::getDescription, CanaryDescriptors::new, CanaryDescriptors::merge, HashMap::new));
    }

    void registerDynamicTest(CanaryTestDescriptor testDescriptor) {
        descriptionToDescriptors.computeIfAbsent(testDescriptor.getDescription(), __ -> new CanaryDescriptors()).add(
                testDescriptor);
        runnerDescendants.add(testDescriptor);
    }

    RunnerTestDescriptor getRunnerTestDescriptor() {
        return runnerTestDescriptor;
    }

    Collection<TestDescriptor> getInProgressTestDescriptorsWithSyntheticStartEvents() {
        List<TestDescriptor> result = inProgressDescriptors.entrySet().stream() //
                .filter(entry -> entry.getValue().equals(EventType.SYNTHETIC)) //
                .map(Map.Entry::getKey) //
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(result);
        return result;
    }

    boolean isDescendantOfRunnerTestDescriptor(TestDescriptor testDescriptor) {
        return runnerDescendants.contains(testDescriptor);
    }

    Optional<CanaryTestDescriptor> lookupNextTestDescriptor(TestDescription description) {
        return lookupUnambiguouslyOrApplyFallback(description, CanaryDescriptors::getNextUnstarted);
    }

    Optional<CanaryTestDescriptor> lookupCurrentTestDescriptor(TestDescription description) {
        return lookupUnambiguouslyOrApplyFallback(description, __ -> {
            CanaryTestDescriptor lastStarted = inProgressDescriptorsByStartingThread.get().peekLast();
            if (lastStarted != null && description.equals(lastStarted.getDescription())) {
                return Optional.of(lastStarted);
            }
            return Optional.empty();
        });
    }

    private Optional<CanaryTestDescriptor> lookupUnambiguouslyOrApplyFallback(TestDescription description,
                                                                               Function<CanaryDescriptors, Optional<CanaryTestDescriptor>> fallback) {
        CanaryDescriptors canaryDescriptors = descriptionToDescriptors.getOrDefault(description,
                CanaryDescriptors.NONE);
        Optional<CanaryTestDescriptor> result = canaryDescriptors.getUnambiguously(description);
        if (!result.isPresent()) {
            result = fallback.apply(canaryDescriptors);
        }
        return result;
    }

    void markSkipped(TestDescriptor testDescriptor) {
        skippedDescriptors.add(testDescriptor);
        if (testDescriptor instanceof CanaryTestDescriptor) {
            CanaryTestDescriptor vintageDescriptor = (CanaryTestDescriptor) testDescriptor;
            descriptionToDescriptors.get(vintageDescriptor.getDescription()).incrementSkippedOrStarted();
        }
    }

    boolean isNotSkipped(TestDescriptor testDescriptor) {
        return !isSkipped(testDescriptor);
    }

    boolean isSkipped(TestDescriptor testDescriptor) {
        return skippedDescriptors.contains(testDescriptor);
    }

    void markStarted(TestDescriptor testDescriptor, EventType eventType) {
        inProgressDescriptors.put(testDescriptor, eventType);
        startedDescriptors.add(testDescriptor);
        if (testDescriptor instanceof CanaryTestDescriptor) {
            CanaryTestDescriptor vintageDescriptor = (CanaryTestDescriptor) testDescriptor;
            inProgressDescriptorsByStartingThread.get().addLast(vintageDescriptor);
            descriptionToDescriptors.get(vintageDescriptor.getDescription()).incrementSkippedOrStarted();
        }
    }

    boolean isNotStarted(TestDescriptor testDescriptor) {
        return !startedDescriptors.contains(testDescriptor);
    }

    void markFinished(TestDescriptor testDescriptor) {
        inProgressDescriptors.remove(testDescriptor);
        finishedDescriptors.add(testDescriptor);
        if (testDescriptor instanceof CanaryTestDescriptor) {
            CanaryTestDescriptor descriptor = (CanaryTestDescriptor) testDescriptor;
            inProgressDescriptorsByStartingThread.get().removeLastOccurrence(descriptor);
        }
    }

    boolean isNotFinished(TestDescriptor testDescriptor) {
        return !isFinished(testDescriptor);
    }

    boolean isFinished(TestDescriptor testDescriptor) {
        return finishedDescriptors.contains(testDescriptor);
    }

    boolean areAllFinishedOrSkipped(Set<? extends TestDescriptor> testDescriptors) {
        return testDescriptors.stream().allMatch(this::isFinishedOrSkipped);
    }

    boolean isFinishedOrSkipped(TestDescriptor testDescriptor) {
        return isFinished(testDescriptor) || isSkipped(testDescriptor);
    }

    void storeResult(TestDescriptor testDescriptor, TestExecutionResult result) {
        List<TestExecutionResult> testExecutionResults = executionResults.computeIfAbsent(testDescriptor,
                key -> new ArrayList<>());
        testExecutionResults.add(result);
    }

    TestExecutionResult getStoredResultOrSuccessful(TestDescriptor testDescriptor) {
        List<TestExecutionResult> testExecutionResults = executionResults.get(testDescriptor);

        if (testExecutionResults == null) {
            return TestExecutionResult.successful();
        }
        if (testExecutionResults.size() == 1) {
            return testExecutionResults.get(0);
        }
        List<Throwable> failures = testExecutionResults
                .stream()
                .map(TestExecutionResult::getThrowable)
                .map(Optional::get)
                .collect(Collectors.toList());
        return TestExecutionResult.failed(new MultipleFailuresError("", failures));
    }

    private static class CanaryDescriptors {

        private static final CanaryDescriptors NONE = new CanaryDescriptors(Collections.emptyList());

        private final List<CanaryTestDescriptor> descriptors;
        private int skippedOrStartedCount;

        static CanaryDescriptors merge(CanaryDescriptors a, CanaryDescriptors b) {
            List<CanaryTestDescriptor> mergedDescriptors = new ArrayList<>(
                    a.descriptors.size() + b.descriptors.size());
            mergedDescriptors.addAll(a.descriptors);
            mergedDescriptors.addAll(b.descriptors);
            return new CanaryDescriptors(mergedDescriptors);
        }

        CanaryDescriptors(CanaryTestDescriptor vintageTestDescriptor) {
            this();
            add(vintageTestDescriptor);
        }

        CanaryDescriptors() {
            this(new ArrayList<>(1));
        }

        CanaryDescriptors(List<CanaryTestDescriptor> descriptors) {
            this.descriptors = descriptors;
        }

        void add(CanaryTestDescriptor descriptor) {
            descriptors.add(descriptor);
        }

        Optional<CanaryTestDescriptor> getUnambiguously(TestDescription description) {
            if (descriptors.isEmpty()) {
                return Optional.empty();
            }
            if (descriptors.size() == 1) {
                return Optional.of(descriptors.get(0));
            }
            // @formatter:off
            return descriptors.stream()
                    .filter(testDescriptor -> description == testDescriptor.getDescription())
                    .findFirst();
            // @formatter:on
        }

        public void incrementSkippedOrStarted() {
            skippedOrStartedCount++;
        }

        public Optional<CanaryTestDescriptor> getNextUnstarted() {
            if (skippedOrStartedCount < descriptors.size()) {
                return Optional.of(descriptors.get(skippedOrStartedCount));
            }
            return Optional.empty();
        }

    }

}
