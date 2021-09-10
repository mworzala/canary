package com.mattworzala.canary.server.givemeahome;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StructureWriter {

    public static void writeStructure(Structure structure, Path filePath) {
        GsonBuilder gson = new GsonBuilder().setPrettyPrinting();

        // when registering type adapters for specific generic values
        // make sure that you register in the order of general -> specific

        Type blockMapType = new TypeToken<Map<Integer, Block>>() {
        }.getType();
        gson.registerTypeAdapter(blockMapType, new BlockMapSerializer());

        Type blockDefList = new TypeToken<List<Structure.BlockDef>>() {
        }.getType();
        gson.registerTypeAdapter(blockDefList, new BlockDefListSerializer());

        gson.registerTypeAdapter(Vec.class, new VecSerializer());

        var output = gson.create().toJson(structure);
        File file = filePath.toFile();

        try (FileOutputStream fos = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            byte[] bytes = output.getBytes();
            bos.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("JSON OUTPUT");
        System.out.println(output);
    }

    private static class VecSerializer implements JsonSerializer<Vec> {
        public JsonElement serialize(Vec vec, Type typeOfSRc, JsonSerializationContext context) {
            return context.serialize(new int[]{vec.blockX(), vec.blockY(), vec.blockZ()});
        }
    }

    private static class BlockDefListSerializer implements JsonSerializer<List<Structure.BlockDef>> {
        public JsonElement serialize(List<Structure.BlockDef> blockDefList, Type typeOfSRc, JsonSerializationContext context) {

            return new JsonPrimitive(blockDefList.stream()
                    .map((blockDef) -> blockDef.blockId() + "," + blockDef.blockCount())
                    .collect(Collectors.joining(";")));
        }
    }

    private static class BlockMapSerializer implements JsonSerializer<Map<Integer, Block>> {
        public JsonElement serialize(Map<Integer, Block> blockMap, Type typeOfSRc, JsonSerializationContext context) {
            JsonArray arr = new JsonArray();
            int index = 0;
            while (blockMap.containsKey(index)) {
                Block b = blockMap.get(index);
                arr.add(new BlockSerializer().serialize(b, Block.class, context));
                index++;
            }
            return arr;
        }
    }

    private static class BlockSerializer implements JsonSerializer<Block> {
        public JsonElement serialize(Block block, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.add("block", new JsonPrimitive(block.name()));
            if (block.handler() != null) {
                object.add("handler", new JsonPrimitive(block.handler().toString()));
            }
            if (block.nbt() != null) {
                object.add("data", new JsonPrimitive(block.nbt().toString()));
            }
            return object;
        }
    }
}
