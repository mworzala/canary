package com.mattworzala.canary.internal.structure;

import java.nio.file.Path;

public interface StructureWriter {
    void writeStructure(Structure structure, Path filePath);
}
