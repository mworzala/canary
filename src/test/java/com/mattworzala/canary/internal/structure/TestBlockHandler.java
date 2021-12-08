package com.mattworzala.canary.internal.structure;

import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

public class TestBlockHandler implements BlockHandler {


    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return NamespaceID.from("example:my_block_handler");
    }
}
