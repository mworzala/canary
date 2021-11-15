package com.mattworzala.canary.internal.assertion;

import com.mattworzala.canary.internal.assertion.node.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class AeSimpleParser {
    /** Contains the steps originally provided to the parser */
    private final List<AssertionStep> steps;

    /** Contains the last parsed node. Represents the LHS for binary expressions for example and ultimately contains the root. */
    private AeNode lastNode;

    public AeSimpleParser(List<AssertionStep> steps) {
        this.steps = new ArrayList<>(steps); // Defensive copy
    }

    public AeNode parse() {
        Iterator<AssertionStep> iter = steps.iterator();

        while (iter.hasNext()) {
            lastNode = parseNode(iter.next(), iter);
        }

        return lastNode;
    }

    @NotNull
    private AeNode parseNode(@NotNull AssertionStep step, @NotNull Iterator<AssertionStep> stepIter) {
        return switch (step.type()) {
            case CONDITION -> parseCondition(step);

            case AND -> parseAnd(step, stepIter);

            case NOT -> parseNot(step, stepIter);

            case ALWAYS -> parseAlways(step, stepIter);

            case THEN -> parseThen(step, stepIter);
        };
    }

    @NotNull
    private AeNode parseCondition(@NotNull AssertionStep step) {
        assert step.type() == AssertionStep.Type.CONDITION;
        assert step.condition() != null;
        return new AeConditionNode(
                Objects.requireNonNull(step.debugName()),
                Objects.requireNonNull(step.supplier()),
                Objects.requireNonNull(step.condition())
        );
    }

    @NotNull
    private AeNode parseAnd(@NotNull AssertionStep step, @NotNull Iterator<AssertionStep> stepIter) {
        assert step.type() == AssertionStep.Type.AND;
        return new AeAndNode(parseBinaryOperator(stepIter));
    }

    @NotNull
    private AeNode parseNot(@NotNull AssertionStep step, @NotNull Iterator<AssertionStep> stepIter) {
        assert step.type() == AssertionStep.Type.NOT;
        return new AeNotNode(parseUnaryOperator(stepIter));
    }

    @NotNull
    private AeNode parseThen(@NotNull AssertionStep step, @NotNull Iterator<AssertionStep> stepIter) {
        assert step.type() == AssertionStep.Type.THEN;
        return new AeThenNode(parseBinaryOperator(stepIter));
    }

    @NotNull
    private AeNode parseAlways(@NotNull AssertionStep step, @NotNull Iterator<AssertionStep> stepIter) {
        assert step.type() == AssertionStep.Type.ALWAYS;
        return new AeAlwaysNode(parseUnaryOperator(stepIter));
    }

    @NotNull
    private List<AeNode> parseBinaryOperator(@NotNull Iterator<AssertionStep> stepIter) {
        // Left side (already was parsed since it was before this one)
        AeNode lhs = lastNode;
        if (lhs == null) {
            throw new RuntimeException("A binary node must be surrounded by two other nodes (missing left side)");
        }

        if (!stepIter.hasNext()) {
            throw new RuntimeException("A binary node must be surrounded by two other nodes (missing right side)");
        }
        AeNode rhs = parseNode(stepIter.next(), stepIter);

        return List.of(lhs, rhs);
    }

    private List<AeNode> parseUnaryOperator(Iterator<AssertionStep> stepIter) {
        AeNode item = parseNode(stepIter.next(), stepIter);
        //todo not validated that next item is a condition. This should be validated prior
        if (item == null) {
            throw new RuntimeException("A unary node must be followed by a condition node.");
        }
        return List.of(item);
    }
}
