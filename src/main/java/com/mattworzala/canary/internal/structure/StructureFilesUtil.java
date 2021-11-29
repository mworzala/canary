package com.mattworzala.canary.internal.structure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class StructureFilesUtil {
    public static final String STRUCTURE_RES_ENV_VARIABLE = "CANARY_TEST_RESOURCES";
    private static String structureTopFilePath;

    private static String getStructureTopFile() {
        if (structureTopFilePath == null) {
            structureTopFilePath = System.getenv(STRUCTURE_RES_ENV_VARIABLE);
        }
        return structureTopFilePath;
    }

    public static void saveStructureFile(Structure structure, String path) {
        Path outputPath = Paths.get(getStructureTopFile(), path);

        StructureWriter structureWriter = new JsonStructureIO();
        structureWriter.writeStructure(structure, outputPath);

    }

    /**
     * @param file A file path returned by getStructureFiles()
     * @return The structure read from the given file path
     */
    public static Structure structureFromFile(String file) {
        Path path = Paths.get(getStructureTopFile(), file);

        JsonStructureIO jsonStructureIO = new JsonStructureIO();
        return jsonStructureIO.readStructure(path);
    }

    /**
     * @return A list of all the structure files along with their path in the folder defined by CANARY_TEST_RESOURCES
     */
    public static List<String> getStructureFiles() {
        try {
            Path basePath = Paths.get(getStructureTopFile());

            return Files.walk(basePath)
                    .filter(Files::isRegularFile)
                    .map(p -> basePath.relativize(p).toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}