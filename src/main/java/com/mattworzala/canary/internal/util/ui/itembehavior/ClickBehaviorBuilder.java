package com.mattworzala.canary.internal.util.ui.itembehavior;

import com.mattworzala.canary.internal.util.ui.itembehavior.argument.Argument;

import java.util.ArrayList;
import java.util.List;

public class ClickBehaviorBuilder {
    private String baseCommand;
    private ItemBehaviorBuilder parent;

    private List<Argument> args = new ArrayList<>();

    public ClickBehaviorBuilder(String baseCommand, ItemBehaviorBuilder parent) {
        this.baseCommand = baseCommand;
        this.parent = parent;
    }

    public ClickBehaviorBuilder arg(Argument argument) {
        args.add(argument);
        return this;
    }

    public ClickBehaviorBuilder onLeftClick(String command) {
        return parent.onLeftClick(command);
    }

    public ClickBehaviorBuilder onRightClick(String command) {
        return parent.onRightClick(command);
    }

    public ItemBehavior build() {
        return parent.build();
    }

    public CommandClick buildClickBehavior() {
        return new CommandClick(baseCommand, args);
    }

}
