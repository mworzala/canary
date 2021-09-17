package com.mattworzala.canary.server.instance;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.pointer.Pointer;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.data.Data;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.ExperienceOrb;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.pathfinding.PFInstanceSpace;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockGetter;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.chunk.ChunkSupplier;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OffsetInstanceDelegate extends InstanceContainer {
    private final InstanceContainer delegate;
    private final Point offset;

    public OffsetInstanceDelegate(InstanceContainer delegate, Point offset) {
        super(delegate.getUniqueId(), delegate.getDimensionType(), delegate.getChunkLoader());

        this.delegate = delegate;
        this.offset = offset;
    }

    public InstanceContainer getDelegate() {
        return delegate;
    }

    public @NotNull Point absoluteToRelative(Point absolute) {
        return absolute.sub(offset);
    }

    public @NotNull Point relativeToAbsolute(Point relative) {
        return relative.add(offset);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        setBlock(new Vec(x, y, z), block);
    }

    @Override
    public boolean placeBlock(BlockHandler.@NotNull Placement placement) {
        BlockHandler.Placement absolutePlacement;
        if (placement instanceof BlockHandler.PlayerPlacement playerPlacement) {
            absolutePlacement = new BlockHandler.PlayerPlacement(
                    placement.getBlock(),
                    placement.getInstance(),
                    relativeToAbsolute(placement.getBlockPosition()),
                    playerPlacement.getPlayer(),
                    playerPlacement.getHand(),
                    playerPlacement.getBlockFace(),
                    playerPlacement.getCursorX(),
                    playerPlacement.getCursorY(),
                    playerPlacement.getCursorZ()
            );
        } else {
            absolutePlacement = new BlockHandler.Placement(
                    placement.getBlock(),
                    placement.getInstance(),
                    relativeToAbsolute(placement.getBlockPosition())
            );
        }
        return delegate.placeBlock(absolutePlacement);
    }

    @Override
    public boolean breakBlock(@NotNull Player player, @NotNull Point blockPosition) {
        return delegate.breakBlock(player, relativeToAbsolute(blockPosition));
    }

    @Override
    public @NotNull CompletableFuture<Chunk> loadChunk(int chunkX, int chunkZ) {
        //todo how to handle chunk loading?
        throw new RuntimeException("Direct chunk loading in test instances is not allowed!");
//        return delegate.loadChunk(chunkX, chunkZ);
    }

    @Override
    public @NotNull CompletableFuture<Chunk> loadOptionalChunk(int chunkX, int chunkZ) {
        throw new RuntimeException("Direct chunk loading in test instances is not allowed!");
//        return delegate.loadOptionalChunk(chunkX, chunkZ);
    }

    @Override
    public void unloadChunk(@NotNull Chunk chunk) {
        throw new RuntimeException("Direct chunk unloading in test instances is not allowed!");
//        delegate.unloadChunk(chunk);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        throw new RuntimeException("Direct chunk access in test instances is not allowed!");
//        return delegate.getChunk(chunkX, chunkZ);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveInstance() {
        throw new RuntimeException("Saving in test instances is not allowed!");
//        return delegate.saveInstance();
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunkToStorage(@NotNull Chunk chunk) {
        throw new RuntimeException("Saving in test instances is not allowed!");
//        return delegate.saveChunkToStorage(chunk);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveChunksToStorage() {
        throw new RuntimeException("Saving in test instances is not allowed!");
//        return delegate.saveChunksToStorage();
    }

    @Override
    public void enableAutoChunkLoad(boolean enable) {
        throw new RuntimeException("Direct chunk loading in test instances is not allowed!");
//        delegate.enableAutoChunkLoad(enable);
    }

    @Override
    public boolean hasEnabledAutoChunkLoad() {
        return false;
//        return delegate.hasEnabledAutoChunkLoad();
    }

    @Override
    public boolean isInVoid(@NotNull Point point) {
        return delegate.isInVoid(relativeToAbsolute(point));
    }

    @Override
    public void setChunkSupplier(@NotNull ChunkSupplier chunkSupplier) {
        throw new RuntimeException("Direct chunk manipulation in test instances is not allowed!");
//        delegate.setChunkSupplier(chunkSupplier);
    }

    @Override
    public ChunkSupplier getChunkSupplier() {
        throw new RuntimeException("Direct chunk manipulation in test instances is not allowed!");
//        return delegate.getChunkSupplier();
    }

    @Override
    public List<SharedInstance> getSharedInstances() {
        return delegate.getSharedInstances();
    }

    @Override
    public boolean hasSharedInstances() {
        return delegate.hasSharedInstances();
    }

    @Override
    public InstanceContainer copy() {
        throw new RuntimeException("Copying in test instances is not allowed!");
//        return delegate.copy();
    }

    @Override
    @Nullable
    public InstanceContainer getSrcInstance() {
        return delegate;
    }

    @Override
    public long getLastBlockChangeTime() {
        return delegate.getLastBlockChangeTime();
    }

    @Override
    public void refreshLastBlockChangeTime() {
        delegate.refreshLastBlockChangeTime();
    }

    @Override
    public ChunkGenerator getChunkGenerator() {
        throw new RuntimeException("Direct chunk manipulation in test instances is not allowed!");
//        return delegate.getChunkGenerator();
    }

    @Override
    public void setChunkGenerator(ChunkGenerator chunkGenerator) {
        throw new RuntimeException("Direct chunk manipulation in test instances is not allowed!");
//        delegate.setChunkGenerator(chunkGenerator);
    }

    @Override
    public @NotNull Collection<@NotNull Chunk> getChunks() {
        throw new RuntimeException("Direct chunk manipulation in test instances is not allowed!");
//        return delegate.getChunks();
    }

    @Override
    public IChunkLoader getChunkLoader() {
        throw new RuntimeException("Direct chunk manipulation in test instances is not allowed!");
//        return delegate.getChunkLoader();
    }

    @Override
    public void setChunkLoader(IChunkLoader chunkLoader) {
        throw new RuntimeException("Direct chunk manipulation in test instances is not allowed!");
//        delegate.setChunkLoader(chunkLoader);
    }

    @Override
    public void tick(long time) {
        delegate.tick(time);
    }

    @Override
    public void scheduleNextTick(@NotNull Consumer<Instance> callback) {
        delegate.scheduleNextTick(callback);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Chunk> loadChunk(@NotNull Point point) {
        throw new RuntimeException("Direct chunk manipulation in test instances is not allowed!");
//        return delegate.loadChunk(point);
    }

    @Override
    public @NotNull CompletableFuture<@Nullable Chunk> loadOptionalChunk(@NotNull Point point) {
        throw new RuntimeException("Direct chunk manipulation in test instances is not allowed!");
//        return delegate.loadOptionalChunk(point);
    }

    @Override
    public void unloadChunk(int chunkX, int chunkZ) {
        throw new RuntimeException("Direct chunk manipulation in test instances is not allowed!");
//        delegate.unloadChunk(chunkX, chunkZ);
    }

    @Override
    public boolean isRegistered() {
        return delegate.isRegistered();
    }

    @Override
    public DimensionType getDimensionType() {
        return delegate.getDimensionType();
    }

    @Override
    public long getWorldAge() {
        return delegate.getWorldAge();
    }

    @Override
    public long getTime() {
        return delegate.getTime();
    }

    @Override
    public void setTime(long time) {
        delegate.setTime(time);
    }

    @Override
    public int getTimeRate() {
        return delegate.getTimeRate();
    }

    @Override
    public void setTimeRate(int timeRate) {
        delegate.setTimeRate(timeRate);
    }

    @Override
    public @Nullable Duration getTimeUpdate() {
        return delegate.getTimeUpdate();
    }

    @Override
    public void setTimeUpdate(@Nullable Duration timeUpdate) {
        delegate.setTimeUpdate(timeUpdate);
    }

    @Override
    @NotNull
    public WorldBorder getWorldBorder() {
        return delegate.getWorldBorder();
    }

    @Override
    public @NotNull Set<@NotNull Entity> getEntities() {
        return delegate.getEntities();
    }

    @Override
    public @NotNull Set<@NotNull Player> getPlayers() {
        return delegate.getPlayers();
    }

    @Override
    public @NotNull Set<@NotNull EntityCreature> getCreatures() {
        return delegate.getCreatures();
    }

    @Override
    public @NotNull Set<@NotNull ExperienceOrb> getExperienceOrbs() {
        return delegate.getExperienceOrbs();
    }

    @Override
    public @NotNull Set<@NotNull Entity> getChunkEntities(Chunk chunk) {
        throw new RuntimeException("Direct chunk manipulation in test instances is not allowed!");
//        return delegate.getChunkEntities(chunk);
    }

    @Override
    public @NotNull Collection<Entity> getNearbyEntities(@NotNull Point point, double range) {
        return delegate.getNearbyEntities(relativeToAbsolute(point), range);
    }

    @Override
    public @Nullable Block getBlock(int x, int y, int z, BlockGetter.@NotNull Condition condition) {
        return getBlock(new Vec(x, y, z), condition);
    }

    @Override
    public void sendBlockAction(@NotNull Point blockPosition, byte actionId, byte actionParam) {
        delegate.sendBlockAction(relativeToAbsolute(blockPosition), actionId, actionParam);
    }

    @Override
    @Nullable
    public Chunk getChunkAt(double x, double z) {
        throw new RuntimeException("Direct chunk manipulation in test instances is not allowed!");
//        return delegate.getChunkAt(x, z);
    }

    @Override
    @Nullable
    public Chunk getChunkAt(@NotNull Point point) {
        throw new RuntimeException("Direct chunk manipulation in test instances is not allowed!");
//        return delegate.getChunkAt(point);
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return delegate.getUniqueId();
    }

    @Override
    @ApiStatus.Internal
    public void UNSAFE_addEntity(@NotNull Entity entity) {
        delegate.UNSAFE_addEntity(entity);
    }

    @Override
    @ApiStatus.Internal
    public void UNSAFE_removeEntity(@NotNull Entity entity) {
        delegate.UNSAFE_removeEntity(entity);
    }

    @Override
    @ApiStatus.Internal
    public void UNSAFE_switchEntityChunk(@NotNull Entity entity, @NotNull Chunk lastChunk, @NotNull Chunk newChunk) {
        delegate.UNSAFE_switchEntityChunk(entity, lastChunk, newChunk);
    }

    @Override
    public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
        return delegate.getTag(tag);
    }

    @Override
    public <T> void setTag(@NotNull Tag<T> tag, @Nullable T value) {
        delegate.setTag(tag, value);
    }

    @Override
    public void explode(float centerX, float centerY, float centerZ, float strength) {
        Point absolutePos = relativeToAbsolute(new Vec(centerX, centerY, centerZ));
        delegate.explode((float) absolutePos.x(), (float) absolutePos.y(), (float) absolutePos.z(), strength);
    }

    @Override
    public void explode(float centerX, float centerY, float centerZ, float strength, @Nullable Data additionalData) {
        Point absolutePos = relativeToAbsolute(new Vec(centerX, centerY, centerZ));
        delegate.explode((float) absolutePos.x(), (float) absolutePos.y(), (float) absolutePos.z(), strength, additionalData);
    }

    @Override
    @Nullable
    public ExplosionSupplier getExplosionSupplier() {
        return delegate.getExplosionSupplier();
    }

    @Override
    public void setExplosionSupplier(@Nullable ExplosionSupplier supplier) {
        delegate.setExplosionSupplier(supplier);
    }

    @Override
    @ApiStatus.Internal
    public @NotNull PFInstanceSpace getInstanceSpace() {
        return delegate.getInstanceSpace();
    }

    @Override
    public @NotNull Pointers pointers() {
        return delegate.pointers();
    }

    @Override
    public @Nullable Block getBlock(@NotNull Point point, BlockGetter.@NotNull Condition condition) {
        return delegate.getBlock(relativeToAbsolute(point), condition);
    }

    @Override
    public @NotNull Block getBlock(int x, int y, int z) {
        return getBlock(new Vec(x, y, z));
    }

    @Override
    public @NotNull Block getBlock(@NotNull Point point) {
        return delegate.getBlock(relativeToAbsolute(point));
    }

    @Override
    public void setBlock(@NotNull Point blockPosition, @NotNull Block block) {
        delegate.setBlock(relativeToAbsolute(blockPosition), block);
    }

    @Override
    public boolean hasTag(@NotNull Tag<?> tag) {
        return delegate.hasTag(tag);
    }

    @Override
    public void removeTag(@NotNull Tag<?> tag) {
        delegate.removeTag(tag);
    }

    @Override
    public void sendGroupedPacket(ServerPacket packet) {
        delegate.sendGroupedPacket(packet);
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        delegate.sendMessage(source, message, type);
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        delegate.sendActionBar(message);
    }

    @Override
    public void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {
        delegate.sendPlayerListHeaderAndFooter(header, footer);
    }

    @Override
    public void showTitle(@NotNull Title title) {
        delegate.showTitle(title);
    }

    @Override
    public void clearTitle() {
        delegate.clearTitle();
    }

    @Override
    public void resetTitle() {
        delegate.resetTitle();
    }

    @Override
    public void showBossBar(@NotNull BossBar bar) {
        delegate.showBossBar(bar);
    }

    @Override
    public void hideBossBar(@NotNull BossBar bar) {
        delegate.hideBossBar(bar);
    }

    @Override
    public void playSound(@NotNull Sound sound, double x, double y, double z) {
        Point absolutePos = relativeToAbsolute(new Vec(x, y, z));
        delegate.playSound(sound, absolutePos.x(), absolutePos.y(), absolutePos.z());
    }

    @Override
    public void playSound(@NotNull Sound sound, Sound.@NotNull Emitter emitter) {
        delegate.playSound(sound, emitter);
    }

    @Override
    public void stopSound(@NotNull SoundStop stop) {
        delegate.stopSound(stop);
    }

    @Override
    @NotNull
    public Iterable<? extends Audience> audiences() {
        return delegate.audiences();
    }

    @Override
    public @NotNull <T> Optional<T> get(@NotNull Pointer<T> pointer) {
        return delegate.get(pointer);
    }

    @Override
    @Contract("_, null -> null; _, !null -> !null")
    public <T> @Nullable T getOrDefault(@NotNull Pointer<T> pointer, @Nullable T defaultValue) {
        return delegate.getOrDefault(pointer, defaultValue);
    }

    @Override
    public <T> @UnknownNullability T getOrDefaultFrom(@NotNull Pointer<T> pointer, @NotNull Supplier<? extends T> defaultValue) {
        return delegate.getOrDefaultFrom(pointer, defaultValue);
    }

    @Override
    public void sendMessage(@NotNull Identified source, @NotNull Component message, @NotNull MessageType type) {
        delegate.sendMessage(source, message, type);
    }

    @Override
    public void sendPlayerListHeader(@NotNull Component header) {
        delegate.sendPlayerListHeader(header);
    }

    @Override
    public void sendPlayerListFooter(@NotNull Component footer) {
        delegate.sendPlayerListFooter(footer);
    }

    @Override
    public void playSound(@NotNull Sound sound) {
        delegate.playSound(sound);
    }

    @Override
    public void openBook(@NotNull Book book) {
        delegate.openBook(book);
    }

    @Override
    public void sendMessage(@NotNull ComponentLike message) {
        delegate.sendMessage(message);
    }

    @Override
    public void sendMessage(@NotNull Identified source, @NotNull ComponentLike message) {
        delegate.sendMessage(source, message);
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull ComponentLike message) {
        delegate.sendMessage(source, message);
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        delegate.sendMessage(message);
    }

    @Override
    public void sendMessage(@NotNull Identified source, @NotNull Component message) {
        delegate.sendMessage(source, message);
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message) {
        delegate.sendMessage(source, message);
    }

    @Override
    public void sendMessage(@NotNull ComponentLike message, @NotNull MessageType type) {
        delegate.sendMessage(message, type);
    }

    @Override
    public void sendMessage(@NotNull Identified source, @NotNull ComponentLike message, @NotNull MessageType type) {
        delegate.sendMessage(source, message, type);
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull ComponentLike message, @NotNull MessageType type) {
        delegate.sendMessage(source, message, type);
    }

    @Override
    public void sendMessage(@NotNull Component message, @NotNull MessageType type) {
        delegate.sendMessage(message, type);
    }

    @Override
    public void sendActionBar(@NotNull ComponentLike message) {
        delegate.sendActionBar(message);
    }

    @Override
    public void sendPlayerListHeader(@NotNull ComponentLike header) {
        delegate.sendPlayerListHeader(header);
    }

    @Override
    public void sendPlayerListFooter(@NotNull ComponentLike footer) {
        delegate.sendPlayerListFooter(footer);
    }

    @Override
    public void sendPlayerListHeaderAndFooter(@NotNull ComponentLike header, @NotNull ComponentLike footer) {
        delegate.sendPlayerListHeaderAndFooter(header, footer);
    }

    @Override
    public void stopSound(@NotNull Sound sound) {
        delegate.stopSound(sound);
    }

    @Override
    public void openBook(Book.@NotNull Builder book) {
        delegate.openBook(book);
    }
}
