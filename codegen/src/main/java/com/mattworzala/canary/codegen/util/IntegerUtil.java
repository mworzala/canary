package com.mattworzala.canary.codegen.util;

public class IntegerUtil {
    private IntegerUtil() {}

    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
