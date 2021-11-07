package com.mattworzala.canary.structure;

import com.mattworzala.canary.server.structure.JsonStructureIO;
import com.mattworzala.canary.server.structure.Structure;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StructureTests {
    @TempDir
    static Path tmpDir;
    static Path tempStructureFile;


    static final String basicStructureJson = "{\"id\": \"my-test-world\",\n" +
            "    \"size\": [\n" +
            "        16,\n" +
            "        16,\n" +
            "        16\n" +
            "    ],\n" +
            "    \"blockmap\": [\n" +
            "        \"minecraft:stone\",\n" +
            "        \"minecraft:cobblestone_stairs[facing=north]\",\n" +
            "        {\n" +
            "            \"block\": \"minecraft:stone_stairs[facing=south,waterlogged=true]\",\n" +
            "            \"handler\": \"example:my_block_handler\",\n" +
            "            \"data\": \"{name1:123,name2:\\\"sometext1\\\",name3:{subname1:456,subname2:\\\"sometext2\\\"}}\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"blocks\": \"0,256;1,16;0,240;-1,3584\"}";

    @BeforeAll
    public static void init() throws IOException {
        tempStructureFile = Files.createFile(tmpDir.resolve("structure.json"));
        Files.writeString(tempStructureFile, basicStructureJson);
    }

    @Test
    public void testBasicJsonReadSize() {
        JsonStructureIO jsonStructureIO = new JsonStructureIO();
        Structure structure = jsonStructureIO.readStructure(tempStructureFile);
        assertEquals(16, structure.getSizeX());
        assertEquals(16, structure.getSizeY());
        assertEquals(16, structure.getSizeZ());
    }

    @Test
    public void testBasicJsonReadId() {
        JsonStructureIO jsonStructureIO = new JsonStructureIO();
        Structure structure = jsonStructureIO.readStructure(tempStructureFile);
        assertEquals("my-test-world", structure.getId());
    }

}
