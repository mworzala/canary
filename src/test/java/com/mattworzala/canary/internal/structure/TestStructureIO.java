package com.mattworzala.canary.internal.structure;

import com.mattworzala.canary.internal.structure.JsonStructureIO;
import com.mattworzala.canary.internal.structure.Structure;
import net.minestom.server.MinecraftServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestStructureIO {
    @TempDir
    static Path tmpDir;
    static Path tempStructureFile;


    static final String basicStructureJson = """
            {
                "id": "my-test-world",
                "size": [
                    16,
                    16,
                    16
                ],
                "blockmap": [
                    "minecraft:stone",
                    "minecraft:cobblestone_stairs[facing=north]",
                    {
                        "block": "minecraft:stone_stairs[facing=south,waterlogged=true]",
                        "handler": "example:my_block_handler",
                        "data": "{name1:123,name2:\\"sometext1\\",name3:{subname1:456,subname2:\\"sometext2\\"}}"
                    }
                ],
                "blocks": "0,256;1,16;0,240;-1,3584"
            }
            """;

    @BeforeAll
    public static void init() throws IOException {
        tempStructureFile = Files.createFile(tmpDir.resolve("structure.json"));
        Files.writeString(tempStructureFile, basicStructureJson);
    }

    @Test
    public void testBasicJsonReadSize() {
        MinecraftServer.init();
        JsonStructureIO jsonStructureIO = new JsonStructureIO();
        Structure structure = jsonStructureIO.readStructure(tempStructureFile);
        assertEquals(16, structure.getSizeX());
        assertEquals(16, structure.getSizeY());
        assertEquals(16, structure.getSizeZ());
    }

    @Test
    public void testBasicJsonReadId() {
        MinecraftServer.init();
        JsonStructureIO jsonStructureIO = new JsonStructureIO();
        Structure structure = jsonStructureIO.readStructure(tempStructureFile);
        assertEquals("my-test-world", structure.getId());
    }

}
