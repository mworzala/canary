package com.mattworzala.canary.internal.structure;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StructureIOTest {
    @TempDir
    static Path tmpDir;
    static Path tempStructureFile;


    static final String basicStructureJson = """
            {
                "id": "my-test-world",
                "size": [
                    2,
                    2,
                    2
                ],
                "markers": {
                    "m1": 1,
                    "m2": 10,
                    "m3": 20
                },
                "blockmap": [
                    "minecraft:stone",
                    "minecraft:cobblestone_stairs[facing=north]",
                    {
                        "block": "minecraft:stone_stairs[facing=south,waterlogged=true]",
                        "handler": "example:my_block_handler",
                        "data": "{name1:123,name2:\\"sometext1\\",name3:{subname1:456,subname2:\\"sometext2\\"}}"
                    }
                ],
                "blocks": "0,4;1,1;0,2;-1,1"
            }
            """;

    @BeforeAll
    public static void init() throws IOException {
        tempStructureFile = Files.createFile(tmpDir.resolve("structure.json"));
        Files.writeString(tempStructureFile, basicStructureJson);
    }

    @BeforeEach
    public void minestomInit() {
        // the json structure parser needs minestom to parse the block info
        MinecraftServer.init();
        MinecraftServer.getBlockManager().registerHandler("example:my_block_handler", TestBlockHandler::new);
    }

    @Test
    public void testBasicJsonReadSize() {
        JsonStructureIO jsonStructureIO = new JsonStructureIO();
        Structure structure = jsonStructureIO.readStructure(tempStructureFile);
        assertEquals(2, structure.getSizeX());
        assertEquals(2, structure.getSizeY());
        assertEquals(2, structure.getSizeZ());
    }

    @Test
    public void testBasicJsonReadId() {
        JsonStructureIO jsonStructureIO = new JsonStructureIO();
        Structure structure = jsonStructureIO.readStructure(tempStructureFile);
        assertEquals("my-test-world", structure.getId());
    }

    @Test
    public void testBasicJsonReadMarkers() {
        JsonStructureIO jsonStructureIO = new JsonStructureIO();
        Structure structure = jsonStructureIO.readStructure(tempStructureFile);
        Map<String, Integer> markers = structure.getMarkers();
        assertNotNull(markers);
        assertEquals(1, markers.get("m1"));
        assertEquals(10, markers.get("m2"));
        assertEquals(20, markers.get("m3"));
    }

    @Test
    public void testBasicJsonReaderBlockMap() throws IOException, NBTException {
        JsonStructureIO jsonStructureIO = new JsonStructureIO();
        Structure structure = jsonStructureIO.readStructure(tempStructureFile);
        Map<Integer, Block> blocks = structure.blockmap;
        for (Integer i : blocks.keySet()) {
            System.out.println(i + ": " + blocks.get(i).toString());
        }
        assertEquals("minecraft:air", blocks.get(-1).name());
        assertEquals("minecraft:stone", blocks.get(0).name());
        assertEquals("minecraft:cobblestone_stairs", blocks.get(1).name());
        assertEquals("north", blocks.get(1).properties().get("facing"));

        Block stairs = blocks.get(2);
        assertEquals("minecraft:stone_stairs", stairs.name());
        assertNotNull(stairs.handler());
        assertEquals("example:my_block_handler", stairs.handler().getNamespaceId().asString());

        assertEquals("south", stairs.properties().get("facing"));
        assertEquals("true", stairs.properties().get("waterlogged"));

        // "data": "{name1:123,name2:\\"sometext1\\",name3:{subname1:456,subname2:\\"sometext2\\"}}"
        assertEquals(123, stairs.getTag(Tag.Integer("name1")));
        assertEquals("sometext1", stairs.getTag(Tag.String("name2")));
        // TODO - figure out how to test subname1 and subname2
    }


    @Test
    public void testBasicJsonReadBlocks() {
        JsonStructureIO jsonStructureIO = new JsonStructureIO();
        Structure structure = jsonStructureIO.readStructure(tempStructureFile);
        // "blocks": "0,4;1,1;0,2;-1,1"
        List<Structure.BlockDef> blocks = structure.blocks;
        assertEquals(new Structure.BlockDef(0, 4), blocks.get(0));
        assertEquals(new Structure.BlockDef(1, 1), blocks.get(1));
        assertEquals(new Structure.BlockDef(0, 2), blocks.get(2));
        assertEquals(new Structure.BlockDef(-1, 1), blocks.get(3));
    }
}
