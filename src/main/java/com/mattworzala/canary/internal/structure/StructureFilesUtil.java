package com.mattworzala.canary.internal.structure;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
     * @return
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
        File structureResourceDirectory = new File(getStructureTopFile());
        return recursiveGetSubFiles(structureResourceDirectory);
    }

    private static List<String> recursiveGetSubFiles(File top) {
        File children[] = top.listFiles();

        List<String> files = new ArrayList<>();

        for (File child : children) {
            if (child.isDirectory()) {
                files.addAll(recursiveGetSubFiles(child).stream().map(s -> child.getName() + "/" + s).collect(Collectors.toList()));
            } else {
                files.add(child.getName());
            }
        }
        return files;
    }


}
