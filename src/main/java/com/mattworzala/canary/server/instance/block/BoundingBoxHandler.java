package com.mattworzala.canary.server.instance.block;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class BoundingBoxHandler implements BlockHandler {
    private final NamespaceID ID = NamespaceID.from("canary:bounding_box");

    public static final Block BLOCK = Block.STRUCTURE_BLOCK
            .withTag(Tags.Author, "?")
            .withTag(Tags.IgnoreEntities, (byte) 0)
            .withTag(Tags.Integrity, 1f)
            .withTag(Tags.Metadata, "")
            .withTag(Tags.Mirror, "NONE")
            .withTag(Tags.Mode, "SAVE")
            .withTag(Tags.Name, "test123")
            .withTag(Tags.PosX, 0)
            .withTag(Tags.PosY, 1)
            .withTag(Tags.PosZ, 0)
            .withTag(Tags.Powered, (byte) 0)
            .withTag(Tags.Rotation, "NONE")
            .withTag(Tags.Seed, 0L)
            .withTag(Tags.ShowBoundingBox, (byte) 1)
            .withHandler(new BoundingBoxHandler());

    private BoundingBoxHandler() {
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return List.of(
                Tags.Author,
                Tags.IgnoreEntities,
                Tags.Integrity,
                Tags.Metadata,
                Tags.Mirror,
                Tags.Mode,
                Tags.Name,
                Tags.PosX, Tags.PosY, Tags.PosZ,
                Tags.Powered,
                Tags.Rotation,
                Tags.Seed,
                Tags.ShowBoundingBox,
                Tags.SizeX, Tags.SizeY, Tags.SizeZ
        );
    }

    @Override
    public byte getBlockEntityAction() {
        return 7;
    }

    @Override
    public @NotNull NamespaceID getNamespaceId() {
        return ID;
    }

    public static class Tags {
        public static final Tag<String> Author = Tag.String("author");
        public static final Tag<Byte> IgnoreEntities = Tag.Byte("ignoreEntities");
        public static final Tag<Float> Integrity = Tag.Float("integrity");
        public static final Tag<String> Metadata = Tag.String("metadata");
        public static final Tag<String> Mirror = Tag.String("mirror");
        public static final Tag<String> Mode = Tag.String("mode");
        public static final Tag<String> Name = Tag.String("name");
        public static final Tag<Integer> PosX = Tag.Integer("posX");
        public static final Tag<Integer> PosY = Tag.Integer("posY");
        public static final Tag<Integer> PosZ = Tag.Integer("posZ");
        public static final Tag<Byte> Powered = Tag.Byte("powered");
        public static final Tag<String> Rotation = Tag.String("rotation");
        public static final Tag<Long> Seed = Tag.Long("seed");
        public static final Tag<Byte> ShowBoundingBox = Tag.Byte("showboundingbox");
        public static final Tag<Integer> SizeX = Tag.Integer("sizeX");
        public static final Tag<Integer> SizeY = Tag.Integer("sizeY");
        public static final Tag<Integer> SizeZ = Tag.Integer("sizeZ");
    }
}
