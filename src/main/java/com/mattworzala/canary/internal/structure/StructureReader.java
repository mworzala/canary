package com.mattworzala.canary.internal.structure;

import java.nio.file.Path;

public interface StructureReader {
    Structure readStructure(Path p);
}
