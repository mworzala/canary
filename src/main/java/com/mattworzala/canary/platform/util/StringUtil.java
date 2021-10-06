package com.mattworzala.canary.platform.util;

import com.mattworzala.canary.platform.util.hint.EnvType;
import com.mattworzala.canary.platform.util.hint.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

@Environment(EnvType.PLATFORM)
public class StringUtil {
    @NotNull
    public static String randomString(int length) {
        return "" + ThreadLocalRandom.current().ints(97, 123)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append);
    }
}
