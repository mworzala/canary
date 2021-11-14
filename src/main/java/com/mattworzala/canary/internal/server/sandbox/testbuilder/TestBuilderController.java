package com.mattworzala.canary.internal.server.sandbox.testbuilder;

import com.mattworzala.canary.internal.server.instance.block.BoundingBoxHandler;
import com.mattworzala.canary.internal.structure.JsonStructureIO;
import com.mattworzala.canary.internal.structure.Structure;
import com.mattworzala.canary.internal.structure.StructureWriter;
import com.mattworzala.canary.internal.util.testbuilder.BlockBoundingBox;
import com.mattworzala.canary.internal.util.ui.BlockClickingItemStack;
import com.mattworzala.canary.internal.util.ui.ItemBehavior;
import com.mattworzala.canary.internal.util.ui.Prompt;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.network.packet.server.play.BlockEntityDataPacket;
import net.minestom.server.world.DimensionType;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TestBuilderController {

    private static final int MAX_STRUCTURE_DIMENSION = 48;
    // put the structure block as low as possible (48 blocks down) without going into negative y
    private static final int MAX_STRUCTURE_BLOCK_OFFSET = 48;

    private BlockBoundingBox blockBoundingBox = new BlockBoundingBox(MAX_STRUCTURE_DIMENSION);

    private Point structureBlockPos;
    private Block lastOverwrittenBlock;

    private InstanceContainer testBuilderInstance;

    private List<Player> players = new ArrayList<>();
    private List<Instance> playersPreviousInstances = new ArrayList<>();
    private List<Point> playersPreviousInstancePos = new ArrayList<>();

    private Map<String, Point> markers = new HashMap<>();

    private static final EventNode<PlayerEvent> testBuilderPlayerEventNode = EventNode.type("test-builder-controller-player", EventFilter.PLAYER);

    static {
        MinecraftServer.getGlobalEventHandler().addChild(testBuilderPlayerEventNode);
    }

    private String name;

    public TestBuilderController(String name) {
        this.name = name;

        testBuilderInstance = new InstanceContainer(UUID.randomUUID(), DimensionType.OVERWORLD);
        MinecraftServer.getInstanceManager().registerInstance(testBuilderInstance);

        this.placeStartingPlatform();
    }

    public void reset() {
        Collection<Chunk> chunks = testBuilderInstance.getChunks();
        for (Chunk c : chunks) {
            c.reset();
        }

        blockBoundingBox = new BlockBoundingBox(MAX_STRUCTURE_DIMENSION);
        structureBlockPos = null;
        lastOverwrittenBlock = null;
    }

    private void placeStartingPlatform() {
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                var point = new Vec(x, 40, z);
                blockBoundingBox.addBlock(point);
                testBuilderInstance.setBlock(point, Block.STONE);
            }
        }

    }

    public void addMarker(Player player, Point point) {
        ItemStack leftItem = ItemStack.builder(Material.RED_STAINED_GLASS).displayName(Component.text("")).lore(Component.text("cancel")).build();
        ItemStack rightItem = ItemStack.builder(Material.GREEN_STAINED_GLASS).build();
        Prompt.AnvilPromptOption cancel = new Prompt.AnvilPromptOption(leftItem, s -> System.out.println("canceled"));
        Prompt.AnvilPromptOption confirm = new Prompt.AnvilPromptOption(rightItem, s -> markers.put(s, point));
        Prompt.anvilPrompt(player, "Marker Name", cancel, confirm);
    }

    public void addPlayer(Player player) {
        this.players.add(player);
        // if this is the first player, load the starting platform
        if (this.players.size() == 1) {
            placeStartingPlatform();
        }
        playersPreviousInstances.add(player.getInstance());
        playersPreviousInstancePos.add(player.getPosition());
        player.setInstance(testBuilderInstance, new Vec(0, 41, 0));

        // REFACTOR : Register events once in constructor in test builder controller
        // other things to track: player leaving
        testBuilderPlayerEventNode.addListener(EventListener.builder(PlayerBlockPlaceEvent.class)
                .filter(event -> event.getPlayer().equals(player))
                .handler(playerBlockPlaceEvent -> {
                    System.out.println("PLAYER BLOCK PLACE EVENT");
                    System.out.println("BLOCK POS: " + playerBlockPlaceEvent.getBlockPosition());
                    if (blockBoundingBox.addBlock(playerBlockPlaceEvent.getBlockPosition())) {
                        this.updateStructureOutline();
                    } else {
                        playerBlockPlaceEvent.setCancelled(true);
                    }
                }).build());

        testBuilderPlayerEventNode.addListener(EventListener.builder(PlayerBlockBreakEvent.class)
                .filter(event -> event.getPlayer().equals(player))
                .handler(playerBlockBreakEvent -> {
                    System.out.println("PLAYER BLOCK BREAK EVENT");
                    System.out.println("BLOCK POS: " + playerBlockBreakEvent.getBlockPosition());
                    blockBoundingBox.removeBlock(playerBlockBreakEvent.getBlockPosition());
                    this.updateStructureOutline();
                }).build());

        ItemBehavior markerItem = new MarkerItem(this);
        BlockClickingItemStack blockClicker = new BlockClickingItemStack(markerItem);
        blockClicker.giveToPlayer(player, player.getHeldSlot());

        updateStructureOutline();
    }

    public void importStructure(Structure structure) {
        for (int x = 0; x < 5; x++) {
            for (int z = 0; z < 5; z++) {
                var point = new Vec(x, 40, z);
//                blockBoundingBox.removeBlock(point);
                testBuilderInstance.setBlock(point, Block.AIR);
            }
        }
        this.blockBoundingBox = new BlockBoundingBox(MAX_STRUCTURE_DIMENSION);
        TestBuilderBlockSetter testBuilderBlockSetter = new TestBuilderBlockSetter(this.testBuilderInstance, this.blockBoundingBox);
        structure.loadIntoBlockSetter(testBuilderBlockSetter, new Vec(0, 40, 0));

        updateStructureOutline();
    }

    // REFACTOR : Prompt to save when leaving test builder
    // TODO - give code stub
    public void finish() {
        System.out.println("FINISHING BUILDING STRUCTURE: " + name);

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            Instance previousInstance = playersPreviousInstances.get(i);
            Point previousPos = playersPreviousInstancePos.get(i);
            player.setInstance(previousInstance, previousPos);
            players.remove(i);
            playersPreviousInstances.remove(i);
            playersPreviousInstancePos.remove(i);

        }

        Point minPoint = blockBoundingBox.getMinPoint();
        Structure structure = Structure.structureFromWorld(testBuilderInstance, name, minPoint, blockBoundingBox.getSize());

        for (String markerName : markers.keySet()) {
            structure.addMarker(markerName, markers.get(markerName).sub(minPoint));
        }

        // TODO - do this correctly using env variables
        Path root = FileSystems.getDefault().getPath("..").toAbsolutePath();
        Path filePath = Paths.get(root.toString(), "src", "main", "resources", name + ".json");
        StructureWriter structureWriter = new JsonStructureIO();
        structureWriter.writeStructure(structure, filePath);

        this.reset();
    }

    private void updateStructureOutline() {
        if (structureBlockPos == null) {
            recomputeStructureBlockPos();
        } else {
            Point minPoint = blockBoundingBox.getMinPoint();
            Point size = blockBoundingBox.getSize();

            Point structureBlockOffset = minPoint.sub(structureBlockPos);
            int x = structureBlockOffset.blockX();
            int y = structureBlockOffset.blockY();
            int z = structureBlockOffset.blockZ();

            if (x <= MAX_STRUCTURE_BLOCK_OFFSET &&
                    y <= MAX_STRUCTURE_BLOCK_OFFSET &&
                    z <= MAX_STRUCTURE_BLOCK_OFFSET) {
                // if the structure block doesn't need to move, we just update the size and offset
                Block boundingBox = boundingBoxBlockFromSizeAndPos(size, structureBlockOffset);
                BlockEntityDataPacket blockEntityDataPacket = new BlockEntityDataPacket();

                blockEntityDataPacket.blockPosition = structureBlockPos;
                blockEntityDataPacket.action = 7;
                blockEntityDataPacket.nbtCompound = boundingBox.nbt();

                sendPacketToPlayers(blockEntityDataPacket);
            } else {
                // the structure block does need to move
                recomputeStructureBlockPos();
            }

        }
    }

    /**
     * Fully calculates the position of the structure block,
     * removes the current structure block if it exists, and sends update to player
     */
    private void recomputeStructureBlockPos() {
        Point minPoint = blockBoundingBox.getMinPoint();
        Point size = blockBoundingBox.getSize();

        int structureBlockYPos = minPoint.blockY() <= MAX_STRUCTURE_BLOCK_OFFSET ? 0 : minPoint.blockY() - MAX_STRUCTURE_BLOCK_OFFSET;
        int structureBlockYOffset = minPoint.blockY() - structureBlockYPos;

        Block boundingBox = boundingBoxBlockFromSizeAndYPos(size, structureBlockYOffset);
        BlockEntityDataPacket blockEntityDataPacket = new BlockEntityDataPacket();

        Point blockPos = minPoint.add(new Vec(0, -structureBlockYOffset, 0));
        blockEntityDataPacket.blockPosition = blockPos;
        blockEntityDataPacket.action = 7;
        blockEntityDataPacket.nbtCompound = boundingBox.nbt();

        if (lastOverwrittenBlock != null) {
            testBuilderInstance.setBlock(structureBlockPos, lastOverwrittenBlock);
        }
        lastOverwrittenBlock = testBuilderInstance.getBlock(blockPos);
        structureBlockPos = blockPos;
        testBuilderInstance.setBlock(blockPos, boundingBox);

        sendPacketToPlayers(blockEntityDataPacket);
    }

    private Block boundingBoxBlockFromSizeAndPos(Point size, Point pos) {
        return BoundingBoxHandler.BLOCK
                .withTag(BoundingBoxHandler.Tags.SizeX, size.blockX())
                .withTag(BoundingBoxHandler.Tags.SizeY, size.blockY())
                .withTag(BoundingBoxHandler.Tags.SizeZ, size.blockZ())
                .withTag(BoundingBoxHandler.Tags.PosX, pos.blockX())
                .withTag(BoundingBoxHandler.Tags.PosY, pos.blockY())
                .withTag(BoundingBoxHandler.Tags.PosZ, pos.blockZ());
    }

    private Block boundingBoxBlockFromSizeAndYPos(Point size, int yPos) {
        return boundingBoxBlockFromSizeAndPos(size, new Vec(0, yPos, 0));
    }

    private void sendPacketToPlayers(ServerPacket packet) {
        for (Player p : players) {
            p.sendPacket(packet);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public boolean hasPlayer(Player p) {
        for (Player player : this.players) {
            if (player.getUuid().equals(p.getUuid())) {
                return true;
            }
        }
        return false;
    }

    public Instance getTestBuilderInstance() {
        return testBuilderInstance;
    }

    public void unregister() {
        MinecraftServer.getInstanceManager().unregisterInstance(testBuilderInstance);
    }
}
