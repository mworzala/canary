package com.mattworzala.canary.server.structure;

import java.nio.file.Path;

public interface StructureReader {
    Structure readStructure(Path p);
}
