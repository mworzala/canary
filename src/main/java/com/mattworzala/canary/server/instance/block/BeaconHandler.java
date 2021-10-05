package com.mattworzala.canary.server.instance.block;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class BeaconHandler implements BlockHandler {
    private static final NamespaceID ID = NamespaceID.from("canary:beacon");

    public static final Block BLOCK = Block.BEACON
            .withTag(Tags.Levels, 1)
            .withTag(Tags.Primary, -1)
            .withTag(Tags.Secondary, -1)
            .withHandler(new BeaconHandler());

    private BeaconHandler() {}

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(Tags.Levels, Tags.Primary, Tags.Secondary);
    }

    @Override
    public byte getBlockEntityAction() {
        return 3;
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return ID;
    }

    private static class Tags {
        public static final Tag<Integer> Levels = Tag.Integer("Levels");
        public static final Tag<Integer> Primary = Tag.Integer("Primary");
        public static final Tag<Integer> Secondary = Tag.Integer("Secondary");
    }
}
