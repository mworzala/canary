package com.mattworzala.canary.codegen.util;

public class StringUtil {
    private StringUtil() {}

    public static String insertPTags(String input) {
        return input.replaceAll("\n", "\n<p>\n");
    }
}
