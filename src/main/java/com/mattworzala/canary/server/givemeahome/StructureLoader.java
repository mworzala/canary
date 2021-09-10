package com.mattworzala.canary.server.givemeahome;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentBlockState;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.SNBTParser;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class StructureLoader {

    public static Block parseBlock(JsonElement block) {
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

    public static List<Integer> parseSize(JsonArray sizeArr) {
        var sizeList = new ArrayList<Integer>();
        for (JsonElement elem : sizeArr) {
            sizeList.add(elem.getAsInt());
        }
        return sizeList;
    }

    public static List<Structure.BlockDef> parseBlocks(String blocks) throws Exception {
        List<Structure.BlockDef> list = new ArrayList<>();
        for (String s : blocks.split(";")) {
            Structure.BlockDef blockDef = parseBlockDef(s);
            list.add(blockDef);
        }
        return list;
    }

    public static Structure.BlockDef parseBlockDef(String blockDef) throws Exception {
        String[] fields = blockDef.split(",");
        if (fields.length != 2) {
            throw new Exception("the block definition " + blockDef + " did not have two comma separated fields");
        }
        int blockId = Integer.parseInt(fields[0]);
        int blockCount = Integer.parseInt(fields[1]);
        return new Structure.BlockDef(blockId, blockCount);
    }

    public static Structure parseStructure(Reader reader) {
        JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();

        String id = object.get("id").getAsString();

        JsonArray sizeArr = object.get("size").getAsJsonArray();
        List<Integer> sizeList = parseSize(sizeArr);

        int sizeX = sizeList.get(0);
        int sizeY = sizeList.get(1);
        int sizeZ = sizeList.get(2);

        Structure resultStructure = new Structure(id, sizeX, sizeY, sizeZ);
        resultStructure.putInBlockMap(-1, Block.fromNamespaceId("minecraft:air"));

        var blockMapArr = object.get("blockmap").getAsJsonArray();

        int index = 0;
        for (JsonElement block : blockMapArr) {
            Block b = StructureLoader.parseBlock(block);
            resultStructure.putInBlockMap(index, b);

            index++;
        }

        var blocks = object.get("blocks").getAsString();

        List<Structure.BlockDef> blockDefs;
        try {
            blockDefs = parseBlocks(blocks);
            resultStructure.setBlockDefList(blockDefs);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        int numBlocksDef = 0;
        for (Structure.BlockDef def : blockDefs) {
            numBlocksDef += def.blockCount();
        }

        int totalBlocks = sizeX * sizeY * sizeZ;
        if (numBlocksDef != totalBlocks) {
            System.out.println(numBlocksDef + " blocks were defined, but the size is " + totalBlocks + " blocks");
            return null;
        }


//        int blockIndex = 0;
//        for (Structure.BlockDef def : parsedBlockDefinitions) {
//            Block block = blockMap.get(def.blockId());
//            for (int i = 0; i < def.blockCount(); i++) {
//                resultStructure.setBlock(blockIndex, block);
//                blockIndex++;
//            }
//        }
//        resultStructure.apply(getInstance(), originX, originY, originZ);
        return resultStructure;
    }
}
