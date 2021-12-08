package com.mattworzala.canary.internal.util.ui.itembehavior;

import net.minestom.server.item.ItemStack;

public class ItemBehaviorBuilder {
    private String baseCommand;
    private ItemStack itemStack;
    private ClickBehaviorBuilder leftClickBuilder;
    private ClickBehaviorBuilder rightClickBuilder;


    public ItemBehaviorBuilder(String baseCommand) {
        this.baseCommand = baseCommand;
    }

    public ItemBehaviorBuilder setItemStack(ItemStack stack) {
        itemStack = stack;
        return this;
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
        if (itemStack != null) {
            return new GenericItemBehavior(baseCommand, buildClick(leftClickBuilder), buildClick(rightClickBuilder), itemStack);
        } else {
            return new GenericItemBehavior(baseCommand, buildClick(leftClickBuilder), buildClick(rightClickBuilder));
        }
    }

    private CommandClick buildClick(ClickBehaviorBuilder clickBehaviorBuilder) {
        if (clickBehaviorBuilder != null) {
            return clickBehaviorBuilder.buildClickBehavior();
        }
        return null;
    }

}
