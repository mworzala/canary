package com.mattworzala.canary.internal.structure;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentBlockState;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.parser.SNBTParser;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonStructureIO implements StructureWriter, StructureReader {

    private final Gson gson;

    public JsonStructureIO() {
        GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();

        // when registering type adapters for specific generic values
        // make sure that you register in the order of general -> specific

        Type blockMapType = new TypeToken<Map<Integer, Block>>() {
        }.getType();
        gsonBuilder.registerTypeAdapter(blockMapType, new BlockMapSerializer());

        Type blockDefList = new TypeToken<List<Structure.BlockDef>>() {
        }.getType();
        gsonBuilder.registerTypeAdapter(blockDefList, new BlockDefListSerializer());

        gsonBuilder.registerTypeAdapter(Vec.class, new VecSerializer());
        gsonBuilder.registerTypeAdapter(Block.class, new BlockSerializer());

        this.gson = gsonBuilder.create();
    }

    public void writeStructure(Structure structure, Path filePath) {
        var output = this.gson.toJson(structure);
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

    public Structure readStructure(Path p) {
        try {
            Reader reader = Files.newBufferedReader(p);

            return gson.fromJson(reader, Structure.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class VecSerializer implements JsonSerializer<Vec>, JsonDeserializer<Vec> {
        public JsonElement serialize(Vec vec, Type typeOfSRc, JsonSerializationContext context) {
            return context.serialize(new int[]{vec.blockX(), vec.blockY(), vec.blockZ()});
        }

        public Vec deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonArray sizeArr = json.getAsJsonArray();
            var sizeList = new ArrayList<Integer>();
            for (JsonElement elem : sizeArr) {
                sizeList.add(elem.getAsInt());
            }
            if (sizeList.size() != 3) {
                throw new JsonParseException("expected the size to be an array of 3 elements");
            }
            return new Vec(sizeList.get(0), sizeList.get(1), sizeList.get(2));
        }
    }

    private static class BlockDefListSerializer implements JsonSerializer<List<Structure.BlockDef>>, JsonDeserializer<List<Structure.BlockDef>> {
        public JsonElement serialize(List<Structure.BlockDef> blockDefList, Type typeOfSRc, JsonSerializationContext context) {

            return context.serialize(blockDefList.stream()
                    .map((blockDef) -> blockDef.blockId() + "," + blockDef.blockCount())
                    .collect(Collectors.joining(";")));
        }


        @Override
        public List<Structure.BlockDef> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String blocks = json.getAsString();
            List<Structure.BlockDef> list = new ArrayList<>();
            for (String s : blocks.split(";")) {
                String[] fields = s.split(",");
                if (fields.length != 2) {
                    throw new JsonParseException("the block definition " + s + " did not have two comma separated fields");
                }
                int blockId = Integer.parseInt(fields[0]);
                int blockCount = Integer.parseInt(fields[1]);
                list.add(new Structure.BlockDef(blockId, blockCount));
            }
            return list;
        }
    }

    private static class BlockMapSerializer implements JsonSerializer<Map<Integer, Block>>, JsonDeserializer<Map<Integer, Block>> {
        public JsonElement serialize(Map<Integer, Block> blockMap, Type typeOfSRc, JsonSerializationContext context) {
            JsonArray arr = new JsonArray();
            int index = 0;
            while (blockMap.containsKey(index)) {
                Block b = blockMap.get(index);
                arr.add(context.serialize(b, Block.class));
                index++;
            }
            return arr;
        }

        @Override
        public Map<Integer, Block> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var blockMapArr = json.getAsJsonArray();
            var resultBlockMap = new HashMap<Integer, Block>();
            resultBlockMap.put(-1, Block.fromNamespaceId("minecraft:air"));

            int index = 0;
            for (JsonElement block : blockMapArr) {
                Block b = context.deserialize(block, Block.class);
                resultBlockMap.put(index, b);

                index++;
            }

            return resultBlockMap;
        }
    }

    private static class BlockSerializer implements JsonSerializer<Block>, JsonDeserializer<Block> {
        public JsonElement serialize(Block block, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.add("block", context.serialize(block.name()));
            if (block.handler() != null) {
                object.add("handler", context.serialize(block.handler().toString()));
            }
            if (block.nbt() != null) {
                object.add("data", context.serialize(block.nbt().toString()));
            }
            return object;
        }

        @Override
        public Block deserialize(JsonElement block, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String blockField;
            String handlerField = "";
            String dataField = "";

            if (block.isJsonObject()) {
                JsonObject blockObj = block.getAsJsonObject();

                blockField = blockObj.get("block").getAsString();

                if (blockObj.get("handler") != null) {
                    handlerField = blockObj.get("handler").getAsString();
                }
                if (blockObj.get("data") != null) {
                    dataField = blockObj.get("data").getAsString();
                }
            } else {
                blockField = block.getAsString();
            }
            var argBlockState = new ArgumentBlockState("blockStateId");
            Block b = argBlockState.parse(blockField);

            if (handlerField.length() > 0) {
                final BlockHandler handler = MinecraftServer.getBlockManager().getHandler(handlerField);
                if (handler == null) {
                    System.out.println(handlerField + " is not a valid handler");
                    return null;
                }
                b = b.withHandler(handler);
            }
            if (dataField.length() > 0) {
                SNBTParser parser = new SNBTParser(new StringReader(dataField));
                try {
                    b = b.withNbt((NBTCompound) parser.parse());
                } catch (NBTException e) {
                    e.printStackTrace();
                }
            }

            return b;
        }
    }
}
