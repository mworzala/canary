package com.mattworzala.canary.server.givemeahome;

import java.nio.file.Path;

public interface StructureReader {
    Structure readStructure(Path p);
}
