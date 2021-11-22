package com.mattworzala.canary.internal.util.ui.itembehavior;

public class ItemBehaviorBuilder {
    private String baseCommand;
    private ClickBehaviorBuilder leftClickBuilder;
    private ClickBehaviorBuilder rightClickBuilder;


    public ItemBehaviorBuilder(String baseCommand) {
        this.baseCommand = baseCommand;
    }

    public ClickBehaviorBuilder onLeftClick(String command) {
        leftClickBuilder = new ClickBehaviorBuilder(command, this);
        return leftClickBuilder;
    }

    public ClickBehaviorBuilder onRightClick(String command) {
        rightClickBuilder = new ClickBehaviorBuilder(command, this);
        return rightClickBuilder;
    }

    public ItemBehavior build() {
        return new GenericItemBehavior(baseCommand, buildClick(leftClickBuilder), buildClick(rightClickBuilder));
    }

    private CommandClick buildClick(ClickBehaviorBuilder clickBehaviorBuilder) {
        if (clickBehaviorBuilder != null) {
            return clickBehaviorBuilder.buildClickBehavior();
        }
        return null;
    }

}
