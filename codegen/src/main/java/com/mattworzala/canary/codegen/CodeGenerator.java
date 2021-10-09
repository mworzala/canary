package com.mattworzala.canary.codegen;

import com.squareup.javapoet.JavaFile;

import java.util.List;

public interface CodeGenerator {

    void generate();

    List<JavaFile> getEmittedFiles();
}
